package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.geometry.ILocation;
import l2s.gameserver.network.l2.OutgoingExPackets;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A packet used to draw points and lines on client.<br/>
 * <b>Note:</b> Names in points and lines are bugged they will appear even when not looking at them.
 */
public class ExServerPrimitive implements IClientOutgoingPacket
{
	private final String _name;
	private final int _x;
	private final int _y;
	private final int _z;
	private final List<Point> _points = new ArrayList<>();
	private final List<Line> _lines = new ArrayList<>();
	
	/**
	 * @param name A unique name this will be used to replace lines if second packet is sent
	 * @param x the x coordinate usually middle of drawing area
	 * @param y the y coordinate usually middle of drawing area
	 * @param z the z coordinate usually middle of drawing area
	 */
	public ExServerPrimitive(String name, int x, int y, int z)
	{
		_name = name;
		_x = x;
		_y = y;
		_z = z;
	}
	
	/**
	 * Adds a point to be displayed on client.
	 * @param name the name that will be displayed over the point
	 * @param color the color
	 * @param isNameColored if {@code true} name will be colored as well.
	 * @param x the x coordinate for this point
	 * @param y the y coordinate for this point
	 * @param z the z coordinate for this point
	 */
	public void addPoint(String name, int color, boolean isNameColored, int x, int y, int z)
	{
		_points.add(new Point(name, color, isNameColored, x, y, z));
	}

	/**
	 * Adds a line to be displayed on client
	 * @param name the name that will be displayed over the middle of line
	 * @param color the color
	 * @param isNameColored if {@code true} name will be colored as well.
	 * @param x the x coordinate for this line start point
	 * @param y the y coordinate for this line start point
	 * @param z the z coordinate for this line start point
	 * @param x2 the x coordinate for this line end point
	 * @param y2 the y coordinate for this line end point
	 * @param z2 the z coordinate for this line end point
	 */
	public void addLine(String name, int color, boolean isNameColored, int x, int y, int z, int x2, int y2, int z2)
	{
		_lines.add(new Line(name, color, isNameColored, x, y, z, x2, y2, z2));
	}

	/**
	 * Adds a point to be displayed on client.
	 * @param name the name that will be displayed over the point
	 * @param color the color
	 * @param isNameColored if {@code true} name will be colored as well.
	 * @param locational the ILocation to take coordinates for this point
	 */
	public void addPoint(String name, int color, boolean isNameColored, ILocation locational)
	{
		addPoint(name, color, isNameColored, locational.getX(), locational.getY(), locational.getZ());
	}

	/**
	 * Adds a point to be displayed on client.
	 * @param color the color
	 * @param x the x coordinate for this point
	 * @param y the y coordinate for this point
	 * @param z the z coordinate for this point
	 */
	public void addPoint(int color, int x, int y, int z)
	{
		addPoint("", color, false, x, y, z);
	}

	/**
	 * Adds a point to be displayed on client.
	 * @param color the color
	 * @param locational the ILocation to take coordinates for this point
	 */
	public void addPoint(int color, ILocation locational)
	{
		addPoint("", color, false, locational);
	}

	/**
	 * Adds a point to be displayed on client.
	 * @param name the name that will be displayed over the point
	 * @param color the color
	 * @param isNameColored if {@code true} name will be colored as well.
	 * @param x the x coordinate for this point
	 * @param y the y coordinate for this point
	 * @param z the z coordinate for this point
	 */
	public void addPoint(String name, Color color, boolean isNameColored, int x, int y, int z)
	{
		addPoint(name, color.getRGB(), isNameColored, x, y, z);
	}

	/**
	 * Adds a point to be displayed on client.
	 * @param name the name that will be displayed over the point
	 * @param color the color
	 * @param isNameColored if {@code true} name will be colored as well.
	 * @param locational the ILocation to take coordinates for this point
	 */
	public void addPoint(String name, Color color, boolean isNameColored, ILocation locational)
	{
		addPoint(name, color.getRGB(), isNameColored, locational);
	}

