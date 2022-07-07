package l2s.gameserver.network.l2;

import com.mmobite.as.api.AntispamAPI;
import com.mmobite.as.api.model.Direction;
import com.mmobite.as.api.model.GameSessionInfo;
import com.mmobite.as.api.model.NetworkSessionInfo;
import l2s.commons.dbutils.DbUtils;
import l2s.commons.net.nio.impl.MMOClient;
import l2s.commons.net.nio.impl.MMOConnection;
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
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.components.hwid.HwidHolder;
import l2s.gameserver.network.l2.s2c.ActionFailPacket;
import l2s.gameserver.network.l2.s2c.CharacterSelectedPacket;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.NetPingPacket;
import l2s.gameserver.punishment.PunishmentService;
import l2s.gameserver.punishment.PunishmentType;
import l2s.gameserver.security.SecondaryPasswordAuth;
import l2s.gameserver.utils.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

public final class GameClient extends MMOClient<MMOConnection<GameClient>>
{
	private static final Logger _log = LoggerFactory.getLogger(GameClient.class);
	private static final String NO_IP = "?.?.?.?";
	public GameCrypt _crypt = null;
	public GameClientState _state;
	private String _login;
	private int _premiumAccountType = 0;
	private int _premiumAccountExpire;
	private int _points = 0;

    private int selectedIndex = -1;
	private Language _language = Config.DEFAULT_LANG;
    private SecondaryPasswordAuth _secondaryAuth = null;
    private final List<Integer> _charSlotMapping = new ArrayList<>();

	private Player _activeChar;
	private SessionKey _sessionKey;
	private String _ip = NO_IP;
	private int revision = 0;

	private HwidHolder hwidHolder = null;
	private int _failedPackets = 0;
	private int _unknownPackets = 0;

	private final Instant dropIfNotAcceptTime;

	private long antispamSession;

	public GameClient(MMOConnection<GameClient> con)
	{
		super(con);
		_state = GameClientState.CONNECTED;
		_ip = con.getSocket().getInetAddress().getHostAddress();
		_crypt = new GameCrypt();
        dropIfNotAcceptTime = Instant.now().plusSeconds(Config.ACCEPT_TIME.toSeconds());
		setAntispamSession(AntispamAPI.openGameSession(new NetworkSessionInfo()));
	}

	@Override
	public boolean isValid()
	{
		if(_state == GameClientState.DISCONNECTED) {
            return false;
        }

		if (_state == GameClientState.CONNECTED) {
			return Instant.now().isBefore(dropIfNotAcceptTime);
		}

		return true;
	}

	@Override
	protected void onDisconnection()
	{
		if(_pingTaskFuture != null)
		{
			_pingTaskFuture.cancel(true);
			_pingTaskFuture = null;
		}

		setState(GameClientState.DISCONNECTED);
		Player player = getActiveChar();
		setActiveChar(null);
		if(player != null)
		{
			player.setNetConnection(null);
			player.scheduleDelete();
		}
		if(getSessionKey() != null)
			if(isAuthed())
			{
				AuthServerClientService.INSTANCE.removeAuthedClient(getLogin());
				GameServer.getInstance().getAuthServerCommunication().sendPacket(new PlayerLogout(getLogin()));
			}
			else
				AuthServerClientService.INSTANCE.removeWaitingClient(getLogin());

		AntispamAPI.closeGameSession(antispamSession);
	}

	@Override
	protected void onForcedDisconnection()
	{}

