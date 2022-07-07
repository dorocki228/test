package l2s.gameserver.data.xml.parser;

import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.PetitionGroupHolder;
import l2s.gameserver.model.petition.PetitionMainGroup;
import l2s.gameserver.model.petition.PetitionSubGroup;
import l2s.gameserver.utils.Language;
import org.dom4j.Element;

import java.io.File;
import java.util.Iterator;

public class PetitionGroupParser extends AbstractParser<PetitionGroupHolder>
{
	private static final PetitionGroupParser _instance;

	public static PetitionGroupParser getInstance()
	{
		return _instance;
	}

	private PetitionGroupParser()
	{
		super(PetitionGroupHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/petition_group.xml");
	}

	@Override
	public String getDTDFileName()
	{
		return "petition_group.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Iterator<Element> iterator = rootElement.elementIterator();
		while(iterator.hasNext())
		{
			Element groupElement = iterator.next();
			PetitionMainGroup group = new PetitionMainGroup(Integer.parseInt(groupElement.attributeValue("id")));
			getHolder().addPetitionGroup(group);
			Iterator<Element> subIterator = groupElement.elementIterator();
			while(subIterator.hasNext())
			{
				Element subElement = subIterator.next();
				if("name".equals(subElement.getName()))
					group.setName(Language.valueOf(subElement.attributeValue("lang")), subElement.getText());
				else if("description".equals(subElement.getName()))
					group.setDescription(Language.valueOf(subElement.attributeValue("lang")), subElement.getText());
				else
				{
					if(!"sub_group".equals(subElement.getName()))
						continue;
					PetitionSubGroup subGroup = new PetitionSubGroup(Integer.parseInt(subElement.attributeValue("id")), subElement.attributeValue("handler"));
					group.addSubGroup(subGroup);
					Iterator<Element> sub2Iterator = subElement.elementIterator();
					while(sub2Iterator.hasNext())
					{
						Element sub2Element = sub2Iterator.next();
						if("name".equals(sub2Element.getName()))
							subGroup.setName(Language.valueOf(sub2Element.attributeValue("lang")), sub2Element.getText());
						else
						{
							if(!"description".equals(sub2Element.getName()))
								continue;
							subGroup.setDescription(Language.valueOf(sub2Element.attributeValue("lang")), sub2Element.getText());
						}
					}
				}
			}
		}
	}

	static
	{
		_instance = new PetitionGroupParser();
	}
}
