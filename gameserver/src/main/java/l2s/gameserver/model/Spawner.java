package l2s.gameserver.model;

import l2s.commons.collections.MultiValueSet;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.EventOwner;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.instances.PetInstance;
import l2s.gameserver.taskmanager.SpawnTaskManager;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.templates.spawn.SpawnRange;
import l2s.gameserver.utils.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class Spawner extends EventOwner implements Cloneable
{
	protected static final Logger _log = LoggerFactory.getLogger(Spawner.class);
	protected static final int MIN_RESPAWN_DELAY = 20;
	protected int _maximumCount;
	protected int _referenceCount;
	protected int _currentCount;
	protected int _scheduledCount;
	protected int _respawnDelay;
	protected int _respawnDelayRandom;
	protected int _nativeRespawnDelay;
	protected int _respawnTime;
	protected boolean _doRespawn;
	protected NpcInstance _lastSpawn;
	protected List<NpcInstance> _spawned;
	protected Reflection _reflection;

	public Spawner()
	{
		_reflection = ReflectionManager.MAIN;
	}

	public String getName()
	{
		return "";
	}

	public void decreaseScheduledCount()
	{
		if(_scheduledCount > 0)
			--_scheduledCount;
	}

	public boolean isDoRespawn()
	{
		return _doRespawn;
	}

	public Reflection getReflection()
	{
		return _reflection;
	}

	public void setReflection(Reflection reflection)
	{
		_reflection = reflection;
	}

	public int getRespawnDelay()
	{
		return _respawnDelay;
	}

	public int getNativeRespawnDelay()
	{
		return _nativeRespawnDelay;
	}

	public int getRespawnDelayRandom()
	{
		return _respawnDelayRandom;
	}

	public int getRespawnDelayWithRnd()
	{
		return _respawnDelayRandom == 0 ? _respawnDelay : Rnd.get(_respawnDelay - _respawnDelayRandom, _respawnDelay);
	}

	public int getRespawnTime()
	{
		return _respawnTime;
	}

	public NpcInstance getLastSpawn()
	{
		return _lastSpawn;
	}

	public void setAmount(int amount)
	{
		if(_referenceCount == 0)
			_referenceCount = amount;
		_maximumCount = amount;
	}

	public void deleteAll()
	{
		stopRespawn();
		for(NpcInstance npc : _spawned)
			npc.deleteMe();
		_spawned.clear();
		_respawnTime = 0;
		_scheduledCount = 0;
		_currentCount = 0;
	}

	public int getScheduledCount()
	{
		return _scheduledCount;
	}

	public int getCurrentCount()
	{
		return _currentCount;
	}

	public int getMaxCount()
	{
		return _maximumCount;
	}

	public abstract void decreaseCount(NpcInstance p0);

	public abstract NpcInstance doSpawn(boolean p0);

	public abstract void respawnNpc(NpcInstance p0);

	protected abstract NpcInstance initNpc(NpcInstance p0, boolean p1);

	public abstract int getMainNpcId();

	public abstract SpawnRange getRandomSpawnRange();

	@Override
	public abstract Spawner clone();

	public int init()
	{
		while(_currentCount + _scheduledCount < _maximumCount)
			doSpawn(false);
		_doRespawn = true;
		return _currentCount;
	}

	public NpcInstance spawnOne()
	{
		return doSpawn(false);
	}

	public void stopRespawn()
	{
		_doRespawn = false;
	}

	public void startRespawn()
	{
		_doRespawn = true;
	}

	public List<NpcInstance> getAllSpawned()
	{
		return _spawned;
	}

	public NpcInstance getFirstSpawned()
	{
		List<NpcInstance> npcs = getAllSpawned();
		return !npcs.isEmpty() ? npcs.get(0) : null;
	}

	public void setRespawnDelay(int respawnDelay, int respawnDelayRandom)
	{
		if(respawnDelay < 0)
			_log.warn("respawn delay is negative");
		_nativeRespawnDelay = respawnDelay;
		_respawnDelay = respawnDelay;
		_respawnDelayRandom = respawnDelayRandom;
	}

	public void setRespawnDelay(int respawnDelay)
	{
        setRespawnDelay(respawnDelay, 0);
	}

	public void setRespawnTime(int respawnTime)
	{
		_respawnTime = respawnTime;
	}

	protected NpcInstance doSpawn0(NpcTemplate template, boolean spawn, MultiValueSet<String> set)
	{
		if(template.isInstanceOf(PetInstance.class))
		{
			++_currentCount;
			return null;
		}
		NpcInstance tmp = template.getNewInstance(set);
		if(tmp == null)
			return null;
		if(!spawn)
			spawn = _respawnTime <= System.currentTimeMillis() / 1000L + MIN_RESPAWN_DELAY;
		return initNpc(tmp, spawn);
	}

	protected NpcInstance initNpc0(NpcInstance mob, Location newLoc, boolean spawn)
	{
		mob.setCurrentHpMp(mob.getMaxHp(), mob.getMaxMp(), true);
		mob.setSpawn(this);
		mob.setSpawnedLoc(newLoc);
		mob.setUnderground(GeoEngine.getHeight(newLoc, getReflection().getGeoIndex()) < GeoEngine.getHeight(newLoc.clone().changeZ(5000), getReflection().getGeoIndex()));
		for(Event e : getEvents())
			mob.addEvent(e);
		if(spawn)
		{
			mob.setReflection(getReflection());
			if(mob.isMonster())
				((MonsterInstance) mob).setChampion();
			++_currentCount;

			mob.spawnMe(newLoc);
		}
		else
		{
			mob.setLoc(newLoc);
			++_scheduledCount;
			SpawnTaskManager.getInstance().addSpawnTask(mob, _respawnTime * 1000L - System.currentTimeMillis());
		}

		_spawned.add(mob);
		return _lastSpawn = mob;
	}

	public void decreaseCount0(NpcTemplate template, NpcInstance spawnedNpc, long deadTime)
	{
		--_currentCount;
		if(_currentCount < 0)
			_currentCount = 0;
		if(_respawnDelay == 0 && _respawnDelayRandom == 0)
			return;
		if(template == null || spawnedNpc == null)
			return;
		if(_doRespawn && _scheduledCount + _currentCount < _maximumCount)
		{
			++_scheduledCount;
			long delay = (long) (template.isRaid ? Config.ALT_RAID_RESPAWN_MULTIPLIER * getRespawnDelayWithRnd() : getRespawnDelayWithRnd()) * 1000L;
			delay = Math.max(1000L, delay - deadTime);
			_respawnTime = (int) ((System.currentTimeMillis() + delay) / 1000L);
			SpawnTaskManager.getInstance().addSpawnTask(spawnedNpc, delay);
		}
	}

	public List<NpcInstance> initAndReturn()
	{
		List<NpcInstance> spawnedNpcs = new ArrayList<>();
		while(_currentCount + _scheduledCount < _maximumCount)
			spawnedNpcs.add(doSpawn(false));
		_doRespawn = true;
		return spawnedNpcs;
	}

	public String getGroup()
	{
		return "";
	}
}
