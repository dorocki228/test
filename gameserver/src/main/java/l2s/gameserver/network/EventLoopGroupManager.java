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
package l2s.gameserver.network;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

/**
 * @author Nos
 */
public class EventLoopGroupManager
{
	// TODO config
	private static int IO_PACKET_THREAD_CORE_SIZE = 4;

	private final EventLoopGroup _bossGroup;
	private final EventLoopGroup _workerGroup;

	public EventLoopGroupManager() {
		if (Epoll.isAvailable()) {
			_bossGroup = new EpollEventLoopGroup(1);
			_workerGroup = new EpollEventLoopGroup(IO_PACKET_THREAD_CORE_SIZE);
		} else {
			_bossGroup = new NioEventLoopGroup(1);
			_workerGroup = new NioEventLoopGroup(IO_PACKET_THREAD_CORE_SIZE);
		}
	}

	public EventLoopGroup getBossGroup()
	{
		return _bossGroup;
	}

	public EventLoopGroup getWorkerGroup()
	{
		return _workerGroup;
	}

	public void shutdown()
	{
		_bossGroup.shutdownGracefully();
		_workerGroup.shutdownGracefully();
	}

	public static EventLoopGroupManager getInstance()
	{
		return SingletonHolder._instance;
	}

	private static class SingletonHolder
	{
		protected static final EventLoopGroupManager _instance = new EventLoopGroupManager();
	}
}
