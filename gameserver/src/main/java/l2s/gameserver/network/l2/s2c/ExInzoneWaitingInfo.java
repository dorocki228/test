package l2s.gameserver.network.l2.s2c;

import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

import l2s.gameserver.data.xml.holder.InstantZoneHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
**/
public class ExInzoneWaitingInfo implements IClientOutgoingPacket
{
	private final boolean _openWindow;
	private int _currentInzoneID = -1;
	private TIntIntMap _instanceTimes;

	public ExInzoneWaitingInfo(Player player, boolean openWindow)
	{
		_openWindow = openWindow;
		_instanceTimes = new TIntIntHashMap();

		if(player.getActiveReflection() != null)
			_currentInzoneID = player.getActiveReflection().getInstancedZoneId();

		int limit;
		for(int i : player.getInstanceReuses().keySet())
		{
			limit = InstantZoneHolder.getInstance().getMinutesToNextEntrance(i, player);
			if(limit > 0)
				_instanceTimes.put(i, limit * 60);
		}
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_INZONE_WAITING_INFO.writeId(packetWriter);
		packetWriter.writeC(_openWindow);
		packetWriter.writeD(_currentInzoneID);
		packetWriter.writeD(_instanceTimes.size());

		TIntIntIterator iterator = _instanceTimes.iterator();
		while(iterator.hasNext())
		{
			iterator.advance();
			packetWriter.writeD(iterator.key());
			packetWriter.writeD(iterator.value());
		}

		return true;
	}
}