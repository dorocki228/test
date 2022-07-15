package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Creature;
import l2s.gameserver.network.l2.OutgoingPackets;

public class SetupGaugePacket implements IClientOutgoingPacket
{
	public static enum Colors
	{
		NONE,
		RED,
		BLUE,
		GREEN;
	}

	private int _charId;
	private int _color;
	private int _time;
	private int _lostTime;

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
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.SETUP_GAUGE.writeId(packetWriter);
		packetWriter.writeD(_charId);
		packetWriter.writeD(_color);
		packetWriter.writeD(_lostTime);
		packetWriter.writeD(_time);

		return true;
	}
}