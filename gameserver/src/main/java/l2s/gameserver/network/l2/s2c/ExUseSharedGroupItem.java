package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;
import l2s.gameserver.skills.TimeStamp;

public class ExUseSharedGroupItem implements IClientOutgoingPacket
{
	private int _itemId, _grpId, _remainedTime, _totalTime;

	public ExUseSharedGroupItem(int grpId, TimeStamp timeStamp)
	{
		_grpId = grpId;
		_itemId = timeStamp.getId();
		_remainedTime = (int) (timeStamp.getReuseCurrent() / 1000);
		_totalTime = (int) (timeStamp.getReuseBasic() / 1000);
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_USE_SHARED_GROUP_ITEM.writeId(packetWriter);
		packetWriter.writeD(_itemId);
		packetWriter.writeD(_grpId);
		packetWriter.writeD(_remainedTime);
		packetWriter.writeD(_totalTime);

		return true;
	}
}