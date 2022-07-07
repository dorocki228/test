package l2s.gameserver.model.instances;

import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.Phantoms.enums.PhantomType;
import l2s.commons.collections.MultiValueSet;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.data.xml.holder.SoulCrystalHolder;
import l2s.gameserver.model.*;
import l2s.gameserver.model.AggroList.HateInfo;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestEventType;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.model.reward.RewardItem;
import l2s.gameserver.model.reward.RewardList;
import l2s.gameserver.model.reward.RewardType;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExMagicAttackInfo;
import l2s.gameserver.network.l2.s2c.ExMagicAttackInfo.MagicAttackType;
import l2s.gameserver.network.l2.s2c.SocialActionPacket;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.templates.SoulCrystal;
import l2s.gameserver.templates.npc.Faction;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ItemFunctions;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MonsterInstance extends NpcInstance
{
	private static final long serialVersionUID = 1L;
	public static final String UNSOWING = "unsowing";
	private static final int MONSTER_MAINTENANCE_INTERVAL = 1000;
	private boolean _isSeeded;
	private int _seederId;
	private boolean _altSeed;
	private RewardItem _harvestItem;
	private final Lock harvestLock;
	private int overhitAttackerId;
	private double _overhitDamage;
	private boolean _isSpoiled;
	private int spoilerId;
	private List<RewardItem> _sweepItems;
	private boolean _sweeped;
	private final Lock sweepLock;
	private int _isChampion;
	private final boolean _canMove;
	private final boolean _isUnsowing;
	private boolean _blockReward;

	public MonsterInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
		harvestLock = new ReentrantLock();
		sweepLock = new ReentrantLock();
		_blockReward = false;
		_isUnsowing = getParameter("unsowing", true);
		_canMove = getParameter("canMove", true);
	}

	@Override
	public boolean isMovementDisabled()
	{
		return !_canMove || super.isMovementDisabled();
	}

	@Override
	public boolean isLethalImmune()
	{
		return _isChampion > 0 || super.isLethalImmune();
	}

	@Override
	public boolean isFearImmune()
	{
		return _isChampion > 0 || super.isFearImmune();
	}

	@Override
	public boolean isParalyzeImmune()
	{
		return _isChampion > 0 || super.isParalyzeImmune();
	}

	@Override
	public boolean isAttackable(Creature attacker) {
		return isAutoAttackable(attacker);
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		Fraction fraction = attacker.getFraction();
		if (fraction == null) {
			throw new IllegalStateException(attacker + " have no faction.");
		}

		if (!fraction.canAttack(getFraction())) {
			return false;
		}

		if (attacker.getPlayer() != null) {
			return true;
		}

		if (attacker.isDefender()) {
			return true;
		}

		return false;
	}

	public int getChampion()
	{
		return _isChampion;
	}

	public void setChampion()
	{
		if(getReflection().canChampions() && canChampion())
		{
			double random = Rnd.nextDouble();
			if(Config.ALT_CHAMPION_CHANCE2 / 100.0 >= random)
                setChampion(2);
			else if((Config.ALT_CHAMPION_CHANCE1 + Config.ALT_CHAMPION_CHANCE2) / 100.0 >= random)
                setChampion(1);
			else
                setChampion(0);
		}
		else
            setChampion(0);
	}

	public void setChampion(int level)
	{
		if(level == 0)
		{
			removeSkillById(4407);
			_isChampion = 0;
		}
		else
		{
			addSkill(SkillHolder.getInstance().getSkillEntry(4407, level));
			_isChampion = level;
		}
	}

	public boolean canChampion()
	{
		return !isMinion() && getTemplate().rewardExp > 0L && getTemplate().level >= Config.ALT_CHAMPION_MIN_LEVEL && getTemplate().level <= Config.ALT_CHAMPION_TOP_LEVEL;
	}

	@Override
	public TeamType getTeam()
	{
		return getChampion() == 2 ? TeamType.RED : getChampion() == 1 ? TeamType.BLUE : TeamType.NONE;
	}

	@Override
	protected void onDespawn()
	{
		setOverhitDamage(0.0);
		setOverhitAttacker(null);
		clearSweep();
		clearHarvest();
		clearAbsorbers();
		super.onDespawn();
	}

	@Override
	public void spawnMinion(NpcInstance minion)
	{
		if(minion.isMonster())
			if(getChampion() == 2)
				((MonsterInstance) minion).setChampion(1);
			else
				((MonsterInstance) minion).setChampion(0);
		super.spawnMinion(minion);
	}

	@Override
	protected void onDeath(Creature killer)
	{
		if(!_blockReward)
			calculateRewards(killer);
		super.onDeath(killer);
	}

	@Override
	protected void onReduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp, boolean isDot)
	{
		if (attacker!=null&& attacker.isPlayer() && !attacker.isPhantom() && attacker.getPlayer().tScheme_record.isLogging())
		{
			//attacker.getPlayer().tScheme_record.getRoute().setMoveType(TSchemeType.CHANGE_TYPE);
			attacker.getPlayer().tScheme_record.getRoute().setPhantomType(PhantomType.PHANTOM);
			attacker.getPlayer().tScheme_record.getRoute().setLvl(this.getLevel());
			attacker.getPlayer().tScheme_record.stopRecord(false);
		}
		
		if(skill != null && skill.isOverhit())
		{
			double overhitDmg = (getCurrentHp() - damage) * -1.0;
			if(overhitDmg <= 0.0)
			{
				setOverhitDamage(0.0);
				setOverhitAttacker(null);
			}
			else
			{
				setOverhitDamage(overhitDmg);
				setOverhitAttacker(attacker);
			}
		}
		super.onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp, isDot);
	}

	public void calculateRewards(Creature lastAttacker)
	{
		Creature topDamager = getAggroList().getTopDamager(lastAttacker);
		if(lastAttacker == null || !lastAttacker.isPlayable())
			lastAttacker = topDamager;

		if(lastAttacker == null || !lastAttacker.isPlayable())
			return;

		Player killer = lastAttacker.getPlayer();
		if(killer == null)
			return;

		Map<Playable, AggroList.HateInfo> aggroMap = getAggroList().getPlayableMap();

		Set<Quest> quests = getTemplate().getEventQuests(QuestEventType.MOB_KILLED_WITH_QUEST);
		if(quests != null && !quests.isEmpty())
		{
			List<Player> players = null;
			if(isRaid() && Config.ALT_NO_LASTHIT)
			{
				players = new ArrayList<>();
				for(Playable pl : aggroMap.keySet())
					if(!pl.isDead() && (isInRangeZ(pl, Config.ALT_PARTY_DISTRIBUTION_RANGE) || killer.isInRangeZ(pl, Config.ALT_PARTY_DISTRIBUTION_RANGE)) && !players.contains(pl.getPlayer()))
						players.add(pl.getPlayer());
			}
			else if(killer.getParty() != null)
			{
				players = new ArrayList<>(killer.getParty().getMemberCount());
				for(Player pl : killer.getParty().getPartyMembers())
					if(!pl.isDead() && (isInRangeZ(pl, Config.ALT_PARTY_DISTRIBUTION_RANGE) || killer.isInRangeZ(pl, Config.ALT_PARTY_DISTRIBUTION_RANGE)))
						players.add(pl);
			}

			for(Quest quest : quests)
			{
				Player toReward = killer;
				if(quest.getPartyType() != Quest.PARTY_NONE && players != null)
					if(isRaid() || quest.getPartyType() == Quest.PARTY_ALL)
					{
						for(Player pl : players)
						{
							QuestState qs = pl.getQuestState(quest);
							if(qs != null && !qs.isCompleted())
								quest.notifyKill(this, qs);
						}
						toReward = null;
					}
					else
					{
						List<Player> interested = new ArrayList<>(players.size());
						for(Player pl : players)
						{
							QuestState qs = pl.getQuestState(quest);
							if(qs != null && !qs.isCompleted())
								interested.add(pl);
						}

						if(interested.isEmpty())
							continue;

						toReward = interested.get(Rnd.get(interested.size()));

						if(toReward == null)
							toReward = killer;
					}

				if(toReward != null)
				{
					QuestState qs = toReward.getQuestState(quest);
					if(qs == null || qs.isCompleted())
						continue;
					quest.notifyKill(this, qs);
				}
			}
		}

		Map<Player, RewardInfo> rewards = new HashMap<>();

		for(HateInfo info : aggroMap.values())
		{
			if(info.damage <= 1)
				continue;
			Playable attacker = (Playable) info.attacker;
			Player player = attacker.getPlayer();
			RewardInfo reward = rewards.get(player);

			if(reward == null)
				rewards.put(player, new RewardInfo(player, info.damage));
			else
				reward.addDamage(info.damage);
		}

		if(topDamager != null && topDamager.isPlayable())
		{
			for(RewardList rewardList : getTemplate().getRewards())
				rollRewards(rewardList, lastAttacker, topDamager);

			Player player = topDamager.getPlayer();

			if(player != null && isAbsorbed(player))
			{
				int itemId = _absorbersIds.get(topDamager.getObjectId());
				ItemInstance isa = player.getInventory().getItemByItemId(itemId);
				SoulCrystal sa = SoulCrystalHolder.getInstance().getCrystal(itemId);

				if(isa != null && sa != null && Rnd.chance(sa.getChance()))
				{
					if(player.consumeItem(itemId, 1, true))
					{
						player.getInventory().addItem(sa.getNextItemId(), 1);
						ItemFunctions.addItem(player, sa.getNextItemId(), 1);

						player.sendPacket(SystemMsg.THE_SOUL_CRYSTAL_SUCCEEDED_IN_ABSORBING_A_SOUL);
					}
				}
				else
					player.sendPacket(SystemMsg.THE_SOUL_CRYSTAL_WAS_NOT_ABLE_TO_ABSORB_THE_SOUL);
			}
		}

		Player[] attackers = rewards.keySet().toArray(new Player[0]);
		double[] xpsp = new double[2];

		for(Player attacker : attackers)
			if(!attacker.isDead())
			{
				RewardInfo reward = rewards.get(attacker);
				if(reward != null)
				{
					Party party = attacker.getParty();
					int maxHp = getMaxHp();

					xpsp[0] = 0.0;
					xpsp[1] = 0.0;

					if(party == null)
					{
						int damage = Math.min(reward._dmg, maxHp);
						if(damage > 0)
						{
							if(isInRangeZ(attacker, Config.ALT_PARTY_DISTRIBUTION_RANGE))
								xpsp = calculateExpAndSp(attacker.getLevel(), damage);

							xpsp[0] = applyOverhit(killer, xpsp[0]);

							attacker.addExpAndCheckBonus(this, (long) xpsp[0], (long) xpsp[1]);
						}
						rewards.remove(attacker);
					}
					else
					{
						int partyDmg = 0;
						int partylevel = 1;

						List<Player> rewardedMembers = new ArrayList<>();
						List<Player> partyMembers = party.getCommandChannel() == null || !isBoss() ? party.getPartyMembers() : party.getCommandChannel().getMembers();
						for(Player partyMember : partyMembers)
						{
							RewardInfo ai = rewards.remove(partyMember);

							if(!partyMember.isDead())
							{
								if(!isInRangeZ(partyMember, Config.ALT_PARTY_DISTRIBUTION_RANGE))
									continue;

								if(ai != null)
									partyDmg += ai._dmg;

								rewardedMembers.add(partyMember);

								if(partyMember.getLevel() <= partylevel)
									continue;

								partylevel = partyMember.getLevel();
							}
						}

						partyDmg = Math.min(partyDmg, maxHp);

						if(partyDmg > 0)
						{
							xpsp = calculateExpAndSp(partylevel, partyDmg);

							double partyMul = (double) partyDmg / (double) maxHp;

							xpsp[0] *= partyMul;
							xpsp[1] *= partyMul;
							xpsp[0] = applyOverhit(killer, xpsp[0]);

							party.distributeXpAndSp(xpsp[0], xpsp[1], rewardedMembers, lastAttacker, this);
						}
					}
				}
			}
	}

	@Override
	public void onRandomAnimation()
	{
		if(System.currentTimeMillis() - _lastSocialAction > 10000L)
		{
            broadcastPacket(new SocialActionPacket(getObjectId(), 1));
			_lastSocialAction = System.currentTimeMillis();
		}
	}

	@Override
	public void startRandomAnimation()
	{}

	@Override
	public int getKarma()
	{
		return 0;
	}

	public RewardItem takeHarvest()
	{
		harvestLock.lock();
		try
		{
			RewardItem harvest = _harvestItem;
			clearHarvest();
			return harvest;
		}
		finally
		{
			harvestLock.unlock();
		}
	}

	public void clearHarvest()
	{
		harvestLock.lock();
		try
		{
			_harvestItem = null;
			_altSeed = false;
			_seederId = 0;
			_isSeeded = false;
		}
		finally
		{
			harvestLock.unlock();
		}
	}

	public boolean setSeeded(Player player, int seedId, boolean altSeed)
	{
		harvestLock.lock();
		try
		{
			if(isSeeded())
				return false;
			_isSeeded = true;
			_altSeed = altSeed;
			_seederId = player.getObjectId();
			_harvestItem = new RewardItem(Manor.getInstance().getCropType(seedId));
			if(getTemplate().rateHp > 1.0)
				_harvestItem.count = Rnd.get(Math.round(getTemplate().rateHp), Math.round(1.5 * getTemplate().rateHp));
		}
		finally
		{
			harvestLock.unlock();
		}
		return true;
	}

	public boolean isSeeded(Player player)
	{
		return isSeeded() && _seederId == player.getObjectId() && getDeadTime() < 20000L;
	}

	public boolean isSeeded()
	{
		return _isSeeded;
	}

	public boolean isSpoiled()
	{
		return _isSpoiled;
	}

	public boolean isSpoiled(Player player)
	{
		if(!isSpoiled())
			return false;
		if(player.getObjectId() == spoilerId && getDeadTime() < 20000L)
			return true;
		if(player.isInParty())
			for(Player pm : player.getParty().getPartyMembers())
				if(pm.getObjectId() == spoilerId && getDistance(pm) < Config.ALT_PARTY_DISTRIBUTION_RANGE)
					return true;
		return false;
	}

	public boolean setSpoiled(Player player)
	{
		sweepLock.lock();
		try
		{
			if(isSpoiled())
				return false;
			_isSpoiled = true;
			spoilerId = player.getObjectId();
		}
		finally
		{
			sweepLock.unlock();
		}
		return true;
	}

	public boolean isSweepActive()
	{
		sweepLock.lock();
		try
		{
			return _sweepItems != null && !_sweepItems.isEmpty();
		}
		finally
		{
			sweepLock.unlock();
		}
	}

	public boolean takeSweep(Player player)
	{
		sweepLock.lock();
		try
		{
			_sweeped = true;
			if(_sweepItems == null || _sweepItems.isEmpty())
			{
				clearSweep();
				return false;
			}
			for(RewardItem item : _sweepItems)
			{
				ItemInstance sweep = ItemFunctions.createItem(item.itemId);
				sweep.setCount(item.count);
				if(player.isInParty() && player.getParty().isDistributeSpoilLoot())
					player.getParty().distributeItem(player, sweep, null);
				else if(!player.getInventory().validateCapacity(sweep) || !player.getInventory().validateWeight(sweep))
					sweep.dropToTheGround(player, this);
				else
				{
					player.getInventory().addItem(sweep);
					if(item.count == 1L)
					{
						SystemMessagePacket smsg = new SystemMessagePacket(SystemMsg.YOU_HAVE_OBTAINED_S1);
						smsg.addItemName(item.itemId);
						player.sendPacket(smsg);
					}
					else
					{
						SystemMessagePacket smsg = new SystemMessagePacket(SystemMsg.YOU_HAVE_OBTAINED_S2_S1);
						smsg.addItemName(item.itemId);
						smsg.addNumber(item.count);
						player.sendPacket(smsg);
					}
					if(!player.isInParty())
						continue;
					if(item.count == 1L)
					{
						SystemMessagePacket smsg = new SystemMessagePacket(SystemMsg.C1_HAS_OBTAINED_S2_BY_USING_SWEEPER);
						smsg.addName(player);
						smsg.addItemName(item.itemId);
						player.getParty().broadCast(smsg);
					}
					else
					{
						SystemMessagePacket smsg = new SystemMessagePacket(SystemMsg.C1_HAS_OBTAINED_S3_S2_BY_USING_SWEEPER);
						smsg.addName(player);
						smsg.addItemName(item.itemId);
						smsg.addNumber(item.count);
						player.getParty().broadCast(smsg);
					}
				}
			}
			clearSweep();
			return true;
		}
		finally
		{
			sweepLock.unlock();
		}
	}

	public boolean isSweeped()
	{
		return _sweeped;
	}

	public void clearSweep()
	{
		sweepLock.lock();
		try
		{
			_isSpoiled = false;
			spoilerId = 0;
			_sweepItems = null;
		}
		finally
		{
			sweepLock.unlock();
		}
	}

	@Override
	public void rollRewards(RewardList list, Creature lastAttacker, Creature topDamager)
	{
		RewardType type = list.getType();
		if(type == RewardType.SWEEP && !isSpoiled())
			return;
		Creature activeChar = type == RewardType.SWEEP ? lastAttacker : lockDropTo(topDamager);
		Player activePlayer = activeChar.getPlayer();
		if(activePlayer == null)
			return;
		double penaltyMod = Experience.penaltyModifier(calculateLevelDiffForDrop(topDamager.getLevel()));
		List<RewardItem> rewardItems = list.roll(activePlayer, penaltyMod, this);
		switch(type)
		{
			case SWEEP:
			{
				_sweepItems = rewardItems;
				break;
			}
			default:
			{
				for(RewardItem drop : rewardItems)
				{
					if(isSeeded() && !_altSeed && !drop.isAdena() && !drop.isHerb())
						continue;
					if(!Config.DROP_ONLY_THIS.isEmpty() && !Config.DROP_ONLY_THIS.contains(drop.itemId) && (!Config.INCLUDE_RAID_DROP || !isRaid()))
						return;
                    dropItem(activePlayer, drop.itemId, drop.count);
				}
				if(getChampion() > 0 && Config.SPECIAL_ITEM_ID > 0 && Math.abs(getLevel() - activePlayer.getLevel()) < 9 && Rnd.chance(Config.SPECIAL_ITEM_DROP_CHANCE))
				{
					ItemFunctions.addItem(activePlayer, Config.SPECIAL_ITEM_ID, Config.SPECIAL_ITEM_COUNT);
					break;
				}
				break;
			}
		}
	}

	private double[] calculateExpAndSp(int level, long damage)
	{
		int diff = Math.min(Math.max(0, level - getLevel()), Config.MONSTER_LEVEL_DIFF_EXP_PENALTY.length - 1);

		double xp = getExpReward() * damage / getMaxHp();
		double sp = getSpReward() * damage / getMaxHp();

		double mod = (100.0 - Config.MONSTER_LEVEL_DIFF_EXP_PENALTY[diff]) / 100.0;

		xp *= mod;
		sp *= mod;

		xp = Math.max(0.0, xp);
		sp = Math.max(0.0, sp);

		return new double[] { xp, sp };
	}

	private double applyOverhit(Player killer, double xp)
	{
		if(xp > 0.0 && killer.getObjectId() == overhitAttackerId)
		{
			int overHitExp = calculateOverhitExp(xp);
			ExMagicAttackInfo.packet(killer, this, MagicAttackType.OVERHIT);
			killer.sendPacket(SystemMsg.OVERHIT);
			xp += overHitExp;
		}
		return xp;
	}

	@Override
	public void setOverhitAttacker(Creature attacker)
	{
		overhitAttackerId = attacker == null ? 0 : attacker.getObjectId();
	}

	public double getOverhitDamage()
	{
		return _overhitDamage;
	}

	@Override
	public void setOverhitDamage(double damage)
	{
		_overhitDamage = damage;
	}

	public int calculateOverhitExp(double normalExp)
	{
		double overhitPercentage = getOverhitDamage() * 100.0 / getMaxHp();
		if(overhitPercentage > 25.0)
			overhitPercentage = 25.0;
		double overhitExp = overhitPercentage / 100.0 * normalExp;
		setOverhitAttacker(null);
		setOverhitDamage(0.0);
		return (int) Math.round(overhitExp);
	}

	@Override
	public boolean isAggressive()
	{
		return (Config.ALT_CHAMPION_CAN_BE_AGGRO || getChampion() == 0) && super.isAggressive();
	}

	@Override
	public Faction getFaction()
	{
		if(getTemplate().isNoClan())
			return Faction.NONE;
		return Config.ALT_CHAMPION_CAN_BE_SOCIAL || getChampion() == 0 ? super.getFaction() : Faction.NONE;
	}

	@Override
	public boolean isMonster()
	{
		return true;
	}

	@Override
	public Clan getClan()
	{
		return null;
	}

	@Override
	public boolean isPeaceNpc()
	{
		return false;
	}

	public final boolean isUnsowing()
	{
		return _isUnsowing;
	}

	@Override
	public void blockReward()
	{
		_blockReward = true;
	}

	protected static final class RewardInfo
	{
		protected Creature _attacker;
		protected int _dmg;

		public RewardInfo(Creature attacker, int dmg)
		{
			_dmg = 0;
			_attacker = attacker;
			_dmg = dmg;
		}

		public void addDamage(int dmg)
		{
			if(dmg < 0)
				dmg = 0;
			_dmg += dmg;
		}

		@Override
		public int hashCode()
		{
			return _attacker.getObjectId();
		}
	}

	protected Creature lockDropTo(Creature topDamager)
	{
		return topDamager;
	}

	/** The table containing all players objectID that successfully absorbed the soul of this L2NpcInstance */
	private TIntObjectHashMap<Integer> _absorbersIds;
	private final Lock absorbLock = new ReentrantLock();

	public void addAbsorber(Player attacker, int itemId)
	{
		// The attacker must not be null
		if(attacker == null)
			return;

		if(getCurrentHpPercents() > 50)
			return;

		absorbLock.lock();
		try
		{
			if(_absorbersIds == null)
				_absorbersIds = new TIntObjectHashMap<>();

			_absorbersIds.put(attacker.getObjectId(), itemId);
		}
		finally
		{
			absorbLock.unlock();
		}
	}

	public boolean isAbsorbed(Player player)
	{
		absorbLock.lock();
		try
		{
			if(_absorbersIds == null)
				return false;
			if(!_absorbersIds.contains(player.getObjectId()))
				return false;
		}
		finally
		{
			absorbLock.unlock();
		}
		return true;
	}

	public void clearAbsorbers()
	{
		absorbLock.lock();
		try
		{
			if(_absorbersIds != null)
				_absorbersIds.clear();
		}
		finally
		{
			absorbLock.unlock();
		}
	}
}
