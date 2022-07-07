package l2s.Phantoms;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.Phantoms.ai.abstracts.PhantomDefaultAI;
import l2s.Phantoms.ai.chat.ChatBoot;
import l2s.Phantoms.ai.individual.DefaultFighter;
import l2s.Phantoms.ai.individual.DefaultMage;
import l2s.Phantoms.ai.individual.Other;
import l2s.Phantoms.ai.individual.DarkElf.GhostHunter;
import l2s.Phantoms.ai.individual.DarkElf.GhostSentinel;
import l2s.Phantoms.ai.individual.DarkElf.ShilienSaint;
import l2s.Phantoms.ai.individual.DarkElf.ShilienTemplar;
import l2s.Phantoms.ai.individual.DarkElf.SpectralDancer;
import l2s.Phantoms.ai.individual.DarkElf.SpectralMaster;
import l2s.Phantoms.ai.individual.DarkElf.StormScreamer;
import l2s.Phantoms.ai.individual.Dwarf.FortuneSeeker;
import l2s.Phantoms.ai.individual.Dwarf.Maestro;
import l2s.Phantoms.ai.individual.Elf.ElementalMaster;
import l2s.Phantoms.ai.individual.Elf.EvaSaint;
import l2s.Phantoms.ai.individual.Elf.EvaTemplar;
import l2s.Phantoms.ai.individual.Elf.MoonlightSentinel;
import l2s.Phantoms.ai.individual.Elf.MysticMuse;
import l2s.Phantoms.ai.individual.Elf.SwordMuse;
import l2s.Phantoms.ai.individual.Elf.WindRider;
import l2s.Phantoms.ai.individual.Orc.Dominator;
import l2s.Phantoms.ai.individual.Orc.Doomcryer;
import l2s.Phantoms.ai.individual.Orc.GrandKhauatari;
import l2s.Phantoms.ai.individual.Orc.Titan;
import l2s.Phantoms.ai.individual.human.Adventurer;
import l2s.Phantoms.ai.individual.human.ArcanaLord;
import l2s.Phantoms.ai.individual.human.Archmage;
import l2s.Phantoms.ai.individual.human.Cardinal;
import l2s.Phantoms.ai.individual.human.Dreadnought;
import l2s.Phantoms.ai.individual.human.Duelist;
import l2s.Phantoms.ai.individual.human.HellKnight;
import l2s.Phantoms.ai.individual.human.Hierophant;
import l2s.Phantoms.ai.individual.human.PhoenixKnight;
import l2s.Phantoms.ai.individual.human.Saggittarius;
import l2s.Phantoms.ai.individual.human.Soultaker;
import l2s.Phantoms.ai.merchants.Merchants;
import l2s.Phantoms.ai.tasks.other.DeadPhantomActionTask;
import l2s.Phantoms.ai.tasks.other.EndPeaceCooldownTask;
import l2s.Phantoms.ai.tasks.other.MoveToGkTask;
import l2s.Phantoms.enums.Behavior;
import l2s.Phantoms.enums.Condition;
import l2s.Phantoms.enums.PartyState;
import l2s.Phantoms.listener.CastCleansingSkills;
import l2s.Phantoms.listener.PhantomAddSkillEffectListener;
import l2s.Phantoms.listener.PhantomAttacked;
import l2s.Phantoms.listener.PhantomOnCreatureAttacked;
import l2s.Phantoms.listener.StopEffect;
import l2s.Phantoms.objects.PCondition;
import l2s.Phantoms.objects.PhantomClassAI;
import l2s.Phantoms.objects.PhantomPartyObject;
import l2s.Phantoms.objects.TrafficScheme.PhantomRoute;
import l2s.Phantoms.parsers.Trade.TradePhantom;
import l2s.Phantoms.templates.PhantomSkill;
import l2s.Phantoms.templates.SkillsGroup;
import l2s.commons.threading.RunnableImpl;
import l2s.commons.util.Rnd;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.actor.instances.creature.AbnormalList;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.skills.EffectUseType;
import l2s.gameserver.templates.skill.EffectTemplate;
import l2s.gameserver.utils.Location;

public class PhantomParams
{
	protected final Logger _log = LoggerFactory.getLogger(PhantomParams.class);
	private Player actor;

	private final PhantomAddSkillEffectListener addSkilleffect = new CastCleansingSkills();
	//private final OnLevelUpListener phantomOnLevelUp = new PhantomLvlUp();
	private final PhantomAttacked _attacked = new PhantomAttacked();
	//private final PhantomNewSummon _newSummon = new PhantomNewSummon();

