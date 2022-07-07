package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.utils.Location;

/**
 * Примеры пакетов:
 *
 * Ставит флажок на карте и показывает стрелку на компасе:
 * EB 00 00 00 00 01 00 00 00 40 2B FF FF 8C 3C 02 00 A0 F6 FF FF
 * Убирает флажок и стрелку
 * EB 02 00 00 00 02 00 00 00 40 2B FF FF 8C 3C 02 00 A0 F6 FF FF
 */
public class RadarControlPacket extends L2GameServerPacket
{
	private final int _x;
	private final int _y;
	private final int _z;
	private final int _type;
	private final int _showRadar;

	public RadarControlPacket(int showRadar, int type, Location loc)
	{
		this(showRadar, type, loc.x, loc.y, loc.z);
	}

	public RadarControlPacket(int showRadar, int type, int x, int y, int z)
	{
		_showRadar = showRadar;
		_type = type;
		_x = x;
		_y = y;
		_z = z;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_showRadar); // showRadar?? 0 = showRadar; 1 = delete radar;
		writeD(_type); // 1 - только стрелка над головой, 2 - флажок на карте
		writeD(_x);
		writeD(_y);
		writeD(_z);
	}
}
