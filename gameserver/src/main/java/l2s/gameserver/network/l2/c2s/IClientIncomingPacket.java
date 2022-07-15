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
package l2s.gameserver.network.l2.c2s;

import l2s.commons.network.IIncomingPacket;
import l2s.commons.network.PacketReader;
import l2s.gameserver.network.l2.GameClient;

/**
 * Packets received by the game server from clients
 * @author KenM
 */
public interface IClientIncomingPacket extends IIncomingPacket<GameClient>
{
	@Override
	default boolean canBeRead(GameClient client) {
		return client.checkFloodProtection(getFloodProtectorType(), getClass().getSimpleName());
	}

	/**
	 * Reads a packet.
	 * @param client the client
	 * @param packet the packet reader
	 * @return {@code true} if packet was read successfully, {@code false} otherwise.
	 */
	@Override
	default boolean read(GameClient client, PacketReader packet) {
		return readImpl(client, packet);
	}

	/**
	 * Reads a packet.
	 * @param client the client
	 * @param packet the packet reader
	 * @return {@code true} if packet was read successfully, {@code false} otherwise.
	 */
	boolean readImpl(GameClient client, PacketReader packet);

	default String getType()
	{
		return "[C] " + getClass().getSimpleName();
	}

	default String getFloodProtectorType()
	{
		return getClass().getSimpleName();
	}
}