	private final StopEffect _stopEff = new StopEffect();
	private final PhantomOnCreatureAttacked onCreatureAttacked = new PhantomOnCreatureAttacked();

	private final Map<Integer, Long> ignorelist;
	private final Map<Integer, Long> items_delay_list;

	public PhantomParams(Player player)
	{
		actor = player;
		chat_list = new HashMap<Integer, ChatBoot>();
		items_delay_list = new HashMap<Integer, Long>();
		ignorelist = new HashMap<Integer, Long>();
	}

	private PhantomDefaultAI _phantomAi;
	private PhantomPartyObject _phantomPartyAi;

	private long _lastClanNotifyTime = 0;
	private long _minClanNotifyInterval = 10000;

	private boolean _need_rebuff = false;
	private boolean _isResurrecting = false;
	private boolean _isPk = false;
	private boolean _isPvP = false;

	private Creature _lockedTarget = null; // цель за которой гоняется бот

	private Creature _lockedHealerTarget = null; // цель для хилов
	private Creature _resTarget = null; // цель для реса

	private Creature _nextlockedTarget = null; // следующая (запланированная) цель
	private Creature _subTarget = null; // вторичная цель (для дебафа сапортом и т.д.)

	private long _ChatTradeTime = 0;
	// private Creature _attacker = null; // цель которая атакует бота
	private TradePhantom trade_list = null; // трейдлист для трейдботов
	private Location _lockedTargetFirstLocation = null; // местоположение цели, в момент выбора её в таргет
	private int peace_cooldown = 0; // время в секундах, которое фантом находится в городе после смерти
	private boolean _Stop = false;
	private boolean _follow = false;
	private boolean Party_Invite = false;
	private int _comeback_distance;

	private int zone_id; // использовано для пати в инсты = ид инстанса
	private String[] textPartyRoom;
	private boolean chat_answer = false;

	private double chanceOlyCast = 1.3;

	private int _PauseBtarget = 40; // рандом выбора цели

	private int _RndDelayAi = 10;
	private long delayNewTarget = 0;

	int p1 = 70;

	public void setp1(int _p1)
	{
		p1 = _p1;
	}

	public int getp1()
	{
		return p1;
	}

	int p2 = 250;

	public void setp2(int _p2)
	{
		p2 = _p2;
	}

	public int getp2()
	{
		return p2;
	}

	int p3 = 379;

	public void setp3(int _p3)
	{
		p3 = _p3;
	}

	public int getp3()
	{
		return p3;
	}

	int p4 = 50;

	public void setp4(int _p4)
	{
		p4 = _p4;
	}

	public int getp4()
	{
		return p4;
	}

	private Behavior behavior = Behavior.ATTACK;

	public Behavior getBehavior()
	{
		return behavior;
	}

	public void changeBehavior(Behavior b)
	{
		behavior = b;
	}

	public int getPauseBtarget()
	{
		return _PauseBtarget;
	}

	public void setPauseBtarget(int PauseBtarget)
	{
		_PauseBtarget = PauseBtarget;
	}

	public void setLastClanNotifyTime(long lastClanNotifyTime)
	{
		_lastClanNotifyTime = lastClanNotifyTime;
	}

	public long getLastClanNotifyTime()
	{
		return _lastClanNotifyTime;
	}

	public long getMinClanNotifyInterval()
	{
		return _minClanNotifyInterval;
	}

	private ScheduledFuture<?> _СhangeClothesTask = null;

	public void abortСhangeClothesTask()
	{
		if(_СhangeClothesTask != null)
		{
			_СhangeClothesTask.cancel(true);
			_СhangeClothesTask = null;
		}
	}

