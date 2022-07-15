package l2s.gameserver.network.l2;

import com.google.common.flogger.FluentLogger;
import com.google.common.flogger.StackSize;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import l2s.commons.dbutils.DbUtils;
import l2s.commons.network.ChannelInboundHandler;
import l2s.commons.network.IConnectionState;
import l2s.commons.network.ICrypt;
import l2s.commons.network.IIncomingPacket;
import l2s.gameserver.Config;
import l2s.gameserver.GameServer;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.dao.CharacterDAO;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.CharSelectInfoPackage;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.authcomm.AuthServerClientService;
import l2s.gameserver.network.authcomm.SessionKey;
import l2s.gameserver.network.authcomm.gs2as.PlayerLogout;
import l2s.gameserver.network.floodprotector.FloodProtector;
import l2s.gameserver.network.floodprotector.config.FloodProtectorConfig;
import l2s.gameserver.network.floodprotector.config.FloodProtectorConfigs;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.IClientOutgoingPacket;
import l2s.gameserver.network.l2.s2c.LogOutOkPacket;
import l2s.gameserver.network.l2.s2c.ServerClose;
import l2s.gameserver.security.HWIDUtils;
import l2s.gameserver.security.SecondaryPasswordAuth;
import l2s.gameserver.utils.Language;
import org.strixplatform.StrixPlatform;
import org.strixplatform.network.IStrixClientData;
import org.strixplatform.network.cipher.StrixGameCrypt;
import org.strixplatform.utils.StrixClientData;
import smartguard.core.properties.GuardProperties;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.flogger.LazyArgs.lazy;

/**
 * Represents a client connected on Game Server
 */
