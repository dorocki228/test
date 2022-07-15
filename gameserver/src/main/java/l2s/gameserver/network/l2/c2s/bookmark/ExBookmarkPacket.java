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
package l2s.gameserver.network.l2.c2s.bookmark;

import l2s.commons.network.IIncomingPacket;
import l2s.commons.network.PacketReader;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.c2s.*;

/**
 * @author Sdw
 */
public class ExBookmarkPacket implements IClientIncomingPacket
{
	private IIncomingPacket<GameClient> _exBookmarkPacket;
	
	@Override
	public boolean readImpl(GameClient client, PacketReader packet)
	{
		int subId = packet.readD();
		
		switch (subId)
		{
			case 0:
			{
				_exBookmarkPacket = new RequestBookMarkSlotInfo();
				break;
			}
			case 1:
			{
				_exBookmarkPacket = new RequestSaveBookMarkSlot();
				break;
			}
			case 2:
			{
				_exBookmarkPacket = new RequestModifyBookMarkSlot();
				break;
			}
			case 3:
			{
				_exBookmarkPacket = new RequestDeleteBookMarkSlot();
				break;
			}
			case 4:
			{
				_exBookmarkPacket = new RequestTeleportBookMark();
				break;
			}
			case 5:
			{
				_exBookmarkPacket = new RequestChangeBookMarkSlot();
				break;
			}
		}
		return (_exBookmarkPacket != null) && _exBookmarkPacket.read(client, packet);
	}
	
	@Override
	public void run(GameClient client) throws Exception
	{
		_exBookmarkPacket.run(client);
	}
}
