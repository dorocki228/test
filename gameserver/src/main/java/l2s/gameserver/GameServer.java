package l2s.gameserver;

import com.google.common.flogger.FluentLogger;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import l2s.commons.lang.StatsUtils;
import l2s.commons.listener.Listener;
import l2s.commons.listener.ListenerList;
import l2s.commons.net.HostInfo;
import l2s.commons.net.nio.impl.SelectorStats;
import l2s.commons.versioning.Version;
import l2s.gameserver.cache.CrestCache;
import l2s.gameserver.cache.ImagesCache;
import l2s.gameserver.config.xml.ConfigParsers;
import l2s.gameserver.config.xml.holder.HostsConfigHolder;
import l2s.gameserver.config.xml.holder.VoteRewardConfigHolder;
import l2s.gameserver.dao.*;
import l2s.gameserver.data.BoatHolder;
import l2s.gameserver.data.xml.Parsers;
import l2s.gameserver.data.xml.holder.EventHolder;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.data.xml.holder.StaticObjectHolder;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.database.UpdatesInstaller;
import l2s.gameserver.features.foursepulchers.FourSepulchersManager;
import l2s.gameserver.features.huntingzones.HuntingZonesService;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.handler.admincommands.AdminCommandHandler;
import l2s.gameserver.handler.bbs.BbsHandlerHolder;
import l2s.gameserver.handler.bypass.BypassHolder;
import l2s.gameserver.handler.dailymissions.DailyMissionHandlerHolder;
import l2s.gameserver.handler.effects.EffectHandlerHolder;
import l2s.gameserver.handler.items.ItemHandler;
import l2s.gameserver.handler.onshiftaction.OnShiftActionHolder;
import l2s.gameserver.handler.skillconditions.SkillConditionHandler;
import l2s.gameserver.handler.usercommands.UserCommandHandler;
import l2s.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.instancemanager.*;
import l2s.gameserver.instancemanager.clansearch.ClanSearchManager;
import l2s.gameserver.instancemanager.games.MiniGameScoreManager;
import l2s.gameserver.listener.GameListener;
import l2s.gameserver.listener.game.OnShutdownListener;
import l2s.gameserver.listener.game.OnStartListener;
import l2s.gameserver.model.World;
import l2s.gameserver.model.entity.Hero;
import l2s.gameserver.model.entity.MonsterRace;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.network.authcomm.vertx.AuthServerCommunication;
import l2s.gameserver.network.floodprotector.config.FloodProtectorConfigs;
import l2s.gameserver.network.l2.ClientNetworkManager;
import l2s.gameserver.network.telnet.TelnetServer;
import l2s.gameserver.scripts.Scripts;
import l2s.gameserver.security.HWIDBan;
import l2s.gameserver.tables.ClanTable;
import l2s.gameserver.tables.EnchantHPBonusTable;
import l2s.gameserver.tables.FakePlayersTable;
import l2s.gameserver.tables.SubClassTable;
import l2s.gameserver.taskmanager.AutomaticTasks;
import l2s.gameserver.taskmanager.ItemsAutoDestroy;
import l2s.gameserver.utils.OnlineTxtGenerator;
import l2s.gameserver.utils.Strings;
import l2s.gameserver.utils.TradeHelper;
import l2s.gameserver.utils.velocity.VelocityUtils;

import java.awt.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import static com.google.common.flogger.LazyArgs.lazy;

