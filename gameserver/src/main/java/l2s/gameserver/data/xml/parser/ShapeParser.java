package l2s.gameserver.data.xml.parser;

import l2s.commons.data.xml.AbstractParser;
import l2s.commons.geometry.Circle;
import l2s.commons.geometry.Polygon.PolygonBuilder;
import l2s.commons.geometry.Rectangle;
import l2s.commons.geometry.Shape;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.ShapeHolder;
import org.dom4j.Element;

import java.io.File;
import java.util.Iterator;

public final class ShapeParser extends AbstractParser<ShapeHolder>
{
	private static final ShapeParser _instance = new ShapeParser();

	public static ShapeParser getInstance()
	{
		return _instance;
	}

	protected ShapeParser()
	{
		super(ShapeHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/spawn/");
	}

	@Override
	public String getDTDFileName()
	{
		return "spawn.dtd";
	}

	@Override
	public void readData(Element rootElement) throws Exception
	{
		Iterator<Element> territoryIterator = rootElement.elementIterator("territory");

		while(territoryIterator.hasNext())
		{
			Element territory = territoryIterator.next();
			String name = territory.attributeValue("name");

			Iterator<Element> dataIterator = territory.elementIterator();
			Shape shape = null;

			while(dataIterator.hasNext())
			{
				Element data = dataIterator.next();
				if("add".equalsIgnoreCase(data.getName()))
				{
					PolygonBuilder polygonBuilder = new PolygonBuilder();
					Iterator<Element> addIterator = territory.elementIterator("add");
					while(addIterator.hasNext())
					{
						Element addElement = addIterator.next();
						int x = Integer.parseInt(addElement.attributeValue("x"));
						int y = Integer.parseInt(addElement.attributeValue("y"));
						int zmin = Integer.parseInt(addElement.attributeValue("zmin"));
						int zmax = Integer.parseInt(addElement.attributeValue("zmax"));
						polygonBuilder.add(x, y).setZmin(zmin).setZmax(zmax);
					}
					if(!polygonBuilder.validate())
                        error("Invalid polygon: " + name + "{" + polygonBuilder + "}. File: " + getCurrentFileName());

					shape = polygonBuilder.createPolygon();
					break;
				}
				else if("rectangle".equalsIgnoreCase(data.getName()))
				{
					int x2 = Integer.parseInt(data.attributeValue("x1"));
					int y2 = Integer.parseInt(data.attributeValue("y1"));
					int x3 = Integer.parseInt(data.attributeValue("x2"));
					int y3 = Integer.parseInt(data.attributeValue("y2"));
					int zmin = Integer.parseInt(data.attributeValue("zmin"));
					int zmax = Integer.parseInt(data.attributeValue("zmax"));
					Rectangle rectangle = new Rectangle(x2, y2, x3, y3);
					rectangle.setZmin(zmin);
					rectangle.setZmax(zmax);

					shape = rectangle;
				}
				else if("circle".equalsIgnoreCase(data.getName()))
				{
					int x = Integer.parseInt(data.attributeValue("x"));
					int y = Integer.parseInt(data.attributeValue("y"));
					int zmin2 = Integer.parseInt(data.attributeValue("zmin"));
					int zmax2 = Integer.parseInt(data.attributeValue("zmax"));
					int radius = Integer.parseInt(data.attributeValue("radius"));
					Circle circle = new Circle(x, y, radius);
					circle.setZmin(zmin2);
					circle.setZmax(zmax2);

					shape = circle;
				}
			}
			getHolder().addShape(name, shape);
		}

	}
}
