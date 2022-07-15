package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.data.xml.holder.InstantZoneHolder;
import l2s.gameserver.instancemanager.MatchingRoomManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.OutgoingExPackets;


public class ExListPartyMatchingWaitingRoom implements IClientOutgoingPacket
{
	private static final int ITEMS_PER_PAGE = 64;
	private final List<PartyMatchingWaitingInfo> _waitingList = new ArrayList<PartyMatchingWaitingInfo>(ITEMS_PER_PAGE);
	private final int _fullSize;

	public ExListPartyMatchingWaitingRoom(Player searcher, int minLevel, int maxLevel, int page, int[] classes)
	{
		final List<Player> temp = MatchingRoomManager.getInstance().getWaitingList(minLevel, maxLevel, classes);
		_fullSize = temp.size();

		final int first = Math.max((page - 1) * ITEMS_PER_PAGE, 0);
		final int firstNot = Math.min(page * ITEMS_PER_PAGE, _fullSize);
		for(int i = first; i < firstNot; i++)
			_waitingList.add(new PartyMatchingWaitingInfo(temp.get(i)));
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_LIST_PARTY_MATCHING_WAITING_ROOM.writeId(packetWriter);
		packetWriter.writeD(_fullSize);
		packetWriter.writeD(_waitingList.size());
		for(PartyMatchingWaitingInfo waitingInfo : _waitingList)
		{
			packetWriter.writeS(waitingInfo.name);
			packetWriter.writeD(waitingInfo.classId);
			packetWriter.writeD(waitingInfo.level);
			packetWriter.writeD(waitingInfo.locationId);
			packetWriter.writeD(waitingInfo.instanceReuses.size());
			for(int i : waitingInfo.instanceReuses)
				packetWriter.writeD(i);
		}

		return true;
	}

	static class PartyMatchingWaitingInfo
	{
		public final int classId, level, locationId;
		public final String name;
		public final List<Integer> instanceReuses;

		public PartyMatchingWaitingInfo(Player member)
		{
			name = member.getName();
			classId = member.getClassId().getId();
			level = member.getLevel();
			locationId = MatchingRoomManager.getInstance().getLocation(member);
			instanceReuses = InstantZoneHolder.getInstance().getLockedInstancesList(member);
		}
	}
}