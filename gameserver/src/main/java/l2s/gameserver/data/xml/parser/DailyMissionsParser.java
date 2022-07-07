package l2s.gameserver.data.xml.parser;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import l2s.commons.data.xml.AbstractParser;
import l2s.commons.string.StringArrayUtils;
import l2s.commons.time.cron.SchedulingPattern;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.DailyMissionsHolder;
import l2s.gameserver.templates.dailymissions.DailyMissionTemplate;
import l2s.gameserver.templates.dailymissions.DailyRewardTemplate;
import l2s.gameserver.templates.item.data.ItemData;
import org.apache.logging.log4j.core.util.Booleans;
import org.dom4j.Element;

import java.io.File;
import java.util.Iterator;

public final class DailyMissionsParser extends AbstractParser<DailyMissionsHolder>
{
	private static final DailyMissionsParser _instance;

	public static DailyMissionsParser getInstance()
	{
		return _instance;
	}

	private DailyMissionsParser()
	{
		super(DailyMissionsHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/daily_missions.xml");
	}

	@Override
	public String getDTDFileName()
	{
		return "daily_missions.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Iterator<Element> iterator = rootElement.elementIterator();
		while(iterator.hasNext())
		{
			Element element = iterator.next();
			int id = Integer.parseInt(element.attributeValue("id"));
			if(id > 255)
			{
				error("Mission id can't be higher than 255.");
				continue;
			}
			String handler = element.attributeValue("handler");
			String value = element.attributeValue("value");
			var repetitionCount = element.attributeValue("repetition_count") == null
                    ? 0 : Integer.parseInt(element.attributeValue("repetition_count"));;
			var reusePattern = element.attributeValue("reuse_pattern") == null
					? null : new SchedulingPattern(element.attributeValue("reuse_pattern"));
			var checkHwid = Booleans.parseBoolean(element.attributeValue("check_hwid"), false);
			var partyShared = Booleans.parseBoolean(element.attributeValue("party_shared"), false);
			DailyMissionTemplate mission = new DailyMissionTemplate(id, handler, value, repetitionCount, reusePattern, checkHwid, partyShared);
			Iterator<Element> rewardsIterator = element.elementIterator("rewards");
			while(rewardsIterator.hasNext())
			{
				Element rewardsElement = rewardsIterator.next();
				int[] classes = rewardsElement.attributeValue("classes") == null ? null : StringArrayUtils.stringToIntArray(rewardsElement.attributeValue("classes"), ",");
				TIntSet classIds = classes == null ? null : new TIntHashSet(classes);
				DailyRewardTemplate reward = new DailyRewardTemplate(classIds);
				Iterator<Element> rewardIterator = rewardsElement.elementIterator("reward");
				while(rewardIterator.hasNext())
				{
					Element rewardElement = rewardIterator.next();
					int rewardId = Integer.parseInt(rewardElement.attributeValue("id"));
					long rewardCount = Long.parseLong(rewardElement.attributeValue("count"));
					reward.addRewardItem(new ItemData(rewardId, rewardCount));
				}
				mission.addReward(reward);
			}
			getHolder().addMission(mission);
		}
	}

	static
	{
		_instance = new DailyMissionsParser();
	}
}