	/**
	 * Adds a point to be displayed on client.
	 * @param color the color
	 * @param x the x coordinate for this point
	 * @param y the y coordinate for this point
	 * @param z the z coordinate for this point
	 */
	public void addPoint(Color color, int x, int y, int z)
	{
		addPoint("", color, false, x, y, z);
	}

	/**
	 * Adds a point to be displayed on client.
	 * @param color the color
	 * @param locational the ILocation to take coordinates for this point
	 */
	public void addPoint(Color color, ILocation locational)
	{
		addPoint("", color, false, locational);
	}

	/**
	 * Adds a line to be displayed on client
	 * @param name the name that will be displayed over the middle of line
	 * @param color the color
	 * @param isNameColored if {@code true} name will be colored as well.
	 * @param locational the ILocation to take coordinates for this line start point
	 * @param x2 the x coordinate for this line end point
	 * @param y2 the y coordinate for this line end point
	 * @param z2 the z coordinate for this line end point
	 */
	public void addLine(String name, int color, boolean isNameColored, ILocation locational, int x2, int y2, int z2)
	{
		addLine(name, color, isNameColored, locational.getX(), locational.getY(), locational.getZ(), x2, y2, z2);
	}

	/**
	 * Adds a line to be displayed on client
	 * @param name the name that will be displayed over the middle of line
	 * @param color the color
	 * @param isNameColored if {@code true} name will be colored as well.
	 * @param x the x coordinate for this line start point
	 * @param y the y coordinate for this line start point
	 * @param z the z coordinate for this line start point
	 * @param locational2 the ILocation to take coordinates for this line end point
	 */
	public void addLine(String name, int color, boolean isNameColored, int x, int y, int z, ILocation locational2)
	{
		addLine(name, color, isNameColored, x, y, z, locational2.getX(), locational2.getY(), locational2.getZ());
	}

	/**
	 * Adds a line to be displayed on client
	 * @param name the name that will be displayed over the middle of line
	 * @param color the color
	 * @param isNameColored if {@code true} name will be colored as well.
	 * @param locational the ILocation to take coordinates for this line start point
	 * @param locational2 the ILocation to take coordinates for this line end point
	 */
	public void addLine(String name, int color, boolean isNameColored, ILocation locational, ILocation locational2)
	{
		addLine(name, color, isNameColored, locational, locational2.getX(), locational2.getY(), locational2.getZ());
	}

	/**
	 * Adds a line to be displayed on client
	 * @param color the color
	 * @param x the x coordinate for this line start point
	 * @param y the y coordinate for this line start point
	 * @param z the z coordinate for this line start point
	 * @param x2 the x coordinate for this line end point
	 * @param y2 the y coordinate for this line end point
	 * @param z2 the z coordinate for this line end point
	 */
	public void addLine(int color, int x, int y, int z, int x2, int y2, int z2)
	{
		addLine("", color, false, x, y, z, x2, y2, z2);
	}

	/**
	 * Adds a line to be displayed on client
	 * @param color the color
	 * @param locational the ILocation to take coordinates for this line start point
	 * @param x2 the x coordinate for this line end point
	 * @param y2 the y coordinate for this line end point
	 * @param z2 the z coordinate for this line end point
	 */
	public void addLine(int color, ILocation locational, int x2, int y2, int z2)
	{
		addLine("", color, false, locational, x2, y2, z2);
	}

	/**
	 * Adds a line to be displayed on client
	 * @param color the color
	 * @param x the x coordinate for this line start point
	 * @param y the y coordinate for this line start point
	 * @param z the z coordinate for this line start point
	 * @param locational2 the ILocation to take coordinates for this line end point
	 */
	public void addLine(int color, int x, int y, int z, ILocation locational2)
	{
		addLine("", color, false, x, y, z, locational2);
	}

	/**
	 * Adds a line to be displayed on client
	 * @param color the color
	 * @param locational the ILocation to take coordinates for this line start point
	 * @param locational2 the ILocation to take coordinates for this line end point
	 */
	public void addLine(int color, ILocation locational, ILocation locational2)
	{
		addLine("", color, false, locational, locational2);
	}

