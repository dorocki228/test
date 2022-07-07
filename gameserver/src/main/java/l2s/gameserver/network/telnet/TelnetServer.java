package l2s.gameserver.network.telnet;

import com.google.common.util.concurrent.AbstractIdleService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import l2s.gameserver.Config;

import java.net.InetSocketAddress;

public class TelnetServer extends AbstractIdleService
{
	private final EventLoopGroup eventLoopGroup;

	public TelnetServer()
	{
		eventLoopGroup = new NioEventLoopGroup(1);
	}

	@Override
	protected void startUp() throws Exception {
		ServerBootstrap b = new ServerBootstrap();
		b.group(eventLoopGroup)
				.channel(NioServerSocketChannel.class)
				.handler(new LoggingHandler(LogLevel.INFO))
				.childHandler(new TelnetServerInitializer(null));

		String hostname = "*".equals(Config.TELNET_HOSTNAME) ? null : Config.TELNET_HOSTNAME;
		InetSocketAddress inetSocketAddress = new InetSocketAddress(hostname, Config.TELNET_PORT);
		b.bind(inetSocketAddress);
	}

	@Override
	protected void shutDown() throws Exception {
		eventLoopGroup.shutdownGracefully();
	}
}