	public void startСhangeClothesTask(long delay, Runnable r)
	{
		try
		{
			abortСhangeClothesTask();
			_СhangeClothesTask = ThreadPoolManager.getInstance().PhantomOtherSchedule(r, delay);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public ScheduledFuture<?> getСhangeClothesTask()
	{
		return _СhangeClothesTask;
	}

	private ScheduledFuture<?> _DeadPhantomActionTask = null;

	public void abortDeadPhantomActionTask()
	{
		if(_DeadPhantomActionTask != null)
		{
			_DeadPhantomActionTask.cancel(true);
			_DeadPhantomActionTask = null;
		}
	}

	public void startDeadPhantomActionTask(long delay)
	{
		try
		{
			abortDeadPhantomActionTask();
			_DeadPhantomActionTask = ThreadPoolManager.getInstance().PhantomOtherSchedule(new DeadPhantomActionTask(actor), delay);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public ScheduledFuture<?> getDeadPhantomActionTask()
	{
		return _DeadPhantomActionTask;
	}

	public Map<Integer, Long> getItemsDelayList()
	{
		return items_delay_list;
	}

	public void deleteItemDelay(Integer item_id)
	{
		items_delay_list.remove(item_id);
	}

	public void addItemDelay(Integer item_id, Long delay)
	{
		items_delay_list.put(item_id, delay);
	}

	public String[] getTextPartyRoom()
	{
		return textPartyRoom;
	}

	public void setTextPartyRoom(String[] text)
	{
		textPartyRoom = text;
	}

	public boolean getСhatAnswer()
	{
		return chat_answer;
	}

	public void setСhatAnswer(boolean i)
	{
		chat_answer = i;
	}

	public int getIdZone()
	{
		return zone_id;
	}

	public void setZoneId(int i)
	{
		zone_id = i;
	}

	private boolean male = true;

	public boolean isMale()
	{
		return male;
	}

	public void setMale(boolean b)
	{
		male = b;
	}

	public Map<Integer, ChatBoot> chat_list;

	public Map<Integer, ChatBoot> getChatList()
	{
		return chat_list;
	}

	public ChatBoot getChat(int oid)
	{
		return chat_list.get(oid);
	}

	public ChatBoot addChat(int oid, ChatBoot Chat)
	{
		return chat_list.put(oid, Chat);
	}

	public boolean getPartyInvite()
	{
		return Party_Invite;
	}

	public void setPartyInvite(boolean b)
	{
		Party_Invite = b;
	}

	public void addIgnore(int i, long time)
	{
		ignorelist.put(i, time);
	}

	public Map<Integer, Long> getIgnoreList()
	{
		return ignorelist;
	}

	public void clearIgnoreList()
	{
		ignorelist.clear();
	}

	private boolean gm_log = false;

	public void setGmLog(boolean b)
	{
		gm_log = b;
	}

	public boolean getGmLog()
	{
		return gm_log;
	}

	private PhantomRoute trafficScheme = null;

	/*
	 * режим передвижения за целью используется фантомами рабами и пати фантомами
	 */
	public void setfollow(boolean b)
	{
		_follow = b;
	}

	public boolean getfollow()
	{
		return _follow;
	}

	public void setStop(boolean b)
	{
		_Stop = b;
	}

	public boolean getActivity()
	{
		return _Stop;
	}

	public void setPeaceCooldown(int val)
	{
		peace_cooldown = val;
	}

	public int getPeaceCooldown()
	{
		return peace_cooldown + Rnd.get(2,10);
	}

	public void setLockedHealerTarget(Creature target)
	{
		try
		{
			_lockedHealerTarget = target;
			actor.setTarget(target);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public Creature getLockedHealerTarget()
	{
		return _lockedHealerTarget;
	}

	public void setLockedTarget(Creature target)
	{
		try
		{
			_lockedTarget = target;
			actor.setTarget(target);
			if(target != null)
				_lockedTargetFirstLocation = target.getLoc();
			else
				_lockedTargetFirstLocation = null;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public Creature getLockedTarget()
	{
		return _lockedTarget;
	}

	public void setNextLockedTarget(Creature target)
	{
		try
		{
			_nextlockedTarget = target;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public Creature getNextLockedTarget()
	{
		return _nextlockedTarget;
	}

	public void setSubTarget(Creature target)
	{
		_subTarget = target;
	}

	public Creature getSubTarget()
	{
		return _subTarget;
	}

	/*
	 * public void setAttackerTarget(Creature target) { _attacker = target; } public Creature getAttackerTarget() { return _attacker; }
	 */
	public Location getLockedTargetFirstLocation()
	{
		return _lockedTargetFirstLocation;
	}

	public void setIsResurrecting(boolean val)
	{
		_isResurrecting = val;
	}

	public boolean isResurrecting()
	{
		return _isResurrecting;
	}

	PhantomClassAI class_ai;

	public void setPhantomClassAI(PhantomClassAI class_ai)
	{
		this.class_ai = class_ai;
	}

	public PhantomClassAI getClassAI()
	{
		return class_ai;
	}

	private int level_loc = 1;

	public int getLevelLoc()
	{
		return level_loc;
	}

	public void setLevelLoc(int lvl)
	{
		level_loc = lvl;
	}

	public void setPhantomMerchantsAI()
	{
		setPhantomAI(new Merchants());
	}

	public void setPhantomTownsAI()
	{
		//setPhantomAI(new TownAI());
	}

	public void setComebackDistance(int set)
	{
		_comeback_distance = set;
	}

	public int getComebackDistance()
	{
		return _comeback_distance;
	}

	// TODO переделать, избавить от мусора (убрать аналоги)
	public void setPhantomAI()
	{
		PhantomDefaultAI ai;
		// люди
		if(actor.getClassId() == ClassId.SOULTAKER)
			ai = new Soultaker();
		else if(actor.getClassId() == ClassId.ARCHMAGE)
			ai = new Archmage();
		else if(actor.getClassId() == ClassId.ARCANA_LORD)
			ai = new ArcanaLord();
		else if(actor.getClassId() == ClassId.ELEMENTAL_MASTER)
			ai = new ElementalMaster();
		else if(actor.getClassId() == ClassId.DREADNOUGHT)
			ai = new Dreadnought();
		else if(actor.getClassId() == ClassId.DUELIST)
			ai = new Duelist();
		else if(actor.getClassId() == ClassId.ADVENTURER)
			ai = new Adventurer();
		else if(actor.getClassId() == ClassId.SAGITTARIUS)
			ai = new Saggittarius();
		else if(actor.getClassId() == ClassId.PHOENIX_KNIGHT)
			ai = new PhoenixKnight();
		else if(actor.getClassId() == ClassId.HELL_KNIGHT)
			ai = new HellKnight();
		else if(actor.getClassId() == ClassId.CARDINAL)
			ai = new Cardinal();
		else if(actor.getClassId() == ClassId.HIEROPHANT)
			ai = new Hierophant();
		else if(actor.getClassId() == ClassId.HUMAN_MAGE)
			ai = new DefaultMage();
		else if(actor.getClassId() == ClassId.HUMAN_FIGHTER)
			ai = new DefaultFighter();
		else if(actor.getClassId() == ClassId.ROGUE)
			ai = new DefaultFighter();
		else if(actor.getClassId() == ClassId.KNIGHT)
			ai = new DefaultFighter();
		else if(actor.getClassId() == ClassId.CLERIC)
			ai = new DefaultMage();
		else if(actor.getClassId() == ClassId.WIZARD)
			ai = new DefaultMage();
		else if(actor.getClassId() == ClassId.WARRIOR)
			ai = new DefaultFighter();
		else if(actor.getClassId() == ClassId.HAWKEYE)
			ai = new Saggittarius();
		else if(actor.getClassId() == ClassId.TREASURE_HUNTER)
			ai = new Adventurer();
		else if(actor.getClassId() == ClassId.SORCERER)
			ai = new Archmage();
		else if(actor.getClassId() == ClassId.NECROMANCER)
			ai = new Soultaker();
		else if(actor.getClassId() == ClassId.WARLOCK)
			ai = new ArcanaLord();
		else if(actor.getClassId() == ClassId.BISHOP)
			ai = new Cardinal();
		else if(actor.getClassId() == ClassId.PROPHET)
			ai = new Hierophant();
		else if(actor.getClassId() == ClassId.PALADIN)
			ai = new PhoenixKnight();
		else if(actor.getClassId() == ClassId.DARK_AVENGER)
			ai = new HellKnight();
		else if(actor.getClassId() == ClassId.WARLORD)
			ai = new Dreadnought();
		else if(actor.getClassId() == ClassId.GLADIATOR)
			ai = new Duelist();

		// орки
		else if(actor.getClassId() == ClassId.GRAND_KHAVATARI)
			ai = new GrandKhauatari();
		else if(actor.getClassId() == ClassId.DOMINATOR)
			ai = new Dominator();
		else if(actor.getClassId() == ClassId.DOOMCRYER)
			ai = new Doomcryer();
		else if(actor.getClassId() == ClassId.TITAN)
			ai = new Titan();
		else if(actor.getClassId() == ClassId.OVERLORD)
			ai = new Dominator();
		else if(actor.getClassId() == ClassId.WARCRYER)
			ai = new Doomcryer();
		else if(actor.getClassId() == ClassId.TYRANT)
			ai = new GrandKhauatari();
		else if(actor.getClassId() == ClassId.DESTROYER)
			ai = new Titan();
		else if(actor.getClassId() == ClassId.ORC_MONK)
			ai = new DefaultFighter();
		else if(actor.getClassId() == ClassId.ORC_SHAMAN)
			ai = new DefaultFighter();
		else if(actor.getClassId() == ClassId.ORC_RAIDER)
			ai = new DefaultFighter();
		else if(actor.getClassId() == ClassId.ORC_FIGHTER)
			ai = new DefaultFighter();
		else if(actor.getClassId() == ClassId.ORC_MAGE)
			ai = new DefaultFighter();
		// гномы
		else if(actor.getClassId() == ClassId.MAESTRO)
			ai = new Maestro();
		else if(actor.getClassId() == ClassId.FORTUNE_SEEKER)
			ai = new FortuneSeeker();
		else if(actor.getClassId() == ClassId.BOUNTY_HUNTER)
			ai = new FortuneSeeker();
		else if(actor.getClassId() == ClassId.WARSMITH)
			ai = new Maestro();
		else if(actor.getClassId() == ClassId.SCAVENGER)
			ai = new DefaultFighter();
		else if(actor.getClassId() == ClassId.ARTISAN)
			ai = new DefaultFighter();
		else if(actor.getClassId() == ClassId.DWARVEN_FIGHTER)
			ai = new DefaultFighter();

		// темные ельфы
		else if(actor.getClassId() == ClassId.SHILLIEN_SAINT)
			ai = new ShilienSaint();
		else if(actor.getClassId() == ClassId.SPECTRAL_DANCER)
			ai = new SpectralDancer();
		else if(actor.getClassId() == ClassId.SHILLIEN_TEMPLAR)
			ai = new ShilienTemplar();
		else if(actor.getClassId() == ClassId.GHOST_SENTINEL)
			ai = new GhostSentinel();
		else if(actor.getClassId() == ClassId.GHOST_HUNTER)
			ai = new GhostHunter();
		else if(actor.getClassId() == ClassId.SPECTRAL_MASTER)
			ai = new SpectralMaster();
		else if(actor.getClassId() == ClassId.STORM_SCREAMER)
			ai = new StormScreamer();
		else if(actor.getClassId() == ClassId.PHANTOM_RANGER)
			ai = new GhostSentinel();
		else if(actor.getClassId() == ClassId.ABYSS_WALKER)
			ai = new GhostHunter();
		else if(actor.getClassId() == ClassId.SPELLHOWLER)
			ai = new StormScreamer();
		else if(actor.getClassId() == ClassId.PHANTOM_SUMMONER)
			ai = new SpectralMaster();
		else if(actor.getClassId() == ClassId.SHILLEN_ELDER)
			ai = new ShilienSaint();
		else if(actor.getClassId() == ClassId.BLADEDANCER)
			ai = new DefaultFighter();
		else if(actor.getClassId() == ClassId.SHILLEN_KNIGHT)
			ai = new ShilienTemplar();
		else if(actor.getClassId() == ClassId.ASSASIN)
			ai = new DefaultFighter();
		else if(actor.getClassId() == ClassId.SHILLEN_ORACLE)
			ai = new DefaultMage();
		else if(actor.getClassId() == ClassId.DARK_WIZARD)
			ai = new DefaultMage();
		else if(actor.getClassId() == ClassId.PALUS_KNIGHT)
			ai = new DefaultFighter();
		else if(actor.getClassId() == ClassId.DARK_MAGE)
			ai = new DefaultMage();
		else if(actor.getClassId() == ClassId.DARK_FIGHTER)
			ai = new DefaultFighter();

		// ельфы
		else if(actor.getClassId() == ClassId.MYSTIC_MUSE)
			ai = new MysticMuse();
		else if(actor.getClassId() == ClassId.MOONLIGHT_SENTINEL)
			ai = new MoonlightSentinel();
		else if(actor.getClassId() == ClassId.WIND_RIDER)
			ai = new WindRider();
		else if(actor.getClassId() == ClassId.EVAS_TEMPLAR)
			ai = new EvaTemplar();
		else if(actor.getClassId() == ClassId.SWORD_MUSE)
			ai = new SwordMuse();
		else if(actor.getClassId() == ClassId.EVAS_SAINT)
			ai = new EvaSaint();
		else if(actor.getClassId() == ClassId.ELVEN_MAGE)
			ai = new DefaultMage();
		else if(actor.getClassId() == ClassId.ELVEN_FIGHTER)
			ai = new DefaultFighter();
		else if(actor.getClassId() == ClassId.ELVEN_KNIGHT)
			ai = new DefaultFighter();
		else if(actor.getClassId() == ClassId.ELVEN_WIZARD)
			ai = new DefaultMage();
		else if(actor.getClassId() == ClassId.ORACLE)
			ai = new DefaultMage();
		else if(actor.getClassId() == ClassId.ELVEN_SCOUT)
			ai = new DefaultFighter();
		else if(actor.getClassId() == ClassId.SILVER_RANGER)
			ai = new MoonlightSentinel();
		else if(actor.getClassId() == ClassId.PLAIN_WALKER)
			ai = new WindRider();
		else if(actor.getClassId() == ClassId.SPELLSINGER)
			ai = new MysticMuse();
		else if(actor.getClassId() == ClassId.ELEMENTAL_SUMMONER)
			ai = new ElementalMaster();
		else if(actor.getClassId() == ClassId.ELDER)
			ai = new EvaSaint();
		else if(actor.getClassId() == ClassId.SWORDSINGER)
			ai = new SwordMuse();
		else if(actor.getClassId() == ClassId.TEMPLE_KNIGHT)
			ai = new EvaTemplar();
		else
			ai = new Other();

		setPhantomAI(ai);

	}

	public void setPhantomAI(PhantomDefaultAI ai)
	{
		_phantomAi = ai;
		_phantomAi.setActor(actor);
		actor.addListener(addSkilleffect);
		actor.addListener(_attacked);
		//actor.addListener(_groupEntry);
		//actor.addListener(phantomOnLevelUp);
		//actor.addListener(_newSummon);
		actor.addListener(_stopEff);
		actor.addListener(onCreatureAttacked);
	}

	public PhantomDefaultAI getPhantomAI()
	{
		return _phantomAi;
	}

	public void setNullTrafficScheme()
	{
		this.trafficScheme = null;
	}

	/**
	 * @param scheme     - новая схема, возможен null при удалении (использовать при добавлении маршрута)
	 * @param oldscheme  - старая схема, возможен null при добавлении (использовать при освобождении маршрута)
	 * @param fullremove - обнуление переменной (использовать при смене типа фантома)
	 */
	public void setTrafficScheme(final PhantomRoute scheme, PhantomRoute oldscheme, boolean fullremove)
	{
		if(scheme != null)
		{
			PhantomPlayers.getInstance().addRoute(scheme.getName()); // занимаем маршрут
			this.trafficScheme = scheme;
		}
		else
			this.trafficScheme = null;

		if(oldscheme != null && oldscheme.getName() != null && !oldscheme.getName().isBlank())
			PhantomPlayers.getInstance().removeRoute(oldscheme.getName()); // свобождаем маршрут

		if(fullremove)
			this.trafficScheme = null;
	}

	public PhantomRoute getTrafficScheme()
	{
		return trafficScheme;
	}

	public void setPhantomPartyAI(PhantomPartyObject ai)
	{
		_phantomPartyAi = ai;
	}

	public PhantomPartyObject getPhantomPartyAI()
	{
		return _phantomPartyAi;
	}

	public void setChatTradeTime(long l)
	{
		_ChatTradeTime = l;
	}

	public long getChatTradeTime()
	{
		return _ChatTradeTime;
	}

	public void setTradeList(TradePhantom im)
	{
		trade_list = im;
	}

	public TradePhantom getTradeList()
	{
		return trade_list;
	}

	public void setPK(boolean isPk)
	{
		_isPk = isPk;
	}

	public boolean getPK()
	{
		return _isPk;
	}

	public void setPvP(boolean isPvP)
	{
		_isPvP = isPvP;
	}

	public boolean getPvP()
	{
		return _isPvP;
	}

	public double getChanceOlyCast()
	{
		return chanceOlyCast;
	}

	public void setChanceOlyCast(double _chanceOlyCast)
	{
		chanceOlyCast = _chanceOlyCast;
	}

	public int getRndDelayAi()
	{
		return _RndDelayAi;
	}

	public void setRndDelayAi(int _RndDelayAi)
	{
		this._RndDelayAi = _RndDelayAi;
	}

	public Creature getResTarget()
	{
		if(_resTarget != null && _resTarget.isDead())
			return _resTarget;
		return null;
	}

	public void setResTarget(Creature _resTarget)
	{
		this._resTarget = _resTarget;
	}

	public void setDelayNewTarget(long l)
	{
		delayNewTarget = l;
	}

	public long getDelayNewTarget()
	{
		return delayNewTarget;
	}

	private ScheduledFuture<?> delayAssistChange = null;

	public void delayAssistChange()
	{
		if(delayAssistChange != null && (delayAssistChange.isCancelled() || delayAssistChange.isDone()))
		{
			delayAssistChange.cancel(true);
			delayAssistChange = null;
		}
		if(delayAssistChange != null)
			return;
		delayAssistChange = ThreadPoolManager.getInstance().PhantomSchedule(new Runnable(){
			@Override
			public void run()
			{
				if(actor == null)
					return;
				actor.phantom_params.setLockedTarget(getPhantomPartyAI().getPartyTarget());
			}
		}, Rnd.get(5, 20) * 100);
	}

	protected ScheduledFuture<?> _MoveToGkTask = null;

	public boolean getMoveToGkTask()
	{
		if(_MoveToGkTask != null && !_MoveToGkTask.isDone())
			return true;
		return false;
	}

	public void initMoveToGkTask(long delay)
	{
		try
		{
			abortMoveToGkTask();
			_MoveToGkTask = ThreadPoolManager.getInstance().schedule(new MoveToGkTask(actor), delay);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void abortMoveToGkTask()
	{
		if(_MoveToGkTask != null)
		{
			_MoveToGkTask.cancel(true);
			_MoveToGkTask = null;
		}
	}
	
	protected ScheduledFuture<?> _EndPeaceCooldownTask = null;

	public boolean getEndPeaceCooldownTask()
	{
		if(_EndPeaceCooldownTask != null && !_EndPeaceCooldownTask.isDone())
			return true;
		return false;
	}

	public void initEndPeaceCooldownTask(long delay)
	{
		try
		{
			abortEndPeaceCooldownTask();
			_EndPeaceCooldownTask = ThreadPoolManager.getInstance().schedule(new EndPeaceCooldownTask(actor), delay);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void abortEndPeaceCooldownTask()
	{
		if(_EndPeaceCooldownTask != null)
		{
			_EndPeaceCooldownTask.cancel(true);
			_EndPeaceCooldownTask = null;
		}
	}
	
	protected ScheduledFuture<?> _SoeTask = null;

	public boolean getSoeTask()
	{
		if(_SoeTask != null && !_SoeTask.isDone())
			return true;
		return false;
	}

	public void initSoeTask(Runnable r, long delay)
	{
		try
		{
			abortSoeTask();
			_SoeTask = ThreadPoolManager.getInstance().schedule(r, delay);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void abortSoeTask()
	{
		if(_SoeTask != null)
		{
			_SoeTask.cancel(true);
			_SoeTask = null;
		}
	}

	private Creature _priorityTarget = null; // цель за которой гоняется бот

	public Creature getPriorityTarget()
	{
		if(_priorityTarget != null && (_priorityTarget.isDeleted() || _priorityTarget.isDead()))
		{
			_priorityTarget = null;
			return null;
		}
		return _priorityTarget;
	}

	public void setPriorityTarget(Creature target)
	{
		_priorityTarget = target;
	}

	protected ScheduledFuture<?> _actionTask = null;

	class SupportActionTask extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			Skill skill = onSupportActionTask();
			_actionTask = ThreadPoolManager.getInstance().PhantomSchedule(new SupportActionTask(), skill == null ? 1000 : skill.getHitTime() * 333 / Math.max(actor.getMAtkSpd(), 1) - 100);
		}
	}

	public synchronized void stopSupportBuffTask()
	{
		if(_actionTask != null)
		{
			_actionTask.cancel(false);
			_actionTask = null;
		}
	}

	public synchronized void startSupportBuffTask()
	{
		if(_actionTask != null)
			stopSupportBuffTask();
		if(_actionTask == null)
			_actionTask = ThreadPoolManager.getInstance().PhantomSchedule(new SupportActionTask(), 1000);
	}

	private boolean checkEffect(Abnormal ef, Skill skill)
	{
		if(ef == null)
			return false;
		if(ef.checkBlockedAbnormalType(skill.getAbnormalType()))
			return true;
		EffectTemplate effectTemplate = skill.getEffectTemplates(EffectUseType.NORMAL).get(0);
		return AbnormalList.checkAbnormalType(ef.getTemplate(), effectTemplate) && ef.getAbnormalLvl() >= effectTemplate.getAbnormalLvl() && (ef.getTimeLeft() > 10 || ef.getNext() != null && checkEffect(ef.getNext(), skill));
	}

	/********** слежение за бафом и ребаф *******/
	public Skill onSupportActionTask()
	{
		if(actor.isDead())
			return null;

		Player target = actor;
		if(target != null && target.isOnline() && !target.isDead() && !target.isInvul() && !actor.isCastingNow() && target.getDistance(actor) < 800)
		{
			SkillsGroup buffs_skills = actor.phantom_params.getClassAI().getBuffSkills();
			outer: for(PhantomSkill buff : buffs_skills.getAllSkills())
			{
				Skill skill = actor.getSkillById(buff.getId());
				if(skill != null)
				{
					if(!buff.getCondition().isEmpty()) // проверяем наличие кондишена
						if(!checkConditionSkill(target, buff)) // проверяем условия если не подошло отменяем каст
							continue;

					for(Abnormal ef : target.getAbnormalList().getEffects())
						if(checkEffect(ef, skill))
							continue outer;
					if(skill.checkCondition(actor, target, false, !actor.isFollowing(), true))
					{
						actor.phantom_params.getPhantomAI().CastSkill(actor, skill, target, false);
						return skill;
					}
				}
			}
		}
		return null;
	}

	private boolean checkConditionSkill(Creature target, PhantomSkill check_skill)
	{
		Boolean check = true;
		Skill skill = actor.getSkillById(check_skill.getId());
		for(Entry<Condition, PCondition> cond : check_skill.getCondition().entrySet())
		{
			switch(cond.getKey())
			{
				case TARGET_MAGE:
					if(target == null || (target.isPlayer() && !target.isMageClass()))
					{
						check = false;
						continue;
					}
					break;
				case TARGET_FIGHTER:
					if(target == null || (target.isPlayer() && target.isMageClass()))
					{
						check = false;
						continue;
					}
					break;
				case SELF_NOT_SKILL_EFFECT:
					if(actor.getAbnormalList() != null && actor.getAbnormalList().containEffectFromSkills(cond.getValue().getList().stream().mapToInt(i -> i).toArray()))
					{
						check = false;
						continue;
					}
					break;
				case SELF_SKILL_EFFECT:
					if(actor.getAbnormalList() != null && !actor.getAbnormalList().containEffectFromSkills(cond.getValue().getList().stream().mapToInt(i -> i).toArray()))
					{
						check = false;
						continue;
					}
					break;
				case TARGET_CLASS_ID:
					if(target != null && target.isPlayer() && !cond.getValue().getList().contains(target.getPlayer().getClassId().getId()))
					{
						check = false;
						continue;
					}
					break;
				case TARGET_WEAPON_TYPE:
					if(target == null || (target.getActiveWeaponInstance() != null && !cond.getValue().getWeaponTypeList().contains(target.getActiveWeaponInstance().getItemType())))
					{
						check = false;
						continue;
					}
					break;
				case TARGET_ARMOR_TYPE:
					if(target == null || (target.isPlayer() && target.getPlayer().getInventory().getPaperdollItem(10) != null && !cond.getValue().getArmorTypeList().contains(target.getPlayer().getInventory().getPaperdollItem(10).getTemplate().getItemType())))
					{
						check = false;
						continue;
					}
					break;
				case TARGET_NOT_USE_CLASS_ID:
					if(target != null && target.isPlayer() && cond.getValue().getList().contains(target.getPlayer().getClassId().getId()))
					{
						check = false;
						continue;
					}
					break;
				case SKILL_DISABLED:
					if(skill == null || actor.getSkillById(cond.getValue().IntParameter()) == null || !actor.isSkillDisabled(actor.getSkillById(cond.getValue().IntParameter())))
					{
						check = false;
						continue;
					}
					break;
				default:
					break;

			}
		}
		return check;

	}

	public boolean isNeedRebuff()
	{
		return _need_rebuff;
	}

	public void setNeedRebuff(boolean _need_rebuff)
	{
		this._need_rebuff = _need_rebuff;
	}

	PartyState _state = PartyState.battle;

	public void changeState(PartyState state)
	{
		_state = state;
	}

	public PartyState getState()
	{
		return _state;
	}

}
