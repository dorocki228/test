package l2s.gameserver.instancemanager;

import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.gameserver.data.xml.holder.DoorHolder;
import l2s.gameserver.data.xml.holder.ZoneHolder;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.utils.Location;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReflectionManager
{
	public static final Reflection MAIN = Reflection.createReflection(0);
	public static final Reflection PARNASSUS = Reflection.createReflection(-1);
	public static final Reflection GIRAN_HARBOR = Reflection.createReflection(-2);
	public static final Reflection JAIL = Reflection.createReflection(-3);
	private static final ReflectionManager _instance = new ReflectionManager();
	private final TIntObjectHashMap<Reflection> _reflections;
	private final ReadWriteLock lock;
	private final Lock readLock;
	private final Lock writeLock;

	public static ReflectionManager getInstance()
	{
		return _instance;
	}

	private ReflectionManager()
	{
		_reflections = new TIntObjectHashMap<>();
		lock = new ReentrantReadWriteLock();
		readLock = lock.readLock();
		writeLock = lock.writeLock();
	}

	public void init()
	{
		add(MAIN);
		add(PARNASSUS);
		add(GIRAN_HARBOR);
		add(JAIL);
		MAIN.init(DoorHolder.getInstance().getDoors(), ZoneHolder.getInstance().getZones());
		JAIL.setCoreLoc(new Location(-114648, -249384, -2984));
	}

	public Reflection get(int id)
	{
		readLock.lock();
		try
		{
			return _reflections.get(id);
		}
		finally
		{
			readLock.unlock();
		}
	}

	public Reflection add(Reflection ref)
	{
		writeLock.lock();
		try
		{
			return _reflections.put(ref.getId(), ref);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	public Reflection remove(Reflection ref)
	{
		writeLock.lock();
		try
		{
			return _reflections.remove(ref.getId());
		}
		finally
		{
			writeLock.unlock();
		}
	}

	public TIntObjectHashMap<Reflection> getAll()
	{
		readLock.lock();
		try
		{
			return _reflections;
		}
		finally
		{
			readLock.unlock();
		}
	}

	public int getCountByIzId(int izId)
	{
		readLock.lock();
		try
		{
			int i = 0;
			for(Reflection r : getAll().valueCollection())
				if(r.getInstancedZoneId() == izId)
					++i;
			return i;
		}
		finally
		{
			readLock.unlock();
		}
	}

	public int size()
	{
		return _reflections.size();
	}

}
