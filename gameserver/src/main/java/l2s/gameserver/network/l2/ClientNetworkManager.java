/*
 * Copyright (C) 2004-2013 L2J Unity
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
package l2s.gameserver.network.l2;

import l2s.commons.network.NetworkManager;
import l2s.gameserver.network.EventLoopGroupManager;

/**
 * @author Nos
 */
public class ClientNetworkManager extends NetworkManager
{
	protected ClientNetworkManager()
	{
		super(EventLoopGroupManager.getInstance().getBossGroup(),
				EventLoopGroupManager.getInstance().getWorkerGroup(),
				new ClientInitializer());
	}

	@Override
	public void stop() throws InterruptedException {
		super.stop();
		EventLoopGroupManager.getInstance().shutdown();
	}

	public static ClientNetworkManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ClientNetworkManager _instance = new ClientNetworkManager();
	}
}
