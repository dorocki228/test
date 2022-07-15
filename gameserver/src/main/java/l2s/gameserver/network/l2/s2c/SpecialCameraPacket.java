package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;

public class SpecialCameraPacket implements IClientOutgoingPacket
{
	private int _id;
	private int _dist;
	private int _yaw;
	private int _pitch;
	private int _time;
	private int _duration;
	private final int _turn;
	private final int _rise;
	private final int _widescreen;
	private final int _unknown;

	public SpecialCameraPacket(int id, int dist, int yaw, int pitch, int time, int duration)
	{
		_id = id;
		_dist = dist;
		_yaw = yaw;
		_pitch = pitch;
		_time = time;
		_duration = duration;
		_turn = 0;
		_rise = 0;
		_widescreen = 0;
		_unknown = 0;
	}

	public SpecialCameraPacket(int id, int dist, int yaw, int pitch, int time, int duration, int turn, int rise, int widescreen, int unk)
	{
		_id = id;
		_dist = dist;
		_yaw = yaw;
		_pitch = pitch;
		_time = time;
		_duration = duration;
		_turn = turn;
		_rise = rise;
		_widescreen = widescreen;
		_unknown = unk;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.SPECIAL_CAMERA.writeId(packetWriter);
		// ddddddddddd
		packetWriter.writeD(_id); // object id
		packetWriter.writeD(_dist); //расстояние до объекта
		packetWriter.writeD(_yaw); // North=90, south=270, east=0, west=180
		packetWriter.writeD(_pitch); // > 0:looks up,pitch < 0:looks down (угол наклона)
		packetWriter.writeD(_time); //faster that small value is
		packetWriter.writeD(_duration); //время анимации
		packetWriter.writeD(_turn);
		packetWriter.writeD(_rise);
		packetWriter.writeD(_widescreen);
		packetWriter.writeD(_unknown);

		return true;
	}
}