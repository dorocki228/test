package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExShowTrace implements IClientOutgoingPacket
{
	private final List<Trace> _traces = new ArrayList<Trace>();
	private final int _time;

	static final class Trace
	{
		public final int _x;
		public final int _y;
		public final int _z;

		public Trace(int x, int y, int z)
		{
			_x = x;
			_y = y;
			_z = z;
		}
	}

	public ExShowTrace(int time)
	{
		_time = time;
	}

	public ExShowTrace addTrace(int x, int y, int z)
	{
		_traces.add(new Trace(x, y, z));
		return this;
	}

	public ExShowTrace addLine(Location from, Location to, int step)
	{
		addLine(from.x, from.y, from.z, to.x, to.y, to.z, step);
		return this;
	}

	public ExShowTrace addLine(int from_x, int from_y, int from_z, int to_x, int to_y, int to_z, int step)
	{
		int x_diff = to_x - from_x;
		int y_diff = to_y - from_y;
		int z_diff = to_z - from_z;
		double xy_dist = Math.sqrt(x_diff * x_diff + y_diff * y_diff);
		double full_dist = Math.sqrt(xy_dist * xy_dist + z_diff * z_diff);
		int steps = (int) (full_dist / step);

		addTrace(from_x, from_y, from_z);
		if(steps > 1)
		{
			int step_x = x_diff / steps;
			int step_y = y_diff / steps;
			int step_z = z_diff / steps;

			for(int i = 1; i < steps; i++)
				addTrace(from_x + step_x * i, from_y + step_y * i, from_z + step_z * i);
		}
		addTrace(to_x, to_y, to_z);
		return this;
	}

	public ExShowTrace addTrace(GameObject obj)
	{
		this.addTrace(obj.getX(), obj.getY(), obj.getZ());
		return this;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_SHOW_TRACE.writeId(packetWriter);
		packetWriter.writeH(0); // type broken in H5
		packetWriter.writeD(0); // time broken in H5
		packetWriter.writeH(_traces.size());
		for(Trace t : _traces)
		{
			packetWriter.writeD(t._x);
			packetWriter.writeD(t._y);
			packetWriter.writeD(t._z);
		}

		return true;
	}
}