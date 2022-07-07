package l2s.gameserver.data.xml.parser;

import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.SoulCrystalHolder;
import l2s.gameserver.templates.SoulCrystal;
import org.dom4j.Element;

import java.io.File;
import java.util.Iterator;

public final class SoulCrystalParser extends AbstractParser<SoulCrystalHolder>
{
	private static final SoulCrystalParser _instance;

	public static SoulCrystalParser getInstance()
	{
		return _instance;
	}

	private SoulCrystalParser()
	{
		super(SoulCrystalHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/soul_crystals.xml");
	}

	@Override
	public String getDTDFileName()
	{
		return "soul_crystals.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Iterator<Element> iterator = rootElement.elementIterator("crystal");
		while(iterator.hasNext())
		{
			Element element = iterator.next();
			int itemId = Integer.parseInt(element.attributeValue("item_id"));
			int level = Integer.parseInt(element.attributeValue("level"));
			int nextItemId = Integer.parseInt(element.attributeValue("next_item_id"));
			int cursedNextItemId = element.attributeValue("cursed_next_item_id") == null ? 0 : Integer.parseInt(element.attributeValue("cursed_next_item_id"));
			double chance = Double.parseDouble(element.attributeValue("chance"));

			getHolder().addCrystal(new SoulCrystal(itemId, level, nextItemId, cursedNextItemId, chance));
		}
	}

	static
	{
		_instance = new SoulCrystalParser();
	}
}
