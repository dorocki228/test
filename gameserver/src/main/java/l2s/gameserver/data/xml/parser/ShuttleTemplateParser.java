package l2s.gameserver.data.xml.parser;

import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.ShuttleTemplateHolder;
import l2s.gameserver.templates.ShuttleTemplate;
import l2s.gameserver.templates.StatsSet;
import org.dom4j.Element;

import java.io.File;
import java.util.Iterator;

public final class ShuttleTemplateParser extends AbstractParser<ShuttleTemplateHolder>
{
	private static final ShuttleTemplateParser _instance;

	public static ShuttleTemplateParser getInstance()
	{
		return _instance;
	}

	protected ShuttleTemplateParser()
	{
		super(ShuttleTemplateHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/shuttle_data.xml");
	}

	@Override
	public String getDTDFileName()
	{
		return "shuttle_data.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Iterator<Element> iterator = rootElement.elementIterator();
		while(iterator.hasNext())
		{
			Element shuttleElement = iterator.next();
			int shuttleId = Integer.parseInt(shuttleElement.attributeValue("id"));
			ShuttleTemplate template = new ShuttleTemplate(shuttleId);
			Iterator<Element> doorsIterator = shuttleElement.elementIterator("doors");
			while(doorsIterator.hasNext())
			{
				Element doorsElement = doorsIterator.next();
				Iterator<Element> doorIterator = doorsElement.elementIterator("door");
				while(doorIterator.hasNext())
				{
					Element doorElement = doorIterator.next();
					int doorId = Integer.parseInt(doorElement.attributeValue("id"));
					StatsSet set = new StatsSet();
					Iterator<Element> setIterator = doorElement.elementIterator("set");
					while(setIterator.hasNext())
					{
						Element setElement = setIterator.next();
						set.set(setElement.attributeValue("name"), setElement.attributeValue("value"));
					}
					template.addDoor(new ShuttleTemplate.ShuttleDoor(doorId, set));
				}
			}
			getHolder().addTemplate(template);
		}
	}

	static
	{
		_instance = new ShuttleTemplateParser();
	}
}
