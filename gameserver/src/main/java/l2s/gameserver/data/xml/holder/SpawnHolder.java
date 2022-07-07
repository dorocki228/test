package l2s.gameserver.data.xml.holder;

import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.templates.spawn.SpawnTemplate;

import java.util.*;

public final class SpawnHolder extends AbstractHolder
{
	private static final SpawnHolder _instance;
	private final Map<String, List<SpawnTemplate>> _spawns;

	public SpawnHolder()
	{
		_spawns = new HashMap<>();
	}

	public static SpawnHolder getInstance()
	{
		return _instance;
	}

	public void addSpawn(String group, SpawnTemplate spawn)
	{
        List<SpawnTemplate> spawns = _spawns.computeIfAbsent(group, k -> new ArrayList<>());
        spawns.add(spawn);
	}

	public List<SpawnTemplate> getSpawn(String name)
	{
		List<SpawnTemplate> template = _spawns.get(name);
		return template == null ? Collections.emptyList() : template;
	}

	@Override
	public int size()
	{
		int i = 0;
		for(List<SpawnTemplate> l : _spawns.values())
			i += l.size();
		return i;
	}

	@Override
	public void clear()
	{
		_spawns.clear();
	}

	public Map<String, List<SpawnTemplate>> getSpawns()
	{
		return _spawns;
	}

	static
	{
		_instance = new SpawnHolder();
	}
}
