package l2s.gameserver.templates.dailymissions;

import gnu.trove.set.TIntSet;
import l2s.gameserver.templates.item.data.ItemData;

import java.util.ArrayList;
import java.util.List;

public class DailyRewardTemplate
{
	private final TIntSet _classIds;
	private final List<ItemData> _rewardItems;

	public DailyRewardTemplate(TIntSet classIds)
	{
		_rewardItems = new ArrayList<>();
		_classIds = classIds;
	}

	public boolean containsClassId(int classId)
	{
		return _classIds == null || _classIds.contains(classId);
	}

	public void addRewardItem(ItemData item)
	{
		_rewardItems.add(item);
	}

	public ItemData[] getRewardItems()
	{
		return _rewardItems.toArray(new ItemData[0]);
	}
}
