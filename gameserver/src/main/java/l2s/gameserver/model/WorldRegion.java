package l2s.gameserver.model;

import l2s.commons.lang.ArrayUtils;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class WorldRegion implements Iterable<GameObject>
{
	public static final WorldRegion[] EMPTY_L2WORLDREGION_ARRAY;

	private final short tileX;
	private final short tileY;
	private final short tileZ;

	private volatile GameObject[] _objects = GameObject.EMPTY_L2OBJECT_ARRAY;
	private int _objectsCount;
	private volatile Zone[] _zones = Zone.EMPTY_L2ZONE_ARRAY;
	private short _playersCount;

	private final AtomicBoolean _isActive = new AtomicBoolean();
	private Future<?> _activateTask;
	private final Lock lock = new ReentrantLock();

	WorldRegion(int x, int y, int z)
	{
		tileX = (short) x;
		tileY = (short) y;
		tileZ = (short) z;
	}

	short getX()
	{
		return tileX;
	}

	short getY()
	{
		return tileY;
	}

	short getZ()
	{
		return tileZ;
	}

	void setActive(boolean activate)
	{
		if(!_isActive.compareAndSet(!activate, activate))
			return;
		for(GameObject obj : this)
		{
			if(!obj.isNpc())
				continue;
			NpcInstance npc = (NpcInstance) obj;
			if(npc.getAI().isActive() == isActive())
				continue;
			if(isActive())
			{
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				npc.getAI().startAITask();
				npc.startRandomAnimation();
			}
			else
			{
				if(npc.getAI().isGlobalAI())
					continue;
				npc.stopRandomAnimation();
				npc.getAI().stopAITask();
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			}
		}
	}

	void addToPlayers(GameObject object, Creature dropper)
	{
		if(object == null)
			return;
		Player player = null;
		if(object.isPlayer())
			player = (Player) object;
		else if(object.isObservePoint())
			player = object.getPlayer();
		int oid = object.getObjectId();
		int rid = object.getReflectionId();
		for(GameObject obj : this)
			if(obj.getObjectId() != oid)
			{
				if(obj.getReflectionId() != rid)
					continue;
				if(player != null)
					player.sendPacket(player.addVisibleObject(obj, null));
				if(!obj.isPlayer() && !obj.isObservePoint())
					continue;
				if(obj.isPlayer() && ((Player) obj).isInObserverMode())
					continue;
				Player p = obj.getPlayer();
				p.sendPacket(p.addVisibleObject(object, dropper));
			}
	}

	void removeFromPlayers(GameObject object)
	{
		if(object == null)
			return;
		Player player = null;
		if(object.isPlayer())
			player = (Player) object;
		else if(object.isObservePoint())
			player = object.getPlayer();
		int oid = object.getObjectId();
		int rid = object.getReflectionId();
		List<L2GameServerPacket> d = null;
		for(GameObject obj : this)
			if(obj.getObjectId() != oid)
			{
				if(obj.getReflectionId() != rid)
					continue;
				if(player != null)
					player.sendPacket(player.removeVisibleObject(obj, null));
				if(!obj.isPlayer() && !obj.isObservePoint())
					continue;
				if(obj.isPlayer() && ((Player) obj).isInObserverMode())
					continue;
				Player p = obj.getPlayer();
				p.sendPacket(p.removeVisibleObject(object, d == null ? (d = object.deletePacketList(p)) : d));
			}
	}

	public void addObject(GameObject obj)
	{
		if(obj == null)
			return;
		lock.lock();
		try
		{
			GameObject[] objects = _objects;
			GameObject[] resizedObjects = new GameObject[_objectsCount + 1];
			System.arraycopy(objects, 0, resizedObjects, 0, _objectsCount);
			objects = resizedObjects;
			objects[_objectsCount++] = obj;
			_objects = resizedObjects;
			if(obj.isPlayer() && _playersCount++ == 0)
			{
				if(_activateTask != null)
					_activateTask.cancel(false);
				_activateTask = ThreadPoolManager.getInstance().schedule(new ActivateTask(true), 1000L);
			}
		}
		finally
		{
			lock.unlock();
		}
	}

	public void removeObject(GameObject obj)
	{
		if(obj == null)
			return;
		lock.lock();
		try
		{
			GameObject[] objects = _objects;
			int index = -1;
			for(int i = 0; i < _objectsCount; ++i)
				if(objects[i] == obj)
				{
					index = i;
					break;
				}
			if(index == -1)
				return;
			--_objectsCount;
			GameObject[] resizedObjects = new GameObject[_objectsCount];
			objects[index] = objects[_objectsCount];
			System.arraycopy(objects, 0, resizedObjects, 0, _objectsCount);
			_objects = resizedObjects;
			if(obj.isPlayer() && --_playersCount == 0)
			{
				if(_activateTask != null)
					_activateTask.cancel(false);
				_activateTask = ThreadPoolManager.getInstance().schedule(new ActivateTask(false), 60000L);
			}
		}
		finally
		{
			lock.unlock();
		}
	}

	public int getObjectsSize()
	{
		return _objectsCount;
	}

	public int getPlayersCount()
	{
		return _playersCount;
	}

	public boolean isEmpty()
	{
		return _playersCount == 0;
	}

	public boolean isActive()
	{
		return _isActive.get();
	}

	void addZone(Zone zone)
	{
		lock.lock();
		try
		{
			_zones = ArrayUtils.add(_zones, zone);
		}
		finally
		{
			lock.unlock();
		}
	}

	void removeZone(Zone zone)
	{
		lock.lock();
		try
		{
			_zones = ArrayUtils.remove(_zones, zone);
		}
		finally
		{
			lock.unlock();
		}
	}

	Zone[] getZones()
	{
		return _zones;
	}

	@Override
	public String toString()
	{
		return "[" + tileX + ", " + tileY + ", " + tileZ + "]";
	}

	@Override
	public Iterator<GameObject> iterator()
	{
		return new InternalIterator(_objects);
	}

	static
	{
		EMPTY_L2WORLDREGION_ARRAY = new WorldRegion[0];
	}

	public class ActivateTask implements Runnable
	{
		private final boolean _isActivating;

		public ActivateTask(boolean isActivating)
		{
			_isActivating = isActivating;
		}

		@Override
		public void run()
		{
			if(_isActivating)
				World.activate(WorldRegion.this);
			else
				World.deactivate(WorldRegion.this);
		}
	}

	private class InternalIterator implements Iterator<GameObject>
	{
		final GameObject[] objects;
		int cursor;

		public InternalIterator(GameObject[] objects)
		{
			cursor = 0;
			this.objects = objects;
		}

		@Override
		public boolean hasNext()
		{
			return cursor < objects.length && objects[cursor] != null;
		}

		@Override
		public GameObject next()
		{
			return objects[cursor++];
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}
}
