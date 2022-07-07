package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.utils.Location;

public class PlaySoundPacket extends L2GameServerPacket
{
	public static final L2GameServerPacket SIEGE_VICTORY;
	public static final L2GameServerPacket B04_S01;
	public static final L2GameServerPacket HB01;
	public static final L2GameServerPacket BROKEN_KEY;
	private final Type _type;
	private final String _soundFile;
	private final int _hasCenterObject;
	private final int _objectId;
	private final int _x;
	private final int _y;
	private final int _z;

	public PlaySoundPacket(String soundFile)
	{
		this(Type.SOUND, soundFile, 0, 0, 0, 0, 0);
	}

	public PlaySoundPacket(Type type, String soundFile, int c, int objectId, Location loc)
	{
		this(type, soundFile, c, objectId, loc == null ? 0 : loc.x, loc == null ? 0 : loc.y, loc == null ? 0 : loc.z);
	}

	public PlaySoundPacket(Type type, String soundFile, int c, int objectId, int x, int y, int z)
	{
		_type = type;
		_soundFile = soundFile;
		_hasCenterObject = c;
		_objectId = objectId;
		_x = x;
		_y = y;
		_z = z;
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_type.ordinal());
		writeS(_soundFile);
        writeD(_hasCenterObject);
        writeD(_objectId);
        writeD(_x);
        writeD(_y);
        writeD(_z);
	}

	static
	{
		SIEGE_VICTORY = new PlaySoundPacket("Siege_Victory");
		B04_S01 = new PlaySoundPacket("B04_S01");
		HB01 = new PlaySoundPacket(Type.MUSIC, "HB01", 0, 0, 0, 0, 0);
		BROKEN_KEY = new PlaySoundPacket("ItemSound2.broken_key");
	}

	public enum Type
	{
		SOUND,
		MUSIC,
		VOICE
    }
}
