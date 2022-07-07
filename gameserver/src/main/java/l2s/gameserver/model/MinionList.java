package l2s.gameserver.model;

import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.npc.MinionData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MinionList implements Runnable
{
	private final Set<MinionData> _minionData;
	private final Set<NpcInstance> _minions;
	private final Lock lock;
	private final NpcInstance _master;

	public MinionList(NpcInstance master)
	{
		_master = master;
		_minions = new HashSet<>();
		(_minionData = new HashSet<>()).addAll(_master.getTemplate().getMinionData());
		lock = new ReentrantLock();
	}

	@Override
	public void run()
	{
		if(_master.isVisible() && !_master.isDead())
			spawnMinions();
	}

	public boolean addMinion(MinionData m)
	{
		lock.lock();
		try
		{
			return _minionData.add(m);
		}
		finally
		{
			lock.unlock();
		}
	}

	public boolean addMinion(NpcInstance m)
	{
		lock.lock();
		try
		{
			return _minions.add(m);
		}
		finally
		{
			lock.unlock();
		}
	}

	public boolean hasAliveMinions()
	{
		lock.lock();
		try
		{
			for(NpcInstance m : _minions)
				if(m.isVisible() && !m.isDead())
					return true;
		}
		finally
		{
			lock.unlock();
		}
		return false;
	}

	public boolean hasMinions()
	{
		return !_minionData.isEmpty() || !_minions.isEmpty();
	}

	public List<NpcInstance> getAliveMinions()
	{
		List<NpcInstance> result = new ArrayList<>(_minions.size());
		lock.lock();
		try
		{
			for(NpcInstance m : _minions)
				if(m.isVisible() && !m.isDead())
					result.add(m);
		}
		finally
		{
			lock.unlock();
		}
		return result;
	}

	public void spawnMinions()
	{
		for(MinionData minion : _minionData)
			spawnMinion(minion.getMinionId(), minion.getAmount(), true);
	}

	public void spawnMinion(int minionId, int minionCount, boolean onSpawn)
	{
		if(_master.isMinion() && _master.getNpcId() == minionId)
			return;
		lock.lock();
		try
		{
			int count = minionCount;
			for(NpcInstance m : _minions)
				if(m.getNpcId() == minionId && (onSpawn || m.isDead()))
				{
					--count;
					m.stopDecay();
					m.decayMe();
					m.refreshID();
					_master.spawnMinion(m);
				}
			if(count > 0)
				for(int i = 0; i < count; ++i)
				{
					NpcInstance m = NpcHolder.getInstance().getTemplate(minionId).getNewInstance();
					m.setLeader(_master);
					_master.spawnMinion(m);
					_minions.add(m);
				}
		}
		finally
		{
			lock.unlock();
		}
	}

	public void unspawnMinions()
	{
		lock.lock();
		try
		{
			for(NpcInstance m : _minions)
				m.decayMe();
		}
		finally
		{
			lock.unlock();
		}
	}

	public void deleteMinions()
	{
		lock.lock();
		try
		{
			for(NpcInstance m : _minions)
				m.deleteMe();
			_minions.clear();
		}
		finally
		{
			lock.unlock();
		}
	}
}
