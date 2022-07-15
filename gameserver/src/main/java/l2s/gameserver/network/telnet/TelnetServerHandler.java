package l2s.gameserver.network.telnet;

import com.google.common.flogger.FluentLogger;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import l2s.gameserver.Config;
import l2s.gameserver.network.telnet.commands.TelnetServer;
import l2s.gameserver.network.telnet.commands.*;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TelnetServerHandler extends SimpleChannelInboundHandler<String> implements TelnetCommandHolder
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	

	//The following regex splits a line into its parts, separated by spaces, unless there are quotes, in which case the quotes take precedence.  
	private static final Pattern COMMAND_ARGS_PATTERN = Pattern.compile("\"([^\"]*)\"|([^\\s]+)");

	private Set<TelnetCommand> _commands = new LinkedHashSet<TelnetCommand>();

	private static final AttributeKey<Boolean> LOGGED = AttributeKey.valueOf("LOGGED");

	public TelnetServerHandler()
	{
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
					{
						sb.append(cmd.getCommand()).append("\n");
					}

					return sb.toString();
				}
				else
				{
					TelnetCommand cmd = TelnetServerHandler.this.getCommand(args[0]);
					if(cmd == null)
						return "Unknown command.\n";

					return "usage:\n" + cmd.getUsage() + "\n";
				}
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
		// Send greeting for a new connection.
		ctx.write("Welcome to L2 GameServer telnet console.\n");
		ctx.write("It is " + new Date() + " now.\n");
		if(!Config.TELNET_PASSWORD.isEmpty())
		{
			// Ask password
			ctx.channel().attr(LOGGED).set(false);
			ctx.write("Password:");
		}
		else
		{
			ctx.write("Type 'help' to see all available commands.\n");
			ctx.channel().attr(LOGGED).set(true);
		}
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, String request) throws Exception
	{
		// Generate and write a response.
		String response = null;
		boolean close = false;

		if (ctx.channel().attr(LOGGED).get()) {
			if (request.isEmpty())
				response = "Type 'help' to see all available commands.\n";
			else if ("exit".equals(request.toLowerCase())) {
				response = "Have a good day!\n";
				close = true;
			} else {
				Matcher m = COMMAND_ARGS_PATTERN.matcher(request);

				m.find();
				String command = m.group();

				List<String> args = new ArrayList<String>();
				String arg;
				while (m.find()) {
					arg = m.group(1);
					if (arg == null)
						arg = m.group(0);
					args.add(arg);
				}

				response = tryHandleCommand(command, args.toArray(new String[0]));
			}
		} else {
			if(Config.TELNET_PASSWORD.equals(request))
			{
				ctx.channel().attr(LOGGED).set(true);
				request = "";
			}
			else
			{
				response = "Wrong password!\n";
				close = true;
			}
		}

		// We do not need to write a ChannelBuffer here.
		// We know the encoder inserted at TelnetServerInitializer will do the conversion.
		ChannelFuture future = ctx.write(response);

		// Close the connection after sending 'Have a good day!'
		// if the client has sent 'exit'.
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
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(cause).log( "" );
	}
}
