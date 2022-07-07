package l2s.gameserver.network.l2.s2c;

public class ExSubPledgetSkillAdd extends L2GameServerPacket
{
	private final int _type;
	private final int _id;
	private final int _level;

	public ExSubPledgetSkillAdd(int type, int id, int level)
	{
		_type = type;
		_id = id;
		_level = level;
	}

	@Override
	protected void writeImpl()
	{
        writeD(_type);
        writeD(_id);
        writeD(_level);
	}
}