public class GameServer
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	public static boolean DEVELOP = false;

	public static final String PROJECT_REVISION = "L2s [33006]";
	public static final String UPDATE_NAME = "Classic: Saviors (Secret of Empire)";

	public static final int AUTH_SERVER_PROTOCOL = 4;

	public static class GameServerListenerList extends ListenerList<GameServer>
	{
		public void onStart()
		{
			for(Listener<GameServer> listener : getListeners())
				if(OnStartListener.class.isInstance(listener))
					((OnStartListener) listener).onStart();
		}

		public void onShutdown()
		{
			for(Listener<GameServer> listener : getListeners())
				if(OnShutdownListener.class.isInstance(listener))
					((OnShutdownListener) listener).onShutdown();
		}
	}

	public static GameServer _instance;

	private final SelectorStats _selectorStats = new SelectorStats();
	private Version version;
	private TelnetServer statusServer;
	private final GameServerListenerList _listeners;
	private final AuthServerCommunication authServerCommunication;
	private final Vertx vertx;

	private long _serverStartTimeMillis;

	private final String _licenseHost;
	private final int _onlineLimit;

	public SelectorStats getSelectorStats()
	{
		return _selectorStats;
	}

	public Vertx getVertx() {
		return vertx;
	}

	public AuthServerCommunication getAuthServerCommunication() {
		return authServerCommunication;
	}

	public long getServerStartTime()
	{
		return _serverStartTimeMillis;
	}

	public String getLicenseHost()
	{
		return _licenseHost;
	}

	public int getOnlineLimit()
	{
		return _onlineLimit;
	}

	@SuppressWarnings("unchecked")
	public GameServer() throws Exception
	{
		_instance = this;
		_serverStartTimeMillis = System.currentTimeMillis();
		_listeners = new GameServerListenerList();

		VertxOptions vertxOptions = new VertxOptions()
				.setInternalBlockingPoolSize(2)
				.setWorkerPoolSize(2)
				.setEventLoopPoolSize(2);
		vertx = Vertx.vertx(vertxOptions);

		version = new Version(GameServer.class);

		_log.atInfo().log( "=================================================" );
		_log.atInfo().log( "Project Revision: ........ %s", PROJECT_REVISION );
		_log.atInfo().log( "Build Revision: .......... %s", lazy(() -> version.getRevisionNumber()) );
		_log.atInfo().log( "Update: .................. %s", UPDATE_NAME );
		_log.atInfo().log( "Build date: .............. %s", lazy(() -> version.getBuildDate()) );
		_log.atInfo().log( "Compiler version: ........ %s", lazy(() -> version.getBuildJdk()) );
		_log.atInfo().log( "=================================================" );

		// Initialize config
		ConfigParsers.parseAllOnLoad();
		Config.load();
		FloodProtectorConfigs.load();
		VelocityUtils.init();

		final HostInfo[] hosts = HostsConfigHolder.getInstance().getGameServerHosts();
		if(hosts.length == 0)
		{
			throw new Exception("Server hosts list is empty!");
		}

		final TIntSet ports = new TIntHashSet();
		for(HostInfo host : hosts)
		{
			if(host.getAddress() != null)
			{
				while(!checkFreePort(host.getAddress(), host.getPort()))
				{
					_log.atWarning().log( "Port \'%s\' on host \'%s\' is allready binded. Please free it and restart server.", host.getPort(), host.getAddress() );
					try
					{
						Thread.sleep(1000L);
					}
					catch(InterruptedException e2)
					{
						//
					}
				}
				ports.add(host.getPort());
			}
		}

		final int[] portsArray = ports.toArray();

		if(portsArray.length == 0)
		{
			throw new Exception("Server ports list is empty!");
		}

		_licenseHost = Config.EXTERNAL_HOSTNAME;
		_onlineLimit = Config.MAXIMUM_ONLINE_USERS;

		if(_onlineLimit == 0)
		{
			throw new Exception("Server online limit is zero!");
		}

		// Initialize database
		DatabaseFactory.getInstance().getConnection().close();

		UpdatesInstaller.checkAndInstall();

		IdFactory _idFactory = IdFactory.getInstance();
		if(!_idFactory.isInitialized())
		{
			_log.atSevere().log( "Could not read object IDs from DB. Please Check Your Data." );
			throw new Exception("Could not initialize the ID factory");
		}

		ThreadPoolManager.getInstance();

		BotCheckManager.loadBotQuestions();

		HidenItemsDAO.LoadAllHiddenItems();

		CustomHeroDAO.getInstance();

		HWIDBan.getInstance().load();

		ItemHandler.getInstance();

		DailyMissionHandlerHolder.getInstance();

		Scripts.getInstance();

		GeoEngine.load();

		Strings.reload();

		GameTimeController.getInstance();

		World.init();

		Parsers.parseAll();
		HuntingZonesService.INSTANCE.load();

		SkillConditionHandler.getInstance();

		EffectHandlerHolder.getInstance();

		ItemsDAO.getInstance();

		ThreadPoolManager.getInstance().execute(() ->{
			CrestCache.getInstance();
			ImagesCache.getInstance();
		});

		CharacterDAO.getInstance();

		ClanTable.getInstance();

		SubClassTable.getInstance();

		EnchantHPBonusTable.getInstance();

		FencesDAO.getInstance().restore();

		StaticObjectHolder.getInstance().spawnAll();

		SpawnManager.getInstance().spawnAll();

		RaidBossSpawnManager.getInstance();

		ConfigParsers.parseAllOnInit();

		Scripts.getInstance().init();

		Announcements.getInstance();

		PlayerMessageStack.getInstance();

		if(Config.AUTODESTROY_ITEM_AFTER > 0)
			ItemsAutoDestroy.getInstance();

		MonsterRace.getInstance();

		if(Config.ENABLE_OLYMPIAD)
		{
			Olympiad.load();
			Hero.getInstance();
		}

		PetitionManager.getInstance();

		if(Config.ALLOW_WEDDING)
			CoupleManager.getInstance();

		AdminCommandHandler.getInstance().log();
		UserCommandHandler.getInstance().log();
		VoicedCommandHandler.getInstance().log();
		BbsHandlerHolder.getInstance().log();
		BypassHolder.getInstance().log();
		OnShiftActionHolder.getInstance().log();

		AutomaticTasks.init();

		ClanTable.getInstance().checkClans();

		_log.atInfo().log( "=[Events]=========================================" );
		ResidenceHolder.getInstance().callInit();
		EventHolder.getInstance().callInit();
		_log.atInfo().log( "==================================================" );

		BoatHolder.getInstance().spawnAll();

		Runtime.getRuntime().addShutdownHook(Shutdown.getInstance());

		_log.atInfo().log( "IdFactory: Free ObjectID\'s remaining: %s", lazy(() -> IdFactory.getInstance().size()) );

		MiniGameScoreManager.getInstance();

		ClanSearchManager.getInstance().load();

		BotReportManager.getInstance();

		TrainingCampManager.getInstance().init();

		FourSepulchersManager.Companion.init();

		VoteRewardConfigHolder.getInstance().callInit();

		Shutdown.getInstance().schedule(Config.RESTART_AT_TIME, Shutdown.RESTART);

		_log.atInfo().log( "GameServer Started" );
		_log.atInfo().log( "Maximum Numbers of Connected Players: %s", lazy(() -> getOnlineLimit()) );

		GameBanManager.getInstance().init();

		registerSelectorThreads(ports);

		getListeners().onStart();

		if(Config.BUFF_STORE_ENABLED)
		{
			_log.atInfo().log( "Restoring offline buffers..." );
			int count = TradeHelper.restoreOfflineBuffers();
			_log.atInfo().log( "Restored %s offline buffers.", count );
		}

		if(Config.SERVICES_OFFLINE_TRADE_RESTORE_AFTER_RESTART)
		{
			_log.atInfo().log( "Restoring offline traders..." );
			int count = TradeHelper.restoreOfflineTraders();
			_log.atInfo().log( "Restored %s offline traders.", count );
		}

		if (Config.ALLOW_FAKE_PLAYERS || Config.FAKE_PLAYERS_COUNT > 0) {
			FakePlayersTable.getInstance();
		}

		if(Config.ONLINE_GENERATOR_ENABLED)
			ThreadPoolManager.getInstance().scheduleAtFixedRate(new OnlineTxtGenerator(), 5000L, Config.ONLINE_GENERATOR_DELAY * 60 * 1000L);

		HostInfo hostInfo = HostsConfigHolder.getInstance().getAuthServerHost();
		InetAddress inetAddress = hostInfo.getAddress().equals("*") ? null : InetAddress.getByName(hostInfo.getAddress());
		authServerCommunication = new AuthServerCommunication(inetAddress, hostInfo.getPort());
		vertx.deployVerticle(authServerCommunication);

		Toolkit.getDefaultToolkit().beep();

		if(Config.IS_TELNET_ENABLED)
			statusServer = new TelnetServer();
		else
			_log.atInfo().log( "Telnet server is currently disabled." );

		_log.atInfo().log( "=================================================" );
		String memUsage = new StringBuilder().append(StatsUtils.getMemUsage()).toString();
		for(String line : memUsage.split("\n"))
			_log.atInfo().log( line );
		_log.atInfo().log( "=================================================" );
	}

	public GameServerListenerList getListeners()
	{
		return _listeners;
	}

	public static GameServer getInstance()
	{
		return _instance;
	}

	public <T extends GameListener> boolean addListener(T listener)
	{
		return _listeners.add(listener);
	}

	public <T extends GameListener> boolean removeListener(T listener)
	{
		return _listeners.remove(listener);
	}

	private static boolean checkFreePort(String hostname, int port)
	{
		ServerSocket ss = null;
		try
		{
			if(hostname.equalsIgnoreCase("*"))
				ss = new ServerSocket(port);
			else
				ss = new ServerSocket(port, 50, InetAddress.getByName(hostname));
		}
		catch(Exception e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "" );
			return false;
		}
		finally
		{
			try
			{
				ss.close();
			}
			catch(Exception e)
			{
				//
			}
		}
		return true;
	}

	private static boolean checkOpenPort(String ip, int port)
	{
		Socket socket = null;
		try
		{
			socket = new Socket();
			socket.connect(new InetSocketAddress(ip, port), 100);
		}
		catch(Exception e)
		{
			return false;
		}
		finally
		{
			try
			{
				socket.close();
			}
			catch(Exception e)
			{
				//
			}
		}
		return true;
	}

	private void registerSelectorThreads(TIntSet ports)
	{
		for(int port : ports.toArray())
			registerSelectorThread(null, port);
	}

	private void registerSelectorThread(String ip, int port)
	{
		try
		{
			ClientNetworkManager.getInstance().start(ip == null ? null : InetAddress.getByName(ip), port);
		}
		catch(Exception e)
		{
			//
		}
	}

	public static void main(String[] args) throws Exception
	{
		for(String arg : args)
		{
			if(arg.equalsIgnoreCase("-dev"))
				DEVELOP = true;
		}
		new GameServer();
	}

	public Version getVersion()
	{
		return version;
	}

	public TelnetServer getStatusServer()
	{
		return statusServer;
	}
}