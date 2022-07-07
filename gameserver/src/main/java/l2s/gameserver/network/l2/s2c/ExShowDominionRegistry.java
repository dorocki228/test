package l2s.gameserver.network.l2.s2c;

public class ExShowDominionRegistry extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
        writeD(0);
		writeS("");
		writeS("");
		writeS("");
        writeD(0);
        writeD(0);
        writeD(0);
        writeD(0);
        writeD(0);
        writeD(0);
        writeD(1);
        writeD(0);
	}
}
