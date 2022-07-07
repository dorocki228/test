package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.utils.Location;

public class ExShowTerritory extends L2GameServerPacket
{

	private final int _minZ;
	private final int _maxZ;
	private final List<Location> _vertices = new ArrayList<>();
	
	public ExShowTerritory(int minZ, int maxZ)
	{
		_minZ = minZ;
		_maxZ = maxZ;
	}
	
	public void addVertice(Location loc)
	{
		_vertices.add(loc);
	}
	

	@Override
	protected void writeImpl()
	{
		//writeEx(0x89);
		//writeEx(0x8C);
		writeD(_vertices.size());
		writeD(_minZ);
		writeD(_maxZ);
		for (Location loc : _vertices)
		{
			writeD(loc.getX());
			writeD(loc.getY());
		}
	}
}