package l2s.gameserver.network.l2.s2c;

public class ExShowCommission extends L2GameServerPacket
{
	@Override
	protected final void writeImpl()
	{
        writeD(1);
	}
}