	/**
	 * Adds a line to be displayed on client
	 * @param name the name that will be displayed over the middle of line
	 * @param color the color
	 * @param isNameColored if {@code true} name will be colored as well.
	 * @param x the x coordinate for this line start point
	 * @param y the y coordinate for this line start point
	 * @param z the z coordinate for this line start point
	 * @param x2 the x coordinate for this line end point
	 * @param y2 the y coordinate for this line end point
	 * @param z2 the z coordinate for this line end point
	 */
	public void addLine(String name, Color color, boolean isNameColored, int x, int y, int z, int x2, int y2, int z2)
	{
		addLine(name, color.getRGB(), isNameColored, x, y, z, x2, y2, z2);
	}

	/**
	 * Adds a line to be displayed on client
	 * @param name the name that will be displayed over the middle of line
	 * @param color the color
	 * @param isNameColored if {@code true} name will be colored as well.
	 * @param locational the ILocation to take coordinates for this line start point
	 * @param x2 the x coordinate for this line end point
	 * @param y2 the y coordinate for this line end point
	 * @param z2 the z coordinate for this line end point
	 */
	public void addLine(String name, Color color, boolean isNameColored, ILocation locational, int x2, int y2, int z2)
	{
		addLine(name, color.getRGB(), isNameColored, locational, x2, y2, z2);
	}

	/**
	 * Adds a line to be displayed on client
	 * @param name the name that will be displayed over the middle of line
	 * @param color the color
	 * @param isNameColored if {@code true} name will be colored as well.
	 * @param x the x coordinate for this line start point
	 * @param y the y coordinate for this line start point
	 * @param z the z coordinate for this line start point
	 * @param locational2 the ILocation to take coordinates for this line end point
	 */
	public void addLine(String name, Color color, boolean isNameColored, int x, int y, int z, ILocation locational2)
	{
		addLine(name, color.getRGB(), isNameColored, x, y, z, locational2);
	}

	/**
	 * Adds a line to be displayed on client
	 * @param name the name that will be displayed over the middle of line
	 * @param color the color
	 * @param isNameColored if {@code true} name will be colored as well.
	 * @param locational the ILocation to take coordinates for this line start point
	 * @param locational2 the ILocation to take coordinates for this line end point
	 */
	public void addLine(String name, Color color, boolean isNameColored, ILocation locational, ILocation locational2)
	{
		addLine(name, color.getRGB(), isNameColored, locational, locational2);
	}

	/**
	 * Adds a line to be displayed on client
	 * @param color the color
	 * @param x the x coordinate for this line start point
	 * @param y the y coordinate for this line start point
	 * @param z the z coordinate for this line start point
	 * @param x2 the x coordinate for this line end point
	 * @param y2 the y coordinate for this line end point
	 * @param z2 the z coordinate for this line end point
	 */
	public void addLine(Color color, int x, int y, int z, int x2, int y2, int z2)
	{
		addLine("", color, false, x, y, z, x2, y2, z2);
	}

	/**
	 * Adds a line to be displayed on client
	 * @param color the color
	 * @param locational the ILocation to take coordinates for this line start point
	 * @param x2 the x coordinate for this line end point
	 * @param y2 the y coordinate for this line end point
	 * @param z2 the z coordinate for this line end point
	 */
	public void addLine(Color color, ILocation locational, int x2, int y2, int z2)
	{
		addLine("", color, false, locational, x2, y2, z2);
	}

	/**
	 * Adds a line to be displayed on client
	 * @param color the color
	 * @param x the x coordinate for this line start point
	 * @param y the y coordinate for this line start point
	 * @param z the z coordinate for this line start point
	 * @param locational2 the ILocation to take coordinates for this line end point
	 */
	public void addLine(Color color, int x, int y, int z, ILocation locational2)
	{
		addLine("", color, false, x, y, z, locational2);
	}

