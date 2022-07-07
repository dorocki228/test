package l2s.gameserver.model;

import l2s.gameserver.Config;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DamageList
{
	private final Player pc;
	private final Map<Integer, DamageInfo> damageList;
	private final ReadWriteLock lock;
	private final Lock readLock;
	private final Lock writeLock;

	public DamageList(Player pc)
	{
		damageList = new HashMap<>();
		lock = new ReentrantReadWriteLock();
		readLock = lock.readLock();
		writeLock = lock.writeLock();
		this.pc = pc;
	}

	public void addDamage(Playable playable, int damage)
	{
		damage = Math.max(damage, 0);

		if(damage == 0)
			return;

		if(playable.isConfused())
			return;

		Player attacker = playable.getPlayer();
		writeLock.lock();
		try
		{
			DamageInfo ai = damageList.get(attacker.getObjectId());
			if(ai == null)
				damageList.put(attacker.getObjectId(), ai = new DamageInfo(attacker));

			ai.damage += damage;
			ai.damage = Math.max(ai.damage, 0);
			ai.lastAttack = Instant.now();
		}
		finally
		{
			writeLock.unlock();
		}
	}

	public void clear()
	{
		writeLock.lock();
		try
		{
			if(damageList.isEmpty())
				return;

			damageList.clear();
		}
		finally
		{
			writeLock.unlock();
		}
	}

	public Player getTopDamager(Player defaultDamager)
	{
		readLock.lock();
		DamageInfo[] hated;
		try
		{
			if(damageList.isEmpty())
				return defaultDamager;
			hated = damageList.values().toArray(new DamageInfo[0]);
		}
		finally
		{
			readLock.unlock();
		}
		Arrays.sort(hated, DamageComparator.getInstance());

		if(hated[0].damage == 0)
			return defaultDamager;

		Player topDamager = defaultDamager;

		List<Player> chars = World.getAroundPlayers(pc);

        Instant now = Instant.now();
		single_damage: for(DamageInfo ai : hated)
		{
            if(ai.lastAttack.plus(Config.GVE_GIVE_ASSIST_ATTACK_DELAY).isBefore(now))
				continue;

			if(defaultDamager.getObjectId() == ai.attackerId)
				continue;

			for(Player cha : chars)
			{
				if(cha.isDead())
					continue;

				if(cha.getObjectId() == ai.attackerId)
				{
					topDamager = cha;
					break single_damage;
				}
			}
		}

		return topDamager;
	}

	public Player getAssistant(Player defaultAssistant)
	{

        readLock.lock();
        DamageInfo[] damageInfos;
        try
		{
			if(damageList.isEmpty())
				return defaultAssistant;
			damageInfos = damageList.values().toArray(new DamageInfo[0]);
		}
		finally
		{
			readLock.unlock();
		}

		Arrays.sort(damageInfos, Comparator.comparing(o -> o.lastAttack, Comparator.reverseOrder()));

        Instant now = Instant.now();

		if(damageInfos[0].lastAttack.plus(Config.GVE_GIVE_ASSIST_ATTACK_DELAY).isBefore(now))
			return defaultAssistant;

		Player assistant = defaultAssistant;

		List<Player> chars = World.getAroundPlayers(pc);

		single_damage: for(DamageInfo ai : damageInfos)
		{
            if(ai.lastAttack.plus(Config.GVE_GIVE_ASSIST_ATTACK_DELAY).isBefore(now))
                return defaultAssistant;

			if(defaultAssistant.getObjectId() == ai.attackerId)
				continue;

			for(Player cha : chars)
			{
				if(cha.isDead())
					continue;

				if(cha.getObjectId() == ai.attackerId)
				{
					assistant = cha;
					break single_damage;
				}
			}
		}

		return assistant;
	}

	private abstract static class DamageHate
	{
		public int damage;
		public Instant lastAttack;
	}

	private static class DamageInfo extends DamageHate
	{
		public int attackerId;

		DamageInfo(Creature attacker)
		{
			attackerId = attacker.getObjectId();
		}
	}

	private static class DamageComparator implements Comparator<DamageHate>
	{
		private static final Comparator<DamageHate> instance = new DamageComparator();

		public static Comparator<DamageHate> getInstance()
		{
			return instance;
		}

		@Override
		public int compare(DamageHate o1, DamageHate o2)
		{
			return Integer.compare(o2.damage, o1.damage);
		}
	}

}
