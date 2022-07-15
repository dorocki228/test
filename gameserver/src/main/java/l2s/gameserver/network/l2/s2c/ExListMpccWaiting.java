package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.network.l2.OutgoingExPackets;
import org.apache.commons.lang3.StringUtils;
import l2s.gameserver.instancemanager.MatchingRoomManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.matching.MatchingRoom;

/**
 * @author VISTALL
 */
public class ExListMpccWaiting implements IClientOutgoingPacket
{
	private static final int ITEMS_PER_PAGE = 10;
	private int _page;
	private List<MatchingRoom> _list;

	public ExListMpccWaiting(Player player, int page, int location, boolean allLevels)
	{
		int first = (page - 1) * ITEMS_PER_PAGE;
		int firstNot = page * ITEMS_PER_PAGE;

		List<MatchingRoom> temp = MatchingRoomManager.getInstance().getMatchingRooms(MatchingRoom.CC_MATCHING, location, allLevels, player);
		_page = page;
		_list = new ArrayList<MatchingRoom>(ITEMS_PER_PAGE);

		for(int i = 0; i < temp.size(); i++)
		{
			if (i < first || i >= firstNot)
				continue;

			_list.add(temp.get(i));
		}
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_LIST_MPCC_WAITING.writeId(packetWriter);
		packetWriter.writeD(_page);
		packetWriter.writeD(_list.size());
		for(MatchingRoom room : _list)
		{
			packetWriter.writeD(room.getId());
			Player leader = room.getLeader();
			packetWriter.writeS(leader == null ? StringUtils.EMPTY : leader.getName());
			packetWriter.writeD(room.getPlayers().size());
			packetWriter.writeD(room.getMinLevel());
			packetWriter.writeD(room.getMaxLevel());
			packetWriter.writeD(1);  //min group
			packetWriter.writeD(room.getMaxMembersSize());   //max group
			packetWriter.writeS(room.getTopic());
		}
		packetWriter.writeD(0x00); // Total amount of parties
		packetWriter.writeD(0x00); // Total amount of party members

		return true;
	}
}
