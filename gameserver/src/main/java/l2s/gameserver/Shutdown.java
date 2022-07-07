package l2s.gameserver;

import l2s.Phantoms.PhantomPlayers;
import l2s.commons.net.nio.impl.SelectorThread;
import l2s.commons.time.cron.SchedulingPattern;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.instancemanager.BotReportManager;
import l2s.gameserver.instancemanager.CoupleManager;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.network.telnet.TelnetServer;
import l2s.gameserver.statistics.StatisticsService;
import l2s.gameserver.tables.ClanTable;
import l2s.gameserver.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

public class Shutdown extends Thread
{
	private static final Logger _log = LoggerFactory.getLogger(Shutdown.class);
	public static final int SHUTDOWN = 0;
	public static final int RESTART = 2;
	public static final int NONE = -1;
	public static final int FULL_ANNOUNCES = 2;
	public static final int OFFLIKE_ANNOUNCES = 1;
	private static final Shutdown _instance = new Shutdown();
	private Timer counter;
	private int shutdownMode;
	private int shutdownCounter;

	public static final Shutdown getInstance()
	{
		return _instance;
	}

	private Shutdown()
	{
		setName(getClass().getSimpleName());
		shutdownMode = -1;
	}

	public int getSeconds()
	{
		return shutdownMode == -1 ? -1 : shutdownCounter;
	}

	public int getMode()
	{
		return shutdownMode;
	}

	public synchronized void schedule(int seconds, int shutdownMode)
	{
		if(seconds < 0)
			return;
		if(counter != null)
			counter.cancel();
		this.shutdownMode = shutdownMode;
		shutdownCounter = seconds;
		_log.info("Scheduled server " + (shutdownMode == 0 ? "shutdown" : "restart") + " in " + Util.formatTime(seconds) + ".");
		(counter = new Timer("ShutdownCounter", true)).scheduleAtFixedRate(new ShutdownCounter(), 0L, 1000L);
	}

	public void schedule(String time, int shutdownMode)
	{
		SchedulingPattern cronTime;
		try
		{
			cronTime = new SchedulingPattern(time);
		}
		catch(IllegalArgumentException e)
		{
			return;
		}
		int seconds = (int) (cronTime.next(System.currentTimeMillis()) / 1000L - System.currentTimeMillis() / 1000L);
        schedule(seconds, shutdownMode);
	}

	public synchronized void cancel()
	{
		shutdownMode = -1;
		if(counter != null)
			counter.cancel();
		counter = null;
	}

	@Override
	public void run()
	{
		PhantomPlayers.getInstance().shutdown();
		System.out.println("Shutting down LS/GS communication...");
		GameServer.getInstance().getVertx().close();
		TelnetServer telnetServer = GameServer.getInstance().getStatusServer();
		if (telnetServer != null)
			telnetServer.stopAsync().awaitTerminated();

		StatisticsService.getInstance().stopAsync();

		GameServer gameServer = GameServer.getInstance();
		if(gameServer != null) {
			try {
				gameServer.getListeners().onShutdown();
			}
			catch (Exception e) {
				_log.error("Shutdown events error ", e);
			}
		}
		System.out.println("Disconnecting players...");
		disconnectAllPlayers();
		System.out.println("Saving data...");
		saveData();
		try
		{
			System.out.println("Shutting down thread pool...");
			ThreadPoolManager.getInstance().shutdown();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("Shutting down selector...");
		if(gameServer != null)
			for(SelectorThread<GameClient> st : gameServer.getSelectorThreads())
				try
				{
					st.shutdown();
				}
				catch(Exception e2)
				{
					e2.printStackTrace();
				}
		try
		{
			System.out.println("Shutting down database communication...");
			DatabaseFactory.getInstance().shutdown();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("Shutdown finished.");
	}

	private void saveData()
	{
		if(Config.ALLOW_WEDDING)
			try
			{
				CoupleManager.getInstance().store();
				System.out.println("CoupleManager: Data saved.");
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		try
		{
			ClanTable.getInstance().storeClanWars();
			System.out.println("Clan War: Data saved.");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		if(Config.BOTREPORT_ENABLED)
			try
			{
				BotReportManager.getInstance().saveReportedCharData();
				System.out.println("BotReportManager: Data saved.");
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
	}

	private void disconnectAllPlayers()
	{
		for(Player player : GameObjectsStorage.getPlayers())
			try
			{
				player.logout();
			}
			catch(Exception e)
			{
				System.out.println("Error while disconnecting: " + player + "!");
				e.printStackTrace();
			}
	}

	private class ShutdownCounter extends TimerTask
	{
		@Override
		public void run()
		{
			switch(shutdownCounter)
			{
				case 1800:
				case 900:
				case 600:
				case 300:
				case 240:
				case 180:
				case 120:
				case 60:
				{
					if(Config.SHUTDOWN_ANN_TYPE == 2)
						Announcements.announceToAllFromStringHolder("THE_SERVER_WILL_BE_COMING_DOWN_IN_S1_MINUTES", String.valueOf(shutdownCounter / 60));
					break;
				}
				case 30:
				case 20:
				case 10:
				case 5:
				case 4:
				case 3:
				case 2:
				case 1:
				{
					if(Config.SHUTDOWN_ANN_TYPE == 2 || Config.SHUTDOWN_ANN_TYPE == 1)
						Announcements.announceToAll(new SystemMessagePacket(SystemMsg.THE_SERVER_WILL_BE_COMING_DOWN_IN_S1_SECONDS__PLEASE_FIND_A_SAFE_PLACE_TO_LOG_OUT).addNumber(shutdownCounter));
					break;
				}
				case 0:
				{
					switch(shutdownMode)
					{
						case 0:
						{
							Runtime.getRuntime().exit(0);
							break;
						}
						case 2:
						{
							Runtime.getRuntime().exit(2);
							break;
						}
					}
					cancel();
					return;
				}
			}
			shutdownCounter--;
		}
	}
}
