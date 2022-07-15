package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import l2s.gameserver.instancemanager.MatchingRoomManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.matching.MatchingRoom;
import l2s.gameserver.network.l2.OutgoingPackets;

public class ListPartyWaitingPacket implements IClientOutgoingPacket
{
	private static final int ITEMS_PER_PAGE = 16;
	private final Collection<MatchingRoom> _rooms = new ArrayList<MatchingRoom>(ITEMS_PER_PAGE);
	private final int _page;

	public ListPartyWaitingPacket(int region, boolean allLevels, int page, Player activeChar)
	{
		_page = page;
		final List<MatchingRoom> temp = MatchingRoomManager.getInstance().getMatchingRooms(MatchingRoom.PARTY_MATCHING, region, allLevels, activeChar);

		final int first = Math.max((page - 1) * ITEMS_PER_PAGE, 0);
		final int firstNot = Math.min(page * ITEMS_PER_PAGE, temp.size());
		for(int i = first; i < firstNot; i++)
			_rooms.add(temp.get(i));
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.LIST_PARTY_WATING.writeId(packetWriter);
		packetWriter.writeD(_page);
		packetWriter.writeD(_rooms.size());

		for(MatchingRoom room : _rooms)
		{
			packetWriter.writeD(room.getId()); //room id
			packetWriter.writeS(room.getTopic()); // room name
			packetWriter.writeD(room.getLocationId());
			packetWriter.writeD(room.getMinLevel()); //min level
			packetWriter.writeD(room.getMaxLevel()); //max level
			packetWriter.writeD(room.getMaxMembersSize()); //max members coun
			packetWriter.writeS(room.getLeader() == null ? "None" : room.getLeader().getName());

			Collection<Player> players = room.getPlayers();
			packetWriter.writeD(players.size()); //members count
			for(Player player : players)
			{
				packetWriter.writeD(player.getClassId().getId());
				packetWriter.writeS(player.getName());
			}
		}
		packetWriter.writeD(0x00); // Total amount of parties
		packetWriter.writeD(0x00); // Total amount of party members

		return true;
	}
}