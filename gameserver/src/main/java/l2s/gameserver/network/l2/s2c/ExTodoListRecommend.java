package l2s.gameserver.network.l2.s2c;

public class ExTodoListRecommend extends L2GameServerPacket
{
	@Override
	protected final void writeImpl()
	{
        writeH(0);
        int instancesCount = 0;
        for(int i = 0; i < instancesCount; ++i)
		{
			writeC(0);
			writeS("");
			writeS("");
			writeH(0);
			writeH(0);
			writeH(0);
			writeH(0);
			writeC(0);
			writeC(0);
		}
	}
}
