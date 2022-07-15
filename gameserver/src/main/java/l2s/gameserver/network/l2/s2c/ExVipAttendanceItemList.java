package l2s.gameserver.network.l2.s2c;

import java.util.Collection;
import java.util.Collections;

import l2s.gameserver.data.xml.holder.AttendanceRewardHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.OutgoingExPackets;
import l2s.gameserver.templates.item.data.AttendanceRewardData;

/**
 * @author Bonux
**/
public class ExVipAttendanceItemList implements IClientOutgoingPacket
{
	private final int _indexToReceive;
	private final int _lastReceivedIndex;
	private final boolean _received;
	private final Collection<AttendanceRewardData> _rewards;
	
	public ExVipAttendanceItemList(Player player)
	{
		_indexToReceive = player.getAttendanceRewards().getNextRewardIndex();
		_lastReceivedIndex = player.getAttendanceRewards().getReceivedRewardIndex();
		_received = player.getAttendanceRewards().isReceived();
		_rewards = _indexToReceive > 0 ? AttendanceRewardHolder.getInstance().getRewards(player.hasPremiumAccount()) : Collections.emptyList();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_VIP_ATTENDANCE_ITEMLIST.writeId(packetWriter);
		packetWriter.writeC(_indexToReceive); // ID to receive
		packetWriter.writeC(_lastReceivedIndex); // Received count
		packetWriter.writeD(0x00); // UNK
		packetWriter.writeD(0x00); // UNK
		packetWriter.writeC(0x01); // UNK
		packetWriter.writeC(!_received); // Not Received
		packetWriter.writeC(0xFA); // UNK
		packetWriter.writeC(_rewards.size()); // Items Count
		_rewards.forEach(reward ->
		{
			packetWriter.writeD(reward.getId()); // Item ID
			packetWriter.writeQ(reward.getCount()); // Item count
			packetWriter.writeC(reward.isUnknown()); // UNK
			packetWriter.writeC(reward.isBest()); // Is luxary
		});
		packetWriter.writeC(0x00); // VIP rewards
		packetWriter.writeD(0x00); // UNK

		return true;
	}
}