package l2s.gameserver.model.entity.events.objects;

import com.google.common.flogger.FluentLogger;
import l2s.gameserver.Config;
import l2s.gameserver.instancemanager.SpawnManager;
import l2s.gameserver.model.Spawner;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.instances.NpcInstance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author VISTALL
 * @date  17:33/10.12.2010
 */
public class SpawnExObject implements SpawnableObject
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	

	private final List<Spawner> _spawns;
	private boolean _spawned = false;
	private String _name;

	public SpawnExObject(String name)
	{
		_name = name;
		_spawns = SpawnManager.getInstance().getSpawners(_name);
		if(_spawns.isEmpty() && !Config.DONTLOADSPAWN)
			_log.atWarning().log( "SpawnExObject: not found spawn group: %s", name );
	}

	public SpawnExObject(SpawnExObject source)
	{
		_name = source._name;
		_spawns = new ArrayList<Spawner>(source._spawns.size());
		for(Spawner spawn : source._spawns)
			_spawns.add(spawn.clone());
	}

	@Override
	public void spawnObject(Event event, Reflection reflection)
	{
		if(_spawned)
			_log.atWarning().withStackTrace(com.google.common.flogger.StackSize.FULL).log( "SpawnExObject: can\'t spawn twice: %s; event: %s", _name, event );
		else
		{
			for(Spawner spawn : _spawns)
			{
				if(event.isInProgress())
					spawn.addEvent(event);
				else
					spawn.removeEvent(event);

				spawn.setReflection(reflection);
				spawn.init();
			}
			_spawned = true;
		}
	}

	@Override
	public void respawnObject(Event event, Reflection reflection)
	{
		if(!_spawned)
			_log.atWarning().withStackTrace(com.google.common.flogger.StackSize.FULL).log( "SpawnExObject: can\'t respawn, not spawned: %s; event: %s", _name, event );
		else
			for(Spawner spawn : _spawns)
				spawn.init();
	}

	@Override
	public void despawnObject(Event event, Reflection reflection)
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
	public void refreshObject(Event event, Reflection reflection)
	{
		for(NpcInstance npc : getAllSpawned())
		{
			if(event.isInProgress())
				npc.addEvent(event);
			else
				npc.removeEvent(event);
		}
	}

	public List<Spawner> getSpawns()
	{
		return _spawns;
	}

	public List<NpcInstance> getAllSpawned()
	{
		List<NpcInstance> npcs = new ArrayList<NpcInstance>();
		for(Spawner spawn : _spawns)
			npcs.addAll(spawn.getAllSpawned());
		return npcs.isEmpty() ? Collections.<NpcInstance> emptyList() : npcs;
	}

	public NpcInstance getFirstSpawned()
	{
		List<NpcInstance> npcs = getAllSpawned();
		return npcs.size() > 0 ? npcs.get(0) : null;
	}

	public boolean isSpawned()
	{
		return _spawned;
	}
}
