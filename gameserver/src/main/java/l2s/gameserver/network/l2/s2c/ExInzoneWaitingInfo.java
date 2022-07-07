package l2s.gameserver.network.l2.s2c;

import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import l2s.gameserver.data.xml.holder.InstantZoneHolder;
import l2s.gameserver.model.Player;

public class ExInzoneWaitingInfo extends L2GameServerPacket
{
	private int _currentInzoneID;
	private final TIntIntMap _instanceTimes;

	public ExInzoneWaitingInfo(Player player)
	{
		_currentInzoneID = -1;
		_instanceTimes = new TIntIntHashMap();
		if(player.getActiveReflection().isPresent())
			_currentInzoneID = player.getActiveReflection().get().getInstancedZoneId();
		for(int i : player.getInstanceReuses().keySet())
		{
			int limit = InstantZoneHolder.getInstance().getMinutesToNextEntrance(i, player);
			if(limit > 0)
				_instanceTimes.put(i, limit * 60);
		}
	}

	@Override
	protected void writeImpl()
	{
        writeD(_currentInzoneID);
        writeD(_instanceTimes.size());
		TIntIntIterator iterator = _instanceTimes.iterator();
		while(iterator.hasNext())
		{
			iterator.advance();
            writeD(iterator.key());
            writeD(iterator.value());
		}
	}
}