public final class GameClient extends ChannelInboundHandler<GameClient> implements IStrixClientData
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	private final Map<String, FloodProtector> _floodProtectors = new HashMap<String, FloodProtector>();

	private InetAddress _addr;
	private Channel _channel;
	private Player _activeChar;
	private SessionKey _sessionKey;

	private int revision = 0;

	private SecondaryPasswordAuth _secondaryAuth = null;

	private List<Integer> _charSlotMapping = new ArrayList<Integer>();

	private String _hwid = null;

	private final ICrypt _crypt;

	private volatile boolean _isDetached = false;

	private boolean isAuthed = false;

	/** Данные аккаунта */
	private String _login;
	private int _premiumAccountType = 0;
	private int _premiumAccountExpire;
	private int _points = 0;
	private Language _language = Config.DEFAULT_LANG;
	private long _phoneNumber = 0L;

	private StrixClientData clientData;

    public GameClient()
	{
		// SmartGuard
		if (GuardProperties.ProtectionEnabled)
			_crypt = new GameCryptSmartGuard();
		else if (StrixPlatform.getInstance().isPlatformEnabled()) {
			_crypt = new StrixGameCrypt();
		}
		else
			_crypt = new GameCrypt();
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx)
	{
		super.channelActive(ctx);

		setConnectionState(ConnectionState.CONNECTED);
		final InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
		_addr = address.getAddress();
		_channel = ctx.channel();

		for(FloodProtectorConfig config : FloodProtectorConfigs.FLOOD_PROTECTORS)
			_floodProtectors.put(config.FLOOD_PROTECTOR_TYPE, new FloodProtector(this, config));

		_log.atFine().log( "Client Connected: %s", lazy(() -> ctx.channel()) );
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx)
	{
		_log.atFine().log( "Client Disconnected: %s", lazy(() -> ctx.channel()) );

		final Player player;

		setConnectionState(ConnectionState.DISCONNECTED);
		player = getActiveChar();
		setActiveChar(null);

		if(player != null)
		{
			player.setNetConnection(null);
			player.scheduleDelete();
		}

		if(getSessionKey() != null)
		{
			if(isAuthed())
			{
				AuthServerClientService.INSTANCE.removeAuthedClient(getLogin());
				GameServer.getInstance().getAuthServerCommunication().sendPacket(new PlayerLogout(getLogin()));
			}
			else
			{
				AuthServerClientService.INSTANCE.removeWaitingClient(getLogin());
			}
		}
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, IIncomingPacket<GameClient> packet)
	{
		ThreadPoolManager.getInstance().execute(() -> {
			try
			{
				packet.run(GameClient.this);
			}
			catch (Exception e)
			{
				_log.atWarning().withStackTrace(StackSize.FULL)
						.withCause(e)
						.log("Exception for: %s on packet.run: %s", toString(), packet.getClass().getSimpleName());
			}
		});
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
	{
		if (cause instanceof IOException) {
			if (cause.getMessage().equals("Connection reset by peer")) {
				_log.atFine().withStackTrace(StackSize.FULL).withCause(cause).log("Network exception caught for: %s", toString());
				ctx.close();
				return;
			}

			if (cause.getMessage().equals("An existing connection was forcibly closed by the remote host")) {
				_log.atFine().withStackTrace(StackSize.FULL).withCause(cause).log("Network exception caught for: %s", toString());
				ctx.close();
				return;
			}
		}

		_log.atWarning().withStackTrace(StackSize.FULL).withCause(cause).log("Network exception caught for: %s", toString());
	}

	public boolean isConnected()
	{
		final Channel conn = _channel;
		return conn != null && conn.isActive();
	}

	public void markRestoredChar(int charslot) throws Exception
	{
		int objid = getObjectIdForSlot(charslot);
		if(objid < 0)
			return;

		if(_activeChar != null && _activeChar.getObjectId() == objid)
			_activeChar.setDeleteTimer(0);

		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE characters SET deletetime=0 WHERE obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.atSevere().withStackTrace(StackSize.FULL).withCause(e).log( "" );
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void markToDeleteChar(int charslot) throws Exception
	{
		int objid = getObjectIdForSlot(charslot);
		if(objid < 0)
			return;

		if(_activeChar != null && _activeChar.getObjectId() == objid)
			_activeChar.setDeleteTimer((int) (System.currentTimeMillis() / 1000));

		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE characters SET deletetime=? WHERE obj_id=?");
			statement.setLong(1, (int) (System.currentTimeMillis() / 1000L));
			statement.setInt(2, objid);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.atSevere().withStackTrace(StackSize.FULL).withCause(e).log( "data error on update deletime char:" );
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void deleteChar(int charslot) throws Exception
	{
		//have to make sure active character must be nulled
		if(_activeChar != null)
			return;

		int objid = getObjectIdForSlot(charslot);
		if(objid == -1)
			return;

		CharacterDAO.getInstance().deleteCharByObjId(objid);
	}

	public Player loadCharFromDisk(int charslot)
	{
		int objectId = getObjectIdForSlot(charslot);
		if(objectId == -1)
			return null;

		Player character = null;
		Player oldPlayer = GameObjectsStorage.getPlayer(objectId);

		if(oldPlayer != null)
		{
			if(oldPlayer.isInOfflineMode() || oldPlayer.isLogoutStarted())
			{
				// оффтрейдового чара проще выбить чем восстанавливать
				oldPlayer.kick();
			}
			else
			{
				oldPlayer.sendPacket(SystemMsg.ANOTHER_PERSON_HAS_LOGGED_IN_WITH_THE_SAME_ACCOUNT);

				GameClient oldClient = oldPlayer.getNetConnection();
				if(oldClient != null)
				{
					oldClient.setActiveChar(null);
					oldClient.closeNow();
				}
				oldPlayer.setNetConnection(this);
				character = oldPlayer;
			}
		}

		if(character == null)
			character = Player.restore(objectId, false);

		if(character != null)
			setActiveChar(character);
		else
			_log.atWarning().log( "could not restore obj_id: %s in slot:%s", objectId, charslot );

		return character;
	}

	public int getObjectIdForSlot(int charslot)
	{
		if(charslot < 0 || charslot >= _charSlotMapping.size())
		{
			_log.atWarning().log( "%s tried to modify Character in slot %s but no characters exits at that slot.", getLogin(), charslot );
			return -1;
		}
		return _charSlotMapping.get(charslot);
	}

	public Player getActiveChar()
	{
		return _activeChar;
	}

	/**
	 * @return Returns the sessionId.
	 */
	public SessionKey getSessionKey()
	{
		return _sessionKey;
	}

	public String getLogin()
	{
		return _login;
	}

	public ICrypt getCrypt()
	{
		return _crypt;
	}

	public void setLoginName(String loginName)
	{
		_login = loginName;

		if(Config.EX_SECOND_AUTH_ENABLED)
			_secondaryAuth = new SecondaryPasswordAuth(this);
	}

	public void setActiveChar(Player player)
	{
		_activeChar = player;
		if(player != null)
			player.setNetConnection(this);
	}

	public void setSessionId(SessionKey sessionKey)
	{
		_sessionKey = sessionKey;
	}

	public void setCharSelection(CharSelectInfoPackage[] chars)
	{
		_charSlotMapping.clear();

		for(CharSelectInfoPackage element : chars)
		{
			int objectId = element.getObjectId();
			_charSlotMapping.add(objectId);
		}
	}

	public void setCharSelection(int c)
	{
		_charSlotMapping.clear();
		_charSlotMapping.add(c);
	}

	public int getRevision()
	{
		return revision;
	}

	public void setRevision(int revision)
	{
		this.revision = revision;
	}

	public boolean checkFloodProtection(String type, String command)
	{
		FloodProtector floodProtector = _floodProtectors.get(type.toUpperCase());
		if (floodProtector == null) {
			floodProtector = _floodProtectors.get("DEFAULT");
		}
		return floodProtector == null || floodProtector.tryPerformAction(command);
	}

	public void sendPacket(IClientOutgoingPacket packet)
	{
		if (_isDetached || packet == null)
		{
			return;
		}

		// Write into the channel.
		_channel.writeAndFlush(packet);

		// Run packet implementation.
		packet.runImpl(getActiveChar());
	}

	public void closeNow()
	{
		if (_channel != null)
		{
			_channel.close();
		}
	}

	public void close(IClientOutgoingPacket packet)
	{
		sendPacket(packet);
		closeNow();
	}

	public void close(boolean toLoginScreen)
	{
		close(toLoginScreen ? ServerClose.STATIC_PACKET : LogOutOkPacket.STATIC);
	}

	/**
	 * For loaded offline traders returns localhost address.
	 * @return cached connection IP address, for checking detached clients.
	 */
	public InetAddress getConnectionAddress()
	{
		return _addr;
	}

	public String getIpAddr()
	{
		return _addr.getHostAddress();
	}

	public byte[] enableCrypt()
	{
		// SmartGuard
		byte[] key;
		if (GuardProperties.ProtectionEnabled)
			key = BlowFishKeygenSmartGuard.getRandomKey();
		else
			key = BlowFishKeygen.getRandomKey();
		_crypt.setKey(key);
		return key;
	}

	public boolean hasPremiumAccount()
	{
		return _premiumAccountType != 0 && _premiumAccountExpire > System.currentTimeMillis() / 1000L;
	}

	public void setPremiumAccountType(int type)
	{
		_premiumAccountType = type;
	}

	public int getPremiumAccountType()
	{
		return _premiumAccountType;
	}

	public void setPremiumAccountExpire(int expire)
	{
		_premiumAccountExpire = expire;
	}

	public int getPremiumAccountExpire()
	{
		return _premiumAccountExpire;
	}

	public int getPoints()
	{
		return _points;
	}

	public void setPoints(int points)
	{
		_points = points;
	}

	public Language getLanguage()
	{
		return _language;
	}

	public void setLanguage(Language language)
	{
		_language = language;
	}

	public long getPhoneNumber()
	{
		return _phoneNumber;
	}

	public void setPhoneNumber(long value)
	{
		_phoneNumber = value;
	}

	public SecondaryPasswordAuth getSecondaryAuth()
	{
		return _secondaryAuth;
	}

	@Override
	public String toString()
	{
		try
		{
			final InetAddress address = _addr;
			IConnectionState state = getConnectionState();
			final Player player = getActiveChar();
			if (ConnectionState.CONNECTED.equals(state)) {
				return "[IP: " + (address == null ? "disconnected" : address.getHostAddress()) + "]";
			} else if (ConnectionState.AUTHENTICATED.equals(state)) {
				return "[Account: " + getLogin() + " - IP: " + (address == null ? "disconnected" : address.getHostAddress()) + "]";
			} else if (ConnectionState.IN_GAME.equals(state) || ConnectionState.JOINING_GAME.equals(state)) {
				return "[Character: " + (player == null ? "disconnected" : player)
						+ " - Account: " + getLogin()
						+ " - IP: " + (address == null ? "disconnected" : address.getHostAddress()) + "]";
			}
			throw new IllegalStateException("Missing state on switch: " + state);
		}
		catch (NullPointerException e)
		{
			return "[Character read failed due to disconnect]";
		}
	}

	public boolean secondaryAuthed()
	{
		if(!Config.EX_SECOND_AUTH_ENABLED)
			return true;

		return getSecondaryAuth().isAuthed();
	}

	public String getHWID()
	{
		if (clientData != null) {
			return clientData.getClientHWID();
		}

		return _hwid;
	}

	public void setHWID(String hwid)
	{
		_hwid = hwid;
	}

	public void checkHwid(String allowedHwid) 
	{
		HWIDUtils.checkHWID(this, allowedHwid);
	}

	public boolean isAuthed() {
		return isAuthed;
	}

	public void setAuthed(boolean authed) {
		isAuthed = authed;
	}

	//TODO[K] - Guard section start
	@Override
	public void setStrixClientData(final StrixClientData clientData)
	{
		this.clientData = clientData;
	}

	@Override
	public StrixClientData getStrixClientData()
	{
		return clientData;
	}
	//TODO[K] - Guard section end
}