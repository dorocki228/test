package l2s.gameserver.templates.npc;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.ai.CharacterAI;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.instances.RaidBossInstance;
import l2s.gameserver.model.instances.ReflectionBossInstance;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestEventType;
import l2s.gameserver.model.reward.RewardList;
import l2s.gameserver.scripts.Scripts;
import l2s.gameserver.skills.EffectUseType;
import l2s.gameserver.templates.CreatureTemplate;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.TeleportLocation;
import l2s.gameserver.templates.item.data.ResourcesData;
import l2s.gameserver.templates.skill.EffectTemplate;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class NpcTemplate extends CreatureTemplate
{
	private static final Logger _log = LoggerFactory.getLogger(NpcTemplate.class);

	public static final Constructor<?> DEFAULT_TYPE_CONSTRUCTOR = NpcInstance.class.getConstructors()[0];
	public static final Constructor<?> DEFAULT_AI_CONSTRUCTOR = CharacterAI.class.getConstructors()[0];

	private final int _npcId;
	public final String name;
	public final String title;
	public int level;
	public final long rewardExp;
	public final long rewardSp;
	public final int rewardCrp;
	public int aggroRange;
	public final int rhand;
	public final int lhand;
	public final double rateHp;
	private Faction faction;
	public final String jClass;
	public final int displayId;
	private final ShotsType _shots;
	public boolean isRaid;
	private StatsSet _AIParams;
	private int race;
	private final int _castleId;
	private List<RewardList> _rewardList;
	private final TIntObjectMap<List<TeleportLocation>> _teleportList;
	private List<MinionData> _minions;
	private ResourcesData resources = null;

	private Map<QuestEventType, Set<Quest>> _questEvents;
	private final TIntObjectMap<Skill> _skills;
	private Skill[] _damageSkills;
	private Skill[] _dotSkills;
	private Skill[] _debuffSkills;
	private Skill[] _buffSkills;
	private Skill[] _stunSkills;
	private Skill[] _healSkills;
	private Class<?> _classType;

	private Constructor<?> _constructorType;
	private Class<?> _classAI;
	private Constructor<?> _constructorAI;
	private final String _htmRoot;
	private final TIntObjectMap<WalkerRoute> _walkerRoute;
	private RandomActions _randomActions;
	public final int _enchantEffect;
	private final int _baseRandDam;
	private final int _baseReuseDelay;
	private final double _basePHitModify;
	private final double _basePAvoidModify;
	private final double _baseHitTimeFactor;
	private final int _baseSafeHeight;

	private boolean isNoClan;
	private final boolean _isServerSideName;
	private final boolean _isServerSideTitle;
	private final Fraction _fraction;
	private final boolean _isHealBlocked;
	private final boolean _dropOnTheGround;

	public NpcTemplate(StatsSet set)
	{
		super(set);
		faction = Faction.NONE;
		isRaid = false;
		race = 0;
		_rewardList = Collections.emptyList();
		_teleportList = new TIntObjectHashMap<>(1);
		_minions = Collections.emptyList();
		_questEvents = Collections.emptyMap();
		_skills = new TIntObjectHashMap<>();
		_damageSkills = Skill.EMPTY_ARRAY;
		_dotSkills = Skill.EMPTY_ARRAY;
		_debuffSkills = Skill.EMPTY_ARRAY;
		_buffSkills = Skill.EMPTY_ARRAY;
		_stunSkills = Skill.EMPTY_ARRAY;
		_healSkills = Skill.EMPTY_ARRAY;
		_classType = NpcInstance.class;
		_constructorType = DEFAULT_TYPE_CONSTRUCTOR;
		_classAI = CharacterAI.class;
		_constructorAI = DEFAULT_AI_CONSTRUCTOR;
		_walkerRoute = new TIntObjectHashMap<>();
		_randomActions = null;
		_npcId = set.getInteger("npcId");
		displayId = set.getInteger("displayId");
		name = set.getString("name");
		title = set.getString("title");
		level = set.getInteger("level");
		rewardExp = set.getLong("rewardExp", 0);
		rewardSp = set.getLong("rewardSp", 0);
		rewardCrp = set.getInteger("rewardCrp", 0);
		aggroRange = set.getInteger("aggroRange");

		int tempRightHand = set.getInteger("rhand", 0);
		if(tempRightHand != 0 && !ItemHolder.getInstance().isTemplateExist(tempRightHand))
        {
            _log.warn("Can't find right hand weapon {} for npc {}", tempRightHand, _npcId);
            tempRightHand = 0;
        }
        rhand = tempRightHand;
		int tempLeftHand = set.getInteger("lhand", 0);
		if(tempLeftHand != 0 && !ItemHolder.getInstance().isTemplateExist(tempLeftHand))
        {
            _log.warn("Can't find left hand weapon {} for npc {}", tempLeftHand, _npcId);
            tempLeftHand = 0;
        }
        lhand = tempLeftHand;

        rateHp = set.getDouble("baseHpRate");
		jClass = set.getString("texture", null);
		_htmRoot = set.getString("htm_root", null);
		_shots = set.getEnum("shots", ShotsType.class, ShotsType.NONE);
		_castleId = set.getInteger("castle_id", 0);
		_AIParams = (StatsSet) set.getObject("aiParams", StatsSet.EMPTY);
		_enchantEffect = set.getInteger("enchant_effect", 0);
		_baseRandDam = set.getInteger("baseRandDam", 5 + (int) Math.sqrt(level));
		_baseReuseDelay = set.getInteger("baseReuseDelay", 0);
		_basePHitModify = set.getDouble("basePHitModify", 0.0);
		_basePAvoidModify = set.getDouble("basePAvoidModify", 0.0);
		_baseHitTimeFactor = set.getDouble("baseHitTimeFactor", 0.0);
		_baseSafeHeight = set.getInteger("baseSafeHeight", 100);
		_isServerSideName = set.getBool("isServerSideName", false);
		_isServerSideTitle = set.getBool("isServerSideTitle", false);
		_isHealBlocked = set.getBool("isHealBlocked", false);
		_dropOnTheGround = set.getBool("dropOnTheGround", false);
		_fraction = set.getEnum("fraction", Fraction.class, Fraction.NONE);
		setType(set.getString("type", null));
		setAI(set.getString("ai_type", null));
	}

	public Class<?> getInstanceClass()
	{
		return _classType;
	}

	public Constructor<?> getInstanceConstructor()
	{
		return _constructorType;
	}

	public boolean isInstanceOf(Class<?> _class)
	{
		return _class.isAssignableFrom(getInstanceClass());
	}

	public NpcInstance getNewInstance(MultiValueSet<String> set)
	{
		try
		{
			return NpcInstance.class.cast(_constructorType.newInstance(IdFactory.getInstance().getNextId(), this, set));
		}
		catch(Exception e)
		{
			_log.error("Unable to create instance of NPC " + _npcId, e);
			return null;
		}
	}

	public NpcInstance getNewInstance()
	{
		return getNewInstance(StatsSet.EMPTY);
	}

	public CharacterAI getNewAI(NpcInstance npc)
	{
		try
		{
			return CharacterAI.class.cast(_constructorAI.newInstance(npc));
		}
		catch(Exception e)
		{
			_log.error("Unable to create ai of NPC " + _npcId, e);
			return new CharacterAI(npc);
		}
	}

	public Class<?> getClassType()
	{
		return _classType;
	}
	
	protected void setType(String type)
	{
		Class<?> classType;
		try
		{
			classType = Class.forName("l2s.gameserver.model.instances." + type + "Instance");
		}
		catch(ClassNotFoundException e)
		{
			classType = Scripts.getInstance().getClasses().get("npc.model." + type + "Instance");
			if(classType == null)
				classType = Scripts.getInstance().getClasses().get(type + "Instance");
		}
		if(classType == null)
			_log.error("Not found type class for type: " + type + ". NpcId: " + _npcId);
		if(_npcId == 0)
		{
			try
			{
				classType = Class.forName("l2s.gameserver.model.instances.NpcInstance");
			}
			catch(ClassNotFoundException ex)
			{}
			_classType = classType;
			_constructorType = _classType.getConstructors()[0];
		}
		else
		{
			_classType = classType;
			_constructorType = _classType.getConstructors()[0];
		}
		if(_classType.isAnnotationPresent(Deprecated.class))
			_log.error("Npc type: " + type + ", is deprecated. NpcId: " + _npcId);
		isRaid = isInstanceOf(RaidBossInstance.class) && !isInstanceOf(ReflectionBossInstance.class);
	}

	protected void setAI(String ai)
	{
		Class<?> classAI;
		try
		{
			classAI = Class.forName("l2s.gameserver.ai." + ai);
		}
		catch(ClassNotFoundException e)
		{
			classAI = Scripts.getInstance().getClasses().get("ai." + ai);

			if(classAI == null)
				classAI = Scripts.getInstance().getClasses().get(ai);
		}
		if(classAI == null)
			_log.error("Not found ai class for ai: " + ai + ". NpcId: " + _npcId);
		if(_npcId == 0)
		{
			try
			{
				classAI = Class.forName("l2s.gameserver.ai.NpcAI");
			}
			catch(ClassNotFoundException ex)
			{}
			_classAI = classAI;
			_constructorAI = _classAI.getConstructors()[0];
		}
		else
		{
			_classAI = classAI;
			_constructorAI = _classAI.getConstructors()[0];
		}
		if(_classAI.isAnnotationPresent(Deprecated.class))
			_log.error("Ai type: " + ai + ", is deprecated. NpcId: " + _npcId);
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
			_rewardList = new CopyOnWriteArrayList<>();
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
			_minions = new ArrayList<>(1);
		_minions.add(minion);
	}

	public void setResources(ResourcesData resources)
	{
		this.resources = resources;
	}

	public ResourcesData getResources()
	{
		return resources;
	}

	public void setFaction(Faction faction)
	{
		this.faction = faction;
	}

	public Faction getFaction()
	{
		return faction;
	}

	public void addSkill(Skill skill)
	{
		_skills.put(skill.getId(), skill);
		if(skill.isNotUsedByAI() || skill.getTargetType() == Skill.SkillTargetType.TARGET_NONE || skill.getSkillType() == Skill.SkillType.NOTDONE || !skill.isActive())
			return;
		switch(skill.getSkillType())
		{
			case PDAM:
			case MANADAM:
			case MDAM:
			case DRAIN:
			case DRAIN_SOUL:
			{
				boolean added = false;
				for(EffectTemplate eff : skill.getEffectTemplates(EffectUseType.NORMAL))
					switch(eff.getEffectType())
					{
						case Stun:
						{
							_stunSkills = (Skill[]) ArrayUtils.add((Object[]) _stunSkills, skill);
							added = true;
							continue;
						}
						case t_hp:
						{
							if(eff.getValue() < 0.0)
							{
								_dotSkills = (Skill[]) ArrayUtils.add((Object[]) _dotSkills, skill);
								added = true;
								continue;
							}
							continue;
						}
						case ManaDamOverTime:
						case LDManaDamOverTime:
						{
							_dotSkills = (Skill[]) ArrayUtils.add((Object[]) _dotSkills, skill);
							added = true;
							continue;
						}
					}
				if(!added)
				{
					_damageSkills = (Skill[]) ArrayUtils.add((Object[]) _damageSkills, skill);
					break;
				}
				break;
			}
			case DOT:
			case MDOT:
			case POISON:
			{
				_dotSkills = (Skill[]) ArrayUtils.add((Object[]) _dotSkills, skill);
				break;
			}
			case DEBUFF:
			case SLEEP:
			case ROOT:
			case PARALYZE:
			case MUTE:
			case AGGRESSION:
			{
				_debuffSkills = (Skill[]) ArrayUtils.add((Object[]) _debuffSkills, skill);
				break;
			}
			case BUFF:
			{
				_buffSkills = (Skill[]) ArrayUtils.add((Object[]) _buffSkills, skill);
				break;
			}
			case STUN:
			{
				_stunSkills = (Skill[]) ArrayUtils.add((Object[]) _stunSkills, skill);
				break;
			}
			case HEAL:
			case HEAL_PERCENT:
			case HOT:
			{
				_healSkills = (Skill[]) ArrayUtils.add((Object[]) _healSkills, skill);
				break;
			}
		}
	}

	public Skill[] getDamageSkills()
	{
		return _damageSkills;
	}

	public Skill[] getDotSkills()
	{
		return _dotSkills;
	}

	public Skill[] getDebuffSkills()
	{
		return _debuffSkills;
	}

	public Skill[] getBuffSkills()
	{
		return _buffSkills;
	}

	public Skill[] getStunSkills()
	{
		return _stunSkills;
	}

	public Skill[] getHealSkills()
	{
		return _healSkills;
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
			_questEvents = new HashMap<>();
        Set<Quest> quests = _questEvents.computeIfAbsent(eventType, k -> new HashSet<>());
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

	public final String getJClass()
	{
		return jClass;
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

	@Override
	public int getBaseRandDam()
	{
		return _baseRandDam;
	}

	public int getBaseReuseDelay()
	{
		return _baseReuseDelay;
	}

	public boolean isNoClan()
	{
		return isNoClan;
	}

	public void setNoClan(boolean noClan)
	{
		isNoClan = noClan;
	}

	public enum ShotsType
	{
		NONE,
		SOUL,
		SPIRIT,
		BSPIRIT,
		SOUL_SPIRIT,
		SOUL_BSPIRIT
    }

	public boolean isServerSideName()
	{
		return _isServerSideName;
	}

	public boolean isServerSideTitle()
	{
		return _isServerSideTitle;
	}

	public Fraction getFraction()
	{
		return _fraction;
	}

	public boolean isHealBlocked()
	{
		return _isHealBlocked;
	}

	public boolean dropOnTheGround()
	{
		return _dropOnTheGround;
	}
}
