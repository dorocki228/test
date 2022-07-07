package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.items.ItemInstance;

public class ExGetCrystalizingEstimation extends L2GameServerPacket
{
	private final int _crystalId;
	private final long _crystalCount;

	public ExGetCrystalizingEstimation(ItemInstance item)
	{
		_crystalId = /*item.getGrade().getCrystalId()*/ 0;
		_crystalCount = /*item.getCrystalCountOnCrystallize()*/ 0;
	}

	@Override
	protected final void writeImpl()
	{
		if(_crystalId > 0 && _crystalCount > 0L)
		{
            writeD(1);
            writeD(_crystalId);
			writeQ(_crystalCount);
			writeF(100.0);
		}
		else
            writeD(0);
	}
}