	public void markRestoredChar(int charslot) throws Exception
	{
		int objid = getObjectIdByIndex(charslot);

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
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void markToDeleteChar(int charslot) throws Exception
	{
		int objid = getObjectIdByIndex(charslot);

		if(objid < 0)
			return;

		if(_activeChar != null && _activeChar.getObjectId() == objid)
			_activeChar.setDeleteTimer((int) (System.currentTimeMillis() / 1000L));

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
			_log.error("data error on update deletime char:", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void deleteChar(int charslot) throws Exception
	{
		if(_activeChar != null)
			return;

		int objid = getObjectIdByIndex(charslot);

		if(objid == -1)
			return;

		CharacterDAO.getInstance().deleteCharByObjId(objid);
	}

	public void playerSelected(int index)
	{
        int objectIdByIndex = getObjectIdByIndex(index);
        if(objectIdByIndex <= 0 || getActiveChar() != null
				|| PunishmentService.INSTANCE.isPunished(PunishmentType.CHARACTER, String.valueOf(objectIdByIndex)))
        {
            sendPacket(ActionFailPacket.STATIC);
            return;
        }

        selectedIndex = index;

        if(!secondaryAuthed())
        {
            sendPacket(ActionFailPacket.STATIC);
            return;
        }

        Player noCarrierPlayer = null;
        for(int i = 0; i < _charSlotMapping.size(); i++)
        {
            Integer objectId = _charSlotMapping.get(i);
            Player player = objectId != null ? GameObjectsStorage.getPlayer(objectId) : null;
            if(player == null)
                continue;

            // если у нас чар в оффлайне, или выходит, и этот чар выбран - кикаем
            if(player.isInOfflineMode() || player.isLogoutStarted())
            {
                if(index == i)
                    player.kick();
            }
            else
            {
                player.sendPacket(SystemMsg.ANOTHER_PERSON_HAS_LOGGED_IN_WITH_THE_SAME_ACCOUNT);

                // если есть чар
                // если это выбраный - обнуляем конект и используем, иначе кикаем
                if(index == i)
                {
                    noCarrierPlayer = player;

                    GameClient oldClient = player.getNetConnection();
                    if(oldClient != null)
                    {
                        oldClient.setActiveChar(null);
                        oldClient.closeNow(false);
                    }

                    // обновляем статы при использовании старого чара - защита от эксплоитов
                    noCarrierPlayer.getInventory().writeLock();
                    try
                    {
                        noCarrierPlayer.getInventory().refreshEquip();
                    }
                    finally
                    {
                        noCarrierPlayer.getInventory().writeUnlock();
                    }
                    noCarrierPlayer.getHennaList().refreshStats(true);
                    noCarrierPlayer.updateStats();
                }
                else
                    player.logout();
            }
        }

        Player selectedPlayer = noCarrierPlayer == null ? Player.restore(objectIdByIndex, hwidHolder) : noCarrierPlayer;
        if(selectedPlayer == null)
        {
            sendPacket(ActionFailPacket.STATIC);
            return;
        }

        if(selectedPlayer.getAccessLevel() < 0)
            selectedPlayer.setAccessLevel(0);

        setActiveChar(selectedPlayer);
        setState(GameClientState.IN_GAME);

        sendPacket(new CharacterSelectedPacket(selectedPlayer, getSessionKey().playOkID1));

		// Antispam block
		GameSessionInfo gameSessionInfo = new GameSessionInfo();
		gameSessionInfo.account_name = getLogin();
		gameSessionInfo.character_name = selectedPlayer.getName();
		gameSessionInfo.hwid = getHwidString();
		gameSessionInfo.char_dbid = selectedPlayer.getObjectId();
		gameSessionInfo.account_dbid = selectedPlayer.getObjectId(); //TODO: same as char_dbid
		gameSessionInfo.online_time = selectedPlayer.getOnlineTime() / 1000;
		gameSessionInfo.char_level = selectedPlayer.getLevel();
		AntispamAPI.sendGameSessionInfo(getAntispamSession(), gameSessionInfo);
	}

	public int getObjectIdByIndex(int charslot)
	{
		if(charslot < 0 || charslot >= _charSlotMapping.size())
		{
			_log.warn(getLogin() + " tried to modify Character in slot " + charslot + " but no characters exits at that slot.");
			return -1;
		}

		return _charSlotMapping.get(charslot);
	}

	public Player getActiveChar()
	{
		return _activeChar;
	}

	public SessionKey getSessionKey()
	{
		return _sessionKey;
	}

	public String getLogin()
	{
		return _login;
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
		{
			player.setNetConnection(this);
		}
	}

	public void setSessionId(SessionKey sessionKey)
	{
		_sessionKey = sessionKey;
	}

	public List<Integer> getCharSlotMapping()
	{
		return _charSlotMapping;
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

	@Override
	public boolean encrypt(ByteBuffer buf, int size)
	{
        AntispamAPI.sendPacketData(antispamSession, Direction.gameclient.value, buf.array(), buf.position(), size);

        _crypt.encrypt(buf.array(), buf.position(), size);
		buf.position(buf.position() + size);
		return true;
	}

	@Override
	public boolean decrypt(ByteBuffer buf, int size)
	{
		boolean success = _crypt.decrypt(buf.array(), buf.position(), size);
        AntispamAPI.sendPacketData(antispamSession, Direction.clientgame.value, buf.array(), buf.position(), size);
		return success;
	}

	public void sendPacket(L2GameServerPacket gsp)
	{
		//		System.out.println("server: " + gsp.getClass().getName());
		if(isConnected())
			getConnection().sendPacket(gsp);
	}

	public void sendPacket(L2GameServerPacket... gsp)
	{
		//		for(L2GameServerPacket gspp : gsp)
		//			System.out.println("server: " + gspp.getClass().getName());

		if(isConnected())
			getConnection().sendPacket(gsp);
	}

	public void sendPackets(List<L2GameServerPacket> gsp)
	{
		//		for(L2GameServerPacket gspp : gsp)
		//			System.out.println("server: " + gspp.getClass().getName());

		if(isConnected())
			getConnection().sendPackets(gsp);
	}

	public void close(L2GameServerPacket gsp)
	{
		if(isConnected())
			getConnection().close(gsp);
	}

	public String getIpAddr()
	{
		return _ip;
	}

	public byte[] enableCrypt()
	{
		byte[] key = BlowFishKeygen.getRandomKey();
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

    public int getSelectedIndex()
    {
        return selectedIndex;
    }

    public Language getLanguage()
	{
		return _language;
	}

	public void setPoints(int points)
	{
		_points = points;
	}

	public void setLanguage(Language language)
	{
		_language = language;
	}

	public GameClientState getState()
	{
		return _state;
	}

	public void setState(GameClientState state)
	{
		_state = state;
		if(state == GameClientState.AUTHED)
			doPing();
	}

	public SecondaryPasswordAuth getSecondaryAuth()
	{
		return _secondaryAuth;
	}

	public void onPacketReadFail()
	{
		if(_failedPackets++ >= 10)
		{
			_log.warn("Too many client packet fails, connection closed : " + this);
			closeNow(true);
		}
	}

	public void onUnknownPacket()
	{
		if(_unknownPackets++ >= 10)
		{
			_log.warn("Too many client unknown packets, connection closed : " + this);
			closeNow(true);
		}
	}

	@Override
	public String toString()
	{
		return _state + " IP: " + getIpAddr() + (_login == null ? "" : " Account: " + _login) + (_activeChar == null ? "" : " Player : " + _activeChar);
	}

	public boolean secondaryAuthed()
	{
		return !Config.EX_SECOND_AUTH_ENABLED || getSecondaryAuth().isAuthed();
	}

	public void setHwidHolder(HwidHolder hwidHolder)
	{
		this.hwidHolder = hwidHolder;
	}

	public HwidHolder getHwidHolder()
	{
		return hwidHolder;
	}

	public String getHwidString()
	{
		return hwidHolder.asString();
	}

	public enum GameClientState
	{
		CONNECTED,
		AUTHED,
		IN_GAME,
		DISCONNECTED
    }

	public static int DEFAULT_PAWN_CLIPPING_RANGE = 2048;
	private int _ping = 0;
	private int _fps = 0;
	private int _pawnClippingRange = 0;
	private int _pingTimestamp = 0;
	private ScheduledFuture<?> _pingTaskFuture;

	public int getPing()
	{
		return _ping;
	}

	public int getFps()
	{
		return _fps;
	}

	public int getPawnClippingRange()
	{
		return _pawnClippingRange;
	}

	public void setAntispamSession(long id)
	{
		antispamSession = id;
	}

	public long getAntispamSession()
	{
		return antispamSession;
	}

	public void onPing(int timestamp, int fps, int pawnClipRange)
	{
		if(_pingTimestamp == 0 || _pingTimestamp == timestamp)
		{
			long nowMs = System.currentTimeMillis();
			long serverStartTimeMs = GameServer.getInstance().getServerStartTime();

			_ping = _pingTimestamp > 0 ? (int) (nowMs - serverStartTimeMs - timestamp) : 0;
			_fps = fps;
			_pawnClippingRange = pawnClipRange;
			_pingTaskFuture = ThreadPoolManager.getInstance().schedule(new PingTask(this), 30000);
		}
	}

	private final void doPing()
	{
		long nowMs = System.currentTimeMillis();
		long serverStartTimeMs = GameServer.getInstance().getServerStartTime();

		_pingTimestamp = (int) (nowMs - serverStartTimeMs);
		sendPacket(new NetPingPacket(_pingTimestamp));
	}

	private static class PingTask implements Runnable
	{
		private final GameClient _client;

		private PingTask(GameClient client)
		{
			_client = client;
		}

		@Override
		public void run()
		{
			if(_client == null || !_client.isConnected())
				return;

			_client.doPing();
		}
	}
}
