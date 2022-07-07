package l2s.gameserver.data.xml.parser;

import com.google.common.collect.ImmutableList;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.data.xml.holder.OptionDataHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.handler.items.impl.SkillsItemHandler;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.stats.conditions.Condition;
import l2s.gameserver.templates.OptionDataTemplate;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.item.*;
import l2s.gameserver.templates.item.data.CapsuledItemData;
import l2s.gameserver.templates.item.support.VisualChange;
import org.dom4j.Element;

import java.io.File;
import java.util.Iterator;
import java.util.List;

public final class ItemParser extends StatParser<ItemHolder>
{
	private static final ItemParser _instance;

	public static ItemParser getInstance()
	{
		return _instance;
	}

	protected ItemParser()
	{
		super(ItemHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/items/");
	}

    @Override
	public String getDTDFileName()
	{
		return "item.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Iterator<Element> itemIterator = rootElement.elementIterator();
		while(itemIterator.hasNext())
		{
			Element itemElement = itemIterator.next();
			StatsSet set = new StatsSet();
			set.set("item_id", itemElement.attributeValue("id"));
			set.set("name", itemElement.attributeValue("name"));
			set.set("add_name", itemElement.attributeValue("add_name", ""));
			int slot = 0;
			ImmutableList.Builder<VisualChange> visualChanges = ImmutableList.builderWithExpectedSize(1);

			Iterator<Element> subIterator = itemElement.elementIterator();
			while(subIterator.hasNext())
			{
				Element subElement = subIterator.next();
				String subName = subElement.getName();
				if("set".equalsIgnoreCase(subName))
					set.set(subElement.attributeValue("name"), subElement.attributeValue("value"));
				else
				{
					if(!"equip".equalsIgnoreCase(subName))
						continue;
					Iterator<Element> slotIterator = subElement.elementIterator();
					while(slotIterator.hasNext())
					{
						Element slotElement = slotIterator.next();
						String equipElementName = slotElement.getName();

						if ("slot".equalsIgnoreCase(equipElementName)) {
							Bodypart bodypart = Bodypart.valueOf(slotElement.attributeValue("id"));
							if (bodypart.getReal() != null)
								slot = bodypart.mask();
							else
								slot |= bodypart.mask();
						} else if ("visual".equalsIgnoreCase(equipElementName)) {
							List<Element> changeElements = slotElement.elements();
							for (Element changeElement : changeElements) {
								Bodypart bodypart = Bodypart.valueOf(changeElement.attributeValue("slot"));
								int toId = Integer.parseInt(changeElement.attributeValue("to_id"));
								var visualChange = new VisualChange(bodypart, toId);
								visualChanges.add(visualChange);
							}
						}
					}
				}
			}
			set.set("bodypart", slot);
			ItemTemplate template = null;
			try
			{
				if("weapon".equalsIgnoreCase(itemElement.getName()))
					template = new WeaponTemplate(set);
				else if("armor".equalsIgnoreCase(itemElement.getName()))
					template = new ArmorTemplate(set);
				else
					template = new EtcItemTemplate(set);
			}
			catch(Exception e)
			{
                warn("Fail create item: " + set.get("item_id"), e);
				continue;
			}

			template.setVisualChanges(visualChanges.build());

			boolean haveNoAltSkill = false;
			Iterator<Element> subIterator2 = itemElement.elementIterator();
			while(subIterator2.hasNext())
			{
				Element subElement2 = subIterator2.next();
				String subName2 = subElement2.getName();
				if("for".equalsIgnoreCase(subName2))
					parseFor(subElement2, template);
				else if("triggers".equalsIgnoreCase(subName2))
					parseTriggers(subElement2, template);
				else if("skills".equalsIgnoreCase(subName2))
				{
					Iterator<Element> nextIterator = subElement2.elementIterator();
					while(nextIterator.hasNext())
					{
						Element nextElement = nextIterator.next();
						int id = Integer.parseInt(nextElement.attributeValue("id"));
						int level = Integer.parseInt(nextElement.attributeValue("level"));
						SkillEntry skillEntry = SkillHolder.getInstance().getSkillEntry(id, level);
						if(skillEntry != null)
						{
							if(!skillEntry.getTemplate().altUse() && template.getHandler() instanceof SkillsItemHandler)
							{
								if(haveNoAltSkill)
                                    warn("Item:" + set.getObject("item_id") + " already has a \"no-alt\" skill: ID[" + skillEntry.getId() + "] LEVEL[" + skillEntry.getLevel() + "] that can lead to malfunction of item!");
								else
									haveNoAltSkill = true;
								if(!skillEntry.getTemplate().isHandler())
                                    warn("Item:" + set.getObject("item_id") + " have \"no-handler\" skill: ID[" + skillEntry.getId() + "] LEVEL[" + skillEntry.getLevel() + "]!");
							}
							template.attachSkill(skillEntry);
						}
						else
                            warn("Skill not found(" + id + "," + level + ") for item:" + set.getObject("item_id") + "; file:" + getCurrentFileName());
					}
				}
				else if("enchant4_skill".equalsIgnoreCase(subName2))
				{
					int id2 = Integer.parseInt(subElement2.attributeValue("id"));
					int level2 = Integer.parseInt(subElement2.attributeValue("level"));
					SkillEntry skillEntry2 = SkillHolder.getInstance().getSkillEntry(id2, level2);
					if(skillEntry2 == null)
						continue;
					template.setEnchant4Skill(skillEntry2);
				}
				else if("cond".equalsIgnoreCase(subName2))
				{
					Condition condition = parseFirstCond(subElement2);
					if(condition == null)
						continue;
					if(subElement2.attributeValue("msgId") != null)
					{
						int msgId = parseNumber(subElement2.attributeValue("msgId"), new int[0]).intValue();
						condition.setSystemMsg(msgId);
					}
					else {
						String customMessageLink = subElement2.attributeValue("customMessageLink");
						if(customMessageLink != null) {
							condition.setCustomMessageLink(customMessageLink);
						}
					}
					template.addCondition(condition);
				}
				else if("attributes".equalsIgnoreCase(subName2))
				{
					int[] attributes = new int[6];
					Iterator<Element> nextIterator2 = subElement2.elementIterator();
					while(nextIterator2.hasNext())
					{
						Element nextElement2 = nextIterator2.next();
						if("attribute".equalsIgnoreCase(nextElement2.getName()))
						{
							l2s.gameserver.model.base.Element element = l2s.gameserver.model.base.Element.getElementByName(nextElement2.attributeValue("element"));
							attributes[element.getId()] = Integer.parseInt(nextElement2.attributeValue("value"));
						}
					}
					template.setBaseAtributeElements(attributes);
				}
				else if("capsuled_items".equalsIgnoreCase(subName2))
				{
					Iterator<Element> nextIterator = subElement2.elementIterator();
					while(nextIterator.hasNext())
					{
						Element nextElement = nextIterator.next();
						if("capsuled_item".equalsIgnoreCase(nextElement.getName()))
						{
							int c_item_id = Integer.parseInt(nextElement.attributeValue("id"));
							long c_min_count = Long.parseLong(nextElement.attributeValue("min_count"));
							long c_max_count = Long.parseLong(nextElement.attributeValue("max_count"));
							double c_chance = nextElement.attributeValue("chance") == null ? 100.0 : Double.parseDouble(nextElement.attributeValue("chance"));
							int enchant_level = nextElement.attributeValue("enchant_level") == null ? 0 : Integer.parseInt(nextElement.attributeValue("enchant_level"));
							template.addCapsuledItem(new CapsuledItemData(c_item_id, c_min_count, c_max_count, c_chance, enchant_level));
						}
					}
				}
				else
				{
					if(!"enchant_options".equalsIgnoreCase(subName2))
						continue;
					Iterator<Element> nextIterator = subElement2.elementIterator();
					while(nextIterator.hasNext())
					{
						Element nextElement = nextIterator.next();
						if("level".equalsIgnoreCase(nextElement.getName()))
						{
							int val = Integer.parseInt(nextElement.attributeValue("value"));
							int i = 0;
							int[] options = new int[3];
							for(Element optionElement : nextElement.elements())
							{
								OptionDataTemplate optionData = OptionDataHolder.getInstance().getTemplate(Integer.parseInt(optionElement.attributeValue("id")));
								if(optionData == null)
                                    error("Not found option_data for id: " + optionElement.attributeValue("id") + "; item_id: " + set.get("item_id"));
								else
									options[i++] = optionData.getId();
							}
							template.addEnchantOptions(val, options);
						}
					}
				}
			}
			getHolder().addItem(template);
		}
	}

	@Override
	protected Object getTableValue(String name, int... arg)
	{
		return null;
	}

	static
	{
		_instance = new ItemParser();
	}
}
