package l2s.gameserver.data.xml.parser;

import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.ClassDataHolder;
import l2s.gameserver.templates.player.ClassData;
import org.dom4j.Element;

import java.io.File;
import java.util.Iterator;

public final class ClassDataParser extends AbstractParser<ClassDataHolder>
{
	private static final ClassDataParser _instance;

	public static ClassDataParser getInstance()
	{
		return _instance;
	}

	private ClassDataParser()
	{
		super(ClassDataHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/pc_parameters/class_data/");
	}

	@Override
	public String getDTDFileName()
	{
		return "class_data.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Iterator<Element> iterator = rootElement.elementIterator();
		while(iterator.hasNext())
		{
			Element element = iterator.next();
			int classId = Integer.parseInt(element.attributeValue("class_id"));
			ClassData template = new ClassData(classId);
			Iterator<Element> subIterator = element.elementIterator();
			while(subIterator.hasNext())
			{
				Element subElement = subIterator.next();
				if("hp_mp_cp_data".equalsIgnoreCase(subElement.getName()))
					for(Element e : subElement.elements())
					{
						int lvl = Integer.parseInt(e.attributeValue("lvl"));
						double hp = Double.parseDouble(e.attributeValue("hp"));
						double mp = Double.parseDouble(e.attributeValue("mp"));
						double cp = Double.parseDouble(e.attributeValue("cp"));
						template.addHpMpCpData(lvl, hp, mp, cp);
					}
			}
			getHolder().addClassData(template);
		}
	}

	static
	{
		_instance = new ClassDataParser();
	}
}
