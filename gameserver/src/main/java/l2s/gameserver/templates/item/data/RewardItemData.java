package l2s.gameserver.templates.item.data;

import com.google.common.collect.Range;
import l2s.commons.util.Rnd;
import l2s.gameserver.utils.Strings;
import l2s.gameserver.utils.XMLUtil;
import org.dom4j.Element;

public class RewardItemData extends ChancedItemData
{
	private final long _maxCount;

	public RewardItemData(int id, long minCount, long maxCount, double chance)
	{
		super(id, minCount, chance);
		_maxCount = maxCount;
	}

	public RewardItemData(int id, Range<Long> count, double chance)
	{
		super(id, count.lowerEndpoint(), chance);
		_maxCount = count.upperEndpoint();
	}

	public RewardItemData(Element element)
	{
		this(Integer.parseInt(element.attributeValue("id")),
				XMLUtil.getRange(element, "count").get(),
				Double.parseDouble(element.attributeValue("chance")));
	}

	public RewardItemData(String text)
	{
		this(text.split(","));
	}

	public RewardItemData(String[] parts)
	{
		this(Integer.parseInt(parts[0]),
				Strings.getRange(parts[1]).get(),
				Double.parseDouble(parts[2]));
	}

	public long getMinCount()
	{
		return getCount();
	}

	public long getMaxCount()
	{
		return _maxCount;
	}

    public long getRandomCount()
    {
        return Rnd.get(getMinCount(), getMaxCount());
    }
}
