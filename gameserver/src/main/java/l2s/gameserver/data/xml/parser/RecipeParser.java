package l2s.gameserver.data.xml.parser;

import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.data.xml.holder.RecipeHolder;
import l2s.gameserver.templates.item.ItemGrade;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.item.RecipeTemplate;
import l2s.gameserver.templates.item.data.ChancedItemData;
import l2s.gameserver.templates.item.data.ItemData;
import org.dom4j.Element;

import java.io.File;
import java.util.Iterator;

public final class RecipeParser extends AbstractParser<RecipeHolder>
{
	private static final RecipeParser _instance;

	public static RecipeParser getInstance()
	{
		return _instance;
	}

	private RecipeParser()
	{
		super(RecipeHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/recipes.xml");
	}

	@Override
	public String getDTDFileName()
	{
		return "recipes.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Iterator<Element> iterator = rootElement.elementIterator();
		while(iterator.hasNext())
		{
			Element element = iterator.next();
			int id = Integer.parseInt(element.attributeValue("id"));
			int level = Integer.parseInt(element.attributeValue("level"));
			int mpConsume = element.attributeValue("mp_consume") == null ? 0 : Integer.parseInt(element.attributeValue("mp_consume"));
			int successRate = Integer.parseInt(element.attributeValue("success_rate"));
			int itemId = Integer.parseInt(element.attributeValue("item_id"));
			boolean isCommon = element.attributeValue("is_common") != null && Boolean.parseBoolean(element.attributeValue("is_common"));
			RecipeTemplate recipe = new RecipeTemplate(id, level, mpConsume, successRate, itemId, isCommon);
			Iterator<Element> subIterator = element.elementIterator();
			while(subIterator.hasNext())
			{
				Element subElement = subIterator.next();
				if("materials".equalsIgnoreCase(subElement.getName()))
				{
					for(Element e : subElement.elements())
						if("item".equalsIgnoreCase(e.getName()))
						{
							int item_id = Integer.parseInt(e.attributeValue("id"));
							long count = Long.parseLong(e.attributeValue("count"));
							if(Config.ALT_EASY_RECIPES && !checkComponent(item_id))
								continue;
							recipe.addMaterial(new ItemData(item_id, count));
						}
				}
				else if("products".equalsIgnoreCase(subElement.getName()))
				{
					for(Element e : subElement.elements())
						if("item".equalsIgnoreCase(e.getName()))
						{
							int item_id = Integer.parseInt(e.attributeValue("id"));
							long count = Long.parseLong(e.attributeValue("count"));
							int chance = Integer.parseInt(e.attributeValue("chance"));
							recipe.addProduct(new ChancedItemData(item_id, count, chance));
							if(!Config.ALT_EASY_RECIPES)
								continue;
							int book_id = checkAndAddBook(item_id);
							if(book_id == 0)
								continue;
							recipe.addMaterial(new ItemData(item_id, 1L));
						}
				}
				else
				{
					if(!"npc_fee".equalsIgnoreCase(subElement.getName()))
						continue;
					for(Element e : subElement.elements())
						if("item".equalsIgnoreCase(e.getName()))
						{
							if(Config.ALT_EASY_RECIPES)
								continue;
							int item_id = Integer.parseInt(e.attributeValue("id"));
							long count = Long.parseLong(e.attributeValue("count"));
							recipe.addNpcFee(new ItemData(item_id, count));
						}
				}
			}
			getHolder().addRecipe(recipe);
		}
	}

	public static boolean checkComponent(int itemId)
	{
		ItemTemplate template = ItemHolder.getInstance().getTemplate(itemId);
		return template.isRecipe() || template.isCrystall();
	}

	public static int checkAndAddBook(int itemId)
	{
		ItemTemplate template = ItemHolder.getInstance().getTemplate(itemId);
		if(template == null)
			return 0;
		if(template.getGrade() == ItemGrade.NONE)
			return 0;
		if(!template.isEquipable())
			return 0;
		return getBookId(template.getGrade(), template.isWeapon());
	}

	public static int getBookId(ItemGrade grade, boolean isWpn)
	{
		switch(grade)
		{
			case D:
			{
				if(isWpn)
					return 40000;
				return 40001;
			}
			case C:
			{
				if(isWpn)
					return 40002;
				return 40003;
			}
			case B:
			{
				if(isWpn)
					return 40004;
				return 40005;
			}
			case A:
			{
				if(isWpn)
					return 40006;
				return 40007;
			}
			case S:
			{
				if(isWpn)
					return 40008;
				return 40009;
			}
			case S80:
			{
				if(isWpn)
					return 40010;
				return 40011;
			}
			case R:
			{
				if(isWpn)
					return 40012;
				return 40013;
			}
			case R95:
			{
				if(isWpn)
					return 40014;
				return 40015;
			}
			case R99:
			{
				if(isWpn)
					return 40016;
				return 40017;
			}
			default:
			{
				return 40000;
			}
		}
	}

	static
	{
		_instance = new RecipeParser();
	}
}
