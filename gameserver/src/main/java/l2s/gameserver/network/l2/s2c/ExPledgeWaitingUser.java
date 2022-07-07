package l2s.gameserver.network.l2.s2c;

public class ExPledgeWaitingUser extends L2GameServerPacket
{
	private final int _charId;
	private final String _desc;

	public ExPledgeWaitingUser(int charId, String desc)
	{
		_charId = charId;
		_desc = desc;
	}

	@Override
	protected void writeImpl()
	{
        writeD(_charId);
		writeS(_desc);
	}
}
