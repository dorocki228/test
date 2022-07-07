package l2s.gameserver.templates.skill.restoration;

import l2s.commons.util.Rnd;
import l2s.gameserver.network.l2.components.SystemMsg;

import java.util.ArrayList;
import java.util.List;

public final class RestorationInfo
{
	private final int _itemConsumeId;
	private final int _itemConsumeCount;
	private final SystemMsg _onFailMessage;
	private final List<RestorationGroup> _restorationGroups;

	public RestorationInfo(int itemConsumeId, int itemConsumeCount, int onFailMessage)
	{
		_itemConsumeId = itemConsumeId;
		_itemConsumeCount = itemConsumeCount;
		if(onFailMessage > 0)
			_onFailMessage = SystemMsg.valueOf(onFailMessage);
		else
			_onFailMessage = null;
		_restorationGroups = new ArrayList<>();
	}

	public int getItemConsumeId()
	{
		return _itemConsumeId;
	}

	public int getItemConsumeCount()
	{
		return _itemConsumeCount;
	}

	public SystemMsg getOnFailMessage()
	{
		return _onFailMessage;
	}

	public void addRestorationGroup(RestorationGroup group)
	{
		_restorationGroups.add(group);
	}

	public List<RestorationItem> getRandomGroupItems()
	{
		double chancesAmount = 0.0;
		for(RestorationGroup group : _restorationGroups)
			chancesAmount += group.getChance();
		if(Rnd.chance(chancesAmount))
		{
			double chanceMod = (100.0 - chancesAmount) / _restorationGroups.size();
			List<RestorationGroup> successGroups = new ArrayList<>();
			int tryCount = 0;
			while(successGroups.isEmpty())
			{
				++tryCount;
				for(RestorationGroup group2 : _restorationGroups)
				{
					if(tryCount % 10 == 0)
						++chanceMod;
					if(Rnd.chance(group2.getChance() + chanceMod))
						successGroups.add(group2);
				}
			}
			RestorationGroup[] groupsArray = successGroups.toArray(new RestorationGroup[0]);
			return groupsArray[Rnd.get(groupsArray.length)].getRestorationItems();
		}
		return new ArrayList<>(0);
	}
}
