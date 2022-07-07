package l2s.gameserver.model.instances;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import l2s.commons.collections.MultiValueSet;
import l2s.commons.lang.reference.HardReference;
import l2s.commons.lang.reference.HardReferences;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CharacterAI;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.data.QuestHolder;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.data.xml.holder.MultiSellHolder;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.data.xml.holder.SkillAcquireHolder;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.handler.bypass.BypassHolder;
import l2s.gameserver.handler.onshiftaction.OnShiftActionHolder;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.instancemanager.EventTriggersManager;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.listener.NpcListener;
import l2s.gameserver.model.*;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.actor.listener.NpcListenerList;
import l2s.gameserver.model.actor.recorder.NpcStatsChangeRecorder;
import l2s.gameserver.model.base.AcquireType;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.base.MountType;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.entity.residence.ClanHall;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.PcInventory;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.SubUnit;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestEventType;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.model.reward.RewardItem;
import l2s.gameserver.model.reward.RewardList;
import l2s.gameserver.network.l2.c2s.L2GameClientPacket;
import l2s.gameserver.network.l2.c2s.RequestItemEnsoul;
import l2s.gameserver.network.l2.c2s.RequestTryEnSoulExtraction;
import l2s.gameserver.network.l2.c2s.augmentation.RequestRefine;
import l2s.gameserver.network.l2.c2s.augmentation.RequestRefineCancel;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.*;
import l2s.gameserver.network.l2.s2c.augmentation.ExShowVariationCancelWindow;
import l2s.gameserver.network.l2.s2c.augmentation.ExShowVariationMakeWindow;
import l2s.gameserver.network.l2.s2c.updatetype.IUpdateTypeComponent;
import l2s.gameserver.network.l2.s2c.updatetype.NpcInfoType;
import l2s.gameserver.skills.EffectType;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.tables.ClanTable;
import l2s.gameserver.taskmanager.DecayTaskManager;
import l2s.gameserver.taskmanager.LazyPrecisionTaskManager;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.TeleportLocation;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.item.WeaponTemplate;
import l2s.gameserver.templates.npc.BuyListTemplate;
import l2s.gameserver.templates.npc.Faction;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.templates.spawn.SpawnRange;
import l2s.gameserver.utils.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

public class NpcInstance extends Creature
{
	public static final int BASE_CORPSE_TIME = 7;
	public static final String CORPSE_TIME = "corpse_time";
	public static final String NO_CHAT_WINDOW = "noChatWindow";
	public static final String NO_RANDOM_WALK = "noRandomWalk";
	public static final String NO_RANDOM_ANIMATION = "noRandomAnimation";
	public static final String NO_SHIFT_CLICK = "noShiftClick";
	public static final String NO_LETHAL = "noLethal";
	public static final String TARGETABLE = "targetable";
	public static final String SHOW_NAME = "show_name";
	public static final String NO_SLEEP_MODE = "no_sleep_mode";
	public static final String IS_IMMORTAL = "is_immortal";
	public static final String EVENT_TRIGGER_ID = "event_trigger_id";
	private static final Logger _log;
	private int _personalAggroRange;
	private int _level;
	private long _dieTime;
	protected int _spawnAnimation;
	private int _currentLHandId;
	private int _currentRHandId;
	private double _collisionHeightModifier;
	private double _collisionRadiusModifier;
	private int npcState;
	protected boolean _hasRandomAnimation;
	protected boolean _hasRandomWalk;
	protected boolean _hasChatWindow;
	private Future<?> _decayTask;
	private Future<?> _animationTask;
	private final AggroList _aggroList;
	private final boolean _noLethal;
	private boolean _showName;
	private final boolean _noShiftClick;
	private Castle _nearestCastle;
	private ClanHall _nearestClanHall;
	private NpcString _nameNpcString;
	private NpcString _titleNpcString;
	private Spawner _spawn;
	private Location _spawnedLoc;
	private SpawnRange _spawnRange;
	private NpcInstance _master;
	private MinionList _minionList;
	private MultiValueSet<String> _parameters;
	private final int _enchantEffect;
	private final boolean _isNoSleepMode;
	private final int _corpseTime;
	private final boolean _isImmortal;
	private HardReference<Player> _ownerRef;
	private final TIntSet _eventTriggers;
	protected boolean _unAggred;
	private int _displayId;
	private ScheduledFuture<?> _broadcastCharInfoTask;
	protected long _lastSocialAction;
	private boolean _isBusy;
	private String _busyMessage;
	private boolean _isUnderground;
	private final boolean _unspawnMinions;

