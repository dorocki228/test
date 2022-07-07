package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;

public class ExVoteSystemInfoPacket extends L2GameServerPacket
{
	private final int _receivedRec;
	private final int _givingRec;
	private final int _time;
	private final int _bonusPercent;
	private final boolean _showTimer;

	public ExVoteSystemInfoPacket(Player player)
	{
		_receivedRec = player.getRecomLeft();
		_givingRec = player.getRecomHave();
		_time = 0;
		_bonusPercent = 0;
		_showTimer = false;
	}

	@Override
	protected void writeImpl()
	{
        writeD(_receivedRec);
        writeD(_givingRec);
        writeD(_time);
        writeD(_bonusPercent);
        writeD(_showTimer ? 1 : 0);
	}
}
