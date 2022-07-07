package l2s.authserver;

import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import l2s.authserver.database.DatabaseFactory;
import l2s.authserver.network.gamecomm.vertx.GameServerCommunication;
import l2s.authserver.network.l2.L2LoginClient;
import l2s.authserver.network.l2.L2LoginPacketHandler;
import l2s.authserver.network.l2.SelectorHelper;
import l2s.commons.net.nio.impl.SelectorConfig;
import l2s.commons.net.nio.impl.SelectorStats;
import l2s.commons.net.nio.impl.SelectorThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

public class AuthServer
{
	private static final Logger _log = LoggerFactory.getLogger(AuthServer.class);
	private static AuthServer authServer;
	private final Verticle gameServerCommunication;
	private final SelectorThread<L2LoginClient> _selectorThread;

	public static AuthServer getInstance()
	{
		return authServer;
	}

	public AuthServer() throws Exception
	{
		Vertx vertx = Vertx.vertx();
		Config.initCrypt();
		GameServerManager.getInstance();

		L2LoginPacketHandler lph = new L2LoginPacketHandler();

		SelectorHelper sh = new SelectorHelper();
		SelectorConfig sc = new SelectorConfig();

		sc.AUTH_TIMEOUT = 60000L;

		SelectorStats sts = new SelectorStats();
		_selectorThread = new SelectorThread<>(sc, sts, lph, sh, sh, sh);

		InetAddress inetAddress = Config.GAME_SERVER_LOGIN_HOST.equals("*") ? null : InetAddress.getByName(Config.GAME_SERVER_LOGIN_HOST);
		gameServerCommunication = new GameServerCommunication(inetAddress, Config.GAME_SERVER_LOGIN_PORT);
		vertx.deployVerticle(gameServerCommunication);

		_selectorThread.openServerSocket(Config.LOGIN_HOST.equals("*") ? null : InetAddress.getByName(Config.LOGIN_HOST), Config.PORT_LOGIN);
		_selectorThread.start();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			vertx.close();
			DatabaseFactory.getInstance().shutdown();
		}));

		_log.info("Listening for clients on {}:{}", Config.LOGIN_HOST, Config.PORT_LOGIN);
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
				catch(Exception ex)
				{}
		}
	}

	public static void main(String[] args) throws Exception
	{
		Config.load();
		checkFreePorts();

		DatabaseFactory.getInstance();

		authServer = new AuthServer();
	}
}
