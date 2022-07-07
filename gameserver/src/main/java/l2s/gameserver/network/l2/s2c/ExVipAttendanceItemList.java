package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.data.xml.holder.AttendanceRewardHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.templates.item.data.AttendanceRewardData;

import java.util.Collection;
import java.util.Collections;

public class ExVipAttendanceItemList extends L2GameServerPacket
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
		_rewards = (Collection<AttendanceRewardData>) (_indexToReceive > 0 ? AttendanceRewardHolder.getInstance().getRewards(player.hasPremiumAccount()) : Collections.emptyList());
	}

	@Override
	protected void writeImpl()
	{
        writeC(_indexToReceive);
        writeC(_lastReceivedIndex);
        writeD(0);
        writeD(0);
        writeC(1);
        writeC(!_received);
        writeC(250);
        writeC(_rewards.size());
		_rewards.forEach(reward -> {
            writeD(reward.getId());
			writeQ(reward.getCount());
            writeC(reward.isUnknown());
            writeC(reward.isBest());
			return;
		});
        writeC(0);
        writeD(0);
	}
}
