package l2s.gameserver.network.telnet.commands;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import l2s.commons.dao.JdbcEntityStats;
import l2s.commons.lang.StatsUtils;
import l2s.commons.net.nio.impl.SelectorStats;
import l2s.commons.threading.RunnableStatsManager;
import l2s.gameserver.Config;
import l2s.gameserver.GameServer;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.dao.ItemsDAO;
import l2s.gameserver.dao.MailDAO;
import l2s.gameserver.geodata.pathfind.PathFindBuffers;
import l2s.gameserver.model.mail.Mail;
import l2s.gameserver.network.telnet.TelnetCommand;
import l2s.gameserver.network.telnet.TelnetCommandHolder;
import l2s.gameserver.taskmanager.AiTaskManager;
import l2s.gameserver.taskmanager.EffectTaskManager;
import l2s.gameserver.utils.GameStats;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.LinkedHashSet;
import java.util.Set;

public class TelnetPerfomance implements TelnetCommandHolder
{
	private final Set<TelnetCommand> _commands;

	public TelnetPerfomance()
	{
		(_commands = new LinkedHashSet<>()).add(new TelnetCommand("pool", "p"){
			@Override
			public String getUsage()
			{
				return "pool [dump]";
			}

			@Override
			public String handle(String[] args)
			{
				StringBuilder sb = new StringBuilder();
				if(args.length == 0 || args[0].isEmpty())
					sb.append(ThreadPoolManager.getInstance().getStats());
				else
				{
					if(!"dump".equals(args[0]))
						if(!"d".equals(args[0]))
							return null;
					try
					{
						new File("stats").mkdir();
						String format = new SimpleDateFormat("MMddHHmmss").format(System.currentTimeMillis());
						var file = Path.of("stats/RunnableStats-" + format + ".txt");
						Files.writeString(file, RunnableStatsManager.getInstance().getStats().toString());
						sb.append("Runnable stats saved.\n");
					}
					catch(IOException e)
					{
						sb.append("Exception: " + e.getMessage() + "!\n");
					}
				}
				return sb.toString();
			}
		});
		_commands.add(new TelnetCommand("mem", "m"){
			@Override
			public String getUsage()
			{
				return "mem";
			}

			@Override
			public String handle(String[] args)
			{
				StringBuilder sb = new StringBuilder();
				sb.append(StatsUtils.getMemUsage());
				return sb.toString();
			}
		});
		_commands.add(new TelnetCommand("threads", "t"){
			@Override
			public String getUsage()
			{
				return "threads [dump]";
			}

			@Override
			public String handle(String[] args)
			{
				StringBuilder sb = new StringBuilder();
				if(args.length == 0 || args[0].isEmpty())
					sb.append(StatsUtils.getThreadStats());
				else
				{
					if(!"dump".equals(args[0]))
						if(!"d".equals(args[0]))
							return null;
					try
					{
						new File("stats").mkdir();
						String format = new SimpleDateFormat("MMddHHmmss").format(System.currentTimeMillis());
						var file = Path.of("stats/ThreadsDump-" + format + ".txt");
						Files.writeString(file, StatsUtils.getThreadStats(true, true, true).toString());
						sb.append("Threads stats saved.\n");
					}
					catch(IOException e)
					{
						sb.append("Exception: " + e.getMessage() + "!\n");
					}
				}
				return sb.toString();
			}
		});
		_commands.add(new TelnetCommand("gc"){
			@Override
			public String getUsage()
			{
				return "gc";
			}

			@Override
			public String handle(String[] args)
			{
				StringBuilder sb = new StringBuilder();
				sb.append(StatsUtils.getGCStats());
				return sb.toString();
			}
		});
		_commands.add(new TelnetCommand("net", "ns"){
			@Override
			public String getUsage()
			{
				return "net";
			}

			@Override
			public String handle(String[] args)
			{
				StringBuilder sb = new StringBuilder();
				SelectorStats sts = GameServer.getInstance().getSelectorStats();
				sb.append("selectorThreadCount: .... ").append(GameServer.getInstance().getSelectorThreads().size()).append("\n");
				sb.append("=================================================\n");
				sb.append("getTotalConnections: .... ").append(sts.getTotalConnections()).append("\n");
				sb.append("getCurrentConnections: .. ").append(sts.getCurrentConnections()).append("\n");
				sb.append("getMaximumConnections: .. ").append(sts.getMaximumConnections()).append("\n");
				sb.append("getIncomingBytesTotal: .. ").append(sts.getIncomingBytesTotal()).append("\n");
				sb.append("getOutgoingBytesTotal: .. ").append(sts.getOutgoingBytesTotal()).append("\n");
				sb.append("getIncomingPacketsTotal:  ").append(sts.getIncomingPacketsTotal()).append("\n");
				sb.append("getOutgoingPacketsTotal:  ").append(sts.getOutgoingPacketsTotal()).append("\n");
				sb.append("getMaxBytesPerRead: ..... ").append(sts.getMaxBytesPerRead()).append("\n");
				sb.append("getMaxBytesPerWrite: .... ").append(sts.getMaxBytesPerWrite()).append("\n");
				sb.append("=================================================\n");
				return sb.toString();
			}
		});
		_commands.add(new TelnetCommand("pathfind", "pfs"){
			@Override
			public String getUsage()
			{
				return "pathfind";
			}

			@Override
			public String handle(String[] args)
			{
				StringBuilder sb = new StringBuilder();
				sb.append(PathFindBuffers.getStats());
				return sb.toString();
			}
		});
		_commands.add(new TelnetCommand("dbstats", "ds"){
			@Override
			public String getUsage()
			{
				return "dbstats";
			}

			@Override
			public String handle(String[] args)
			{
				StringBuilder sb = new StringBuilder();
				sb.append("Basic database usage\n");
				sb.append("=================================================\n");
				sb.append("Connections").append("\n");
				// TODO metrics
				//int busy = DatabaseFactory.getInstance().getBusyConnectionCount();
				//int idle = DatabaseFactory.getInstance().getIdleConnectionCount();
				int busy = 0;
                sb.append("     Busy: ........................ ").append(busy).append("\n");
                int idle = 0;
                sb.append("     Idle: ........................ ").append(idle).append("\n");
				sb.append("Players").append("\n");
				sb.append("     Update: ...................... ").append(GameStats.getUpdatePlayerBase()).append("\n");
				Cache itemCache = ItemsDAO.getInstance().getCache();
				CacheStats itemCacheStats = itemCache.stats();
				JdbcEntityStats entityStats = ItemsDAO.getInstance().getStats();
				sb.append("Items").append("\n");
				sb.append("     getLoadCount: ................ ").append(entityStats.getLoadCount()).append("\n");
				sb.append("     getInsertCount: .............. ").append(entityStats.getInsertCount()).append("\n");
				sb.append("     getUpdateCount: .............. ").append(entityStats.getUpdateCount()).append("\n");
				sb.append("     getDeleteCount: .............. ").append(entityStats.getDeleteCount()).append("\n");
				sb.append("Cache:").append("\n");
				sb.append(itemCacheStats).append("\n");
				sb.append("=================================================\n");
				Cache<Integer, Mail> mailCache = MailDAO.getInstance().getCache();
				CacheStats mailCacheStats = mailCache.stats();
				entityStats = MailDAO.getInstance().getStats();
				sb.append("Mail").append("\n");
				sb.append("     getLoadCount: ................ ").append(entityStats.getLoadCount()).append("\n");
				sb.append("     getInsertCount: .............. ").append(entityStats.getInsertCount()).append("\n");
				sb.append("     getUpdateCount: .............. ").append(entityStats.getUpdateCount()).append("\n");
				sb.append("     getDeleteCount: .............. ").append(entityStats.getDeleteCount()).append("\n");
				sb.append("Cache:").append("\n");
				sb.append(mailCacheStats).append("\n");
				sb.append("=================================================\n");
				return sb.toString();
			}
		});
		_commands.add(new TelnetCommand("aistats", "as"){
			@Override
			public String getUsage()
			{
				return "aistats";
			}

			@Override
			public String handle(String[] args)
			{
				StringBuilder sb = new StringBuilder();
				for(int i = 0; i < Config.AI_TASK_MANAGER_COUNT; ++i)
				{
					sb.append("AiTaskManager #").append(i + 1).append("\n");
					sb.append("=================================================\n");
					sb.append(AiTaskManager.getInstance().getStats(i));
					sb.append("=================================================\n");
				}
				return sb.toString();
			}
		});
		_commands.add(new TelnetCommand("effectstats", "es"){
			@Override
			public String getUsage()
			{
				return "effectstats";
			}

			@Override
			public String handle(String[] args)
			{
				StringBuilder sb = new StringBuilder();
				for(int i = 0; i < Config.EFFECT_TASK_MANAGER_COUNT; ++i)
				{
					sb.append("EffectTaskManager #").append(i + 1).append("\n");
					sb.append("=================================================\n");
					sb.append(EffectTaskManager.getInstance().getStats(i));
					sb.append("=================================================\n");
				}
				return sb.toString();
			}
		});
	}

	@Override
	public Set<TelnetCommand> getCommands()
	{
		return _commands;
	}
}
