package l2s.gameserver;

import as.AdminJobServerInitializer;
import com.mmobite.as.api.AntispamAPI;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import gve.zones.GveZoneManager;
import io.vertx.core.Vertx;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import l2s.Phantoms.PhantomPlayers;
import l2s.commons.lang.StatsUtils;
import l2s.commons.listener.Listener;
import l2s.commons.listener.ListenerList;
import l2s.commons.net.nio.impl.SelectorStats;
import l2s.commons.net.nio.impl.SelectorThread;
import l2s.commons.versioning.Version;
import l2s.gameserver.cache.CrestCache;
import l2s.gameserver.cache.ImagesCache;
import l2s.gameserver.config.templates.HostInfo;
import l2s.gameserver.config.xml.ConfigParsers;
import l2s.gameserver.config.xml.holder.HostsConfigHolder;
import l2s.gameserver.dao.CharacterDAO;
import l2s.gameserver.dao.DatabaseSettings;
import l2s.gameserver.dao.FractionTreasureDAO;
import l2s.gameserver.dao.HidenItemsDAO;
import l2s.gameserver.dao.ItemsDAO;
import l2s.gameserver.data.BoatHolder;
import l2s.gameserver.data.xml.Parsers;
import l2s.gameserver.data.xml.holder.EventHolder;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.data.xml.holder.StaticObjectHolder;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.handler.admincommands.AdminCommandHandler;
import l2s.gameserver.handler.bbs.BbsHandlerHolder;
import l2s.gameserver.handler.bypass.BypassHolder;
import l2s.gameserver.handler.dailymissions.DailyMissionHandlerHolder;
import l2s.gameserver.handler.items.ItemHandler;
import l2s.gameserver.handler.onshiftaction.OnShiftActionHolder;
import l2s.gameserver.handler.usercommands.UserCommandHandler;
import l2s.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.instancemanager.BotCheckManager;
import l2s.gameserver.instancemanager.BotReportManager;
import l2s.gameserver.instancemanager.ClanRewardManager;
import l2s.gameserver.instancemanager.CoupleManager;
import l2s.gameserver.instancemanager.PetitionManager;
import l2s.gameserver.instancemanager.PlayerMessageStack;
import l2s.gameserver.instancemanager.RaidBossSpawnManager;
import l2s.gameserver.instancemanager.SpawnManager;
import l2s.gameserver.instancemanager.clansearch.ClanSearchManager;
import l2s.gameserver.instancemanager.games.MiniGameScoreManager;
import l2s.gameserver.listener.GameListener;
import l2s.gameserver.listener.game.OnShutdownListener;
import l2s.gameserver.listener.game.OnStartListener;
import l2s.gameserver.model.World;
import l2s.gameserver.model.entity.Hero;
import l2s.gameserver.model.entity.MonsterRace;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.model.farm.SteadBarnManager;
import l2s.gameserver.network.authcomm.vertx.AuthServerCommunication;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.GamePacketHandler;
import l2s.gameserver.network.telnet.TelnetServer;
import l2s.gameserver.punishment.PunishmentDao;
import l2s.gameserver.punishment.PunishmentService;
import l2s.gameserver.scripts.Scripts;
import l2s.gameserver.service.ArenaEventService;
import l2s.gameserver.service.ArtifactService;
import l2s.gameserver.service.CasinoEventService;
import l2s.gameserver.service.ConfrontationService;
import l2s.gameserver.service.FactionLeaderService;
import l2s.gameserver.service.FractionService;
import l2s.gameserver.service.MultisellDiscountService;
import l2s.gameserver.service.PartyClassLimitService;
import l2s.gameserver.service.PartyService;
import l2s.gameserver.service.SubsSkillsService;
import l2s.gameserver.statistics.StatisticsService;
import l2s.gameserver.tables.ClanTable;
import l2s.gameserver.taskmanager.AutomaticTasks;
import l2s.gameserver.taskmanager.ItemsAutoDestroy;
import l2s.gameserver.utils.OnlineTxtGenerator;
import l2s.gameserver.utils.Strings;
import l2s.gameserver.utils.TradeHelper;
import l2s.gameserver.utils.velocity.VelocityUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameServer {
    public static boolean DEVELOP;

    public static final String PROJECT_REVISION = "L2s [21963]";
    public static final String UPDATE_NAME = "Classic 1.5";

    public static final int AUTH_SERVER_PROTOCOL = 2;

    private static final Logger _log = LoggerFactory.getLogger(GameServer.class);

    public static GameServer _instance;

    private final Vertx vertx;
    private final List<SelectorThread<GameClient>> _selectorThreads;
    private final SelectorStats _selectorStats;
    private final AuthServerCommunication authServerCommunication;
    private final Version version;
    private TelnetServer statusServer;
    private final GameServerListenerList _listeners;
    private final long _serverStartTimeMillis;
    private final int _onlineLimit;

    public Vertx getVertx() {
        return vertx;
    }

    public List<SelectorThread<GameClient>> getSelectorThreads() {
        return _selectorThreads;
    }

    public SelectorStats getSelectorStats() {
        return _selectorStats;
    }

    public AuthServerCommunication getAuthServerCommunication() {
        return authServerCommunication;
    }

    public long getServerStartTime() {
        return _serverStartTimeMillis;
    }

    public int getOnlineLimit() {
        return _onlineLimit;
    }

    public GameServer() throws Exception {
        vertx = Vertx.vertx();
        _selectorThreads = new ArrayList<>();
        _selectorStats = new SelectorStats();
        _instance = this;
        _serverStartTimeMillis = System.currentTimeMillis();
        _listeners = new GameServerListenerList();

        version = new Version(GameServer.class);
        _log.info("=================================================");
        _log.info("Build Revision: .......... " + version.getRevisionNumber());
        _log.info("Update: .................. Classic 1.5");
        _log.info("Build date: .............. " + version.getBuildDate());
        _log.info("Compiler version: ........ " + version.getBuildJdk());
        _log.info("=================================================");
        ConfigParsers.parseAll();
        Config.load();
        VelocityUtils.init();
        int onlineLimit = 0;
        TIntSet ports = new TIntHashSet();
        for (HostInfo host : HostsConfigHolder.getInstance().getGameServerHosts()) {
            onlineLimit = Config.MAXIMUM_ONLINE_USERS;
            if (host.getIP() != null || host.getInnerIP() != null)
                ports.add(host.getPort());
        }
        _onlineLimit = onlineLimit;
        checkFreePorts(ports);

        DatabaseFactory.getInstance();

        IdFactory _idFactory = IdFactory.getInstance();
        if (!_idFactory.isInitialized()) {
            _log.error("Could not read object IDs from DB. Please Check Your Data.");
            throw new Exception("Could not initialize the ID factory");
        }

        ThreadPoolManager.getInstance();
        BotCheckManager.loadBotQuestions();
        HidenItemsDAO.LoadAllHiddenItems();

        ItemHandler.getInstance();

        DailyMissionHandlerHolder.getInstance();

        Scripts.getInstance();
        GeoEngine.load();
        Strings.reload();

        World.init();
        Parsers.parseAll();
        Config.loadBufferConfig();
        ItemsDAO.getInstance();
        CrestCache.getInstance();
        ImagesCache.getInstance();
        CharacterDAO.getInstance();
        ClanTable.getInstance().load();

        SpawnManager.getInstance().spawnAll();
        StaticObjectHolder.getInstance().spawnAll();
        RaidBossSpawnManager.getInstance();
        Scripts.getInstance().init();
        Announcements.getInstance();
        PlayerMessageStack.getInstance();
        if (Config.AUTODESTROY_ITEM_AFTER > 0)
            ItemsAutoDestroy.getInstance();
        MonsterRace.getInstance();
        if (Config.ENABLE_OLYMPIAD) {
            Olympiad.load();
            Hero.getInstance();
        }
        PetitionManager.getInstance();
        if (Config.ALLOW_WEDDING)
            CoupleManager.getInstance();

        AdminCommandHandler.getInstance().log();
        UserCommandHandler.getInstance().log();
        VoicedCommandHandler.getInstance().log();
        BbsHandlerHolder.getInstance().log();
        BypassHolder.getInstance().log();
        OnShiftActionHolder.getInstance().log();

        AutomaticTasks.getInstance().init();
        ClanTable.getInstance().init();

        _log.info("=[Events]=========================================");
        ResidenceHolder.getInstance().callInit();
        EventHolder.getInstance().callInit();
        _log.info("==================================================");
        BoatHolder.getInstance().spawnAll();
        //		CastleManorManager.getInstance();
        Runtime.getRuntime().addShutdownHook(Shutdown.getInstance());
        _log.info("IdFactory: Free ObjectID's remaining: " + IdFactory.getInstance().size());
        MiniGameScoreManager.getInstance();
        ClanRewardManager.getInstance().checkNewDay();
        ClanSearchManager.getInstance().load();
        BotReportManager.getInstance();
        GveZoneManager.getInstance().init();
        FractionService.getInstance();
        FractionTreasureDAO.getInstance().init();
        ArtifactService.getInstance().restore();
        PartyService.getInstance().init();
        PartyClassLimitService.getInstance().init();
        ConfrontationService.getInstance().restore();
        FactionLeaderService.getInstance().restore();
        CasinoEventService.getInstance().restore();
        SubsSkillsService.getInstance();

        if (Config.GVE_FARM_ENABLED) {
            SteadBarnManager.getInstance().init();
        }

        MultisellDiscountService.getInstance().init();

        Shutdown.getInstance().schedule(Config.RESTART_AT_TIME, 2);

        registerSelectorThreads(ports);

        getListeners().onStart();

        if (Config.SERVICES_OFFLINE_TRADE_RESTORE_AFTER_RESTART) {
            _log.info("Restoring offline traders...");
            int count = TradeHelper.restoreOfflineTraders();
            _log.info("Restored " + count + " offline traders.");
        }

        if (Config.ONLINE_GENERATOR_ENABLED)
            ThreadPoolManager.getInstance().scheduleAtFixedRate(new OnlineTxtGenerator(), 5000L, Config.ONLINE_GENERATOR_DELAY * 60 * 1000L);

        HostInfo hostInfo = HostsConfigHolder.getInstance().getAuthServerHost();
        InetAddress inetAddress = hostInfo.getIP().equals("*") ? null : InetAddress.getByName(hostInfo.getIP());
        authServerCommunication = new AuthServerCommunication(inetAddress, hostInfo.getPort());
        vertx.deployVerticle(authServerCommunication);

        if (Config.GVE.statisticsScheduleEnabled()) {
            StatisticsService.getInstance().startAsync();
        }

        DatabaseSettings.INSTANCE.getDatabase();

        PunishmentDao.INSTANCE.onInit();

        PunishmentService.INSTANCE.load();
        ArenaEventService.getInstance().init();
        AdminJobServerInitializer.start();
        //boolean antispam = AntispamAPI.init("config/antispam/client.properties", 64);
       // _log.info("AntiSpam state {}", antispam);

        if (Config.IS_TELNET_ENABLED) {
            statusServer = new TelnetServer();
            statusServer.startAsync();
        }
        else
            _log.info("Telnet server is currently disabled.");

        _log.info("Maximum Numbers of Connected Players: " + getOnlineLimit());
        _log.info("=================================================");
        Duration duration = Duration.ofMillis(System.currentTimeMillis() - _serverStartTimeMillis);
        _log.info("GameServer Started in {}", String.format("%sm %ss %sms",
                duration.toMinutesPart(), duration.toSecondsPart(), duration.toMillisPart()));
        _log.info("=================================================");
        String memUsage = String.valueOf(StatsUtils.getMemUsage());
        for (String line : memUsage.split("\n"))
            _log.info(line);
        _log.info("=================================================");
        PhantomPlayers.init();
    }

    public GameServerListenerList getListeners() {
        return _listeners;
    }

    public static GameServer getInstance() {
        return _instance;
    }

    public <T extends GameListener> boolean addListener(T listener) {
        return _listeners.add(listener);
    }

    public <T extends GameListener> boolean removeListener(T listener) {
        return _listeners.remove(listener);
    }

    private void checkFreePorts(TIntSet ports) {
        for (int port : ports.toArray())
            while (!checkFreePort(null, port)) {
            }
    }

    private boolean checkFreePort(String ip, int port) {
        try {
            ServerSocket ss;
            if (ip == null)
                ss = new ServerSocket(port);
            else
                ss = new ServerSocket(port, 50, InetAddress.getByName(ip));
            ss.close();
        } catch (Exception e) {
            _log.warn("Port " + port + " is allready binded. Please free it and restart server.");
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException ex) {
            }
            return false;
        }
        return true;
    }

    private void registerSelectorThreads(TIntSet ports) {
        GamePacketHandler gph = new GamePacketHandler();
        for (int port : ports.toArray())
            registerSelectorThread(gph, null, port);
    }

    private void registerSelectorThread(GamePacketHandler gph, String ip, int port) {
        try {
            SelectorThread<GameClient> selectorThread = new SelectorThread(Config.SELECTOR_CONFIG, _selectorStats, gph, gph, gph, null);
            selectorThread.openServerSocket(ip == null ? null : InetAddress.getByName(ip), port);
            selectorThread.start();
            _selectorThreads.add(selectorThread);
        } catch (Exception ex) {
        }
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("vertx.cacheDirBase", "vertx-cache");

        for (String arg : args)
            if ("-dev".equalsIgnoreCase(arg)) {
                DEVELOP = true;

                _log.info("Gameserver started in dev mode.");

                Configurator.setAllLevels("l2s", Level.DEBUG);
            }

        new GameServer();
    }

    public Version getVersion() {
        return version;
    }

    public TelnetServer getStatusServer() {
        return statusServer;
    }

    public class GameServerListenerList extends ListenerList<GameServer> {
        public void onStart() {
            for (Listener<GameServer> listener : getListeners())
                if (OnStartListener.class.isInstance(listener))
                    ((OnStartListener) listener).onStart();
        }

        public void onShutdown() {
            for (Listener<GameServer> listener : getListeners())
                if (OnShutdownListener.class.isInstance(listener))
                    ((OnShutdownListener) listener).onShutdown();
        }
    }
}
