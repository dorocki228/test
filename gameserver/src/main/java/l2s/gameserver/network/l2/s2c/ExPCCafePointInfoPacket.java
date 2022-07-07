package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;

public class ExPCCafePointInfoPacket extends L2GameServerPacket
{
	private final int _mAddPoint;
	private final int _mPeriodType;
	private final int _pointType;
	private final int _pcBangPoints;
	private final int _remainTime;

	public ExPCCafePointInfoPacket(Player player, int mAddPoint, int mPeriodType, int pointType, int remainTime)
	{
		_pcBangPoints = player.getPcBangPoints();
		_mAddPoint = mAddPoint;
		_mPeriodType = mPeriodType;
		_pointType = pointType;
		_remainTime = remainTime;
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_pcBangPoints);
        writeD(_mAddPoint);
        writeC(_mPeriodType);
        writeD(_remainTime);
        writeC(_pointType);
        writeD(0);
	}
}
