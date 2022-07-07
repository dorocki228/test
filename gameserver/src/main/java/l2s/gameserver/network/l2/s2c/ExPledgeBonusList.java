package l2s.gameserver.network.l2.s2c;

public class ExPledgeBonusList extends L2GameServerPacket
{
	@Override
	protected final void writeImpl()
	{
		writeD(55168);
		writeD(55169);
		writeD(55170);
		writeD(55171);
		writeD(70020);
		writeD(70021);
		writeD(70022);
		writeD(70023);
	}
}
