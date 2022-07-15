package l2s.gameserver.templates.npc;

import com.google.common.flogger.FluentLogger;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.collections.MultiValueSet;
import l2s.commons.util.ReflectionUtils;
import l2s.gameserver.ai.NpcAI;
import l2s.gameserver.handler.effects.impl.instant.retail.*;
import l2s.gameserver.handler.effects.impl.pump.retail.*;
import l2s.gameserver.handler.effects.impl.tick.t_hp;
import l2s.gameserver.handler.effects.impl.tick.t_hp_fatal;
import l2s.gameserver.handler.effects.impl.tick.t_hp_magic;
import l2s.gameserver.handler.effects.impl.tick.t_mp;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.listener.hooks.ListenerHook;
import l2s.gameserver.listener.hooks.ListenerHookType;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.base.AISkillScope;
import l2s.gameserver.model.base.ElementalElement;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.instances.RaidBossInstance;
import l2s.gameserver.model.instances.ReflectionBossInstance;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestEventType;
import l2s.gameserver.model.reward.RewardList;
import l2s.gameserver.scripts.Scripts;
import l2s.gameserver.stats.DoubleStat;
import l2s.gameserver.templates.CreatureTemplate;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.TeleportLocation;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class NpcTemplate extends CreatureTemplate
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	@SuppressWarnings("unchecked")
	public static final Constructor<NpcInstance> DEFAULT_TYPE_CONSTRUCTOR = (Constructor<NpcInstance>) NpcInstance.class.getConstructors()[0];
	@SuppressWarnings("unchecked")
	public static final Constructor<NpcAI> DEFAULT_AI_CONSTRUCTOR = (Constructor<NpcAI>) NpcAI.class.getConstructors()[0];

	private static final Map<String, Constructor<NpcAI>> AI_CONSTRUCTORS = new HashMap<String, Constructor<NpcAI>>();

	public static enum ShotsType
	{
		NONE,
		SOUL,
		SPIRIT,
		BSPIRIT,
		SOUL_SPIRIT,
		SOUL_BSPIRIT
	}

	private final int _npcId;

	public final String name;
	public final String title;
	// не используется - public final String sex;
	public int level;
	public final long rewardExp;
	public final long rewardSp;
	public int aggroRange;
	public final int rhand;
	public final int lhand;
	public final double rateHp;

	private Faction faction = Faction.NONE;

	public final int displayId;

	private final ShotsType _shots;

	public boolean isRaid = false;
	private StatsSet _AIParams;

	/** fixed skills*/
	private int race = 0;
	private final int _castleId;

	private List<RewardList> _rewardList = Collections.emptyList();

	private TIntObjectMap<List<TeleportLocation>> _teleportList = new TIntObjectHashMap<List<TeleportLocation>>(1);
	private List<MinionData> _minions = Collections.emptyList();

	private Map<QuestEventType, Set<Quest>> _questEvents = Collections.emptyMap();
	private TIntObjectMap<Skill> _skills = new TIntObjectHashMap<Skill>();
	private Map<AISkillScope, List<Skill>> aiSkillLists = new HashMap<>();

	private Class<NpcInstance> _classType = NpcInstance.class;
	private Constructor<NpcInstance> _constructorType = DEFAULT_TYPE_CONSTRUCTOR;

	private final String _htmRoot;

	private TIntObjectMap<WalkerRoute> _walkerRoute = new TIntObjectHashMap<WalkerRoute>();
	private RandomActions _randomActions = null;

	public final int _enchantEffect;

	private final int _baseReuseDelay;
	@SuppressWarnings("unused")
	private final double _basePHitModify;
	@SuppressWarnings("unused")
	private final double _basePAvoidModify;
	@SuppressWarnings("unused")
	private final double _baseHitTimeFactor;
	@SuppressWarnings("unused")
	private final int _baseSafeHeight;

	private final String _aiType;

	private final ElementalElement _elementalElement;

	/**
	 * Constructor<?> of L2Character.<BR><BR>
	 * @param set The StatsSet object to transfer data to the method
	 */
	public NpcTemplate(StatsSet set)
	{
		super(set);
		_npcId = set.getInteger("npcId");
		displayId = set.getInteger("displayId");

		name = set.getString("name");
		title = set.getString("title");
		// sex = set.getString("sex");
		level = set.getInteger("level");
		rewardExp = set.getLong("rewardExp");
		rewardSp = set.getLong("rewardSp");
		aggroRange = set.getInteger("aggroRange");
		rhand = set.getInteger("rhand", 0);
		lhand = set.getInteger("lhand", 0);
		rateHp = set.getDouble("baseHpRate");
		_htmRoot = set.getString("htm_root", null);
		_shots = set.getEnum("shots", ShotsType.class, ShotsType.NONE);
		_castleId = set.getInteger("castle_id", 0);
		_AIParams = (StatsSet) set.getObject("aiParams", StatsSet.EMPTY);
		_enchantEffect = set.getInteger("enchant_effect", 0);

		addBaseValue(DoubleStat.RANDOM_DAMAGE, set.getOptionalDouble("baseRandDam", 5 + (int) Math.sqrt(level)));

		_baseReuseDelay = set.getInteger("baseReuseDelay", 0);
		_basePHitModify = set.getDouble("basePHitModify", 0);
		_basePAvoidModify = set.getDouble("basePAvoidModify", 0);
		_baseHitTimeFactor = set.getDouble("baseHitTimeFactor", 0);
		_baseSafeHeight = set.getInteger("baseSafeHeight", 100);

		_elementalElement = set.getEnum("elemental_element", ElementalElement.class, ElementalElement.NONE);

		setType(set.getString("type", null));

		_aiType = set.getString("ai_type", "NpcAI");
	}

	public Class<? extends NpcInstance> getInstanceClass()
	{
		return _classType;
	}

	public Constructor<? extends NpcInstance> getInstanceConstructor()
	{
		return _constructorType;
	}

	public boolean isInstanceOf(Class<?> _class)
	{
		return _class.isAssignableFrom(getInstanceClass());
	}

	/**
	 * Создает новый инстанс NPC. Для него следует вызывать (именно в этом порядке):
	 * <br> setSpawnedLoc (обязательно)
	 * <br> setReflection (если reflection не базовый)
	 * <br> setChampion (опционально)
	 * <br> setCurrentHpMp (если вызывался setChampion)
	 * <br> spawnMe (в качестве параметра брать getSpawnedLoc)
	 */
	public NpcInstance getNewInstance(MultiValueSet<String> set)
	{
		try
		{
			return _constructorType.newInstance(IdFactory.getInstance().getNextId(), this, set);
		}
		catch(Exception e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "Unable to create instance of NPC %s", _npcId );
		}

		return null;
	}

	public NpcInstance getNewInstance()
	{
		return getNewInstance(StatsSet.EMPTY);
	}

	@SuppressWarnings("unchecked")
	public NpcAI getNewAI(NpcInstance npc)
	{
		String ai = npc.getParameter("ai_type", _aiType);

		Constructor<NpcAI> constructorAI = AI_CONSTRUCTORS.get(ai);
		if(constructorAI == null)
		{
			Class<NpcAI> classAI = null;
			try
			{
				classAI = (Class<NpcAI>) Class.forName("l2s.gameserver.ai." + ai);
			}
			catch(ClassNotFoundException e)
			{
				classAI = (Class<NpcAI>) Scripts.getInstance().getClasses().get("ai." + ai);
			}

			if(classAI == null)
			{
				classAI = NpcAI.class;
				_log.atSevere().log( "Not found ai class for ai: %s. NpcId: %s", ai, npc.getNpcId() );
			}

			constructorAI = (Constructor<NpcAI>) classAI.getConstructors()[0];

			if(classAI.isAnnotationPresent(Deprecated.class))
				_log.atSevere().log( "Ai type: %s, is deprecated. NpcId: %s", ai, npc.getNpcId() );

			AI_CONSTRUCTORS.put(ai, constructorAI);
		}

		try
		{
			return constructorAI.newInstance(npc);
		}
		catch(Exception e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "Unable to create ai of NPC %s", npc.getNpcId() );
		}

		return new NpcAI(npc);
	}

	@SuppressWarnings("unchecked")
	protected void setType(String type)
	{
		Class<NpcInstance> classType = (Class<NpcInstance>) Scripts.getInstance().getClasses().get("npc.model." + type + "Instance");

		try
		{
			classType = (Class<NpcInstance>) Class.forName("l2s.gameserver.model.instances." + type + "Instance");
		}
		catch(ClassNotFoundException e)
		{
			if (classType == null) {
				classType = (Class<NpcInstance>) ReflectionUtils.INSTANCE.findClassByName(type + "Instance");
			}
		}

		if(classType == null)
			_log.atSevere().log("Not found type class for type: %s. NpcId: %s", type, _npcId);

		if(_npcId == 0) //temp
		{
			try
			{
				classType = (Class<NpcInstance>) Class.forName("l2s.gameserver.model.instances.NpcInstance");
			}
			catch(ClassNotFoundException e)
			{}

			_classType = classType;
			_constructorType = (Constructor<NpcInstance>)_classType.getConstructors()[0];
		}
		else
		{
			_classType = classType;
			_constructorType = (Constructor<NpcInstance>) _classType.getConstructors()[0];
		}

		if(_classType.isAnnotationPresent(Deprecated.class))
			_log.atSevere().log( "Npc type: %s, is deprecated. NpcId: %s", type, _npcId );

		//TODO [G1ta0] сделать поле в соотвествующих классах
		isRaid = isInstanceOf(RaidBossInstance.class) && !isInstanceOf(ReflectionBossInstance.class);
	}

	public void addTeleportList(int id, List<TeleportLocation> list)
	{
		_teleportList.put(id, list);
	}

	public List<TeleportLocation> getTeleportList(int id)
	{
		return _teleportList.get(id);
	}

	public TIntObjectMap<List<TeleportLocation>> getTeleportList()
	{
		return _teleportList;
	}

	public void addRewardList(RewardList rewardList)
	{
		if(_rewardList.isEmpty())
			_rewardList = new CopyOnWriteArrayList<RewardList>();

		_rewardList.add(rewardList);
	}

	public void removeRewardList(RewardList rewardList)
	{
		_rewardList.remove(rewardList);
	}

	public Collection<RewardList> getRewards()
	{
		return _rewardList;
	}

	public void addMinion(MinionData minion)
	{
		if(_minions.isEmpty())
			_minions = new ArrayList<MinionData>(1);

		_minions.add(minion);
	}

	public void setFaction(Faction faction)
	{
		this.faction = faction;
	}

	public Faction getFaction()
	{
		return faction;
	}

	public void addSkill(Skill skill) {
		_skills.put(skill.getId(), skill);

		//TODO [G1ta0] перенести в AI
		if(skill.isNotUsedByAI() || !skill.isActive()) {
			return;
		}

		List<AISkillScope> aiSkillScopes = new ArrayList<>();
		final AISkillScope shortOrLongRangeScope = skill.getCastRange() <= 150 ? AISkillScope.SHORT_RANGE : AISkillScope.LONG_RANGE;
		if (skill.isSuicideAttack()) {
			aiSkillScopes.add(AISkillScope.DAMAGE);
			aiSkillScopes.add(AISkillScope.SUICIDE);
		} else {
			aiSkillScopes.add(AISkillScope.GENERAL);

			if (skill.isContinuous()) {
				if (!skill.isDebuff()) {
					aiSkillScopes.add(AISkillScope.BUFF);
				} else {
					aiSkillScopes.add(AISkillScope.COT);
					aiSkillScopes.add(shortOrLongRangeScope);
				}
			} else {
				if (skill.hasEffect(i_dispel_all.class,
						i_dispel_by_category.class,
						i_dispel_by_slot_probability.class)) {
					aiSkillScopes.add(AISkillScope.COT);
					aiSkillScopes.add(shortOrLongRangeScope);
				} else if (skill.hasEffect(i_dispel_by_slot.class,
						i_dispel_by_slot_myself.class)) {
					aiSkillScopes.add(AISkillScope.COT);
					aiSkillScopes.add(shortOrLongRangeScope);
				} else if (skill.hasEffect(t_hp.class,
						t_hp_fatal.class,
						t_hp_magic.class,
						t_mp.class)) {
					aiSkillScopes.add(AISkillScope.DOT);
					aiSkillScopes.add(shortOrLongRangeScope);
				} else if (skill.hasEffect(i_backstab.class,
						i_energy_attack.class,
						i_fatal_blow.class,
						i_p_attack.class,
						i_p_attack_over_hit.class)) {
					aiSkillScopes.add(AISkillScope.DAMAGE);
					aiSkillScopes.add(shortOrLongRangeScope);
				} else if (skill.hasEffect(i_physical_attack_hp_link.class)) {
					aiSkillScopes.add(AISkillScope.DAMAGE);
					aiSkillScopes.add(shortOrLongRangeScope);
				} else if (skill.hasEffect(i_m_attack.class,
						i_m_attack_over_hit.class,
						i_m_attack_by_abnormal.class,
						i_m_attack_by_abnormal_slot.class,
						i_m_attack_by_dist.class,
						i_m_attack_mp.class,
						i_m_attack_range.class)) {
					aiSkillScopes.add(AISkillScope.DAMAGE);
					aiSkillScopes.add(shortOrLongRangeScope);
				} else if (skill.hasEffect(i_death_link.class)) {
					aiSkillScopes.add(AISkillScope.DAMAGE);
					aiSkillScopes.add(shortOrLongRangeScope);
				} else if (skill.hasEffect(i_hp_drain.class)) {
					aiSkillScopes.add(AISkillScope.DAMAGE);
					aiSkillScopes.add(shortOrLongRangeScope);
				} else if (skill.hasEffect(i_heal.class,
						i_heal_special.class,
						i_hp_per_max.class)) {
					aiSkillScopes.add(AISkillScope.HEAL);
				} else if (skill.hasEffect(p_block_act.class,
						p_condition_block_act_item.class,
						p_condition_block_act_skill.class)) {
					aiSkillScopes.add(AISkillScope.IMMOBILIZE);
					aiSkillScopes.add(shortOrLongRangeScope);
				} else if (skill.hasEffect(p_block_spell.class)) {
					aiSkillScopes.add(AISkillScope.COT);
					aiSkillScopes.add(shortOrLongRangeScope);
				} else if (skill.hasEffect(p_block_controll.class)) {
					aiSkillScopes.add(AISkillScope.COT);
					aiSkillScopes.add(shortOrLongRangeScope);
				}
			}
		}

		for (AISkillScope aiSkillScope : aiSkillScopes) {
			aiSkillLists.computeIfAbsent(aiSkillScope, k -> new ArrayList<>()).add(skill);
		}
	}

	public List<Skill> getAISkills(AISkillScope aiSkillScope) {
		return aiSkillLists.getOrDefault(aiSkillScope, Collections.emptyList());
	}

	public List<MinionData> getMinionData()
	{
		return _minions;
	}

	public TIntObjectMap<Skill> getSkills()
	{
		return _skills;
	}

	public void addQuestEvent(QuestEventType eventType, Quest quest)
	{
		if(_questEvents.isEmpty())
			_questEvents = new HashMap<QuestEventType, Set<Quest>>();

		Set<Quest> quests = _questEvents.get(eventType);
		if(quests == null)
		{
			quests = new HashSet<Quest>();
			_questEvents.put(eventType, quests);
		}
		quests.add(quest);
	}

	public Set<Quest> getEventQuests(QuestEventType eventType)
	{
		return _questEvents.get(eventType);
	}

	public int getRace()
	{
		return race;
	}

	public void setRace(int newrace)
	{
		race = newrace;
	}

	public boolean isUndead()
	{
		return race == 1;
	}

	@Override
	public String toString()
	{
		return "Npc template " + name + "[" + _npcId + "]";
	}

	@Override
	public int getId()
	{
		return _npcId;
	}

	public String getName()
	{
		return name;
	}
	
	public ShotsType getShots()
	{
		return _shots;
	}

	public final StatsSet getAIParams()
	{
		return _AIParams;
	}

	public final void setAIParam(String name, Object value)
	{
		if(_AIParams == StatsSet.EMPTY)
			_AIParams = new StatsSet();
		_AIParams.set(name, value);
	}

	public int getCastleId()
	{
		return _castleId;
	}

	public Map<QuestEventType, Set<Quest>> getQuestEvents()
	{
		return _questEvents;
	}

	public String getHtmRoot()
	{
		return _htmRoot;
	}

	public void addWalkerRoute(WalkerRoute walkerRoute)
	{
		if(!walkerRoute.isValid())
			return;

		_walkerRoute.put(walkerRoute.getId(), walkerRoute);
	}

	public WalkerRoute getWalkerRoute(int id)
	{
		return _walkerRoute.get(id);
	}

	public void setRandomActions(RandomActions randomActions)
	{
		_randomActions = randomActions;
	}

	public RandomActions getRandomActions()
	{
		return _randomActions;
	}

	public int getEnchantEffect()
	{
		return _enchantEffect;
	}

	public int getBaseReuseDelay()
	{
		return _baseReuseDelay;
	}

	public ElementalElement getElementalElement()
	{
		return _elementalElement;
	}

	//------------------------------------------------------------------------------------------------------------------

	private final Map<ListenerHookType, Set<ListenerHook>> _listenerHooks = new HashMap<ListenerHookType, Set<ListenerHook>>();

	public void addListenerHook(ListenerHookType type, ListenerHook hook)
	{
		Set<ListenerHook> hooks = _listenerHooks.get(type);
		if(hooks == null)
		{
			hooks = new HashSet<ListenerHook>();
			_listenerHooks.put(type, hooks);
		}
		hooks.add(hook);
	}

	public Set<ListenerHook> getListenerHooks(ListenerHookType type)
	{
		Set<ListenerHook> hooks = _listenerHooks.get(type);
		if(hooks == null)
			return Collections.emptySet();
		return hooks;
	}

	private boolean isNoClan;

	public boolean isNoClan()
	{
		return isNoClan;
	}

	public void setNoClan(boolean noClan)
	{
		isNoClan = noClan;
	}
}