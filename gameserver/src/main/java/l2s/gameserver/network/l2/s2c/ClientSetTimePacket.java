package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.time.GameTimeService;

public class ClientSetTimePacket extends L2GameServerPacket
{
	public static final L2GameServerPacket STATIC;

	@Override
	protected final void writeImpl()
	{
        writeD(GameTimeService.INSTANCE.getGameTime());
        writeD(6);
	}

	static
	{
		STATIC = new ClientSetTimePacket();
	}
}
