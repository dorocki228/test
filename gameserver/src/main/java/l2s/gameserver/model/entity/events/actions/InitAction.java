package l2s.gameserver.model.entity.events.actions;

import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.EventAction;

public class InitAction implements EventAction
{
	private final String _name;

	public InitAction(String name)
	{
		_name = name;
	}

	@Override
	public void call(Event event)
	{
		event.initAction(_name);
	}
}
