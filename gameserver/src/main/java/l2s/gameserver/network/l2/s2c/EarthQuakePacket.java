package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.utils.Location;

public class EarthQuakePacket extends L2GameServerPacket
{
	private final Location _loc;
	private final int _intensity;
	private final int _duration;

	public EarthQuakePacket(Location loc, int intensity, int duration)
	{
		_loc = loc;
		_intensity = intensity;
		_duration = duration;
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_loc.x);
        writeD(_loc.y);
        writeD(_loc.z);
        writeD(_intensity);
        writeD(_duration);
        writeD(0);
	}
}
