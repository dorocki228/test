package l2s.gameserver.model.entity.events.actions;

import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.EventAction;

import java.util.Collections;
import java.util.List;

public class IfElseAction implements EventAction
{
	private final String _name;
	private final boolean _reverse;
	private List<EventAction> _ifList;
	private List<EventAction> _elseList;

	public IfElseAction(String name, boolean reverse)
	{
		_ifList = Collections.emptyList();
		_elseList = Collections.emptyList();
		_name = name;
		_reverse = reverse;
	}

	@Override
	public void call(Event event)
	{
		List<EventAction> list = (_reverse ? !event.ifVar(_name) : event.ifVar(_name)) ? _ifList : _elseList;
		for(EventAction action : list)
			action.call(event);
	}

	public void setIfList(List<EventAction> ifList)
	{
		_ifList = ifList;
	}

	public void setElseList(List<EventAction> elseList)
	{
		_elseList = elseList;
	}
}
