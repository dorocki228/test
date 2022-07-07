package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

public class StatusUpdatePacket extends L2GameServerPacket
{
	public static final int CUR_HP = 9;
	public static final int MAX_HP = 10;
	public static final int CUR_MP = 11;
	public static final int MAX_MP = 12;
	public static final int CUR_LOAD = 14;
	public static final int MAX_LOAD = 15;
	public static final int PVP_FLAG = 26;
	public static final int KARMA = 27;
	public static final int CUR_CP = 33;
	public static final int MAX_CP = 34;
	public static final int DAMAGE = 35;
	private final int _objectId;
	private final int _playerId;
	private final List<Attribute> _attributes;

	public StatusUpdatePacket(int objectId)
	{
		_attributes = new ArrayList<>();
		_objectId = objectId;
		_playerId = 0;
	}

	public StatusUpdatePacket(int objectId, int playerId)
	{
		_attributes = new ArrayList<>();
		_objectId = objectId;
		_playerId = playerId;
	}

	public StatusUpdatePacket addAttribute(int id, int level)
	{
		_attributes.add(new Attribute(id, level));
		return this;
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_objectId);
        writeD(_playerId);
        writeC(0);
        writeC(_attributes.size());
		for(Attribute temp : _attributes)
		{
            writeC(temp.id);
            writeD(temp.value);
		}
	}

	public boolean hasAttributes()
	{
		return !_attributes.isEmpty();
	}

	class Attribute
	{
		public final int id;
		public final int value;

		Attribute(int id, int value)
		{
			this.id = id;
			this.value = value;
		}
	}
}
