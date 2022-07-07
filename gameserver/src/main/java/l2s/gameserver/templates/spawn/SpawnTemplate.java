package l2s.gameserver.templates.spawn;

import java.util.ArrayList;
import java.util.List;

public class SpawnTemplate
{
	private final String _name;
	private final String _group;
	private final PeriodOfDay _periodOfDay;
	private final int _count;
	private final int _respawn;
	private final int _respawnRandom;
	private final List<SpawnNpcInfo> _npcList;
	private final List<SpawnRange> _spawnRangeList;

	public SpawnTemplate(String name, String group, PeriodOfDay periodOfDay, int count, int respawn, int respawnRandom)
	{
		_npcList = new ArrayList<>(1);
		_spawnRangeList = new ArrayList<>(1);
		_name = name;
		_group = group;
		_periodOfDay = periodOfDay;
		_count = count;
		_respawn = respawn;
		_respawnRandom = respawnRandom;
	}

	public void addSpawnRange(SpawnRange range)
	{
		_spawnRangeList.add(range);
	}

	public SpawnRange getSpawnRange(int index)
	{
		return _spawnRangeList.get(index);
	}

	public void addNpc(SpawnNpcInfo info)
	{
		_npcList.add(info);
	}

	public SpawnNpcInfo getNpcId(int index)
	{
		return _npcList.get(index);
	}

	public List<SpawnNpcInfo> getNpcList()
	{
		return _npcList;
	}

	public List<SpawnRange> getSpawnRangeList()
	{
		return _spawnRangeList;
	}

	public String getName()
	{
		return _name;
	}

	public String getGroup()
	{
		return _group;
	}

	public int getCount()
	{
		return _count;
	}

	public int getRespawn()
	{
		return _respawn;
	}

	public int getRespawnRandom()
	{
		return _respawnRandom;
	}

	public PeriodOfDay getPeriodOfDay()
	{
		return _periodOfDay;
	}
}
