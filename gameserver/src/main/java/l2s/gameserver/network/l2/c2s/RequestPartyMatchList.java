package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.matching.MatchingRoom;
import l2s.gameserver.model.matching.PartyMatchingRoom;

public class RequestPartyMatchList implements IClientIncomingPacket
{
	private int _lootDist;
	private int _maxMembers;
	private int _minLevel;
	private int _maxLevel;
	private int _roomId;
	private String _roomTitle;

	/**
	 * Format:(ch) dddddS
	 */
	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_roomId = packet.readD();
		_maxMembers = packet.readD();
		_minLevel = packet.readD();
		_maxLevel = packet.readD();
		_lootDist = packet.readD();
		_roomTitle = packet.readS(64);
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player player = client.getActiveChar();
		if(player == null)
			return;

		Party party = player.getParty();
		if(party != null && party.getPartyLeader() != player)
			return;

		MatchingRoom room = player.getMatchingRoom();
		if(room == null)
		{
			room = new PartyMatchingRoom(player, _minLevel, _maxLevel, _maxMembers, _lootDist, _roomTitle);
			if(party != null)
				for(Player member : party.getPartyMembers())
					if(member != null && member != player)
						room.addMemberForce(member);
		}
		else if(room.getId() == _roomId && room.getType() == MatchingRoom.PARTY_MATCHING && room.getLeader() == player)
		{
			room.setMinLevel(_minLevel);
			room.setMaxLevel(_maxLevel);
			room.setMaxMemberSize(_maxMembers);
			room.setTopic(_roomTitle);
			room.setLootType(_lootDist);
			room.broadCast(room.infoRoomPacket());
		}
	}
}