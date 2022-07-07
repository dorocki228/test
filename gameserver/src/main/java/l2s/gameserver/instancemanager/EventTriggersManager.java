package l2s.gameserver.instancemanager;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.network.l2.s2c.EventTriggerPacket;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;
import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.impl.HashIntSet;

public class EventTriggersManager
{
	private static final EventTriggersManager _instance;
	private static final int[] EMPTY_INT_ARRAY;
	private final IntObjectMap<IntSet> _activeTriggers;
	private final IntObjectMap<IntSet> _activeTriggersByMap;

	public static EventTriggersManager getInstance()
	{
		return _instance;
	}

	private EventTriggersManager()
	{
		_activeTriggers = new HashIntObjectMap();
		_activeTriggersByMap = new HashIntObjectMap();
	}

	public boolean addTrigger(Reflection reflection, int triggerId)
	{
		IntSet triggers = _activeTriggers.get(reflection.getId());
		if(triggers == null)
		{
			triggers = new HashIntSet();
			_activeTriggers.put(reflection.getId(), triggers);
		}
		if(triggers.add(triggerId))
		{
			onAddTrigger(reflection, triggerId);
			return true;
		}
		return false;
	}

	public boolean addTrigger(int mapX, int mapY, int triggerId)
	{
		IntSet triggers = _activeTriggersByMap.get(getMapHash(mapX, mapY));
		if(triggers == null)
		{
			triggers = new HashIntSet();
			_activeTriggersByMap.put(getMapHash(mapX, mapY), triggers);
		}
		if(triggers.add(triggerId))
		{
			onAddTrigger(ReflectionManager.MAIN, triggerId);
			return true;
		}
		return false;
	}

	public boolean removeTrigger(Reflection reflection, int triggerId)
	{
		IntSet triggers = _activeTriggers.get(reflection.getId());
		if(triggers != null && triggers.remove(triggerId))
		{
			onRemoveTrigger(reflection, triggerId);
			return true;
		}
		return false;
	}

	public boolean removeTrigger(int mapX, int mapY, int triggerId)
	{
		IntSet triggers = _activeTriggersByMap.get(getMapHash(mapX, mapY));
		if(triggers != null && triggers.remove(triggerId))
		{
			onRemoveTrigger(ReflectionManager.MAIN, triggerId);
			return true;
		}
		return false;
	}

	public int[] getTriggers(Reflection reflection, boolean all)
	{
		if(all && reflection.isMain())
		{
			IntSet allTriggers = new HashIntSet();
			IntSet triggers = _activeTriggers.get(reflection.getId());
			if(triggers != null)
				allTriggers.addAll(triggers);
			for(IntSet t : _activeTriggersByMap.values())
				allTriggers.addAll(t);
			return allTriggers.toArray();
		}
		IntSet triggers2 = _activeTriggers.get(reflection.getId());
		if(triggers2 == null)
			return EMPTY_INT_ARRAY;
		return triggers2.toArray();
	}

	public int[] getTriggers(int mapX, int mapY)
	{
		IntSet triggers = _activeTriggersByMap.get(getMapHash(mapX, mapY));
		if(triggers == null)
			return EMPTY_INT_ARRAY;
		return triggers.toArray();
	}

	public void removeTriggers(Reflection reflection)
	{
		IntSet triggers = _activeTriggers.remove(reflection.getId());
		if(triggers != null)
			for(int triggerId : triggers.toArray())
				onRemoveTrigger(reflection, triggerId);
		if(reflection.isMain())
		{
			for(IntSet t : _activeTriggersByMap.values())
				for(int triggerId2 : t.toArray())
					onRemoveTrigger(reflection, triggerId2);
			_activeTriggersByMap.clear();
		}
	}

	private void onAddTrigger(Reflection reflection, int triggerId)
	{
		EventTriggerPacket packet = new EventTriggerPacket(triggerId, true);
		for(Player player : reflection.getPlayers())
			player.sendPacket(packet);
	}

	private void onRemoveTrigger(Reflection reflection, int triggerId)
	{
		EventTriggerPacket packet = new EventTriggerPacket(triggerId, false);
		for(Player player : reflection.getPlayers())
			player.sendPacket(packet);
	}

	private static int getMapHash(int mapX, int mapY)
	{
		return mapX * 1000 + mapY;
	}

	static
	{
		_instance = new EventTriggersManager();
		EMPTY_INT_ARRAY = new int[0];
	}
}
