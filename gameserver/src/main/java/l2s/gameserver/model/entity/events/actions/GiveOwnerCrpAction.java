package l2s.gameserver.model.entity.events.actions;

import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.EventAction;

public class GiveOwnerCrpAction implements EventAction
{
	private final int _count;

	public GiveOwnerCrpAction(int count)
	{
		_count = count;
	}

	@Override
	public void call(Event event)
	{
		event.giveOwnerCrp(_count);
	}
}
