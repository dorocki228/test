package l2s.gameserver.model;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AggroList
{
	private final NpcInstance npc;
	private final TIntObjectHashMap<AggroInfo> hateList;
	private final Map<Party, PartyDamage> partyDamageMap;
	private final Map<CommandChannel, CommandChannelDamage> ccDamageMap;
	private final ReadWriteLock lock;
	private final Lock readLock;
	private final Lock writeLock;

	public AggroList(NpcInstance npc)
	{
		hateList = new TIntObjectHashMap<>();
		partyDamageMap = new HashMap<>();
		ccDamageMap = new HashMap<>();
		lock = new ReentrantReadWriteLock();
		readLock = lock.readLock();
		writeLock = lock.writeLock();
		this.npc = npc;
	}

	public void addDamageHate(Creature attacker, int damage, int aggro)
	{
		damage = Math.max(damage, 0);

		if(damage == 0 && aggro == 0)
			return;

		if(attacker.isConfused())
			return;

		writeLock.lock();
		try
		{
			AggroInfo ai;
			if((ai = hateList.get(attacker.getObjectId())) == null)
				hateList.put(attacker.getObjectId(), ai = new AggroInfo(attacker));

			if(attacker.getPlayer() != null)
			{
				Party party = attacker.getPlayer().getParty();

				if(party != null)
				{
					PartyDamage pd = partyDamageMap.get(party);

					if(pd == null)
					{
						pd = new PartyDamage(party);
						partyDamageMap.put(party, pd);
					}

					pd.damage += damage;

					if(party.isInCommandChannel())
					{
						CommandChannel cc = party.getCommandChannel();

						CommandChannelDamage ccd = ccDamageMap.get(cc);

						if(ccd == null)
						{
							ccd = new CommandChannelDamage(cc);
							ccDamageMap.put(cc, ccd);
						}
						ccd.damage += damage;
					}
				}
			}

			ai.damage += damage;
			ai.hate += aggro;
			ai.damage = Math.max(ai.damage, 0);
			ai.hate = Math.max(ai.hate, 0);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	public void reduceHate(Creature target, int hate)
	{
		writeLock.lock();
		try
		{
			AggroInfo ai = hateList.get(target.getObjectId());
			if(ai != null)
			{
				ai.hate -= hate;
				ai.hate = Math.max(ai.hate, 0);
			}
		}
		finally
		{
			writeLock.unlock();
		}
	}

	public int getHate(Creature target)
	{
        writeLock.lock();
        int hate = 0;
        try
		{
			AggroInfo ai = hateList.get(target.getObjectId());
			if(ai != null)
				hate = ai.hate;
		}
		finally
		{
			writeLock.unlock();
		}
		return hate;
	}

	public AggroInfo get(Creature attacker)
	{
		readLock.lock();
		try
		{
			return hateList.get(attacker.getObjectId());
		}
		finally
		{
			readLock.unlock();
		}
	}

	private void remove(int objectId, boolean onlyHate)
	{
		writeLock.lock();
		try
		{
			if(!onlyHate)
			{
				hateList.remove(objectId);
				return;
			}
			AggroInfo ai = hateList.get(objectId);
			if(ai != null)
				if(ai.damage == 0)
					hateList.remove(objectId);
				else
					ai.hate = 0;
		}
		finally
		{
			writeLock.unlock();
		}
	}

	public void remove(Creature attacker, boolean onlyHate)
	{
        remove(attacker.getObjectId(), onlyHate);
	}

	public void clear()
	{
        clear(false);
	}

	public void clear(boolean onlyHate)
	{
		writeLock.lock();
		try
		{
			if(hateList.isEmpty())
				return;
			if(!onlyHate)
			{
				hateList.clear();
				return;
			}
			TIntObjectIterator<AggroInfo> itr = hateList.iterator();
			while(itr.hasNext())
			{
				itr.advance();
				AggroInfo ai = itr.value();
				ai.hate = 0;
				if(ai.damage == 0)
					itr.remove();
			}
		}
		finally
		{
			writeLock.unlock();
		}
	}

	public boolean isEmpty()
	{
		readLock.lock();
		try
		{
			return hateList.isEmpty();
		}
		finally
		{
			readLock.unlock();
		}
	}

	private Creature getOrRemoveHated(int objectId)
	{
		GameObject object = GameObjectsStorage.findObject(objectId);
		if(object == null || !object.isCreature())
		{
            remove(objectId, true);
			return null;
		}
		Creature cha = (Creature) object;
		if(cha.isPlayable() && ((Playable) cha).isInNonAggroTime())
		{
            remove(objectId, true);
			return null;
		}
		if(cha.isPlayer() && !((Player) cha).isOnline())
		{
            remove(objectId, true);
			return null;
		}
		return cha;
	}

	public List<Creature> getHateList(int radius)
	{
		readLock.lock();
		AggroInfo[] hated;
		try
		{
			if(hateList.isEmpty())
				return Collections.emptyList();
			hated = hateList.values(new AggroInfo[hateList.size()]);
		}
		finally
		{
			readLock.unlock();
		}
		Arrays.sort(hated, HateComparator.getInstance());
		if(hated[0].hate == 0)
			return Collections.emptyList();
		List<Creature> hateList = new ArrayList<>();
		for(int i = 0; i < hated.length; ++i)
		{
			AggroInfo ai = hated[i];
			if(ai.hate != 0)
			{
				Creature cha = getOrRemoveHated(ai.attackerId);
				if(cha != null)
					if(radius == -1 || cha.isInRangeZ(npc.getLoc(), radius))
					{
						hateList.add(cha);
						break;
					}
			}
		}
		return hateList;
	}

	public Creature getMostHated(int radius)
	{
		readLock.lock();
		AggroInfo[] hated;
		try
		{
			if(hateList.isEmpty())
				return null;
			hated = hateList.values(new AggroInfo[hateList.size()]);
		}
		finally
		{
			readLock.unlock();
		}
		Arrays.sort(hated, HateComparator.getInstance());
		if(hated[0].hate == 0)
			return null;
		for(int i = 0; i < hated.length; ++i)
		{
			AggroInfo ai = hated[i];
			if(ai.hate != 0)
			{
				Creature cha = getOrRemoveHated(ai.attackerId);
				if(cha != null)
					if(radius == -1 || cha.isInRangeZ(npc.getLoc(), radius))
						if(!cha.isDead())
							return cha;
			}
		}
		return null;
	}

	public Creature getRandomHated(int radius)
	{
		readLock.lock();
		AggroInfo[] hated;
		try
		{
			if(hateList.isEmpty())
				return null;
			hated = hateList.values(new AggroInfo[hateList.size()]);
		}
		finally
		{
			readLock.unlock();
		}
		Arrays.sort(hated, HateComparator.getInstance());
		if(hated[0].hate == 0)
			return null;
		List<Creature> randomHated = new ArrayList<>();
		for(int i = 0; i < hated.length; ++i)
		{
			AggroInfo ai = hated[i];
			if(ai.hate != 0)
			{
				Creature cha = getOrRemoveHated(ai.attackerId);
				if(cha != null)
					if(radius == -1 || cha.isInRangeZ(npc.getLoc(), radius))
						if(!cha.isDead())
						{
							randomHated.add(cha);
							break;
						}
			}
		}

		return randomHated.isEmpty() ? null : randomHated.get(Rnd.get(randomHated.size()));
	}

	public Creature getTopDamager(Creature defaultDamager)
	{
		readLock.lock();
		AggroInfo[] hated;
		try
		{
			if(hateList.isEmpty())
				return defaultDamager;
			hated = hateList.values(new AggroInfo[hateList.size()]);
		}
		finally
		{
			readLock.unlock();
		}
		Arrays.sort(hated, DamageComparator.getInstance());
		if(hated[0].damage == 0)
			return defaultDamager;

		Creature topDamager = defaultDamager;

		int topDamage = 0;

		List<Creature> chars = World.getAroundCharacters(npc);

		single_damage: for(AggroInfo ai : hated)
			for(Creature cha : chars)
				if(cha.getObjectId() == ai.attackerId)
				{
					topDamager = cha;
					topDamage = ai.damage;
					break single_damage;
				}

		readLock.lock();
		PartyDamage[] partyDmg;
		try
		{
			partyDmg = partyDamageMap.values().toArray(new PartyDamage[0]);
		}
		finally
		{
			readLock.unlock();
		}

		Arrays.sort(partyDmg, DamageComparator.getInstance());

		party_damage: for(PartyDamage pd : partyDmg)
			if(pd.damage > topDamage)
				for(AggroInfo ai : hated)
					for(Player player : pd.party.getPartyMembers())
						if(player.getObjectId() == ai.attackerId && chars.contains(player))
						{
							topDamager = player;
							topDamage = pd.damage;
							break party_damage;
						}

		readLock.lock();
		CommandChannelDamage[] ccDmg;
		try
		{
			if(ccDamageMap.isEmpty())
				return topDamager;
			ccDmg = ccDamageMap.values().toArray(new CommandChannelDamage[0]);
		}
		finally
		{
			readLock.unlock();
		}

		Arrays.sort(ccDmg, DamageComparator.getInstance());

		cc_damage: for(CommandChannelDamage ccd : ccDmg)
			if(ccd.damage > topDamage)
				for(AggroInfo ai : hated)
					for(Player player : ccd.channel.getMembers())
						if(player.getObjectId() == ai.attackerId && chars.contains(player))
						{
							topDamager = player;
							topDamage = ccd.damage;
							break cc_damage;
						}

		return topDamager;
	}

	public Map<Creature, HateInfo> getCharMap()
	{
		if(isEmpty())
			return Collections.emptyMap();
        List<Creature> chars = World.getAroundCharacters(npc);
		readLock.lock();
        Map<Creature, HateInfo> aggroMap = new HashMap<>();
        try
		{
			TIntObjectIterator<AggroInfo> itr = hateList.iterator();
			while(itr.hasNext())
			{
				itr.advance();
				AggroInfo ai = itr.value();
				if(ai.damage == 0 && ai.hate == 0)
					continue;
				for(Creature attacker : chars)
					if(attacker.getObjectId() == ai.attackerId)
					{
						aggroMap.put(attacker, new HateInfo(attacker, ai));
						break;
					}
			}
		}
		finally
		{
			readLock.unlock();
		}
		return aggroMap;
	}

	public Map<Playable, HateInfo> getPlayableMap()
	{
		if(isEmpty())
			return Collections.emptyMap();
        List<Playable> chars = World.getAroundPlayables(npc);
		readLock.lock();
        Map<Playable, HateInfo> aggroMap = new HashMap<>();
        try
		{
			TIntObjectIterator<AggroInfo> itr = hateList.iterator();
			while(itr.hasNext())
			{
				itr.advance();
				AggroInfo ai = itr.value();
				if(ai.damage == 0 && ai.hate == 0)
					continue;
				for(Playable attacker : chars)
					if(attacker.getObjectId() == ai.attackerId)
					{
						aggroMap.put(attacker, new HateInfo(attacker, ai));
						break;
					}
			}
		}
		finally
		{
			readLock.unlock();
		}
		return aggroMap;
	}

	public Collection<AggroInfo> getAggroInfos()
	{
		readLock.lock();
		Collection<AggroInfo> infos;
		try
		{
			infos = hateList.valueCollection();
		}
		finally
		{
			readLock.unlock();
		}
		return infos;
	}

	public Collection<PartyDamage> getPartyDamages()
	{
		readLock.lock();
		Collection<PartyDamage> damages;
		try
		{
			damages = partyDamageMap.values();
		}
		finally
		{
			readLock.unlock();
		}
		return damages;
	}

	public void copy(AggroList aggroList)
	{
		writeLock.lock();
		try
		{
			Collection<AggroInfo> aggroInfos = aggroList.getAggroInfos();
			for(AggroInfo aggroInfo : aggroInfos)
				hateList.put(aggroInfo.attackerId, aggroInfo);
			Collection<PartyDamage> partyDamages = aggroList.getPartyDamages();
			for(PartyDamage partyDamage : partyDamages)
				partyDamageMap.put(partyDamage.party, partyDamage);
			Collection<CommandChannelDamage> ccDamages = aggroList.getCommandChannelDamages();
			for(CommandChannelDamage ccDamage : ccDamages)
				ccDamageMap.put(ccDamage.channel, ccDamage);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	private Collection<CommandChannelDamage> getCommandChannelDamages()
	{
		readLock.lock();
		Collection<CommandChannelDamage> damages;
		try
		{
			damages = ccDamageMap.values();
		}
		finally
		{
			readLock.unlock();
		}
		return damages;
	}

	private abstract static class DamageHate
	{
		public int hate;
		public int damage;
	}

	public static class HateInfo extends DamageHate
	{
		public Creature attacker;

		HateInfo(Creature attacker, AggroInfo ai)
		{
			this.attacker = attacker;
			hate = ai.hate;
			damage = ai.damage;
		}
	}

	public static class AggroInfo extends DamageHate
	{
		public int attackerId;

		AggroInfo(Creature attacker)
		{
			attackerId = attacker.getObjectId();
		}
	}

	public static class PartyDamage extends DamageHate
	{
		public Party party;

		PartyDamage(Party party)
		{
			this.party = party;
		}
	}

	public static class CommandChannelDamage extends DamageHate
	{
		public CommandChannel channel;

		CommandChannelDamage(CommandChannel channel)
		{
			this.channel = channel;
		}
	}

	public static class DamageComparator implements Comparator<DamageHate>
	{
		private static final Comparator<DamageHate> instance;

		public static Comparator<DamageHate> getInstance()
		{
			return instance;
		}

		@Override
		public int compare(DamageHate o1, DamageHate o2)
		{
			return Integer.compare(o2.damage, o1.damage);
		}

		static
		{
			instance = new DamageComparator();
		}
	}

	public static class HateComparator implements Comparator<DamageHate>
	{
		private static final Comparator<DamageHate> instance;

		public static Comparator<DamageHate> getInstance()
		{
			return instance;
		}

		@Override
		public int compare(DamageHate o1, DamageHate o2)
		{
			int result = Integer.compare(o2.hate, o1.hate);
			if(result != 0)
				return result;
			return Integer.compare(o2.damage, o1.damage);
		}

		static
		{
			instance = new HateComparator();
		}
	}
}
