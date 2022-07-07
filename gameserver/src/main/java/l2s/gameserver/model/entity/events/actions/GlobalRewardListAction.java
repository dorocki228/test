package l2s.gameserver.model.entity.events.actions;

import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.EventAction;
import l2s.gameserver.model.reward.RewardGroup;
import l2s.gameserver.model.reward.RewardList;
import l2s.gameserver.templates.npc.NpcTemplate;

import java.util.List;

public class GlobalRewardListAction implements EventAction
{
	private final boolean _add;
	private final String _name;

	public GlobalRewardListAction(boolean add, String name)
	{
		_add = add;
		_name = name;
	}

	@Override
	public void call(Event event)
	{
		List<Object> list = event.getObjects(_name);
		for(NpcTemplate npc : NpcHolder.getInstance().getAll())
			Label_0202:
			{
				if(npc != null && !npc.getRewards().isEmpty())
					for(RewardList rl : npc.getRewards())
						for(RewardGroup rg : rl)
							if(!rg.isAdena())
							{
								for(Object o : list)
									if(o instanceof RewardList)
										if(_add)
											npc.addRewardList((RewardList) o);
										else
											npc.removeRewardList((RewardList) o);
								break Label_0202;
							}
			}
	}
}
