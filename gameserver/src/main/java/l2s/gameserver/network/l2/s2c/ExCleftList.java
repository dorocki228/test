package l2s.gameserver.network.l2.s2c;

public class ExCleftList extends L2GameServerPacket
{
	public static final int CleftType_Close = -1;
	public static final int CleftType_Total = 0;
	public static final int CleftType_Add = 1;
	public static final int CleftType_Remove = 2;
	public static final int CleftType_TeamChange = 3;
	private final int CleftType;

	public ExCleftList()
	{
		CleftType = 0;
	}

	@Override
	protected void writeImpl()
	{
        writeD(CleftType);
		switch(CleftType)
		{
			case 0:
			{}
			case 1:
			{}
			case 2:
			{}
		}
	}
}
