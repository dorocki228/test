package l2s.gameserver.data;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.data.xml.holder.ShuttleTemplateHolder;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.model.entity.boat.Boat;
import l2s.gameserver.model.entity.boat.Shuttle;
import l2s.gameserver.templates.CreatureTemplate;
import l2s.gameserver.templates.ShuttleTemplate;

import java.lang.reflect.Constructor;

public final class BoatHolder extends AbstractHolder
{
	public static final CreatureTemplate TEMPLATE;
	private static final BoatHolder _instance;
	private final TIntObjectHashMap<Boat> _boats;

	public BoatHolder()
	{
		_boats = new TIntObjectHashMap();
	}

	public static BoatHolder getInstance()
	{
		return _instance;
	}

	public void spawnAll()
	{
		log();
		TIntObjectIterator<Boat> iterator = _boats.iterator();
		while(iterator.hasNext())
		{
			iterator.advance();
			iterator.value().spawnMe();
            info("Spawning: " + iterator.value().getName());
		}
	}

	public Boat initBoat(String name, String clazz)
	{
		try
		{
			Class<?> cl = Class.forName("l2s.gameserver.model.entity.boat." + clazz);
			Constructor<?> constructor = cl.getConstructor(Integer.TYPE, CreatureTemplate.class);
			Boat boat = (Boat) constructor.newInstance(IdFactory.getInstance().getNextId(), TEMPLATE);
			boat.setName(name);
			addBoat(boat);
			return boat;
		}
		catch(Exception e)
		{
            error("Fail to init boat: " + clazz, e);
			return null;
		}
	}

	public Shuttle initShuttle(String name, int shuttleId)
	{
		try
		{
			ShuttleTemplate template = ShuttleTemplateHolder.getInstance().getTemplate(shuttleId);
			Shuttle shuttle = new Shuttle(IdFactory.getInstance().getNextId(), template);
			shuttle.setName(name);
			addBoat(shuttle);
			return shuttle;
		}
		catch(Exception e)
		{
            error("Fail to init shuttle id: " + shuttleId, e);
			return null;
		}
	}

	public Boat getBoat(String name)
	{
		TIntObjectIterator<Boat> iterator = _boats.iterator();
		while(iterator.hasNext())
		{
			iterator.advance();
			if(iterator.value().getName().equals(name))
				return iterator.value();
		}
		return null;
	}

	public Boat getBoat(int boatId)
	{
		return _boats.get(boatId);
	}

	public void addBoat(Boat boat)
	{
		_boats.put(boat.getBoatId(), boat);
	}

	public void removeBoat(Boat boat)
	{
		_boats.remove(boat.getBoatId());
	}

	@Override
	public int size()
	{
		return _boats.size();
	}

	@Override
	public void clear()
	{
		_boats.clear();
	}

	static
	{
		TEMPLATE = new CreatureTemplate(CreatureTemplate.getEmptyStatsSet());
		_instance = new BoatHolder();
	}
}
