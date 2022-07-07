package l2s.gameserver.data.xml.parser;

import l2s.commons.data.xml.AbstractParser;
import l2s.commons.geometry.Polygon;
import l2s.commons.geometry.Polygon.PolygonBuilder;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.DoorHolder;
import l2s.gameserver.templates.DoorTemplate;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.utils.Location;
import org.dom4j.Element;

import java.io.File;
import java.util.Iterator;

public final class DoorParser extends AbstractParser<DoorHolder>
{
	private static final DoorParser _instance;

	public static DoorParser getInstance()
	{
		return _instance;
	}

	protected DoorParser()
	{
		super(DoorHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/doors/");
	}

	@Override
	public String getDTDFileName()
	{
		return "doors.dtd";
	}

	private StatsSet initBaseStats()
	{
		StatsSet baseDat = new StatsSet();
		baseDat.set("level", 0);
		baseDat.set("baseSTR", 0);
		baseDat.set("baseCON", 0);
		baseDat.set("baseDEX", 0);
		baseDat.set("baseINT", 0);
		baseDat.set("baseWIT", 0);
		baseDat.set("baseMEN", 0);
		baseDat.set("baseShldDef", 0);
		baseDat.set("baseShldRate", 0);
		baseDat.set("basePCritRate", 0);
		baseDat.set("baseMCritRate", 0);
		baseDat.set("baseAtkRange", 0);
		baseDat.set("baseMpMax", 0);
		baseDat.set("baseCpMax", 0);
		baseDat.set("basePAtk", 0);
		baseDat.set("baseMAtk", 0);
		baseDat.set("basePAtkSpd", 0);
		baseDat.set("baseMAtkSpd", 0);
		baseDat.set("baseWalkSpd", 0);
		baseDat.set("baseRunSpd", 0);
		baseDat.set("baseHpReg", 0);
		baseDat.set("baseCpReg", 0);
		baseDat.set("baseMpReg", 0);
		return baseDat;
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Iterator<Element> iterator = rootElement.elementIterator();
		while(iterator.hasNext())
		{
			Element doorElement = iterator.next();
			if("door".equals(doorElement.getName()))
			{
				StatsSet doorSet = initBaseStats();
                doorSet.set("door_type", doorElement.attributeValue("type"));
				Element posElement = doorElement.element("pos");
				int x = Integer.parseInt(posElement.attributeValue("x"));
				int y = Integer.parseInt(posElement.attributeValue("y"));
				int z = Integer.parseInt(posElement.attributeValue("z"));
				Location doorPos;
				doorSet.set("pos", doorPos = new Location(x, y, z));
				PolygonBuilder polygonBuilder = new PolygonBuilder();
                Element shapeElement = doorElement.element("shape");
                int minz = Integer.parseInt(shapeElement.attributeValue("minz"));
                int maxz = Integer.parseInt(shapeElement.attributeValue("maxz"));
                polygonBuilder.add(Integer.parseInt(shapeElement.attributeValue("ax")), Integer.parseInt(shapeElement.attributeValue("ay")));
				polygonBuilder.add(Integer.parseInt(shapeElement.attributeValue("bx")), Integer.parseInt(shapeElement.attributeValue("by")));
				polygonBuilder.add(Integer.parseInt(shapeElement.attributeValue("cx")), Integer.parseInt(shapeElement.attributeValue("cy")));
				polygonBuilder.add(Integer.parseInt(shapeElement.attributeValue("dx")), Integer.parseInt(shapeElement.attributeValue("dy")));
				polygonBuilder.setZmin(minz);
				polygonBuilder.setZmax(maxz);
				Polygon polygon = polygonBuilder.createPolygon();
				doorSet.set("shape", polygon);
				doorPos.setZ(minz + 32);
				Iterator<Element> i = doorElement.elementIterator();
                StatsSet aiParams = null;
                while(i.hasNext())
				{
					Element n = i.next();
					if("set".equals(n.getName()))
						doorSet.set(n.attributeValue("name"), n.attributeValue("value"));
					else
					{
						if(!"ai_params".equals(n.getName()))
							continue;
						if(aiParams == null)
						{
							aiParams = new StatsSet();
							doorSet.set("ai_params", aiParams);
						}
						Iterator<Element> aiParamsIterator = n.elementIterator();
						while(aiParamsIterator.hasNext())
						{
							Element aiParamElement = aiParamsIterator.next();
							aiParams.set(aiParamElement.attributeValue("name"), aiParamElement.attributeValue("value"));
						}
					}
				}
				doorSet.set("uid", doorElement.attributeValue("id"));
				doorSet.set("name", doorElement.attributeValue("name"));
				doorSet.set("baseHpMax", doorElement.attributeValue("hp"));
				doorSet.set("basePDef", doorElement.attributeValue("pdef"));
				doorSet.set("baseMDef", doorElement.attributeValue("mdef"));
				doorSet.set("collision_height", maxz - minz & 0xFFF0);
				doorSet.set("collision_radius", Math.max(50, Math.min(doorPos.x - polygon.getXmin(), doorPos.y - polygon.getYmin())));

				Element angleElement = doorElement.element("angle");
				if (angleElement != null) {
					String angleValue = angleElement.getStringValue();
					if (!angleValue.isEmpty()) {
						int angle = Integer.parseInt(angleValue);
						doorSet.set("angle", angle);
					}
				}

				DoorTemplate template = new DoorTemplate(doorSet);
				getHolder().addTemplate(template);
			}
		}
	}

	static
	{
		_instance = new DoorParser();
	}
}
