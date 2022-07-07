package l2s.gameserver.network.telnet.commands;

import l2s.commons.lang.StatsUtils;
import l2s.gameserver.GameServer;
import l2s.gameserver.Shutdown;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.model.World;
import l2s.gameserver.network.telnet.TelnetCommand;
import l2s.gameserver.network.telnet.TelnetCommandHolder;
import l2s.gameserver.tables.GmListTable;
import l2s.gameserver.time.GameTimeService;
import l2s.gameserver.utils.Util;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

public class TelnetStatus implements TelnetCommandHolder
{
	private final Set<TelnetCommand> _commands;

	public TelnetStatus()
	{
		(_commands = new LinkedHashSet<>()).add(new TelnetCommand("status", "s"){
			@Override
			public String getUsage()
			{
				return "status";
			}

			@Override
			public String handle(String[] args)
			{
				StringBuilder sb = new StringBuilder();
				int[] stats = World.getStats();
				sb.append("Server Status: ").append("\n");
				sb.append("Players: ................. ").append(stats[12]).append("/").append(GameServer.getInstance().getOnlineLimit()).append("\n");
				sb.append("     Online: ............. ").append(stats[12] - stats[13]).append("\n");
				sb.append("     Offline: ............ ").append(stats[13]).append("\n");
				sb.append("     GM: ................. ").append(GmListTable.getAllGMs().size()).append("\n");
				sb.append("Objects: ................. ").append(stats[10]).append("\n");
				sb.append("Characters: .............. ").append(stats[11]).append("\n");
				sb.append("Summons: ................. ").append(stats[18]).append("\n");
				sb.append("Npcs: .................... ").append(stats[15]).append("/").append(stats[14]).append("\n");
				sb.append("Monsters: ................ ").append(stats[16]).append("\n");
				sb.append("Minions: ................. ").append(stats[17]).append("\n");
				sb.append("Doors: ................... ").append(stats[19]).append("\n");
				sb.append("Items: ................... ").append(stats[20]).append("\n");
				sb.append("Reflections: ............. ").append(ReflectionManager.getInstance().getAll().size()).append("\n");
				sb.append("Regions: ................. ").append(stats[0]).append("\n");
				sb.append("     Active: ............. ").append(stats[1]).append("\n");
				sb.append("     Inactive: ........... ").append(stats[2]).append("\n");
				sb.append("     Null: ............... ").append(stats[3]).append("\n");
				sb.append("Game Time: ............... ").append(getGameTime()).append("\n");
				sb.append("Real Time: ............... ").append(getCurrentTime()).append("\n");
				sb.append("Start Time: .............. ").append(getStartTime()).append("\n");
				sb.append("Uptime: .................. ").append(getUptime()).append("\n");
				sb.append("Shutdown: ................ ").append(Util.formatTime(Shutdown.getInstance().getSeconds())).append("/").append(Shutdown.getInstance().getMode()).append("\n");
				sb.append("Threads: ................. ").append(Thread.activeCount()).append("\n");
				sb.append("RAM Used: ................ ").append(StatsUtils.getMemUsedMb()).append("\n");
				return sb.toString();
			}
		});
	}

	@Override
	public Set<TelnetCommand> getCommands()
	{
		return _commands;
	}

	private static String getGameTime()
	{
		int t = GameTimeService.INSTANCE.getGameTime();
		int h = t / 60;
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
		Calendar cal = Calendar.getInstance();
		cal.set(11, h);
        int m = t % 60;
        cal.set(12, m);
		return format.format(cal.getTime());
	}

	private static String getUptime()
	{
		return DurationFormatUtils.formatDurationHMS(ManagementFactory.getRuntimeMXBean().getUptime());
	}

	private static String getStartTime()
	{
		return new Date(ManagementFactory.getRuntimeMXBean().getStartTime()).toString();
	}

	private static String getCurrentTime()
	{
		return new Date().toString();
	}
}