	/**
	 * Adds a line to be displayed on client
	 * @param color the color
	 * @param locational the ILocation to take coordinates for this line start point
	 * @param locational2 the ILocation to take coordinates for this line end point
	 */
	public void addLine(Color color, ILocation locational, ILocation locational2)
	{
		addLine("", color, false, locational, locational2);
	}
	
	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_SERVER_PRIMITIVE.writeId(packetWriter);
		//System.out.println("name : "+_name+" x: "+_x+" y: "+_y+" z: "+_z+"");
		packetWriter.writeS(_name);
		packetWriter.writeD(_x);
		packetWriter.writeD(_y);
		packetWriter.writeD(_z);
		packetWriter.writeD(65535); // has to do something with display range and angle
		packetWriter.writeD(65535); // has to do something with display range and angle
		
		packetWriter.writeD(_points.size() + _lines.size());
		//System.out.println("points size: " + _points.size());
		//System.out.println("line size: " + _lines.size());
		for (Point point : _points)
		{
			packetWriter.writeC(1); // Its the type in this case Point
			packetWriter.writeS(point.getName());
			int color = point.getColor();
			packetWriter.writeD((color >> 16) & 0xFF); // R
			packetWriter.writeD((color >> 8) & 0xFF); // G
			packetWriter.writeD(color & 0xFF); // B
			packetWriter.writeD(point.isNameColored() ? 1 : 0);
			packetWriter.writeD(point.getX());
			packetWriter.writeD(point.getY());
			packetWriter.writeD(point.getZ());
			//System.out.println("point name: " + point.getName() + " point color: "+point.getColor()+" point colored: "+point.isNameColored()+" point x: "+point.getX()+" point y: "+point.getY()+" point z: "+point.getZ()+"");
		}
		
		for (Line line : _lines)
		{
			packetWriter.writeC(2); // Its the type in this case Line
			packetWriter.writeS(line.getName());
			int color = line.getColor();
			packetWriter.writeD((color >> 16) & 0xFF); // R
			packetWriter.writeD((color >> 8) & 0xFF); // G
			packetWriter.writeD(color & 0xFF); // B
			packetWriter.writeD(line.isNameColored() ? 1 : 0);
			packetWriter.writeD(line.getX());
			packetWriter.writeD(line.getY());
			packetWriter.writeD(line.getZ());
			packetWriter.writeD(line.getX2());
			packetWriter.writeD(line.getY2());
			packetWriter.writeD(line.getZ2());
			//System.out.println("line name: " + line.getName() + " line color: "+line.getColor()+" line colored: "+line.isNameColored()+" line x: "+line.getX()+" line y: "+line.getY()+" line z: "+line.getZ()+" line x2: "+line.getX2()+" line y2: "+line.getY2()+" line z2: "+line.getZ2()+"");
			
		}

		return true;
	}
	
	private static class Point
	{
		private final String _name;
		private final int _color;
		private final boolean _isNameColored;
		private final int _x;
		private final int _y;
		private final int _z;
		
		public Point(String name, int color, boolean isNameColored, int x, int y, int z)
		{
			_name = name;
			_color = color;
			_isNameColored = isNameColored;
			_x = x;
			_y = y;
			_z = z;
		}
		
		/**
		 * @return the name
		 */
		public String getName()
		{
			return _name;
		}
		
		/**
		 * @return the color
		 */
		public int getColor()
		{
			return _color;
		}
		
		/**
		 * @return the isNameColored
		 */
		public boolean isNameColored()
		{
			return _isNameColored;
		}
		
		/**
		 * @return the x
		 */
		public int getX()
		{
			return _x;
		}
		
		/**
		 * @return the y
		 */
		public int getY()
		{
			return _y;
		}
		
		/**
		 * @return the z
		 */
		public int getZ()
		{
			return _z;
		}
	}
	
	private static class Line extends Point
	{
		private final int _x2;
		private final int _y2;
		private final int _z2;
		
		public Line(String name, int color, boolean isNameColored, int x, int y, int z, int x2, int y2, int z2)
		{
			super(name, color, isNameColored, x, y, z);
			_x2 = x2;
			_y2 = y2;
			_z2 = z2;
		}
		
		/**
		 * @return the x2
		 */
		public int getX2()
		{
			return _x2;
		}
		
		/**
		 * @return the y2
		 */
		public int getY2()
		{
			return _y2;
		}
		
		/**
		 * @return the z2
		 */
		public int getZ2()
		{
			return _z2;
		}
	}
}