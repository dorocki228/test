package l2s.gameserver.model.entity.events.actions;

import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.EventAction;

public class SpawnDespawnAction implements EventAction
{
	private final boolean _spawn;
	private final String _name;
	private final int _delay;

	public SpawnDespawnAction(String name, int delay, boolean spawn)
	{
		_spawn = spawn;
		_name = name;
		_delay = delay;
	}

	@Override
	public void call(Event event)
	{
		event.spawnAction(_name, _delay, _spawn);
	}
}
