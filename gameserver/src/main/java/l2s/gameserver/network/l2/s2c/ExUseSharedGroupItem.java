package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.skills.TimeStamp;

public class ExUseSharedGroupItem extends L2GameServerPacket
{
	private final int _itemId;
	private final int _grpId;
	private final int _remainedTime;
	private final int _totalTime;

	public ExUseSharedGroupItem(int grpId, TimeStamp timeStamp)
	{
		_grpId = grpId;
		_itemId = timeStamp.getId();
		_remainedTime = (int) (timeStamp.getReuseCurrent() / 1000L);
		_totalTime = (int) (timeStamp.getReuseBasic() / 1000L);
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_itemId);
        writeD(_grpId);
        writeD(_remainedTime);
        writeD(_totalTime);
	}
}