	public NpcInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template);
		_personalAggroRange = -1;
		_level = 0;
		_dieTime = 0L;
		_spawnAnimation = 2;
		_collisionHeightModifier = 1.0;
		_collisionRadiusModifier = 1.0;
		npcState = 0;
		_nameNpcString = NpcString.NONE;
		_titleNpcString = NpcString.NONE;
		_spawnedLoc = new Location();
		_master = null;
		_minionList = null;
		_parameters = StatsSet.EMPTY;
		_ownerRef = HardReferences.emptyRef();
		_eventTriggers = new TIntHashSet();
		_unAggred = false;
		_displayId = 0;
		_busyMessage = "";
		_isUnderground = false;
		if(template == null)
			throw new NullPointerException("No template for Npc. Please check your datapack is setup correctly.");
		setParameters(template.getAIParams());
		setParameters(set);
		_hasRandomAnimation = !getParameter("noRandomAnimation", false) && Config.MAX_NPC_ANIMATION > 0;
		_hasRandomWalk = !getParameter("noRandomWalk", false);
		_noShiftClick = getParameter("noShiftClick", isPeaceNpc());
		_noLethal = getParameter("noLethal", false);
		_unspawnMinions = getParameter("unspawnMinions", true);
		setHasChatWindow(!getParameter("noChatWindow", false));
		setTargetable(getParameter("targetable", true));
		setShowName(getParameter("show_name", true));
		_isImmortal = getParameter("is_immortal", false);
		for(Skill skill : template.getSkills().valueCollection())
			addSkill(skill.getEntry());
		setName(template.name);
		String customTitle = template.title;
		if(isMonster() && Config.ALT_SHOW_MONSTERS_LVL)
		{
			customTitle = "LvL: " + getLevel();
			if(Config.ALT_SHOW_MONSTERS_AGRESSION && isAggressive())
				customTitle += " A";
		}
		setTitle(customTitle);
		setLHandId(getTemplate().lhand);
		setRHandId(getTemplate().rhand);
		_aggroList = new AggroList(this);
		setFlying(getParameter("isFlying", false));
		int enchant = Math.min(127, getTemplate().getEnchantEffect());
		if(enchant == 0 && Config.NPC_RANDOM_ENCHANT)
			enchant = Rnd.get(0, 18);
		_enchantEffect = enchant;
		_isNoSleepMode = getParameter("no_sleep_mode", false);
		_corpseTime = getParameter("corpse_time", 7);
		if(!template.getMinionData().isEmpty())
			getMinionList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public HardReference<NpcInstance> getRef()
	{
		return (HardReference<NpcInstance>) super.getRef();
	}

	@Override
	public CharacterAI getAI()
	{
		if(_ai == null)
			synchronized (this)
			{
				if(_ai == null)
					_ai = getTemplate().getNewAI(this);
			}
		return _ai;
	}

	public Location getSpawnedLoc()
	{
		return getLeader() != null ? getLeader().getLoc() : _spawnedLoc;
	}

	public void setSpawnedLoc(Location loc)
	{
		_spawnedLoc = loc;
	}

	public int getRightHandItem()
	{
		return _currentRHandId;
	}

	public int getLeftHandItem()
	{
		return _currentLHandId;
	}

	public void setLHandId(int newWeaponId)
	{
		_currentLHandId = newWeaponId;
	}

	public void setRHandId(int newWeaponId)
	{
		_currentRHandId = newWeaponId;
	}

	@Override
	protected void onReduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp, boolean isDot)
	{
		Creature damager = attacker;
		if(attacker.isConfused())
			for(Abnormal e : attacker.getAbnormalList().getEffects())
				if(e.getEffectType() == EffectType.Discord)
				{
					damager = e.getEffector();
					break;
				}
		if(attacker.isPlayable())
			getAggroList().addDamageHate(damager, (int) damage, 0);
		super.onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp, isDot);
	}

	@Override
	protected void onDeath(Creature killer)
	{
		_dieTime = System.currentTimeMillis();
		if(isMonster() && (((MonsterInstance) this).isSeeded() || ((MonsterInstance) this).isSpoiled()))
			startDecay(20000L);
		else
			startDecay(getCorpseTime());
		setLHandId(getTemplate().lhand);
		setRHandId(getTemplate().rhand);
		getAI().stopAITask();
		stopAttackStanceTask();
		stopRandomAnimation();
		if(getLeader() != null)
			getLeader().notifyMinionDied(this);

		Creature topdam = getAggroList().getTopDamager(killer);
		if(!isMonster())
			for(RewardList list : getTemplate().getRewards())
				rollRewards(list, killer, topdam);

		if(killer.isPlayable() && getRewardCrp() > 0)
		{
			Player pc = killer.getPlayer();
			if(pc.isInClan())
				pc.getClan().incReputation(getRewardCrp(), false, "NpcDie:" + getNpcId());
		}
		super.onDeath(killer);
        broadcastPacket(new NpcInfoState(this));
	}

	public void rollRewards(RewardList list, Creature lastAttacker, Creature topDamager)
	{
		Player activePlayer = topDamager.getPlayer();
		if(activePlayer == null)
			return;

		double penaltyMod = Experience.penaltyModifier(calculateLevelDiffForDrop(topDamager.getLevel()));

		List<RewardItem> rewardItems = list.roll(activePlayer, penaltyMod, this);

		for(RewardItem drop : rewardItems)
			dropItem(activePlayer, drop.itemId, drop.count);
	}

	public long getDeadTime()
	{
		if(_dieTime <= 0L)
			return 0L;
		return System.currentTimeMillis() - _dieTime;
	}

	public AggroList getAggroList()
	{
		return _aggroList;
	}

	public void setLeader(NpcInstance leader)
	{
		_master = leader;
	}

	public NpcInstance getLeader()
	{
		return _master;
	}

	@Override
	public boolean isMinion()
	{
		return getLeader() != null;
	}

	public MinionList getMinionList()
	{
		if(_minionList == null)
			_minionList = new MinionList(this);
		return _minionList;
	}

	public boolean hasMinions()
	{
		return _minionList != null && _minionList.hasMinions();
	}

	public void notifyMinionDied(NpcInstance minion)
	{}

	public Location getRndMinionPosition()
	{
		return Location.findPointToStay(this, (int) (getColRadius() * 5.0), (int) (getColRadius() * 5.0));
	}

	public void spawnMinion(NpcInstance minion)
	{
		minion.setReflection(getReflection());
		minion.setHeading(getHeading());
		minion.setCurrentHpMp(minion.getMaxHp(), minion.getMaxMp(), true);
		minion.spawnMe(getRndMinionPosition());
		if(isRunning())
			minion.setRunning();
	}

	@Override
	public void setReflection(Reflection reflection)
	{
		super.setReflection(reflection);
		if(hasMinions())
			for(NpcInstance m : getMinionList().getAliveMinions())
				m.setReflection(reflection);
	}

	public void dropItem(Player lastAttacker, int itemId, long itemCount)
	{
		if(itemCount == 0L || lastAttacker == null)
			return;
		for(long i = 0L; i < itemCount; ++i)
		{
			ItemInstance item = ItemFunctions.createItem(itemId);
			for(Event e : getEvents())
				item.addEvent(e);
			if(item.isStackable())
			{
				i = itemCount;
				item.setCount(itemCount);
			}
			if(isRaid() || this instanceof ReflectionBossInstance)
			{
				SystemMessagePacket sm;
				if(itemId == 57)
				{
					sm = new SystemMessagePacket(SystemMsg.C1_HAS_DIED_AND_DROPPED_S2_ADENA);
					sm.addName(this);
					sm.addNumber(item.getCount());
				}
				else
				{
					sm = new SystemMessagePacket(SystemMsg.C1_DIED_AND_DROPPED_S3_S2);
					sm.addName(this);
					sm.addItemName(itemId);
					sm.addNumber(item.getCount());
				}
                broadcastPacket(sm);
			}
			lastAttacker.doAutoLootOrDrop(item, this);
		}
	}

	public void dropItem(Player lastAttacker, ItemInstance item)
	{
		if(item.getCount() == 0L)
			return;
		if(isRaid() || this instanceof ReflectionBossInstance)
		{
			SystemMessagePacket sm;
			if(item.getItemId() == 57)
			{
				sm = new SystemMessagePacket(SystemMsg.C1_HAS_DIED_AND_DROPPED_S2_ADENA);
				sm.addName(this);
				sm.addNumber(item.getCount());
			}
			else
			{
				sm = new SystemMessagePacket(SystemMsg.C1_DIED_AND_DROPPED_S3_S2);
				sm.addName(this);
				sm.addItemName(item.getItemId());
				sm.addNumber(item.getCount());
			}
            broadcastPacket(sm);
		}
		lastAttacker.doAutoLootOrDrop(item, this);
	}

	public void dropItemToTheGround(Collection<Player> dropPlayers, int itemId, long itemCount)
	{
		if(itemCount == 0L || dropPlayers == null)
			return;

		for(long i = 0L; i < itemCount; ++i)
		{
			ItemInstance item = ItemFunctions.createItem(itemId);
			for(Event e : getEvents())
				item.addEvent(e);
			if(item.isStackable())
			{
				i = itemCount;
				item.setCount(itemCount);
			}
			if(isRaid() || this instanceof ReflectionBossInstance)
			{
				SystemMessagePacket sm;
				if(itemId == 57)
				{
					sm = new SystemMessagePacket(SystemMsg.C1_HAS_DIED_AND_DROPPED_S2_ADENA);
					sm.addName(this);
					sm.addNumber(item.getCount());
				}
				else
				{
					sm = new SystemMessagePacket(SystemMsg.C1_DIED_AND_DROPPED_S3_S2);
					sm.addName(this);
					sm.addItemName(itemId);
					sm.addNumber(item.getCount());
				}
				broadcastPacket(sm);
			}

			if(dropOnTheGround())
			{
				item.dropToTheGround(dropPlayers, this);
				return;
			}
		}
	}

	@Override
	public boolean isAttackable(Creature attacker)
	{
		return true;
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return false;
	}

	@Override
	protected void onSpawn()
	{
		super.onSpawn();
        setCurrentHpMp(getMaxHp(), getMaxMp(), true);
		_dieTime = 0L;
		_spawnAnimation = 0;
		getAI().notifyEvent(CtrlEvent.EVT_SPAWN);
		getListeners().onSpawn();
		if(getAI().isGlobalAI() || getCurrentRegion() != null && getCurrentRegion().isActive())
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			getAI().startAITask();
			startRandomAnimation();
		}
		if(hasMinions())
			ThreadPoolManager.getInstance().schedule(getMinionList(), 1500L);
		int eventTriggerId = getParameter("event_trigger_id", 0);
		if(eventTriggerId != 0)
			_eventTriggers.add(eventTriggerId);
		for(int triggerId : _eventTriggers.toArray())
			if(getReflection().isMain())
				EventTriggersManager.getInstance().addTrigger(MapUtils.regionX(getX()), MapUtils.regionY(getY()), triggerId);
			else
				EventTriggersManager.getInstance().addTrigger(getReflection(), triggerId);
	}

	@Override
	protected void onDespawn()
	{
		getAggroList().clear();
		stopRandomAnimation();
		getAI().stopAITask();
		getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		getAI().notifyEvent(CtrlEvent.EVT_DESPAWN);
		super.onDespawn();
		for(int triggerId : _eventTriggers.toArray())
			if(getReflection().isMain())
				EventTriggersManager.getInstance().removeTrigger(MapUtils.regionX(getX()), MapUtils.regionY(getY()), triggerId);
			else
				EventTriggersManager.getInstance().removeTrigger(getReflection(), triggerId);
		_eventTriggers.clear();
	}

	@Override
	public NpcTemplate getTemplate()
	{
		return (NpcTemplate) super.getTemplate();
	}

	@Override
	public int getNpcId()
	{
		return getTemplate().getId();
	}

	public void setUnAggred(boolean state)
	{
		_unAggred = state;
	}

	public boolean isAggressive()
	{
		return getAggroRange() > 0;
	}

	public int getAggroRange()
	{
		if(_unAggred)
			return 0;
		if(_personalAggroRange >= 0)
			return _personalAggroRange;
		return getTemplate().aggroRange;
	}

	public void setAggroRange(int aggroRange)
	{
		_personalAggroRange = aggroRange;
	}

	public Faction getFaction()
	{
		return getTemplate().getFaction();
	}

	public boolean isInFaction(NpcInstance npc)
	{
		return getFaction().equals(npc.getFaction()) && !getFaction().isIgnoreNpcId(npc.getNpcId());
	}

	public long getExpReward()
	{
		return (long) calcStat(Stats.EXP_RATE_MULTIPLIER, getTemplate().rewardExp, null, null);
	}

	public long getSpReward()
	{
		return (long) calcStat(Stats.SP_RATE_MULTIPLIER, getTemplate().rewardSp, null, null);
	}

	public int getRewardCrp()
	{
		return getTemplate().rewardCrp;
	}

	@Override
	protected void onDelete()
	{
		getAI().stopAllTaskAndTimers();
		stopDecay();
		if(_spawn != null)
			_spawn.stopRespawn();
		setSpawn(null);
		if(hasMinions() && doUnspawnMinions())
			getMinionList().deleteMinions();
		super.onDelete();
	}

	public Spawner getSpawn()
	{
		return _spawn;
	}

	public void setSpawn(Spawner spawn)
	{
		_spawn = spawn;
	}

	public final void decayOrDelete()
	{
		onDecay();
	}

	@Override
	protected void onDecay()
	{
		super.onDecay();

		getListeners().onDecay();

		_spawnAnimation = 2;
		if(_spawn != null)
			_spawn.decreaseCount(this);
		else if(!isMinion())
			deleteMe();
	}

	protected void startDecay(long delay)
	{
		stopDecay();
		_decayTask = DecayTaskManager.getInstance().addDecayTask(this, delay);
	}

	public void stopDecay()
	{
		if(_decayTask != null)
		{
			_decayTask.cancel(false);
			_decayTask = null;
		}
	}

	public void endDecayTask()
	{
		if(_decayTask != null)
		{
			_decayTask.cancel(false);
			_decayTask = null;
		}
		doDecay();
	}

	@Override
	public boolean isUndead()
	{
		return getTemplate().isUndead();
	}

	public void setLevel(int level)
	{
		_level = level;
	}

	@Override
	public int getLevel()
	{
		return _level == 0 ? getTemplate().level : _level;
	}

	public void setDisplayId(int displayId)
	{
		_displayId = displayId;
	}

	public int getDisplayId(Creature creature) {
		return getDisplayId();
	}

	public int getDisplayId()
	{
		return _displayId > 0 ? _displayId : getTemplate().displayId;
	}

	@Override
	public ItemInstance getActiveWeaponInstance()
	{
		return null;
	}

	@Override
	public WeaponTemplate getActiveWeaponTemplate()
	{
		int weaponId = getTemplate().rhand;
		if(weaponId < 1)
			return null;
		ItemTemplate item = ItemHolder.getInstance().getTemplate(getTemplate().rhand);
		if(!(item instanceof WeaponTemplate))
			return null;
		return (WeaponTemplate) item;
	}

	@Override
	public ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}

	@Override
	public WeaponTemplate getSecondaryWeaponTemplate()
	{
		int weaponId = getTemplate().lhand;
		if(weaponId < 1)
			return null;
		ItemTemplate item = ItemHolder.getInstance().getTemplate(getTemplate().lhand);
		if(!(item instanceof WeaponTemplate))
			return null;
		return (WeaponTemplate) item;
	}

	@Override
	public void sendChanges()
	{
		if(isFlying())
			return;
		super.sendChanges();
	}

	public void onMenuSelect(Player player, int ask, int reply)
	{
		if(ask == -303)
		{
			Castle castle = getCastle(player);
			MultiSellHolder.getInstance().SeparateAndSend(reply, player, getObjectId(),
					castle != null ? castle.getSellTaxRate(player) : 0.0);
		}
		for(QuestState qs : player.getAllQuestsStates())
			if(qs.getQuest().getId() == ask && !qs.isCompleted())
			{
				qs.getQuest().notifyMenuSelect(reply, qs, this);
				return;
			}
		getAI().notifyEvent(CtrlEvent.EVT_MENU_SELECTED, player, ask, reply);
	}

	@Override
	public void broadcastCharInfo()
	{
		if(!isVisible())
			return;
		if(_broadcastCharInfoTask != null)
			return;
		_broadcastCharInfoTask = ThreadPoolManager.getInstance().schedule(new BroadcastCharInfoTask(), Config.BROADCAST_CHAR_INFO_INTERVAL);
	}

	@Override
	public void broadcastCharInfoImpl(IUpdateTypeComponent... components)
	{
		if(components.length == 0)
		{
			_log.warn(getClass().getSimpleName() + ": Trying broadcast char info without components!", new Exception());
			return;
		}
		for(Player player : World.getAroundObservers(this))
			player.sendPacket(new NpcInfoPacket(this, player).update(components));
	}

	public void onRandomAnimation()
	{
		if(System.currentTimeMillis() - _lastSocialAction > 10000L)
		{
            broadcastPacket(new SocialActionPacket(getObjectId(), 2));
			_lastSocialAction = System.currentTimeMillis();
		}
	}

	public void startRandomAnimation()
	{
		if(!hasRandomAnimation())
			return;
		_animationTask = LazyPrecisionTaskManager.getInstance().addNpcAnimationTask(this);
	}

	public void stopRandomAnimation()
	{
		if(_animationTask != null)
		{
			_animationTask.cancel(false);
			_animationTask = null;
		}
	}

	public boolean hasRandomAnimation()
	{
		return _hasRandomAnimation;
	}

	public void setHaveRandomAnim(boolean value)
	{
		_hasRandomAnimation = value;
	}

	public boolean hasRandomWalk()
	{
		return _hasRandomWalk;
	}

	public void setRandomWalk(boolean value)
	{
		_hasRandomWalk = value;
	}

	public Castle getCastle()
	{
		if(getReflection() == ReflectionManager.PARNASSUS && Config.SERVICES_PARNASSUS_NOTAX)
			return null;
		if(Config.SERVICES_OFFSHORE_NO_CASTLE_TAX && getReflection() == ReflectionManager.GIRAN_HARBOR)
			return null;
		if(Config.SERVICES_OFFSHORE_NO_CASTLE_TAX && getReflection() == ReflectionManager.PARNASSUS)
			return null;
		if(Config.SERVICES_OFFSHORE_NO_CASTLE_TAX && isInZone(Zone.ZoneType.offshore))
			return null;
		if(_nearestCastle == null)
			_nearestCastle = ResidenceHolder.getInstance().getResidence(getTemplate().getCastleId());
		if(_nearestCastle == null)
			_nearestCastle = ResidenceHolder.getInstance().findNearestResidence(Castle.class, getX(), getY(), getZ(), getReflection(), 65536);

		return _nearestCastle;
	}

	public Castle getCastle(Player player)
	{
		return getCastle();
	}

	public ClanHall getClanHall()
	{
		if(_nearestClanHall == null)
			_nearestClanHall = ResidenceHolder.getInstance().findNearestResidence(ClanHall.class, getX(), getY(), getZ(), getReflection(), 32768);
		return _nearestClanHall;
	}

	private boolean showPkDenyChatWindow(Player player, String type)
	{
		showChatWindow(player, type + "/" + getNpcId() + "-pk.htm", false);
		player.sendActionFailed();
		return true;
	}

	@Override
	public void onAction(Player player, boolean shift)
	{
		if(!isTargetable(player))
		{
			player.sendActionFailed();
			return;
		}
		if(player.getTarget() != this)
		{
			player.setNpcTarget(this);
			return;
		}
		if(shift && OnShiftActionHolder.getInstance().callShiftAction(player, NpcInstance.class, this, true))
			return;
		if(isAutoAttackable(player))
		{
			player.getAI().Attack(this, false, shift);
			return;
		}
		if(!checkInteractionDistance(player))
		{
			if(player.getAI().getIntention() != CtrlIntention.AI_INTENTION_INTERACT)
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this, null);
			return;
		}
		if(player.getKarma() < 0)
		{
			if(!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && isMerchant() && showPkDenyChatWindow(player, "merchant"))
				return;
			else if(!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_GK && isTeleporter() && showPkDenyChatWindow(player, "teleporter"))
				return;
			else if(!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE && isWarehouse() && showPkDenyChatWindow(player, "warehouse"))
				return;
		}
		if(!Config.ALLOW_TALK_WHILE_SITTING && player.isSitting() || player.isActionsDisabled())
		{
			player.sendActionFailed();
			return;
		}
		player.sendActionFailed();
		if(player.isMoving())
			player.stopMove();
		player.sendPacket(new MTPPacket(player, this, 200));
		if(_isBusy)
			showBusyWindow(player);
		else if(isHasChatWindow())
		{
			if (!player.isPhantom() && player.tScheme_record.isLogging())
				player.tScheme_record.setTarget(this.getNpcId());
			
			boolean flag = false;
			Set<Quest> quests = getTemplate().getEventQuests(QuestEventType.NPC_FIRST_TALK);
			if(quests != null)
				for(Quest quest : quests)
				{
					QuestState qs = player.getQuestState(quest);
					if((qs == null || !qs.isCompleted()) && quest.notifyFirstTalk(this, player))
						flag = true;
				}
			if(!flag)
			{
                showChatWindow(player, 0, true);
				if(Config.NPC_DIALOG_PLAYER_DELAY > 0)
					player.setNpcDialogEndTime((int) (System.currentTimeMillis() / 1000L) + Config.NPC_DIALOG_PLAYER_DELAY);
			}
		}
	}

	public void showQuestWindow(Player player, int questId)
	{
		if(!player.isQuestContinuationPossible(true))
			return;
		int count = 0;
		for(QuestState qs : player.getAllQuestsStates())
			if(qs != null && qs.getQuest().isVisible(player) && qs.isStarted() && qs.getCond() > 0)
				++count;
		if(count > 40)
		{
            showChatWindow(player, "quest-limit.htm", false);
			return;
		}
		try
		{
			QuestState qs2 = player.getQuestState(questId);
			if(qs2 != null)
			{
				if(qs2.isCompleted() && qs2.getQuest().notifyCompleted(this, qs2))
					return;
				if(qs2.getQuest().notifyTalk(this, qs2))
					return;
			}
			else
			{
				Quest quest = QuestHolder.getInstance().getQuest(questId);
				if(quest != null)
				{
					Set<Quest> quests = getTemplate().getEventQuests(QuestEventType.QUEST_START);
					if(quests != null && quests.contains(quest))
					{
						qs2 = quest.newQuestState(player);
						if(qs2.getQuest().notifyTalk(this, qs2))
							return;
					}
				}
			}
            showChatWindow(player, "no-quest.htm", false);
		}
		catch(Exception e)
		{
			_log.warn("problem with npc text(QUEST ID[" + questId + "]" + e);
			_log.error("", e);
		}
		player.sendActionFailed();
	}

	public boolean canBypassCheck(Player player)
	{
		if(player.isDead() || !checkInteractionDistance(player))
		{
			player.sendActionFailed();
			return false;
		}
		return true;
	}

	public void onBypassFeedback(Player player, String command)
	{
		try
		{
			StringTokenizer st = new StringTokenizer(command, "_");
			String cmd = st.nextToken();
			if("TerritoryStatus".equalsIgnoreCase(command))
			{
				HtmlMessage html = new HtmlMessage(this);
				Castle castle = getCastle(player);
				if(castle != null && castle.getId() > 0)
				{
					if(castle.getOwnerId() > 0)
					{
						Clan clan = ClanTable.getInstance().getClan(castle.getOwnerId());
						if(clan != null)
						{
							html.setFile("merchant/territorystatus.htm");
							html.replace("%castlename%", HtmlUtils.htmlResidenceName(castle.getId()));
							html.replace("%taxpercent%", String.valueOf(castle.getSellTaxPercent()));
							html.replace("%clanname%", clan.getName());
							html.replace("%clanleadername%", clan.getLeaderName());
						}
						else
						{
							html.setFile("merchant/territorystatus_noowner.htm");
							html.replace("%castlename%", HtmlUtils.htmlResidenceName(castle.getId()));
						}
					}
					else
					{
						html.setFile("merchant/territorystatus_noowner.htm");
						html.replace("%castlename%", HtmlUtils.htmlResidenceName(castle.getId()));
					}
				}
				else
				{
					html.setFile("merchant/territorystatus_noowner.htm");
					html.replace("%castlename%", "\u2014");
				}
				player.sendPacket(html);
			}
			else if(command.startsWith("QuestEvent"))
			{
				StringTokenizer tokenizer = new StringTokenizer(command);
				tokenizer.nextToken();
				String questName = tokenizer.nextToken();
				player.processQuestEvent(Integer.parseInt(questName), command.substring(12 + questName.length()), this);
			}
			else if(command.startsWith("Quest"))
			{
				String quest = command.substring(5).trim();
				if(quest.isEmpty())
                    showQuestWindow(player);
				else
					try
					{
						int questId = Integer.parseInt(quest);
                        showQuestWindow(player, questId);
					}
					catch(NumberFormatException nfe)
					{
						_log.error("", nfe);
					}
			}
			else if(command.startsWith("Chat"))
				try
				{
					int val = Integer.parseInt(command.substring(5));
                    showChatWindow(player, val, false);
				}
				catch(NumberFormatException nfe3)
				{
					String filename = command.substring(5).trim();
					if(filename.isEmpty())
                        showChatWindow(player, "npcdefault.htm", false);
					else
                        showChatWindow(player, filename, false);
				}
			else if(command.startsWith("AttributeCancel"))
				player.sendPacket(new ExShowBaseAttributeCancelWindow(player));
			else if(command.startsWith("NpcLocationInfo"))
			{
				int val = Integer.parseInt(command.substring(16));
				NpcInstance npc = GameObjectsStorage.getByNpcId(val);
				if(npc != null)
				{
					player.sendPacket(new RadarControlPacket(2, 2, npc.getLoc()));
					player.sendPacket(new RadarControlPacket(0, 1, npc.getLoc()));
				}
			}
			else if(command.startsWith("Multisell") || command.startsWith("multisell"))
			{
				String listId = command.substring(9).trim();
				Castle castle = getCastle(player);
				MultiSellHolder.getInstance().SeparateAndSend(Integer.parseInt(listId), player, getObjectId(),
						castle != null ? castle.getSellTaxRate(player) : 0.0);
			}
			else if("ClanSkillList".equalsIgnoreCase(command))
				showClanSkillList(player);
			else if(command.startsWith("SubUnitSkillList"))
				showSubUnitSkillList(player);
			else if(command.startsWith("Augment"))
			{
				int cmdChoice = Integer.parseInt(command.substring(8, 9).trim());
				if(cmdChoice == 1)
					player.sendPacket(SystemMsg.SELECT_THE_ITEM_TO_BE_AUGMENTED, ExShowVariationMakeWindow.STATIC_PACKET);
				else if(cmdChoice == 2)
					player.sendPacket(SystemMsg.SELECT_THE_ITEM_FROM_WHICH_YOU_WISH_TO_REMOVE_AUGMENTATION, ExShowVariationCancelWindow.STATIC_PACKET);
			}
			else if(command.startsWith("Link"))
                showChatWindow(player, command.substring(5), false);
			else if("teleport".equalsIgnoreCase(cmd))
			{
				if(!st.hasMoreTokens())
				{
					errorBypass(command, player);
					return;
				}
				String cmd2 = st.nextToken();
				if("list".equalsIgnoreCase(cmd2))
				{
					int listId2 = 1;
					if(st.hasMoreTokens())
						listId2 = Integer.parseInt(st.nextToken());
                    showTeleportList(player, listId2);
				}
				else if("id".equalsIgnoreCase(cmd2))
				{
					int listId2 = Integer.parseInt(st.nextToken());
					int teleportNameId = Integer.parseInt(st.nextToken());
					List<TeleportLocation> list = getTemplate().getTeleportList(listId2);
					if(list == null || list.isEmpty())
					{
						errorBypass(command, player);
						return;
					}
					TeleportLocation teleportLocation = null;
					for(TeleportLocation tl : list)
						if(tl.getName() == teleportNameId)
						{
							teleportLocation = tl;
							break;
						}
					if(teleportLocation == null)
					{
						errorBypass(command, player);
						return;
					}
					long itemCount = calcTeleportPrice(player, teleportLocation);
					if(st.hasMoreTokens())
						itemCount = Long.parseLong(st.nextToken());
                    teleportPlayer(player, teleportLocation, itemCount);
				}
				else if("mdt".equalsIgnoreCase(cmd2))
				{
					if(!st.hasMoreTokens())
					{
						errorBypass(command, player);
						return;
					}
					String cmd3 = st.nextToken();
					if("to".equalsIgnoreCase(cmd3))
					{
						player.setVar("@mdt_back_cords", player.getLoc().toXYZString(), -1L);
						player.teleToLocation(14040, 182504, -3568);
					}
					else if("from".equalsIgnoreCase(cmd3))
					{
						String var = player.getVar("@mdt_back_cords");
						if(var == null || var.isEmpty())
						{
							player.teleToLocation(12902, 181011, -3563);
							return;
						}
						player.teleToLocation(Location.parseLoc(var));
					}
				}
				else if("fi".equalsIgnoreCase(cmd2))
				{
					if(!st.hasMoreTokens())
					{
						errorBypass(command, player);
						return;
					}
					String cmd3 = st.nextToken();
					if("to".equalsIgnoreCase(cmd3))
					{
						player.setVar("@fi_back_cords", player.getLoc().toXYZString(), -1L);
						switch(Rnd.get(4))
						{
							case 1:
							{
								player.teleToLocation(-60695, -56896, -2032);
								break;
							}
							case 2:
							{
								player.teleToLocation(-59716, -55920, -2032);
								break;
							}
							case 3:
							{
								player.teleToLocation(-58752, -56896, -2032);
								break;
							}
							default:
							{
								player.teleToLocation(-59716, -57864, -2032);
								break;
							}
						}
					}
					else if("from".equalsIgnoreCase(cmd3))
					{
						String var = player.getVar("@fi_back_cords");
						if(var == null || var.isEmpty())
						{
							player.teleToLocation(12902, 181011, -3563);
							return;
						}
						player.teleToLocation(Location.parseLoc(var));
					}
				}
				else
				{
					if(st.countTokens() < 2)
					{
						errorBypass(command, player);
						return;
					}
                    int y = Integer.parseInt(st.nextToken());
					int z = Integer.parseInt(st.nextToken());
					int itemId = 0;
					if(st.hasMoreTokens())
						itemId = Integer.parseInt(st.nextToken());
					int itemCount2 = 0;
					if(st.hasMoreTokens())
						itemCount2 = Integer.parseInt(st.nextToken());
					int castleId = 0;
					if(st.hasMoreTokens())
						castleId = Integer.parseInt(st.nextToken());
					int reflectionId = 0;
					if(st.hasMoreTokens())
						reflectionId = Integer.parseInt(st.nextToken());
                    int x = Integer.parseInt(cmd2);
                    teleportPlayer(player, x, y, z, itemId, itemCount2, new int[] { castleId }, reflectionId);
				}
			}
			else if(command.startsWith("open_gate"))
			{
				int val = Integer.parseInt(command.substring(10));
				ReflectionUtils.getDoor(val).openMe();
				player.sendActionFailed();
			}
			else if(command.startsWith("ExitFromQuestInstance"))
			{
				Reflection r = player.getReflection();
				if(r.isDefault())
					return;
				r.startCollapseTimer(60000L);
				player.teleToLocation(r.getReturnLoc(), ReflectionManager.MAIN);
				if(command.length() > 22)
					try
					{
						int val2 = Integer.parseInt(command.substring(22));
                        showChatWindow(player, val2, false);
					}
					catch(NumberFormatException nfe)
					{
						String filename2 = command.substring(22).trim();
						if(!filename2.isEmpty())
                            showChatWindow(player, filename2, false);
					}
			}
			else if("WithdrawP".equalsIgnoreCase(cmd))
				WarehouseFunctions.showRetrieveWindow(player);
			else if("DepositP".equalsIgnoreCase(cmd))
				WarehouseFunctions.showDepositWindow(player);
			else if("WithdrawC".equalsIgnoreCase(cmd))
				WarehouseFunctions.showWithdrawWindowClan(player);
			else if("DepositC".equalsIgnoreCase(cmd))
				WarehouseFunctions.showDepositWindowClan(player);
			else if(command.startsWith("ensoul_"))
			{
				var ensoulCommand = command.substring("ensoul_".length());
				if(Objects.equals(ensoulCommand, "insert"))
					player.sendPacket(ExShowEnsoulWindow.STATIC);
				else if(Objects.equals(ensoulCommand, "extract"))
					player.sendPacket(ExEnSoulExtractionShow.STATIC);
			}
			else if(cmd.startsWith("exchangeRune"))
			{
                PcInventory inventory = player.getInventory();
				inventory.writeLock();
				try
				{
                    int id = Integer.parseInt(command.substring(13));
                    if(inventory.getCountOf(id) >= 2)
					{
						ItemFunctions.deleteItem(player, id, 2, true);
						ItemTemplate template = ItemHolder.getInstance().getTemplate(id);
						int lvl = Integer.parseInt(template.getName().split("Ур.")[1]);
						ItemFunctions.addItem(player, 69999 + lvl, 1);
					}
					else
					{
						HtmlMessage html = new HtmlMessage(this);
						html.setFile("blacksmith/weapon_variation003.htm");
						player.sendPacket(html);
					}
				}
				finally
				{
					inventory.writeUnlock();
				}
			}
			else
			{
				String word = command.split("\\s+")[0];
				String args = command.substring(word.length()).trim();
				Pair<Object, Method> b = BypassHolder.getInstance().getBypass(word);
				if(b != null)
					b.getValue().invoke(b.getKey(), player, this, StringUtils.isEmpty(args) ? new String[0] : args.split("\\s+"));
				else
					_log.warn("Unknown command=[" + command + "] npcId:" + getTemplate().getId());
			}
		}
		catch(NumberFormatException nfe2)
		{
			_log.warn("Invalid bypass to Server command parameter! npcId=" + getTemplate().getId() + " command=[" + command + "]", nfe2);
		}
		catch(Exception sioobe)
		{
			_log.warn("Incorrect htm bypass! npcId=" + getTemplate().getId() + " command=[" + command + "]", sioobe);
		}
	}

	public void errorBypass(String bypass, Player player)
	{
		player.sendMessage(new CustomMessage("l2s.gameserver.model.instance.NpcInstance.ErrorBypass").addNumber(getNpcId()).addString(bypass));
	}

	public boolean teleportPlayer(Player player, int x, int y, int z, int itemId, long itemCount, int[] castleIds, int reflectionId)
	{
		if(player == null)
			return false;
		if(player.getMountType() == MountType.WYVERN)
			return false;
		switch(getNpcId())
		{
			case 30483:
			{
				if(Config.CRUMA_GATEKEEPER_LVL > 0 && player.getLevel() >= Config.CRUMA_GATEKEEPER_LVL)
				{
                    showChatWindow(player, "teleporter/" + getNpcId() + "-no.htm", false);
					return false;
				}
				break;
			}
			case 32864:
			case 32865:
			case 32866:
			case 32867:
			case 32868:
			case 32869:
			case 32870:
			{
				if(player.getLevel() < 80)
				{
                    showChatWindow(player, "teleporter/" + getNpcId() + "-no.htm", false);
					return false;
				}
				break;
			}
		}

		if(itemId > 0 && itemCount > 0L && ItemFunctions.getItemCount(player, itemId) < itemCount)
		{
			if(itemId == 57)
				player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			else
				player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
			return false;
		}
		if(castleIds.length > 0 && player.getReflection().isMain() && !Config.ALT_TELEPORT_TO_TOWN_DURING_SIEGE)
			for(int castleId : castleIds)
			{
				Castle castle = ResidenceHolder.getInstance().getResidence(Castle.class, castleId);
				if(castle != null && castle.getSiegeEvent() != null && castle.getSiegeEvent().isInProgress())
				{
					player.sendPacket(SystemMsg.YOU_CANNOT_TELEPORT_TO_A_VILLAGE_THAT_IS_IN_A_SIEGE);
					return false;
				}
			}
		if(itemId > 0 && itemCount > 0L && !ItemFunctions.deleteItem(player, itemId, itemCount, true))
			return false;
		Location pos = Location.findPointToStay(x, y, z, 50, 100, player.getGeoIndex());
		if(reflectionId > -1)
		{
			Reflection reflection = ReflectionManager.getInstance().get(reflectionId);
			if(reflection == null)
			{
				_log.warn("Cannot teleport to reflection ID: " + reflectionId + "!");
				return false;
			}
			player.teleToLocation(pos, reflection);
		}
		else
			player.teleToLocation(pos);
		return true;
	}

	public boolean teleportPlayer(Player player, Location loc, int itemId, long itemCount, int[] castleIds, int reflectionId)
	{
		return teleportPlayer(player, loc.getX(), loc.getY(), loc.getZ(), itemId, itemCount, castleIds, reflectionId);
	}

	public boolean teleportPlayer(Player player, int x, int y, int z, int itemId, long itemCount)
	{
		return teleportPlayer(player, x, y, z, itemId, itemCount, new int[0], -1);
	}

	public boolean teleportPlayer(Player player, Location loc, int itemId, long itemCount)
	{
		return teleportPlayer(player, loc.getX(), loc.getY(), loc.getZ(), itemId, itemCount, new int[0], -1);
	}

	private boolean teleportPlayer(Player player, TeleportLocation loc, long itemCount)
	{
		return teleportPlayer(player, loc, loc.getItemId(), itemCount, loc.getCastleIds(), -1);
	}

	private long calcTeleportPrice(Player player, TeleportLocation loc)
	{
		if(loc.getItemId() != 57)
			return loc.getPrice();
		double pricemod = loc.isPrimeHours() && player.getLevel() <= Config.GATEKEEPER_FREE ? 0.0 : Config.GATEKEEPER_MODIFIER;
		return (long) (loc.getPrice() * pricemod * player.getPremiumAccount().getRates().getTaxTp());
	}

	public void showTeleportList(Player player)
	{
        showTeleportList(player, 1);
	}

	public void showTeleportList(Player player, int listId)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("&$556;").append("<br><br>");
		List<TeleportLocation> list = getTemplate().getTeleportList(listId);
		if(list != null && !list.isEmpty() && player.getPlayerAccess().UseTeleport)
		{
			for(TeleportLocation tl : list)
				if(tl.getQuestZoneId() > 0 && tl.getQuestZoneId() == player.getQuestZoneId())
					if(tl.getItemId() == 57)
					{
						long price = calcTeleportPrice(player, tl);
						sb.append("<Button ALIGN=LEFT ICON=\"QUEST\" action=\"bypass -h npc_%objectId%_teleport_id_").append(listId).append("_").append(tl.getName()).append("_").append(price).append("\" msg=\"811;F;").append(tl.getName()).append("\">").append(HtmlUtils.htmlNpcString(tl.getName()));
						if(price > 0L)
							sb.append(" - ").append(price).append(" ").append(HtmlUtils.htmlItemName(57));
						sb.append("</button>");
					}
					else
					{
						sb.append("<Button ALIGN=LEFT ICON=\"QUEST\" action=\"bypass -h npc_%objectId%_teleport_id_").append(listId).append("_").append(tl.getName()).append("\" msg=\"811;F;").append(tl.getName()).append("\">").append(HtmlUtils.htmlNpcString(tl.getName()));
						if(tl.getItemId() > 0 && tl.getPrice() > 0L)
							sb.append(" - ").append(tl.getPrice()).append(" ").append(HtmlUtils.htmlItemName(tl.getItemId()));
						sb.append("</button>");
					}
			for(TeleportLocation tl : list)
				if(tl.getQuestZoneId() <= 0 || tl.getQuestZoneId() != player.getQuestZoneId())
					if(tl.getItemId() == 57)
					{
						long price = calcTeleportPrice(player, tl);
						sb.append("<Button ALIGN=LEFT ICON=\"TELEPORT\" action=\"bypass -h npc_%objectId%_teleport_id_").append(listId).append("_").append(tl.getName()).append("_").append(price).append("\" msg=\"811;F;").append(tl.getName()).append("\">").append(HtmlUtils.htmlNpcString(tl.getName()));
						if(price > 0L)
							sb.append(" - ").append(price).append(" ").append(HtmlUtils.htmlItemName(57));
						sb.append("</button>");
					}
					else
					{
						sb.append("<Button ALIGN=LEFT ICON=\"TELEPORT\" action=\"bypass -h npc_%objectId%_teleport_id_").append(listId).append("_").append(tl.getName()).append("\" msg=\"811;F;").append(tl.getName()).append("\">").append(HtmlUtils.htmlNpcString(tl.getName()));
						if(tl.getItemId() > 0 && tl.getPrice() > 0L)
							sb.append(" - ").append(tl.getPrice()).append(" ").append(HtmlUtils.htmlItemName(tl.getItemId()));
						sb.append("</button>");
					}
		}
		else
			sb.append("No teleports available for you.");
		HtmlMessage html = new HtmlMessage(this);
		html.setHtml(HtmlUtils.bbParse(sb.toString()));
		player.sendPacket(html);
	}

	public void showQuestWindow(Player player)
	{
		TIntObjectMap<QuestInfo> options = new TIntObjectHashMap<>();
		Set<Quest> quests = getTemplate().getEventQuests(QuestEventType.QUEST_START);
		if(quests != null)
			for(Quest quest : quests)
				if(quest.isVisible(player) && quest.checkStartNpc(this, player) && !options.containsKey(quest.getId()))
					options.put(quest.getId(), new QuestInfo(quest, player, true));
		List<QuestState> awaits = player.getQuestsForEvent(this, QuestEventType.QUEST_TALK);
		if(awaits != null)
			for(QuestState qs : awaits)
			{
				Quest quest2 = qs.getQuest();
				if(quest2.isVisible(player) && quest2.checkTalkNpc(this, qs) && !options.containsKey(quest2.getId()))
					options.put(quest2.getId(), new QuestInfo(quest2, player, false));
			}
		if(options.size() > 1)
		{
			List<QuestInfo> list = new ArrayList<>(options.valueCollection());
			Collections.sort(list);
			showQuestChooseWindow(player, list);
		}
		else if(options.size() == 1)
            showQuestWindow(player, options.values(new QuestInfo[1])[0].getQuest().getId());
		else
            showChatWindow(player, "no-quest.htm", false);
	}

	public void showQuestChooseWindow(Player player, List<QuestInfo> quests)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<html><body>");
		for(QuestInfo info : quests)
		{
			Quest q = info.getQuest();
			if(!q.isVisible(player))
				continue;
			sb.append("<button icon=quest align=left action=\"bypass -h npc_").append(getObjectId()).append("_Quest ").append(q.getId()).append("\">").append(q.getDescr(this, player, info.isStart())).append("</button>");
		}
		sb.append("</body></html>");
		HtmlMessage html = new HtmlMessage(this);
		html.setHtml(sb.toString());
		player.sendPacket(html);
	}

	public void showChatWindow(Player player, int val, boolean firstTalk, Object... replace)
	{
		String filename = getHtmlPath(getHtmlFilename(val, player), player);

		HtmlMessage packet = new HtmlMessage(this, filename).setPlayVoice(firstTalk);
		if(replace.length % 2 == 0)
			for(int i = 0; i < replace.length; i += 2)
				packet.replace(String.valueOf(replace[i]), String.valueOf(replace[i + 1]));
		player.sendPacket(packet);
	}

	public void showChatWindow(Player player, String filename, boolean firstTalk, Object... replace)
	{
		HtmlMessage packet;
		if(filename.endsWith(".htm"))
			packet = new HtmlMessage(this, filename);
		else
		{
			packet = new HtmlMessage(this);
			packet.setHtml(filename);
		}
		packet.setPlayVoice(firstTalk);
		if(replace.length % 2 == 0)
			for(int i = 0; i < replace.length; i += 2)
				packet.replace(String.valueOf(replace[i]), String.valueOf(replace[i + 1]));
		player.sendPacket(packet);
	}

	public String getHtmlFilename(int val, Player player)
	{
		String filename;
		if(val == 0)
			filename = getNpcId() + ".htm";
		else
			filename = getNpcId() + "-" + val + ".htm";
		return filename;
	}

	public String getHtmlDir(String filename, Player player)
	{
		if(getTemplate().getHtmRoot() != null)
			return getTemplate().getHtmRoot();
		if(HtmCache.getInstance().getIfExists(filename, player) != null)
			return "";
		if(HtmCache.getInstance().getIfExists("default/" + filename, player) != null)
			return "default/";
		if(HtmCache.getInstance().getIfExists("blacksmith/" + filename, player) != null)
			return "blacksmith/";
		if(HtmCache.getInstance().getIfExists("merchant/" + filename, player) != null)
			return "merchant/";
		if(HtmCache.getInstance().getIfExists("teleporter/" + filename, player) != null)
			return "teleporter/";
		if(HtmCache.getInstance().getIfExists("petmanager/" + filename, player) != null)
			return "petmanager/";
		if(HtmCache.getInstance().getIfExists("mammons/" + filename, player) != null)
			return "mammons/";
		if(HtmCache.getInstance().getIfExists("warehouse/" + filename, player) != null)
			return "warehouse/";
		return null;
	}

	public final String getHtmlPath(String filename, Player player)
	{
		String dir = getHtmlDir(filename, player);
		if(dir == null)
			return "npcdefault.htm";
		String path = dir + filename;
		if(HtmCache.getInstance().getIfExists(path, player) != null)
			return path;
		return "npcdefault.htm";
	}

	public final boolean isBusy()
	{
		return _isBusy;
	}

	public void setBusy(boolean isBusy)
	{
		_isBusy = isBusy;
	}

	public final String getBusyMessage()
	{
		return _busyMessage;
	}

	public void setBusyMessage(String message)
	{
		_busyMessage = message;
	}

	public void showBusyWindow(Player player)
	{
		HtmlMessage html = new HtmlMessage(this);
		html.setFile("npcbusy.htm");
		html.replace("%busymessage%", _busyMessage);
		player.sendPacket(html);
	}

	public static void showClanSkillList(Player player)
	{
		if(player.getClan() == null || !player.isClanLeader())
		{
			player.sendPacket(SystemMsg.ONLY_THE_CLAN_LEADER_IS_ENABLED);
			player.sendActionFailed();
			return;
		}
		showAcquireList(AcquireType.CLAN, player);
	}

	public static void showAcquireList(AcquireType t, Player player)
	{
		Collection<SkillLearn> skills = SkillAcquireHolder.getInstance().getAvailableSkills(player, t);
		ExAcquirableSkillListByClass asl = new ExAcquirableSkillListByClass(t, skills.size());
		for(SkillLearn s : skills)
			asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), s.getCost(), s.getMinLevel());
		if(skills.isEmpty())
		{
			player.sendPacket(AcquireSkillDonePacket.STATIC);
			player.sendPacket(SystemMsg.THERE_ARE_NO_OTHER_SKILLS_TO_LEARN);
		}
		else
			player.sendPacket(asl);
		player.sendActionFailed();
	}

	public static void showSubUnitSkillList(Player player)
	{
		Clan clan = player.getClan();
		if(clan == null)
			return;
		if((player.getClanPrivileges() & 0x200) != 0x200)
		{
			player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		Set<SkillLearn> learns = new TreeSet<>();
		for(SubUnit sub : player.getClan().getAllSubUnits())
			learns.addAll(SkillAcquireHolder.getInstance().getAvailableSkills(player, AcquireType.SUB_UNIT, sub));
		ExAcquirableSkillListByClass asl = new ExAcquirableSkillListByClass(AcquireType.SUB_UNIT, learns.size());
		for(SkillLearn s : learns)
			asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), s.getCost(), 1, 2002);
		if(learns.isEmpty())
		{
			player.sendPacket(AcquireSkillDonePacket.STATIC);
			player.sendPacket(SystemMsg.THERE_ARE_NO_OTHER_SKILLS_TO_LEARN);
		}
		else
			player.sendPacket(asl);
		player.sendActionFailed();
	}

	public int getSpawnAnimation()
	{
		return _spawnAnimation;
	}

	public int calculateLevelDiffForDrop(int charLevel)
	{
		if(!Config.DEEPBLUE_DROP_RULES)
			return 0;
		int mobLevel = getLevel();
		int deepblue_maxdiff = this instanceof RaidBossInstance ? Config.DEEPBLUE_DROP_RAID_MAXDIFF : Config.DEEPBLUE_DROP_MAXDIFF;
		return Math.max(charLevel - mobLevel - deepblue_maxdiff, 0);
	}

	@Override
	public String toString()
	{
		return getNpcId() + " " + getName();
	}

	public void refreshID()
	{
		GameObjectsStorage.remove(this);
		objectId = IdFactory.getInstance().getNextId();
		GameObjectsStorage.put(this);
	}

	public void setUnderground(boolean b)
	{
		_isUnderground = b;
	}

	public boolean isUnderground()
	{
		return _isUnderground;
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
	public NpcListenerList getListeners()
	{
		if(listeners == null)
			synchronized (this)
			{
				if(listeners == null)
					listeners = new NpcListenerList(this);
			}
		return (NpcListenerList) listeners;
	}

	public <T extends NpcListener> boolean addListener(T listener)
	{
		return getListeners().add(listener);
	}

	public <T extends NpcListener> boolean removeListener(T listener)
	{
		return getListeners().remove(listener);
	}

	@Override
	public NpcStatsChangeRecorder getStatsRecorder()
	{
		if(_statsRecorder == null)
			synchronized (this)
			{
				if(_statsRecorder == null)
					_statsRecorder = new NpcStatsChangeRecorder(this);
			}
		return (NpcStatsChangeRecorder) _statsRecorder;
	}

	public void setNpcState(int stateId)
	{
        broadcastPacket(new ExChangeNPCState(getObjectId(), stateId));
		npcState = stateId;
	}

	public int getNpcState()
	{
		return npcState;
	}

	@Override
	public List<L2GameServerPacket> addPacketList(Player forPlayer, Creature dropper)
	{
		List<L2GameServerPacket> list = new ArrayList<>(3);
		list.add(new NpcInfoPacket(this, forPlayer).init());
		if(isInCombat())
			list.add(new AutoAttackStartPacket(getObjectId()));
		if(isMoving() || isFollowing())
			list.add(movePacket());
		return list;
	}

	@Override
	public boolean isNpc()
	{
		return true;
	}

	@Override
	public int getGeoZ(Location loc)
	{
		if(isFlying() || isInWater() || isInBoat() || isBoat() || isDoor())
			return loc.z;
		if(!isNpc())
			return super.getGeoZ(loc);
		if(_spawnRange instanceof Territory)
			return GeoEngine.getHeight(loc, getGeoIndex());
		return loc.z;
	}

	@Override
	public Clan getClan()
	{
		Castle castle = getCastle();
		if(castle == null)
			return null;
		return castle.getOwner();
	}

	public NpcString getNameNpcString()
	{
		return _nameNpcString;
	}

	public NpcString getTitleNpcString()
	{
		return _titleNpcString;
	}

	public void setNameNpcString(NpcString nameNpcString)
	{
		_nameNpcString = nameNpcString;
	}

	public void setTitleNpcString(NpcString titleNpcString)
	{
		_titleNpcString = titleNpcString;
	}

	public SpawnRange getSpawnRange()
	{
		return _spawnRange;
	}

	public void setSpawnRange(SpawnRange spawnRange)
	{
		_spawnRange = spawnRange;
	}

	public void setParameter(String str, Object val)
	{
		if(_parameters == StatsSet.EMPTY)
			_parameters = new StatsSet();
		_parameters.set(str, val);
	}

	public void setParameters(MultiValueSet<String> set)
	{
		if(set.isEmpty())
			return;
		if(_parameters == StatsSet.EMPTY)
			_parameters = new MultiValueSet<>(set.size());
		_parameters.putAll(set);
	}

	public int getParameter(String str, int val)
	{
		return _parameters.getInteger(str, val);
	}

	public long getParameter(String str, long val)
	{
		return _parameters.getLong(str, val);
	}

	public boolean getParameter(String str, boolean val)
	{
		return _parameters.getBool(str, val);
	}

	public String getParameter(String str, String val)
	{
		return _parameters.getString(str, val);
	}

	public MultiValueSet<String> getParameters()
	{
		return _parameters;
	}

	@Override
	public boolean isPeaceNpc()
	{
		return true;
	}

	public boolean isHasChatWindow()
	{
		return _hasChatWindow;
	}

	public void setHasChatWindow(boolean hasChatWindow)
	{
		_hasChatWindow = hasChatWindow;
	}

	public boolean isServerObject()
	{
		return false;
	}

	@Override
	public double getColRadius()
	{
		return getCollisionRadius();
	}

	private double getCollisionRadius()
	{
		if(isVisualTransformed())
			return super.getColRadius();
		return super.getColRadius() * getCollisionRadiusModifier();
	}

	@Override
	public double getColHeight()
	{
		return getCollisionHeight();
	}

	private double getCollisionHeight()
	{
		if(isVisualTransformed())
			return super.getColHeight();
		return super.getColHeight() * getCollisionHeightModifier();
	}

	public final double getCollisionHeightModifier()
	{
		return _collisionHeightModifier;
	}

	public final void setCollisionHeightModifier(double value)
	{
		_collisionHeightModifier = value;
	}

	public final double getCollisionRadiusModifier()
	{
		return _collisionRadiusModifier;
	}

	public final void setCollisionRadiusModifier(double value)
	{
		_collisionRadiusModifier = value;
	}

	@Override
	public int getEnchantEffect()
	{
		return _enchantEffect;
	}

	public final boolean isNoSleepMode()
	{
		return _isNoSleepMode;
	}

	@Override
	public boolean isImmortal()
	{
		return _isImmortal;
	}

	public void blockReward()
	{}

	@Override
	public boolean isFearImmune()
	{
		return getLeader() != null ? getLeader().isFearImmune() : !isMonster() || super.isFearImmune();
	}

	public boolean canPassPacket(Player player, Class<? extends L2GameClientPacket> packet, Object... arg)
	{
		return packet == RequestItemEnsoul.class || packet == RequestTryEnSoulExtraction.class
				|| packet == RequestRefine.class || packet == RequestRefineCancel.class;
	}

	public boolean noShiftClick()
	{
		return _noShiftClick;
	}

	@Override
	public boolean isLethalImmune()
	{
		return _noLethal || super.isLethalImmune();
	}

	public double getRewardRate(Player player)
	{
		return player.getRateItems();
	}

	@Override
	protected L2GameServerPacket changeMovePacket()
	{
		return new NpcInfoState(this);
	}

	@SuppressWarnings("unchecked")
	public void setOwner(Player owner)
	{
		_ownerRef = (HardReference<Player>) (owner == null ? HardReferences.emptyRef() : owner.getRef());
	}

	@Override
	public Player getPlayer()
	{
		return _ownerRef.get();
	}

	public boolean isServerSideName()
	{
		return getTemplate().isServerSideName();
	}

	public boolean isServerSideTitle()
	{
		return getTemplate().isServerSideTitle();
	}

	public boolean addEventTrigger(int triggerId)
	{
		if(!_eventTriggers.add(triggerId))
			return false;
		if(getReflection().isMain())
			return EventTriggersManager.getInstance().addTrigger(MapUtils.regionX(getX()), MapUtils.regionY(getY()), triggerId);
		return EventTriggersManager.getInstance().addTrigger(getReflection(), triggerId);
	}

	public boolean removeEventTrigger(int triggerId)
	{
		if(!_eventTriggers.remove(triggerId))
			return false;
		if(getReflection().isMain())
			return EventTriggersManager.getInstance().removeTrigger(MapUtils.regionX(getX()), MapUtils.regionY(getY()), triggerId);
		return EventTriggersManager.getInstance().removeTrigger(getReflection(), triggerId);
	}

	public void onSeeSocialAction(Player talker, int actionId)
	{}

	public BuyListTemplate getBuyList(int listId)
	{
		return null;
	}

	public void onChangeClassBypass(Player player, int classId)
	{}

	public void onTeleportRequest(Player talker)
	{
        showTeleportList(talker);
	}

	@Override
	public boolean onTeleported()
	{
		if(!super.onTeleported())
			return false;
		getAI().notifyEvent(CtrlEvent.EVT_TELEPORTED);
		return true;
	}

	static
	{
		_log = LoggerFactory.getLogger(NpcInstance.class);
	}

	public Object getParameter(String str) {
		return _parameters.get(str);
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

	private class QuestInfo implements Comparable<QuestInfo>
	{
		private final Quest quest;
		private final Player player;
		private final boolean isStart;

		public QuestInfo(Quest quest, Player player, boolean isStart)
		{
			this.quest = quest;
			this.player = player;
			this.isStart = isStart;
		}

		public final Quest getQuest()
		{
			return quest;
		}

		public final boolean isStart()
		{
			return isStart;
		}

		@Override
		public int compareTo(QuestInfo info)
		{
			int quest1 = quest.getDescrState(NpcInstance.this, player, isStart);
			int quest2 = info.getQuest().getDescrState(NpcInstance.this, player, isStart);
			int questId1 = quest.getId();
			int questId2 = info.getQuest().getId();
			if(quest1 == 1 && quest2 == 2)
				return 1;
			if(quest1 == 2 && quest2 == 1)
				return -1;
			if(quest1 == 3 && quest2 == 4)
				return 1;
			if(quest1 == 4 && quest2 == 3)
				return -1;
			if(quest1 > quest2)
				return 1;
			if(quest1 < quest2)
				return -1;
			return Integer.compare(questId1, questId2);
		}
	}

	@Override
	public String getName()
	{
		return getTemplate().name;
	}

	@Override
	public String getTitle()
	{
		return getTemplate().title;
	}

	public int getCorpseTime()
	{
		return _corpseTime * 1000;
	}

	public boolean doUnspawnMinions()
	{
		return _unspawnMinions;
	}

	private final int[] teleporters = {
			29055,
			29061,
			30006,
			30059,
			30080,
			30134,
			30146,
			30162,
			30177,
			30233,
			30256,
			30320,
			30427,
			30429,
			30483,
			30484,
			30485,
			30486,
			30487,
			30540,
			30576,
			30716,
			30719,
			30722,
			30727,
			30836,
			30848,
			30878,
			30899,
			31095,
			31096,
			31097,
			31098,
			31099,
			31100,
			31101,
			31102,
			31103,
			31104,
			31105,
			31106,
			31107,
			31108,
			31109,
			31110,
			31111,
			31112,
			31114,
			31115,
			31116,
			31117,
			31118,
			31119,
			31120,
			31121,
			31122,
			31123,
			31124,
			31125,
			31211,
			31212,
			31213,
			31214,
			31215,
			31216,
			31217,
			31218,
			31219,
			31220,
			31221,
			31222,
			31223,
			31224,
			31275,
			31320,
			31376,
			31383,
			31698,
			31699,
			31759,
			31842,
			31843,
			31859,
			31861,
			31865,
			31866,
			31867,
			31868,
			31869,
			31870,
			31871,
			31872,
			31873,
			31874,
			31875,
			31876,
			31877,
			31878,
			31879,
			31880,
			31881,
			31882,
			31883,
			31884,
			31885,
			31886,
			31887,
			31888,
			31889,
			31890,
			31891,
			31892,
			31893,
			31894,
			31895,
			31896,
			31897,
			31898,
			31899,
			31900,
			31901,
			31902,
			31903,
			31904,
			31905,
			31906,
			31907,
			31908,
			31909,
			31910,
			31911,
			31912,
			31913,
			31914,
			31915,
			31916,
			31917,
			31918,
			31964,
			32034,
			32035,
			32036,
			32037,
			32039,
			32040,
			32107,
			35092,
			35093,
			35094,
			35134,
			35135,
			35136,
			35176,
			35177,
			35178,
			35218,
			35219,
			35220,
			35261,
			35262,
			35263,
			35264,
			35265,
			35271,
			35308,
			35309,
			35310,
			35352,
			35353,
			35354,
			35497,
			35498,
			35499,
			35500,
			35501,
			35544,
			35545,
			35546,
			35560,
			35561,
			35562,
			35563,
			35564,
			35565 };

	private boolean isTeleporter()
	{
		return ArrayUtils.contains(teleporters, getNpcId());
	}

	private final int[] merchants = {
			30001,
			30002,
			30003,
			30004,
			30047,
			30060,
			30061,
			30062,
			30063,
			30078,
			30081,
			30082,
			30084,
			30085,
			30087,
			30088,
			30090,
			30091,
			30093,
			30094,
			30097,
			30098,
			30135,
			30136,
			30137,
			30138,
			30147,
			30148,
			30149,
			30150,
			30163,
			30164,
			30165,
			30166,
			30178,
			30179,
			30180,
			30181,
			30207,
			30208,
			30209,
			30230,
			30231,
			30253,
			30254,
			30294,
			30301,
			30313,
			30314,
			30315,
			30321,
			30387,
			30420,
			30436,
			30437,
			30516,
			30517,
			30518,
			30519,
			30558,
			30559,
			30560,
			30561,
			30684,
			30834,
			30837,
			30838,
			30839,
			30840,
			30841,
			30842,
			30879,
			30890,
			30891,
			30892,
			30893,
			30924,
			31044,
			31045,
			31256,
			31257,
			31258,
			31259,
			31260,
			31261,
			31262,
			31263,
			31273,
			31274,
			31284,
			31291,
			31300,
			31301,
			31302,
			31303,
			31304,
			31305,
			31306,
			31307,
			31318,
			31319,
			31338,
			31339,
			31351,
			31366,
			31373,
			31375,
			31380,
			31382,
			31386,
			31413,
			31414,
			31415,
			31416,
			31417,
			31418,
			31419,
			31420,
			31421,
			31422,
			31423,
			31424,
			31425,
			31426,
			31427,
			31428,
			31429,
			31430,
			31431,
			31432,
			31433,
			31434,
			31435,
			31436,
			31437,
			31438,
			31439,
			31440,
			31441,
			31442,
			31443,
			31444,
			31445,
			31666,
			31667,
			31668,
			31669,
			31670,
			31756,
			31757,
			31945,
			31946,
			31947,
			31948,
			31949,
			31950,
			31951,
			31952,
			31962,
			31963,
			31973,
			31980,
			32105,
			32106 };

	private boolean isMerchant()
	{
		return ArrayUtils.contains(merchants, getNpcId());
	}

	private final int[] warehouses = {
			30005,
			30054,
			30055,
			30057,
			30058,
			30079,
			30083,
			30086,
			30092,
			30095,
			30103,
			30104,
			30139,
			30140,
			30151,
			30152,
			30153,
			30169,
			30170,
			30182,
			30183,
			30210,
			30232,
			30255,
			30316,
			30322,
			30350,
			30521,
			30522,
			30562,
			30563,
			30686,
			30843,
			30844,
			30895,
			30896,
			31225,
			31267,
			31268,
			31270,
			31311,
			31312,
			31313,
			31315,
			31374,
			31381,
			31773,
			31956,
			31957,
			31959 };

	private boolean isWarehouse()
	{
		return ArrayUtils.contains(warehouses, getNpcId());
	}

	public static void showFishingSkillList(Player player)
	{
		Collection<SkillLearn> skills = SkillAcquireHolder.getInstance().getAvailableSkills(player, AcquireType.FISHING);

		ExAcquirableSkillListByClass asl = new ExAcquirableSkillListByClass(AcquireType.FISHING, skills.size());

		for(SkillLearn s : skills)
			asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), s.getCost(), 0);

		if(skills.isEmpty())
		{
			player.sendPacket(AcquireSkillDonePacket.STATIC);
			player.sendPacket(SystemMsg.THERE_ARE_NO_OTHER_SKILLS_TO_LEARN);
		}
		else
		{
			player.sendPacket(asl);
		}

		player.sendActionFailed();
	}

	@Override
	public Fraction getFraction()
	{
		return super.getFraction() != null ? super.getFraction() : getTemplate().getFraction();
	}

	@Override
	public boolean isHealBlocked()
	{
		return getTemplate().isHealBlocked() || super.isHealBlocked();
	}

	public boolean dropOnTheGround()
	{
		return getTemplate().dropOnTheGround();
	}
}
