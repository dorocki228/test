package l2s.gameserver.network.telnet.commands;

import l2s.gameserver.GameServer;
import l2s.gameserver.Shutdown;
import l2s.gameserver.network.telnet.TelnetCommand;
import l2s.gameserver.network.telnet.TelnetCommandHolder;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.lang.management.ManagementFactory;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.Set;

public class TelnetServer implements TelnetCommandHolder
{
	private final Set<TelnetCommand> _commands;

	public TelnetServer()
	{
		(_commands = new LinkedHashSet<>()).add(new TelnetCommand("version", "ver"){
			@Override
			public String getUsage()
			{
				return "version";
			}

			@Override
			public String handle(String[] args)
			{
				return "Revision : " + GameServer.getInstance().getVersion().getRevisionNumber() + " Build date : " + GameServer.getInstance().getVersion().getBuildDate() + "\n";
			}
		});
		_commands.add(new TelnetCommand("uptime"){
			@Override
			public String getUsage()
			{
				return "uptime";
			}

			@Override
			public String handle(String[] args)
			{
				return DurationFormatUtils.formatDurationHMS(ManagementFactory.getRuntimeMXBean().getUptime()) + "\n";
			}
		});
		_commands.add(new TelnetCommand("restart"){
			@Override
			public String getUsage()
			{
				return "restart <seconds>|now>";
			}

			@Override
			public String handle(String[] args)
			{
				if(args.length == 0)
					return null;
				StringBuilder sb = new StringBuilder();
				if(NumberUtils.isNumber(args[0]))
				{
					int val = NumberUtils.toInt(args[0]);
					Shutdown.getInstance().schedule(val, 2);
					sb.append("Server will restart in ").append(Shutdown.getInstance().getSeconds()).append(" seconds!\n");
					sb.append("Type \"abort\" to abort restart!\n");
				}
				else if("now".equalsIgnoreCase(args[0]))
				{
					sb.append("Server will restart now!\n");
					Shutdown.getInstance().schedule(0, 2);
				}
				else
				{
					String[] hhmm = args[0].split(":");
					Calendar date = Calendar.getInstance();
					Calendar now = Calendar.getInstance();
					date.set(11, Integer.parseInt(hhmm[0]));
					date.set(12, hhmm.length > 1 ? Integer.parseInt(hhmm[1]) : 0);
					date.set(13, 0);
					date.set(14, 0);
					if(date.before(now))
						date.roll(5, true);
					int seconds = (int) (date.getTimeInMillis() / 1000L - now.getTimeInMillis() / 1000L);
					Shutdown.getInstance().schedule(seconds, 2);
					sb.append("Server will restart in ").append(Shutdown.getInstance().getSeconds()).append(" seconds!\n");
					sb.append("Type \"abort\" to abort restart!\n");
				}
				return sb.toString();
			}
		});
		_commands.add(new TelnetCommand("shutdown"){
			@Override
			public String getUsage()
			{
				return "shutdown <seconds>|now|<hh:mm>";
			}

			@Override
			public String handle(String[] args)
			{
				if(args.length == 0)
					return null;
				StringBuilder sb = new StringBuilder();
				if(NumberUtils.isNumber(args[0]))
				{
					int val = NumberUtils.toInt(args[0]);
					Shutdown.getInstance().schedule(val, 0);
					sb.append("Server will shutdown in ").append(Shutdown.getInstance().getSeconds()).append(" seconds!\n");
					sb.append("Type \"abort\" to abort shutdown!\n");
				}
				else if("now".equalsIgnoreCase(args[0]))
				{
					sb.append("Server will shutdown now!\n");
					Shutdown.getInstance().schedule(0, 0);
				}
				else
				{
					String[] hhmm = args[0].split(":");
					Calendar date = Calendar.getInstance();
					Calendar now = Calendar.getInstance();
					date.set(11, Integer.parseInt(hhmm[0]));
					date.set(12, hhmm.length > 1 ? Integer.parseInt(hhmm[1]) : 0);
					date.set(13, 0);
					date.set(14, 0);
					if(date.before(now))
						date.roll(5, true);
					int seconds = (int) (date.getTimeInMillis() / 1000L - now.getTimeInMillis() / 1000L);
					Shutdown.getInstance().schedule(seconds, 0);
					sb.append("Server will shutdown in ").append(Shutdown.getInstance().getSeconds()).append(" seconds!\n");
					sb.append("Type \"abort\" to abort shutdown!\n");
				}
				return sb.toString();
			}
		});
		_commands.add(new TelnetCommand("abort"){
			@Override
			public String getUsage()
			{
				return "abort";
			}

			@Override
			public String handle(String[] args)
			{
				Shutdown.getInstance().cancel();
				return "Aborted.\n";
			}
		});
	}

	@Override
	public Set<TelnetCommand> getCommands()
	{
		return _commands;
	}
}
