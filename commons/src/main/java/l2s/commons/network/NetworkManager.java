/*
 * Copyright (C) 2004-2015 L2J Unity
 * 
 * This file is part of L2J Unity.
 * 
 * L2J Unity is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Unity is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2s.commons.network;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Nos
 */
public class NetworkManager
{
	private final ServerBootstrap _serverBootstrap;
	
	private Collection<ChannelFuture> channelFutures = new ArrayList<>(1);
	
	public NetworkManager(EventLoopGroup bossGroup, EventLoopGroup workerGroup, ChannelInitializer<SocketChannel> clientInitializer)
	{
		_serverBootstrap = new ServerBootstrap()
				.group(bossGroup, workerGroup)
				.channel(NioServerSocketChannel.class)
				.childHandler(clientInitializer)
				.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
				.option(ChannelOption.SO_REUSEADDR, true)
				//.option(ChannelOption.SO_KEEPALIVE, true)
				//.option(ChannelOption.TCP_NODELAY, true)
				//.option(ChannelOption.SO_LINGER, 1_000)
				//.option(ChannelOption.AUTO_CLOSE, false)
				.option(ChannelOption.SO_BACKLOG, 1024)
				//.option(ChannelOption.ALLOW_HALF_CLOSURE, true)
				.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
				.childOption(ChannelOption.SO_SNDBUF, 1024 * 1024)
				.childOption(ChannelOption.SO_RCVBUF, 1024 * 1024)
				.childOption(ChannelOption.SO_LINGER, 1_000)
				.childOption(ChannelOption.AUTO_CLOSE, false);
	}
	
	public Collection<ChannelFuture> getChannelFuture()
	{
		return channelFutures;
	}
	
	public void start(InetAddress address, int tcpPort) throws InterruptedException
	{
		InetSocketAddress socketAddress = address == null ? new InetSocketAddress(tcpPort) : new InetSocketAddress(address, tcpPort);
		channelFutures.add(_serverBootstrap.bind(socketAddress).sync());
		NetworkLogger.INSTANCE.getLogger().atInfo().log( "Listening on %s", socketAddress );
	}
	
	public void stop() throws InterruptedException
	{
		channelFutures.forEach(
				channelFuture -> {
					try {
						channelFuture.channel().close().sync();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
		);
	}
}
