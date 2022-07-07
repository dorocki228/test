package l2s.gameserver.data.xml.parser;

import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.PetDataHolder;
import l2s.gameserver.model.base.MountType;
import l2s.gameserver.model.base.PetType;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.item.data.RewardItemData;
import l2s.gameserver.templates.pet.PetData;
import l2s.gameserver.templates.pet.PetLevelData;
import l2s.gameserver.templates.pet.PetSkillData;
import org.dom4j.Element;

import java.io.File;
import java.util.Iterator;

public final class PetDataParser extends AbstractParser<PetDataHolder>
{
	private static final PetDataParser _instance;

	public static PetDataParser getInstance()
	{
		return _instance;
	}

	private PetDataParser()
	{
		super(PetDataHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/pets/");
	}

	@Override
	public String getDTDFileName()
	{
		return "pet_data.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Iterator<Element> iterator = rootElement.elementIterator();
		while(iterator.hasNext())
		{
			Element element = iterator.next();
			int npcId = Integer.parseInt(element.attributeValue("npc_id"));
			int controlItemId = element.attributeValue("control_item") == null ? 0 : Integer.parseInt(element.attributeValue("control_item"));
			String[] sync_levels = element.attributeValue("sync_level") == null ? new String[0] : element.attributeValue("sync_level").split(";");
			int[] syncLvls = new int[sync_levels.length];
			for(int i = 0; i < sync_levels.length; ++i)
				syncLvls[i] = Integer.parseInt(sync_levels[i]);
			PetType type = element.attributeValue("type") == null ? PetType.NORMAL : PetType.valueOf(element.attributeValue("type").toUpperCase());
			MountType mountType = element.attributeValue("mount_type") == null ? MountType.NONE : MountType.valueOf(element.attributeValue("mount_type").toUpperCase());
			int minLvl = Integer.MAX_VALUE;
			int maxLvl = 0;
			PetData template = new PetData(npcId, controlItemId, syncLvls, type, mountType);
			Iterator<Element> secondIterator = element.elementIterator();
			while(secondIterator.hasNext())
			{
				Element secondElement = secondIterator.next();
				if("skills".equalsIgnoreCase(secondElement.getName()))
				{
					Iterator<Element> thirdIterator = secondElement.elementIterator("skill");
					while(thirdIterator.hasNext())
					{
						Element thirdElement = thirdIterator.next();
						int skillId = Integer.parseInt(thirdElement.attributeValue("id"));
						int skillLevel = Integer.parseInt(thirdElement.attributeValue("level"));
						int petMinLevel = Integer.parseInt(thirdElement.attributeValue("min_level"));
						template.addSkill(new PetSkillData(skillId, skillLevel, petMinLevel));
					}
				}
				else if("expiration_reward_items".equalsIgnoreCase(secondElement.getName()))
				{
					Iterator<Element> thirdIterator = secondElement.elementIterator("item");
					while(thirdIterator.hasNext())
					{
						Element thirdElement = thirdIterator.next();
						int itemId = Integer.parseInt(thirdElement.attributeValue("id"));
						long minCount = Integer.parseInt(thirdElement.attributeValue("min_count"));
						long maxCount = Integer.parseInt(thirdElement.attributeValue("max_count"));
						double chance = thirdElement.attributeValue("chance") == null ? 100.0 : Integer.parseInt(thirdElement.attributeValue("chance"));
						template.addExpirationRewardItem(new RewardItemData(itemId, minCount, maxCount, chance));
					}
				}
				else
				{
					if(!"level_data".equalsIgnoreCase(secondElement.getName()))
						continue;
					Iterator<Element> thirdIterator = secondElement.elementIterator("stats");
					while(thirdIterator.hasNext())
					{
						Element thirdElement = thirdIterator.next();
						int level = Integer.parseInt(thirdElement.attributeValue("level"));
						if(minLvl > level)
							minLvl = level;
						if(maxLvl < level)
							maxLvl = level;
						StatsSet stats_set = new StatsSet();
						Iterator<Element> fourthIterator = thirdElement.elementIterator("set");
						while(fourthIterator.hasNext())
						{
							Element fourthElement = fourthIterator.next();
							stats_set.set(fourthElement.attributeValue("name"), fourthElement.attributeValue("value"));
						}
						template.addLvlData(level, new PetLevelData(stats_set));
					}
				}
			}
			template.setMinLvl(minLvl);
			template.setMaxLvl(maxLvl);
			getHolder().addTemplate(template);
		}
	}

	static
	{
		_instance = new PetDataParser();
	}
}
