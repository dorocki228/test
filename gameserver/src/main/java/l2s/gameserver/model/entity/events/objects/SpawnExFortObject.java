package l2s.gameserver.model.entity.events.objects;

import l2s.gameserver.model.entity.events.Event;

public class SpawnExFortObject extends SpawnExObject
{
	public SpawnExFortObject(SpawnExObject source)
	{
		super(source);
	}

	public SpawnExFortObject(String name)
	{
		super(name);
	}

	@Override
	public void spawnObject(Event event)
	{
		if(_spawned)
			SpawnExObject._log.warn("SpawnExObject: can't spawn twice: " + _name + "; event: " + event, new Exception());
		else
		{
            _spawns.forEach(spawn ->
            {
                spawn.addEvent(event);
                spawn.setReflection(event.getReflection());
                spawn.init();
            });
			_spawned = true;
		}
	}
}
