package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.instancemanager.MatchingRoomManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.matching.MatchingRoom;

public class RequestPartyMatchDetail implements IClientIncomingPacket
{
	private int _roomId;
	private int _locations;
	private int _level;

	/**
	 * Format: dddd
	 */
	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_roomId = packet.readD(); // room id, если 0 то autojoin
		_locations = packet.readD(); // location
		_level = packet.readD(); // 1 - all, 0 - my level (только при autojoin)
		//packet.readD();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player player = client.getActiveChar();
		if(player == null)
			return;

		if(player.getMatchingRoom() != null)
			return;

		if(_roomId > 0)
		{
			MatchingRoom room = MatchingRoomManager.getInstance().getMatchingRoom(MatchingRoom.PARTY_MATCHING, _roomId);
			if(room == null)
				return;

			room.addMember(player);
		}
		else
		{
			for(MatchingRoom room : MatchingRoomManager.getInstance().getMatchingRooms(MatchingRoom.PARTY_MATCHING, _locations, _level == 1, player))
				if(room.addMember(player))
					break;
		}
	}
}