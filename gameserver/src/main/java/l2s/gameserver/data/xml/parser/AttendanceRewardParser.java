package l2s.gameserver.data.xml.parser;

import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.AttendanceRewardHolder;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.templates.item.data.AttendanceRewardData;
import org.dom4j.Element;

import java.io.File;
import java.util.Iterator;

public final class AttendanceRewardParser extends AbstractParser<AttendanceRewardHolder>
{
	private static final AttendanceRewardParser _instance;

	public static AttendanceRewardParser getInstance()
	{
		return _instance;
	}

	private AttendanceRewardParser()
	{
		super(AttendanceRewardHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/pc_parameters/attendance_rewards.xml");
	}

	@Override
	public String getDTDFileName()
	{
		return "attendance_rewards.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Iterator<Element> iterator = rootElement.elementIterator();
		while(iterator.hasNext())
		{
			Element element = iterator.next();
			if("config".equalsIgnoreCase(element.getName()))
			{
				if(element.attributeValue("reward_by_account") == null)
					continue;
				Config.VIP_ATTENDANCE_REWARDS_REWARD_BY_ACCOUNT = Boolean.parseBoolean(element.attributeValue("reward_by_account"));
			}
			else if("normal_account_rewards".equalsIgnoreCase(element.getName()))
			{
				Iterator<Element> subIterator = element.elementIterator("item");
				while(subIterator.hasNext())
				{
					Element subElement = subIterator.next();
					AttendanceRewardData reward = parseReward(subElement);
					if(reward != null)
						getHolder().addNormalReward(reward);
				}
			}
			else
			{
				if(!"premium_account_rewards".equalsIgnoreCase(element.getName()))
					continue;
				Iterator<Element> subIterator = element.elementIterator("item");
				while(subIterator.hasNext())
				{
					Element subElement = subIterator.next();
					AttendanceRewardData reward = parseReward(subElement);
					if(reward != null)
						getHolder().addPremiumReward(reward);
				}
			}
		}
	}

	private AttendanceRewardData parseReward(Element element)
	{
		int id = Integer.parseInt(element.attributeValue("id"));
		if(ItemHolder.getInstance().getTemplate(id) == null)
		{
            warn("Cannot find item template ID[" + id + "]!");
			return null;
		}
		int count = Integer.parseInt(element.attributeValue("count"));
		boolean unknown = element.attributeValue("unknown") == null || Boolean.parseBoolean(element.attributeValue("unknown"));
		boolean is_best = element.attributeValue("is_best") != null && Boolean.parseBoolean(element.attributeValue("is_best"));
		return new AttendanceRewardData(id, count, unknown, is_best);
	}

	static
	{
		_instance = new AttendanceRewardParser();
	}
}
