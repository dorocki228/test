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

import static com.google.common.flogger.LazyArgs.lazy;

import com.google.common.flogger.FluentLogger;
import l2s.commons.network.IIncomingPacket;
import l2s.commons.network.PacketReader;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.IncomingExPackets;

/**
 * @author Nos
 */
public class ExPacket implements IClientIncomingPacket
{
	private static final FluentLogger LOGGER = FluentLogger.forEnclosingClass();

	
	
	private IncomingExPackets _exIncomingPacket;
	private IIncomingPacket<GameClient> _exPacket;
	
	@Override
	public boolean readImpl(GameClient client, PacketReader packet)
	{
		int exPacketId = packet.readH() & 0xFFFF;
		if ((exPacketId < 0) || (exPacketId >= IncomingExPackets.PACKET_ARRAY.length))
		{
			return false;
		}
		
		_exIncomingPacket = IncomingExPackets.PACKET_ARRAY[exPacketId];
		if (_exIncomingPacket == null)
		{
			LOGGER.atFine().log( "%s: Unknown packet: %s", lazy(() -> getClass().getSimpleName()), lazy(() -> Integer.toHexString(exPacketId)) );
			return false;
		}
		
		_exPacket = _exIncomingPacket.newIncomingPacket();
		return (_exPacket != null) && _exPacket.read(client, packet);
	}
	
	@Override
	public void run(GameClient client) throws Exception
	{
		if (!_exIncomingPacket.getConnectionStates().contains(client.getConnectionState()))
		{
			LOGGER.atFine().log( "%s: Connection at invalid state: %s Required State: %s", _exIncomingPacket, lazy(() -> client.getConnectionState()), lazy(() -> _exIncomingPacket.getConnectionStates()) );
			return;
		}
		_exPacket.run(client);
	}
}
