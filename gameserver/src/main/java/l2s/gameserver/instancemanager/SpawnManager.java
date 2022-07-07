package l2s.gameserver.instancemanager;

import l2s.gameserver.Config;
import l2s.gameserver.dao.SpawnsDAO;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.data.xml.holder.SpawnHolder;
import l2s.gameserver.data.xml.parser.SpawnParser;
import l2s.gameserver.listener.game.OnDayNightChangeListener;
import l2s.gameserver.model.HardSpawner;
import l2s.gameserver.model.Spawner;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.templates.spawn.PeriodOfDay;
import l2s.gameserver.templates.spawn.SpawnTemplate;
import l2s.gameserver.time.GameTimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SpawnManager
{
	private static final Logger _log = LoggerFactory.getLogger(SpawnManager.class);
	private static final SpawnManager _instance = new SpawnManager();
	private final Map<String, List<Spawner>> _spawns;
	private final Listeners _listeners;

	public static SpawnManager getInstance()
	{
		return _instance;
	}

	private SpawnManager()
	{
		_spawns = new ConcurrentHashMap<>();
		_listeners = new Listeners();
		for(Map.Entry<String, List<SpawnTemplate>> entry : SpawnHolder.getInstance().getSpawns().entrySet())
			fillSpawn(entry.getKey(), entry.getValue());
		fillSpawn("NONE", SpawnsDAO.getInstance().restore());
		GameTimeService.INSTANCE.addListener(_listeners);
	}

	public List<Spawner> fillSpawn(String group, List<SpawnTemplate> templateList)
	{
		if(Config.DONTLOADSPAWN)
			return Collections.emptyList();

		List<Spawner> spawnerList = _spawns.get(group);

		if(spawnerList == null)
			_spawns.put(group, spawnerList = new ArrayList<>(templateList.size()));

		for(SpawnTemplate template : templateList)
		{
			HardSpawner spawner = new HardSpawner(template);
			spawnerList.add(spawner);
			NpcTemplate npcTemplate = NpcHolder.getInstance().getTemplate(spawner.getMainNpcId());
			if(Config.RATE_MOB_SPAWN > 1 && npcTemplate.getInstanceClass() == MonsterInstance.class && npcTemplate.level >= Config.RATE_MOB_SPAWN_MIN_LEVEL && npcTemplate.level <= Config.RATE_MOB_SPAWN_MAX_LEVEL)
				spawner.setAmount(template.getCount() * Config.RATE_MOB_SPAWN);
			else
				spawner.setAmount(template.getCount());
			spawner.setRespawnDelay(template.getRespawn(), template.getRespawnRandom());
			spawner.setReflection(ReflectionManager.MAIN);
			spawner.setRespawnTime(0);
			if(npcTemplate.isRaid && group.equals(PeriodOfDay.NONE.name()))
				RaidBossSpawnManager.getInstance().addNewSpawn(npcTemplate.getId(), spawner);
		}
		return spawnerList;
	}

	public void spawnAll()
	{
        spawn(PeriodOfDay.NONE.name());
		if(Config.ALLOW_EVENT_GATEKEEPER)
            spawn("event_gatekeeper");
		if(Config.SPAWN_VITAMIN_MANAGER)
            spawn("vitamin_manager");
		if(!Config.ALLOW_CLASS_MASTERS_LIST.isEmpty())
            spawn("class_master");
	}

	public void spawn(String group, boolean logging)
	{
		List<Spawner> spawnerList = _spawns.get(group);
		if(spawnerList == null)
			return;
		int npcSpawnCount = 0;
		for(Spawner spawner : spawnerList)
		{
			npcSpawnCount += spawner.init();
			if(logging && npcSpawnCount % 1000 == 0 && npcSpawnCount != 0)
				_log.info("SpawnManager: spawned " + npcSpawnCount + " npc for group: " + group);
		}
		if(logging)
			_log.debug("SpawnManager: spawned " + npcSpawnCount + " npc; spawns: " + spawnerList.size() + "; group: " + group);
	}

	public void spawn(String group)
	{
        spawn(group, true);
	}

	public void despawn(String group, boolean logging)
	{
		List<Spawner> spawnerList = _spawns.get(group);
		if(spawnerList == null)
			return;
		for(Spawner spawner : spawnerList)
			spawner.deleteAll();
		if(logging)
			_log.debug("SpawnManager: despawned spawns: " + spawnerList.size() + "; group: " + group);
	}

	public void despawn(String group)
	{
		despawn(group, true);
	}

	public List<Spawner> getSpawners(String group)
	{
		List<Spawner> list = _spawns.get(group);
		return list == null ? Collections.emptyList() : list;
	}

	public void reloadAll()
	{
		RaidBossSpawnManager.getInstance().cleanUp();
		for(List<Spawner> spawnerList : _spawns.values())
			for(Spawner spawner : spawnerList)
				spawner.deleteAll();

		parse();

		RaidBossSpawnManager.getInstance().reloadBosses();
		spawnAll();
		if(GameTimeService.INSTANCE.isNowNight())
			_listeners.onNight();
		else
			_listeners.onDay();
	}

	private void parse()
	{
		SpawnParser.getInstance().reload();

		_spawns.clear();

		for(Map.Entry<String, List<SpawnTemplate>> entry : SpawnHolder.getInstance().getSpawns().entrySet())
			fillSpawn(entry.getKey(), entry.getValue());
		fillSpawn("NONE", SpawnsDAO.getInstance().restore());

	}

	private class Listeners implements OnDayNightChangeListener
	{
		@Override
		public void onDay()
		{
			despawn(PeriodOfDay.NIGHT.name());
            spawn(PeriodOfDay.DAY.name());
		}

		@Override
		public void onNight()
		{
			despawn(PeriodOfDay.DAY.name());
            spawn(PeriodOfDay.NIGHT.name());
		}
	}
}
