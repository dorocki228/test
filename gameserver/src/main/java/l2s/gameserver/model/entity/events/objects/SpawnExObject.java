package l2s.gameserver.model.entity.events.objects;

import l2s.gameserver.Config;
import l2s.gameserver.instancemanager.SpawnManager;
import l2s.gameserver.model.Spawner;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.instances.NpcInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SpawnExObject implements SpawnableObject
{
	private static final long serialVersionUID = 1L;
	protected static final Logger _log = LoggerFactory.getLogger(SpawnExObject.class);
	protected final List<Spawner> _spawns;
	protected boolean _spawned;
	protected String _name;

	public SpawnExObject(String name)
	{
		_spawned = false;
		_name = name;
		_spawns = SpawnManager.getInstance().getSpawners(_name);
		if(_spawns.isEmpty() && !Config.DONTLOADSPAWN)
			_log.warn("SpawnExObject: not found spawn group: " + name);
	}

	public SpawnExObject(SpawnExObject source)
	{
		_spawned = false;
		_name = source._name;
		_spawns = new ArrayList<>(source._spawns.size());
		for(Spawner spawn : source._spawns)
			_spawns.add(spawn.clone());
	}

	@Override
	public void spawnObject(Event event)
	{
		if(_spawned)
			_log.warn("SpawnExObject: can't spawn twice: " + _name + "; event: " + event, new Exception());
		else
		{
			for(Spawner spawn : _spawns)
			{
				if(event.isInProgress())
					spawn.addEvent(event);
				else
					spawn.removeEvent(event);
				spawn.setReflection(event.getReflection());
				spawn.init();
			}
			_spawned = true;
		}
	}

	@Override
	public void respawnObject(Event event)
	{
		if(!_spawned)
			_log.warn("SpawnExObject: can't respawn, not spawned: " + _name + "; event: " + event, new Exception());
		else
			for(Spawner spawn : _spawns)
				spawn.init();
	}

	@Override
	public void despawnObject(Event event)
	{
		if(!_spawned)
			return;
		_spawned = false;
		for(Spawner spawn : _spawns)
		{
			spawn.removeEvent(event);
			spawn.deleteAll();
		}
	}

	@Override
	public void refreshObject(Event event)
	{
		for(NpcInstance npc : getAllSpawned())
			if(event.isInProgress())
				npc.addEvent(event);
			else
				npc.removeEvent(event);
	}

	public List<Spawner> getSpawns()
	{
		return _spawns;
	}

	public List<NpcInstance> getAllSpawned()
	{
		List<NpcInstance> npcs = new ArrayList<>();
		for(Spawner spawn : _spawns)
			npcs.addAll(spawn.getAllSpawned());
		return npcs.isEmpty() ? Collections.emptyList() : npcs;
	}

	public NpcInstance getFirstSpawned()
	{
		List<NpcInstance> npcs = getAllSpawned();
		return !npcs.isEmpty() ? npcs.get(0) : null;
	}

	public boolean isSpawned()
	{
		return _spawned;
	}
}
