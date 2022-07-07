package l2s.gameserver.model;

import l2s.commons.util.Rnd;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.templates.spawn.SpawnRange;
import l2s.gameserver.utils.Location;

import java.util.ArrayList;

public class SimpleSpawner extends Spawner implements Cloneable
{
	//	private static final Logger _log = LoggerFactory.getLogger(SimpleSpawner.class);
	private final NpcTemplate _npcTemplate;
	private int _locx;
	private int _locy;
	private int _locz;
	private int _heading;
	private Territory _territory;

	public SimpleSpawner(NpcTemplate mobTemplate)
	{
		if(mobTemplate == null)
			throw new NullPointerException();
		_npcTemplate = mobTemplate;
		_spawned = new ArrayList<>(1);
	}

	public SimpleSpawner(int npcId)
	{
		NpcTemplate mobTemplate = NpcHolder.getInstance().getTemplate(npcId);
		if(mobTemplate == null)
			throw new NullPointerException("Not find npc: " + npcId);
		_npcTemplate = mobTemplate;
		_spawned = new ArrayList<>(1);
	}

	public int getAmount()
	{
		return _maximumCount;
	}

	public int getSpawnedCount()
	{
		return _currentCount;
	}

	public int getSheduledCount()
	{
		return _scheduledCount;
	}

	public Territory getTerritory()
	{
		return _territory;
	}

	public Location getLoc()
	{
		return new Location(_locx, _locy, _locz);
	}

	public int getLocx()
	{
		return _locx;
	}

	public int getLocy()
	{
		return _locy;
	}

	public int getLocz()
	{
		return _locz;
	}

	@Override
	public int getMainNpcId()
	{
		return _npcTemplate.getId();
	}

	@Override
	public SpawnRange getRandomSpawnRange()
	{
		if(_locx == 0 && _locz == 0)
			return _territory;
		return getLoc();
	}

	public int getHeading()
	{
		return _heading;
	}

	public void restoreAmount()
	{
		_maximumCount = _referenceCount;
	}

	public void setTerritory(Territory territory)
	{
		_territory = territory;
	}

	public void setLoc(Location loc)
	{
		_locx = loc.x;
		_locy = loc.y;
		_locz = loc.z;
		_heading = loc.h;
	}

	public void setLocx(int locx)
	{
		_locx = locx;
	}

	public void setLocy(int locy)
	{
		_locy = locy;
	}

	public void setLocz(int locz)
	{
		_locz = locz;
	}

	public void setHeading(int heading)
	{
		_heading = heading;
	}

	@Override
	public void decreaseCount(NpcInstance oldNpc)
	{
		decreaseCount0(_npcTemplate, oldNpc, oldNpc.getDeadTime());
	}

	@Override
	public NpcInstance doSpawn(boolean spawn)
	{
		return doSpawn0(_npcTemplate, spawn, StatsSet.EMPTY);
	}

	@Override
	protected NpcInstance initNpc(NpcInstance mob, boolean spawn)
	{
		Location newLoc;
		if(_territory != null)
		{
			newLoc = _territory.getRandomLoc(_reflection.getGeoIndex());
			newLoc.setH(Rnd.get(65535));
		}
		else
		{
			newLoc = getLoc();
			newLoc.h = getHeading() == -1 ? Rnd.get(65535) : getHeading();
		}
		return initNpc0(mob, newLoc, spawn);
	}

	@Override
	public void respawnNpc(NpcInstance oldNpc)
	{
		oldNpc.refreshID();
		initNpc(oldNpc, true);
	}

	@Override
	public SimpleSpawner clone()
	{
		SimpleSpawner spawnDat = new SimpleSpawner(_npcTemplate);
		spawnDat.setTerritory(_territory);
		spawnDat.setLocx(_locx);
		spawnDat.setLocy(_locy);
		spawnDat.setLocz(_locz);
		spawnDat.setHeading(_heading);
		spawnDat.setAmount(_maximumCount);
		spawnDat.setRespawnDelay(_respawnDelay, _respawnDelayRandom);
		return spawnDat;
	}
}
