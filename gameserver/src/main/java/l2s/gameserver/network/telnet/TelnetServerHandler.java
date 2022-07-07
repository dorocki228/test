package l2s.gameserver.network.telnet;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import l2s.gameserver.Config;
import l2s.gameserver.network.telnet.commands.TelnetServer;
import l2s.gameserver.network.telnet.commands.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TelnetServerHandler extends SimpleChannelInboundHandler<String> implements TelnetCommandHolder
{
	private static final Logger _log = LoggerFactory.getLogger(TelnetServerHandler.class);

	private static final Pattern COMMAND_ARGS_PATTERN = Pattern.compile("\"([^\"]*)\"|([^\\s]+)");

	private final Set<TelnetCommand> _commands;

	private boolean logged;

	public TelnetServerHandler()
	{
		_commands = new LinkedHashSet<>();
		_commands.add(new TelnetCommand("help", "h"){
			@Override
			public String getUsage()
			{
				return "help [command]";
			}

			@Override
			public String handle(String[] args)
			{
				if(args.length == 0)
				{
					StringBuilder sb = new StringBuilder();
					sb.append("Available commands:\n");
					for(TelnetCommand cmd : _commands)
						sb.append(cmd.getCommand()).append("\n");
					return sb.toString();
				}
				TelnetCommand cmd2 = TelnetServerHandler.this.getCommand(args[0]);
				if(cmd2 == null)
					return "Unknown command.\n";
				return "usage:\n" + cmd2.getUsage() + "\n";
			}
		});
		addHandler(new TelnetBan());
		addHandler(new TelnetConfig());
		addHandler(new TelnetDebug());
		addHandler(new TelnetPerfomance());
		addHandler(new TelnetSay());
		addHandler(new TelnetServer());
		addHandler(new TelnetStatus());
		addHandler(new TelnetWorld());
	}

	public void addHandler(TelnetCommandHolder handler)
	{
        _commands.addAll(handler.getCommands());
	}

	@Override
	public Set<TelnetCommand> getCommands()
	{
		return _commands;
	}

	private TelnetCommand getCommand(String command)
	{
		for(TelnetCommand cmd : _commands)
			if(cmd.equals(command))
				return cmd;
		return null;
	}

	private String tryHandleCommand(String command, String[] args)
	{
		TelnetCommand cmd = getCommand(command);
		if(cmd == null)
			return "Unknown command.\n";
		String response = cmd.handle(args);
		if(response == null)
			response = "usage:\n" + cmd.getUsage() + "\n";
		return response;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception
	{
		ctx.write("Welcome to L2 GameServer telnet console.\n");
		ctx.write("It is " + new Date() + " now.\n");
		if(!Config.TELNET_PASSWORD.isEmpty())
		{
			ctx.write("Password:");
			logged = false;
		}
		else
		{
			ctx.write("Type 'help' to see all available commands.\n");
			logged = true;
		}
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, String request) throws Exception
	{
		String response = null;
		boolean close = false;
		if(!logged)
			if(Config.TELNET_PASSWORD.equals(request))
			{
				logged = true;
				request = "";
			}
			else
			{
				response = "Wrong password!\n";
				close = true;
			}

		if(logged)
			if(request.isEmpty())
				response = "Type 'help' to see all available commands.\n";
			else if("exit".equals(request.toLowerCase()))
			{
				response = "Have a good day!\n";
				close = true;
			}
			else
			{
				Matcher m = COMMAND_ARGS_PATTERN.matcher(request);
				m.find();
				String command = m.group();
				List<String> args = new ArrayList<>();
				while(m.find())
				{
					String arg = m.group(1);
					if(arg == null)
						arg = m.group(0);
					args.add(arg);
				}
				response = tryHandleCommand(command, args.toArray(new String[0]));
			}

		ChannelFuture future = ctx.write(response);
		if(close)
			future.addListener(ChannelFutureListener.CLOSE);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		if(cause instanceof IOException)
			ctx.channel().close();
		else
			_log.error("", cause);
	}
}
