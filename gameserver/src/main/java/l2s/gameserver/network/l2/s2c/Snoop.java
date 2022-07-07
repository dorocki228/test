package l2s.gameserver.network.l2.s2c;

public class Snoop extends L2GameServerPacket
{
	private final int _convoID;
	private final String _name;
	private final int _type;
	private int _fStringId;
	private final String _speaker;
	private String _msg;
	private String[] _params;

	public Snoop(int id, String name, int type, String speaker, String msg, int fStringId, String... params)
	{
		_convoID = id;
		_name = name;
		_type = type;
		_speaker = speaker;
		_fStringId = fStringId;
		_params = params;
	}

	public Snoop(int id, String name, int type, String speaker, String msg)
	{
		_convoID = id;
		_name = name;
		_type = type;
		_speaker = speaker;
		_msg = msg;
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_convoID);
		writeS(_name);
        writeD(0);
        writeD(_type);
		writeS(_speaker);
		writeS(_msg);
	}
}
