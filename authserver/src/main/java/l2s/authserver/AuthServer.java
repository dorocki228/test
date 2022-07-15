package l2s.authserver;

import com.google.common.flogger.FluentLogger;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import l2s.authserver.database.DatabaseFactory;
import l2s.authserver.network.gamecomm.vertx.GameServerCommunication;
import l2s.authserver.network.l2.L2LoginClient;
import l2s.authserver.network.l2.L2LoginPacketHandler;
import l2s.authserver.network.l2.SelectorHelper;
import l2s.commons.net.nio.impl.SelectorConfig;
import l2s.commons.net.nio.impl.SelectorStats;
import l2s.commons.net.nio.impl.SelectorThread;

public class AuthServer
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	public static final int AUTH_SERVER_PROTOCOL = 4;

	private static AuthServer authServer;

	private final Verticle gameServerCommunication;
	private SelectorThread<L2LoginClient> _selectorThread;

	public static AuthServer getInstance()
	{
		return authServer;
	}

	public AuthServer() throws Exception
	{
		VertxOptions vertxOptions = new VertxOptions()
				.setInternalBlockingPoolSize(2)
				.setWorkerPoolSize(2)
				.setEventLoopPoolSize(2);
		Vertx vertx = Vertx.vertx(vertxOptions);

		Config.initCrypt();
		GameServerManager.getInstance();

		L2LoginPacketHandler lph = new L2LoginPacketHandler();
		SelectorHelper sh = new SelectorHelper();
		SelectorConfig sc = new SelectorConfig();
		sc.AUTH_TIMEOUT = Config.LOGIN_TIMEOUT;
		SelectorStats sts = new SelectorStats();
		_selectorThread = new SelectorThread<L2LoginClient>(sc, sts, lph, sh, sh, sh);

		InetAddress inetAddress = Config.GAME_SERVER_LOGIN_HOST.equals("*") ? null : InetAddress.getByName(Config.GAME_SERVER_LOGIN_HOST);
		gameServerCommunication = new GameServerCommunication(inetAddress, Config.GAME_SERVER_LOGIN_PORT);
		vertx.deployVerticle(gameServerCommunication);

		_selectorThread.openServerSocket(Config.LOGIN_HOST.equals("*") ? null : InetAddress.getByName(Config.LOGIN_HOST), Config.PORT_LOGIN);
		_selectorThread.start();
		_log.atInfo().log( "Listening for clients on %s:%s", Config.LOGIN_HOST, Config.PORT_LOGIN );

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			vertx.close();
			DatabaseFactory.getInstance().shutdown();
		}));
	}

	public Verticle getGameServerListener()
	{
		return gameServerCommunication;
	}

	public static void checkFreePorts() throws IOException
	{
		ServerSocket ss = null;

		try
		{
			if(Config.LOGIN_HOST.equalsIgnoreCase("*"))
				ss = new ServerSocket(Config.PORT_LOGIN);
			else
				ss = new ServerSocket(Config.PORT_LOGIN, 50, InetAddress.getByName(Config.LOGIN_HOST));
		}
		finally
		{
			if(ss != null)
				try
				{
					ss.close();
				}
				catch(Exception e)
				{}
		}
	}

	public static void main(String[] args) throws Exception
	{
		// Initialize config
		Config.load();
		// Check binding address
		checkFreePorts();
		// Initialize database
		DatabaseFactory.getInstance();

		AuthBanManager.getInstance().init();

		authServer = new AuthServer();
	}
}