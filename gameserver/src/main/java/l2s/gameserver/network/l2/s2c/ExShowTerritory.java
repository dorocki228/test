package l2s.gameserver.network.l2.s2c;

import l2s.commons.geometry.Point2D;
import l2s.commons.geometry.Shape;
import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
**/
public class ExShowTerritory implements IClientOutgoingPacket
{
	private final Shape _shape;

	public ExShowTerritory(Shape shape)
	{
		_shape = shape;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_SHOW_TERRITORY.writeId(packetWriter);
		final Point2D[] points = _shape.getPoints();
		packetWriter.writeD(points.length);
		packetWriter.writeD(_shape.getZmin());
		packetWriter.writeD(_shape.getZmax());
		for(Point2D point : points)
		{
			packetWriter.writeD(point.getX());
			packetWriter.writeD(point.getY());
		}

		return true;
	}
}