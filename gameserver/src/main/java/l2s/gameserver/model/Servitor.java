package l2s.gameserver.model;

import gnu.trove.iterator.TIntObjectIterator;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.ai.ServitorAI;
import l2s.gameserver.dao.EffectsDAO;
import l2s.gameserver.handler.onshiftaction.OnShiftActionHolder;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.actor.recorder.ServitorStatsChangeRecorder;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.impl.SingleMatchEvent;
import l2s.gameserver.model.instances.PetInstance;
import l2s.gameserver.model.instances.SummonInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.PetInventory;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.*;
import l2s.gameserver.network.l2.s2c.ExMagicAttackInfo.MagicAttackType;
import l2s.gameserver.network.l2.s2c.updatetype.IUpdateTypeComponent;
import l2s.gameserver.network.l2.s2c.updatetype.NpcInfoType;
import l2s.gameserver.skills.AbnormalEffect;
import l2s.gameserver.skills.TimeStamp;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.taskmanager.DecayTaskManager;
import l2s.gameserver.templates.item.WeaponTemplate;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

public abstract class Servitor extends Playable
{
	public static final String TITLE_BY_OWNER_NAME = "%OWNER_NAME%";
	private static final Logger _log;
	private static final int SUMMON_TELEPORT_RANGE = 4096;
	private final Player _owner;
	private int _spawnAnimation;
	protected long _exp;
	protected int _sp;
	private int _maxLoad;
	private boolean _follow;
	private boolean _depressed;
	private UsedSkill _usedSkill;
	private double _chargedSoulshotPower;
	private double _chargedSpiritshotPower;
	private Future<?> _decayTask;
	private int _summonTime;
	private int _index;
	private final int _corpseTime;
	private final boolean _targetable;
	private boolean _showName;
	private ScheduledFuture<?> _broadcastCharInfoTask;
	private Future<?> _petInfoTask;

	public Servitor(int objectId, NpcTemplate template, Player owner)
	{
		super(objectId, template);
		setSpawnAnimation(2);
		_exp = 0L;
		_sp = 0;
		_follow = true;
		_depressed = false;
		_chargedSoulshotPower = 0.0;
		_chargedSpiritshotPower = 0.0;
		_summonTime = 0;
		_index = 0;
		_owner = owner;
		if(!template.getSkills().isEmpty())
		{
			TIntObjectIterator<Skill> iterator = template.getSkills().iterator();
			while(iterator.hasNext())
			{
				iterator.advance();
				addSkill(iterator.value().getEntry());
			}
		}
        setXYZ(owner.getX() + Rnd.get(-100, 100), owner.getY() + Rnd.get(-100, 100), owner.getZ());
		_corpseTime = template.getAIParams().getInteger("corpse_time", 7);
		setTargetable(_targetable = template.getAIParams().getBool("targetable", true));
		setShowName(template.getAIParams().getBool("show_name", true));
	}

	@Override
	protected void onSpawn()
	{
		super.onSpawn();
		setSpawnAnimation(0);
		Player owner = getPlayer();
		Party party = owner.getParty();
		if(party != null)
			party.broadcastToPartyMembers(owner, new ExPartyPetWindowAdd(this));
		getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		EffectsDAO.getInstance().restoreEffects(this);

		transferOwnerBuffs();
		_summonTime = (int) (System.currentTimeMillis() / 1000L);
		_index = owner.getServitorsCount();
		if(owner.isGMInvisible())
			startAbnormalEffect(AbnormalEffect.STEALTH);
	}

	public void setSpawnAnimation(int type)
	{
		_spawnAnimation = type;
	}

	@Override
	public ServitorAI getAI()
	{
		if(_ai == null)
			synchronized (this)
			{
				if(_ai == null)
					_ai = new ServitorAI(this);
			}
		return (ServitorAI) _ai;
	}

	@Override
	public NpcTemplate getTemplate()
	{
		return (NpcTemplate) super.getTemplate();
	}

	@Override
	public boolean isUndead()
	{
		return getTemplate().isUndead();
	}

	public abstract int getServitorType();

	public abstract int getEffectIdentifier();

	public boolean isMountable()
	{
		return false;
	}

