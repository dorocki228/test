package l2s.gameserver.network.l2.s2c;

public class ExShowOwnthingPos extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
        writeD(0);
	}
}
