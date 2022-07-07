package l2s.gameserver.network.l2.s2c;

public class ExCuriousHouseObserveList extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
        writeD(0);
		// while(true) ???
		{
            writeD(0);
			writeS("");
            writeH(0);
            writeD(0);
		}
	}
}
