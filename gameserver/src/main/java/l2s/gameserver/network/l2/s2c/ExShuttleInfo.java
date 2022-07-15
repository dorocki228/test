package l2s.gameserver.network.l2.s2c;

import java.util.Collection;

import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.entity.boat.Shuttle;
import l2s.gameserver.network.l2.OutgoingExPackets;
import l2s.gameserver.templates.ShuttleTemplate.ShuttleStop;

/**
 * @author Bonux
**/
public class ExShuttleInfo implements IClientOutgoingPacket
{
	private final Shuttle _shuttle;
	private final Collection<ShuttleStop> _stops;

	public ExShuttleInfo(Shuttle shuttle)
	{
		_shuttle = shuttle;
		_stops = shuttle.getTemplate().getStops();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_SHUTTLE_INFO.writeId(packetWriter);
		packetWriter.writeD(_shuttle.getBoatId()); // Shuttle ObjID
		packetWriter.writeD(_shuttle.getX()); // X
		packetWriter.writeD(_shuttle.getY()); // Y
		packetWriter.writeD(_shuttle.getZ()); // Z
		packetWriter.writeD(_shuttle.getHeading()); // Maybe H
		packetWriter.writeD(_shuttle.getBoatId()); // Shuttle ID (Arkan: 1,2; Cruma: 3)
		packetWriter.writeD(_stops.size()); // doors_count
		for(ShuttleStop stop : _stops)
		{
			int stopId = stop.getId();
			packetWriter.writeD(stopId); // Stop ID
			for (Location loc : stop.getDimensions())
			{
				packetWriter.writeD(loc.getX());
				packetWriter.writeD(loc.getY());
				packetWriter.writeD(loc.getZ());
			}

			if(_shuttle.getCurrentWayEvent().containsStop(stopId)) {
				packetWriter.writeD(_shuttle.isDocked());
				packetWriter.writeD(true);
			} else {
				packetWriter.writeD(false);
				packetWriter.writeD(false);
			}
		}

		return true;
	}
}