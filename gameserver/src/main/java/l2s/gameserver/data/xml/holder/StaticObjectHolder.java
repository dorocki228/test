package l2s.gameserver.data.xml.holder;

import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.model.instances.StaticObjectInstance;
import l2s.gameserver.templates.StaticObjectTemplate;

import java.util.HashMap;
import java.util.Map;

public final class StaticObjectHolder extends AbstractHolder
{
	private static final StaticObjectHolder _instance = new StaticObjectHolder();
	private final Map<Integer, StaticObjectTemplate> _templates;
	private final Map<Integer, StaticObjectInstance> _spawned;

	public StaticObjectHolder()
	{
		_templates = new HashMap<>();
		_spawned = new HashMap<>();
	}

	public static StaticObjectHolder getInstance()
	{
		return _instance;
	}

	public void addTemplate(StaticObjectTemplate template)
	{
		_templates.put(template.getUId(), template);
	}

	public StaticObjectTemplate getTemplate(int id)
	{
		return _templates.get(id);
	}

	public void spawnAll()
	{
		for(StaticObjectTemplate template : _templates.values())
			if(template.isSpawn())
			{
				StaticObjectInstance obj = template.newInstance();
				_spawned.put(template.getUId(), obj);
			}
        info("spawned: " + _spawned.size() + " static object(s).");
	}

	public StaticObjectInstance getObject(int id)
	{
		return _spawned.get(id);
	}

	@Override
	public int size()
	{
		return _templates.size();
	}

	@Override
	public void clear()
	{
		_templates.clear();
	}
}