	@Override
	public void onAction(Player player, boolean shift)
	{
		Player owner = getPlayer();
		if(!isTargetable(player))
		{
			player.sendActionFailed();
			return;
		}
		if(isFrozen())
		{
			player.sendActionFailed();
			return;
		}
		if(shift && OnShiftActionHolder.getInstance().callShiftAction(player, (Class<Servitor>) getClass(), this, true))
			return;
		if(player.getTarget() != this)
		{
			player.setTarget(this);
			if(player.getTarget() == this)
				player.sendPacket(makeStatusUpdate(9, 10, 11, 12));
			else
				player.sendPacket(ActionFailPacket.STATIC);
		}
		else if(player == owner)
		{
			player.sendPacket(new PetInfoPacket(this).update());

			if(!player.isActionsDisabled())
			{
				if(getDistance(player) > getActingRange())
				{
					if(player.getAI().getIntention() != CtrlIntention.AI_INTENTION_INTERACT)
						player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this, null);
				}
				else
					player.sendPacket(new PetStatusShowPacket(this));
			}

			player.sendPacket(ActionFailPacket.STATIC);
		}
		else if(isAutoAttackable(player))
			player.getAI().Attack(this, false, shift);
		else if(player.getAI().getIntention() != CtrlIntention.AI_INTENTION_FOLLOW)
		{
			if(!shift)
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this, Config.FOLLOW_RANGE);
			else
				player.sendActionFailed();
		}
		else
			player.sendActionFailed();
	}

	public long getExpForThisLevel()
	{
		return Experience.getExpForLevel(getLevel());
	}

	public long getExpForNextLevel()
	{
		return Experience.getExpForLevel(getLevel() + 1);
	}

	@Override
	public int getNpcId()
	{
		return getTemplate().getId();
	}

	public int getDisplayId()
	{
		return getTemplate().displayId;
	}

	public final long getExp()
	{
		return _exp;
	}

	public final void setExp(long exp)
	{
		_exp = exp;
	}

	public final int getSp()
	{
		return _sp;
	}

	public void setSp(int sp)
	{
		_sp = sp;
	}

	@Override
	public int getMaxLoad()
	{
		return _maxLoad;
	}

	public void setMaxLoad(int maxLoad)
	{
		_maxLoad = maxLoad;
	}

	@Override
	public int getBuffLimit()
	{
		Player owner = getPlayer();
		return (int) calcStat(Stats.BUFF_LIMIT, owner.getBuffLimit(), null, null);
	}

	public abstract int getCurrentFed();

	public abstract int getMaxFed();

	@Override
	protected void onDeath(Creature killer)
	{
		super.onDeath(killer);
        broadcastPacket(new NpcInfoState(this));
		startDecay(getCorpseTime() * 1000L);
		Player owner = getPlayer();
		if(killer == null || killer == owner || killer == this || isInZoneBattle() || killer.isInZoneBattle())
			return;
		if(killer.isServitor())
			killer = killer.getPlayer();
		if(killer == null)
			return;
		if(killer.isPlayer())
		{
			if(killer.isMyServitor(getObjectId()))
				return;
			Player pk = (Player) killer;
			if(isInSiegeZone())
				return;
			if(getPvpFlag() == 0 && !getPlayer().atMutualWarWith(pk) && !isPK())
			{
				boolean eventPvPFlag = true;
				List<SingleMatchEvent> matchEvents = getEvents(SingleMatchEvent.class);
				for(SingleMatchEvent matchEvent : matchEvents)
					if(!matchEvent.checkPvPFlag(pk))
					{
						eventPvPFlag = false;
						break;
					}
				if(eventPvPFlag)
				{
					int pkCountMulti = Math.max(pk.getPkKills() / 2, 1);
					pk.decreaseKarma(Config.KARMA_MIN_KARMA_PET * pkCountMulti);
					pk.sendChanges();
				}
			}
		}
	}

	protected void startDecay(long delay)
	{
		stopDecay();
		_decayTask = DecayTaskManager.getInstance().addDecayTask(this, delay);
	}

	protected void stopDecay()
	{
		if(_decayTask != null)
		{
			_decayTask.cancel(false);
			_decayTask = null;
		}
	}

	@Override
	protected void onDecay()
	{
		deleteMe();
	}

	public void endDecayTask()
	{
		stopDecay();
		doDecay();
	}

	@Override
	public void broadcastStatusUpdate()
	{
		if(!needStatusUpdate())
			return;
		Player owner = getPlayer();
		sendStatusUpdate();
        broadcastPacket(makeStatusUpdate(10, 9));
		Party party = owner.getParty();
		if(party != null)
			party.broadcastToPartyMembers(owner, new ExPartyPetWindowUpdate(this));
	}

	public void sendStatusUpdate()
	{
		Player owner = getPlayer();
		owner.sendPacket(new PetStatusUpdatePacket(this));
	}

	@Override
	protected void onDelete()
	{
		Player owner = getPlayer();
		Party party = owner.getParty();
		if(party != null)
			party.broadcastToPartyMembers(owner, new ExPartyPetWindowDelete(this));
		owner.sendPacket(new PetDeletePacket(getObjectId(), getServitorType()));
		owner.deleteServitor(getObjectId());
		if(isSummon())
		{
			List<SummonInstance> summons = owner.getSummons();
			Collections.reverse(summons);
			summons.forEach(summon -> owner.sendPacket(new PetInfoPacket(summon)));
		}
		for(Servitor servitor : owner.getServitors())
			if(_index < servitor.getIndex())
				servitor.setIndex(servitor.getIndex() - 1);
		
		getAI().stopAllTaskAndTimers();
		
		stopDecay();
		super.onDelete();
	}

	public void unSummon(boolean logout)
	{
		storeEffects(!logout);
		deleteMe();
	}

	public void storeEffects(boolean clean)
	{
		Player owner = getPlayer();
		if(owner == null)
			return;
		if(clean || owner.isInOlympiadMode())
			getAbnormalList().stopAllEffects();
		EffectsDAO.getInstance().insert(this);
	}

	public void setFollowMode(boolean state)
	{
		Player owner = getPlayer();
		_follow = state;
		if(_follow)
		{
			if(getAI().getIntention() == CtrlIntention.AI_INTENTION_ACTIVE)
				getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, owner, Config.FOLLOW_RANGE);
		}
		else if(getAI().getIntention() == CtrlIntention.AI_INTENTION_FOLLOW)
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
	}

	public boolean isFollowMode()
	{
		return _follow;
	}

	@Override
	public void updateEffectIconsImpl()
	{
		Player owner = getPlayer();
		PartySpelledPacket ps = new PartySpelledPacket(this, true);
		Party party = owner.getParty();
		if(party != null)
			party.broadCast(ps);
		else
			owner.sendPacket(ps);
		super.updateEffectIconsImpl();
	}

	public int getControlItemObjId()
	{
		return 0;
	}

	@Override
	public PetInventory getInventory()
	{
		return null;
	}

	@Override
	public void doPickupItem(GameObject object)
	{}

	@Override
	public void doRevive()
	{
		super.doRevive();
		setRunning();
		getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		setFollowMode(true);
	}

	@Override
	public ItemInstance getActiveWeaponInstance()
	{
		return null;
	}

	@Override
	public WeaponTemplate getActiveWeaponTemplate()
	{
		return null;
	}

	@Override
	public ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}

	@Override
	public WeaponTemplate getSecondaryWeaponTemplate()
	{
		return null;
	}

	@Override
	public void displayGiveDamageMessage(Creature target, int damage, Servitor servitorTransferedDamage, int transferedDamage, boolean crit, boolean miss, boolean shld, boolean magic)
	{
		super.displayGiveDamageMessage(target, damage, servitorTransferedDamage, transferedDamage, crit, miss, shld, magic);
		if(miss)
		{
			getPlayer().sendPacket(new SystemMessage(SystemMsg.C1S_ATTACK_WENT_ASTRAY).addName(this));
			return;
		}
		if(crit)
		{
			if(magic)
			{
				ExMagicAttackInfo.packet(this, target, MagicAttackType.CRITICAL);
				getPlayer().sendPacket(SystemMsg.MAGIC_CRITICAL_HIT);
			}
			else
				getPlayer().sendPacket(SystemMsg.SUMMONED_MONSTERS_CRITICAL_HIT);
		}
		else if(target.isDamageBlocked(this, null))
		{
			ExMagicAttackInfo.packet(this, target, MagicAttackType.IMMUNE);
			getPlayer().sendPacket(SystemMsg.THE_ATTACK_HAS_BEEN_BLOCKED);
		}

		if(damage > 0)
			getPlayer().sendPacket(new SystemMessagePacket(SystemMsg.C1_HAS_DONE_S3_POINTS_OF_DAMAGE_TO_C2).addName(this).addName(target).addNumber(damage).addHpChange(target.getObjectId(), getObjectId(), -damage));
	}

	@Override
	public void displayReceiveDamageMessage(Creature attacker, int damage)
	{
		if(attacker != this)
			getPlayer().sendPacket(new SystemMessagePacket(SystemMsg.C1_HAS_RECEIVED_S3_DAMAGE_FROM_C2).addName(this).addName(attacker).addNumber(damage).addHpChange(getObjectId(), attacker.getObjectId(), -damage));
	}

	@Override
	public boolean unChargeShots(boolean spirit)
	{
		Player owner = getPlayer();
		if(spirit)
		{
			if(_chargedSpiritshotPower > 0.0)
			{
				_chargedSpiritshotPower = 0.0;
				owner.autoShot();
				return true;
			}
		}
		else if(_chargedSoulshotPower > 0.0)
		{
			_chargedSoulshotPower = 0.0;
			owner.autoShot();
			return true;
		}
		return false;
	}

	@Override
	public double getChargedSoulshotPower()
	{
		if(_chargedSoulshotPower > 0.0)
			return calcStat(Stats.SOULSHOT_POWER, _chargedSoulshotPower);
		return 0.0;
	}

	@Override
	public void setChargedSoulshotPower(double val)
	{
		_chargedSoulshotPower = val;
	}

	@Override
	public double getChargedSpiritshotPower()
	{
		if(_chargedSpiritshotPower > 0.0)
			return calcStat(Stats.SPIRITSHOT_POWER, _chargedSpiritshotPower);
		return 0.0;
	}

	@Override
	public void setChargedSpiritshotPower(double val)
	{
		_chargedSpiritshotPower = val;
	}

	public int getSoulshotConsumeCount()
	{
		return 1;
	}

	public int getSpiritshotConsumeCount()
	{
		return 1;
	}

	public boolean isDepressed()
	{
		return _depressed;
	}

	public void setDepressed(boolean depressed)
	{
		_depressed = depressed;
	}

	public boolean isInRange()
	{
		Player owner = getPlayer();
		return getDistance(owner) < SUMMON_TELEPORT_RANGE;
	}

	public void teleportToOwner()
	{
		Player owner = getPlayer();
		setNonAggroTime(System.currentTimeMillis() + Config.NONAGGRO_TIME_ONTELEPORT);
		setNonPvpTime(System.currentTimeMillis() + Config.NONPVP_TIME_ONTELEPORT);
		setSpawnAnimation(0);
		if(owner.isInOlympiadMode())
			teleToLocation(owner.getLoc(), owner.getReflection());
		else
			teleToLocation(Location.findPointToStay(owner, 50, 150), owner.getReflection());
		if(!isDead() && _follow)
			getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, owner, Config.FOLLOW_RANGE);
	}

	@Override
	public void broadcastCharInfo()
	{
		if(_broadcastCharInfoTask != null)
			return;
		_broadcastCharInfoTask = ThreadPoolManager.getInstance().schedule(new BroadcastCharInfoTask(), Config.BROADCAST_CHAR_INFO_INTERVAL);
	}

	@Override
	public void broadcastCharInfoImpl(IUpdateTypeComponent... components)
	{
        broadcastCharInfoImpl(World.getAroundObservers(this), components);
	}

	public void broadcastCharInfoImpl(Iterable<Player> players, IUpdateTypeComponent... components)
	{
		if(components.length == 0)
		{
			_log.warn(getClass().getSimpleName() + ": Trying broadcast char info without components!", new Exception());
			return;
		}
		Player owner = getPlayer();
		for(Player player : players)
			if(player == owner)
				player.sendPacket(new PetInfoPacket(this).update());
			else
			{
				if(owner.isInvisible(player))
					continue;
				if(isPet())
					player.sendPacket(new NpcInfoPacket.ExPetInfo((PetInstance) this, player).update(components));
				else if(isSummon())
					player.sendPacket(new NpcInfoPacket.SummonInfoPacket((SummonInstance) this, player).update(components));
				else
					player.sendPacket(new NpcInfoPacket(this, player).update(components));
			}
	}

	private void sendPetInfoImpl()
	{
		Player owner = getPlayer();
		owner.sendPacket(new PetInfoPacket(this).update());
	}

	public void sendPetInfo()
	{
        sendPetInfo(false);
	}

	public void sendPetInfo(boolean force)
	{
		if(Config.USER_INFO_INTERVAL == 0L || force)
		{
			if(_petInfoTask != null)
			{
				_petInfoTask.cancel(false);
				_petInfoTask = null;
			}
			sendPetInfoImpl();
			return;
		}
		if(_petInfoTask != null)
			return;
		_petInfoTask = ThreadPoolManager.getInstance().schedule(new PetInfoTask(), Config.USER_INFO_INTERVAL);
	}

	public int getSpawnAnimation()
	{
		return _spawnAnimation;
	}

	@Override
	public void startPvPFlag(Creature target)
	{
		Player owner = getPlayer();
		owner.startPvPFlag(target);
	}

	@Override
	public int getPvpFlag()
	{
		Player owner = getPlayer();
		return owner.getPvpFlag();
	}

	@Override
	public int getKarma()
	{
		Player owner = getPlayer();
		return owner.getKarma();
	}

	@Override
	public TeamType getTeam()
	{
		Player owner = getPlayer();
		return owner.getTeam();
	}

	@Override
	public Player getPlayer()
	{
		return _owner;
	}

	public abstract double getExpPenalty();

	@Override
	public ServitorStatsChangeRecorder getStatsRecorder()
	{
		if(_statsRecorder == null)
			synchronized (this)
			{
				if(_statsRecorder == null)
					_statsRecorder = new ServitorStatsChangeRecorder(this);
			}
		return (ServitorStatsChangeRecorder) _statsRecorder;
	}

	@Override
	public List<L2GameServerPacket> addPacketList(Player forPlayer, Creature dropper)
	{
		List<L2GameServerPacket> list = new ArrayList<>();
		Player owner = getPlayer();
		if(owner == forPlayer)
		{
			list.add(new PetInfoPacket(this));
			list.add(new PartySpelledPacket(this, true));
			if(getNpcState() != 101)
				list.add(new ExChangeNPCState(getObjectId(), getNpcState()));
			if(isPet())
				list.add(new PetItemListPacket((PetInstance) this));
		}
		else
		{
			if(getPlayer().isInvisible(forPlayer))
				return Collections.emptyList();
			Party party = forPlayer.getParty();
			if(getReflection() == ReflectionManager.GIRAN_HARBOR && (owner == null || party == null || party != owner.getParty()))
				return list;
			if(isPet())
				list.add(new NpcInfoPacket.ExPetInfo((PetInstance) this, forPlayer).init());
			else if(isSummon())
				list.add(new NpcInfoPacket.SummonInfoPacket((SummonInstance) this, forPlayer).init());
			else
				list.add(new NpcInfoPacket(this, forPlayer).init());
			if(owner != null && party != null && party == owner.getParty())
				list.add(new PartySpelledPacket(this, true));
		}
		if(isInCombat())
			list.add(new AutoAttackStartPacket(getObjectId()));
		if(owner != forPlayer)
			list.add(new RelationChangedPacket(this, forPlayer));
		if(isInBoat())
			list.add(getBoat().getOnPacket(this, getInBoatPosition()));
		else if(isMoving() || isFollowing())
			list.add(movePacket());
		return list;
	}

	@Override
	public void startAttackStanceTask()
	{
		startAttackStanceTask0();
		Player player = getPlayer();
		if(player != null)
			player.startAttackStanceTask0();
	}

	@Override
	public <E extends Event> E getEvent(Class<E> eventClass)
	{
		Player player = getPlayer();
		if(player != null)
			return player.getEvent(eventClass);
		return super.getEvent(eventClass);
	}

	@Override
	public Set<Event> getEvents()
	{
		Player player = getPlayer();
		if(player != null)
			return player.getEvents();
		return super.getEvents();
	}

	@Override
	public void sendReuseMessage(Skill skill)
	{
		Player player = getPlayer();
		if(player != null && isSkillDisabled(skill))
		{
			TimeStamp sts = getSkillReuse(skill);
			if(sts == null || !sts.hasNotPassed())
				return;
			long timeleft = sts.getReuseCurrent();
			if(!Config.ALT_SHOW_REUSE_MSG && timeleft < 10000L || timeleft < 500L)
				return;
			long hours = timeleft / 3600000L;
			long minutes = (timeleft - hours * 3600000L) / 60000L;
			long seconds = (long) Math.ceil((timeleft - hours * 3600000L - minutes * 60000L) / 1000.0);
			if(hours > 0L)
				player.sendPacket(new SystemMessage(2305).addSkillName(skill.getId(), skill.getDisplayLevel()).addNumber(hours).addNumber(minutes).addNumber(seconds));
			else if(minutes > 0L)
				player.sendPacket(new SystemMessage(2304).addSkillName(skill.getId(), skill.getDisplayLevel()).addNumber(minutes).addNumber(seconds));
			else
				player.sendPacket(new SystemMessage(2303).addSkillName(skill.getId(), skill.getDisplayLevel()).addNumber(seconds));
		}
	}

	@Override
	public boolean startFear()
	{
		final boolean result = super.startFear();
		if (!result)
			getAI().storeIntention();

		return result;
	}

	@Override
	public boolean stopFear()
	{
		final boolean result = super.stopFear();
		if (!result)
			getAI().restoreIntention();

		return result;
	}

	@Override
	public boolean startMoveBlock()
	{
		final boolean result = super.startMoveBlock();
		if (!result)
			getAI().storeIntention();

		return result;
	}

	@Override
	public boolean stopMoveBlock()
	{
		final boolean result = super.stopMoveBlock();
		if (!result)
			getAI().restoreIntention();

		return result;
	}

	@Override
	public boolean startSleeping()
	{
		final boolean result = super.startSleeping();
		if (!result)
			getAI().storeIntention();

		return result;
	}

	@Override
	public boolean stopSleeping()
	{
		final boolean result = super.stopSleeping();
		if (!result)
			getAI().restoreIntention();

		return result;
	}

	@Override
	public boolean startStunning()
	{
		final boolean result = super.startStunning();
		if (!result)
			getAI().storeIntention();

		return result;
	}

	@Override
	public boolean stopStunning()
	{
		final boolean result = super.stopStunning();
		if (!result)
			getAI().restoreIntention();

		return result;
	}

	@Override
	public boolean startParalyzed()
	{
		final boolean result = super.startParalyzed();
		if (!result)
			getAI().storeIntention();

		return result;
	}

	@Override
	public boolean stopParalyzed()
	{
		final boolean result = super.stopParalyzed();
		if (!result)
			getAI().restoreIntention();

		return result;
	}

	@Override
	public boolean isServitor()
	{
		return true;
	}

	public boolean isHungry()
	{
		return false;
	}

	public boolean isNotControlled()
	{
		return false;
	}

	public int getNpcState()
	{
		return 101;
	}

	public void onAttacked(Creature attacker)
	{}

	public void onOwnerGotAttacked(Creature attacker)
	{
		onAttacked(attacker);
	}

	public void onOwnerOfAttacks(Creature target)
	{}

	public void setAttackMode(AttackMode mode)
	{}

	public AttackMode getAttackMode()
	{
		return AttackMode.PASSIVE;
	}

	public void transferOwnerBuffs()
	{
		Collection<Abnormal> effects = getPlayer().getAbnormalList().getEffects();
		for(Abnormal e : effects)
		{
			Skill skill = e.getSkill();
			if(!e.isOffensive() && !skill.isToggle())
			{
				if(skill.isCubicSkill())
					continue;
				if(isSummon() && !skill.applyEffectsOnSummon())
					continue;
				if(isPet() && !skill.applyEffectsOnPet())
					continue;

				Abnormal effect = e.getTemplate().getEffect(e.getEffector(), this, skill);
				if(effect == null)
					continue;
				if(effect.getTemplate().isInstant())
					continue;
				effect.setDuration(e.getDuration());
				effect.setTimeLeft(e.getTimeLeft());
				getAbnormalList().addEffect(effect);
			}
		}
	}

	@Override
	public boolean checkPvP(Creature target, Skill skill)
	{
		if(target.isPlayer() && target.getPlayer().isInDuel())
			return false;

		if(target != this && target.isServitor() && getPlayer().isMyServitor(target.getObjectId()) && (skill == null || skill.isOffensive()))
			return true;

		return super.checkPvP(target, skill);
	}

	public UsedSkill getUsedSkill()
	{
		return _usedSkill;
	}

	public void setUsedSkill(Skill skill, int actionId)
	{
		_usedSkill = new UsedSkill(skill, actionId);
	}

	public void setUsedSkill(UsedSkill usedSkill)
	{
		_usedSkill = usedSkill;
	}

	public void notifyMasterDeath()
	{
		getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		setFollowMode(false);

		if(getNpcId() == 14737 || getNpcId() == 14839)
			doDie(null);
	}

	public void notifyMasterRevival()
	{
		getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		setFollowMode(true);
	}

	public int getSummonTime()
	{
		return _summonTime;
	}

	@Override
	public boolean isSpecialEffect(Skill skill)
	{
		return getPlayer() != null && getPlayer().isSpecialEffect(skill);
	}

	protected int getCorpseTime()
	{
		return _corpseTime;
	}

	public void setIndex(int index)
	{
		_index = index;
	}

	public int getIndex()
	{
		return _index;
	}

	public boolean isShowName()
	{
		return _showName;
	}

	public void setShowName(boolean value)
	{
		_showName = value;
	}

	@Override
	protected L2GameServerPacket changeMovePacket()
	{
		return new NpcInfoState(this);
	}

	@Override
	public boolean isInvisible(GameObject observer)
	{
		Player owner = getPlayer();
		if(owner != null)
		{
			if(owner == observer)
				return false;
			if(observer != null && observer.isPlayer())
			{
				Player observPlayer = (Player) observer;
				if(owner.isInSameParty(observPlayer))
					return false;
			}
			if(owner.isGMInvisible())
				return true;
		}
		return super.isInvisible(observer);
	}

	@Override
	public boolean isTargetable(Creature creature)
	{
		return _targetable && (getPlayer() == creature || super.isTargetable(creature));
	}

	static
	{
		_log = LoggerFactory.getLogger(Servitor.class);
	}

	public static class ServitorComparator implements Comparator<Servitor>
	{
		private static final ServitorComparator _instance;

		public static final ServitorComparator getInstance()
		{
			return _instance;
		}

		@Override
		public int compare(Servitor o1, Servitor o2)
		{
			if(o1 == null)
				return -1;
			if(o2 == null)
				return 1;
			return o1.getSummonTime() - o2.getSummonTime();
		}

		static
		{
			_instance = new ServitorComparator();
		}
	}

	public static class UsedSkill
	{
		private final Skill _skill;
		private final int _actionId;

		public UsedSkill(Skill skill, int actionId)
		{
			_skill = skill;
			_actionId = actionId;
		}

		public Skill getSkill()
		{
			return _skill;
		}

		public int getActionId()
		{
			return _actionId;
		}
	}

	public enum AttackMode
	{
		PASSIVE,
		DEFENCE
    }

	public class BroadcastCharInfoTask implements Runnable
	{
		@Override
		public void run()
		{
            broadcastCharInfoImpl(NpcInfoType.VALUES);
			_broadcastCharInfoTask = null;
		}
	}

	private class PetInfoTask implements Runnable
	{
		@Override
		public void run()
		{
			sendPetInfoImpl();
			_petInfoTask = null;
		}
	}
	@Override
	public Fraction getFraction()
	{
		return getPlayer().getFraction();
	}

	@Override
	public String getTitle() {
		Player player = getPlayer();
		if(player != null && _title.equals(TITLE_BY_OWNER_NAME))
			return String.format("[%s] %s", player.getFraction().toString(), player.getVisibleName(player));
		return super.getTitle();
	}
}
