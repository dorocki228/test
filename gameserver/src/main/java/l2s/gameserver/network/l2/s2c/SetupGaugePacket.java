package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Creature;

public class SetupGaugePacket extends L2GameServerPacket
{
	private final int _charId;
	private final int _color;
	private final int _time;
	private final int _lostTime;

	public SetupGaugePacket(Creature character, Colors color, int time)
	{
		this(character, color, time, time);
	}

	public SetupGaugePacket(Creature character, Colors color, int time, int lostTime)
	{
		_charId = character.getObjectId();
		_color = color.ordinal();
		_time = time;
		_lostTime = lostTime;
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_charId);
        writeD(_color);
        writeD(_lostTime);
        writeD(_time);
	}

	public enum Colors
	{
		NONE,
		RED,
		BLUE,
		GREEN
    }
}
