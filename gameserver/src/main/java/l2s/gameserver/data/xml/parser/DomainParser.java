package l2s.gameserver.data.xml.parser;

import l2s.commons.data.xml.AbstractParser;
import l2s.commons.geometry.Polygon;
import l2s.gameserver.Config;
import l2s.gameserver.instancemanager.MapRegionManager;
import l2s.gameserver.model.Territory;
import l2s.gameserver.templates.mapregion.DomainArea;
import org.dom4j.Element;

import java.io.File;
import java.util.Iterator;

public class DomainParser extends AbstractParser<MapRegionManager>
{
	private static final DomainParser _instance;

	public static DomainParser getInstance()
	{
		return _instance;
	}

	protected DomainParser()
	{
		super(MapRegionManager.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/mapregion/domains.xml");
	}

	@Override
	public String getDTDFileName()
	{
		return "domains.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Iterator<Element> iterator = rootElement.elementIterator();
		while(iterator.hasNext())
		{
			Element listElement = iterator.next();
			if("domain".equals(listElement.getName()))
			{
				int id = Integer.parseInt(listElement.attributeValue("id"));
				Territory territory = null;
				Iterator<Element> i = listElement.elementIterator();
				while(i.hasNext())
				{
					Element n = i.next();
					if("polygon".equalsIgnoreCase(n.getName()))
					{
						Polygon.PolygonBuilder shape = ZoneParser.parsePolygon(n);
						if(!shape.validate())
                            error("DomainParser: invalid territory data : " + shape + "!");
						if(territory == null)
							territory = new Territory();
						territory.add(shape.createPolygon());
					}
				}
				if(territory == null)
					throw new RuntimeException("DomainParser: empty territory!");
				getHolder().addRegionData(new DomainArea(id, territory));
				getHolder().addDomainArea(new DomainArea(id, territory));
			}
		}
	}

	static
	{
		_instance = new DomainParser();
	}
}
