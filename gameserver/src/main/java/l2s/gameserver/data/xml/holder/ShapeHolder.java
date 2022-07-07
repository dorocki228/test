package l2s.gameserver.data.xml.holder;

import l2s.commons.data.xml.AbstractHolder;
import l2s.commons.geometry.Shape;

import java.util.HashMap;
import java.util.Map;

public final class ShapeHolder extends AbstractHolder
{
	private static final ShapeHolder _instance = new ShapeHolder();
	private final Map<String, Shape> _shapes;

	public ShapeHolder()
	{
		_shapes = new HashMap<>();
	}

	public static ShapeHolder getInstance()
	{
		return _instance;
	}

	@Override
	public int size()
	{
		return _shapes.size();
	}

	@Override
	public void clear()
	{
		_shapes.clear();
	}

	public Map<String, Shape> getShapes()
	{
		return _shapes;
	}

	public void addShape(String name, Shape shape)
	{
		if(_shapes.containsKey(name))
			_log.warn("Dublicate shape " + name);

		_shapes.put(name, shape);
	}

	public Shape getShape(String name)
	{
		Shape shape = _shapes.get(name);

		if(shape == null)
			_log.error("Cant find shape " + name);

		return shape;
	}
}
