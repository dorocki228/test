package l2s.gameserver.network.l2.s2c;

public class ExTutorialList extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeS("");
        writeD(0);
        writeD(0);
        writeD(0);
	}
}
