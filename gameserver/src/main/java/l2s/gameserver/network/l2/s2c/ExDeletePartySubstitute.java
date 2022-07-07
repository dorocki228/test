package l2s.gameserver.network.l2.s2c;

public class ExDeletePartySubstitute extends L2GameServerPacket
{
	private final int _obj;

	public ExDeletePartySubstitute(int objectId)
	{
		_obj = objectId;
	}

	@Override
	protected void writeImpl()
	{
        writeD(_obj);
	}
}
