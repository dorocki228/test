package l2s.gameserver.data.xml.parser;

import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.FishDataHolder;
import l2s.gameserver.templates.fish.*;
import org.dom4j.Element;

import java.io.File;
import java.util.Iterator;

public class FishDataParser extends AbstractParser<FishDataHolder>
{
	private static final FishDataParser _instance = new FishDataParser();

	private FishDataParser()
	{
		super(FishDataHolder.getInstance());
	}

	public static FishDataParser getInstance()
	{
		return _instance;
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/fish_data.xml");
	}

	@Override
	public String getDTDFileName()
	{
		return "fish_data.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Iterator<Element> iterator = rootElement.elementIterator();
		while(iterator.hasNext())
		{
            Element element = iterator.next();
			if("config".equals(element.getName()))
			{
				Config.FISHING_ONLY_PREMIUM_ACCOUNTS = Boolean.parseBoolean(element.attributeValue("only_premium_accounts"));
				Config.RATE_FISH_DROP_COUNT = Integer.parseInt(element.attributeValue("fish_drop_count_rate"));
				continue;
			}

            int id;
            if("lure".equals(element.getName()))
			{
				id = Integer.parseInt(element.attributeValue("id"));
				double fail_chance = element.attributeValue("fail_chance") == null ? 0.0 : Double.parseDouble(element.attributeValue("fail_chance"));
				int duration_min = element.attributeValue("duration_min") == null ? 0 : Integer.parseInt(element.attributeValue("duration_min"));
				int duration_max = element.attributeValue("duration_max") == null ? 0 : Integer.parseInt(element.attributeValue("duration_max"));
				LureTemplate lure = new LureTemplate(id, fail_chance, duration_min, duration_max);
				Iterator<Element> fishIterator = element.elementIterator("fish");
				while(fishIterator.hasNext())
				{
					Element fishElement = fishIterator.next();
					int fish_id = Integer.parseInt(fishElement.attributeValue("id"));
					double fish_chance = Double.parseDouble(fishElement.attributeValue("chance"));
					int fish_reward_type = fishElement.attributeValue("reward_type") == null ? 0 : Integer.parseInt(fishElement.attributeValue("reward_type"));
					if(fish_chance <= 0.0)
					{
						warn("Fish ID[" + fish_id + "] in lure ID[" + id + "] have wrong chance (chance <= 0)!");
						continue;
					}
					lure.addFish(new FishTemplate(fish_id, fish_chance, fish_reward_type));
				}
				(getHolder()).addLure(lure);
				continue;
			}
			if("rewards".equals(element.getName()))
			{
				int type = Integer.parseInt(element.attributeValue("type"));
				FishRewardsTemplate rewards = new FishRewardsTemplate(type);
				Iterator<Element> rewardIterator = element.elementIterator("reward");
				while(rewardIterator.hasNext())
				{
					Element rewardElement = rewardIterator.next();
					int reward_min_level = Integer.parseInt(rewardElement.attributeValue("min_level"));
					int reward_max_level = rewardElement.attributeValue("max_level") == null ? Config.ALT_MAX_LEVEL : Integer.parseInt(rewardElement.attributeValue("max_level"));
					long reward_sp = Long.parseLong(rewardElement.attributeValue("sp"));
					rewards.addReward(new FishRewardTemplate(reward_min_level, reward_max_level, reward_sp));
				}
				getHolder().addRewards(rewards);
				continue;
			}
			if(!"rod".equals(element.getName()))
				continue;
			id = Integer.parseInt(element.attributeValue("id"));
			double duration_modifier = Double.parseDouble(element.attributeValue("duration_modifier"));
			double reward_modifier = Double.parseDouble(element.attributeValue("reward_modifier"));
			int shot_consume_count = Integer.parseInt(element.attributeValue("shot_consume_count"));
			int refresh_delay = Integer.parseInt(element.attributeValue("refresh_delay"));
			getHolder().addRod(new RodTemplate(id, duration_modifier, reward_modifier, shot_consume_count, refresh_delay));
		}
	}

}
