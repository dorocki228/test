package l2s.gameserver.model;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.spawn.SpawnNpcInfo;
import l2s.gameserver.templates.spawn.SpawnRange;
import l2s.gameserver.templates.spawn.SpawnTemplate;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class HardSpawner extends Spawner
{
	private final SpawnTemplate _template;
	private final List<NpcInstance> _reSpawned;

	public HardSpawner(SpawnTemplate template)
	{
		_reSpawned = new CopyOnWriteArrayList<>();
		_template = template;
		_spawned = new CopyOnWriteArrayList<>();
	}

	@Override
	public String getName()
	{
		return _template.getName();
	}

	@Override
	public String getGroup()
	{
		return _template.getGroup();
	}

	@Override
	public void decreaseCount(NpcInstance oldNpc)
	{
		oldNpc.setSpawn(null);
		oldNpc.deleteMe();
		_spawned.remove(oldNpc);
		if(_respawnDelay == 0 && _respawnDelayRandom == 0)
		{
			decreaseCount0(null, null, oldNpc.getDeadTime());
			return;
		}
		SpawnNpcInfo npcInfo = getRandomNpcInfo();
		NpcInstance npc = npcInfo.getTemplate().getNewInstance(npcInfo.getParameters());
		npc.setSpawn(this);
		_reSpawned.add(npc);
		decreaseCount0(npcInfo.getTemplate(), npc, oldNpc.getDeadTime());
	}

	@Override
	public NpcInstance doSpawn(boolean spawn)
	{
		SpawnNpcInfo npcInfo = getRandomNpcInfo();
		return doSpawn0(npcInfo.getTemplate(), spawn, npcInfo.getParameters());
	}

	@Override
	protected NpcInstance initNpc(NpcInstance mob, boolean spawn)
	{
		_reSpawned.remove(mob);
		SpawnRange range = getRandomSpawnRange();
		mob.setSpawnRange(range);
		return initNpc0(mob, range.getRandomLoc(getReflection().getGeoIndex()), spawn);
	}

	@Override
	public int getMainNpcId()
	{
		return _template.getNpcId(0).getTemplate().getId();
	}

	@Override
	public void respawnNpc(NpcInstance oldNpc)
	{
		initNpc(oldNpc, true);
	}

	@Override
	public void deleteAll()
	{
		super.deleteAll();
		for(NpcInstance npc : _reSpawned)
		{
			npc.setSpawn(null);
			npc.deleteMe();
		}
		_reSpawned.clear();
	}

	private SpawnNpcInfo getRandomNpcInfo()
	{
		return Rnd.get(_template.getNpcList());
	}

	@Override
	public SpawnRange getRandomSpawnRange()
	{
		return Rnd.get(_template.getSpawnRangeList());
	}

	@Override
	public HardSpawner clone()
	{
		HardSpawner spawnDat = new HardSpawner(_template);
		spawnDat.setAmount(_maximumCount);
		spawnDat.setRespawnDelay(_respawnDelay, _respawnDelayRandom);
		spawnDat.setRespawnTime(0);
		return spawnDat;
	}

	public List<NpcInstance> getAllReSpawned()
	{
		return _reSpawned;
	}
}
