package l2s.gameserver.model;

import com.google.common.flogger.FluentLogger;
import gnu.trove.iterator.TIntLongIterator;
import gnu.trove.map.TIntLongMap;
import l2s.commons.ban.BanBindType;
import l2s.commons.dao.JdbcEntityState;
import l2s.commons.dbutils.DbUtils;
import l2s.commons.lang.reference.HardReference;
import l2s.commons.lang.reference.HardReferences;
import l2s.commons.math.MathUtils;
import l2s.commons.time.cron.SchedulingPattern;
import l2s.commons.util.Rnd;
import l2s.commons.util.concurrent.atomic.AtomicState;
import l2s.gameserver.*;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.ai.FakeAI;
import l2s.gameserver.ai.PlayableAI.AINextAction;
import l2s.gameserver.ai.PlayerAI;
import l2s.gameserver.dao.*;
import l2s.gameserver.data.QuestHolder;
import l2s.gameserver.data.xml.holder.*;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.database.mysql;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.geometry.ILocation;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.handler.items.IItemHandler;
import l2s.gameserver.handler.onshiftaction.OnShiftActionHolder;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.instancemanager.*;
import l2s.gameserver.instancemanager.BotCheckManager.BotCheckQuestion;
import l2s.gameserver.instancemanager.OfflineBufferManager.BufferData;
import l2s.gameserver.listener.actor.player.OnAnswerListener;
import l2s.gameserver.listener.actor.player.OnPlayerChatMessageReceive;
import l2s.gameserver.listener.actor.player.impl.BotCheckAnswerListner;
import l2s.gameserver.listener.actor.player.impl.ReviveAnswerListener;
import l2s.gameserver.listener.actor.player.impl.SummonAnswerListener;
import l2s.gameserver.listener.hooks.ListenerHook;
import l2s.gameserver.listener.hooks.ListenerHookType;
import l2s.gameserver.model.GameObjectTasks.*;
import l2s.gameserver.model.Request.L2RequestType;
import l2s.gameserver.model.Zone.ZoneType;
import l2s.gameserver.model.actor.CreatureSkillCast;
import l2s.gameserver.model.actor.MoveType;
import l2s.gameserver.model.actor.basestats.PlayerBaseStats;
import l2s.gameserver.model.actor.flags.PlayerFlags;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.actor.instances.player.FriendList;
import l2s.gameserver.model.actor.instances.player.*;
import l2s.gameserver.model.actor.instances.player.tasks.EnableUserRelationTask;
import l2s.gameserver.model.actor.listener.PlayerListenerList;
import l2s.gameserver.model.actor.recorder.PlayerStatsChangeRecorder;
import l2s.gameserver.model.actor.stat.PlayerStat;
import l2s.gameserver.model.base.*;
import l2s.gameserver.model.entity.Hero;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.impl.*;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.model.entity.olympiad.OlympiadGame;
import l2s.gameserver.model.entity.olympiad.OlympiadParticipiantData;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.entity.residence.ClanHall;
import l2s.gameserver.model.entity.residence.Residence;
import l2s.gameserver.model.entity.residence.ResidenceSide;
import l2s.gameserver.model.instances.*;
import l2s.gameserver.model.instances.SummonInstance.RestoredSummon;
import l2s.gameserver.model.instances.residences.SiegeToggleNpcInstance;
import l2s.gameserver.model.items.*;
import l2s.gameserver.model.items.Warehouse.WarehouseType;
import l2s.gameserver.model.items.attachment.FlagItemAttachment;
import l2s.gameserver.model.items.attachment.PickableAttachment;
import l2s.gameserver.model.matching.MatchingRoom;
import l2s.gameserver.model.petition.PetitionMainGroup;
import l2s.gameserver.model.pledge.*;
import l2s.gameserver.model.pledge.ClanWar.ClanWarPeriod;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestEventType;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.authcomm.gs2as.BonusRequest;
import l2s.gameserver.network.authcomm.gs2as.ReduceAccountPoints;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.components.*;
import l2s.gameserver.network.l2.s2c.*;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage.ScreenMessageAlign;
import l2s.gameserver.network.l2.s2c.StatusUpdatePacket.UpdateType;
import l2s.gameserver.network.l2.s2c.updatetype.IUpdateTypeComponent;
import l2s.gameserver.network.l2.s2c.updatetype.UserInfoType;
import l2s.gameserver.skills.*;
import l2s.gameserver.stats.BooleanStat;
import l2s.gameserver.stats.DoubleStat;
import l2s.gameserver.stats.Formulas;
import l2s.gameserver.tables.ClanTable;
import l2s.gameserver.taskmanager.AutoSaveManager;
import l2s.gameserver.taskmanager.LazyPrecisionTaskManager;
import l2s.gameserver.templates.CreatureTemplate;
import l2s.gameserver.templates.InstantZone;
import l2s.gameserver.templates.OptionDataTemplate;
import l2s.gameserver.templates.PremiumAccountTemplate;
import l2s.gameserver.templates.cubic.AgathionTemplate;
import l2s.gameserver.templates.fakeplayer.FakePlayerAITemplate;
import l2s.gameserver.templates.item.ArmorTemplate.ArmorType;
import l2s.gameserver.templates.item.EtcItemTemplate.EtcItemType;
import l2s.gameserver.templates.item.*;
import l2s.gameserver.templates.item.WeaponTemplate.WeaponType;
import l2s.gameserver.templates.item.data.ItemData;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.templates.pet.PetData;
import l2s.gameserver.templates.player.PlayerTemplate;
import l2s.gameserver.templates.player.transform.TransformTemplate;
import l2s.gameserver.templates.skill.SkillClassId;
import l2s.gameserver.utils.*;
import l2s.gameserver.utils.loggers.ItemLogger;
import l2s.gameserver.utils.loggers.ItemLogger.ItemProcess;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.napile.primitive.Containers;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;
import org.napile.primitive.maps.impl.CTreeIntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;
import org.napile.primitive.pair.IntObjectPair;
import org.napile.primitive.pair.impl.IntObjectPairImpl;

import java.awt.*;
import java.sql.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static l2s.gameserver.model.actor.instances.player.ShortCutList.AutoShortCutType;
import static l2s.gameserver.network.l2.s2c.ExSetCompassZoneCode.*;

public final class Player extends Playable implements PlayerGroup
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	public static final int DEFAULT_NAME_COLOR = 0xFFFFFF;
	public static final int DEFAULT_TITLE_COLOR = 0xFFFF77;
	public static final int MAX_POST_FRIEND_SIZE = 100;

	public static final Location STABLE_LOCATION = new Location(-119664, 246306, 1232);

	public static final String NO_TRADERS_VAR = "notraders";
	public static final String NO_ANIMATION_OF_CAST_VAR = "notShowBuffAnim";
	public static final String MY_BIRTHDAY_RECEIVE_YEAR = "MyBirthdayReceiveYear";
	private static final String NOT_CONNECTED = "<not connected>";
	private static final String LVL_UP_REWARD_VAR = "@lvl_up_reward";
	private static final String ACADEMY_GRADUATED_VAR = "@academy_graduated";
	private static final String JAILED_VAR = "jailed";
	private static final String PA_ITEMS_RECIEVED = "pa_items_recieved";
	private static final String FREE_PA_RECIEVED = "free_pa_recieved";
	private static final String ACTIVE_SHOT_ID_VAR = "@active_shot_id";
	private static final String PC_BANG_POINTS_VAR = "pc_bang_poins";

	// ???????????? ???????????????????? ???????????????????????? ?????? ?????????????????????? ????????????????.
	public static final String CHANGED_OLD_NAME = "changed_old_name";
	public static final String CHANGED_PLEDGE_NAME = "changed_old_pledge_name";

	private static final String PK_KILL_VAR = "@pk_kill";

	public final static int OBSERVER_NONE = 0;
	public final static int OBSERVER_STARTING = 1;
	public final static int OBSERVER_STARTED = 3;
	public final static int OBSERVER_LEAVING = 2;

	public static final int STORE_PRIVATE_NONE = 0;
	public static final int STORE_PRIVATE_SELL = 1;
	public static final int STORE_PRIVATE_BUY = 3;
	public static final int STORE_PRIVATE_MANUFACTURE = 5;
	public static final int STORE_OBSERVING_GAMES = 7;
	public static final int STORE_PRIVATE_SELL_PACKAGE = 8;
	public static final int STORE_PRIVATE_BUFF = 20;

	private static final long CHANGE_ACTIVE_ELEMENT_DELAY = 10; // In seconds

	private PlayerTemplate _baseTemplate;

	private GameClient _connection;
	private String _login;

	private int _karma, _pkKills, _pvpKills;
	private int _face, _hairStyle, _hairColor;
	private int _beautyFace, _beautyHairStyle, _beautyHairColor;
	private int _recomHave, _recomLeftToday, _fame, _raidPoints;
	private int _recomLeft = 0;
	private int _deleteTimer;
	private boolean _isVoting = false;

	private long _createTime, _onlineTime, _onlineBeginTime, _leaveClanTime, _deleteClanTime, _NoChannel, _NoChannelBegin;
	private long _uptime;
	/**
	 * Time on login in game
	 */
	private int _lastAccess;

	/**
	 * The Color of players name / title (white is 0xFFFFFF)
	 */
	private int _nameColor = DEFAULT_NAME_COLOR;
	private int _titlecolor = DEFAULT_TITLE_COLOR;

	private boolean _overloaded;

	boolean sittingTaskLaunched;

	/**
	 * Time counter when L2Player is sitting
	 */
	private int _waitTimeWhenSit;

	private boolean _autoLoot = Config.AUTO_LOOT;
	private boolean AutoLootHerbs = Config.AUTO_LOOT_HERBS;
	private boolean _autoLootOnlyAdena = Config.AUTO_LOOT_ONLY_ADENA;

	private final PcInventory _inventory = new PcInventory(this);
	private final Warehouse _warehouse = new PcWarehouse(this);
	private final ItemContainer _refund = new PcRefund(this);
	private final PcFreight _freight = new PcFreight(this);

	private final BookMarkList _bookmarks = new BookMarkList(this, 0);
	public Location bookmarkLocation = null;

	private final AntiFlood _antiFlood = new AntiFlood(this);

	private final Map<Integer, RecipeTemplate> _recipebook = new TreeMap<Integer, RecipeTemplate>();
	private final Map<Integer, RecipeTemplate> _commonrecipebook = new TreeMap<Integer, RecipeTemplate>();

	/**
	 * The table containing all Quests began by the L2Player
	 */
	private final IntObjectMap<QuestState> _quests = new HashIntObjectMap<QuestState>();

	/**
	 * The list containing all shortCuts of this L2Player
	 */
	private final ShortCutList _shortCuts = new ShortCutList(this);

	/**
	 * The list containing all macroses of this L2Player
	 */
	private final MacroList _macroses = new MacroList(this);

	/**
	 * The list containing all subclasses of this L2Player
	 */
	private final SubClassList _subClassList = new SubClassList(this);

	/**
	 * The Private Store type of the L2Player (STORE_PRIVATE_NONE=0, STORE_PRIVATE_SELL=1, sellmanage=2, STORE_PRIVATE_BUY=3, buymanage=4, STORE_PRIVATE_MANUFACTURE=5)
	 */
	private int _privatestore;
	/**
	 * ???????????? ?????? ???????????????? ????????????????
	 */
	private String _manufactureName;
	private Map<Integer, ManufactureItem> _createList = Collections.emptyMap();
	/**
	 * ???????????? ?????? ???????????????? ??????????????
	 */
	private String _sellStoreName;
	private String _packageSellStoreName;
	private Map<Integer, TradeItem> _sellList = Collections.emptyMap();
	private Map<Integer, TradeItem> _packageSellList = Collections.emptyMap();
	/**
	 * ???????????? ?????? ???????????????? ??????????????
	 */
	private String _buyStoreName;
	private List<TradeItem> _buyList = Collections.emptyList();
	/**
	 * ???????????? ?????? ????????????
	 */
	private List<TradeItem> _tradeList = Collections.emptyList();

	private Party _party;
	private Location _lastPartyPosition;

	private Clan _clan;
	private PledgeRank _pledgeRank = PledgeRank.VAGABOND;
	private int _pledgeType = Clan.SUBUNIT_NONE, _powerGrade = 0, _lvlJoinedAcademy = 0, _apprentice = 0;

	/**
	 * GM Stuff
	 */
	private int _accessLevel;
	private PlayerAccess _playerAccess = new PlayerAccess();

	private boolean _messageRefusal = false, _tradeRefusal = false, _blockAll = false;

	/**
	 * The L2Summon of the L2Player
	 */
	private SummonInstance _summon = null;
	private PetInstance _pet = null;
	private SymbolInstance _symbol = null;

	private boolean _riding;

	private int _botRating;

	private List<DecoyInstance> _decoys = new CopyOnWriteArrayList<DecoyInstance>();

	private IntObjectMap<Cubic> _cubics = null;
	private Agathion _agathion = null;

	private Request _request;

	private ItemInstance _arrowItem;

	/**
	 * The fists L2Weapon of the L2Player (used when no weapon is equipped)
	 */
	private WeaponTemplate _fistsWeaponItem;

	private Map<Integer, String> _chars = new HashMap<Integer, String>(8);

	private ItemInstance _enchantScroll = null;
	private ItemInstance _appearanceStone = null;
	private ItemInstance _appearanceExtractItem = null;

	private WarehouseType _usingWHType;

	private boolean _isOnline = false;

	private final AtomicBoolean _isLogout = new AtomicBoolean();

	/**
	 * The L2NpcInstance corresponding to the last Folk which one the player talked.
	 */
	private HardReference<NpcInstance> _lastNpc = HardReferences.emptyRef();
	/**
	 * ?????? ???????????? ???????????????????? ?? ?????????????? ????????????????
	 */
	private MultiSellListContainer _multisell = null;

	private IntObjectMap<ShotType> _activeAutoShots = new CHashIntObjectMap<ShotType>();

	private ObservePoint _observePoint;
	private AtomicInteger _observerMode = new AtomicInteger(0);

	public int _telemode = 0;

	public boolean entering = true;

	/**
	 * ?????? ?????????? ?????????????????????? ?????? ?????????????????? ???????????? ????????, ?? ???????? ???? ?????????? null ?????? ???????????????????????? ?? ??????
	 * ???????????????????????? ???????????????? ?????? ?????????????????????? ?????? ?????????????? ?? ??????????????
	 * ???????? heading ???????????????????????? ?????? ???????????????? ?????????? ???????????????????????? ?????? ????????
	 */
	private Location _stablePoint = null;

	/**
	 * new loto ticket *
	 */
	public int _loto[] = new int[5];
	/**
	 * new race ticket *
	 */
	public int _race[] = new int[2];

	private final BlockList _blockList = new BlockList(this);
	private final FriendList _friendList = new FriendList(this);
	private final PremiumItemList _premiumItemList = new PremiumItemList(this);
	private final ProductHistoryList _productHistoryList = new ProductHistoryList(this);
	private final HennaList _hennaList = new HennaList(this);
	private final AttendanceRewards _attendanceRewards = new AttendanceRewards(this);
	private final DailyMissionList _dailiyMissionList = new DailyMissionList(this);
	private final ElementalList _elementalList = new ElementalList(this);
	private final VIP _vip = new VIP(this);
	private final CostumeList costumes = new CostumeList(this);

	private boolean _hero = false;

	private PremiumAccountTemplate _premiumAccount = PremiumAccountHolder.getInstance().getPremiumAccount(0);
	private Future<?> _premiumAccountExpirationTask;

	private boolean _isSitting;
	private ChairInstance _chairObject;

	private boolean _inOlympiadMode;
	private OlympiadGame _olympiadGame;
	private ObservableArena _observableArena;

	private int _olympiadSide = -1;

	/**
	 * ally with ketra or varka related wars
	 */
	private int _varka = 0;
	private int _ketra = 0;
	private int _ram = 0;

	private byte[] _keyBindings = ArrayUtils.EMPTY_BYTE_ARRAY;

	private final Fishing _fishing = new Fishing(this);

	private Future<?> _taskWater;
	private Future<?> _autoSaveTask;
	private Future<?> _kickTask;

	private Future<?> _pcCafePointsTask;
	private Future<?> _unjailTask;

	private Future<?> _trainingCampTask;

	private Future<?> _sceneMovieEndTask;

	private final Lock _storeLock = new ReentrantLock();

	private int _zoneMask;

	private long _offlineStartTime = 0L;
	private boolean _awaying = false;

	private boolean _registeredInEvent = false;

	private int _pcBangPoints;

	private int _expandInventory = 0;
	private int _expandWarehouse = 0;
	private int _battlefieldChatId;
	private int _lectureMark;

	private AtomicState _gmInvisible = new AtomicState();
	private AtomicState _gmUndying = new AtomicState();

	private IntObjectMap<String> _postFriends = Containers.emptyIntObjectMap();

	private List<String> _blockedActions = new ArrayList<String>();

	private BypassStorage _bypassStorage = new BypassStorage(this);

	private boolean _notShowBuffAnim = false;
	private boolean _notShowTraders = false;
	private boolean _canSeeAllShouts = false;
	private boolean _debug = false;

	private long _dropDisabled;
	private long _lastItemAuctionInfoRequest;

	private IntObjectPair<OnAnswerListener> _askDialog = null;

	private boolean _matchingRoomWindowOpened = false;
	private MatchingRoom _matchingRoom;
	private PetitionMainGroup _petitionGroup;
	private final Map<Integer, Long> _instancesReuses = new ConcurrentHashMap<Integer, Long>();

	private Language _language = Config.DEFAULT_LANG;

	private int _npcDialogEndTime = 0;

	private Mount _mount = null;

	private final Map<String, CharacterVariable> _variables = new ConcurrentHashMap<String, CharacterVariable>();

	private List<RestoredSummon> _restoredSummons = null;

	private boolean _autoSearchParty;
	private Future<?> _substituteTask;

	private TransformTemplate _transform = null;

	private final IntObjectMap<SkillEntry> _transformSkills = new CHashIntObjectMap<SkillEntry>();

	private long _lastMultisellBuyTime = 0L;
	private long _lastEnchantItemTime = 0L;
	private long _lastAttributeItemTime = 0L;

	private Future<?> _enableRelationTask;

	private boolean _isInReplaceTeleport = false;

	private int _armorSetEnchant = 0;

	private int _usedWorldChatPoints = 0;

	private boolean _hideHeadAccessories = false;

	private ItemInstance _synthesisItem1 = null;
	private ItemInstance _synthesisItem2 = null;

	private List<TrapInstance> _traps = Collections.emptyList();

	private boolean _isInJail = false;

	private final IntObjectMap<OptionDataTemplate> _options = new CTreeIntObjectMap<OptionDataTemplate>();

	private long _receivedExp = 0L;

	private Reflection _activeReflection = null;

	private int _questZoneId = -1;

	private ClassId _selectedMultiClassId = null;

	/** ???????????????????? ?????? ????????????/???????????? ?????????????????? ?????????????? */
	protected final ReadWriteLock questLock = new ReentrantReadWriteLock();
	protected final Lock questRead = questLock.readLock();
	protected final Lock questWrite = questLock.writeLock();

	private final Map<Object, ScheduledFuture<?>> _tasks = new ConcurrentHashMap<>();

	// during fall validations will be disabled for 10 ms.
	private static final int FALLING_VALIDATION_DELAY = 10000;
	private volatile long _fallingTimestamp = 0;

	private long _lastChangeActiveElementTime = 0L;

	private DamageInfo damageInfo = new DamageInfo();

	private int _expertiseArmorPenalty = 0;
	private int _expertiseWeaponPenalty = 0;

	/**
	 * ?????????????????????? ?????? L2Player. ???????????????? ???? ????????????????????, ?????? ???????????????? ???????????? ???????????????????????? PlayerManager.create
	 */
	public Player(final int objectId, final PlayerTemplate template, final String accountName)
	{
		super(objectId, template);

		_baseTemplate = template;
		_login = accountName;
	}

	/**
	 * Constructor<?> of L2Player (use L2Character constructor).<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Call the L2Character constructor to create an empty _skills slot and copy basic Calculator set to this L2Player </li>
	 * <li>Create a L2Radar object</li>
	 * <li>Retrieve from the database all items of this L2Player and add them to _inventory </li>
	 * <p/>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SET the account name of the L2Player</B></FONT><BR><BR>
	 *  @param objectId Identifier of the object to initialized
	 * @param template The L2PlayerTemplate to apply to the L2Player
	 */
	private Player(final FakePlayerAITemplate fakeAiTemplate, final int objectId, final PlayerTemplate template)
	{
		this(objectId, template, null);

		_ai = new FakeAI(this, fakeAiTemplate);
	}

	private Player(final int objectId, final PlayerTemplate template)
	{
		this(objectId, template, null);

		_baseTemplate = template;
		_ai = new PlayerAI(this);

		if(!Config.EVERYBODY_HAS_ADMIN_RIGHTS)
			setPlayerAccess(Config.gmlist.get(objectId));
		else
			setPlayerAccess(Config.gmlist.get(0));
	}

	@SuppressWarnings("unchecked")
	@Override
	public HardReference<Player> getRef()
	{
		return (HardReference<Player>) super.getRef();
	}

	public String getAccountName()
	{
		if(_connection == null)
			return _login;
		return _connection.getLogin();
	}

	public String getIP()
	{
		if(_connection == null)
			return NOT_CONNECTED;
		return _connection.getIpAddr();
	}

	public String getLogin()
	{
		return _login;
	}

	public void setLogin(String val)
	{
		_login = val;
	}

	/**
	 * ???????????????????? ???????????? ???????????????????? ???? ????????????????, ???? ?????????????????????? ????????????????
	 *
	 * @return ???????????? ????????????????????
	 */
	public Map<Integer, String> getAccountChars()
	{
		return _chars;
	}

	@Override
	public final PlayerTemplate getTemplate()
	{
		return (PlayerTemplate) super.getTemplate();
	}

	@Override
	public final void setTemplate(CreatureTemplate template)
	{
		if(isBaseClassActive())
			_baseTemplate = (PlayerTemplate) template;

		super.setTemplate(template);
	}

	public final PlayerTemplate getBaseTemplate()
	{
		return _baseTemplate;
	}

	@Override
	public final boolean isTransformed()
	{
		return _transform != null;
	}

	@Override
	public final TransformTemplate getTransform()
	{
		return _transform;
	}

	@Override
	public final void setTransform(int id)
	{
		TransformTemplate template = id > 0 ? TransformTemplateHolder.getInstance().getTemplate(getSex(), id) : null;
		setTransform(template);
	}

	@Override
	public synchronized final void setTransform(TransformTemplate transform)
	{
		if(transform == _transform || transform != null && _transform != null)
			return;

		// ?????? ???????????? ?????????????????????????? ???????? ?????????? ????????????
		if(transform == null) // ?????????????? ??????????
		{
			if(!_transformSkills.isEmpty())
			{
				// ?????????????? ?????????? ??????????????????????????
				for(SkillEntry skillEntry : _transformSkills.valueCollection())
				{
					if(!SkillAcquireHolder.getInstance().isSkillPossible(this, skillEntry.getTemplate()))
						super.removeSkill(skillEntry, true);
				}
				_transformSkills.clear();
			}

			if(_transform.getItemCheckType() != LockType.NONE)
				getInventory().unlock();

			_transform = null;

			checkActiveToggleEffects();

			// ?????????????????????????? ?????????????? ???????????? ??????????????????????????
			getAbnormalList().stop(AbnormalType.TRANSFORM);
		}
		else
		{
			boolean isFlying = transform.getType() == TransformType.FLYING;

			if(isFlying)
			{
				for(Servitor servitor : getServitors())
					servitor.unSummon(false);
			}

			// ?????????????????? ???????????? ??????????????????????????
			for(SkillLearn sl : transform.getSkills())
			{
				SkillEntry skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.NONE, sl.getId(), sl.getLevel());
				if(skillEntry == null)
					continue;

				_transformSkills.put(skillEntry.getId(), skillEntry);
			}

			// ?????????????????? ???????????? ?????????????????????????? ?????????????????? ???? ???????????? ??????????????????
			for(SkillLearn sl : transform.getAddtionalSkills())
			{
				if(sl.getMinLevel() > getLevel())
					continue;

				SkillEntry skillEntry = _transformSkills.get(sl.getId());
				if(skillEntry != null && skillEntry.getLevel() >= sl.getLevel())
					continue;

				skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.NONE, sl.getId(), sl.getLevel());
				if(skillEntry == null)
					continue;

				_transformSkills.put(skillEntry.getId(), skillEntry);
			}

			for(SkillEntry skillEntry : _transformSkills.valueCollection())
				addSkill(skillEntry, false);

			if(transform.getItemCheckType() != LockType.NONE)
			{
				getInventory().unlock();
				getInventory().lockItems(transform.getItemCheckType(), transform.getItemCheckIDs());
			}

			checkActiveToggleEffects();

			_transform = transform;
		}

		Location destLoc = getLoc().correctGeoZ(getGeoIndex()).changeZ((_transform == null ? 0 : _transform.getSpawnHeight()) + (int) getCurrentCollisionHeight());
		sendPacket(new FlyToLocation(this, destLoc, FlyToLocation.FlyType.DUMMY, 0, 0, 0));
		setLoc(destLoc);

		sendPacket(new ExBasicActionList(this));
		sendSkillList();
		sendPacket(new ShortCutInitPacket(this));
		sendActiveAutoShots();
		sendChanges();
	}

	@Override
	public final boolean isFlying()
	{
		return super.isFlying() || _transform != null && _transform.getType() == TransformType.FLYING;
	}

	public void changeSex()
	{
		PlayerTemplate template = PlayerTemplateHolder.getInstance().getPlayerTemplate(getRace(), getClassId(), getSex().revert());
		if(template == null)
			return;

		setTemplate(template);
		if(isTransformed())
		{
			int transformId = getTransform().getId();
			setTransform(null);
			setTransform(transformId);
		}
	}

	@Override
	public PlayerAI getAI()
	{
		return (PlayerAI) _ai;
	}

	@Override
	public boolean doCast(final SkillEntry skillEntry, final Creature target, boolean forceUse)
	{
		return super.doCast(skillEntry, target, forceUse);

		//if(getUseSeed() != 0 && skill.getSkillType() == SkillType.SOWING)
		//	sendPacket(new ExUseSharedGroupItem(getUseSeed(), getUseSeed(), 5000, 5000));
	}

	@Override
	public void sendReuseMessage(Skill skill)
	{
		/*???? ??????????????????, ???????????? ???????????????? ?????? ?????? ??????????????????.
		if(getSkillCast(SkillCastingType.NORMAL).isCastingNow() && (!isDualCastEnable() || getSkillCast(SkillCastingType.NORMAL_SECOND).isCastingNow()))
			return;

		TimeStamp sts = getSkillReuse(skill);
		if(sts == null || !sts.hasNotPassed())
			return;

		long timeleft = sts.getReuseCurrent();
		if(!Config.ALT_SHOW_REUSE_MSG && timeleft < 10000 || timeleft < 500)
			return;

		long hours = timeleft / 3600000;
		long minutes = (timeleft - hours * 3600000) / 60000;
		long seconds = (long) Math.max(1, Math.ceil((timeleft - hours * 3600000 - minutes * 60000) / 1000.));
		if(hours > 0)
			sendPacket(new SystemMessagePacket(SystemMsg.THERE_ARE_S2_HOURS_S3_MINUTES_AND_S4_SECONDS_REMAINING_IN_S1S_REUSE_TIME).addSkillName(skill).addInteger(hours).addInteger(minutes).addInteger(seconds));
		else if(minutes > 0)
			sendPacket(new SystemMessagePacket(SystemMsg.THERE_ARE_S2_MINUTES_S3_SECONDS_REMAINING_IN_S1S_REUSE_TIME).addSkillName(skill).addInteger(minutes).addInteger(seconds));
		else
			sendPacket(new SystemMessagePacket(SystemMsg.THERE_ARE_S2_SECONDS_REMAINING_IN_S1S_REUSE_TIME).addSkillName(skill).addInteger(seconds));*/
	}

	@Override
	public final int getLevel()
	{
		return getActiveSubClass() == null ? 1 : getActiveSubClass().getLevel();
	}

	@Override
	public final Sex getSex()
	{
		return getTemplate().getSex();
	}

	public int getFace()
	{
		return _face;
	}

	public void setFace(int face)
	{
		_face = face;
	}

	public int getBeautyFace()
	{
		return _beautyFace;
	}

	public void setBeautyFace(int face)
	{
		_beautyFace = face;
	}

	public int getHairColor()
	{
		return _hairColor;
	}

	public void setHairColor(int hairColor)
	{
		_hairColor = hairColor;
	}

	public int getBeautyHairColor()
	{
		return _beautyHairColor;
	}

	public void setBeautyHairColor(int hairColor)
	{
		_beautyHairColor = hairColor;
	}

	public int getHairStyle()
	{
		return _hairStyle;
	}

	public void setHairStyle(int hairStyle)
	{
		_hairStyle = hairStyle;
	}

	public int getBeautyHairStyle()
	{
		return _beautyHairStyle;
	}

	public void setBeautyHairStyle(int hairStyle)
	{
		_beautyHairStyle = hairStyle;
	}

	public void offline()
	{
		offline(Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK);
	}

	public void offline(int delay)
	{
		if(_connection != null)
		{
			_connection.setActiveChar(null);
			_connection.close(ServerCloseSocketPacket.STATIC);
			setNetConnection(null);
		}

		getAI().stopAutoplay();
		stopAutoShortcuts(AutoShortCutType.ALL);

		startAbnormalEffect(Config.SERVICES_OFFLINE_TRADE_ABNORMAL_EFFECT);
		setOfflineMode(true);

		if(isInBuffStore())
			OfflineBufferManager.getInstance().storeBufferData(this);
		else
			storePrivateStore();

		if(delay > 0)
		{
			setVar(isInBuffStore() ? "offlinebuff" : "offline", delay + (System.currentTimeMillis() / 1000));
			startKickTask(delay * 1000L);
		}
		else
			setVar(isInBuffStore() ? "offlinebuff" : "offline", Integer.MAX_VALUE);

		Party party = getParty();
		if(party != null)
			leaveParty(false);

		if(isAutoSearchParty())
		{
			PartySubstituteManager.getInstance().removeWaitingPlayer(this);
		}

		for(Servitor servitor : getServitors())
			servitor.unSummon(false);

		Olympiad.logoutPlayer(this);

		if(isFishing())
			getFishing().stop();

		MatchingRoomManager.getInstance().removeFromWaitingList(this);

		broadcastCharInfo();
		stopWaterTask();
		stopPremiumAccountTask();
		stopHourlyTask();
		stopPcBangPointsTask();
		stopTrainingCampTask();;
		stopAutoSaveTask();
		stopQuestTimers();
		stopEnableUserRelationTask();

		broadcastUserInfo(true);

		try
		{
			getInventory().store();
		}
		catch(Throwable t)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(t).log( "" );
		}

		try
		{
			store(false);
		}
		catch(Throwable t)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(t).log( "" );
		}
	}

	/**
	 * ???????????????????? ??????????????????????, ???????????? ??????????????????????, ???????????????? ?????????????????????? ?? ?????????????????? ???? ????????
	 */
	public void kick()
	{
		prepareToLogout1();
		if(_connection != null)
		{
			_connection.close(LogOutOkPacket.STATIC);
			setNetConnection(null);
		}
		prepareToLogout2();
		deleteMe();
	}

	/**
	 * ???????????????????? ???? ??????????????????????, ???????????? ???? ??????????????????????, ???????????????? ?????????????????????? ?? ?????????????????? ???? ????????
	 */
	public void restart()
	{
		prepareToLogout1();
		if(_connection != null)
		{
			_connection.setActiveChar(null);
			setNetConnection(null);
		}
		prepareToLogout2();
		deleteMe();
	}

	/**
	 * ???????????????????? ??????????????????????, ???????????? ???? ??????????????????????, ???????????????? ?????????????????????? ?? ?????????????????? ???? ????????
	 */
	public void logout(boolean toLoginScreen)
	{
		prepareToLogout1();
		if(_connection != null)
		{
			_connection.close(toLoginScreen ? ServerCloseSocketPacket.STATIC : LogOutOkPacket.STATIC);
			setNetConnection(null);
		}
		prepareToLogout2();
		deleteMe();
	}

	private void prepareToLogout1()
	{
		for(Servitor servitor : getServitors())
			sendPacket(new PetDeletePacket(servitor.getObjectId(), servitor.getServitorType()));

		if(isProcessingRequest())
		{
			Request request = getRequest();
			if(isInTrade())
			{
				Player parthner = request.getOtherPlayer(this);
				parthner.sendPacket(SystemMsg.THAT_PLAYER_IS_NOT_ONLINE);
				parthner.sendPacket(TradeDonePacket.FAIL);
			}
			request.cancel();
		}
		World.removeObjectsFromPlayer(this);
	}

	private void prepareToLogout2()
	{
		if(_isLogout.getAndSet(true))
			return;

		for(ListenerHook hook : getListenerHooks(ListenerHookType.PLAYER_QUIT_GAME))
			hook.onPlayerQuitGame(this);

		for(ListenerHook hook : ListenerHook.getGlobalListenerHooks(ListenerHookType.PLAYER_QUIT_GAME))
			hook.onPlayerQuitGame(this);

		FlagItemAttachment attachment = getActiveWeaponFlagAttachment();
		if(attachment != null)
			attachment.onLogout(this);

		setNetConnection(null);
		setIsOnline(false);

		getListeners().onExit();

		if(isFlying() && !checkLandingState())
			_stablePoint = TeleportUtils.getRestartPoint(this, RestartType.VILLAGE).getLoc();

		if(isCastingNow())
			abortCast(true, true);

		Party party = getParty();
		if(party != null)
			leaveParty(false);

		if(_observableArena != null)
			_observableArena.removeObserver(_observePoint);

		Olympiad.logoutPlayer(this);

		if(isFishing())
			getFishing().stop();

		//if(_stablePoint != null)
			//teleToLocation(_stablePoint);

		for(Servitor servitor : getServitors())
			servitor.unSummon(true);

		if(isMounted())
			_mount.onLogout();

		_friendList.notifyFriends(false);

		if(getClan() != null)
			getClan().loginClanCond(this, false);

		if(isProcessingRequest())
			getRequest().cancel();

		stopAllTimers();

		if(isInBoat())
			getBoat().removePlayer(this);

		SubUnit unit = getSubUnit();
		UnitMember member = unit == null ? null : unit.getUnitMember(getObjectId());
		if(member != null)
		{
			int sponsor = member.getSponsor();
			int apprentice = getApprentice();
			PledgeShowMemberListUpdatePacket memberUpdate = new PledgeShowMemberListUpdatePacket(this);
			for(Player clanMember : _clan.getOnlineMembers(getObjectId()))
			{
				clanMember.sendPacket(memberUpdate);
				if(clanMember.getObjectId() == sponsor)
					clanMember.sendPacket(new SystemMessage(SystemMessage.S1_YOUR_CLAN_ACADEMYS_APPRENTICE_HAS_LOGGED_OUT).addString(_name));
				else if(clanMember.getObjectId() == apprentice)
					clanMember.sendPacket(new SystemMessage(SystemMessage.S1_YOUR_CLAN_ACADEMYS_SPONSOR_HAS_LOGGED_OUT).addString(_name));
			}
			member.setPlayerInstance(this, true);
		}

		MatchingRoom room = getMatchingRoom();
		if(room != null)
		{
			if(room.getLeader() == this)
				room.disband();
			else
				room.removeMember(this, false);
		}
		setMatchingRoom(null);

		MatchingRoomManager.getInstance().removeFromWaitingList(this);

		destroyAllTraps();

		if(!_decoys.isEmpty())
		{
			for(DecoyInstance decoy : getDecoys())
			{
				decoy.unSummon();
				removeDecoy(decoy);
			}
		}

		stopPvPFlag();

		Reflection ref = getReflection();

		if(!ref.isMain())
		{
			if(ref.getReturnLoc() != null)
				_stablePoint = ref.getReturnLoc();

			ref.removeObject(this);
		}

		try
		{
			getInventory().store();
			getRefund().clear();
		}
		catch(Throwable t)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(t).log( "" );
		}

		try
		{
			store(false);
		}
		catch(Throwable t)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(t).log( "" );
		}
	}

	/**
	 * @return a table containing all L2RecipeList of the L2Player.<BR><BR>
	 */
	public Collection<RecipeTemplate> getDwarvenRecipeBook()
	{
		return _recipebook.values();
	}

	public Collection<RecipeTemplate> getCommonRecipeBook()
	{
		return _commonrecipebook.values();
	}

	public int recipesCount()
	{
		return _commonrecipebook.size() + _recipebook.size();
	}

	public boolean hasRecipe(final RecipeTemplate id)
	{
		return _recipebook.containsValue(id) || _commonrecipebook.containsValue(id);
	}

	public boolean findRecipe(final int id)
	{
		return _recipebook.containsKey(id) || _commonrecipebook.containsKey(id);
	}

	/**
	 * Add a new L2RecipList to the table _recipebook containing all L2RecipeList of the L2Player
	 */
	public void registerRecipe(final RecipeTemplate recipe, boolean saveDB)
	{
		if(recipe == null)
			return;

		if(recipe.isCommon())
			_commonrecipebook.put(recipe.getId(), recipe);
		else
			_recipebook.put(recipe.getId(), recipe);

		if(saveDB)
			mysql.set("REPLACE INTO character_recipebook (char_id, id) VALUES(?,?)", getObjectId(), recipe.getId());
	}

	/**
	 * Remove a L2RecipList from the table _recipebook containing all L2RecipeList of the L2Player
	 */
	public void unregisterRecipe(final int RecipeID)
	{
		if(_recipebook.containsKey(RecipeID))
		{
			mysql.set("DELETE FROM `character_recipebook` WHERE `char_id`=? AND `id`=? LIMIT 1", getObjectId(), RecipeID);
			_recipebook.remove(RecipeID);
		}
		else if(_commonrecipebook.containsKey(RecipeID))
		{
			mysql.set("DELETE FROM `character_recipebook` WHERE `char_id`=? AND `id`=? LIMIT 1", getObjectId(), RecipeID);
			_commonrecipebook.remove(RecipeID);
		}
		else
			_log.atWarning().log( "Attempted to remove unknown RecipeList%s", RecipeID );
	}

	public QuestState getQuestState(int id)
	{
		questRead.lock();
		try
		{
			return _quests.get(id);
		}
		finally
		{
			questRead.unlock();
		}
	}

	public QuestState getQuestState(Quest quest)
	{
		return getQuestState(quest.getId());
	}

	public boolean isQuestCompleted(int id)
	{
		QuestState qs = getQuestState(id);
		return qs != null && qs.isCompleted();
	}

	public boolean isQuestCompleted(Quest quest)
	{
		return isQuestCompleted(quest.getId());
	}

	public void setQuestState(QuestState qs)
	{
		questWrite.lock();
		try
		{
			_quests.put(qs.getQuest().getId(), qs);
		}
		finally
		{
			questWrite.unlock();
		}
	}

	public void removeQuestState(int id)
	{
		questWrite.lock();
		try
		{
			_quests.remove(id);
		}
		finally
		{
			questWrite.unlock();
		}
	}

	public void removeQuestState(Quest quest)
	{
		removeQuestState(quest.getId());
	}

	public Quest[] getAllActiveQuests()
	{
		List<Quest> quests = new ArrayList<Quest>(_quests.size());
		questRead.lock();
		try
		{
			for(final QuestState qs : _quests.valueCollection())
			{
				if(qs.isStarted())
					quests.add(qs.getQuest());
			}
		}
		finally
		{
			questRead.unlock();
		}
		return quests.toArray(new Quest[quests.size()]);
	}

	public QuestState[] getAllQuestsStates()
	{
		questRead.lock();
		try
		{
			return _quests.values(new QuestState[_quests.size()]);
		}
		finally
		{
			questRead.unlock();
		}
	}

	public List<QuestState> getQuestsForEvent(NpcInstance npc, QuestEventType event)
	{
		List<QuestState> states = new ArrayList<QuestState>();
		Set<Quest> quests = npc.getTemplate().getEventQuests(event);
		if(quests != null)
		{
			QuestState qs;
			for(Quest quest : quests)
			{
				qs = getQuestState(quest);
				if(qs != null && !qs.isCompleted())
					states.add(getQuestState(quest));
			}
		}
		return states;
	}

	public void processQuestEvent(int questId, String event, NpcInstance npc)
	{
		if(event == null)
			event = "";
		QuestState qs = getQuestState(questId);
		if(qs == null)
		{
			Quest q = QuestHolder.getInstance().getQuest(questId);
			if(q == null)
			{
				_log.atWarning().log( "Quest ID[%s] not found!", questId );
				return;
			}
			qs = q.newQuestState(this);
		}
		if(qs == null || qs.isCompleted())
			return;
		qs.getQuest().notifyEvent(event, qs, npc);
		sendPacket(new QuestListPacket(this)); // TODO: ??????????? ??.0
	}

	public boolean isInventoryFull()
	{
		if(getWeightPenalty() >= 3 || getInventoryLimit() * 0.8 < getInventory().getSize())
			return true;
		return false;
	}

	/**
	 * ???????????????? ???? ???????????????????????? ?????????????????? ?? ?????????????? ?? ???????? ?????? ?????????????? ?? ??????????????
	 *
	 * @return true ???????? ???? ???????????????? ???????????? ??????????????
	 */
	public boolean isQuestContinuationPossible(boolean msg)
	{
		if(isInventoryFull() || Config.QUEST_INVENTORY_MAXIMUM * 0.8 < getInventory().getQuestSize())
		{
			if(msg)
				sendPacket(SystemMsg.PROGRESS_IN_A_QUEST_IS_POSSIBLE_ONLY_WHEN_YOUR_INVENTORYS_WEIGHT_AND_SLOT_COUNT_ARE_LESS_THAN_80_PERCENT_OF_CAPACITY);
			return false;
		}
		return true;
	}

	/**
	 * ?????????????????????????? ?? ???????????????????? ?????? ?????????????????? ??????????????
	 */
	public void stopQuestTimers()
	{
		for(QuestState qs : getAllQuestsStates())
			if(qs.isStarted())
				qs.pauseQuestTimers();
			else
				qs.stopQuestTimers();
	}

	/**
	 * ?????????????????????????????? ?????? ?????????????????? ??????????????
	 */
	public void resumeQuestTimers()
	{
		for(QuestState qs : getAllQuestsStates())
			qs.resumeQuestTimers();
	}

	// ----------------- End of Quest Engine -------------------

	public Collection<ShortCut> getAllShortCuts()
	{
		return _shortCuts.getAllShortCuts();
	}

	public ShortCut getShortCut(int slot, int page)
	{
		return _shortCuts.getShortCut(slot, page);
	}

	public void registerShortCut(ShortCut shortcut)
	{
		_shortCuts.registerShortCut(shortcut);
	}

	public void deleteShortCut(int slot, int page)
	{
		_shortCuts.deleteShortCut(slot, page);
	}

	public List<ShortCut> getAllEnabledAutoShortcuts()
	{
		return _shortCuts.getEnabledAutoShortcuts(AutoShortCutType.ALL);
	}

	public void startAutoShortcuts(AutoShortCutType type)
	{
		_shortCuts.startAutoShortcuts(type);
	}

	public void stopAutoShortcuts(AutoShortCutType type)
	{
		_shortCuts.stopAutoShortcuts(type);
	}

	public void enableAutoShortcut(Player player, ShortCut shortCut)
	{
		_shortCuts.enableAutoShortcut(player, shortCut);
	}

	public void disableAutoShortcut(Player player, ShortCut shortCut)
	{
		_shortCuts.disableAutoShortcut(player, shortCut);
	}

	public AtomicBoolean getAutoShortcutsCast()
	{
		return _shortCuts.getAutoShortcutsCast();
	}

	public void registerMacro(Macro macro)
	{
		_macroses.registerMacro(macro);
	}

	public void deleteMacro(int id)
	{
		_macroses.deleteMacro(id);
	}

	public MacroList getMacroses()
	{
		return _macroses;
	}

	public boolean isCastleLord(int castleId)
	{
		return _clan != null && isClanLeader() && _clan.getCastle() == castleId;
	}

	public int getPkKills()
	{
		return _pkKills;
	}

	public void setPkKills(final int pkKills)
	{
		_pkKills = pkKills;
	}

	public long getCreateTime()
	{
		return _createTime;
	}

	public void setCreateTime(final long createTime)
	{
		_createTime = createTime;
	}

	public int getDeleteTimer()
	{
		return _deleteTimer;
	}

	public void setDeleteTimer(final int deleteTimer)
	{
		_deleteTimer = deleteTimer;
	}

	@Override
	public int getCurrentLoad()
	{
		return getInventory().getTotalWeight();
	}

	public long getLastAccess()
	{
		return _lastAccess;
	}

	public void setLastAccess(int value)
	{
		_lastAccess = value;
	}

	public int getRecomHave()
	{
		return _recomHave;
	}

	public void setRecomHave(int value)
	{
		if(value > 255)
			_recomHave = 255;
		else if(value < 0)
			_recomHave = 0;
		else
			_recomHave = value;
	}

	public int getRecomLeft()
	{
		return _recomLeft;
	}

	public void setRecomLeft(final int value)
	{
		_recomLeft = value;
	}

	public void giveRecom(final Player target)
	{
		int targetRecom = target.getRecomHave();
		if(targetRecom < 255)
			target.addRecomHave(1);
		if(getRecomLeft() > 0)
			setRecomLeft(getRecomLeft() - 1);

		sendUserInfo(true);
	}

	public void addRecomHave(final int val)
	{
		setRecomHave(getRecomHave() + val);
		broadcastUserInfo(true);
	}

	@Override
	public int getKarma()
	{
		return _karma;
	}

	public void setKarma(int karma)
	{
		if(_karma == karma)
			return;

		_karma = Math.min(0, karma);

		sendChanges();

		for(Servitor servitor : getServitors())
			servitor.broadcastCharInfo();
	}

	@Override
	public int getMaxLoad()
	{
		// Weight Limit = (CON Modifier*69000) * Skills
		// Source http://l2p.bravehost.com/weightlimit.html (May 2007)
		double baseLoad = Math.floor(BaseStats.CON.calcBonus(this) * 69000 * Config.MAXLOAD_MODIFIER);
		return (int) getStat().getValue(DoubleStat.WEIGHT_LIMIT, baseLoad);
	}

	@Override
	public void updateAbnormalIcons()
	{
		if(entering || isLogoutStarted())
			return;

		super.updateAbnormalIcons();
	}

	@Override
	public void updateAbnormalIconsImpl()
	{
		Abnormal[] effects = getAbnormalList().toArray();
		Arrays.sort(effects, AbnormalsComparator.getInstance());

		PartySpelledPacket ps = new PartySpelledPacket(this, false);
		AbnormalStatusUpdatePacket abnormalStatus = new AbnormalStatusUpdatePacket();

		for(Abnormal effect : effects)
		{
			if(effect != null)
			{
				if(effect.checkAbnormalType(AbnormalType.HP_RECOVER))
					sendPacket(new ShortBuffStatusUpdatePacket(effect));
				else
					effect.addIcon(abnormalStatus);
				if(_party != null)
					effect.addPartySpelledIcon(ps);
			}
		}

		sendPacket(abnormalStatus);
		if(_party != null)
			_party.broadCast(ps);

		if(isInOlympiadMode() && isOlympiadCompStart())
		{
			OlympiadGame olymp_game = _olympiadGame;
			if(olymp_game != null)
			{
				ExOlympiadSpelledInfoPacket olympiadSpelledInfo = new ExOlympiadSpelledInfoPacket();

				for(Abnormal effect : effects)
					if(effect != null)
						effect.addOlympiadSpelledIcon(this, olympiadSpelledInfo);

				sendPacket(olympiadSpelledInfo);

				for(ObservePoint observer : olymp_game.getObservers())
					observer.sendPacket(olympiadSpelledInfo);
			}
		}

		final List<SingleMatchEvent> events = getEvents(SingleMatchEvent.class);
		for(SingleMatchEvent event : events)
			event.onEffectIconsUpdate(this, effects);

		super.updateAbnormalIconsImpl();
	}

	@Override
	public int getWeightPenalty()
	{
		return getSkillLevel(4270, 0);
	}

	public void refreshOverloaded()
	{
		if(isLogoutStarted() || getMaxLoad() <= 0)
			return;

		setOverloaded(getCurrentLoad() > getMaxLoad());
		double weightproc = (getCurrentLoad() - getBonusWeightPenalty()) * 100.0 / getMaxLoad();
		int newWeightPenalty = 0;

		if(weightproc < 50)
			newWeightPenalty = 0;
		else if(weightproc < 66.6)
			newWeightPenalty = 1;
		else if(weightproc < 80)
			newWeightPenalty = 2;
		else if(weightproc < 100)
			newWeightPenalty = 3;
		else
			newWeightPenalty = 4;

		int current = getWeightPenalty();
		if(current == newWeightPenalty)
			return;

		boolean update = false;
		if(newWeightPenalty > 0) {
			SkillEntry skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 4270, newWeightPenalty);
			if(skillEntry != null) {
				if(!skillEntry.equals(addSkill(skillEntry)))
					update = true;
			}
		}
		else if(super.removeSkill(getKnownSkill(4270)) != null)
			update = true;

		if(update) {
			sendSkillList();
			sendEtcStatusUpdate();
			updateStats(false, true);
		}
	}

	public int getExpertiseArmorPenalty()
	{
		return _expertiseArmorPenalty;
	}

	public int getExpertiseWeaponPenalty()
	{
		return _expertiseWeaponPenalty;
	}

	public void refreshExpertisePenalty()
	{
		if(isLogoutStarted())
			return;

		final ItemGrade expertiseLevel = getStat().getExpertiseLevel().plusLevel(getStat().getExpertisePenaltyBonus());

		int armorPenalty = 0;
		int weaponPenalty = 0;

		Collection<ItemInstance> items = getInventory().getPaperdollItems(item -> item != null && item.getItemType() != EtcItemType.ARROW && item.getItemType() != EtcItemType.BOLT && item.getTemplate().getGrade().isGreater(expertiseLevel));
		for (ItemInstance item : items)
		{
			if (item.isArmor())
			{
				// Armor penalty level increases depending on amount of penalty armors equipped, not grade level difference.
				armorPenalty = MathUtils.INSTANCE.constrain(armorPenalty + 1, 0, 4);
			}
			else
			{
				// Weapon penalty level increases based on grade difference.
				weaponPenalty = MathUtils.INSTANCE.constrain(item.getTemplate().getGrade().getLevel() - expertiseLevel.getLevel(), 0, 4);
			}
		}

		boolean changed = false;

		if ((getExpertiseWeaponPenalty() != weaponPenalty) || (getSkillLevel(CommonSkill.WEAPON_GRADE_PENALTY.getId()) != weaponPenalty))
		{
			_expertiseWeaponPenalty = weaponPenalty;
			if (_expertiseWeaponPenalty > 0)
			{
				addSkill(SkillEntry.makeSkillEntry(SkillEntryType.NONE, CommonSkill.WEAPON_GRADE_PENALTY.getId(), _expertiseWeaponPenalty));
			}
			else
			{
				removeSkillById(CommonSkill.WEAPON_GRADE_PENALTY.getId());
			}
			changed = true;
		}

		if ((getExpertiseArmorPenalty() != armorPenalty) || (getSkillLevel(CommonSkill.ARMOR_GRADE_PENALTY.getId()) != armorPenalty))
		{
			_expertiseArmorPenalty = armorPenalty;
			if (_expertiseArmorPenalty > 0)
			{
				addSkill(SkillEntry.makeSkillEntry(SkillEntryType.NONE, CommonSkill.ARMOR_GRADE_PENALTY.getId(), _expertiseArmorPenalty));
			}
			else
			{
				removeSkillById(CommonSkill.ARMOR_GRADE_PENALTY.getId());
			}
			changed = true;
		}

		if(changed)
		{
			// getInventory().refreshEquip();

			sendSkillList();
			sendEtcStatusUpdate();
			updateStats(true, false);
		}
	}

	public int getPvpKills()
	{
		return _pvpKills;
	}

	public void setPvpKills(int pvpKills)
	{
		_pvpKills = pvpKills;
	}

	public ClassLevel getClassLevel()
	{
		return getClassId().getClassLevel();
	}

	public boolean isAcademyGraduated()
	{
		return getVarBoolean(ACADEMY_GRADUATED_VAR, false);
	}

	/**
	 * Set the template of the L2Player.
	 *
	 * @param id The Identifier of the L2PlayerTemplate to set to the L2Player
	 */
	public synchronized void setClassId(final int id, boolean noban)
	{
		ClassId classId = ClassId.valueOf(id);
		if(classId.isDummy())
			return;

		if(!noban && !(classId.equalsOrChildOf(getClassId()) || getPlayerAccess().CanChangeClass || Config.EVERYBODY_HAS_ADMIN_RIGHTS))
		{
			Thread.dumpStack();
			return;
		}

		PlayerTemplate template = PlayerTemplateHolder.getInstance().getPlayerTemplate(getRace(), classId, getSex());
		if(template == null)
		{
			_log.atSevere().log( "Missing template for classId: %s", id );
			return;
		}
		setTemplate(template);

		//???????? ?????????? ID ???? ?????????????????????? ?????????????????? ?????????????? ???????????? ?????? ?????????? ??????????
		if(!_subClassList.containsClassId(id))
		{
			final SubClass cclass = getActiveSubClass();
            final ClassId oldClass = ClassId.valueOf(cclass.getClassId());

			_subClassList.changeSubClassId(oldClass.getId(), id);
			changeClassInDb(oldClass.getId(), id);

			onReceiveNewClassId(oldClass, classId);

			storeCharSubClasses();

			getListeners().onClassChange(oldClass, classId);

			for(QuestState qs : getAllQuestsStates())
				qs.getQuest().notifyTutorialEvent("CE", false, "100", qs);
		}
		else
			getListeners().onClassChange(null, classId);

		broadcastUserInfo(true);

		// Update class icon in party and clan
		if(isInParty())
			getParty().broadCast(new PartySmallWindowUpdatePacket(this));
		if(getClan() != null)
			getClan().broadcastToOnlineMembers(new PledgeShowMemberListUpdatePacket(this));
		if(_matchingRoom != null)
			_matchingRoom.broadcastPlayerUpdate(this);
	}

	private void onReceiveNewClassId(ClassId oldClass, ClassId newClass)
	{
		if(oldClass != null)
		{
			if(isBaseClassActive())
			{
				OlympiadParticipiantData participant = Olympiad.getParticipantInfo(getObjectId());
				if(participant != null)
					participant.setClassId(newClass.getId());
			}

			if(!newClass.equalsOrChildOf(oldClass))
			{
				removeAllSkills();
				restoreSkills();
				rewardSkills(false);

				checkSkills();

				refreshExpertisePenalty();

				getInventory().refreshEquip();
				getInventory().validateItems();

				getHennaList().refreshStats(true);

				sendSkillList();

				//updateStats();
			}
			else
				rewardSkills(true);
		}
	}

	public long getExp()
	{
		return getActiveSubClass() == null ? 0 : getActiveSubClass().getExp();
	}

	public long getMaxExp()
	{
		return getActiveSubClass() == null ? Experience.getExpForLevel(Experience.getMaxLevel() + 1) : getActiveSubClass().getMaxExp();
	}

	public void setEnchantScroll(final ItemInstance scroll)
	{
		_enchantScroll = scroll;
	}

	public ItemInstance getEnchantScroll()
	{
		return _enchantScroll;
	}

	public void setAppearanceStone(final ItemInstance stone)
	{
		_appearanceStone = stone;
	}

	public ItemInstance getAppearanceStone()
	{
		return _appearanceStone;
	}

	public void setAppearanceExtractItem(final ItemInstance item)
	{
		_appearanceExtractItem = item;
	}

	public ItemInstance getAppearanceExtractItem()
	{
		return _appearanceExtractItem;
	}

	public void addExpAndCheckBonus(MonsterInstance mob, final double noRateExp, double noRateSp)
	{
		if(getActiveSubClass() == null)
			return;

		if(noRateExp > 0)
		{
			if(!(getVarBoolean("NoExp") && getExp() == Experience.getExpForLevel(getLevel() + 1) - 1))
			{
				Clan clan = getClan();
				if(clan != null)
				{
					// TODO: [Bonux] ???????????????????? ?????????????? ???? ????????.
					int huntingPoints = Math.max((int) (noRateExp * (getRateExp() / Config.RATE_XP_BY_LVL[getLevel()]) / Math.pow(getLevel(), 2) * Config.CLAN_HUNTING_PROGRESS_RATE), 1);
					clan.addHuntingProgress(huntingPoints);
				}

				Elemental elemental = getElementalList().get(mob.getActiveElement().getDominant());
				if(elemental != null)
				{
					ElementalElement element = elemental.getElement();
					double mulRate = 0.;
					switch(elemental.getElement())
					{
						case FIRE:
							mulRate = getStat().getMul(DoubleStat.FIRE_ELEMENTAL_EXP_RATE);
							break;
						case WATER:
							mulRate = getStat().getMul(DoubleStat.WATER_ELEMENTAL_EXP_RATE);
							break;
						case WIND:
							mulRate = getStat().getMul(DoubleStat.WIND_ELEMENTAL_EXP_RATE);
							break;
						case EARTH:
							mulRate = getStat().getMul(DoubleStat.EARTH_ELEMENTAL_EXP_RATE);
							break;
					}
					// TODO: Check formula.
					long elementalExp = (long) (noRateExp / 281. * Config.RATE_ELEMENTAL_XP * mulRate);
					if(elementalExp > 0)
						elemental.addExp(this, elementalExp);
				}
			}
		}

		long normalExp = (long) (noRateExp * getRateExp() * (mob.isRaid() ? Config.RATE_XP_RAIDBOSS_MODIFIER : 1.));
		long normalSp = (long) (noRateSp * getRateSp() * (mob.isRaid() ? Config.RATE_SP_RAIDBOSS_MODIFIER : 1.));

		long expWithoutBonus = (long) (noRateExp * Config.RATE_XP_BY_LVL[getLevel()]);
		long spWithoutBonus = (long) (noRateSp * Config.RATE_SP_BY_LVL[getLevel()]);

		addExpAndSp(normalExp, normalSp, normalExp - expWithoutBonus, normalSp - spWithoutBonus, false, true, false, true, true);
	}

	@Override
	public void addExpAndSp(long exp, long sp)
	{
		addExpAndSp(exp, sp, -1, -1, false, false, Config.ALT_DELEVEL_ON_DEATH_PENALTY_MIN_LEVEL > -1 && getLevel() > Config.ALT_DELEVEL_ON_DEATH_PENALTY_MIN_LEVEL, true, true);
	}

	public void addExpAndSp(long exp, long sp, boolean delevel)
	{
		addExpAndSp(exp, sp, -1, -1, false, false, delevel, true, true);
	}

	public void addExpAndSp(long addToExp, long addToSp, long bonusAddExp, long bonusAddSp, boolean applyRate, boolean applyToPet, boolean delevel, boolean clearKarma, boolean sendMsg)
	{
		if(getActiveSubClass() == null)
			return;

		if(addToExp < 0 && isFakePlayer())
			return;

		if(applyRate)
		{
			addToExp *= getRateExp();
			addToSp *= getRateSp();
		}

		PetInstance pet = getPet();
		if(addToExp > 0)
		{
			if(applyToPet)
			{
				if(pet != null && !pet.isDead() && !pet.getData().isOfType(PetType.SPECIAL))
				{
					// Sin Eater ???????????????? ?????? ?????????? ?? ??????????????????
					if(pet.getData().isOfType(PetType.KARMA))
					{
						pet.addExpAndSp(addToExp, 0);
						addToExp = 0;
					}
					else if(pet.getExpPenalty() > 0f)
					{
						if(pet.getLevel() > getLevel() - 20 && pet.getLevel() < getLevel() + 5)
						{
							pet.addExpAndSp((long) (addToExp * pet.getExpPenalty()), 0);
							addToExp *= 1. - pet.getExpPenalty();
						}
						else
						{
							pet.addExpAndSp((long) (addToExp * pet.getExpPenalty() / 5.), 0);
							addToExp *= 1. - pet.getExpPenalty() / 5.;
						}
					}
					else if(pet.isSummon())
						addToExp *= 1. - pet.getExpPenalty();
				}
			}

			if(clearKarma)
			{
				// Remove Karma when the player kills L2MonsterInstance
				//TODO [G1ta0] ?????????????? ?? ?????????? ???????????????????? ???????????? ?????? ???????????????? ????????
				if(isPK() && !isInZoneBattle())
				{
					int karmaLost = Formulas.INSTANCE.calculateKarmaLost(this, addToExp);
					if(karmaLost > 0)
					{
						_karma += karmaLost;
						if(_karma > 0)
							_karma = 0;

						if(sendMsg)
							sendPacket(new SystemMessagePacket(SystemMsg.YOUR_FAME_HAS_BEEN_CHANGED_TO_S1).addInteger(_karma));
					}
				}
			}

			long max_xp = getVarBoolean("NoExp") || isInDuel() ? Experience.getExpForLevel(getLevel() + 1) - 1 : getMaxExp();
			addToExp = Math.min(addToExp, max_xp - getExp());
		}

		int oldLvl = getActiveSubClass().getLevel();

		getActiveSubClass().addExp(addToExp, delevel);
		getActiveSubClass().addSp(addToSp);

		if(addToExp > 0)
		{
			getListeners().onExpReceive(addToExp, bonusAddExp >= 0);
			_receivedExp += addToExp;
		}

		if(sendMsg)
		{
			if((addToExp > 0 || addToSp > 0) && bonusAddExp >= 0 && bonusAddSp >= 0)
				sendPacket(new SystemMessagePacket(SystemMsg.YOU_HAVE_ACQUIRED_S1_EXP_BONUS_S2_AND_S3_SP_BONUS_S4).addLong(addToExp).addLong(bonusAddExp).addInteger(addToSp).addInteger((int) bonusAddSp));
			else if(addToSp > 0 && addToExp == 0)
				sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_ACQUIRED_S1_SP).addNumber(addToSp));
			else if(addToSp > 0 && addToExp > 0)
				sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_EARNED_S1_EXPERIENCE_AND_S2_SP).addNumber(addToExp).addNumber(addToSp));
			else if(addToSp == 0 && addToExp > 0)
				sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_EARNED_S1_EXPERIENCE).addNumber(addToExp));
		}

		int level = getActiveSubClass().getLevel();
		if(level != oldLvl)
		{
			levelSet(level - oldLvl);
			getListeners().onLevelChange(oldLvl, level);

			for(ListenerHook hook : ListenerHook.getGlobalListenerHooks(ListenerHookType.PLAYER_GLOBAL_LEVEL_UP))
				hook.onPlayerGlobalLevelUp(this, oldLvl, level);
		}

		if(pet != null && pet.getData().isOfType(PetType.SPECIAL))
		{
			pet.setLevel(getLevel());
			pet.setExp(pet.getExpForNextLevel());
			pet.broadcastStatusUpdate();
		}

		sendCurrentHpMpCpExpSp();
	}

	private boolean _dontRewardSkills = false; // ???????????? ????????????????, ???? ??????????????.

	public void rewardSkills(boolean send)
	{
		rewardSkills(send, true, Config.AUTO_LEARN_SKILLS, true);
	}

	public int rewardSkills(boolean send, boolean checkShortCuts, boolean learnAllSkills, boolean checkRequiredItems)
	{
		if(_dontRewardSkills)
			return 0;

		List<SkillLearn> skillLearns = new ArrayList<SkillLearn>(SkillAcquireHolder.getInstance().getAvailableNextLevelsSkills(this, AcquireType.NORMAL));
		Collections.sort(skillLearns);
		Collections.reverse(skillLearns);

		IntObjectMap<SkillLearn> skillsToLearnMap = new HashIntObjectMap<SkillLearn>();
		for(SkillLearn sl : skillLearns)
		{
			if(!(sl.isAutoGet() && ((learnAllSkills && (!checkRequiredItems || !sl.haveRequiredItemsForLearn(AcquireType.NORMAL))) || sl.isFreeAutoGet(AcquireType.NORMAL))))
			{
				// ???????? ???????????????????? ?????????????? ???????????? ???????????? ???? ??????????????????, ???? ???? ???????? ?????????????????? ?????????????? ?????????????? ????????????.
				skillsToLearnMap.remove(sl.getId());
				continue;
			}

			if(!skillsToLearnMap.containsKey(sl.getId()))
				skillsToLearnMap.put(sl.getId(), sl);
		}

		boolean update = false;
		int addedSkillsCount = 0;

		for(SkillLearn sl : skillsToLearnMap.valueCollection())
		{
			SkillEntry skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.NONE, sl.getId(), sl.getLevel());
			if(skillEntry == null)
				continue;

			if(addSkill(skillEntry, true) == null)
				addedSkillsCount++;

			if(checkShortCuts && getAllShortCuts().size() > 0 && skillEntry.getLevel() > 1)
				updateSkillShortcuts(skillEntry.getId(), skillEntry.getLevel());

			update = true;
		}

		if(isTransformed())
		{
			boolean added = false;
			// ?????????????????? ???????????? ?????????????????????????? ?????????????????? ???? ???????????? ??????????????????
			for(SkillLearn sl : _transform.getAddtionalSkills())
			{
				if(sl.getMinLevel() > getLevel())
					continue;

				SkillEntry skillEntry = _transformSkills.get(sl.getId());
				if(skillEntry != null && skillEntry.getLevel() >= sl.getLevel())
					continue;

				skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.NONE, sl.getId(), sl.getLevel());
				if(skillEntry == null)
					continue;

				_transformSkills.remove(skillEntry.getId());
				_transformSkills.put(skillEntry.getId(), skillEntry);

				update = true;
				added = true;
			}

			if(added)
			{
				for(SkillEntry skillEntry : _transformSkills.valueCollection())
				{
					if(addSkill(skillEntry, false) == null)
						addedSkillsCount++;
				}
			}
		}

		//updateStats();

		if(send && update)
			sendSkillList();

		return addedSkillsCount;
	}

	public Race getRace()
	{
		return ClassId.valueOf(getBaseClassId()).getRace();
	}

	public ClassType getBaseClassType()
	{
		return ClassId.valueOf(getBaseClassId()).getType();
	}

	public long getSp()
	{
		return getActiveSubClass() == null ? 0 : getActiveSubClass().getSp();
	}

	public void setSp(long sp)
	{
		if(getActiveSubClass() != null)
			getActiveSubClass().setSp(sp);
	}

	public int getClanId()
	{
		return _clan == null ? 0 : _clan.getClanId();
	}

	public long getLeaveClanTime()
	{
		return _leaveClanTime;
	}

	public long getDeleteClanTime()
	{
		return _deleteClanTime;
	}

	public void setLeaveClanTime(final long time)
	{
		_leaveClanTime = time;
	}

	public void setDeleteClanTime(final long time)
	{
		_deleteClanTime = time;
	}

	public void setOnlineTime(final long time)
	{
		_onlineTime = time;
		_onlineBeginTime = System.currentTimeMillis();
	}

	public int getOnlineTime()
	{
		long result = _onlineTime;
		if(_onlineBeginTime > 0) {
			result += System.currentTimeMillis() - _onlineBeginTime;
		}
		if(_offlineStartTime > 0) {
			result -= System.currentTimeMillis() - _offlineStartTime;
		}
		return (int) (result / 1000);
	}

	public long getOnlineBeginTime()
	{
		return _onlineBeginTime;
	}

	public void setNoChannel(final long time)
	{
		_NoChannel = time;
		if(_NoChannel > 2145909600000L || _NoChannel < 0)
			_NoChannel = -1;

		if(_NoChannel > 0)
			_NoChannelBegin = System.currentTimeMillis();
		else
			_NoChannelBegin = 0;
	}

	public long getNoChannel()
	{
		return _NoChannel;
	}

	public long getNoChannelRemained()
	{
		if(_NoChannel == 0)
			return 0;
		else if(_NoChannel < 0)
			return -1;
		else
		{
			long remained = _NoChannel - System.currentTimeMillis() + _NoChannelBegin;
			if(remained < 0)
				return 0;

			return remained;
		}
	}

	public boolean isChatBlocked()
	{
		return getStat().has(BooleanStat.BLOCK_CHAT);
	}

	public boolean isPartyBlocked()
	{
		return getFlags().getPartyBlocked().get();
	}

	public boolean isVioletBoy()
	{
		return getFlags().getVioletBoy().get();
	}

	public void setLeaveClanCurTime()
	{
		_leaveClanTime = System.currentTimeMillis();
	}

	public void setDeleteClanCurTime()
	{
		_deleteClanTime = System.currentTimeMillis();
	}

	public boolean canJoinClan()
	{
		if(_leaveClanTime == 0)
			return true;
		if(System.currentTimeMillis() - _leaveClanTime >= Config.ALT_CLAN_LEAVE_PENALTY_TIME * 60 * 60 * 1000L)
		{
			_leaveClanTime = 0;
			return true;
		}
		return false;
	}

	public boolean canCreateClan()
	{
		if(_deleteClanTime == 0)
			return true;
		if(System.currentTimeMillis() - _deleteClanTime >= Config.ALT_CLAN_CREATE_PENALTY_TIME * 60 * 60 * 1000L)
		{
			_deleteClanTime = 0;
			return true;
		}
		return false;
	}

	public IBroadcastPacket canJoinParty(Player inviter)
	{
		Request request = getRequest();
		if(request != null && request.isInProgress() && request.getOtherPlayer(this) != inviter)
			return SystemMsg.WAITING_FOR_ANOTHER_REPLY.packet(inviter); // ??????????
		if(isBlockAll() || getMessageRefusal()) // ???????? ??????????
			return SystemMsg.THAT_PERSON_IS_IN_MESSAGE_REFUSAL_MODE.packet(inviter);
		if(isInParty()) // ??????
			return new SystemMessagePacket(SystemMsg.C1_IS_A_MEMBER_OF_ANOTHER_PARTY_AND_CANNOT_BE_INVITED).addName(this);
		if(isPartyBlocked()) // ?????????????? ???? ???????? ?? ????????????.
			return new SystemMessagePacket(SystemMsg.C1_HAS_BEEN_REPORTED_AS_AN_ILLEGAL_PROGRAM_USER_AND_CANNOT_JOIN_A_PARTY).addName(this);
		if(inviter.getReflection() != getReflection()) // ?? ???????????? ??????????????????
			if(!inviter.getReflection().isMain() && !getReflection().isMain())
				return SystemMsg.INVALID_TARGET.packet(inviter);
		if(inviter.isInOlympiadMode() || isInOlympiadMode()) // ??????????????????
			return SystemMsg.A_USER_CURRENTLY_PARTICIPATING_IN_THE_OLYMPIAD_CANNOT_SEND_PARTY_AND_FRIEND_INVITATIONS.packet(inviter);
		if(!inviter.getPlayerAccess().CanJoinParty || !getPlayerAccess().CanJoinParty) // ????????
			return SystemMsg.INVALID_TARGET.packet(inviter);
		for(Event event : getEvents())
		{
			if(!event.canJoinParty(inviter, this))
				return SystemMsg.INVALID_TARGET.packet(inviter);
		}
		return null;
	}

	@Override
	@NotNull
	public PcInventory getInventory()
	{
		return _inventory;
	}

	@Override
	public long getWearedMask()
	{
		return _inventory.getWearedMask();
	}

	public PcFreight getFreight()
	{
		return _freight;
	}

	public void removeItemFromShortCut(final int objectId)
	{
		_shortCuts.deleteShortCutByObjectId(objectId);
	}

	public void removeSkillFromShortCut(final int skillId)
	{
		_shortCuts.deleteShortCutBySkillId(skillId);
	}

	@Override
	public boolean isSitting()
	{
		return _isSitting;
	}

	public void setSitting(boolean val)
	{
		_isSitting = val;
	}

	public boolean getSittingTask()
	{
		return sittingTaskLaunched;
	}

	public ChairInstance getChairObject()
	{
		return _chairObject;
	}

	@Override
	public void sitDown(ChairInstance chair)
	{
		if(isSitting() || sittingTaskLaunched || isAlikeDead())
			return;

		if(isStunned() || isSleeping() || isDecontrolled() || isAttackingNow() || isCastingNow() || getMovement().isMoving())
		{
			getAI().setNextAction(AINextAction.REST, null, null, false, false);
			return;
		}

		resetWaitSitTime();
		getAI().setIntention(CtrlIntention.AI_INTENTION_REST, null, null);

		if(chair == null)
			broadcastPacket(new ChangeWaitTypePacket(this, ChangeWaitTypePacket.WT_SITTING));
		else
		{
			chair.setSeatedPlayer(this);
			broadcastPacket(new ChairSitPacket(this, chair));
		}

		_chairObject = chair;
		setSitting(true);
		sittingTaskLaunched = true;
		ThreadPoolManager.getInstance().schedule(new EndSitDownTask(this), 2500);
	}

	@Override
	public void standUp()
	{
		if(!isSitting() || sittingTaskLaunched || isInStoreMode() || isAlikeDead())
			return;

		if (getStat().has(BooleanStat.RELAX)) {
			for (Abnormal abnormal : getAbnormalList()) {
				if (abnormal.getSkill().getNextAction() == NextActionType.SIT) {
					getAbnormalList().stop(abnormal.getId());
				}
			}
		}

		getAI().clearNextAction();
		broadcastPacket(new ChangeWaitTypePacket(this, ChangeWaitTypePacket.WT_STANDING));

		if(_chairObject != null)
			_chairObject.setSeatedPlayer(this);

		_chairObject = null;
		//setSitting(false);
		sittingTaskLaunched = true;
		ThreadPoolManager.getInstance().schedule(new EndStandUpTask(this), 2500);
	}

	public void updateWaitSitTime()
	{
		if(_waitTimeWhenSit < 200)
			_waitTimeWhenSit += 2;
	}

	public int getWaitSitTime()
	{
		return _waitTimeWhenSit;
	}

	public void resetWaitSitTime()
	{
		_waitTimeWhenSit = 0;
	}

	public Warehouse getWarehouse()
	{
		return _warehouse;
	}

	public ItemContainer getRefund()
	{
		return _refund;
	}

	public long getAdena()
	{
		return getInventory().getAdena();
	}

	public boolean reduceAdena(long adena)
	{
		return reduceAdena(adena, false);
	}

	/**
	 * ???????????????? ?????????? ?? ????????????.<BR><BR>
	 *
	 * @param adena  - ?????????????? ?????????? ??????????????
	 * @param notify - ???????????????????? ?????????????????? ??????????????????
	 * @return true ???????? ??????????
	 */
	public boolean reduceAdena(long adena, boolean notify)
	{
		if(adena < 0)
			return false;
		if(adena == 0)
			return true;
		boolean result = getInventory().reduceAdena(adena);
		if(notify && result)
			sendPacket(SystemMessagePacket.removeItems(ItemTemplate.ITEM_ID_ADENA, adena));
		return result;
	}

	public ItemInstance addAdena(long adena)
	{
		return addAdena(adena, false);
	}

	/**
	 * ?????????????????? ?????????? ????????????.<BR><BR>
	 *
	 * @param adena  - ?????????????? ?????????? ????????
	 * @param notify - ???????????????????? ?????????????????? ??????????????????
	 * @return L2ItemInstance - ?????????? ???????????????????? ??????????
	 */
	public ItemInstance addAdena(long adena, boolean notify)
	{
		if(adena < 1)
			return null;
		ItemInstance item = getInventory().addAdena(adena);
		if(item != null && notify)
			sendPacket(SystemMessagePacket.obtainItems(ItemTemplate.ITEM_ID_ADENA, adena, 0));
		return item;
	}

	public GameClient getNetConnection()
	{
		return _connection;
	}

	public boolean checkFloodProtection(String type, String command)
	{
		return _connection != null && _connection.checkFloodProtection(type, command);
	}

	public int getRevision()
	{
		return _connection == null ? 0 : _connection.getRevision();
	}

	public void setNetConnection(final GameClient connection)
	{
		_connection = connection;
	}

	public boolean isConnected()
	{
		return _connection != null && _connection.isConnected();
	}

	@Override
	public void onAction(final Player player, boolean shift)
	{
		if(!isTargetable(player))
		{
			player.sendActionFailed();
			return;
		}

		/*if(isControlBlocked())
		{
			player.sendPacket(ActionFailPacket.STATIC);
			return;
		}*/

		if(shift && OnShiftActionHolder.getInstance().callShiftAction(player, Player.class, this, true))
			return;

		// Check if the other player already target this L2Player
		if(player.getTarget() != this)
		{
			player.setTarget(this);
			if(player.getTarget() != this)
				player.sendPacket(ActionFailPacket.STATIC);
		}
		else if(getPrivateStoreType() != STORE_PRIVATE_NONE)
		{
			if(!player.checkInteractionDistance(this) && player.getAI().getIntention() != CtrlIntention.AI_INTENTION_INTERACT)
			{
				if(!shift)
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this, null);
				else
					player.sendPacket(ActionFailPacket.STATIC);
			}
			else
				player.doInteract(this);
		}
		else if(isAutoAttackable(player))
			player.getAI().Attack(this, false, shift);
		else if(player != this)
		{
			if(player.getAI().getIntention() != CtrlIntention.AI_INTENTION_FOLLOW)
			{
				if(!shift)
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this, Config.FOLLOW_RANGE);
				else
					player.sendPacket(ActionFailPacket.STATIC);
			}
			else
				player.sendPacket(ActionFailPacket.STATIC);
		}
		else
			player.sendPacket(ActionFailPacket.STATIC);
	}

	@Override
	public void broadcastStatusUpdate()
	{
		if(!needStatusUpdate()) //???? ???????? ?????? ???????????? ?????????????? ??????????????. ?????????? ?????????? ?? ???????????????????????? - ???????????? ?????? ??????????????.
			return;

		broadcastPacket(new StatusUpdate(this, StatusUpdatePacket.UpdateType.DEFAULT, StatusUpdatePacket.CUR_HP, StatusUpdatePacket.MAX_HP, StatusUpdatePacket.CUR_MP, StatusUpdatePacket.MAX_MP, StatusUpdatePacket.CUR_CP, StatusUpdatePacket.MAX_CP));

		// Check if a party is in progress
		if(isInParty())
			// Send the Server->Client packet PartySmallWindowUpdatePacket with current HP, MP and Level to all other L2Player of the Party
			getParty().broadcastToPartyMembers(this, new PartySmallWindowUpdatePacket(this));

		final List<SingleMatchEvent> events = getEvents(SingleMatchEvent.class);
		for(SingleMatchEvent event : events)
			event.onStatusUpdate(this);

		if(isInOlympiadMode() && isOlympiadCompStart())
		{
			if(_olympiadGame != null)
				_olympiadGame.broadcastInfo(this, null, false);
		}
	}

	private ScheduledFuture<?> _broadcastCharInfoTask;

	public class BroadcastCharInfoTask implements Runnable
	{
		@Override
		public void run()
		{
			broadcastCharInfoImpl();
			_broadcastCharInfoTask = null;
		}
	}

	@Override
	public void broadcastCharInfo()
	{
		broadcastUserInfo(false);
	}

	public void broadcastUserInfo(boolean force)
	{
		sendUserInfo(force);

		if(!isVisible())
			return;

		if(Config.BROADCAST_CHAR_INFO_INTERVAL == 0)
			force = true;

		if(force)
		{
			if(_broadcastCharInfoTask != null)
			{
				_broadcastCharInfoTask.cancel(false);
				_broadcastCharInfoTask = null;
			}
			broadcastCharInfoImpl();
			return;
		}

		if(_broadcastCharInfoTask != null)
			return;

		_broadcastCharInfoTask = ThreadPoolManager.getInstance().schedule(new BroadcastCharInfoTask(), Config.BROADCAST_CHAR_INFO_INTERVAL);
	}

	private int _polyNpcId;

	public void setPolyId(int polyid)
	{
		_polyNpcId = polyid;

		teleToLocation(getLoc());
		broadcastUserInfo(true);
	}

	public boolean isPolymorphed()
	{
		return _polyNpcId != 0;
	}

	public int getPolyId()
	{
		return _polyNpcId;
	}

	@Override
	public void broadcastCharInfoImpl(IUpdateTypeComponent... components)
	{
		if(!isVisible())
			return;

		for(Player target : World.getAroundObservers(this))
		{
			if(isInvisible(target))
				continue;

			target.sendPacket(isPolymorphed() ? new NpcInfoPoly(this, target) : new CIPacket(this, target));
			target.sendPacket(new RelationChangedPacket(this, target));
		}
	}

	public void sendEtcStatusUpdate()
	{
		if(!isVisible())
			return;

		sendPacket(new EtcStatusUpdatePacket(this));
	}

	private Future<?> _userInfoTask;

	private class UserInfoTask implements Runnable
	{
		@Override
		public void run()
		{
			sendUserInfoImpl();
			_userInfoTask = null;
		}
	}

	private void sendUserInfoImpl()
	{
		sendPacket(new UIPacket(this));
	}

	public void sendUserInfo()
	{
		sendUserInfo(false);
	}

	public void sendUserInfo(boolean force)
	{
		if(!isVisible() || entering || isLogoutStarted() || isFakePlayer())
			return;

		if(Config.USER_INFO_INTERVAL == 0 || force)
		{
			if(_userInfoTask != null)
			{
				_userInfoTask.cancel(false);
				_userInfoTask = null;
			}
			sendUserInfoImpl();
			return;
		}

		if(_userInfoTask != null)
			return;

		_userInfoTask = ThreadPoolManager.getInstance().schedule(new UserInfoTask(), Config.USER_INFO_INTERVAL);
	}

	public void sendSkillList(int learnedSkillId)
	{
		sendPacket(new SkillListPacket(this, learnedSkillId));
		sendPacket(new AcquireSkillListPacket(this));
	}

	public void sendSkillList()
	{
		sendSkillList(0);
	}

	public void updateSkillShortcuts(int skillId, int skillLevel)
	{
		for(ShortCut sc : getAllShortCuts())
		{
			if(sc.getId() == skillId && sc.getType() == ShortCut.ShortCutType.SKILL)
			{
				ShortCut newsc = new ShortCut(this, sc.getSlot(), sc.getPage(), sc.getType(), sc.getId(), skillLevel, 1);
				sendPacket(new ShortCutRegisterPacket(this, newsc));
				registerShortCut(newsc);
			}
		}
	}

	public void sendStatusUpdate(boolean broadCast, boolean withPet, int... fields)
	{
		if(fields.length == 0 || entering && !broadCast)
			return;

		StatusUpdate su = new StatusUpdate(this, StatusUpdatePacket.UpdateType.DEFAULT, fields);

		List<IBroadcastPacket> packets = new ArrayList<IBroadcastPacket>(withPet ? 2 : 1);
		if(withPet)
		{
			for(Servitor servitor : getServitors())
				packets.add(new StatusUpdate(servitor, StatusUpdatePacket.UpdateType.DEFAULT, fields));
		}

		packets.add(su);

		if(!broadCast)
			sendPacket(packets);
		else if(entering)
			broadcastPacketToOthers(packets);
		else
			broadcastPacket(packets);
	}

	/**
	 * @return the Alliance Identifier of the L2Player.<BR><BR>
	 */
	public int getAllyId()
	{
		return _clan == null ? 0 : _clan.getAllyId();
	}

	@Override
	public void sendPacket(IBroadcastPacket p)
	{
		if(p == null)
			return;

		if(isPacketIgnored(p))
			return;

		GameClient connection = getNetConnection();
		if(connection != null && connection.isConnected())
			_connection.sendPacket(p.packet(this));
	}

	@Override
	public void sendPacket(IBroadcastPacket... packets)
	{
		for(IBroadcastPacket p : packets)
			sendPacket(p);
	}

	@Override
	public void sendPacket(List<? extends IBroadcastPacket> packets)
	{
		if(packets == null)
			return;

		for(IBroadcastPacket p : packets)
			sendPacket(p);
	}

	private boolean isPacketIgnored(IBroadcastPacket p)
	{
		if(p == null)
			return true;

		//if(_notShowTraders && (p.getClass() == PrivateStoreBuyMsg.class || p.getClass() == PrivateStoreMsg.class || p.getClass() == RecipeShopMsgPacket.class))
		//		return true;

		return false;
	}

	public void doInteract(GameObject target)
	{
		if(target == null || isActionsDisabled())
		{
			sendActionFailed();
			return;
		}
		if(target.isPlayer())
		{
			if(checkInteractionDistance(target))
			{
				Player temp = (Player) target;

				if(temp.getPrivateStoreType() == STORE_PRIVATE_SELL || temp.getPrivateStoreType() == STORE_PRIVATE_SELL_PACKAGE)
				{
					sendPacket(new PrivateStoreList(this, temp));
				}
				else if(temp.getPrivateStoreType() == STORE_PRIVATE_BUY)
				{
					sendPacket(new PrivateStoreBuyList(this, temp));
				}
				else if(temp.getPrivateStoreType() == STORE_PRIVATE_MANUFACTURE)
				{
					sendPacket(new RecipeShopSellListPacket(this, temp));
				}
				else if(temp.getPrivateStoreType() == STORE_PRIVATE_BUFF)
				{
					OfflineBufferManager.getInstance().processBypass(this, "bufflist_" + temp.getObjectId());
				}
				sendActionFailed();
			}
			else if(getAI().getIntention() != CtrlIntention.AI_INTENTION_INTERACT)
				getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this, null);
		}
		else
			target.onAction(this, false);
	}

	public void doAutoLootOrDrop(ItemInstance item, NpcInstance fromNpc)
	{
		boolean forceAutoloot = fromNpc.isFlying() || getReflection().isAutolootForced();

		if(fromNpc.isRaid() && !Config.AUTO_LOOT_FROM_RAIDS && !item.isHerb() && !forceAutoloot)
		{
			item.dropToTheGround(this, fromNpc);
			return;
		}

		// Herbs
		if(item.isHerb())
		{
			if(!AutoLootHerbs && !forceAutoloot)
			{
				item.dropToTheGround(this, fromNpc);
				return;
			}

			for(SkillEntry skillEntry : item.getTemplate().getAttachedSkills())
			{
				altUseSkill(skillEntry, this);

				for(Servitor servitor : getServitors())
				{
					if(servitor.isSummon() && !servitor.isDead())
						servitor.altUseSkill(skillEntry, servitor);
				}
			}
			item.deleteMe();
			return;
		}

		boolean autolootAll = _autoLoot && (Config.AUTO_LOOT_ITEM_ID_LIST.isEmpty() || Config.AUTO_LOOT_ITEM_ID_LIST.contains(item.getItemId()));
		boolean autolootOnlyAdena = _autoLootOnlyAdena && item.getTemplate().isAdena();
		if(!forceAutoloot && !autolootAll && !autolootOnlyAdena)
		{
			item.dropToTheGround(this, fromNpc);
			return;
		}

		// Check if the L2Player is in a Party
		if(!isInParty())
		{
			if(!pickupItem(item, ItemProcess.Pickup))
			{
				item.dropToTheGround(this, fromNpc);
				return;
			}
		}
		else
			getParty().distributeItem(this, item, fromNpc);

		broadcastPickUpMsg(item);
	}

	@Override
	public void doPickupItem(final GameObject object)
	{
		// Check if the L2Object to pick up is a L2ItemInstance
		if(!object.isItem())
		{
			_log.atWarning().log( "trying to pickup wrong target.%s", getTarget() );
			return;
		}

		sendActionFailed();
		getMovement().stopMove();

		ItemInstance item = (ItemInstance) object;

		synchronized (item)
		{
			if(!item.isVisible())
				return;

			// Check if me not owner of item and, if in party, not in owner party and nonowner pickup delay still active
			if(!ItemFunctions.checkIfCanPickup(this, item))
			{
				SystemMessage sm;
				if(item.getItemId() == 57)
				{
					sm = new SystemMessage(SystemMessage.YOU_HAVE_FAILED_TO_PICK_UP_S1_ADENA);
					sm.addNumber(item.getCount());
				}
				else
				{
					sm = new SystemMessage(SystemMessage.YOU_HAVE_FAILED_TO_PICK_UP_S1);
					sm.addItemName(item.getItemId());
				}
				sendPacket(sm);
				return;
			}

			// Herbs
			if(item.isHerb())
			{
				for(SkillEntry skillEntry : item.getTemplate().getAttachedSkills())
					altUseSkill(skillEntry, this);

				broadcastPacket(new GetItem(item, getObjectId()));
				item.deleteMe();
				return;
			}

			FlagItemAttachment attachment = item.getAttachment() instanceof FlagItemAttachment ? (FlagItemAttachment) item.getAttachment() : null;

			if(!isInParty() || attachment != null)
			{
				if(pickupItem(item, ItemProcess.Pickup))
				{
					broadcastPacket(new GetItem(item, getObjectId()));
					broadcastPickUpMsg(item);
					item.pickupMe();
				}
			}
			else
				getParty().distributeItem(this, item, null);
		}
	}

	public boolean pickupItem(ItemInstance item, ItemProcess log)
	{
		PickableAttachment attachment = item.getAttachment() instanceof PickableAttachment ? (PickableAttachment) item.getAttachment() : null;

		if(!ItemFunctions.canAddItem(this, item))
			return false;

		ItemLogger.INSTANCE.log(log, this, item);
		sendPacket(SystemMessagePacket.obtainItems(item));
		getInventory().addItem(item);

		if(attachment != null)
			attachment.pickUp(this);

		getListeners().onPickupItem(item);

		sendChanges();
		return true;
	}

	@Override
	public void setTarget(GameObject newTarget)
	{
		// Check if the new target is visible
		if(newTarget != null && !newTarget.isVisible())
			newTarget = null;

		GameObject oldTarget = getTarget();

		if(oldTarget != null)
		{
			if(oldTarget.equals(newTarget))
			{
				// Validate location of the target.
				if(newTarget != null && newTarget.getObjectId() != getObjectId())
				{
					sendPacket(new ValidateLocationPacket(newTarget));
				}
				return;
			}
		}

		super.setTarget(newTarget);

		if(newTarget != null)
		{
			if(newTarget.isCreature())
			{
				final Creature target = (Creature) newTarget;

				// Validate location of the new target.
				if(target.getObjectId() != getObjectId())
				{
					sendPacket(new ValidateLocationPacket(target));
				}

				// Show the client his new target.
				sendPacket(new MyTargetSelectedPacket(this, target));

				// Send max/current hp.
				if(target.isServitor())
					sendPacket(new StatusUpdate(target, this, StatusUpdatePacket.UpdateType.DEFAULT, StatusUpdatePacket.CUR_HP, StatusUpdatePacket.MAX_HP, StatusUpdatePacket.CUR_MP, StatusUpdatePacket.MAX_MP));
				else
					sendPacket(new StatusUpdate(target, this, StatusUpdatePacket.UpdateType.DEFAULT, StatusUpdatePacket.CUR_HP, StatusUpdatePacket.MAX_HP));

				// To others the new target, and not yourself!
				broadcastPacketToOthers(new TargetSelectedPacket(getObjectId(), target.getObjectId(), getLoc()));

				// Send buffs
				sendPacket(target.getAbnormalStatusUpdate());
			}
			/*else
			{
				sendPacket(new MyTargetSelectedPacket(this, newTarget));
				broadcastPacket(new TargetSelectedPacket(getObjectId(), newTarget.getObjectId(), getLoc()));
			}*/
		}
		else
			broadcastPacket(new TargetUnselectedPacket(this));

		if(newTarget != null && newTarget != this && getDecoys() != null && !getDecoys().isEmpty() && newTarget.isCreature())
		{
			for(DecoyInstance dec : getDecoys())
			{
				if(dec == null)
					continue;
				if(dec.getAI() == null)
				{
					_log.atInfo().log( "This decoy has NULL AI" );
					continue;
				}
				if(newTarget.isCreature())
				{
					Creature _nt = (Creature) newTarget;
					if(_nt.isInPeaceZone()) //won't attack in peace zone anyone.
						continue;
				}
				dec.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, newTarget, 1000);
				//dec.getAI().checkAggression(((Creature)newTarget));
				//dec.getAI().Attack(newTarget, true, false);
			}
		}
	}

	/**
	 * @return the active weapon instance (always equipped in the right hand).<BR><BR>
	 */
	@Override
	public ItemInstance getActiveWeaponInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
	}

	/**
	 * @return the active weapon item (always equipped in the right hand).<BR><BR>
	 */
	@Override
	public WeaponTemplate getActiveWeaponTemplate()
	{
		final ItemInstance weapon = getActiveWeaponInstance();

		if(weapon == null)
			return null;

		ItemTemplate template = weapon.getTemplate();
		if(template == null)
			return null;

		if(!(template instanceof WeaponTemplate))
		{
			_log.atWarning().log( "Template in active weapon not WeaponTemplate! (Item ID[%s])", weapon.getItemId() );
			return null;
		}

		return (WeaponTemplate) template;
	}

	/**
	 * @return the secondary weapon instance (always equipped in the left hand).<BR><BR>
	 */
	@Override
	public ItemInstance getSecondaryWeaponInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
	}

	/**
	 * @return the secondary weapon item (always equipped in the left hand) or the fists weapon.<BR><BR>
	 */
	@Override
	public WeaponTemplate getSecondaryWeaponTemplate()
	{
		final ItemInstance weapon = getSecondaryWeaponInstance();

		if(weapon == null)
			return null;

		final ItemTemplate item = weapon.getTemplate();

		if(item instanceof WeaponTemplate)
			return (WeaponTemplate) item;

		return null;
	}

	public ArmorType getWearingArmorType()
	{
		final ItemInstance chest = getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
		if(chest == null)
			return ArmorType.NONE;

		final ItemType chestItemType = chest.getItemType();
		if(!(chestItemType instanceof ArmorType))
			return ArmorType.NONE;

		final ArmorType chestArmorType = (ArmorType) chestItemType;
		if(chest.getBodyPart() == ItemTemplate.SLOT_FULL_ARMOR)
			return chestArmorType;

		final ItemInstance legs = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS);
		if(legs == null)
			return ArmorType.NONE;

		if(legs.getItemType() != chestArmorType)
			return ArmorType.NONE;

		return chestArmorType;
	}

	@Override
	public void reduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflectAndAbsorb, boolean transferDamage, boolean isDot, boolean sendReceiveMessage, boolean sendGiveMessage, boolean crit, boolean miss, boolean shld, double elementalDamage, boolean elementalCrit)
	{
		if(attacker == null || isDead() || (attacker.isDead() && !isDot))
			return;

		// 5182 = Blessing of protection, ???????????????? ???????? ?????????????? ?????????????? ???????????? 10 ?? ???? ?? ???????? ??????????
		if(attacker.isPlayer() && Math.abs(attacker.getLevel() - getLevel()) > 10)
		{
			// ???? ???? ?????????? ?????????????? ???????? ???????? ?? ????????????????????
			if(attacker.isPK() && isProtectionBlessingAffected() && !isInSiegeZone())
				return;
			// ?????? ?? ???????????????????? ???? ?????????? ?????????????? ???????? ????
			if(isPK() && attacker.isProtectionBlessingAffected() && !attacker.isInSiegeZone())
				return;
		}

		// Reduce the current HP of the L2Player
		super.reduceCurrentHp(damage, attacker, skill, awake, standUp, directHp, canReflectAndAbsorb, transferDamage, isDot, sendReceiveMessage, sendGiveMessage, crit, miss, shld, elementalDamage, elementalCrit);
	}

	@Override
	protected void onReduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp, boolean isDot)
	{
		if(damage <= 0)
			return;

		if(standUp)
		{
			standUp();
			if(isFakeDeath())
				breakFakeDeath();
		}

		final double originDamage = damage;

		if(attacker.isPlayable())
		{
			if(!directHp && getCurrentCp() > 0)
			{
				double cp = getCurrentCp();
				if(cp >= damage)
				{
					cp -= damage;
					damage = 0;
				}
				else
				{
					damage -= cp;
					cp = 0;
				}
				setCurrentCp(cp, !isDot);
				if(isDot)
				{
					StatusUpdate su = new StatusUpdate(this, attacker, UpdateType.REGEN, StatusUpdatePacket.CUR_CP);
					attacker.sendPacket(su);
					sendPacket(su);
					broadcastStatusUpdate();
				}
			}
		}

		double hp = getCurrentHp();

		DuelEvent duelEvent = getEvent(DuelEvent.class);
		if(duelEvent != null)
		{
			if((hp - damage) <= 1 && !isDeathImmune()) // ???????? ???? <= 1 - ????????
			{
				setCurrentHp(1, false);
				duelEvent.onDie(this);
				return;
			}
		}

		if(isInOlympiadMode())
		{
			OlympiadGame game = _olympiadGame;
			if(this != attacker && (skill == null || skill.isBad())) // ?????????????? ?????????? ???? ?????????????? ???????????? ?? ?????????????????? ??????????????
				game.addDamage(this, Math.min(hp, originDamage));

			if((hp - damage) <= 1 && !isDeathImmune()) // ???????? ???? <= 1 - ????????
			{
				game.setWinner(getOlympiadSide() == 1 ? 2 : 1);
				game.endGame(20000, false);
				setCurrentHp(1, false);
				attacker.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				attacker.sendActionFailed();
				return;
			}
		}

		/* don't exist in classic
		if(getStat().getValue(DoubleStat.RestoreHPGiveDamage) == 1 && Rnd.chance(1))
		{
			setCurrentHp(getCurrentHp() + getMaxHp() / 10.0, false);
		}*/

		addDamageInfo(damage, attacker, skill);

		super.onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp, isDot);
	}

	@Override
	public final void reduceCurrentCp(int value)
	{
		if (getCurrentCp() > value)
		{
			setCurrentCp(getCurrentCp() - value);
		}
		else
		{
			setCurrentCp(0);
		}
	}

	private void addDamageInfo(double damage, Creature attacker, Skill skill) {
		AttackerType attackerType = attacker.isNpc() ? AttackerType.NPC : AttackerType.PLAYABLE;
		AttackerName attackerName = AttackerName.Companion.create(attacker);
		Clan clan = attacker.getClan();
		PledgeName pledgeName = clan != null
				? new PledgeName(clan.getName()) : new PledgeName("");
		SkillClassId skillClassId = skill != null
				? new SkillClassId(skill.getId()) : new SkillClassId(0);
		DamageAmount damageAmount = new DamageAmount(damage);
		DamageType damageType = DamageType.NORMAL_DAMAGE;
		damageInfo.add(new DamageInfoEntry(attackerType, attackerName, pledgeName,
				skillClassId, damageAmount, damageType));
	}

	@Override
	protected void altDeathPenalty(final Creature killer)
	{
		// Reduce the Experience of the L2Player in function of the calculated Death Penalty
		if(!Config.ALT_GAME_DELEVEL)
			return;

		for(Zone z : getZones())
		{
			if(!z.getTemplate().canLostExp())
				return;
		}
		deathPenalty(killer);
	}

	protected void onRevive()
	{
		super.onRevive();

		sendSlotsInfo();
	}

	public final boolean atWarWith(final Player player)
	{
		final Clan playerClan = getClan();
		if(playerClan != null)
		{
			final ClanWar war = playerClan.getWarWith(player.getClanId());
			if(war != null)
				return getPledgeType() != Clan.SUBUNIT_ACADEMY && player.getPledgeType() != Clan.SUBUNIT_ACADEMY;
		}
		return false;
	}

	public boolean atMutualWarWith(Player player)
	{
		final Clan playerClan = getClan();
		if(playerClan != null)
		{
			final ClanWar war = playerClan.getWarWith(player.getClanId());
			if(war != null && war.getPeriod() == ClanWarPeriod.MUTUAL)
				return getPledgeType() != Clan.SUBUNIT_ACADEMY && player.getPledgeType() != Clan.SUBUNIT_ACADEMY;
		}
		return false;
	}

	public final void doPurePk(final Player killer)
	{
		// Check if the attacker has a PK counter greater than 0
		final int pkCountMulti = (int) Math.max(killer.getPkKills() * Config.KARMA_PENALTY_DURATION_INCREASE, 1);

		// Calculate the level difference Multiplier between attacker and killed L2Player
		//final int lvlDiffMulti = Math.max(killer.getLevel() / _level, 1);

		// Calculate the new Karma of the attacker : newKarma = baseKarma*pkCountMulti*lvlDiffMulti
		// Add karma to attacker and increase its PK counter
		killer.decreaseKarma(Config.KARMA_MIN_KARMA * pkCountMulti); // * lvlDiffMulti);
		killer.setPkKills(killer.getPkKills() + 1);
	}

	public final void doKillInPeace(final Player killer) // Check if the L2Player killed haven't Karma
	{
		if(!isPK())
			doPurePk(killer);
		else
		{
			String var = PK_KILL_VAR + "_" + getObjectId();
			if(!killer.getVarBoolean(var))
			{
				// ?? ?????????????? 30 ?????????? ???? ???????????? ?????????? ???? ???????????????? ?????????????? ????. (TODO: [Bonux] ?????????????????? ?????????? ???? ????????.)
				long expirationTime = System.currentTimeMillis() + (30 * 60 * 1000);
				killer.setVar(var, true, expirationTime);
			}
		}
	}

	public void checkAddItemToDrop(List<ItemInstance> array, List<ItemInstance> items, int maxCount)
	{
		array.addAll(Rnd.get(items, maxCount));
	}

	public FlagItemAttachment getActiveWeaponFlagAttachment()
	{
		ItemInstance item = getActiveWeaponInstance();
		if(item == null || !(item.getAttachment() instanceof FlagItemAttachment))
			return null;
		return (FlagItemAttachment) item.getAttachment();
	}

	protected LostItems doPKPVPManage(Creature killer)
	{
		FlagItemAttachment attachment = getActiveWeaponFlagAttachment();
		if(attachment != null)
			attachment.onDeath(this, killer);

		if(killer == null || isMyServitor(killer.getObjectId()) || killer == this)
			return LostItems.Companion.getEMPTY();

		if(killer.isServitor() && (killer = killer.getPlayer()) == null)
			return LostItems.Companion.getEMPTY();

		if(killer.isPlayer())
			PvPRewardManager.tryGiveReward(this, killer.getPlayer());

		if(isInZoneBattle() || killer.isInZoneBattle())
			return LostItems.Companion.getEMPTY();

		if(killer.getTeam() != TeamType.NONE && getTeam() != TeamType.NONE) //in events or duels we don't increase pvp/pk/karma
			return LostItems.Companion.getEMPTY();

		ClanWar clanWar = null;

		// Processing Karma/PKCount/PvPCount for killer
		if(killer.isPlayer() || killer instanceof FakePlayer) //addon if killer is clone instance should do also this method.
		{
			final Player pk = killer.getPlayer();
			final boolean war = atMutualWarWith(pk);

			if(getPledgeType() != Clan.SUBUNIT_ACADEMY && pk.getPledgeType() != Clan.SUBUNIT_ACADEMY)
			{
				Clan clan = getClan();
				if(clan != null)
				{
					clanWar = clan.getWarWith(pk.getClanId());
					if(clanWar != null)
						clanWar.onKill(pk, this);
				}
			}

			if(isInSiegeZone())
				return LostItems.Companion.getEMPTY();

			final Castle castle = getCastle();
			if(getPvpFlag() > 0 || war || castle != null && castle.getResidenceSide() == ResidenceSide.DARK)
				pk.setPvpKills(pk.getPvpKills() + 1);
			else
				doKillInPeace(pk);

			pk.sendChanges();
		}

		if(isFakePlayer())
			return LostItems.Companion.getEMPTY();

		// No drop from GM's
		if(!Config.KARMA_DROP_GM && isGM())
			return LostItems.Companion.getEMPTY();

		/*if(clanWar != null) TODO: ?????????????? ???? ?????????? ?? ????, ???????? ?????? ?????????????? ????????-??????.
			return LostItems.Companion.getEMPTY();*/

		// ?? ???????????????????? ???????????????? ???????? ???????????????? ???????????? ?????? ???????????? ???? ???????????? ?????? ????????????
		// ?????????? ????????, ???????? ???? ???????????? ?????????? ?????? ???????????? ?????????????????? ???????????? ???????? ?????? ???????????? ???? ??????????????
		boolean isPvP = killer.isPlayable() || killer instanceof GuardInstance;

		if(killer.isMonster())
		{
			if(!Config.DROP_ITEMS_ON_DIE || killer.isRaid())
				return LostItems.Companion.getEMPTY();
	
			NpcInstance npcKiller = (NpcInstance) killer;
			if(npcKiller.getLeader() != null && npcKiller.getLeader().isRaid())
				return LostItems.Companion.getEMPTY();
		}
		else if(isPvP)
		{
			if(_pkKills < Config.MIN_PK_TO_ITEMS_DROP || (Config.KARMA_NEEDED_TO_DROP && _karma >= 0))
				return LostItems.Companion.getEMPTY();
		}
		else
			return LostItems.Companion.getEMPTY();

		final int max_drop_count = isPvP ? Config.KARMA_DROP_ITEM_LIMIT : 1;

		double dropRate; // ?????????????? ???????? ?? ??????????????????
		if(isPvP)
			dropRate = _pkKills * Config.KARMA_DROPCHANCE_MOD + Config.KARMA_DROPCHANCE_BASE;
		else
			dropRate = getStat().getValue(DoubleStat.ITEMS_LOST_CHANCE, Config.NORMAL_DROPCHANCE_BASE);

		int dropEquipCount = 0, dropWeaponCount = 0, dropItemCount = 0;

		for(int i = 0; i < Math.ceil(dropRate / 100) && i < max_drop_count; i++)
		{
			if(Rnd.chance(dropRate))
			{
				int rand = Rnd.get(Config.DROPCHANCE_EQUIPPED_WEAPON + Config.DROPCHANCE_EQUIPMENT + Config.DROPCHANCE_ITEM) + 1;
				if(rand > Config.DROPCHANCE_EQUIPPED_WEAPON + Config.DROPCHANCE_EQUIPMENT)
					dropItemCount++;
				else if(rand > Config.DROPCHANCE_EQUIPPED_WEAPON)
					dropEquipCount++;
				else
					dropWeaponCount++;
			}
		}

		List<ItemInstance> drop = new ArrayList<>(), // ?????????? ???????????? ?? ???????????????????????? ????????????
				dropItem = new ArrayList<>(), dropEquip = new ArrayList<>(), dropWeapon = new ArrayList<>(); // ??????????????????

		getInventory().writeLock();
		try
		{
			for(ItemInstance item : getInventory().getItems())
			{
				if(!item.canBeDropped(this, true) || Config.KARMA_LIST_NONDROPPABLE_ITEMS.contains(item.getItemId()))
					continue;

				if(item.getTemplate().getType2() == ItemTemplate.TYPE2_WEAPON)
					dropWeapon.add(item);
				else if(item.getTemplate().getType2() == ItemTemplate.TYPE2_SHIELD_ARMOR || item.getTemplate().getType2() == ItemTemplate.TYPE2_ACCESSORY)
					dropEquip.add(item);
				else if(item.getTemplate().getType2() == ItemTemplate.TYPE2_OTHER)
					dropItem.add(item);
			}

			checkAddItemToDrop(drop, dropWeapon, dropWeaponCount);
			checkAddItemToDrop(drop, dropEquip, dropEquipCount);
			checkAddItemToDrop(drop, dropItem, dropItemCount);

			// Dropping items, if present
			if(drop.isEmpty())
				return LostItems.Companion.getEMPTY();

			List<LostItem> result = new ArrayList<>(drop.size());
			for(ItemInstance item : drop)
			{
				if(item.isAugmented() && !Config.ALT_ALLOW_DROP_AUGMENTED)
				{
					item.setVariationStoneId(0);
					item.setVariation1Id(0);
					item.setVariation2Id(0);
				}

				item = getInventory().removeItem(item);
				result.add(new LostItem(item));
				ItemLogger.INSTANCE.log(isPvP ? ItemProcess.PvPPlayerDieDrop : ItemProcess.PvEPlayerDieDrop, this, item);

				if(item.getEnchantLevel() > 0)
					sendPacket(new SystemMessage(SystemMessage.DROPPED__S1_S2).addNumber(item.getEnchantLevel()).addItemName(item.getItemId()));
				else
					sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_DROPPED_S1).addItemName(item.getItemId()));

				if(killer.isPlayable() && ((Config.AUTO_LOOT && Config.AUTO_LOOT_PK) || isInFlyingTransform()))
				{
					killer.getPlayer().getInventory().addItem(item);
					ItemLogger.INSTANCE.log(ItemProcess.Pickup, this, item);

					killer.getPlayer().sendPacket(SystemMessagePacket.obtainItems(item));
				}
				else
					item.dropToTheGround(this, Location.findAroundPosition(this, Config.KARMA_RANDOM_DROP_LOCATION_LIMIT));
			}

			return new LostItems(result);
		}
		finally
		{
			getInventory().writeUnlock();
		}
	}

	@Override
	protected void onDeath(Creature killer)
	{
		if(isInStoreMode())
		{
			setPrivateStoreType(STORE_PRIVATE_NONE);
			storePrivateStore();
		}

		if(isProcessingRequest())
		{
			Request request = getRequest();
			if(isInTrade())
			{
				Player parthner = request.getOtherPlayer(this);
				sendPacket(TradeDonePacket.FAIL);
				parthner.sendPacket(TradeDonePacket.FAIL);
			}
			request.cancel();
		}

		getAI().stopAutoplay();
		stopAutoShortcuts(AutoShortCutType.ALL);

		deleteAgathion();

		// clear charges
		_charges.set(0);
		stopChargeTask();

		stopWaterTask();

		if(!isResurrectionSpecial() && isInSiegeZone() && isCharmOfCourage())
		{
			ask(new ConfirmDlgPacket(SystemMsg.YOUR_CHARM_OF_COURAGE_IS_TRYING_TO_RESURRECT_YOU, 60000), new ReviveAnswerListener(this, 100, false));
			setCharmOfCourage(false);
		}

		for(QuestState qs : getAllQuestsStates())
			qs.getQuest().notifyTutorialEvent("CE", false, "200", qs);

		if(isMounted())
			_mount.onDeath();

		for(Servitor servitor : getServitors())
			servitor.notifyMasterDeath();

		for(ListenerHook hook : getListenerHooks(ListenerHookType.PLAYER_DIE))
			hook.onPlayerDie(this, killer);

		for(ListenerHook hook : ListenerHook.getGlobalListenerHooks(ListenerHookType.PLAYER_DIE))
			hook.onPlayerDie(this, killer);

		super.onDeath(killer);

		if(isPK())
		{
			increaseKarma(Config.KARMA_LOST_BASE);
			if(_karma > 0)
				_karma = 0;
		}
	}

	public void restoreExp()
	{
		restoreExp(100.);
	}

	public void restoreExp(double percent)
	{
		if(percent == 0)
			return;

		long lostexp = 0;

		String lostexps = getVar("lostexp");
		if(lostexps != null)
		{
			lostexp = Long.parseLong(lostexps);
			unsetVar("lostexp");
		}

		if(lostexp != 0)
			addExpAndSp((long) (lostexp * percent / 100), 0);
	}

	public void deathPenalty(Creature killer)
	{
		if(killer == null)
			return;

        if (isInPvPEvent()) {
            return;
        }

		final boolean atwar = killer.getPlayer() != null && atMutualWarWith(killer.getPlayer());

		final int level = getLevel();

		// The death steal you some Exp: 10-40 lvl 8% loose
		double percentLost = Config.PERCENT_LOST_ON_DEATH[getLevel()];

		if(isPK())
			percentLost *= Config.PERCENT_LOST_ON_DEATH_MOD_FOR_PK;
		else if(isInPeaceZone())
			percentLost *= Config.PERCENT_LOST_ON_DEATH_MOD_IN_PEACE_ZONE;
		else
		{
			if(atwar) // TODO: ??????????????????, ???????????? ???? ???????????? ???????????? ?????????????????????? ???? ????!
				percentLost *= Config.PERCENT_LOST_ON_DEATH_MOD_IN_WAR;
			else if(killer.getPlayer() != null && killer.getPlayer() != this)
				percentLost *= Config.PERCENT_LOST_ON_DEATH_MOD_IN_PVP;
		}

		if (killer.isRaid()) {
			percentLost *= getStat().getValue(DoubleStat.REDUCE_EXP_LOST_BY_RAID, 1);
		} else if (killer.isMonster()) {
			percentLost *= getStat().getValue(DoubleStat.REDUCE_EXP_LOST_BY_MOB, 1);
		} else if (killer.isPlayable()) {
			percentLost *= getStat().getValue(DoubleStat.REDUCE_EXP_LOST_BY_PVP, 1);
		}

		percentLost *= getPremiumAccount().getExpLost();

		if(percentLost <= 0)
			return;

		// Calculate the Experience loss
		long lostexp = (long) ((Experience.getExpForLevel(level + 1) - Experience.getExpForLevel(level)) * percentLost / 100.0);
		// remove ? lostexp = (long) getStat().getValue(DoubleStat.EXP_LOST, lostexp, killer, null);

		// ???? ???????????????????????????????????? ?????????? ?????? ???????????? ??????????, ???? ?????????? ?????????? - ?????? ?????? ?????????????? ???????????? ???? *????????*
		if(isInSiegeZone())
		{
			boolean onSiegeEvent = containsEvent(SiegeEvent.class);
			if(onSiegeEvent)
				lostexp = 0;

			if(onSiegeEvent)
			{
				int syndromeLvl = 0;
				for(Abnormal e : getAbnormalList())
				{
					if(e.getSkill().getId() == Skill.SKILL_BATTLEFIELD_DEATH_SYNDROME)
					{
						syndromeLvl = e.getSkill().getLevel();
						break;
					}
				}

				if(syndromeLvl == 0)
				{
					Skill skill = SkillHolder.getInstance().getSkill(Skill.SKILL_BATTLEFIELD_DEATH_SYNDROME, 1);
					if(skill != null)
						skill.getEffects(this, this);
				}
				else if(syndromeLvl < 5)
				{
					getAbnormalList().stop(Skill.SKILL_BATTLEFIELD_DEATH_SYNDROME);
					Skill skill = SkillHolder.getInstance().getSkill(Skill.SKILL_BATTLEFIELD_DEATH_SYNDROME, syndromeLvl + 1);
					skill.getEffects(this, this);
				}
				else if(syndromeLvl == 5)
				{
					getAbnormalList().stop(Skill.SKILL_BATTLEFIELD_DEATH_SYNDROME);
					Skill skill = SkillHolder.getInstance().getSkill(Skill.SKILL_BATTLEFIELD_DEATH_SYNDROME, 5);
					skill.getEffects(this, this);
				}
			}
		}

		long before = getExp();
		addExpAndSp(-lostexp, 0);
		long lost = before - getExp();

		if(lost > 0)
			setVar("lostexp", lost);
	}

	public void setRequest(Request transaction)
	{
		_request = transaction;
	}

	public Request getRequest()
	{
		return _request;
	}

	/**
	 * ????????????????, ?????????? ???? ?????????? ?????? ???????????? ???? ??????????
	 *
	 * @return true, ???????? ?????????? ???? ?????????? ???????????????? ???? ????????????
	 */
	public boolean isBusy()
	{
		return isProcessingRequest() || isOutOfControl() || isInOlympiadMode() || getTeam() != TeamType.NONE || isInStoreMode() || isInDuel() || getMessageRefusal() || isBlockAll() || isInvisible(null);
	}

	public boolean isProcessingRequest()
	{
		if(_request == null)
			return false;
		if(!_request.isInProgress())
			return false;
		return true;
	}

	public boolean isInTrade()
	{
		return isProcessingRequest() && getRequest().isTypeOf(L2RequestType.TRADE);
	}

	public List<IClientOutgoingPacket> addVisibleObject(GameObject object, Creature dropper)
	{
		if(isLogoutStarted() || object == null || object.getObjectId() == getObjectId() || !object.isVisible() || object.isObservePoint())
			return Collections.emptyList();

		return object.addPacketList(this, dropper);
	}

	@Override
	public List<IClientOutgoingPacket> addPacketList(Player forPlayer, Creature dropper)
	{
		if(isInvisible(forPlayer) && forPlayer.getObjectId() != getObjectId())
			return Collections.emptyList();

		if(isInStoreMode() && forPlayer.getVarBoolean(NO_TRADERS_VAR))
			return Collections.emptyList();

		List<IClientOutgoingPacket> list = new ArrayList<IClientOutgoingPacket>();
		if(forPlayer.getObjectId() != getObjectId())
			list.add(isPolymorphed() ? new NpcInfoPoly(this, forPlayer) : new CIPacket(this, forPlayer));

		if(isSitting() && _chairObject != null)
			list.add(new ChairSitPacket(this, _chairObject));

		if(isInStoreMode())
			list.add(getPrivateStoreMsgPacket(forPlayer));

		CreatureSkillCast skillCast = getSkillCast(SkillCastingType.NORMAL);
		if(skillCast.isCastingNow())
		{
			Creature castingTarget = skillCast.getTarget();
			SkillEntry castingSkillEntry = skillCast.getSkillEntry();
			long animationEndTime = skillCast.getAnimationEndTime();
			if(castingSkillEntry != null && !castingSkillEntry.getTemplate().isNotBroadcastable() && castingTarget != null && castingTarget.isCreature() && animationEndTime > 0)
				list.add(new MagicSkillUse(this, castingTarget, castingSkillEntry.getId(), castingSkillEntry.getLevel(), (int) (animationEndTime - System.currentTimeMillis()), 0, SkillCastingType.NORMAL));
		}

		skillCast = getSkillCast(SkillCastingType.NORMAL_SECOND);
		if(skillCast.isCastingNow())
		{
			Creature castingTarget = skillCast.getTarget();
			SkillEntry castingSkillEntry = skillCast.getSkillEntry();
			long animationEndTime = skillCast.getAnimationEndTime();
			if(castingSkillEntry != null && !castingSkillEntry.getTemplate().isNotBroadcastable() && castingTarget != null && castingTarget.isCreature() && animationEndTime > 0)
				list.add(new MagicSkillUse(this, castingTarget, castingSkillEntry.getId(), castingSkillEntry.getLevel(), (int) (animationEndTime - System.currentTimeMillis()), 0, SkillCastingType.NORMAL_SECOND));
		}

		if(isInCombat())
			list.add(new AutoAttackStartPacket(getObjectId()));

		list.add(new RelationChangedPacket(this, forPlayer));

		if(isInBoat())
			list.add(getBoat().getOnPacket(this, getInBoatPosition()));
		else
		{
			if(getMovement().isMoving() || getMovement().isFollow())
				list.add(movePacket());
		}

		// VISTALL: ???? ?????????? ?????????????? ????????????????????, ?????????? ?????????????? ???????????? ?????? ?????? ?????????????????? ??????????????
		// DS: ?????? ???????????????? ???? ???? ??????????, ?????????? ???????????? ???? ?????????? ?????????? ?????????? ?? ????????
		if(/*isInMountTransform() || */(isInStoreMode() && entering))
		{
			list.add(new CIPacket(this, forPlayer));
			//list.add(new ExBR_ExtraUserInfo(this));
		}

		return list;
	}

	public List<IClientOutgoingPacket> removeVisibleObject(GameObject object, List<IClientOutgoingPacket> list)
	{
		if(isLogoutStarted() || object == null || object.getObjectId() == getObjectId() || object.isObservePoint()) // FIXME  || isTeleporting()
			return Collections.emptyList();

		List<IClientOutgoingPacket> result = list == null ? object.deletePacketList(this) : list;

		if(getParty() != null && object instanceof Creature)
			getParty().removeTacticalSign((Creature) object);

		if(!isInObserverMode())
			getAI().notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, object);

		return result;
	}

	private void levelSet(int levels)
	{
		if(levels > 0)
		{
			final int level = getLevel();

			checkLevelUpReward(false);

			sendPacket(SystemMsg.YOUR_LEVEL_HAS_INCREASED);
			broadcastPacket(new SocialActionPacket(getObjectId(), SocialActionPacket.LEVEL_UP));

			setCurrentHpMp(getMaxHp(), getMaxMp());
			setCurrentCp(getMaxCp());

			for(QuestState qs : getAllQuestsStates())
				qs.getQuest().notifyTutorialEvent("CE", false, "300", qs);

			// Give Expertise skill of this level
			rewardSkills(false);
			notifyNewSkills();
		}
		else if(levels < 0)
			checkSkills();

		sendUserInfo(true);
		sendSkillList();

		// Recalculate the party level
		if(isInParty())
			getParty().recalculatePartyData();

		if(_clan != null)
			_clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdatePacket(this));

		if(_matchingRoom != null)
			_matchingRoom.broadcastPlayerUpdate(this);
	}

	public boolean notifyNewSkills()
	{
		final Collection<SkillLearn> skills = SkillAcquireHolder.getInstance().getAvailableSkills(this, AcquireType.NORMAL);
		for(SkillLearn s : skills)
		{
			if(s.isFreeAutoGet(AcquireType.NORMAL))
				continue;

			Skill sk = SkillHolder.getInstance().getSkill(s.getId(), s.getLevel());
			if(sk == null)
				continue;

			sendPacket(ExNewSkillToLearnByLevelUp.STATIC);
			return true;
		}
		return false;
	}

	/**
	 * ?????????????? ?????? ????????????, ?????????????? ???????????? ???? ???????????? ??????????????, ?????? ??????????????+maxDiff
	 */
	public boolean checkSkills()
	{
		boolean update = false;
		for(SkillEntry sk : getAllSkillsArray())
		{
			if(SkillUtils.checkSkill(this, sk))
				update = true;
		}
		return update;
	}

	public void startTimers()
	{
		startAutoSaveTask();
		startPcBangPointsTask();
		startPremiumAccountTask();
		getInventory().startTimers();
		resumeQuestTimers();
		getAttendanceRewards().startTasks();
		getProductHistoryList().startTask();
		getVIP().startTask();
	}

	public void stopAllTimers()
	{
		getAI().stopAutoplay();
		stopAutoShortcuts(AutoShortCutType.ALL);
		deleteAgathion();
		stopChargeTask();
		stopWaterTask();
		stopPremiumAccountTask();
		stopHourlyTask();
		stopKickTask();
		stopPcBangPointsTask();
		stopTrainingCampTask();
		stopAutoSaveTask();
		getInventory().stopTimers();
		stopQuestTimers();
		stopEnableUserRelationTask();
		getHennaList().stopHennaRemoveTask();
		getAttendanceRewards().stopTasks();
		stopBanEndTasks();
		getProductHistoryList().stopTask();
		getVIP().stopTask();

		for(ScheduledFuture<?> task : _tasks.values())
			task.cancel(false);

		_tasks.clear();
	}

	@Override
	public boolean isMyServitor(int objId)
	{
		if(_summon != null && _summon.getObjectId() == objId)
			return true;
		if(_pet != null && _pet.getObjectId() == objId)
			return true;
		return false;
	}

	@Override
	public int getServitorsCount()
	{
		int count = 0;
		if(_summon != null)
			count++;
		if(_pet != null)
			count++;
		return count;
	}

	@Override
	public boolean hasServitor()
	{
		return getServitorsCount() > 0;
	}

	@Override
	public List<Servitor> getServitors(Predicate<Servitor> predicate)
	{
        Stream.Builder<Servitor> builder = Stream.builder();
        if(_summon != null)
            builder.add(_summon);
		if(_pet != null)
            builder.add(_pet);

        return builder.build()
                .filter(predicate)
                .sorted(Servitor.ServitorComparator.getInstance())
                .collect(Collectors.toUnmodifiableList());
	}

	public Servitor getAnyServitor()
	{
		return getServitors().stream().findAny().orElse(null);
	}

	public Servitor getFirstServitor()
	{
		return getServitors().stream().findFirst().orElse(null);
	}

	public Servitor getServitor(int objId)
	{
		if(_summon != null && _summon.getObjectId() == objId)
			return _summon;
		if(_pet != null && _pet.getObjectId() == objId)
			return _pet;
		return null;
	}

	public boolean hasSummon()
	{
		return _summon != null;
	}

	public SummonInstance getSummon()
	{
		return _summon;
	}

	public void setSummon(SummonInstance summon)
	{
		if(_summon == summon)
			return;

		_summon = summon;
		if(_summon == null && _pet == null)
		{
			removeAutoShot(ShotType.BEAST_SOULSHOT);
			removeAutoShot(ShotType.BEAST_SPIRITSHOT);
		}
		autoShot(); //TODO: [Bonux] ??????????????????, ?????????? ????.

		if(summon == null)
			getAbnormalList().stop(4140); //TODO: [Bonux] ?????????????????? ?????? ?????? ?? ?? ?????? ????????.
	}

	public void deleteServitor(int objId)
	{
		if(_summon != null && _summon.getObjectId() == objId)
			setSummon(null);
		else if(_pet != null && _pet.getObjectId() == objId)
			setPet(null);
	}

	public PetInstance getPet()
	{
		return _pet;
	}

	public void setPet(PetInstance pet)
	{
		boolean petDeleted = _pet != null;
		_pet = pet;
		unsetVar("pet");
		if(pet == null)
		{
			if(petDeleted)
			{
				if(isLogoutStarted())
				{
					if(getPetControlItem() != null)
						setVar("pet", getPetControlItem().getObjectId());
				}
				setPetControlItem(null);

				if(_summon == null && _pet == null)
				{
					removeAutoShot(ShotType.BEAST_SOULSHOT);
					removeAutoShot(ShotType.BEAST_SPIRITSHOT);
				}
			}
			getAbnormalList().stop(4140); //TODO: [Bonux] ?????????? ???? ?? ???????????

		}
		autoShot();
	}

	public void scheduleDelete()
	{
		long time = 0L;

		if(Config.SERVICES_ENABLE_NO_CARRIER)
			time = NumberUtils.toInt(getVar("noCarrier"), Config.SERVICES_NO_CARRIER_DEFAULT_TIME);

		scheduleDelete(time * 1000L);
	}

	/**
	 * ???????????? ?????????????????? ???? ???????? ?????????? ?????????????????? ??????????, ???????? ???? ???????????? ?????????????????? ?????????????? ???? ???? ?????????? ??????????????????????.
	 * <br><br>
	 * TODO: ?????????? ???????????? ???????????? ?????? ????????????????????.<br>
	 * TODO: ?????????????? ???????????????? ?????????????? ?? ??????????????????, ?????? ?????? ?? ?????????????? ?????????????? ?????????????????? ?? ???????? ???? ?????? ?????????? ?? ????????.<br>
	 * <br>
	 *
	 * @param time ?????????? ?? ??????????????????????????
	 */
	public void scheduleDelete(long time)
	{
		if(isLogoutStarted() || isInOfflineMode())
			return;

		broadcastCharInfo();

		ThreadPoolManager.getInstance().schedule(() -> {
			if(!isConnected())
			{
				prepareToLogout1();
				prepareToLogout2();
				deleteMe();
			}
		}, time);
	}

	@Override
	protected void onDelete()
	{
		deleteCubics();

		// Stop all passives and augment options without broadcasting changes.
		getAbnormalList().stopAllOptions(false, false);

		super.onDelete();

		// ?????????????? ???????? ?? ?????????? ????????????????????
		if(_observePoint != null)
			_observePoint.deleteMe();

		//Send friendlists to friends that this player has logged off
		_friendList.notifyFriends(false);

		getBookMarkList().clear();

		_inventory.clear();
		_warehouse.clear();
		_summon = null;
		_pet = null;
		_arrowItem = null;
		_fistsWeaponItem = null;
		_chars = null;
		_enchantScroll = null;
		_appearanceStone = null;
		_appearanceExtractItem = null;
		_lastNpc = HardReferences.emptyRef();
		_observePoint = null;
	}

	public void setTradeList(List<TradeItem> list)
	{
		_tradeList = list;
	}

	public List<TradeItem> getTradeList()
	{
		return _tradeList;
	}

	public String getSellStoreName()
	{
		return _sellStoreName;
	}

	public void setSellStoreName(String name)
	{
		_sellStoreName = Strings.stripToSingleLine(name);
	}

	public String getPackageSellStoreName()
	{
		return _packageSellStoreName;
	}

	public void setPackageSellStoreName(String name)
	{
		_packageSellStoreName = Strings.stripToSingleLine(name);
	}

	public void setSellList(boolean packageSell, Map<Integer, TradeItem> list)
	{
		if(packageSell)
			_packageSellList = list;
		else
			_sellList = list;
	}

	public Map<Integer, TradeItem> getSellList()
	{
		return getSellList(_privatestore == STORE_PRIVATE_SELL_PACKAGE);
	}

	public Map<Integer, TradeItem> getSellList(boolean packageSell)
	{
		return packageSell ? _packageSellList : _sellList;
	}

	public String getBuyStoreName()
	{
		return _buyStoreName;
	}

	public void setBuyStoreName(String name)
	{
		_buyStoreName = Strings.stripToSingleLine(name);
	}

	public void setBuyList(List<TradeItem> list)
	{
		_buyList = list;
	}

	public List<TradeItem> getBuyList()
	{
		return _buyList;
	}

	public void setManufactureName(String name)
	{
		_manufactureName = Strings.stripToSingleLine(name);
	}

	public String getManufactureName()
	{
		return _manufactureName;
	}

	public Map<Integer, ManufactureItem> getCreateList()
	{
		return _createList;
	}

	public void setCreateList(Map<Integer, ManufactureItem> list)
	{
		_createList = list;
	}

	public void setPrivateStoreType(final int type)
	{
		_privatestore = type;
	}

	public boolean isInStoreMode()
	{
		return _privatestore != STORE_PRIVATE_NONE;
	}

	public boolean isInBuffStore()
	{
		return _privatestore == STORE_PRIVATE_BUFF;
	}

	public int getPrivateStoreType()
	{
		return _privatestore;
	}

	public IClientOutgoingPacket getPrivateStoreMsgPacket(Player forPlayer)
	{
		switch(getPrivateStoreType())
		{
			case STORE_PRIVATE_BUY:
				return new PrivateStoreBuyMsg(this, canTalkWith(forPlayer));
			case STORE_PRIVATE_SELL:
				return new PrivateStoreMsg(this, canTalkWith(forPlayer));
			case STORE_PRIVATE_SELL_PACKAGE:
				return new ExPrivateStoreWholeMsg(this, canTalkWith(forPlayer));
			case STORE_PRIVATE_MANUFACTURE:
				return new RecipeShopMsgPacket(this, canTalkWith(forPlayer));
		}

		return null;
	}

	public void broadcastPrivateStoreInfo()
	{
		if(!isVisible() || _privatestore == STORE_PRIVATE_NONE)
			return;

		sendPacket(getPrivateStoreMsgPacket(this));

		for(Player target : World.getAroundObservers(this))
			target.sendPacket(getPrivateStoreMsgPacket(target));
	}

	/**
	 * Set the _clan object, _clanId, _clanLeader Flag and title of the L2Player.<BR><BR>
	 *
	 * @param clan the clat to set
	 */
	public void setClan(Clan clan)
	{
		if(_clan != clan && _clan != null)
			unsetVar("canWhWithdraw");

		Clan oldClan = _clan;
		if(oldClan != null && clan == null)
			for(SkillEntry skillEntry : oldClan.getSkills())
				removeSkill(skillEntry, false);

		_clan = clan;

		if(clan == null)
		{
			_pledgeType = Clan.SUBUNIT_NONE;
			_pledgeRank = PledgeRank.VAGABOND;
			_powerGrade = 0;
			_apprentice = 0;
			_lvlJoinedAcademy = 0;
			getInventory().validateItems();
			return;
		}

		if(!clan.isAnyMember(getObjectId()))
		{
			setClan(null);
			setTitle("");
		}
	}

	@Override
	public Clan getClan()
	{
		return _clan;
	}

	public SubUnit getSubUnit()
	{
		return _clan == null ? null : _clan.getSubUnit(_pledgeType);
	}

	public ClanHall getClanHall()
	{
		int id = _clan != null ? _clan.getHasHideout() : 0;
		return ResidenceHolder.getInstance().getResidence(ClanHall.class, id);
	}

	public Castle getCastle()
	{
		int id = _clan != null ? _clan.getCastle() : 0;
		return ResidenceHolder.getInstance().getResidence(Castle.class, id);
	}

	public Alliance getAlliance()
	{
		return _clan == null ? null : _clan.getAlliance();
	}

	public boolean isClanLeader()
	{
		return _clan != null && getObjectId() == _clan.getLeaderId();
	}

	public boolean isAllyLeader()
	{
		return getAlliance() != null && getAlliance().getLeader().getLeaderId() == getObjectId();
	}

	@Override
	public void reduceArrowCount()
	{
		if(_arrowItem != null && _arrowItem.getTemplate().isQuiver())
			return;

		sendPacket(SystemMsg.YOU_CAREFULLY_NOCK_AN_ARROW);
		if(!getInventory().destroyItemByObjectId(getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LHAND), 1L))
		{
			getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, null);
			_arrowItem = null;
		}
	}

	/**
	 * Equip arrows needed in left hand and send a Server->Client packet ItemListPacket to the L2Player then return True.
	 */
	public boolean checkAndEquipArrows()
	{
		// Check if nothing is equipped in left hand
		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND) == null)
		{
			ItemInstance activeWeapon = getActiveWeaponInstance();
			if(activeWeapon != null)
			{
				if(activeWeapon.getItemType() == WeaponType.BOW)
					_arrowItem = getInventory().findArrowForBow(activeWeapon.getTemplate());
				else if(activeWeapon.getItemType() == WeaponType.CROSSBOW || activeWeapon.getItemType() == WeaponType.TWOHANDCROSSBOW)
					_arrowItem = getInventory().findArrowForCrossbow(activeWeapon.getTemplate());
			}

			// Equip arrows needed in left hand
			if(_arrowItem != null)
				getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, _arrowItem);
		}
		else
			// Get the L2ItemInstance of arrows equipped in left hand
			_arrowItem = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);

		return _arrowItem != null;
	}

	public void setUptime(final long time)
	{
		_uptime = time;
	}

	public long getUptime()
	{
		return System.currentTimeMillis() - _uptime;
	}

	public boolean isInParty()
	{
		return _party != null;
	}

	public void setParty(final Party party)
	{
		_party = party;
	}

	public void joinParty(final Party party, final boolean force)
	{
		if(party != null)
			party.addPartyMember(this, force);
	}

	public void leaveParty(final boolean force)
	{
		if(isInParty())
			_party.removePartyMember(this, false, force);
	}

	@Override
	public Party getParty()
	{
		return _party;
	}

	public void setLastPartyPosition(Location loc)
	{
		_lastPartyPosition = loc;
	}

	public Location getLastPartyPosition()
	{
		return _lastPartyPosition;
	}

	public boolean isGM()
	{
		return _playerAccess == null ? false : _playerAccess.IsGM;
	}

	/**
	 * ?????????? ???? ????????????????????????, ???? ?????????? ?????????????????????? ?????? ????
	 */
	public void setAccessLevel(final int level)
	{
		_accessLevel = level;
	}

	/**
	 * ?????????? ???? ????????????????????????, ???? ?????????? ?????????????????????? ?????? ????
	 */
	@Override
	public int getAccessLevel()
	{
		return _accessLevel;
	}

	public void setPlayerAccess(final PlayerAccess pa)
	{
		if(pa != null)
			_playerAccess = pa;
		else
			_playerAccess = new PlayerAccess();

		setAccessLevel(isGM() || _playerAccess.Menu ? 100 : 0);
	}

	public PlayerAccess getPlayerAccess()
	{
		return _playerAccess;
	}

	/**
	 * Update Stats of the L2Player client side by sending Server->Client packet UserInfo/StatusUpdatePacket to this L2Player and CIPacket/StatusUpdatePacket to all players around (broadcast).<BR><BR>
	 */
	public void updateStats(boolean refreshOverloaded, boolean refreshExpertisePenalty)
	{
		if(entering || isLogoutStarted())
			return;

		if(refreshOverloaded)
			refreshOverloaded();

		if(refreshExpertisePenalty)
			refreshExpertisePenalty();

		super.updateStats();

		for(Servitor servitor : getServitors())
			servitor.updateStats();
	}

	@Override
	public void updateStats()
	{
		updateStats(true, true);
	}

	@Override
	public void sendChanges()
	{
		if(entering || isLogoutStarted())
			return;
		super.sendChanges();
	}

	/**
	 * Send a Server->Client StatusUpdatePacket packet with Karma to the L2Player and all L2Player to inform (broadcast).
	 */
	public void updateKarma(boolean flagChanged)
	{
		sendStatusUpdate(true, true, StatusUpdatePacket.KARMA);
		if(flagChanged)
			broadcastRelation();
	}

	public boolean isOnline()
	{
		return _isOnline;
	}

	public void setIsOnline(boolean isOnline)
	{
		_isOnline = isOnline;
	}

	public void setOnlineStatus(boolean isOnline)
	{
		_isOnline = isOnline;
		updateOnlineStatus();
	}

	private void updateOnlineStatus()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE characters SET online=?, lastAccess=? WHERE obj_id=?");
			statement.setInt(1, isOnline() && !isInOfflineMode() ? 1 : 0);
			statement.setInt(2, (int) (System.currentTimeMillis() / 1000));
			statement.setInt(3, getObjectId());
			statement.execute();
		}
		catch(final Exception e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "" );
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void storeLastIpAndHWID(String ip, String hwid)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			if(StringUtils.isEmpty(hwid)) {
				statement = con.prepareStatement("UPDATE characters SET last_ip=? WHERE obj_Id=? LIMIT 1");
				statement.setString(1, ip);
				statement.setInt(2, getObjectId());
			} else {
				statement = con.prepareStatement("UPDATE characters SET last_ip=?, last_hwid=? WHERE obj_Id=? LIMIT 1");
				statement.setString(1, ip);
				statement.setString(2, hwid);
				statement.setInt(3, getObjectId());
			}
			statement.execute();
		}
		catch(Exception e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "Could not store %s IP and HWID: ", toString() );
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void decreaseKarma(final long val)
	{
		boolean flagChanged = _karma >= 0;
		long new_karma = _karma - val;

		if(new_karma < Integer.MIN_VALUE)
			new_karma = Integer.MIN_VALUE;

		if(_karma >= 0 && new_karma < 0)
		{
			if(_pvpFlag > 0)
			{
				_pvpFlag = 0;
				if(_PvPRegTask != null)
				{
					_PvPRegTask.cancel(true);
					_PvPRegTask = null;
				}
				sendStatusUpdate(true, true, StatusUpdatePacket.PVP_FLAG);
			}
		}

		setKarma((int) new_karma);

		updateKarma(flagChanged);
	}

	public void increaseKarma(final int val)
	{
		boolean flagChanged = _karma < 0;
		long new_karma = _karma + val;
		if(new_karma > Integer.MAX_VALUE)
			new_karma = Integer.MAX_VALUE;

		setKarma((int) new_karma);

		if(_karma > 0)
			updateKarma(flagChanged);
		else
			updateKarma(false);
	}

	public static Player create(int classId, int sex, String accountName, final String name, final int hairStyle, final int hairColor, final int face)
	{
		ClassId classID = ClassId.valueOf(classId);
		if (classID == null || classID.isDummy() || !classID.isOfLevel(ClassLevel.NONE))
			return null;

		PlayerTemplate template = PlayerTemplateHolder.getInstance().getPlayerTemplate(classID.getRace(), classID, Sex.VALUES[sex]);

		// Create a new L2Player with an account name
		Player player = new Player(IdFactory.getInstance().getNextId(), template, accountName);

		player.setName(name);
		player.setTitle("");
		player.setHairStyle(hairStyle);
		player.setHairColor(hairColor);
		player.setFace(face);
		player.setCreateTime(System.currentTimeMillis());

		if(Config.PC_BANG_POINTS_BY_ACCOUNT)
			player.setPcBangPoints(Integer.parseInt(AccountVariablesDAO.getInstance().select(player.getAccountName(), PC_BANG_POINTS_VAR, "0")), false);

		// Add the player in the characters table of the database
		if(!CharacterDAO.getInstance().insert(player))
			return null;

		int level = Config.STARTING_LVL;
		double hp = classID.getBaseHp(level);
		double mp = classID.getBaseMp(level);
		double cp = classID.getBaseCp(level);
		long exp = Experience.getExpForLevel(level);
		long sp = Config.STARTING_SP;
		boolean active = true;
		SubClassType type = SubClassType.BASE_CLASS;

		// Add the player subclass in the character_subclasses table of the database
		if(!CharacterSubclassDAO.getInstance().insert(player.getObjectId(), classId, exp, sp, hp, mp, cp, hp, mp, cp, level, active, type, ElementalElement.NONE))
			return null;

		return player;
	}

	public static Player restore(final int objectId, boolean fake)
	{
		if(GameObjectsStorage.getPlayers(false, false).size() >= GameServer.getInstance().getOnlineLimit())
		{
			_log.atWarning().log( "Player:restore: Impossible to restore character. Exceeded the number of online players!" );
			return null;
		}

		Player player = null;
		Connection con = null;
		Statement statement = null;
		Statement statement2 = null;
		PreparedStatement statement3 = null;
		ResultSet rset = null;
		ResultSet rset2 = null;
		ResultSet rset3 = null;
		try
		{
			// Retrieve the L2Player from the characters table of the database
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			statement2 = con.createStatement();
			rset = statement.executeQuery("SELECT * FROM `characters` WHERE `obj_Id`=" + objectId + " LIMIT 1");
			rset2 = statement2.executeQuery("SELECT `class_id` FROM `character_subclasses` WHERE `char_obj_id`=" + objectId + " AND `type`=" + SubClassType.BASE_CLASS.ordinal() + " LIMIT 1");

			if(rset.next() && rset2.next())
			{
				final ClassId classId = ClassId.valueOf(rset2.getInt("class_id"));
				final PlayerTemplate template = PlayerTemplateHolder.getInstance().getPlayerTemplate(classId.getRace(), classId, Sex.VALUES[rset.getInt("sex")]);;

				if(fake)
				{
					FakePlayerAITemplate fakeAiTemplate = FakePlayersHolder.getInstance().getAITemplate(classId.getRace(), classId.getType());
					if(fakeAiTemplate == null)
						return null;

					player = new Player(fakeAiTemplate, objectId, template);
				}
				else
					player = new Player(objectId, template);

				if(!player.getSubClassList().restore())
				{
					_log.atWarning().log( "Player:restore: Could not restore character due to a failure when restoring sub-classes!" );
					return null;
				}

				player.restoreVariables();
				player.loadInstanceReuses();
				player.getBookMarkList().setCapacity(rset.getInt("bookmarks"));
				player.getBookMarkList().restore();
				player.setBotRating(rset.getInt("bot_rating"));
				player.getFriendList().restore();
				player.getBlockList().restore();
				player.getProductHistoryList().restore();
				player.getCostumeList().restore();
				player.setPostFriends(CharacterPostFriendDAO.getInstance().select(player));
				CharacterGroupReuseDAO.getInstance().select(player);

				player.setLogin(rset.getString("account_name"));
				player.setName(rset.getString("char_name"));

				player.setFace(rset.getInt("face"));
				player.setBeautyFace(rset.getInt("beautyFace"));
				player.setHairStyle(rset.getInt("hairStyle"));
				player.setBeautyHairStyle(rset.getInt("beautyHairStyle"));
				player.setHairColor(rset.getInt("hairColor"));
				player.setBeautyHairColor(rset.getInt("beautyHairColor"));
				player.setHeading(0);

				player.setKarma(rset.getInt("karma"));
				player.setPvpKills(rset.getInt("pvpkills"));
				player.setPkKills(rset.getInt("pkkills"));
				player.setLeaveClanTime(rset.getLong("leaveclan") * 1000L);
				if(player.getLeaveClanTime() > 0 && player.canJoinClan())
					player.setLeaveClanTime(0);
				player.setDeleteClanTime(rset.getLong("deleteclan") * 1000L);
				if(player.getDeleteClanTime() > 0 && player.canCreateClan())
					player.setDeleteClanTime(0);

				player.setNoChannel(rset.getLong("nochannel") * 1000L);
				if(player.getNoChannel() > 0 && player.getNoChannelRemained() < 0)
					player.setNoChannel(0);

				player.setOnlineTime(rset.getLong("onlinetime") * 1000L);

				final int clanId = rset.getInt("clanid");
				if(clanId > 0)
				{
					player.setClan(ClanTable.getInstance().getClan(clanId));
					player.setPledgeType(rset.getInt("pledge_type"));
					player.setPowerGrade(rset.getInt("pledge_rank"));
					player.setLvlJoinedAcademy(rset.getInt("lvl_joined_academy"));
					player.setApprentice(rset.getInt("apprentice"));
				}

				player.setCreateTime(rset.getLong("createtime") * 1000L);
				player.setDeleteTimer(rset.getInt("deletetime"));

				player.setTitle(rset.getString("title"));

				if(player.getVar("titlecolor") != null)
					player.setTitleColor(Integer.decode("0x" + player.getVar("titlecolor")));

				if(player.getVar("namecolor") == null)
					if(player.isGM())
						player.setNameColor(Config.GM_NAME_COLOUR);
					else if(player.getClan() != null && player.getClan().getLeaderId() == player.getObjectId())
						player.setNameColor(Config.CLANLEADER_NAME_COLOUR);
					else
						player.setNameColor(Config.NORMAL_NAME_COLOUR);
				else
					player.setNameColor(Integer.decode("0x" + player.getVar("namecolor")));

				if(Config.AUTO_LOOT_INDIVIDUAL)
				{
					player._autoLoot = player.getVarBoolean("AutoLoot", Config.AUTO_LOOT);
					player._autoLootOnlyAdena = player.getVarBoolean("AutoLootOnlyAdena", Config.AUTO_LOOT_ONLY_ADENA);
					player.AutoLootHerbs = player.getVarBoolean("AutoLootHerbs", Config.AUTO_LOOT_HERBS);
				}

				player.setUptime(System.currentTimeMillis());
				player.setLastAccess(rset.getInt("lastAccess"));

				player.setRecomHave(rset.getInt("rec_have"));
				player.setRecomLeft(rset.getInt("rec_left"));

				if(!Config.USE_CLIENT_LANG && Config.CAN_SELECT_LANGUAGE)
					player.setLanguage(player.getVar(Language.LANG_VAR));

				player.setKeyBindings(rset.getBytes("key_bindings"));
				if(Config.PC_BANG_POINTS_BY_ACCOUNT)
					player.setPcBangPoints(Integer.parseInt(AccountVariablesDAO.getInstance().select(player.getAccountName(), PC_BANG_POINTS_VAR, "0")), false);
				else
					player.setPcBangPoints(rset.getInt("pcBangPoints"), false);

				player.setFame(rset.getInt("fame"), null, false);

				player.setUsedWorldChatPoints(rset.getInt("used_world_chat_points"));

				player.setHideHeadAccessories(rset.getInt("hide_head_accessories") > 0);

				player.restoreRecipeBook();

				if(Config.ENABLE_OLYMPIAD)
					player.setHero(Hero.getInstance().isHero(player.getObjectId()));

				if(!player.isHero())
					player.setHero(CustomHeroDAO.getInstance().isCustomHero(player.getObjectId()));

				player.updatePledgeRank();

				player.setXYZ(rset.getInt("x"), rset.getInt("y"), rset.getInt("z"));

				int reflection = 0;

				long jailExpireTime = player.getVarExpireTime(JAILED_VAR);
				if(jailExpireTime > System.currentTimeMillis())
				{
					reflection = ReflectionManager.JAIL.getId();
					if(!player.isInZone("[gm_prison]"))
						player.setLoc(Location.findPointToStay(player, AdminFunctions.JAIL_SPAWN, 50, 200));
					player.setIsInJail(true);
					player.startUnjailTask(player, (int) (jailExpireTime - System.currentTimeMillis() / 60000));
				}
				else
				{
					String ref = player.getVar("reflection");
					if(ref != null)
					{
						reflection = Integer.parseInt(ref);
						if(reflection != ReflectionManager.PARNASSUS.getId() && reflection != ReflectionManager.GIRAN_HARBOR.getId()) // ???? ?????????????? ?????????? ???? ????, ??????????????
						{
							String back = player.getVar("backCoords");
							if(back != null)
							{
								player.setLoc(Location.parseLoc(back));
								player.unsetVar("backCoords");
							}
							else
								player.setLoc(STABLE_LOCATION);

							reflection = 0;
							player.unsetVar("reflection");
						}
					}
				}

				player.setReflection(reflection);

				EventHolder.getInstance().findEvent(player);

				//TODO [G1ta0] ?????????????????? ???? ??????????
				Quest.restoreQuestStates(player);

				player.getInventory().restore();

				player.setActiveSubClass(player.getActiveClassId(), false, true);

				player.getVIP().restore();
				player.getProductHistoryList().restore();
				player.getAttendanceRewards().restore();

				player.restoreSummons();

				try
				{
					String var = player.getVar("ExpandInventory");
					if(var != null)
						player.setExpandInventory(Integer.parseInt(var));
				}
				catch(Exception e)
				{
					_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "" );
				}

				try
				{
					String var = player.getVar("ExpandWarehouse");
					if(var != null)
						player.setExpandWarehouse(Integer.parseInt(var));
				}
				catch(Exception e)
				{
					_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "" );
				}

				try
				{
					String var = player.getVar(NO_ANIMATION_OF_CAST_VAR);
					if(var != null)
						player.setNotShowBuffAnim(Boolean.parseBoolean(var));
				}
				catch(Exception e)
				{
					_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "" );
				}

				try
				{
					String var = player.getVar(NO_TRADERS_VAR);
					if(var != null)
						player.setNotShowTraders(Boolean.parseBoolean(var));
				}
				catch(Exception e)
				{
					_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "" );
				}

				try
				{
					String var = player.getVar("pet");
					if(var != null)
						player.setPetControlItem(Integer.parseInt(var));
				}
				catch(Exception e)
				{
					_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "" );
				}

				statement3 = con.prepareStatement("SELECT obj_Id, char_name FROM characters WHERE account_name=? AND obj_Id!=?");
				statement3.setString(1, player._login);
				statement3.setInt(2, objectId);
				rset3 = statement3.executeQuery();
				while(rset3.next())
				{
					final Integer charId = rset3.getInt("obj_Id");
					final String charName = rset3.getString("char_name");
					player._chars.put(charId, charName);
				}

				DbUtils.close(statement3, rset3);

				//if(!player.isGM())
				{
					List<Zone> zones = new ArrayList<>();

					World.getZones(zones, player.getLoc(), player.getReflection());

					if(!zones.isEmpty())
						for(Zone zone : zones)
							if(zone.getType() == ZoneType.no_restart)
							{
								if(System.currentTimeMillis() / 1000L - player.getLastAccess() > zone.getRestartTime())
								{
									player.sendMessage(new CustomMessage("l2s.gameserver.network.l2.c2s.EnterWorld.TeleportedReasonNoRestart"));
									player.setLoc(TeleportUtils.getRestartPoint(player, RestartType.VILLAGE).getLoc());
								}
							}
							else if(zone.getType() == ZoneType.SIEGE)
							{
								SiegeEvent<?, ?> currentSiegeEvent = null;
								for(SiegeEvent<?, ?> siegeEvent : player.getEvents(SiegeEvent.class))
								{
									if(zone.containsEvent(siegeEvent))
									{
										currentSiegeEvent = siegeEvent;
										break;
									}
								}

								if(currentSiegeEvent != null)
									player.setLoc(currentSiegeEvent.getEnterLoc(player, zone));
								else
								{
									int residenceId = zone.getParams().getInteger("residence", -1);
									if (residenceId != -1) {
										Residence r = ResidenceHolder.getInstance().getResidence(residenceId);
										player.setLoc(r.getNotOwnerRestartPoint(player));
									}
								}
							}
				}

				player.getMacroses().restore();

				//FIXME [VISTALL] ?????????? ?????
				player.refreshExpertisePenalty();
				player.refreshOverloaded();

				player.getWarehouse().restore();
				player.getFreight().restore();

				player.restorePrivateStore();

				player.updateKetraVarka();
				player.updateRam();
				player.checkDailyCounters();
				player.checkWeeklyCounters();

				GameObjectsStorage.put(player);
			}
		}
		catch(final Exception e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "Player:restore: Could not restore char data!" );
		}
		finally
		{
			DbUtils.closeQuietly(statement2, rset2);
			DbUtils.closeQuietly(statement3, rset3);
			DbUtils.closeQuietly(con, statement, rset);
		}
		return player;
	}

	/**
	 * Update L2Player stats in the characters table of the database.
	 */
	public void store(boolean fast)
	{
		if(!_storeLock.tryLock())
			return;

		try
		{
			Connection con = null;
			PreparedStatement statement = null;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement(//
						"UPDATE characters SET face=?,beautyFace=?,hairStyle=?,beautyHairStyle=?,hairColor=?,beautyHairColor=?,sex=?,x=?,y=?,z=?" + //
								",karma=?,pvpkills=?,pkkills=?,rec_have=?,rec_left=?,clanid=?,deletetime=?," + //
								"title=?,accesslevel=?,online=?,leaveclan=?,deleteclan=?,nochannel=?," + //
								"onlinetime=?,pledge_type=?,pledge_rank=?,lvl_joined_academy=?,apprentice=?,key_bindings=?,pcBangPoints=?,char_name=?,fame=?,bookmarks=?,bot_rating=?,used_world_chat_points=?,hide_head_accessories=? WHERE obj_Id=? LIMIT 1");
				statement.setInt(1, getFace());
				statement.setInt(2, getBeautyFace());
				statement.setInt(3, getHairStyle());
				statement.setInt(4, getBeautyHairStyle());
				statement.setInt(5, getHairColor());
				statement.setInt(6, getBeautyHairColor());
				statement.setInt(7, getSex().ordinal());
				if(_stablePoint == null) // ???????? ?????????? ?????????????????? ?? ?????????? ?? ?????????????? ?????? ?????????????????? ???? ?????????? (???????????????? ???? ??????????????) ???? ?????????????????????? ?????????????????? ????????????????????
				{
					statement.setInt(8, getX());
					statement.setInt(9, getY());
					statement.setInt(10, getZ());
				}
				else
				{
					statement.setInt(8, _stablePoint.x);
					statement.setInt(9, _stablePoint.y);
					statement.setInt(10, _stablePoint.z);
				}
				statement.setInt(11, getKarma());
				statement.setInt(12, getPvpKills());
				statement.setInt(13, getPkKills());
				statement.setInt(14, getRecomHave());
				statement.setInt(15, getRecomLeft());
				statement.setInt(16, getClanId());
				statement.setInt(17, getDeleteTimer());
				statement.setString(18, _title);
				statement.setInt(19, _accessLevel);
				statement.setInt(20, isOnline() && !isInOfflineMode() ? 1 : 0);
				statement.setLong(21, getLeaveClanTime() / 1000L);
				statement.setLong(22, getDeleteClanTime() / 1000L);
				statement.setLong(23, _NoChannel > 0 ? getNoChannelRemained() / 1000 : _NoChannel);
				statement.setInt(24, getOnlineTime());
				statement.setInt(25, getPledgeType());
				statement.setInt(26, getPowerGrade());
				statement.setInt(27, getLvlJoinedAcademy());
				statement.setInt(28, getApprentice());
				statement.setBytes(29, getKeyBindings());
				statement.setInt(30, Config.PC_BANG_POINTS_BY_ACCOUNT ? 0 : getPcBangPoints());
				statement.setString(31, getName());
				statement.setInt(32, getFame());
				statement.setInt(33, getBookMarkList().getCapacity());
				statement.setInt(34, getBotRating());
				statement.setInt(35, getUsedWorldChatPoints());
				statement.setInt(36, hideHeadAccessories() ? 1 : 0);
				statement.setInt(37, getObjectId());

				statement.executeUpdate();
				GameStats.increaseUpdatePlayerBase();

				if(!fast)
				{
					EffectsDAO.getInstance().insert(this);
					CharacterGroupReuseDAO.getInstance().insert(this);
					storeDisableSkills();
				}

				storeCharSubClasses();
				getBookMarkList().store();
				getDailyMissionList().store();
				getElementalList().store();
				getCostumeList().store();

				if(Config.PC_BANG_POINTS_BY_ACCOUNT)
					AccountVariablesDAO.getInstance().insert(getAccountName(), PC_BANG_POINTS_VAR, String.valueOf(getPcBangPoints()));
			}
			catch(Exception e)
			{
				_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "Could not store char data: %s!", this );
			}
			finally
			{
				DbUtils.closeQuietly(con, statement);
			}
		}
		finally
		{
			_storeLock.unlock();
		}
	}

	/**
	 * Add a skill to the L2Player _skills and its Func objects to the calculator set of the L2Player and save update in the character_skills table of the database.
	 *
	 * @return The L2Skill replaced or null if just added a new L2Skill
	 */
	public SkillEntry addSkill(final SkillEntry newSkillEntry, final boolean store)
	{
		if(newSkillEntry == null)
			return null;

		// Add a skill to the L2Player _skills and its Func objects to the calculator set of the L2Player
		SkillEntry oldSkillEntry = addSkill(newSkillEntry);
		if(newSkillEntry.equals(oldSkillEntry))
			return oldSkillEntry;

		// Add or update a L2Player skill in the character_skills table of the database
		if(store)
			storeSkill(newSkillEntry);

		return oldSkillEntry;
	}

	public SkillEntry removeSkill(SkillInfo skillInfo, boolean fromDB) {
		return removeSkill(skillInfo, false, fromDB);
	}

	public SkillEntry removeSkill(SkillInfo skillInfo, boolean stopEffects, boolean fromDB)
	{
		if(skillInfo == null)
			return null;
		return removeSkill(skillInfo.getId(), stopEffects, fromDB);
	}

	/**
	 * Remove a skill from the L2Character and its Func objects from calculator set of the L2Character and save update in the character_skills table of the database.
	 *
	 * @return The L2Skill removed
	 * @param id
	 * @param fromDB
	 */
	public SkillEntry removeSkill(int id, boolean fromDB) {
		return removeSkill(id, false, fromDB);
	}

	/**
	 * Remove a skill from the L2Character and its Func objects from calculator set of the L2Character and save update in the character_skills table of the database.
	 *
	 * @return The L2Skill removed
	 */
	public SkillEntry removeSkill(int id, boolean stopEffects, boolean fromDB)
	{
		// Remove a skill from the L2Character and its Func objects from calculator set of the L2Character
		SkillEntry oldSkillEntry = removeSkillById(id, stopEffects);

		if(!fromDB)
			return oldSkillEntry;

		if(oldSkillEntry != null)
		{
			Connection con = null;
			PreparedStatement statement = null;
			try
			{
				// Remove or update a L2Player skill from the character_skills table of the database
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("DELETE FROM character_skills WHERE skill_id=? AND char_obj_id=? AND (class_index=? OR class_index=-1 OR class_index=-2)");
				statement.setInt(1, oldSkillEntry.getId());
				statement.setInt(2, getObjectId());
				statement.setInt(3, getActiveClassId());
				statement.execute();
			}
			catch(final Exception e)
			{
				_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "Could not delete skill!" );
			}
			finally
			{
				DbUtils.closeQuietly(con, statement);
			}
		}

		return oldSkillEntry;
	}

	/**
	 * Add or update a L2Player skill in the character_skills table of the database.
	 */
	private void storeSkill(final SkillEntry newSkillEntry)
	{
		if(newSkillEntry == null) // ????????????-???? ????????????????????
		{
			_log.atWarning().log( "could not store new skill. its NULL" );
			return;
		}

		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement("REPLACE INTO character_skills (char_obj_id,skill_id,skill_level,class_index) values(?,?,?,?)");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newSkillEntry.getId());
			statement.setInt(3, newSkillEntry.getLevel());

			// ???????????? ???????????????????????? ???????????????? ???? ???????? ??????-??????????????.
			if(SkillAcquireHolder.getInstance().containsInTree(newSkillEntry.getTemplate(), AcquireType.CERTIFICATION))
				statement.setInt(4, -1);
			else
				statement.setInt(4, getActiveClassId());

			statement.execute();
		}
		catch(final Exception e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "Error could not store skills!" );
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	private void restoreSkills()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT skill_id,skill_level FROM character_skills WHERE char_obj_id=? AND (class_index=? OR class_index=-1 OR class_index=-2)");
			statement.setInt(1, getObjectId());
			statement.setInt(2, getActiveClassId());
			rset = statement.executeQuery();

			while(rset.next())
			{
				final SkillEntry skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.NONE, rset.getInt("skill_id"), rset.getInt("skill_level"));
				if(skillEntry == null)
					continue;

				// Remove skill if not possible
				if(!isGM())
				{
					Skill skill = skillEntry.getTemplate();
					if(!SkillAcquireHolder.getInstance().isSkillPossible(this, skill))
					{
						removeSkill(skillEntry, true);
						//removeSkillFromShortCut(skill.getId());
						//TODO audit
						continue;
					}
				}

				addSkill(skillEntry);
			}

			// Restore Hero skills at main class only
			checkHeroSkills();

			// Restore clan skills
			if(_clan != null)
				_clan.addSkillsQuietly(this);

			if(Config.UNSTUCK_SKILL && getSkillLevel(1050) < 0)
				addSkill(SkillEntry.makeSkillEntry(SkillEntryType.NONE, 2099, 1));

			for(OptionDataTemplate optionData : _options.valueCollection())
			{
				optionData.apply(this);

				for(SkillEntry skillEntry : optionData.getSkills())
					addSkill(skillEntry);
			}

			if(isGM())
				giveGMSkills();
		}
		catch(final Exception e)
		{
			_log.atWarning().log( "Could not restore skills for player objId: %s", getObjectId() );
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "" );
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public void storeDisableSkills()
	{
		Connection con = null;
		Statement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			statement.executeUpdate("DELETE FROM character_skills_save WHERE char_obj_id = " + getObjectId() + " AND class_index=" + getActiveClassId() + " AND `end_time` < " + System.currentTimeMillis());

			if(_skillReuses.isEmpty())
				return;

			SqlBatch b = new SqlBatch("REPLACE INTO `character_skills_save` (`char_obj_id`,`skill_id`,`skill_level`,`class_index`,`end_time`,`reuse_delay_org`) VALUES");
			synchronized (_skillReuses)
			{
				StringBuilder sb;
				for(TimeStamp timeStamp : _skillReuses.valueCollection())
				{
					if(timeStamp.hasNotPassed())
					{
						sb = new StringBuilder("(");
						sb.append(getObjectId()).append(",");
						sb.append(timeStamp.getId()).append(",");
						sb.append(timeStamp.getLevel()).append(",");
						sb.append(getActiveClassId()).append(",");
						sb.append(timeStamp.getEndTime()).append(",");
						sb.append(timeStamp.getReuseBasic()).append(")");
						b.write(sb.toString());
					}
				}
			}
			if(!b.isEmpty())
				statement.executeUpdate(b.close());
		}
		catch(final Exception e)
		{
			_log.atWarning().log( "Could not store disable skills data: %s", e );
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void restoreDisableSkills()
	{
		_skillReuses.clear();

		Connection con = null;
		Statement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			rset = statement.executeQuery("SELECT skill_id,skill_level,end_time,reuse_delay_org FROM character_skills_save WHERE char_obj_id=" + getObjectId() + " AND class_index=" + getActiveClassId());
			while(rset.next())
			{
				int skillId = rset.getInt("skill_id");
				int skillLevel = rset.getInt("skill_level");
				long endTime = rset.getLong("end_time");
				long rDelayOrg = rset.getLong("reuse_delay_org");
				long curTime = System.currentTimeMillis();

				Skill skill = SkillHolder.getInstance().getSkill(skillId, skillLevel);

				if(skill != null && endTime - curTime > 500)
					_skillReuses.put(skill.getReuseHash(), new TimeStamp(skill, endTime, rDelayOrg));
			}
			DbUtils.close(statement);

			statement = con.createStatement();
			statement.executeUpdate("DELETE FROM character_skills_save WHERE char_obj_id = " + getObjectId() + " AND class_index=" + getActiveClassId() + " AND `end_time` < " + System.currentTimeMillis());
		}
		catch(Exception e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "Could not restore active skills data!" );
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	@Override
	public boolean consumeItem(int itemConsumeId, long itemCount, boolean sendMessage)
	{
		return ItemFunctions.deleteItem(this, itemConsumeId, itemCount, sendMessage);
	}

	@Override
	public boolean consumeItemMp(int itemId, int mp)
	{
		for (ItemInstance item : getInventory().getPaperdollItems(item -> item.getItemId() == itemId)) {
			final int newMp = item.getLifeTime() - mp;
			if (newMp >= 0) {
				item.setLifeTime(newMp);
				sendPacket(new InventoryUpdatePacket().addModifiedItem(this, item));
				return true;
			}
			break;
		}

		return false;
	}

	/**
	 * @return True if the L2Player is a Mage.<BR><BR>
	 */
	@Override
	public boolean isMageClass()
	{
		return getClassId().isMage();
	}

	/**
	 * ??????????????????, ?????????? ???? ???????????????????????? ?? ???????? ????????.
	 *
	 * @return ?????????? ???? ??????????????????????
	 */
	@SuppressWarnings("rawtypes")
	public boolean checkLandingState()
	{
		if(isInZone(ZoneType.no_landing))
			return false;

		List<SiegeEvent> siegeEvents = getEvents(SiegeEvent.class);
		if(siegeEvents.isEmpty())
			return true;

		for(SiegeEvent<?, ?> siege : siegeEvents)
		{
			Residence unit = siege.getResidence();
			if(unit != null && getClan() != null && isClanLeader() && getClan().getCastle() == unit.getId())
				return true;
		}

		return false;
	}

	public void setMount(int controlItemObjId, int npcId, int level, int currentFeed)
	{
		Mount mount = Mount.create(this, controlItemObjId, npcId, level, currentFeed);
		if(mount != null)
			setMount(mount);
	}

	public void setMount(Mount mount)
	{
		if(_mount == mount)
			return;

		Mount oldMount = _mount;
		_mount = null;
		if(oldMount != null) // Dismount
			oldMount.onUnride();

		if(mount != null)
		{
			_mount = mount;
			_mount.onRide();
		}
	}

	@Override
	public boolean isMounted()
	{
		return _mount != null;
	}

	public Mount getMount()
	{
		return _mount;
	}

	public int getMountControlItemObjId()
	{
		return isMounted() ? _mount.getControlItemObjId() : 0;
	}

	public int getMountNpcId()
	{
		return isMounted() ? _mount.getNpcId() : 0;
	}

	public int getMountLevel()
	{
		return isMounted() ? _mount.getLevel() : 0;
	}

	public int getMountCurrentFeed()
	{
		return isMounted() ? _mount.getCurrentFeed() : 0;
	}

	public void unEquipWeapon()
	{
		ItemInstance wpn = getSecondaryWeaponInstance();
		if(wpn != null)
		{
			sendDisarmMessage(wpn);
			getInventory().unEquipItem(wpn);
		}

		wpn = getActiveWeaponInstance();
		if(wpn != null)
		{
			sendDisarmMessage(wpn);
			getInventory().unEquipItem(wpn);
		}

		abortAttack(true, true);
		abortCast(true, true);
	}

	public void sendDisarmMessage(ItemInstance wpn)
	{
		if(wpn.getEnchantLevel() > 0)
		{
			SystemMessage sm = new SystemMessage(SystemMessage.EQUIPMENT_OF__S1_S2_HAS_BEEN_REMOVED);
			sm.addNumber(wpn.getEnchantLevel());
			sm.addItemName(wpn.getItemId());
			sendPacket(sm);
		}
		else
		{
			SystemMessage sm = new SystemMessage(SystemMessage.S1__HAS_BEEN_DISARMED);
			sm.addItemName(wpn.getItemId());
			sendPacket(sm);
		}
	}

	/**
	 * ?????????????????????????? ?????? ?????????????????????????? ????????????.
	 *
	 * @param type ?????? ????????????:<BR>
	 *             <ul>
	 *             <li>WarehouseType.PRIVATE
	 *             <li>WarehouseType.CLAN
	 *             <li>WarehouseType.CASTLE
	 *             </ul>
	 */
	public void setUsingWarehouseType(final WarehouseType type)
	{
		_usingWHType = type;
	}

	/**
	 * ??????????????????????????????????????????? ????????????? ???????????????????????????????????????????????????? ????????????????????????.
	 *
	 * @return null ???????????? ????????????? ????????????????????????:<br>
	 *         <ul>
	 *         <li>WarehouseType.PRIVATE
	 *         <li>WarehouseType.CLAN
	 *         <li>WarehouseType.CASTLE
	 *         </ul>
	 */
	public WarehouseType getUsingWarehouseType()
	{
		return _usingWHType;
	}

	public Collection<Cubic> getCubics()
	{
		return _cubics == null ? Collections.<Cubic> emptyList() : _cubics.valueCollection();
	}

	@Override
	public void deleteCubics()
	{
		for(Cubic cubic : getCubics())
			cubic.delete();
	}

	public void addCubic(Cubic cubic)
	{
		if(_cubics == null)
			_cubics = new CHashIntObjectMap<Cubic>(3);

		Cubic oldCubic = _cubics.get(cubic.getSlot());
		if(oldCubic != null)
			oldCubic.delete();

		_cubics.put(cubic.getSlot(), cubic);
		sendPacket(new ExUserInfoCubic(this));
		broadcastCharInfo();
	}

	public void removeCubic(int slot)
	{
		if(_cubics != null)
			_cubics.remove(slot);

		sendPacket(new ExUserInfoCubic(this));
		broadcastCharInfo();
	}

	public Cubic getCubic(int slot)
	{
		return _cubics == null ? null : _cubics.get(slot);
	}

	@Override
	public String toString()
	{
		return getName() + "[id=" + getObjectId() + ", hwid=" + getHWID() + "]";
	}

	/**
	 * @return the modifier corresponding to the Enchant Abnormal of the Active Weapon (Min : 127).<BR><BR>
	 */
	@Override
	public int getEnchantEffect()
	{
		final ItemInstance wpn = getActiveWeaponInstance();
		if(wpn == null)
			return 0;

		return Math.min(127, wpn.getFixedEnchantLevel(this));
	}

	public int getVariation1Id()
	{
		final ItemInstance wpn = getActiveWeaponInstance();
		if(wpn == null)
			return 0;

		return wpn.getVariation1Id();
	}

	public int getVariation2Id()
	{
		final ItemInstance wpn = getActiveWeaponInstance();
		if(wpn == null)
			return 0;

		return wpn.getVariation2Id();
	}

	/**
	 * Set the _lastFolkNpc of the L2Player corresponding to the last Folk witch one the player talked.<BR><BR>
	 */
	public void setLastNpc(final NpcInstance npc)
	{
		if(npc == null)
			_lastNpc = HardReferences.emptyRef();
		else
			_lastNpc = npc.getRef();
	}

	/**
	 * @return the _lastFolkNpc of the L2Player corresponding to the last Folk witch one the player talked.<BR><BR>
	 */
	public NpcInstance getLastNpc()
	{
		return _lastNpc.get();
	}

	public void setMultisell(MultiSellListContainer multisell)
	{
		_multisell = multisell;
	}

	public MultiSellListContainer getMultisell()
	{
		return _multisell;
	}

	@Override
	public boolean unChargeShots(boolean spirit)
	{
		ItemInstance weapon = getActiveWeaponInstance();
		if(weapon == null)
			return false;

		if(spirit)
			weapon.setChargedSpiritshotPower(0, 0, 0);
		else
			weapon.setChargedSoulshotPower(0);

		autoShot();
		return true;
	}

	public boolean unChargeFishShot()
	{
		ItemInstance weapon = getActiveWeaponInstance();
		if(weapon == null)
			return false;

		weapon.setChargedFishshotPower(0);

		autoShot();
		return true;
	}

	public void autoShot()
	{
		for(IntObjectPair<ShotType> entry : _activeAutoShots.entrySet())
		{
			int shotId = entry.getKey();
			ItemInstance item = getInventory().getItemByItemId(shotId);
			if(item == null)
			{
				removeAutoShot(shotId, false, entry.getValue());
				continue;
			}
			item.getTemplate().useItem(this, item, false, false);
		}
	}

	@Override
	public double getChargedSoulshotPower()
	{
		ItemInstance weapon = getActiveWeaponInstance();
		if(weapon != null && weapon.getChargedSoulshotPower() > 0)
			return getStat().getValue(DoubleStat.SOULSHOT_POWER, weapon.getChargedSoulshotPower());
		return 0;
	}

	@Override
	public void setChargedSoulshotPower(double val)
	{
		ItemInstance weapon = getActiveWeaponInstance();
		if(weapon != null)
			weapon.setChargedSoulshotPower(val);
	}

	@Override
	public double getChargedSpiritshotPower()
	{
		ItemInstance weapon = getActiveWeaponInstance();
		if(weapon != null && weapon.getChargedSpiritshotPower() > 0)
			return getStat().getValue(DoubleStat.SPIRITSHOT_POWER, weapon.getChargedSpiritshotPower());
		return 0;
	}

	@Override
	public double getChargedSpiritshotHealBonus()
	{
		ItemInstance weapon = getActiveWeaponInstance();
		if(weapon != null)
			return weapon.getChargedSpiritshotHealBonus();
		return 0;
	}

	@Override
	public void setChargedSpiritshotPower(double power, int unk, double healBonus)
	{
		ItemInstance weapon = getActiveWeaponInstance();
		if(weapon != null)
			weapon.setChargedSpiritshotPower(power, unk, healBonus);
	}

	public double getChargedFishshotPower()
	{
		ItemInstance weapon = getActiveWeaponInstance();
		if(weapon != null)
			return weapon.getChargedFishshotPower();
		return 0;
	}

	public void setChargedFishshotPower(double val)
	{
		ItemInstance weapon = getActiveWeaponInstance();
		if(weapon != null)
			weapon.setChargedFishshotPower(val);
	}

	public boolean addAutoShot(int itemId, boolean sendMessage, ShotType type)
	{
		if(Config.EX_USE_AUTO_SOUL_SHOT)
		{
			for(IntObjectPair<ShotType> entry : _activeAutoShots.entrySet())
			{
				if(entry.getValue() == type)
					_activeAutoShots.remove(entry.getKey());
			}

			if(type == ShotType.SOULSHOT || type == ShotType.SPIRITSHOT)
			{
				WeaponTemplate weaponTemplate = getActiveWeaponTemplate();
				if(weaponTemplate == null)
					return false;

				ItemTemplate shotTemplate = ItemHolder.getInstance().getTemplate(itemId);
				if(shotTemplate == null)
					return false;

				if(shotTemplate.getGrade().extGrade() != weaponTemplate.getGrade().extGrade())
					return false;
			}
			else if(type == ShotType.BEAST_SOULSHOT || type == ShotType.BEAST_SPIRITSHOT)
			{
				if(getServitorsCount() == 0)
					return false;
			}
		}

		if(_activeAutoShots.put(itemId, type) != type)
		{
			if(!Config.EX_USE_AUTO_SOUL_SHOT)
				sendPacket(new ExAutoSoulShot(itemId, 1, type));
			if(sendMessage)
				sendPacket(new SystemMessagePacket(SystemMsg.THE_AUTOMATIC_USE_OF_S1_HAS_BEEN_ACTIVATED).addItemName(itemId));
			return true;
		}
		return false;
	}

	public boolean manuallyAddAutoShot(int itemId, ShotType type, boolean save)
	{
		if(addAutoShot(itemId, true, type))
		{
			if(Config.EX_USE_AUTO_SOUL_SHOT)
			{
				if(save)
					setVar(ACTIVE_SHOT_ID_VAR + "_" + type.ordinal(), itemId);
				else
					unsetVar(ACTIVE_SHOT_ID_VAR + "_" + type.ordinal());
			}
			return true;
		}
		return false;
	}

	public void sendActiveAutoShots()
	{
		if(Config.EX_USE_AUTO_SOUL_SHOT)
			return;

		for(IntObjectPair<ShotType> entry : _activeAutoShots.entrySet())
			sendPacket(new ExAutoSoulShot(entry.getKey(), 1, entry.getValue()));
	}

	public void initActiveAutoShots()
	{
		if(!Config.EX_USE_AUTO_SOUL_SHOT)
			return;

		for(ShotType type : ShotType.VALUES)
		{
			if(!initSavedActiveShot(type))
				sendPacket(new ExAutoSoulShot(0, 1, type));
		}
	}

	public boolean initSavedActiveShot(ShotType type)
	{
		if(!Config.EX_USE_AUTO_SOUL_SHOT)
			return false;

		int shotId = getVarInt(ACTIVE_SHOT_ID_VAR + "_" + type.ordinal(), 0);
		if(shotId > 0)
		{
			ItemInstance item = getInventory().getItemByItemId(shotId);
			if(item != null)
			{
				IItemHandler handler = item.getTemplate().getHandler();
				if(handler != null && handler.isAutoUse())
				{
					if(addAutoShot(shotId, true, type))
					{
						sendPacket(new ExAutoSoulShot(shotId, 3, type));
						useItem(item, false, false);
						return true;
					}
				}
			}
		}
		else if(shotId == -1)
		{
			sendPacket(new ExAutoSoulShot(0, 2, type));
			return true;
		}
		return false;
	}

	public void removeAutoShots(boolean uncharge)
	{
		if(Config.EX_USE_AUTO_SOUL_SHOT)
			return;

		for(IntObjectPair<ShotType> entry : _activeAutoShots.entrySet())
			removeAutoShot(entry.getKey(), false, entry.getValue());

		if(uncharge)
		{
			ItemInstance weapon = getActiveWeaponInstance();
			if(weapon != null)
			{
				weapon.setChargedSoulshotPower(0);
				weapon.setChargedSpiritshotPower(0, 0, 0);
				weapon.setChargedFishshotPower(0);
			}
		}
	}

	public boolean removeAutoShot(int itemId, boolean sendMessage, ShotType type)
	{
		if(_activeAutoShots.remove(itemId) != null)
		{
			if(!Config.EX_USE_AUTO_SOUL_SHOT)
				sendPacket(new ExAutoSoulShot(itemId, 0, type));
			if(sendMessage)
				sendPacket(new SystemMessagePacket(SystemMsg.THE_AUTOMATIC_USE_OF_S1_HAS_BEEN_DEACTIVATED).addItemName(itemId));
			return true;
		}
		return false;
	}

	public boolean manuallyRemoveAutoShot(int itemId, ShotType type, boolean save)
	{
		if(removeAutoShot(itemId, true, type))
		{
			if(Config.EX_USE_AUTO_SOUL_SHOT)
			{
				if(save)
					setVar(ACTIVE_SHOT_ID_VAR + "_" + type.ordinal(), -1);
				else
					unsetVar(ACTIVE_SHOT_ID_VAR + "_" + type.ordinal());
			}
			return true;
		}
		return false;
	}

	public void removeAutoShot(ShotType type)
	{
		if(!Config.EX_USE_AUTO_SOUL_SHOT)
			return;

		for(IntObjectPair<ShotType> entry : _activeAutoShots.entrySet())
		{
			if(entry.getValue() == type)
			{
				removeAutoShot(entry.getKey(), false, entry.getValue());
				sendPacket(new ExAutoSoulShot(entry.getKey(), 1, entry.getValue())); // TODO: ???? ?????????????? ???????????????? ?????? ?????????? ?????????????????????? ????????. ?????????? ?????????????? ??????????????.
			}
		}
	}

	public boolean isAutoShot(int itemId)
	{
		return _activeAutoShots.containsKey(itemId);
	}

	public boolean isChargedShot(ShotType type)
	{
		return _activeAutoShots.containsValue(type);
	}

	@Override
	public boolean isInvisible(GameObject observer)
	{
		if(observer != null)
		{
			if(getObjectId() == observer.getObjectId())
				return false;
			if(isMyServitor(observer.getObjectId()))
				return false;
			if(observer.isPlayer())
			{
				// TODO: ?????????????????? ???? ????????.
				Player observPlayer = (Player) observer;
				if(isInSameParty(observPlayer))
					return false;
				if(observPlayer.getPlayerAccess().CanSeeInHide)
					return false;
			}
		}
		return super.isInvisible(observer) || isGMInvisible();
	}

	@Override
	public boolean startInvisible(Object owner, boolean withServitors)
	{
		if(super.startInvisible(owner, withServitors))
		{
			sendUserInfo(true);
			return true;
		}
		return false;
	}

	@Override
	public boolean stopInvisible(Object owner, boolean withServitors)
	{
		if(super.stopInvisible(owner, withServitors))
		{
			sendUserInfo(true);
			return true;
		}
		return false;
	}

	public boolean isGMInvisible()
	{
		return getPlayerAccess().GodMode && _gmInvisible.get();
	}

	public boolean setGMInvisible(boolean value)
	{
		if(value)
			return _gmInvisible.getAndSet(true);
		return _gmInvisible.setAndGet(false);
	}

	@Override
	public boolean isUndying()
	{
		return super.isUndying() || isGMUndying();
	}

	public boolean isGMUndying()
	{
		return getPlayerAccess().GodMode && _gmUndying.get();
	}

	public boolean setGMUndying(boolean value)
	{
		if(value)
			return _gmUndying.getAndSet(true);
		return _gmUndying.setAndGet(false);
	}

	public int getClanPrivileges()
	{
		if(_clan == null)
			return 0;
		if(isClanLeader())
			return Clan.CP_ALL;
		if(_powerGrade < 1 || _powerGrade > 9)
			return 0;
		RankPrivs privs = _clan.getRankPrivs(_powerGrade);
		if(privs != null)
			return privs.getPrivs();
		return 0;
	}

	public void teleToClosestTown()
	{
		TeleportPoint teleportPoint = TeleportUtils.getRestartPoint(this, RestartType.VILLAGE);
		teleToLocation(teleportPoint.getLoc(), teleportPoint.getReflection());
	}

	public void teleToCastle()
	{
		TeleportPoint teleportPoint = TeleportUtils.getRestartPoint(this, RestartType.CASTLE);
		teleToLocation(teleportPoint.getLoc(), teleportPoint.getReflection());
	}

	public void teleToClanhall()
	{
		TeleportPoint teleportPoint = TeleportUtils.getRestartPoint(this, RestartType.AGIT);
		teleToLocation(teleportPoint.getLoc(), teleportPoint.getReflection());
	}

	@Override
	public void sendMessage(CustomMessage message)
	{
		sendPacket(message);
	}

	public void teleToLocation(ILocation loc, boolean replace)
	{
		_isInReplaceTeleport = replace;

		teleToLocation(loc);

		_isInReplaceTeleport = false;
	}

	@Override
	public boolean onTeleported()
	{
		if(!super.onTeleported())
			return false;

		if(isFakeDeath())
			breakFakeDeath();

		if(isInBoat())
			setLoc(getBoat().getLoc());

		if (!isInPvPEvent()) {
			// 15 ???????????? ?????????? ?????????????????? ???? ?????????????????? ???? ?????????????? ????????
			setNonAggroTime(System.currentTimeMillis() + Config.NONAGGRO_TIME_ONTELEPORT);
			setNonPvpTime(System.currentTimeMillis() + Config.NONPVP_TIME_ONTELEPORT);
		}

		spawnMe();

		if(isPendingRevive())
			doRevive();

		sendActionFailed();

		getAI().notifyEvent(CtrlEvent.EVT_TELEPORTED);

		if(isLockedTarget() && getTarget() != null)
			sendPacket(new MyTargetSelectedPacket(this, getTarget()));

		sendUserInfo(true);

		if(!_isInReplaceTeleport)
		{
			for(Servitor servitor : getServitors())
				servitor.teleportToOwner();
		}

		getListeners().onTeleported();

		for(ListenerHook hook : getListenerHooks(ListenerHookType.PLAYER_TELEPORT))
			hook.onPlayerTeleport(this, getReflectionId());

		for(ListenerHook hook : ListenerHook.getGlobalListenerHooks(ListenerHookType.PLAYER_TELEPORT))
			hook.onPlayerTeleport(this, getReflectionId());

		return true;
	}

	public boolean enterObserverMode(Location loc)
	{
		WorldRegion observerRegion = World.getRegion(loc);
		if(observerRegion == null)
			return false;

		if(!_observerMode.compareAndSet(OBSERVER_NONE, OBSERVER_STARTING))
			return false;

		setTarget(null);
		getMovement().stopMove();
		sitDown(null);
		setFlying(true);

		// ?????????????? ?????? ?????????????? ??????????????
		World.removeObjectsFromPlayer(this);

		_observePoint = new ObservePoint(this);
		_observePoint.setLoc(loc);
		_observePoint.getFlags().getImmobilized().start();

		// ???????????????????? ?????????????? ?????? ??????????????
		broadcastCharInfoImpl();

		// ?????????????????? ?? ?????????? ????????????????????
		sendPacket(new ObserverStartPacket(loc));
		return true;
	}

	public boolean enterArenaObserverMode(ObservableArena arena)
	{
		final Location enterPoint = arena.getObserverEnterPoint(this);

		final WorldRegion observerRegion = World.getRegion(enterPoint);
		if(observerRegion == null)
			return false;

		if(!_observerMode.compareAndSet(isInArenaObserverMode() ? OBSERVER_STARTED : OBSERVER_NONE, OBSERVER_STARTING))
			return false;

		sendPacket(new TeleportToLocationPacket(this, enterPoint));

		setTarget(null);
		getMovement().stopMove();

		// ?????????????? ?????? ?????????????? ??????????????
		World.removeObjectsFromPlayer(this);

		if(_observableArena != null)
		{
			_observableArena.removeObserver(_observePoint);
			_observableArena.onChangeObserverArena(this);

			_observePoint.decayMe();
		}
		else
		{
			// ???????????????????? ?????????????? ?????? ??????????????
			broadcastCharInfoImpl();

			arena.onEnterObserverArena(this);

			_observePoint = new ObservePoint(this);
		}

		_observePoint.setLoc(enterPoint);
		_observePoint.setReflection(arena.getReflection());

		_observableArena = arena;

		sendPacket(new ExTeleportToLocationActivate(this, enterPoint));
		return true;
	}

	public void appearObserverMode()
	{
		if(!_observerMode.compareAndSet(OBSERVER_STARTING, OBSERVER_STARTED))
			return;

		_observePoint.spawnMe();

		sendUserInfo(true);

		if(_observableArena != null)
		{
			_observableArena.addObserver(_observePoint);
			_observableArena.onAppearObserver(_observePoint);
		}
	}

	public void leaveObserverMode()
	{
		if(!_observerMode.compareAndSet(OBSERVER_STARTED, OBSERVER_LEAVING))
			return;

		ObservableArena arena = _observableArena;

		if(arena != null)
		{
			// "??????????????????????????????"
			sendPacket(new TeleportToLocationPacket(this, getLoc()));

			_observableArena.removeObserver(_observePoint);
			_observableArena = null;
		}

		_observePoint.deleteMe();
		_observePoint = null;

		setTarget(null);
		getMovement().stopMove();

		// ?????????????? ???? ???????????? ????????????????????
		if(arena != null)
		{
			arena.onExitObserverArena(this);
			sendPacket(new ExTeleportToLocationActivate(this, getLoc()));
		}
		else
			sendPacket(new ObserverEndPacket(getLoc()));
	}

	public void returnFromObserverMode()
	{
		if(!_observerMode.compareAndSet(OBSERVER_LEAVING, OBSERVER_NONE))
			return;

		standUp();
		setFlying(false);

		broadcastUserInfo(true);

		World.showObjectsToPlayer(this);
	}

	public void setOlympiadSide(final int i)
	{
		_olympiadSide = i;
	}

	public int getOlympiadSide()
	{
		return _olympiadSide;
	}

	public boolean isInObserverMode()
	{
		return getObserverMode() > 0;
	}

	public boolean isInArenaObserverMode()
	{
		return _observableArena != null;
	}

	public ObservableArena getObservableArena()
	{
		return _observableArena;
	}

	public int getObserverMode()
	{
		return _observerMode.get();
	}

	public ObservePoint getObservePoint()
	{
		return _observePoint;
	}

	public int getTeleMode()
	{
		return _telemode;
	}

	public void setTeleMode(final int mode)
	{
		_telemode = mode;
	}

	public void setLoto(final int i, final int val)
	{
		_loto[i] = val;
	}

	public int getLoto(final int i)
	{
		return _loto[i];
	}

	public void setRace(final int i, final int val)
	{
		_race[i] = val;
	}

	public int getRace(final int i)
	{
		return _race[i];
	}

	public boolean getMessageRefusal()
	{
		return _messageRefusal;
	}

	public void setMessageRefusal(final boolean mode)
	{
		_messageRefusal = mode;
	}

	public void setTradeRefusal(final boolean mode)
	{
		_tradeRefusal = mode;
	}

	public boolean getTradeRefusal()
	{
		return _tradeRefusal;
	}

	public boolean isBlockAll()
	{
		return _blockAll;
	}

	public void setBlockAll(final boolean state)
	{
		_blockAll = state;
	}

	public void setHero(final boolean hero)
	{
		_hero = hero;
	}

	@Override
	public boolean isHero()
	{
		return _hero;
	}

	public void setIsInOlympiadMode(final boolean b)
	{
		_inOlympiadMode = b;
	}

	public boolean isInOlympiadMode()
	{
		return _inOlympiadMode;
	}

	public boolean isOlympiadGameStart()
	{
		return _olympiadGame != null && _olympiadGame.getState() == 1;
	}

	public boolean isOlympiadCompStart()
	{
		return _olympiadGame != null && _olympiadGame.getState() == 2;
	}

	public int getSubLevel()
	{
		return isBaseClassActive() ? 0 : getLevel();
	}

	/* varka silenos and ketra orc quests related functions */
	public void updateKetraVarka()
	{
		if(ItemFunctions.getItemCount(this, 7215) > 0)
			_ketra = 5;
		else if(ItemFunctions.getItemCount(this, 7214) > 0)
			_ketra = 4;
		else if(ItemFunctions.getItemCount(this, 7213) > 0)
			_ketra = 3;
		else if(ItemFunctions.getItemCount(this, 7212) > 0)
			_ketra = 2;
		else if(ItemFunctions.getItemCount(this, 7211) > 0)
			_ketra = 1;
		else if(ItemFunctions.getItemCount(this, 7225) > 0)
			_varka = 5;
		else if(ItemFunctions.getItemCount(this, 7224) > 0)
			_varka = 4;
		else if(ItemFunctions.getItemCount(this, 7223) > 0)
			_varka = 3;
		else if(ItemFunctions.getItemCount(this, 7222) > 0)
			_varka = 2;
		else if(ItemFunctions.getItemCount(this, 7221) > 0)
			_varka = 1;
		else
		{
			_varka = 0;
			_ketra = 0;
		}
	}

	public int getVarka()
	{
		return _varka;
	}

	public int getKetra()
	{
		return _ketra;
	}

	public void updateRam()
	{
		if(ItemFunctions.getItemCount(this, 7247) > 0)
			_ram = 2;
		else if(ItemFunctions.getItemCount(this, 7246) > 0)
			_ram = 1;
		else
			_ram = 0;
	}

	public int getRam()
	{
		return _ram;
	}

	public void setPledgeType(final int typeId)
	{
		_pledgeType = typeId;
	}

	public int getPledgeType()
	{
		return _pledgeType;
	}

	public void setLvlJoinedAcademy(int lvl)
	{
		_lvlJoinedAcademy = lvl;
	}

	public int getLvlJoinedAcademy()
	{
		return _lvlJoinedAcademy;
	}

	public PledgeRank getPledgeRank()
	{
		return _pledgeRank;
	}

	public void updatePledgeRank()
	{
		if(isGM()) // ?????? ?????? ?????? ?????????? ???????????????????????? ???????? Lineage 2 ;)
		{
			_pledgeRank = PledgeRank.EMPEROR;
			return;
		}

		int CLAN_LEVEL = _clan == null ? -1 : _clan.getLevel();
		boolean IN_ACADEMY = _clan != null && Clan.isAcademy(_pledgeType);
		boolean IS_GUARD = _clan != null && Clan.isRoyalGuard(_pledgeType);
		boolean IS_KNIGHT = _clan != null && Clan.isOrderOfKnights(_pledgeType);

		boolean IS_GUARD_CAPTAIN = false, IS_KNIGHT_COMMANDER = false, IS_LEADER = false;

		SubUnit unit = getSubUnit();
		if(unit != null)
		{
			UnitMember unitMember = unit.getUnitMember(getObjectId());
			if(unitMember == null)
			{
				_log.atWarning().log( "Player: unitMember null, clan: %s; pledgeType: %s", _clan.getClanId(), unit.getType() );
				return;
			}
			IS_GUARD_CAPTAIN = Clan.isRoyalGuard(unitMember.isLeaderOf());
			IS_KNIGHT_COMMANDER = Clan.isOrderOfKnights(unitMember.isLeaderOf());
			IS_LEADER = unitMember.isLeaderOf() == Clan.SUBUNIT_MAIN_CLAN;
		}

		switch(CLAN_LEVEL)
		{
			case -1:
				_pledgeRank = PledgeRank.VAGABOND;
				break;
			case 0:
			case 1:
			case 2:
			case 3:
				_pledgeRank = PledgeRank.VASSAL;
				break;
			case 4:
				if(IS_LEADER)
					_pledgeRank = PledgeRank.KNIGHT;
				else
					_pledgeRank = PledgeRank.VASSAL;
				break;
			case 5:
				if(IS_LEADER)
					_pledgeRank = PledgeRank.WISEMAN;
				else if(IN_ACADEMY)
					_pledgeRank = PledgeRank.VASSAL;
				else
					_pledgeRank = PledgeRank.HEIR;
				break;
			case 6:
				if(IS_LEADER)
					_pledgeRank = PledgeRank.BARON;
				else if(IN_ACADEMY)
					_pledgeRank = PledgeRank.VASSAL;
				else if(IS_GUARD_CAPTAIN)
					_pledgeRank = PledgeRank.WISEMAN;
				else if(IS_GUARD)
					_pledgeRank = PledgeRank.HEIR;
				else
					_pledgeRank = PledgeRank.KNIGHT;
				break;
			case 7:
				if(IS_LEADER)
					_pledgeRank = PledgeRank.COUNT;
				else if(IN_ACADEMY)
					_pledgeRank = PledgeRank.VASSAL;
				else if(IS_GUARD_CAPTAIN)
					_pledgeRank = PledgeRank.VISCOUNT;
				else if(IS_GUARD)
					_pledgeRank = PledgeRank.KNIGHT;
				else if(IS_KNIGHT_COMMANDER)
					_pledgeRank = PledgeRank.BARON;
				else if(IS_KNIGHT)
					_pledgeRank = PledgeRank.HEIR;
				else
					_pledgeRank = PledgeRank.WISEMAN;
				break;
			case 8:
				if(IS_LEADER)
					_pledgeRank = PledgeRank.MARQUIS;
				else if(IN_ACADEMY)
					_pledgeRank = PledgeRank.VASSAL;
				else if(IS_GUARD_CAPTAIN)
					_pledgeRank = PledgeRank.COUNT;
				else if(IS_GUARD)
					_pledgeRank = PledgeRank.WISEMAN;
				else if(IS_KNIGHT_COMMANDER)
					_pledgeRank = PledgeRank.VISCOUNT;
				else if(IS_KNIGHT)
					_pledgeRank = PledgeRank.KNIGHT;
				else
					_pledgeRank = PledgeRank.BARON;
				break;
			case 9:
				if(IS_LEADER)
					_pledgeRank = PledgeRank.DUKE;
				else if(IN_ACADEMY)
					_pledgeRank = PledgeRank.VASSAL;
				else if(IS_GUARD_CAPTAIN)
					_pledgeRank = PledgeRank.MARQUIS;
				else if(IS_GUARD)
					_pledgeRank = PledgeRank.BARON;
				else if(IS_KNIGHT_COMMANDER)
					_pledgeRank = PledgeRank.COUNT;
				else if(IS_KNIGHT)
					_pledgeRank = PledgeRank.WISEMAN;
				else
					_pledgeRank = PledgeRank.VISCOUNT;
				break;
			case 10:
				if(IS_LEADER)
					_pledgeRank = PledgeRank.GRAND_DUKE;
				else if(IN_ACADEMY)
					_pledgeRank = PledgeRank.VASSAL;
				else if(IS_GUARD)
					_pledgeRank = PledgeRank.VISCOUNT;
				else if(IS_KNIGHT)
					_pledgeRank = PledgeRank.BARON;
				else if(IS_GUARD_CAPTAIN)
					_pledgeRank = PledgeRank.DUKE;
				else if(IS_KNIGHT_COMMANDER)
					_pledgeRank = PledgeRank.MARQUIS;
				else
					_pledgeRank = PledgeRank.COUNT;
				break;
			case 11:
				if(IS_LEADER)
					_pledgeRank = PledgeRank.DISTINGUISHED_KING;
				else if(IN_ACADEMY)
					_pledgeRank = PledgeRank.VASSAL;
				else if(IS_GUARD)
					_pledgeRank = PledgeRank.COUNT;
				else if(IS_KNIGHT)
					_pledgeRank = PledgeRank.VISCOUNT;
				else if(IS_GUARD_CAPTAIN)
					_pledgeRank = PledgeRank.GRAND_DUKE;
				else if(IS_KNIGHT_COMMANDER)
					_pledgeRank = PledgeRank.DUKE;
				else
					_pledgeRank = PledgeRank.MARQUIS;
				break;
		}

		if(isHero() && _pledgeRank.ordinal() < PledgeRank.MARQUIS.ordinal())
			_pledgeRank = PledgeRank.MARQUIS;
	}

	public void setPowerGrade(final int grade)
	{
		_powerGrade = grade;
	}

	public int getPowerGrade()
	{
		return _powerGrade;
	}

	public void setApprentice(final int apprentice)
	{
		_apprentice = apprentice;
	}

	public int getApprentice()
	{
		return _apprentice;
	}

	public int getSponsor()
	{
		return _clan == null ? 0 : _clan.getAnyMember(getObjectId()).getSponsor();
	}

	@Override
	public int getNameColor()
	{
		if(isInObserverMode())
			return Color.black.getRGB();

		return _nameColor;
	}

	public void setNameColor(final int nameColor)
	{
		if(nameColor != Config.NORMAL_NAME_COLOUR && nameColor != Config.CLANLEADER_NAME_COLOUR && nameColor != Config.GM_NAME_COLOUR && nameColor != Config.SERVICES_OFFLINE_TRADE_NAME_COLOR)
			setVar("namecolor", Integer.toHexString(nameColor));
		else if(nameColor == Config.NORMAL_NAME_COLOUR)
			unsetVar("namecolor");
		_nameColor = nameColor;
	}

	public void setNameColor(final int red, final int green, final int blue)
	{
		_nameColor = (red & 0xFF) + ((green & 0xFF) << 8) + ((blue & 0xFF) << 16);
		if(_nameColor != Config.NORMAL_NAME_COLOUR && _nameColor != Config.CLANLEADER_NAME_COLOUR && _nameColor != Config.GM_NAME_COLOUR && _nameColor != Config.SERVICES_OFFLINE_TRADE_NAME_COLOR)
			setVar("namecolor", Integer.toHexString(_nameColor));
		else
			unsetVar("namecolor");
	}

	private void restoreVariables()
	{
		List<CharacterVariable> variables = CharacterVariablesDAO.getInstance().restore(getObjectId());
		for(CharacterVariable var : variables)
			_variables.put(var.getName(), var);
	}

	public Collection<CharacterVariable> getVariables()
	{
		return _variables.values();
	}

	public boolean setVar(String name, Object value)
	{
		return setVar(name, value, -1);
	}

	public boolean setVar(String name, Object value, long expirationTime)
	{
		CharacterVariable var = new CharacterVariable(name, String.valueOf(value), expirationTime);
		if(CharacterVariablesDAO.getInstance().insert(getObjectId(), var))
		{
			_variables.put(name, var);
			return true;
		}
		return false;
	}

	public boolean unsetVar(String name)
	{
		if(name == null || name.isEmpty())
			return false;

		if(_variables.containsKey(name) && CharacterVariablesDAO.getInstance().delete(getObjectId(), name))
			return _variables.remove(name) != null;

		return false;
	}

	public String getVar(String name)
	{
		return getVar(name, null);
	}

	public String getVar(String name, String defaultValue)
	{
		CharacterVariable var = _variables.get(name);
		if(var != null && !var.isExpired())
			return var.getValue();

		return defaultValue;
	}

	public long getVarExpireTime(String name)
	{
		CharacterVariable var = _variables.get(name);
		if(var != null)
			return var.getExpireTime();
		return 0;
	}

	public int getVarInt(String name)
	{
		return getVarInt(name, 0);
	}

	public int getVarInt(String name, int defaultValue)
	{
		String var = getVar(name);
		if(var != null)
			return Integer.parseInt(var);

		return defaultValue;
	}

	public long getVarLong(String name)
	{
		return getVarLong(name, 0L);
	}

	public long getVarLong(String name, long defaultValue)
	{
		String var = getVar(name);
		if(var != null)
			return Long.parseLong(var);

		return defaultValue;
	}

	public double getVarDouble(String name)
	{
		return getVarDouble(name, 0.);
	}

	public double getVarDouble(String name, double defaultValue)
	{
		String var = getVar(name);
		if(var != null)
			return Double.parseDouble(var);

		return defaultValue;
	}

	public boolean getVarBoolean(String name)
	{
		return getVarBoolean(name, false);
	}

	public boolean getVarBoolean(String name, boolean defaultValue)
	{
		String var = getVar(name);
		if(var != null)
			return !(var.equals("0") || var.equalsIgnoreCase("false"));

		return defaultValue;
	}

	public void setLanguage(String val)
	{
		_language = Language.getLanguage(val);
		setVar(Language.LANG_VAR, _language.getShortName(), -1);
	}

	public Language getLanguage()
	{
		if (Config.USE_CLIENT_LANG) {
			final GameClient netConnection = getNetConnection();
			if (netConnection != null) {
				return netConnection.getLanguage();
			}
		}
		if(Config.CAN_SELECT_LANGUAGE)
			return _language;
		return Config.DEFAULT_LANG;
	}

	public int getLocationId()
	{
		if(getNetConnection() != null)
			return getNetConnection().getLanguage().getId();
		return -1;
	}

	public boolean isLangRus()
	{
		return getLanguage() == Language.RUSSIAN || getLanguage().getSecondLanguage() == Language.RUSSIAN;
	}

	public void stopWaterTask()
	{
		if(_taskWater != null)
		{
			_taskWater.cancel(false);
			_taskWater = null;
			sendPacket(new SetupGaugePacket(this, SetupGaugePacket.Colors.BLUE, 0));
			sendChanges();
		}
	}

	public void startWaterTask()
	{
		if(isDead())
			stopWaterTask();
		else if(Config.ALLOW_WATER && _taskWater == null)
		{
			int timeinwater = (int) (getStat().getValue(DoubleStat.BREATH) * 1000L);
			sendPacket(new SetupGaugePacket(this, SetupGaugePacket.Colors.BLUE, timeinwater));
			if(isTransformed() && !getTransform().isCanSwim())
				setTransform(null);

			_taskWater = ThreadPoolManager.getInstance().scheduleAtFixedRate(new WaterTask(this), timeinwater, 1000L);
			sendChanges();
		}
	}

	public void doRevive(double percent)
	{
		restoreExp(percent);
		doRevive();
	}

	@Override
	public void doRevive()
	{
		super.doRevive();
		unsetVar("lostexp");
		updateAbnormalIcons();
		autoShot();
		if(isMounted())
			_mount.onRevive();

		ItemInstance agathionItem = getInventory().getPaperdollItem(Inventory.PAPERDOLL_AGATHION_MAIN);
		if(agathionItem != null)
		{
			AgathionTemplate agathionTemplate = agathionItem.getTemplate().getAgathionTemplate();
			if(agathionTemplate != null)
			{
				Agathion agathion = new Agathion(this, agathionTemplate, null);
				agathion.init();
			}
		}
	}

	public void reviveRequest(Player reviver, double percent, boolean pet)
	{
		if (isResurrectionBlocked()) {
			return;
		}

		ReviveAnswerListener reviveAsk = _askDialog != null && _askDialog.getValue() instanceof ReviveAnswerListener ? (ReviveAnswerListener) _askDialog.getValue() : null;
		if(reviveAsk != null)
		{
			if(reviveAsk.isForPet() == pet && reviveAsk.getPower() >= percent)
			{
				reviver.sendPacket(SystemMsg.RESURRECTION_HAS_ALREADY_BEEN_PROPOSED);
				return;
			}
			if(pet && !reviveAsk.isForPet())
			{
				reviver.sendPacket(SystemMsg.A_PET_CANNOT_BE_RESURRECTED_WHILE_ITS_OWNER_IS_IN_THE_PROCESS_OF_RESURRECTING);
				return;
			}
			if(pet && isDead())
			{
				reviver.sendPacket(SystemMsg.WHILE_A_PET_IS_BEING_RESURRECTED_IT_CANNOT_HELP_IN_RESURRECTING_ITS_MASTER);
				return;
			}
		}

		if(pet && getPet() != null && getPet().isDead() || !pet && isDead())
		{

			ConfirmDlgPacket pkt = new ConfirmDlgPacket(SystemMsg.C1_IS_MAKING_AN_ATTEMPT_TO_RESURRECT_YOU_IF_YOU_CHOOSE_THIS_PATH_S2_EXPERIENCE_WILL_BE_RETURNED_FOR_YOU, 0);
			pkt.addName(reviver).addInteger(Math.round(percent));

			ask(pkt, new ReviveAnswerListener(this, percent, pet));
		}
	}

	public void requestCheckBot()
	{
		BotCheckQuestion question = BotCheckManager.generateRandomQuestion();
		int qId = question.getId();
		String qDescr = question.getDescr(isLangRus());

		ConfirmDlgPacket pkt = new ConfirmDlgPacket(SystemMsg.S1, 60000).addString(qDescr);
		//ConfirmDlgPacket pkt = new ConfirmDlgPacket(qDescr, 60000);
		ask(pkt, new BotCheckAnswerListner(this, qId));
	}

	public void increaseBotRating()
	{
		int bot_points = getBotRating();
		if(bot_points + 1 >= Config.MAX_BOT_POINTS)
			return;
		setBotRating(bot_points + 1);
	}

	public void decreaseBotRating()
	{
		int bot_points = getBotRating();
		if(bot_points - 1 <= Config.MINIMAL_BOT_RATING_TO_BAN)
		{
			if(toJail(Config.AUTO_BOT_BAN_JAIL_TIME))
			{
				sendMessage("You moved to jail, time to escape - " + Config.AUTO_BOT_BAN_JAIL_TIME + " minutes, reason - botting .");
				if(Config.ANNOUNCE_AUTO_BOT_BAN)
					Announcements.announceToAll("Player " + getName() + " jailed for botting!");
			}
		}
		else
		{
			setBotRating(bot_points - 1);
			if(Config.ON_WRONG_QUESTION_KICK)
				kick();
		}
	}

	public void setBotRating(int rating)
	{
		_botRating = rating;
	}

	public int getBotRating()
	{
		return _botRating;
	}

	public boolean isInJail()
	{
		return _isInJail;
	}

	public void setIsInJail(boolean value)
	{
		_isInJail = value;
	}

	public boolean toJail(int time)
	{
		if(isInJail())
			return false;

		setIsInJail(true);
		setVar(JAILED_VAR, true, System.currentTimeMillis() + (time * 60000));
		startUnjailTask(this, time);

		if(getReflection().isMain())
			setVar("backCoords", getLoc().toXYZString(), -1);

		if(isInStoreMode())
		{
			setPrivateStoreType(STORE_PRIVATE_NONE);
			storePrivateStore();
		}

		teleToLocation(Location.findPointToStay(this, AdminFunctions.JAIL_SPAWN, 50, 200), ReflectionManager.JAIL);
		return true;
	}

	public boolean fromJail()
	{
		if(!isInJail())
			return false;

		setIsInJail(false);
		unsetVar(JAILED_VAR);
		stopUnjailTask();

		String back = getVar("backCoords");
		if(back != null)
		{
			teleToLocation(Location.parseLoc(back), ReflectionManager.MAIN);
			unsetVar("backCoords");
		}
		return true;
	}

	public void summonCharacterRequest(final Creature summoner, final Location loc, final int summonConsumeCrystal)
	{
		ConfirmDlgPacket cd = new ConfirmDlgPacket(SystemMsg.C1_WISHES_TO_SUMMON_YOU_FROM_S2, 60000);
		cd.addName(summoner).addZoneName(loc);

		ask(cd, new SummonAnswerListener(this, loc, summonConsumeCrystal));
	}

	public void updateNoChannel(final long time)
	{
		setNoChannel(time);

		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();

			final String stmt = "UPDATE characters SET nochannel = ? WHERE obj_Id=?";
			statement = con.prepareStatement(stmt);
			statement.setLong(1, _NoChannel > 0 ? _NoChannel / 1000 : _NoChannel);
			statement.setInt(2, getObjectId());
			statement.executeUpdate();
		}
		catch(final Exception e)
		{
			_log.atWarning().log( "Could not activate nochannel:%s", e );
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}

		sendPacket(new EtcStatusUpdatePacket(this));
	}

	public boolean canTalkWith(Player player)
	{
		return _NoChannel >= 0 || player == this;
	}

	private static final SchedulingPattern DAILY_TIME_PATTERN = new SchedulingPattern("30 6 * * *");

	private void checkDailyCounters()
	{
		int daysPassed = 0;
		long lastAccessTime = _lastAccess * 1000L;
		while(lastAccessTime < System.currentTimeMillis()) {
			long nextDayTime = DAILY_TIME_PATTERN.next(lastAccessTime);
			if(nextDayTime < System.currentTimeMillis()) {
				daysPassed++;
			}
			lastAccessTime = nextDayTime;
		}

		if(daysPassed > 0) {
			restartDailyCounters(true);
		}
	}

	public void restartDailyCounters(boolean onRestore)
	{
		// World chat
		if(Config.ALLOW_WORLD_CHAT)
		{
			setUsedWorldChatPoints(0);
			if(!onRestore)
				sendPacket(new ExWorldChatCnt(this));
		}
	}

	private static final SchedulingPattern WEEKLY_TIME_PATTERN = new SchedulingPattern("30 6 * * 3");

	private void checkWeeklyCounters()
	{
		int weeksPassed = 0;
		long lastAccessTime = _lastAccess * 1000L;
		while(lastAccessTime < System.currentTimeMillis()) {
			long nextDayTime = WEEKLY_TIME_PATTERN.next(lastAccessTime);
			if(nextDayTime < System.currentTimeMillis()) {
				weeksPassed++;
			}
			lastAccessTime = nextDayTime;
		}

		if(weeksPassed > 0) {
			restartWeeklyCounters(true);
		}
	}

	public void restartWeeklyCounters(boolean onRestore)
	{
		//
	}

	public SubClassList getSubClassList()
	{
		return _subClassList;
	}

	public SubClass getBaseSubClass()
	{
		return _subClassList.getBaseSubClass();
	}

	public int getBaseClassId()
	{
		if(getBaseSubClass() != null)
			return getBaseSubClass().getClassId();

		return -1;
	}

	public SubClass getActiveSubClass()
	{
		return _subClassList.getActiveSubClass();
	}

	public int getActiveClassId()
	{
		return getActiveSubClass().getClassId();
	}

	public boolean isBaseClassActive()
	{
		return getActiveSubClass().isBase();
	}

	public ClassId getClassId()
	{
		return ClassId.valueOf(getActiveClassId());
	}

	public int getMaxLevel()
	{
		if(getActiveSubClass() != null)
			return getActiveSubClass().getMaxLevel();

		return Experience.getMaxLevel();
	}

	/**
	 * Changing index of class in DB, used for changing class when finished professional quests
	 *
	 * @param oldclass
	 * @param newclass
	 */
	private synchronized void changeClassInDb(final int oldclass, final int newclass)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE character_subclasses SET class_id=? WHERE char_obj_id=? AND class_id=?");
			statement.setInt(1, newclass);
			statement.setInt(2, getObjectId());
			statement.setInt(3, oldclass);
			statement.executeUpdate();
			DbUtils.close(statement);

			statement = con.prepareStatement("DELETE FROM character_hennas WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newclass);
			statement.executeUpdate();
			DbUtils.close(statement);

			statement = con.prepareStatement("UPDATE character_hennas SET class_index=? WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, newclass);
			statement.setInt(2, getObjectId());
			statement.setInt(3, oldclass);
			statement.executeUpdate();
			DbUtils.close(statement);

			statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE object_id=? AND class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newclass);
			statement.executeUpdate();
			DbUtils.close(statement);

			statement = con.prepareStatement("UPDATE character_shortcuts SET class_index=? WHERE object_id=? AND class_index=?");
			statement.setInt(1, newclass);
			statement.setInt(2, getObjectId());
			statement.setInt(3, oldclass);
			statement.executeUpdate();
			DbUtils.close(statement);

			statement = con.prepareStatement("DELETE FROM character_skills WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newclass);
			statement.executeUpdate();
			DbUtils.close(statement);

			statement = con.prepareStatement("UPDATE character_skills SET class_index=? WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, newclass);
			statement.setInt(2, getObjectId());
			statement.setInt(3, oldclass);
			statement.executeUpdate();
			DbUtils.close(statement);

			statement = con.prepareStatement("DELETE FROM character_effects_save WHERE object_id=? AND id=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newclass);
			statement.executeUpdate();
			DbUtils.close(statement);

			statement = con.prepareStatement("UPDATE character_effects_save SET id=? WHERE object_id=? AND id=?");
			statement.setInt(1, newclass);
			statement.setInt(2, getObjectId());
			statement.setInt(3, oldclass);
			statement.executeUpdate();
			DbUtils.close(statement);

			statement = con.prepareStatement("DELETE FROM character_skills_save WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newclass);
			statement.executeUpdate();
			DbUtils.close(statement);

			statement = con.prepareStatement("UPDATE character_skills_save SET class_index=? WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, newclass);
			statement.setInt(2, getObjectId());
			statement.setInt(3, oldclass);
			statement.executeUpdate();
			DbUtils.close(statement);
		}
		catch(final SQLException e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "" );
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	/**
	 * ?????????????????? ???????????????????? ?? ?????????????? ?? ????
	 */
	public void storeCharSubClasses()
	{
		SubClass main = getActiveSubClass();
		if(main != null)
		{
			main.setCp(getCurrentCp());
			main.setHp(getCurrentHp());
			main.setMp(getCurrentMp());
		}
		else
			_log.atWarning().log( "Could not store char sub data, main class %s not found for %s", getActiveClassId(), this );

		CharacterSubclassDAO.getInstance().store(this);
	}

	/**
	 * ???????????????? ??????????, ???????????????????????? ???????????? ?????? ????????????????????
	 *
	 * @param storeOld
	 */
	public boolean addSubClass(final int classId, boolean storeOld, long exp, long sp)
	{
		return addSubClass(classId, storeOld, SubClassType.SUBCLASS, exp, sp);
	}

	public boolean addSubClass(final int classId, boolean storeOld, SubClassType type, long exp, long sp)
	{
		return addSubClass(-1, classId, storeOld, type, exp, sp);
	}

	private boolean addSubClass(final int oldClassId, final int classId, boolean storeOld, SubClassType type, long exp, long sp)
	{
		final ClassId newId = ClassId.valueOf(classId);
		if(newId.isDummy() || newId.isOfLevel(ClassLevel.NONE) || newId.isOfLevel(ClassLevel.FIRST))
			return false;

		final SubClass newClass = new SubClass(this);
		newClass.setType(type);
		newClass.setClassId(classId);
		if(exp > 0L)
			newClass.setExp(exp, true);
		if(sp > 0)
			newClass.setSp(sp);
		if(!getSubClassList().add(newClass))
			return false;

		final int level = newClass.getLevel();
		final double hp = newId.getBaseHp(level);
		final double mp = newId.getBaseMp(level);
		final double cp = newId.getBaseCp(level);
		if(!CharacterSubclassDAO.getInstance().insert(getObjectId(), newClass.getClassId(), newClass.getExp(), newClass.getSp(), hp, mp, cp, hp, mp, cp, level, false, type, ElementalElement.NONE))
			return false;

		setActiveSubClass(classId, storeOld, false);

		rewardSkills(true, false, true, false);

		sendSkillList();

		sendSkillList();
		setCurrentHpMp(getMaxHp(), getMaxMp(), true);
		setCurrentCp(getMaxCp());

        final ClassId oldId = oldClassId >= 0 ? ClassId.valueOf(oldClassId) : null;
		onReceiveNewClassId(oldId, newId);

		return true;
	}

	/**
	 * ?????????????? ?????? ???????????????????? ?? ???????????? ?? ?????????????????? ??????????, ???????????? ?????? ????????????????????
	 */
	public boolean modifySubClass(final int oldClassId, final int newClassId, final boolean safeExpSp)
	{
		final SubClass originalClass = getSubClassList().getByClassId(oldClassId);
		if(originalClass == null || originalClass.isBase())
			return false;

		final SubClassType type = originalClass.getType();
		long exp = 0L;
		long sp = 0;
		if(safeExpSp)
		{
			exp = originalClass.getExp();
			sp = originalClass.getSp();
		}

		TrainingCamp trainingCamp = TrainingCampManager.getInstance().getTrainingCamp(this);
		if(trainingCamp != null && trainingCamp.getClassIndex() == originalClass.getIndex())
			TrainingCampManager.getInstance().removeTrainingCamp(this);

		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			// Remove all basic info stored about this sub-class.
			statement = con.prepareStatement("DELETE FROM character_subclasses WHERE char_obj_id=? AND class_id=? AND type != " + SubClassType.BASE_CLASS.ordinal());
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DbUtils.close(statement);

			// Remove all skill info stored for this sub-class.
			statement = con.prepareStatement("DELETE FROM character_skills WHERE char_obj_id=? AND class_index=? ");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DbUtils.close(statement);

			// Remove all saved skills info stored for this sub-class.
			statement = con.prepareStatement("DELETE FROM character_skills_save WHERE char_obj_id=? AND class_index=? ");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DbUtils.close(statement);

			// Remove all saved effects stored for this sub-class.
			statement = con.prepareStatement("DELETE FROM character_effects_save WHERE object_id=? AND id=? ");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DbUtils.close(statement);

			// Remove all henna info stored for this sub-class.
			statement = con.prepareStatement("DELETE FROM character_hennas WHERE char_obj_id=? AND class_index=? ");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DbUtils.close(statement);

			// Remove all shortcuts info stored for this sub-class.
			statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE object_id=? AND class_index=? ");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DbUtils.close(statement);
		}
		catch(final Exception e)
		{
			_log.atWarning().log( "Could not delete char sub-class: %s", e );
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "" );
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		getSubClassList().removeByClassId(oldClassId);

		return newClassId < 0 || addSubClass(oldClassId, newClassId, false, type, exp, sp);
	}

	public boolean setActiveSubClass(final int subId, final boolean store, final boolean onRestore)
	{
		abortAttack(true, false);
		abortCast(true, false);

		if(!onRestore)
		{
			SubClass oldActiveSub = getActiveSubClass();
			if(oldActiveSub != null)
			{
				storeDisableSkills();
				if(store)
				{
					oldActiveSub.setCp(getCurrentCp());
					oldActiveSub.setHp(getCurrentHp());
					oldActiveSub.setMp(getCurrentMp());
				}
			}
		}

		SubClass newActiveSub = _subClassList.changeActiveSubClass(subId);
		if(newActiveSub == null)
			return false;

		setClassId(subId, false);

		removeAllSkills();

		getAbnormalList().stopAll();
		deleteCubics();

		for(Servitor servitor : getServitors())
		{
			if(servitor != null && servitor.isSummon())
				servitor.unSummon(false);
		}

		//deleteAgathion(); TODO: ?????????????????? ???? ????????.

		restoreSkills();
		rewardSkills(false);

		checkSkills();

		refreshExpertisePenalty();

		getInventory().refreshEquip();
		getInventory().validateItems();

		getHennaList().restore();
		getDailyMissionList().restore();
		getElementalList().restore();

		EffectsDAO.getInstance().restoreEffects(this);
		restoreDisableSkills();

		setCurrentHpMp(newActiveSub.getHp(), newActiveSub.getMp());
		setCurrentCp(newActiveSub.getCp());

		_shortCuts.restore();
		sendPacket(new ShortCutInitPacket(this));
		sendActiveAutoShots();

		broadcastPacket(new SocialActionPacket(getObjectId(), SocialActionPacket.LEVEL_UP));

		// clear charges
		_charges.set(0);
		stopChargeTask();

		startHourlyTask();

		sendSkillList();

		broadcastCharInfo();
		updateAbnormalIcons();
		//updateStats();
		return true;
	}

	/**
	 * ?????????? delay ?????????????????????? ???????????????? ???????????? ???? ????????
	 */
	public void startKickTask(long delayMillis)
	{
		stopKickTask();
		_kickTask = ThreadPoolManager.getInstance().schedule(new KickTask(this), delayMillis);
	}

	public void stopKickTask()
	{
		if(_kickTask != null)
		{
			_kickTask.cancel(false);
			_kickTask = null;
		}
	}

	public boolean givePremiumAccount(PremiumAccountTemplate premiumAccount, int delay)
	{
		if(getNetConnection() == null)
			return false;

		int type = premiumAccount.getType();
		if(type == 0)
			return false;

		int expireTime = (delay > 0) ? (int) ((delay * 60 * 60) + (System.currentTimeMillis() / 1000)) : Integer.MAX_VALUE;

		boolean extended = false;

		int oldAccountType = getNetConnection().getPremiumAccountType();
		int oldAccountExpire = getNetConnection().getPremiumAccountExpire();
		if(oldAccountType == type && oldAccountExpire > (System.currentTimeMillis() / 1000))
		{
			expireTime += (int) (oldAccountExpire - (System.currentTimeMillis() / 1000));
			extended = true;
		}

		if(Config.PREMIUM_ACCOUNT_BASED_ON_GAMESERVER)
			PremiumAccountDAO.getInstance().insert(getAccountName(), type, expireTime);
		else
		{
			if(GameServer.getInstance().getAuthServerCommunication().isShutdown())
				return false;

			GameServer.getInstance().getAuthServerCommunication().sendPacket(new BonusRequest(getAccountName(), type, expireTime));
		}

		getNetConnection().setPremiumAccountType(type);
		getNetConnection().setPremiumAccountExpire(expireTime);

		if(startPremiumAccountTask())
		{
			if(!extended)
			{
				if(getParty() != null)
					getParty().recalculatePartyData();

				getAttendanceRewards().onReceivePremiumAccount();

				sendPacket(new ExBR_PremiumStatePacket(this, hasPremiumAccount()));
			}
			return true;
		}
		return false;
	}

	public boolean removePremiumAccount()
	{
		PremiumAccountTemplate oldPremiumAccount = getPremiumAccount();
		if(oldPremiumAccount.getType() == 0)
			return false;

		oldPremiumAccount.onRemove(this);

		_premiumAccount = PremiumAccountHolder.getInstance().getPremiumAccount(0);

		if(getParty() != null)
			getParty().recalculatePartyData();

		if(Config.PREMIUM_ACCOUNT_BASED_ON_GAMESERVER)
			PremiumAccountDAO.getInstance().delete(getAccountName());
		else
			GameServer.getInstance().getAuthServerCommunication().sendPacket(new BonusRequest(getAccountName(), 0, 0));

		if(getNetConnection() != null)
		{
			getNetConnection().setPremiumAccountType(0);
			getNetConnection().setPremiumAccountExpire(0);
		}

		stopPremiumAccountTask();

		removePremiumAccountItems(true);

		sendPacket(new ExBR_PremiumStatePacket(this, hasPremiumAccount()));

		getAttendanceRewards().onRemovePremiumAccount();

		return true;
	}

	private boolean tryGiveFreePremiumAccount()
	{
		if(Config.FREE_PA_TYPE == 0 || Config.FREE_PA_DELAY <= 0)
			return false;

		PremiumAccountTemplate premiumAccount = PremiumAccountHolder.getInstance().getPremiumAccount(Config.FREE_PA_TYPE);
		if(premiumAccount == null)
			return false;

		boolean recieved = Boolean.parseBoolean(AccountVariablesDAO.getInstance().select(getAccountName(), FREE_PA_RECIEVED, "false"));
		if(recieved)
			return false;

		if(givePremiumAccount(premiumAccount, Config.FREE_PA_DELAY))
		{
			AccountVariablesDAO.getInstance().insert(getAccountName(), FREE_PA_RECIEVED, "true");

			if(Config.ENABLE_FREE_PA_NOTIFICATION)
			{
				CustomMessage message = null;
				int accountExpire = getNetConnection().getPremiumAccountExpire();
				if(accountExpire != Integer.MAX_VALUE)
				{
					message = new CustomMessage("l2s.gameserver.model.Player.GiveFreePA");
					message.addString(TimeUtils.toSimpleFormat(accountExpire * 1000L));
				}
				else
					message = new CustomMessage("l2s.gameserver.model.Player.GiveUnlimFreePA");

				sendPacket(new ExShowScreenMessage(message.toString(this), 15000, ScreenMessageAlign.TOP_CENTER, true));
			}
			return true;
		}
		return false;
	}

	private boolean startPremiumAccountTask()
	{
		if(!Config.PREMIUM_ACCOUNT_ENABLED)
			return false;

		stopPremiumAccountTask();

		if(getNetConnection() == null)
			return false;

		int accountType = getNetConnection().getPremiumAccountType();
		PremiumAccountTemplate premiumAccount = accountType == 0 ? null : PremiumAccountHolder.getInstance().getPremiumAccount(accountType);
		if(premiumAccount != null)
		{
			int accountExpire = getNetConnection().getPremiumAccountExpire();
			if(accountExpire > System.currentTimeMillis() / 1000L)
			{
				_premiumAccount = premiumAccount;

				premiumAccount.onAdd(this);

				int itemsReceivedType = getVarInt(PA_ITEMS_RECIEVED);
				if(itemsReceivedType != premiumAccount.getType())
				{
					removePremiumAccountItems(false);

					List<ItemData> items = premiumAccount.getGiveItemsOnStart();
					if(!items.isEmpty())
					{
						if(!isInventoryFull())
						{
							sendPacket(SystemMsg.THE_PREMIUM_ITEM_FOR_THIS_ACCOUNT_WAS_PROVIDED_IF_THE_PREMIUM_ACCOUNT_IS_TERMINATED_THIS_ITEM_WILL_BE_DELETED);
							for(ItemData item : items)
								ItemFunctions.addItem(this, item.getId(), item.getCount(), true);
							setVar(PA_ITEMS_RECIEVED, accountType);
						}
						else
							sendPacket(SystemMsg.THE_PREMIUM_ITEM_CANNOT_BE_RECEIVED_BECAUSE_THE_INVENTORY_WEIGHTQUANTITY_LIMIT_HAS_BEEN_EXCEEDED);
					}
				}

				if(accountExpire != Integer.MAX_VALUE)
					_premiumAccountExpirationTask = LazyPrecisionTaskManager.getInstance().startPremiumAccountExpirationTask(this, accountExpire);
				return true;
			}
			else
			{
				if(!Config.PREMIUM_ACCOUNT_BASED_ON_GAMESERVER)
					GameServer.getInstance().getAuthServerCommunication().sendPacket(new BonusRequest(getAccountName(), 0, 0));
			}
		}

		removePremiumAccountItems(true);

		if(tryGiveFreePremiumAccount())
			return false;

		if(Config.PREMIUM_ACCOUNT_BASED_ON_GAMESERVER)
			PremiumAccountDAO.getInstance().delete(getAccountName());

		if(getNetConnection() != null)
		{
			getNetConnection().setPremiumAccountType(0);
			getNetConnection().setPremiumAccountExpire(0);
		}
		return false;
	}

	private void stopPremiumAccountTask()
	{
		if(_premiumAccountExpirationTask != null)
		{
			_premiumAccountExpirationTask.cancel(false);
			_premiumAccountExpirationTask = null;
		}
	}

	private void removePremiumAccountItems(boolean notify)
	{
		PremiumAccountTemplate premiumAccount = PremiumAccountHolder.getInstance().getPremiumAccount(getVarInt(PA_ITEMS_RECIEVED));
		if(premiumAccount != null)
		{
			List<ItemData> items = premiumAccount.getTakeItemsOnEnd();
			if(!items.isEmpty())
			{
				if(notify)
					sendPacket(SystemMsg.THE_PREMIUM_ACCOUNT_HAS_BEEN_TERMINATED_THE_PROVIDED_PREMIUM_ITEM_WAS_DELETED);
				for(ItemData item : items)
					ItemFunctions.deleteItem(this, item.getId(), item.getCount(), notify);
				for(ItemData item : items)
					ItemFunctions.deleteItemsEverywhere(this, item.getId());
			}
		}
		unsetVar(PA_ITEMS_RECIEVED);
	}

	@Override
	public int getInventoryLimit()
	{
		int ivlim;
		if (isGM()) {
			ivlim = Config.INVENTORY_MAXIMUM_GM;
		} else if (getRace() == Race.DWARF) {
			ivlim = Config.INVENTORY_MAXIMUM_DWARF;
		} else {
			ivlim = Config.INVENTORY_MAXIMUM_NO_DWARF;
		}

		ivlim += (int) getStat().getValue(DoubleStat.INVENTORY_NORMAL, 0);

		ivlim += getExpandInventory();

		ivlim = Math.min(ivlim, Config.SERVICES_EXPAND_INVENTORY_MAX);

		return ivlim;
	}

	public int getWarehouseLimit()
	{
		int whlim;
		if (getRace() == Race.DWARF) {
			whlim = Config.WAREHOUSE_SLOTS_DWARF;
		} else {
			whlim = Config.WAREHOUSE_SLOTS_NO_DWARF;
		}

		whlim += (int) getStat().getValue(DoubleStat.STORAGE_PRIVATE, 0);

		whlim += getExpandWarehouse();

		return whlim;
	}

	public int getPrivateSellStoreLimit()
	{
		int pslim;

		if (getRace() == Race.DWARF) {
			pslim = Config.MAX_PVTSTORE_SLOTS_DWARF;
		} else {
			pslim = Config.MAX_PVTSTORE_SLOTS_OTHER;
		}

		pslim += (int) getStat().getValue(DoubleStat.TRADE_SELL, 0);

		return pslim;
	}

	public int getPrivateBuyStoreLimit()
	{
		int pblim;

		if (getRace() == Race.DWARF) {
			pblim = Config.MAX_PVTSTORE_SLOTS_DWARF;
		} else {
			pblim = Config.MAX_PVTSTORE_SLOTS_OTHER;
		}

		pblim += (int) getStat().getValue(DoubleStat.TRADE_BUY, 0);

		return pblim;
	}

	public int getDwarvenRecipeLimit()
	{
		int value = 50;
		value += (int) getStat().getValue(DoubleStat.RECIPE_DWARVEN, 0);
		value += Config.ALT_ADD_RECIPES;
		return value;
	}

	public int getCommonRecipeLimit()
	{
		int value = 50;
		value += (int) getStat().getValue(DoubleStat.RECIPE_COMMON, 0);
		value += Config.ALT_ADD_RECIPES;
		return value;
	}

	public boolean getAndSetLastItemAuctionRequest()
	{
		if(_lastItemAuctionInfoRequest + 2000L < System.currentTimeMillis())
		{
			_lastItemAuctionInfoRequest = System.currentTimeMillis();
			return true;
		}
		else
		{
			_lastItemAuctionInfoRequest = System.currentTimeMillis();
			return false;
		}
	}

	@Override
	public int getNpcId()
	{
		return -2;
	}

	public GameObject getVisibleObject(int id)
	{
		if(getObjectId() == id)
			return this;

		GameObject target = null;

		if(getTargetId() == id)
			target = getTarget();

		if(target == null && isInParty())
			for(Player p : _party.getPartyMembers())
				if(p != null && p.getObjectId() == id)
				{
					target = p;
					break;
				}

		if(target == null)
			target = World.getAroundObjectById(this, id);

		return target == null || target.isInvisible(this) ? null : target;
	}

	@Override
	public String getTitle()
	{
		return super.getTitle();
	}

	public int getTitleColor()
	{
		return _titlecolor;
	}

	public void setTitleColor(final int titlecolor)
	{
		if(titlecolor != DEFAULT_TITLE_COLOR)
			setVar("titlecolor", Integer.toHexString(titlecolor), -1);
		else
			unsetVar("titlecolor");
		_titlecolor = titlecolor;
	}

	@Override
	public boolean isImmobilized()
	{
		return super.isImmobilized() || isOverloaded() || isSitting() || isFishing() || isInTrainingCamp();
	}

	@Override
	public boolean isBlocked()
	{
		return super.isBlocked() || isInMovie() || isInObserverMode() || isTeleporting() || isLogoutStarted() || isInTrainingCamp();
	}

	@Override
	public boolean isInvulnerable()
	{
		return super.isInvulnerable() || isInMovie() || isInTrainingCamp();
	}

	/**
	 * if True, the L2Player can't take more item
	 */
	public void setOverloaded(boolean overloaded)
	{
		_overloaded = overloaded;
	}

	public boolean isOverloaded()
	{
		return _overloaded;
	}

	public boolean isFishing()
	{
		return _fishing.inStarted();
	}

	public Fishing getFishing()
	{
		return _fishing;
	}

	public PremiumAccountTemplate getPremiumAccount()
	{
		return _premiumAccount;
	}

	public boolean hasPremiumAccount()
	{
		return _premiumAccount.getType() > 0;
	}

	public boolean hasVIPAccount()
	{
		return getVIP().getLevel() > 0;
	}

	public int getPremiumAccountLeftTime()
	{
		if(hasPremiumAccount())
		{
			GameClient client = getNetConnection();
			if(client != null)
				return (int) Math.max(0, client.getPremiumAccountExpire() - (System.currentTimeMillis() / 1000));
		}
		return 0;
	}

	public double getRateExp()
	{
		double baseRate;
		if (isInParty())
			baseRate = Config.RATE_XP_BY_LVL[getLevel()] * _party.getRateExp(this) * getVIP().getTemplate().getExpRate();
		else
			baseRate = Config.RATE_XP_BY_LVL[getLevel()] * getPremiumAccount().getExpRate() * getVIP().getTemplate().getExpRate();

		double bonus = 1.0 + (getStat().getValue(DoubleStat.BONUS_EXP, 0.0) / 100.0);
		baseRate *= bonus;

		return baseRate;
	}

	public double getRateSp()
	{
		double baseRate;
		if (isInParty())
			baseRate = Config.RATE_SP_BY_LVL[getLevel()] * _party.getRateSp(this) * getVIP().getTemplate().getSpRate();
		else
			baseRate = Config.RATE_SP_BY_LVL[getLevel()] * getPremiumAccount().getSpRate() * getVIP().getTemplate().getSpRate();

		double bonus = 1.0 + (getStat().getValue(DoubleStat.BONUS_SP, 0.0) / 100.0);
		baseRate *= bonus;

		return baseRate;
	}

	public double getRateSpoil()
	{
		double rate = Config.RATE_DROP_SPOIL_BY_LVL[getLevel()];
		rate *= isInParty() ? _party.getRateSpoil(this) : getPremiumAccount().getSpoilRate();
		rate *= getVIP().getTemplate().getSpoilRate();

		double bonus = 1.0 + (getStat().getValue(DoubleStat.BONUS_SPOIL, 0.0) / 100.0);
		rate *= bonus;

		return rate;
	}

	public double getRateAdena()
	{
		double rate = Config.RATE_DROP_ADENA_BY_LVL[getLevel()];
		rate *= isInParty() ? _party.getRateAdena(this) : getPremiumAccount().getAdenaRate();
		rate *= getVIP().getTemplate().getAdenaRate();

		double bonus = 1.0 + (getStat().getValue(DoubleStat.BONUS_ADENA, 0.0) / 100.0);
		rate *= bonus;

		return rate;
	}

	public double getRateItems()
	{
		double rate = Config.RATE_DROP_ITEMS_BY_LVL[getLevel()];
		rate *= isInParty() ? _party.getRateDrop(this) : getPremiumAccount().getDropRate();
		rate *= getVIP().getTemplate().getDropRate();

		double bonus = 1.0 + (getStat().getValue(DoubleStat.BONUS_DROP, 0.0) / 100.0);
		rate *= bonus;

		return rate;
	}

	public double getRateQuestsDrop()
	{
		double rate = Config.RATE_QUESTS_DROP;
		rate *= getPremiumAccount().getQuestDropRate();
		rate *= getVIP().getTemplate().getQuestDropRate();
		return rate;
	}

	public double getRateQuestsReward()
	{
		double rate = Config.RATE_QUESTS_REWARD;
		rate *= getPremiumAccount().getQuestRewardRate();
		rate *= getVIP().getTemplate().getQuestRewardRate();
		return rate;
	}

	public double getDropChanceMod()
	{
		double mod = Config.DROP_CHANCE_MODIFIER;
		mod *= isInParty() ? _party.getDropChanceMod(this) : getPremiumAccount().getDropChanceModifier();
		mod *= getVIP().getTemplate().getDropChanceModifier();
		return getStat().getValue(DoubleStat.DROP_CHANCE_MODIFIER, mod);
	}

	public double getDropCountMod()
	{
		double mod = Config.DROP_COUNT_MODIFIER;
		mod *= isInParty() ? _party.getDropCountMod(this) : getPremiumAccount().getDropCountModifier();
		mod *= getVIP().getTemplate().getDropCountModifier();
		return getStat().getValue(DoubleStat.DROP_COUNT_MODIFIER, mod);
	}

	public double getSpoilChanceMod()
	{
		double mod = Config.SPOIL_CHANCE_MODIFIER;
		mod *= isInParty() ? _party.getSpoilChanceMod(this) : getPremiumAccount().getSpoilChanceModifier();
		mod *= getVIP().getTemplate().getSpoilChanceModifier();
		return getStat().getValue(DoubleStat.SPOIL_CHANCE_MODIFIER, mod);
	}

	public double getSpoilCountMod()
	{
		double mod = Config.SPOIL_COUNT_MODIFIER;
		mod *= isInParty() ? _party.getSpoilCountMod(this) : getPremiumAccount().getSpoilCountModifier();
		mod *= getVIP().getTemplate().getSpoilCountModifier();
		return getStat().getValue(DoubleStat.SPOIL_COUNT_MODIFIER, mod);
	}

	private boolean _maried = false;
	private int _partnerId = 0;
	private int _coupleId = 0;
	private boolean _maryrequest = false;
	private boolean _maryaccepted = false;

	public boolean isMaried()
	{
		return _maried;
	}

	public void setMaried(boolean state)
	{
		_maried = state;
	}

	public void setMaryRequest(boolean state)
	{
		_maryrequest = state;
	}

	public boolean isMaryRequest()
	{
		return _maryrequest;
	}

	public void setMaryAccepted(boolean state)
	{
		_maryaccepted = state;
	}

	public boolean isMaryAccepted()
	{
		return _maryaccepted;
	}

	public int getPartnerId()
	{
		return _partnerId;
	}

	public void setPartnerId(int partnerid)
	{
		_partnerId = partnerid;
	}

	public int getCoupleId()
	{
		return _coupleId;
	}

	public void setCoupleId(int coupleId)
	{
		_coupleId = coupleId;
	}

	private OnPlayerChatMessageReceive _snoopListener = null;
	private List<Player> _snoopListenerPlayers = new ArrayList<Player>();

	private class SnoopListener implements OnPlayerChatMessageReceive
	{
		@Override
		public void onChatMessageReceive(Player player, ChatType type, String charName, String text)
		{
			if(_snoopListenerPlayers.size() > 0)
			{
				SnoopPacket sn = new SnoopPacket(getObjectId(), getName(), type.ordinal(), charName, text);
				for(Player pci : _snoopListenerPlayers)
				{
					if(pci != null)
						pci.sendPacket(sn);
				}
			}
		}
	}

	public void addSnooper(Player pci)
	{
		if(!_snoopListenerPlayers.contains(pci))
			_snoopListenerPlayers.add(pci);

		if(!_snoopListenerPlayers.isEmpty() && _snoopListener == null)
		{
			_snoopListener = new SnoopListener();
			addListener(_snoopListener);
		}
	}

	public void removeSnooper(Player pci)
	{
		_snoopListenerPlayers.remove(pci);

		if(_snoopListenerPlayers.isEmpty() && _snoopListener != null)
		{
			removeListener(_snoopListener);
			_snoopListener = null;
		}
	}

	/**
	 * ?????????? ?????????? ???????? ???????????? ??????????????????.
	 */
	public void resetReuse()
	{
		_skillReuses.clear();
		_sharedGroupReuses.clear();
	}

	private boolean _charmOfCourage = false;

	public boolean isCharmOfCourage()
	{
		return _charmOfCourage;
	}

	public void setCharmOfCourage(boolean val)
	{
		_charmOfCourage = val;

		sendEtcStatusUpdate();
	}

	// charges
	private final AtomicInteger _charges = new AtomicInteger();
	private ScheduledFuture<?> _chargeTask = null;

	@Override
	public int getCharges()
	{
		return _charges.get();
	}

	@Override
	public void setCharges(int count)
	{
		restartChargeTask();
		_charges.set(count);
	}

	@Override
	public boolean decreaseCharges(int count)
	{
		if (_charges.get() < count)
		{
			return false;
		}

		// Charge clear task should be reset every time a charge is decreased and stopped when charges become 0.
		if (_charges.addAndGet(-count) == 0)
		{
			stopChargeTask();
		}
		else
		{
			restartChargeTask();
		}

		sendPacket(new EtcStatusUpdatePacket(this));
		return true;
	}

	public void clearCharges()
	{
		_charges.set(0);
		sendPacket(new EtcStatusUpdatePacket(this));
	}

	/**
	 * Starts/Restarts the ChargeTask to Clear Charges after 10 Mins.
	 */
	private void restartChargeTask()
	{
		if (_chargeTask != null)
		{
			_chargeTask.cancel(false);
			_chargeTask = null;
		}
		_chargeTask = ThreadPoolManager.getInstance().schedule(new ResetChargesTask(this), 600000, TimeUnit.MILLISECONDS);
	}

	/**
	 * Stops the Charges Clearing Task.
	 */
	public void stopChargeTask()
	{
		if (_chargeTask != null)
		{
			_chargeTask.cancel(false);
			_chargeTask = null;
		}
	}

	/**
	 * @param z
	 * @return true if character falling now on the start of fall return false for correct coord sync!
	 */
	public final boolean isFalling(int z)
	{
		if(!Config.DAMAGE_FROM_FALLING || isDead() || isFlying() || isInWater() || isInBoat())
			return false;

		if(System.currentTimeMillis() < _fallingTimestamp)
			return true;

		final double deltaZ = Math.abs(getZ() - z);
		if(deltaZ <= getTemplate().getBaseSafeFallHeight())
			return false;

		// If there is no geodata loaded for the place we are client Z correction might cause falling damage.
		if(!GeoEngine.hasGeo(getX(), getY(), getGeoIndex()))
			return false;

		final int damage = (int) Formulas.INSTANCE.calcFallDam(this, deltaZ);
		if(damage > 0)
		{
			setCurrentHp(Math.max(1, (int) (getCurrentHp() - damage)), false);
			sendPacket(new SystemMessage(SystemMessage.YOU_RECEIVED_S1_DAMAGE_FROM_TAKING_A_HIGH_FALL).addNumber(damage));
		}
		
		setFalling();
		
		return false;
	}

	/**
	 * Set falling timestamp
	 */
	public final void setFalling()
	{
		_fallingTimestamp = System.currentTimeMillis() + FALLING_VALIDATION_DELAY;
	}

	/**
	 * ?????????????????? ?????????????????? ?? ?????????????? ?????????????????? ????
	 */
	@Override
	public void checkHpMessages(double curHp, double newHp)
	{
		//???????? ???????????????? ????????????
		int[] _hp = { 30, 30 };
		int[] skills = { 290, 291 };

		//???????? ???????????????? ??????????????
		int[] _effects_skills_id = { 139, 176, 292, 292, 420 };
		int[] _effects_hp = { 30, 30, 30, 60, 30 };

		double percent = getMaxHp() / 100;
		double _curHpPercent = curHp / percent;
		double _newHpPercent = newHp / percent;
		boolean needsUpdate = false;

		//check for passive skills
		for(int i = 0; i < skills.length; i++)
		{
			int level = getSkillLevel(skills[i]);
			if(level > 0)
				if(_curHpPercent > _hp[i] && _newHpPercent <= _hp[i])
				{
					sendPacket(new SystemMessage(SystemMessage.SINCE_HP_HAS_DECREASED_THE_EFFECT_OF_S1_CAN_BE_FELT).addSkillName(skills[i], level));
					needsUpdate = true;
				}
				else if(_curHpPercent <= _hp[i] && _newHpPercent > _hp[i])
				{
					sendPacket(new SystemMessage(SystemMessage.SINCE_HP_HAS_INCREASED_THE_EFFECT_OF_S1_WILL_DISAPPEAR).addSkillName(skills[i], level));
					needsUpdate = true;
				}
		}

		//check for active effects
		for(Integer i = 0; i < _effects_skills_id.length; i++)
			if(getAbnormalList().contains(_effects_skills_id[i]))
				if(_curHpPercent > _effects_hp[i] && _newHpPercent <= _effects_hp[i])
				{
					sendPacket(new SystemMessage(SystemMessage.SINCE_HP_HAS_DECREASED_THE_EFFECT_OF_S1_CAN_BE_FELT).addSkillName(_effects_skills_id[i], 1));
					needsUpdate = true;
				}
				else if(_curHpPercent <= _effects_hp[i] && _newHpPercent > _effects_hp[i])
				{
					sendPacket(new SystemMessage(SystemMessage.SINCE_HP_HAS_INCREASED_THE_EFFECT_OF_S1_WILL_DISAPPEAR).addSkillName(_effects_skills_id[i], 1));
					needsUpdate = true;
				}

		//if(needsUpdate)
		//	sendChanges();
	}

	/**
	 * ?????????????????? ?????????????????? ?????? ???????????? ???????????? ?? ??????/???????? ShadowSence (skill id = 294)
	 */
	public void checkDayNightMessages()
	{
		int level = getSkillLevel(294);
		if(level > 0)
			if(GameTimeController.getInstance().isNowNight())
				sendPacket(new SystemMessage(SystemMessage.IT_IS_NOW_MIDNIGHT_AND_THE_EFFECT_OF_S1_CAN_BE_FELT).addSkillName(294, level));
			else
				sendPacket(new SystemMessage(SystemMessage.IT_IS_DAWN_AND_THE_EFFECT_OF_S1_WILL_NOW_DISAPPEAR).addSkillName(294, level));
		sendChanges();
	}

	public int getZoneMask()
	{
		return _zoneMask;
	}

	//TODO [G1ta0] ???????????????????????? ?? ???????????????
	@Override
	protected void onUpdateZones(List<Zone> leaving, List<Zone> entering)
	{
		super.onUpdateZones(leaving, entering);

		if((leaving == null || leaving.isEmpty()) && (entering == null || entering.isEmpty()))
			return;

		boolean lastInCombatZone = (_zoneMask & ZONE_PVP_FLAG) == ZONE_PVP_FLAG;
		boolean lastInDangerArea = (_zoneMask & ZONE_ALTERED_FLAG) == ZONE_ALTERED_FLAG;
		boolean lastOnSiegeField = (_zoneMask & ZONE_SIEGE_FLAG) == ZONE_SIEGE_FLAG;
		boolean lastInPeaceZone = (_zoneMask & ZONE_PEACE_FLAG) == ZONE_PEACE_FLAG;
		//FIXME G1ta0 boolean lastInSSQZone = (_zoneMask & ZONE_SSQ_FLAG) == ZONE_SSQ_FLAG;

		boolean isInCombatZone = isInZoneBattle();
		boolean isInDangerArea = isInDangerArea() || isInZone(ZoneType.CHANGED_ZONE);
		boolean isOnSiegeField = isInSiegeZone();
		boolean isInPeaceZone = isInPeaceZone();
		boolean isInSSQZone = isInSSQZone();

		// ?????????????????? ????????????, ???????????? ???????? ???????????????? ?? ????????
		int lastZoneMask = _zoneMask;
		_zoneMask = 0;

		if(isInCombatZone)
			_zoneMask |= ZONE_PVP_FLAG;
		if(isInDangerArea)
			_zoneMask |= ZONE_ALTERED_FLAG;
		if(isOnSiegeField)
			_zoneMask |= ZONE_SIEGE_FLAG;
		if(isInPeaceZone)
			_zoneMask |= ZONE_PEACE_FLAG;
		if(isInSSQZone)
			_zoneMask |= ZONE_SSQ_FLAG;

		if(lastZoneMask != _zoneMask)
			sendPacket(new ExSetCompassZoneCode(this));

		boolean broadcastRelation = false;
		if(lastInCombatZone != isInCombatZone)
			broadcastRelation = true;

		if(lastInDangerArea != isInDangerArea)
			sendPacket(new EtcStatusUpdatePacket(this));

		if(lastOnSiegeField != isOnSiegeField)
		{
			broadcastRelation = true;
			if(isOnSiegeField)
				sendPacket(SystemMsg.YOU_HAVE_ENTERED_A_COMBAT_ZONE);
			else
			{
				//???????? ?????????? ?????????????? ???? ???????????????????? ?????????? ?? ?? ???????? ???????? ????????, ???? ???????????????? ?????? ?? ?????????????? ?? ?????????????????? ??????????.
				//TODO: [Bonux] ?????????????????? ?????? ???? ????????.
				FlagItemAttachment attachment = getActiveWeaponFlagAttachment();
				if(attachment != null)
					attachment.onLeaveSiegeZone(this);

				sendPacket(SystemMsg.YOU_HAVE_LEFT_A_COMBAT_ZONE);
				if(!isTeleporting() && getPvpFlag() == 0)
					startPvPFlag(null);
			}
		}

		if(broadcastRelation)
			broadcastRelation();

		if(isInWater())
			startWaterTask();
		else
			stopWaterTask();
	}

	public void startAutoSaveTask()
	{
		if(!Config.AUTOSAVE)
			return;
		if(_autoSaveTask == null)
			_autoSaveTask = AutoSaveManager.getInstance().addAutoSaveTask(this);
	}

	public void stopAutoSaveTask()
	{
		if(_autoSaveTask != null)
			_autoSaveTask.cancel(false);
		_autoSaveTask = null;
	}

	public void startPcBangPointsTask()
	{
		if(!Config.ALT_PCBANG_POINTS_ENABLED || Config.ALT_PCBANG_POINTS_DELAY <= 0)
			return;
		if(_pcCafePointsTask == null)
			_pcCafePointsTask = LazyPrecisionTaskManager.getInstance().addPCCafePointsTask(this);
	}

	public void stopPcBangPointsTask()
	{
		if(_pcCafePointsTask != null)
			_pcCafePointsTask.cancel(false);
		_pcCafePointsTask = null;
	}

	public void startUnjailTask(Player player, int time)
	{
		if(_unjailTask != null)
			_unjailTask.cancel(false);
		_unjailTask = ThreadPoolManager.getInstance().schedule(new UnJailTask(player), time * 60000);
	}

	public void stopUnjailTask()
	{
		if(_unjailTask != null)
			_unjailTask.cancel(false);
		_unjailTask = null;
	}

	public void startTrainingCampTask(long timeRemaining)
	{
		if(_trainingCampTask == null && isInTrainingCamp())
			_trainingCampTask = ThreadPoolManager.getInstance().schedule(() -> TrainingCampManager.getInstance().onExitTrainingCamp(this), timeRemaining);
	}

	public void stopTrainingCampTask()
	{
		if(_trainingCampTask != null)
		{
			_trainingCampTask.cancel(false);
			_trainingCampTask = null;
		}
	}

	public boolean isInTrainingCamp()
	{
		TrainingCamp trainingCamp = TrainingCampManager.getInstance().getTrainingCamp(this);
		return trainingCamp != null && trainingCamp.isTraining() && trainingCamp.isValid(this);
	}

	@Override
	public void sendMessage(String message)
	{
		sendPacket(new SystemMessage(message));
	}

	private int _useSeed = 0;

	public void setUseSeed(int id)
	{
		_useSeed = id;
	}

	public int getUseSeed()
	{
		return _useSeed;
	}

	@Override
	public int getRelation(Player target)
	{
		final Clan clan = getClan();
		final Party party = getParty();
		final Clan targetClan = target.getClan();

		int result = 0;

		if(isInZoneBattle())
			result |= RelationChangedPacket.RELATION_INSIDE_BATTLEFIELD;

		if(isPK())
			result |= RelationChangedPacket.RELATION_CHAOTIC;

		if(getPvpFlag() != 0)
			result |= RelationChangedPacket.RELATION_IN_PVP;

		if(clan != null)
		{
			result |= RelationChangedPacket.RELATION_IN_PLEDGE;

			if(clan == targetClan)
				result |= RelationChangedPacket.RELATION_SAME_PLEDGE;

			Alliance ally = clan.getAlliance();
			if(ally != null)
			{
				result |= RelationChangedPacket.RELATION_IN_ALLIANCE;

				if(ally.getAllyId() == target.getAllyId())
					result |= RelationChangedPacket.RELATION_SAME_ALLIANCE;
			}

			if(isClanLeader())
			{
				result |= RelationChangedPacket.RELATION_PLEDGE_LEADER;

				if(ally != null && clan == ally.getLeader())
					result |= RelationChangedPacket.RELATION_ALLIANCE_LEADER;
			}
		}

		if(party != null)
		{
			result |= RelationChangedPacket.RELATION_IN_PARTY;

			if(party.isLeader(this))
				result |= RelationChangedPacket.RELATION_PARTY_LEADER;

			if(party == target.getParty())
				result |= RelationChangedPacket.RELATION_SAME_PARTY;
		}

		if(clan != null && targetClan != null)
		{
			if(getPledgeType() != Clan.SUBUNIT_ACADEMY && target.getPledgeType() != Clan.SUBUNIT_ACADEMY)
			{
				ClanWar war = clan.getWarWith(target.getClanId());
				if(war != null)
				{
					switch(war.getPeriod())
					{
						case PREPARATION:
						{
							if(war.isAttacker(clan))
								result |= RelationChangedPacket.RELATION_CLAN_WAR_ATTACKER;
							else if(war.isAttacked(clan))
								result |= RelationChangedPacket.RELATION_CLAN_WAR_ATTACKED;
							break;
						}
						case MUTUAL:
						{
							result |= RelationChangedPacket.RELATION_CLAN_WAR_ATTACKER;
							result |= RelationChangedPacket.RELATION_CLAN_WAR_ATTACKED;
							break;
						}
					}
				}
			}
		}

		for(Event e : getEvents())
			result = e.getRelation(this, target, result);

		return result;
	}

	/**
	 * 0=White, 1=Purple, 2=PurpleBlink
	 */
	protected int _pvpFlag;

	private Future<?> _PvPRegTask;
	private long _lastPvPAttack;

	public long getLastPvPAttack()
	{
		return isVioletBoy() ? System.currentTimeMillis() : _lastPvPAttack;
	}

	public void setLastPvPAttack(long time)
	{
		_lastPvPAttack = time;
	}

	@Override
	public void startPvPFlag(Creature target)
	{
		if(isPK() || isVioletBoy())
			return;

		long startTime = System.currentTimeMillis();
		if(target != null && target.getPvpFlag() != 0)
			startTime -= Config.PVP_TIME / 2;

		if(getPvpFlag() != 0 && getLastPvPAttack() >= startTime)
			return;

		_lastPvPAttack = startTime;

		updatePvPFlag(1);

		if(_PvPRegTask == null)
			_PvPRegTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new PvPFlagTask(this), 1000, 1000);
	}

	public void stopPvPFlag()
	{
		if(_PvPRegTask != null)
		{
			_PvPRegTask.cancel(false);
			_PvPRegTask = null;
		}
		updatePvPFlag(0);
	}

	public void updatePvPFlag(int value)
	{
		if(getPvpFlag() == value)
			return;

		setPvpFlag(value);

		sendStatusUpdate(true, true, StatusUpdatePacket.PVP_FLAG);

		broadcastRelation();
	}

	public void setPvpFlag(int pvpFlag)
	{
		_pvpFlag = pvpFlag;
	}

	@Override
	public int getPvpFlag()
	{
		return isVioletBoy() ? 1 : _pvpFlag;
	}

	public boolean isInDuel()
	{
		return containsEvent(DuelEvent.class);
	}

	private long _lastMovePacket = 0;

	public long getLastMovePacket()
	{
		return _lastMovePacket;
	}

	public void setLastMovePacket()
	{
		_lastMovePacket = System.currentTimeMillis();
	}

	public byte[] getKeyBindings()
	{
		return _keyBindings;
	}

	public void setKeyBindings(byte[] keyBindings)
	{
		if(keyBindings == null)
			keyBindings = ArrayUtils.EMPTY_BYTE_ARRAY;
		_keyBindings = keyBindings;
	}

	/**
	 * ???????????????????? ?????????????????? ??????????????, ?? ???????????? ?????????????? ??????????????????????????
	 */
	@Override
	public final Collection<SkillEntry> getAllSkills()
	{
		// ?????????????????????????? ??????????????????
		if(!isTransformed())
			return super.getAllSkills();

		// ?????????????????????????? ??????????????
		IntObjectMap<SkillEntry> temp = new HashIntObjectMap<SkillEntry>();
		for(SkillEntry skillEntry : super.getAllSkills())
		{
			Skill skill = skillEntry.getTemplate();
			if(!skill.isActive() && !skill.isToggle())
				temp.put(skillEntry.getId(), skillEntry);
		}

		temp.putAll(_transformSkills); // ?????????????????? ?? ?????????????????? ?????????? ?????????????? ??????????????????????????
		return temp.valueCollection();
	}

	public final void addTransformSkill(SkillEntry skillEntry)
	{
		_transformSkills.put(skillEntry.getId(), skillEntry);
	}

	public final void removeTransformSkill(SkillEntry skillEntry)
	{
		_transformSkills.remove(skillEntry.getId());
	}

	public void deleteAgathion()
	{
		if(_agathion != null)
			_agathion.delete();
	}

	public void setAgathion(Agathion agathion)
	{
		if(_agathion == agathion)
			return;

		_agathion = agathion;

		sendPacket(new ExUserInfoCubic(this));
		broadcastCharInfo();
	}

	public Agathion getAgathion()
	{
		return _agathion;
	}

	public int getAgathionId()
	{
		return _agathion == null ? 0 : _agathion.getId();
	}

	public int getAgathionNpcId()
	{
		return _agathion == null ? 0 : _agathion.getNpcId();
	}

	/**
	 * ???????????????????? ???????????????????? PcBangPoint'???? ???????????? ????????????
	 *
	 * @return ???????????????????? PcCafe Bang Points
	 */
	public int getPcBangPoints()
	{
		return _pcBangPoints;
	}

	/**
	 * ?????????????????????????? ???????????????????? Pc Cafe Bang Points ?????? ???????????? ????????????
	 *
	 * @param val ?????????? ???????????????????? PcCafeBangPoints
	 */
	public void setPcBangPoints(int val, boolean store)
	{
		_pcBangPoints = val;

		if(store)
		{
			if(Config.PC_BANG_POINTS_BY_ACCOUNT)
				AccountVariablesDAO.getInstance().insert(getAccountName(), PC_BANG_POINTS_VAR, String.valueOf(getPcBangPoints()));
			else
			{
				Connection con = null;
				PreparedStatement st = null;
				try
				{
					con = DatabaseFactory.getInstance().getConnection();
					st = con.prepareStatement("UPDATE characters SET pcBangPoints = ? WHERE obj_Id = ?");
					st.setInt(1, getPcBangPoints());
					st.setInt(2, getObjectId());
					st.executeUpdate();
				}
				catch(Exception e)
				{
					_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "" );
				}
				finally
				{
					DbUtils.closeQuietly(con, st);
				}
			}
		}
	}

	public void addPcBangPoints(int count, boolean doublePoints, boolean notify)
	{
		if(doublePoints)
			count *= 2;

		setPcBangPoints(getPcBangPoints() + count, true);

		if(count > 0 && notify)
			sendPacket(new SystemMessage(doublePoints ? SystemMessage.DOUBLE_POINTS_YOU_AQUIRED_S1_PC_BANG_POINT : SystemMessage.YOU_ACQUIRED_S1_PC_BANG_POINT).addNumber(count));
		sendPacket(new ExPCCafePointInfo(this, count, 1, 2, 12));
	}

	public boolean reducePcBangPoints(int count, boolean notify)
	{
		if(getPcBangPoints() < count)
			return false;

		setPcBangPoints(getPcBangPoints() - count, true);

		if(notify)
			sendPacket(new SystemMessage(SystemMessage.YOU_ARE_USING_S1_POINT).addNumber(count));
		sendPacket(new ExPCCafePointInfo(this, 0, 1, 2, 12));
		return true;
	}

	private Location _groundSkillLoc;

	public void setGroundSkillLoc(Location location)
	{
		_groundSkillLoc = location;
	}

	public Location getGroundSkillLoc()
	{
		return _groundSkillLoc;
	}

	/**
	 * ???????????????? ?? ???????????????? ???????????? ???? ????????
	 *
	 * @return ???????????????????? true ???????? ?????????????? ???????????? ?????? ??????????????
	 */
	public boolean isLogoutStarted()
	{
		if(_isLogout == null) // ?????? ?????????? ??????, ???? ???????????? ???????????? ???????????????? ???? ?????? _isLogout. ???????????????? ???????!?
			return false;
		return _isLogout.get();
	}

	public void setOfflineMode(boolean val)
	{
		if(val == isInOfflineMode())
			return;

		GameObjectsStorage.remove(this);

		_offlineStartTime = val ? System.currentTimeMillis() : 0L;

		GameObjectsStorage.put(this);

		if(!isInOfflineMode())
			unsetVar("offline");
	}

	public boolean isInOfflineMode()
	{
		return _offlineStartTime > 0L;
	}

	public void storePrivateStore() {
		final int storeType = getPrivateStoreType();

		if(storeType == STORE_PRIVATE_NONE)
			unsetVar("storemode");
		else if(Config.ALT_SAVE_PRIVATE_STORE || isInOfflineMode())
			setVar("storemode", storeType);

		List<TradeItem> buyList = getBuyList();
		if(!buyList.isEmpty() && (Config.ALT_SAVE_PRIVATE_STORE || isInOfflineMode() && storeType == STORE_PRIVATE_BUY)) {
			if(CharacterPrivateStoreDAO.getInstance().insertBuys(this, buyList)) {
				String title = getBuyStoreName();
				if (title != null && !title.isEmpty())
					setVar("buystorename", title, -1);
				else
					unsetVar("buystorename");
			} else
				unsetVar("buystorename");
		}
		else {
			CharacterPrivateStoreDAO.getInstance().deleteBuys(this);
			unsetVar("buystorename");
		}

		Map<Integer, TradeItem> sellList = getSellList(false);
		if(!sellList.isEmpty() && (Config.ALT_SAVE_PRIVATE_STORE || isInOfflineMode() && storeType == STORE_PRIVATE_SELL)) {
			if(CharacterPrivateStoreDAO.getInstance().insertSells(this, sellList, false)) {
				String title = getSellStoreName();
				if (title != null && !title.isEmpty())
					setVar("sellstorename", title, -1);
				else
					unsetVar("sellstorename");
			} else
				unsetVar("sellstorename");
		}
		else {
			CharacterPrivateStoreDAO.getInstance().deleteSells(this, false);
			unsetVar("sellstorename");
		}

		Map<Integer, TradeItem> packageSellList = getSellList(true);
		if(!packageSellList.isEmpty() && (Config.ALT_SAVE_PRIVATE_STORE || isInOfflineMode() && storeType == STORE_PRIVATE_SELL_PACKAGE)) {
			if(CharacterPrivateStoreDAO.getInstance().insertSells(this, packageSellList, true)) {
				String title = getPackageSellStoreName();
				if (title != null && !title.isEmpty())
					setVar("packagesellstorename", title, -1);
				else
					unsetVar("packagesellstorename");
			} else
				unsetVar("packagesellstorename");
		}
		else {
			CharacterPrivateStoreDAO.getInstance().deleteSells(this, true);
			unsetVar("packagesellstorename");
		}

		Map<Integer, ManufactureItem> createList = getCreateList();
		if(!createList.isEmpty() && (Config.ALT_SAVE_PRIVATE_STORE || isInOfflineMode() && storeType == STORE_PRIVATE_MANUFACTURE)) {
			if(CharacterPrivateStoreDAO.getInstance().insertManufactures(this, createList)) {
				String title = getManufactureName();
				if (title != null && !title.isEmpty())
					setVar("manufacturename", title, -1);
				else
					unsetVar("manufacturename");
			} else
				unsetVar("manufacturename");
		}
		else {
			CharacterPrivateStoreDAO.getInstance().deleteManufactures(this);
			unsetVar("manufacturename");
		}
	}

	public void restorePrivateStore() {
		List<TradeItem> buyList = CharacterPrivateStoreDAO.getInstance().selectBuys(this);
		if(!buyList.isEmpty()) {
			setBuyList(buyList);

			String name = getVar("buystorename");
			if(name != null)
				setBuyStoreName(name);
		}

		Map<Integer, TradeItem> sellList = CharacterPrivateStoreDAO.getInstance().selectSells(this, false);
		if(!sellList.isEmpty()) {
			setSellList(false, sellList);

			String name = getVar("sellstorename");
			if(name != null)
				setSellStoreName(name);
		}

		Map<Integer, TradeItem> packageSellList = CharacterPrivateStoreDAO.getInstance().selectSells(this, true);
		if(!packageSellList.isEmpty()) {
			setSellList(true, packageSellList);

			String name = getVar("packagesellstorename");
			if(name != null)
				setPackageSellStoreName(name);
		}

		Map<Integer, ManufactureItem> createList = CharacterPrivateStoreDAO.getInstance().selectManufactures(this);
		if(!createList.isEmpty()) {
			setCreateList(createList);

			String name = getVar("manufacturename");
			if(name != null)
				setManufactureName(name);
		}

		int storeType = getVarInt("storemode", STORE_PRIVATE_NONE);
		if(storeType != STORE_PRIVATE_NONE)
		{
			setPrivateStoreType(storeType);
			setSitting(true);
		}
	}

	public void restoreRecipeBook()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT id FROM character_recipebook WHERE char_id=?");
			statement.setInt(1, getObjectId());
			rset = statement.executeQuery();

			while(rset.next())
			{
				int id = rset.getInt("id");
				RecipeTemplate recipe = RecipeHolder.getInstance().getRecipeByRecipeId(id);
				registerRecipe(recipe, false);
			}
		}
		catch(Exception e)
		{
			_log.atWarning().log( "count not recipe skills:%s", e );
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "" );
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public List<DecoyInstance> getDecoys()
	{
		return _decoys;
	}

	public void addDecoy(DecoyInstance decoy)
	{
		_decoys.add(decoy);
	}

	public void removeDecoy(DecoyInstance decoy)
	{
		_decoys.remove(decoy);
	}

	public MountType getMountType()
	{
		return _mount == null ? MountType.NONE : _mount.getType();
	}

	@Override
	public boolean setReflection(Reflection reflection)
	{
		if(getReflection() == reflection)
			return true;

		if(!super.setReflection(reflection))
			return false;

		for(Servitor servitor : getServitors())
		{
			if(!servitor.isDead())
				servitor.setReflection(reflection);
		}

		if(!reflection.isMain())
		{
			String var = getVar("reflection");
			if(var == null || !var.equals(String.valueOf(reflection.getId())))
				setVar("reflection", String.valueOf(reflection.getId()), -1);
		}
		else
			unsetVar("reflection");

		return true;
	}

	private int _buyListId;

	public void setBuyListId(int listId)
	{
		_buyListId = listId;
	}

	public int getBuyListId()
	{
		return _buyListId;
	}

	public int getFame()
	{
		return _fame;
	}

	public void setFame(int fame, String log, boolean notify)
	{
		fame = Math.min(Config.LIM_FAME, fame);
		if(log != null && !log.isEmpty())
			Log.add(_name + "|" + (fame - _fame) + "|" + fame + "|" + log, "fame");
		if(fame > _fame && notify)
			sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_ACQUIRED_S1_REPUTATION_SCORE).addNumber(fame - _fame));
		_fame = fame;
		sendChanges();
	}

	private final int _incorrectValidateCount = 0;

	public int getIncorrectValidateCount()
	{
		return _incorrectValidateCount;
	}

	public int setIncorrectValidateCount(int count)
	{
		return _incorrectValidateCount;
	}

	public int getExpandInventory()
	{
		return _expandInventory;
	}

	public void setExpandInventory(int inventory)
	{
		_expandInventory = inventory;
	}

	public int getExpandWarehouse()
	{
		return _expandWarehouse;
	}

	public void setExpandWarehouse(int warehouse)
	{
		_expandWarehouse = warehouse;
	}

	public boolean isNotShowBuffAnim()
	{
		return _notShowBuffAnim;
	}

	public void setNotShowBuffAnim(boolean value)
	{
		_notShowBuffAnim = value;
	}

	public boolean canSeeAllShouts()
	{
		return _canSeeAllShouts;
	}

	public void setCanSeeAllShouts(boolean b)
	{
		_canSeeAllShouts = b;
	}

	public void enterMovieMode()
	{
		if(isInMovie()) //already in movie
			return;

		abortAttack(true, false);
		abortCast(true, false);
		setTarget(null);
		getMovement().stopMove();
		setMovieId(-1);
		sendPacket(CameraModePacket.ENTER);
	}

	public void leaveMovieMode()
	{
		setMovieId(0);
		sendPacket(CameraModePacket.EXIT);
		broadcastUserInfo(true);
	}

	public void specialCamera(GameObject target, int dist, int yaw, int pitch, int time, int duration)
	{
		sendPacket(new SpecialCameraPacket(target.getObjectId(), dist, yaw, pitch, time, duration));
	}

	public void specialCamera(GameObject target, int dist, int yaw, int pitch, int time, int duration, int turn, int rise, int widescreen, int unk)
	{
		sendPacket(new SpecialCameraPacket(target.getObjectId(), dist, yaw, pitch, time, duration, turn, rise, widescreen, unk));
	}

	private int _movieId = 0;

	public void setMovieId(int id)
	{
		_movieId = id;
	}

	public int getMovieId()
	{
		return _movieId;
	}

	public boolean isInMovie()
	{
		return _movieId != 0 && !isFakePlayer();
	}

	public void startScenePlayer(SceneMovie movie)
	{
		if(isInMovie()) //already in movie
			return;

		sendActionFailed();
		setTarget(null);
		getMovement().stopMove();
		setMovieId(movie.getId());
		sendPacket(movie.packet(this));
		_sceneMovieEndTask = ThreadPoolManager.getInstance().schedule(() -> {
			if(_sceneMovieEndTask != null)
			{
				_sceneMovieEndTask.cancel(false);
				_sceneMovieEndTask = null;
			}
			endScenePlayer(true);
		}, movie.getDuration() + 500L); // ?????????? ???????????? ????????????????
	}

	public void startScenePlayer(int movieId)
	{
		SceneMovie movie = SceneMovie.getMovie(movieId);
		if(movie != null)
			startScenePlayer(movie);
	}

	public void endScenePlayer(boolean force)
	{
		if(!isInMovie())
			return;

		SceneMovie movie = SceneMovie.getMovie(getMovieId());
		if(movie != null)
		{
			if(force && !movie.isCancellable() && _sceneMovieEndTask != null)
				return;
		}
		else if(force)
			return;

		if(_sceneMovieEndTask != null)
		{
			_sceneMovieEndTask.cancel(false);
			_sceneMovieEndTask = null;
		}

		setMovieId(0);
		//decayMe();
		//spawnMe();

		if(force && movie != null)
			sendPacket(new ExStopScenePlayer(movie.getId()));
	}

	public void setAutoLoot(boolean enable)
	{
		if(Config.AUTO_LOOT_INDIVIDUAL)
		{
			_autoLoot = enable;
			setVar("AutoLoot", String.valueOf(enable), -1);
		}
	}

	public void setAutoLootOnlyAdena(boolean enable)
	{
		if(Config.AUTO_LOOT_INDIVIDUAL && Config.AUTO_LOOT_ONLY_ADENA)
		{
			_autoLootOnlyAdena = enable;
			setVar("AutoLootOnlyAdena", String.valueOf(enable), -1);
		}
	}

	public void setAutoLootHerbs(boolean enable)
	{
		if(Config.AUTO_LOOT_INDIVIDUAL)
		{
			AutoLootHerbs = enable;
			setVar("AutoLootHerbs", String.valueOf(enable), -1);
		}
	}

	public boolean isAutoLootEnabled()
	{
		return _autoLoot;
	}

	public boolean isAutoLootOnlyAdenaEnabled()
	{
		return _autoLootOnlyAdena;
	}

	public boolean isAutoLootHerbsEnabled()
	{
		return AutoLootHerbs;
	}

	public final void reName(String name, boolean saveToDB)
	{
		setName(name);
		if(saveToDB)
		{
			saveNameToDB();

			OlympiadParticipiantData participant = Olympiad.getParticipantInfo(getObjectId());
			if(participant != null)
				participant.setName(name);
		}

		sendUserInfo(true);

		for(Player p : World.getAroundObservers(this))
		{
			p.sendPacket(p.removeVisibleObject(this, null));
			if(isVisible() && !isInvisible(p))
				p.sendPacket(p.addVisibleObject(this, null));

			p.getFriendList().notifyChangeName(getObjectId());
			p.getBlockList().notifyChangeName(getObjectId());
		}

		Party party = getParty();
		if(party != null)
			party.updatePartyInfo();

		Clan clan = getClan();
		if(clan != null)
			clan.broadcastClanStatus(true, false, false);
	}

	public final void reName(String name)
	{
		reName(name, false);
	}

	public final void saveNameToDB()
	{
		Connection con = null;
		PreparedStatement st = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			st = con.prepareStatement("UPDATE characters SET char_name = ? WHERE obj_Id = ?");
			st.setString(1, getName());
			st.setInt(2, getObjectId());
			st.executeUpdate();
		}
		catch(Exception e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "" );
		}
		finally
		{
			DbUtils.closeQuietly(con, st);
		}
	}

	@Override
	public Player getPlayer()
	{
		return this;
	}

	public BypassStorage getBypassStorage()
	{
		return _bypassStorage;
	}

	/**
	 * Gets the maximum brooch jewel count.
	 * @return the maximum brooch jewel count
	 */
	public int getBroochJewelSlots()
	{
		return (int) getStat().getValue(DoubleStat.BROOCH_JEWELS, 0);
	}

	public int getTalismanCount()
	{
		return getStat().getTalismanSlots();
	}

	public boolean isActiveMainAgathionSlot()
	{
		return getStat().has(BooleanStat.PRIMARY_AGATHION_SLOT_ENABLED);
	}

	public int getSubAgathionsLimit()
	{
		return (int) getStat().getValue(DoubleStat.SECONDARY_AGATHION_SLOTS, 0);
	}

	public final void disableDrop(int time)
	{
		_dropDisabled = System.currentTimeMillis() + time;
	}

	public final boolean isDropDisabled()
	{
		return _dropDisabled > System.currentTimeMillis();
	}

	private ItemInstance _petControlItem = null;

	public void setPetControlItem(int itemObjId)
	{
		setPetControlItem(getInventory().getItemByObjectId(itemObjId));
	}

	public void setPetControlItem(ItemInstance item)
	{
		_petControlItem = item;
	}

	public ItemInstance getPetControlItem()
	{
		return _petControlItem;
	}

	private AtomicBoolean isActive = new AtomicBoolean();

	public boolean isActive()
	{
		return isActive.get();
	}

	public void setActive()
	{
		endScenePlayer(true);
		setNonAggroTime(0);
		setNonPvpTime(0);

		if(isActive.getAndSet(true))
			return;

		onActive();
	}

	private void onActive()
	{
		sendPacket(SystemMsg.YOU_ARE_NO_LONGER_PROTECTED_FROM_AGGRESSIVE_MONSTERS);

		if(getPetControlItem() != null || _restoredSummons != null && !_restoredSummons.isEmpty())
		{
			ThreadPoolManager.getInstance().execute(() -> {
				if(getPetControlItem() != null)
					summonPet();

				if(_restoredSummons != null && !_restoredSummons.isEmpty())
					spawnRestoredSummons();
			});
		}
	}

	public void summonPet()
	{
		if(getPet() != null)
			return;

		ItemInstance controlItem = getInventory().getItemByObjectId(getPetControlItem().getObjectId());
		if(controlItem == null)
		{
			setPetControlItem(null);
			return;
		}

		PetData petTemplate = PetDataHolder.getInstance().getTemplateByItemId(controlItem.getItemId());
		if(petTemplate == null)
		{
			setPetControlItem(null);
			return;
		}

		NpcTemplate npcTemplate = NpcHolder.getInstance().getTemplate(petTemplate.getNpcId());
		if(npcTemplate == null)
		{
			setPetControlItem(null);
			return;
		}

		PetInstance pet = PetInstance.restore(controlItem, npcTemplate, this);
		if(pet == null)
		{
			setPetControlItem(null);
			return;
		}

		setPet(pet);
		pet.setTitle(Servitor.TITLE_BY_OWNER_NAME);

		if(!pet.isRespawned())
		{
			pet.setCurrentHp(pet.getMaxHp(), false);
			pet.setCurrentMp(pet.getMaxMp());
			pet.setCurrentFed(pet.getMaxFed(), false);
			pet.updateControlItem();
			pet.store();
		}

		pet.getInventory().restore();

		if (!isInPvPEvent()) {
			pet.setNonAggroTime(System.currentTimeMillis() + Config.NONAGGRO_TIME_ONTELEPORT);
			pet.setNonPvpTime(System.currentTimeMillis() + Config.NONPVP_TIME_ONTELEPORT);
		}
		pet.setReflection(getReflection());
		pet.spawnMe(Location.findPointToStay(this, 50, 70));
		pet.setRunning();
		pet.setFollowMode(true);
		pet.getInventory().validateItems();

		if(pet instanceof PetBabyInstance)
			((PetBabyInstance) pet).startBuffTask();

		getListeners().onSummonServitor(pet);
	}

	public void restoreSummons()
	{
		_restoredSummons = SummonsDAO.getInstance().restore(this);
	}

	private void spawnRestoredSummons()
	{
		if(_restoredSummons == null || _restoredSummons.isEmpty())
			return;

		for(RestoredSummon summon : _restoredSummons)
		{
			SummonUtils.INSTANCE.restoreSummon(this, summon);
		}
		_restoredSummons.clear();
		_restoredSummons = null;
	}

	public List<TrapInstance> getTraps()
	{
		return _traps;
	}

	public void addTrap(TrapInstance trap)
	{
		if(_traps == Collections.<TrapInstance> emptyList())
			_traps = new CopyOnWriteArrayList<TrapInstance>();

		_traps.add(trap);
	}

	public void removeTrap(TrapInstance trap)
	{
		_traps.remove(trap);
	}

	public void destroyAllTraps()
	{
		for(TrapInstance t : _traps)
			t.deleteMe();
	}

	@Override
	public PlayerListenerList getListeners()
	{
		if(listeners == null)
			synchronized (this)
			{
				if(listeners == null)
					listeners = new PlayerListenerList(this);
			}
		return (PlayerListenerList) listeners;
	}

	@Override
	public PlayerStatsChangeRecorder getStatsRecorder()
	{
		if(_statsRecorder == null)
			synchronized (this)
			{
				if(_statsRecorder == null)
					_statsRecorder = new PlayerStatsChangeRecorder(this);
			}
		return (PlayerStatsChangeRecorder) _statsRecorder;
	}

	private Future<?> _hourlyTask;
	private AtomicInteger _hoursInGame = new AtomicInteger(0);

	public AtomicInteger getHoursInGame()
	{
		return _hoursInGame;
	}

	public void startHourlyTask()
	{
		_hourlyTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new HourlyTask(this), TimeUnit.HOURS.toMillis(1), TimeUnit.HOURS.toMillis(1));
	}

	public void stopHourlyTask()
	{
		if(_hourlyTask != null)
		{
			_hourlyTask.cancel(false);
			_hourlyTask = null;
		}
	}

	public long getPremiumPoints()
	{
		if(Config.IM_PAYMENT_ITEM_ID > 0)
			return ItemFunctions.getItemCount(this, Config.IM_PAYMENT_ITEM_ID);

		if(getNetConnection() != null)
			return getNetConnection().getPoints();

		return 0;
	}

	public boolean addPremiumPoints(final int val)
	{
		if(Config.IM_PAYMENT_ITEM_ID > 0)
		{
			if(!ItemFunctions.addItem(this, Config.IM_PAYMENT_ITEM_ID, val, true).isEmpty())
				return true;
			return false;
		}

		if(getNetConnection() != null)
		{
			getNetConnection().setPoints((int) (getPremiumPoints() + val));
			GameServer.getInstance().getAuthServerCommunication().sendPacket(new ReduceAccountPoints(getAccountName(), -val));
			return true;
		}
		return false;
	}

	public boolean reducePremiumPoints(final int val)
	{
		if(Config.IM_PAYMENT_ITEM_ID > 0)
		{
			if(ItemFunctions.deleteItem(this, Config.IM_PAYMENT_ITEM_ID, val, true))
				return true;
			return false;
		}

		if(getNetConnection() != null)
		{
			getNetConnection().setPoints((int) (getPremiumPoints() - val));
			GameServer.getInstance().getAuthServerCommunication().sendPacket(new ReduceAccountPoints(getAccountName(), val));
			return true;
		}
		return false;
	}

	private boolean _agathionResAvailable = false;

	public boolean isAgathionResAvailable()
	{
		return _agathionResAvailable;
	}

	public void setAgathionRes(boolean val)
	{
		_agathionResAvailable = val;
	}

	/**
	 * _userSession - ?????????????????????????? ?????? ???????????????? ?????????????????? ????????????????????.
	 */
	private Map<String, String> _userSession;

	public String getSessionVar(String key)
	{
		if(_userSession == null)
			return null;
		return _userSession.get(key);
	}

	public void setSessionVar(String key, String val)
	{
		if(_userSession == null)
			_userSession = new ConcurrentHashMap<String, String>();

		if(val == null || val.isEmpty())
			_userSession.remove(key);
		else
			_userSession.put(key, val);
	}

	public BlockList getBlockList()
	{
		return _blockList;
	}

	public FriendList getFriendList()
	{
		return _friendList;
	}

	public PremiumItemList getPremiumItemList()
	{
		return _premiumItemList;
	}

	public ProductHistoryList getProductHistoryList()
	{
		return _productHistoryList;
	}

	public HennaList getHennaList()
	{
		return _hennaList;
	}

	public AttendanceRewards getAttendanceRewards()
	{
		return _attendanceRewards;
	}

	public DailyMissionList getDailyMissionList()
	{
		return _dailiyMissionList;
	}

	public ElementalList getElementalList()
	{
		return _elementalList;
	}

	public VIP getVIP()
	{
		return _vip;
	}

	public CostumeList getCostumeList() {
		return costumes;
	}

	public boolean isNotShowTraders()
	{
		return _notShowTraders;
	}

	public void setNotShowTraders(boolean notShowTraders)
	{
		_notShowTraders = notShowTraders;
	}

	@Override
	public boolean isDebug()
	{
		return _debug && (Config.ALT_DEBUG_ENABLED || getPlayerAccess().CanDebug);
	}

	public void setDebug(boolean b)
	{
		_debug = b;
	}

	public void sendItemList(boolean show)
	{
		final ItemInstance[] items = getInventory().getItems();

		Collection<ItemInfo> nonQuestItems = new ArrayList<>(items.length);
		Collection<ItemInfo> questItems = new ArrayList<>(items.length / 2);
		Collection<ItemInfo> agathionItems = new ArrayList<>(1);
		for(ItemInstance item : items)
		{
			if(item.getTemplate().isQuest()) {
				questItems.add(new ItemInfo(this, item));
			} else {
				nonQuestItems.add(new ItemInfo(this, item));
			}

			if (item.getTemplate().getAgathionMaxEnergy() > 0) {
				agathionItems.add(new ItemInfo(this, item));
			}
		}

		int nonQuestItemsSize = nonQuestItems.size();
		int questItemsSize = questItems.size();
		int agathionItemsSize = agathionItems.size();

		sendPacket(new ItemListPacket(1, this, nonQuestItemsSize, nonQuestItems, show));
		if (nonQuestItemsSize > 0)
			sendPacket(new ItemListPacket(2, this, nonQuestItemsSize, nonQuestItems, show));
		sendPacket(new ExQuestItemListPacket(1, this, questItemsSize, questItems));
		if (questItemsSize > 0)
			sendPacket(new ExQuestItemListPacket(2, this, questItemsSize, questItems));
		if(agathionItemsSize > 0)
			sendPacket(new ExBR_AgathionEnergyInfoPacket(agathionItemsSize, agathionItems));
	}

	@Override
	public boolean isPlayer()
	{
		return true;
	}

	public boolean checkCoupleAction(Player target)
	{
		if(target.getPrivateStoreType() != STORE_PRIVATE_NONE)
		{
			sendPacket(new SystemMessage(SystemMessage.COUPLE_ACTION_CANNOT_C1_TARGET_IN_PRIVATE_STORE).addName(target));
			return false;
		}
		if(target.isFishing())
		{
			sendPacket(new SystemMessage(SystemMessage.COUPLE_ACTION_CANNOT_C1_TARGET_IS_FISHING).addName(target));
			return false;
		}
		if(target.isInTrainingCamp())
		{
			sendPacket(SystemMsg.YOU_CANNOT_REQUEST_TO_A_CHARACTER_WHO_IS_ENTERING_THE_TRAINING_CAMP);
			return false;
		}
		if(target.isTransformed())
		{
			sendPacket(new SystemMessage(SystemMessage.COUPLE_ACTION_CANNOT_C1_TARGET_IS_IN_TRANSFORM).addName(target));
			return false;
		}
		if(target.isInCombat() || target.isVisualTransformed())
		{
			sendPacket(new SystemMessage(SystemMessage.COUPLE_ACTION_CANNOT_C1_TARGET_IS_IN_COMBAT).addName(target));
			return false;
		}
		if(target.isInOlympiadMode())
		{
			sendPacket(new SystemMessage(SystemMessage.COUPLE_ACTION_CANNOT_C1_TARGET_IS_IN_OLYMPIAD).addName(target));
			return false;
		}
		if(target.isInSiegeZone())
		{
			sendPacket(new SystemMessage(SystemMessage.COUPLE_ACTION_CANNOT_C1_TARGET_IS_IN_SIEGE).addName(target));
			return false;
		}
		if(target.isInBoat() || target.getMountNpcId() != 0)
		{
			sendPacket(new SystemMessage(SystemMessage.COUPLE_ACTION_CANNOT_C1_TARGET_IS_IN_VEHICLE_MOUNT_OTHER).addName(target));
			return false;
		}
		if(target.isTeleporting())
		{
			sendPacket(new SystemMessage(SystemMessage.COUPLE_ACTION_CANNOT_C1_TARGET_IS_TELEPORTING).addName(target));
			return false;
		}
		if(target.isDead())
		{
			sendPacket(new SystemMessage(SystemMessage.COUPLE_ACTION_CANNOT_C1_TARGET_IS_DEAD).addName(target));
			return false;
		}
		return true;
	}

	@Override
	public void startAttackStanceTask()
	{
		startAttackStanceTask0();

		for(Servitor servitor : getServitors())
			servitor.startAttackStanceTask0();
	}

	@Override
	public void displayGiveDamageMessage(Creature target, Skill skill, int damage, Servitor servitorTransferedDamage, int transferedDamage, boolean crit, boolean miss, boolean shld, boolean blocked, int elementalDamage, boolean elementalCrit)
	{
		super.displayGiveDamageMessage(target, skill, damage, servitorTransferedDamage, transferedDamage, crit, miss, shld, blocked, elementalDamage, elementalCrit);

		if(miss)
		{
			if(skill == null)
				sendPacket(new SystemMessage(SystemMessage.C1S_ATTACK_WENT_ASTRAY).addName(this));
			else
				sendPacket(new ExMagicAttackInfo(getObjectId(), target.getObjectId(), ExMagicAttackInfo.EVADED));
			return;
		}

		/*
		5174	1	u,?????????????????????? ???????? ?????????????? $s1!\0	0	79	9B	B0	FF	6	6	0	0	0	0	0	a,	a,	a,	a,none\0
		5176	1	u,$s1 ?????????????? ???????? $s2 $s3 ?????????? (???????? ??????????????: $s4).\0	3	79	9B	B0	FF	6	6	0	0	0	0	0	a,	a,	a,	a,damage\0
		*/
		if(crit)
		{
			if(skill != null)
			{
				if(skill.isMagic())
					sendPacket(SystemMsg.MAGIC_CRITICAL_HIT);
				sendPacket(new ExMagicAttackInfo(getObjectId(), target.getObjectId(), ExMagicAttackInfo.CRITICAL));
			}
			else
				sendPacket(new SystemMessage(SystemMessage.C1_HAD_A_CRITICAL_HIT).addName(this));
		}

		ElementalElement element = getActiveElement();
		if(element != ElementalElement.NONE)
		{
			if(elementalCrit)
				sendPacket(new SystemMessagePacket(SystemMsg.S1_ATTACK_CRITICAL_IS_ACTIVATED).addElementName(element.getId() - 1));
		}

		if(blocked)
		{
			sendPacket(SystemMsg.THE_ATTACK_HAS_BEEN_BLOCKED);
			sendPacket(new ExMagicAttackInfo(getObjectId(), target.getObjectId(), target.isInvulnerable() ? ExMagicAttackInfo.IMMUNE : ExMagicAttackInfo.BLOCKED));
		}
		else if(target.isDoor() || (target instanceof SiegeToggleNpcInstance))
			sendPacket(new SystemMessagePacket(SystemMsg.YOU_HIT_FOR_S1_DAMAGE).addInteger(damage));
		else
		{
			if(element != ElementalElement.NONE && elementalDamage != 0)
			{
				sendPacket(new SystemMessagePacket(SystemMsg.S1_HAS_DELIVERED_S3_ATTRIBUTE_DAMAGE_S4_TO_S2).addName(this).addName(target).addInteger(damage).addInteger(elementalDamage).addHpChange(target.getObjectId(), getObjectId(), -damage));
			}
			else
			{
				if(servitorTransferedDamage != null && transferedDamage > 0)
				{
					SystemMessagePacket sm = new SystemMessagePacket(SystemMsg.C1_INFLICTED_S3_DAMAGE_ON_C2_AND_S4_DAMAGE_ON_THE_DAMAGE_TRANSFER_TARGET);
					sm.addName(this);
					sm.addInteger(damage);
					sm.addName(target);
					sm.addInteger(transferedDamage);
					sm.addHpChange(target.getObjectId(), getObjectId(), -damage);
					sm.addHpChange(servitorTransferedDamage.getObjectId(), getObjectId(), -transferedDamage);
					sendPacket(sm);
				}
				else
					sendPacket(new SystemMessagePacket(SystemMsg.C1_HAS_DONE_S3_POINTS_OF_DAMAGE_TO_C2).addName(this).addName(target).addInteger(damage).addHpChange(target.getObjectId(), getObjectId(), -damage));
			}

			if(shld)
			{
				if(damage == Config.EXCELLENT_SHIELD_BLOCK_RECEIVED_DAMAGE)
				{
					if(skill != null && skill.isMagic())
					{
						sendPacket(new SystemMessagePacket(SystemMsg.C1_RESISTED_C2S_MAGIC).addName(target).addName(this));
						sendPacket(new ExMagicAttackInfo(getObjectId(), target.getObjectId(), ExMagicAttackInfo.RESISTED));
					}
				}
				else if(damage > 0)
				{
					if(skill != null && skill.isMagic())
						sendPacket(new SystemMessagePacket(SystemMsg.YOUR_OPPONENT_HAS_RESISTANCE_TO_MAGIC_THE_DAMAGE_WAS_DECREASED));
				}
			}
		}
	}

	@Override
	public void displayReceiveDamageMessage(Creature attacker, int damage, Servitor servitorTransferedDamage, int transferedDamage, int elementalDamage)
	{
		if(attacker != this)
		{
			ElementalElement element = getActiveElement();
			if(element != ElementalElement.NONE && elementalDamage != 0)
			{
				if(servitorTransferedDamage != null && transferedDamage > 0)
				{
					SystemMessagePacket sm = new SystemMessagePacket(SystemMsg.S1_HAS_DELIVERED_S3_ATTRIBUTE_DAMAGE_S5_TO_S2_S4_DAMAGE_TO_THE_DAMAGE_TRANSFERENCE_TARGET);
					sm.addName(this);
					sm.addName(attacker);
					sm.addInteger(damage);
					sm.addInteger(elementalDamage);
					sm.addInteger(transferedDamage);
					sm.addHpChange(getObjectId(), attacker.getObjectId(), -damage);
					sm.addHpChange(servitorTransferedDamage.getObjectId(), attacker.getObjectId(), -transferedDamage);
					sendPacket(sm);
				}
				else
					sendPacket(new SystemMessagePacket(SystemMsg.S1_HAS_RECEIVED_S3_ATTRIBUTE_DAMAGE_S4_FROM_S2).addName(this).addName(attacker).addInteger(damage).addInteger(elementalDamage).addHpChange(getObjectId(), attacker.getObjectId(), -damage));
			}
			else
				sendPacket(new SystemMessagePacket(SystemMsg.C1_HAS_RECEIVED_S3_DAMAGE_FROM_C2).addName(this).addName(attacker).addInteger(damage).addHpChange(getObjectId(), attacker.getObjectId(), -damage));
		}
	}

	public IntObjectMap<String> getPostFriends()
	{
		return _postFriends;
	}

	public void setPostFriends(IntObjectMap<String> val)
	{
		_postFriends = val;
	}

	public void sendReuseMessage(ItemInstance item)
	{
		TimeStamp sts = getSharedGroupReuse(item.getTemplate().getReuseGroup());
		if(sts == null || !sts.hasNotPassed())
			return;

		long timeleft = sts.getReuseCurrent();
		long hours = timeleft / 3600000;
		long minutes = (timeleft - hours * 3600000) / 60000;
		long seconds = (long) Math.max(1, Math.ceil((timeleft - hours * 3600000 - minutes * 60000) / 1000.));

		if(hours > 0)
			sendPacket(new SystemMessagePacket(item.getTemplate().getReuseType().getMessages()[2]).addItemName(item.getTemplate().getItemId()).addInteger(hours).addInteger(minutes).addInteger(seconds));
		else if(minutes > 0)
			sendPacket(new SystemMessagePacket(item.getTemplate().getReuseType().getMessages()[1]).addItemName(item.getTemplate().getItemId()).addInteger(minutes).addInteger(seconds));
		else
			sendPacket(new SystemMessagePacket(item.getTemplate().getReuseType().getMessages()[0]).addItemName(item.getTemplate().getItemId()).addInteger(seconds));
	}

	public void ask(ConfirmDlgPacket dlg, OnAnswerListener listener)
	{
		if(_askDialog != null)
			return;
		int rnd = Rnd.nextInt();
		_askDialog = new IntObjectPairImpl<OnAnswerListener>(rnd, listener);
		dlg.setRequestId(rnd);
		sendPacket(dlg);
	}

	public IntObjectPair<OnAnswerListener> getAskListener(boolean clear)
	{
		if(!clear)
			return _askDialog;
		else
		{
			IntObjectPair<OnAnswerListener> ask = _askDialog;
			_askDialog = null;
			return ask;
		}
	}

	@Override
	public boolean isDead()
	{
		return (isInOlympiadMode() || isInDuel()) ? getCurrentHp() <= 1. : super.isDead();
	}

	@Override
	public int getAgathionEnergy()
	{
		ItemInstance item = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LBRACELET);
		return item == null ? 0 : item.getAgathionEnergy();
	}

	@Override
	public void setAgathionEnergy(int val)
	{
		ItemInstance item = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LBRACELET);
		if(item == null)
			return;
		item.setAgathionEnergy(val);
		item.setJdbcState(JdbcEntityState.UPDATED);

		sendPacket(new ExBR_AgathionEnergyInfoPacket(1, List.of(new ItemInfo(this, item))));
	}

	public boolean hasPrivilege(Privilege privilege)
	{
		return _clan != null && (getClanPrivileges() & privilege.mask()) == privilege.mask();
	}

	public MatchingRoom getMatchingRoom()
	{
		return _matchingRoom;
	}

	public void setMatchingRoom(MatchingRoom matchingRoom)
	{
		_matchingRoom = matchingRoom;
		if(matchingRoom == null)
			_matchingRoomWindowOpened = false;
	}

	public boolean isMatchingRoomWindowOpened()
	{
		return _matchingRoomWindowOpened;
	}

	public void setMatchingRoomWindowOpened(boolean b)
	{
		_matchingRoomWindowOpened = b;
	}

	public void dispelBuffs()
	{
		for(Abnormal e : getAbnormalList())
		{
			if(e.isOffensive() && e.getSkill().getBuffProtectLevel() == 0 && e.isCancelable() && !e.getSkill().isPreservedOnDeath() && !isSpecialAbnormal(e.getSkill()))
			{
				sendPacket(new SystemMessagePacket(SystemMsg.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED).addSkillName(e.getSkill().getId(), e.getSkill().getLevel()));
				e.exit();
			}
		}

		for(Servitor servitor : getServitors())
		{
			for(Abnormal e : servitor.getAbnormalList())
			{
				if(!e.isOffensive() && e.getSkill().getBuffProtectLevel() == 0 && e.isCancelable() && !e.getSkill().isPreservedOnDeath() && !servitor.isSpecialAbnormal(e.getSkill()))
					e.exit();
			}
		}
	}

	public void setInstanceReuse(int id, long time, boolean notify)
	{
		if(notify)
			sendPacket(new SystemMessage(SystemMessage.INSTANT_ZONE_FROM_HERE__S1_S_ENTRY_HAS_BEEN_RESTRICTED_YOU_CAN_CHECK_THE_NEXT_ENTRY_POSSIBLE).addString(getName()));
		_instancesReuses.put(id, time);
		mysql.set("REPLACE INTO character_instances (obj_id, id, reuse) VALUES (?,?,?)", getObjectId(), id, time);
	}

	public void removeInstanceReuse(int id)
	{
		if(_instancesReuses.remove(id) != null)
			mysql.set("DELETE FROM `character_instances` WHERE `obj_id`=? AND `id`=? LIMIT 1", getObjectId(), id);
	}

	public void removeAllInstanceReuses()
	{
		_instancesReuses.clear();
		mysql.set("DELETE FROM `character_instances` WHERE `obj_id`=?", getObjectId());
	}

	public void removeInstanceReusesByGroupId(int groupId)
	{
		for(int i : InstantZoneHolder.getInstance().getSharedReuseInstanceIdsByGroup(groupId))
			if(getInstanceReuse(i) != null)
				removeInstanceReuse(i);
	}

	public Long getInstanceReuse(int id)
	{
		return _instancesReuses.get(id);
	}

	public Map<Integer, Long> getInstanceReuses()
	{
		return _instancesReuses;
	}

	private void loadInstanceReuses()
	{
		Connection con = null;
		PreparedStatement offline = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			offline = con.prepareStatement("SELECT * FROM character_instances WHERE obj_id = ?");
			offline.setInt(1, getObjectId());
			rs = offline.executeQuery();
			while(rs.next())
			{
				int id = rs.getInt("id");
				long reuse = rs.getLong("reuse");
				_instancesReuses.put(id, reuse);
			}
		}
		catch(Exception e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "" );
		}
		finally
		{
			DbUtils.closeQuietly(con, offline, rs);
		}
	}

	public void setActiveReflection(Reflection reflection)
	{
		_activeReflection = reflection;
	}

	public Reflection getActiveReflection()
	{
		return _activeReflection;
	}

	public boolean canEnterInstance(int instancedZoneId)
	{
		InstantZone iz = InstantZoneHolder.getInstance().getInstantZone(instancedZoneId);

		if(isDead())
			return false;

		if(ReflectionManager.getInstance().size() > Config.MAX_REFLECTIONS_COUNT)
		{
			sendPacket(SystemMsg.THE_MAXIMUM_NUMBER_OF_INSTANCE_ZONES_HAS_BEEN_EXCEEDED);
			return false;
		}

		if(iz == null)
		{
			sendPacket(SystemMsg.SYSTEM_ERROR);
			return false;
		}

		if(ReflectionManager.getInstance().getCountByIzId(instancedZoneId) >= iz.getMaxChannels())
		{
			sendPacket(SystemMsg.THE_MAXIMUM_NUMBER_OF_INSTANCE_ZONES_HAS_BEEN_EXCEEDED);
			return false;
		}

		return iz.getEntryType(this).canEnter(this, iz);
	}

	public boolean canReenterInstance(int instancedZoneId)
	{
		if(getActiveReflection() != null && getActiveReflection().getInstancedZoneId() != instancedZoneId || !getReflection().isMain())
		{
			sendPacket(SystemMsg.YOU_HAVE_ENTERED_ANOTHER_INSTANCE_ZONE_THEREFORE_YOU_CANNOT_ENTER_CORRESPONDING_DUNGEON);
			return false;
		}

		InstantZone iz = InstantZoneHolder.getInstance().getInstantZone(instancedZoneId);
		if(iz.isDispelBuffs())
			dispelBuffs();

		return iz.getEntryType(this).canReEnter(this, iz);
	}

	public int getBattlefieldChatId()
	{
		return _battlefieldChatId;
	}

	public void setBattlefieldChatId(int battlefieldChatId)
	{
		_battlefieldChatId = battlefieldChatId;
	}

	@Override
	public void broadCast(IBroadcastPacket... packet)
	{
		sendPacket(packet);
	}

	@Override
	public int getMemberCount()
	{
		return 1;
	}

	@Override
	public Player getGroupLeader()
	{
		return this;
	}

	@Override
	public Iterator<Player> iterator()
	{
		return Collections.singleton(this).iterator();
	}

	public PlayerGroup getPlayerGroup()
	{
		if(getParty() != null)
		{
			if(getParty().getCommandChannel() != null)
				return getParty().getCommandChannel();
			else
				return getParty();
		}
		else
			return this;
	}

	public boolean isActionBlocked(String action)
	{
		return _blockedActions.contains(action);
	}

	public void blockActions(String... actions)
	{
		Collections.addAll(_blockedActions, actions);
	}

	public void unblockActions(String... actions)
	{
		for(String action : actions)
			_blockedActions.remove(action);
	}

	public OlympiadGame getOlympiadGame()
	{
		return _olympiadGame;
	}

	public void setOlympiadGame(OlympiadGame olympiadGame)
	{
		_olympiadGame = olympiadGame;
	}

	public void addRadar(int x, int y, int z)
	{
		sendPacket(new RadarControlPacket(0, 1, x, y, z));
	}

	public void addRadarWithMap(int x, int y, int z)
	{
		sendPacket(new RadarControlPacket(0, 2, x, y, z));
	}

	public PetitionMainGroup getPetitionGroup()
	{
		return _petitionGroup;
	}

	public void setPetitionGroup(PetitionMainGroup petitionGroup)
	{
		_petitionGroup = petitionGroup;
	}

	public int getLectureMark()
	{
		return _lectureMark;
	}

	public void setLectureMark(int lectureMark)
	{
		_lectureMark = lectureMark;
	}

	public boolean isUserRelationActive()
	{
		return _enableRelationTask == null;
	}

	public void startEnableUserRelationTask(long time, SiegeEvent<?, ?> siegeEvent)
	{
		if(_enableRelationTask != null)
			return;

		_enableRelationTask = ThreadPoolManager.getInstance().schedule(new EnableUserRelationTask(this, siegeEvent), time);
	}

	public void stopEnableUserRelationTask()
	{
		if(_enableRelationTask != null)
		{
			_enableRelationTask.cancel(false);
			_enableRelationTask = null;
		}
	}

	public void broadcastRelation()
	{
		if(!isVisible())
			return;

		for(Player target : World.getAroundObservers(this))
		{
			if(isInvisible(target))
				continue;

			RelationChangedPacket relationChanged = new RelationChangedPacket(this, target);

			for(Servitor servitor : getServitors())
				relationChanged.add(servitor, target);

			target.sendPacket(relationChanged);
		}
	}

	public BookMarkList getBookMarkList()
	{
		return _bookmarks;
	}

	public AntiFlood getAntiFlood()
	{
		return _antiFlood;
	}

	public int getNpcDialogEndTime()
	{
		return _npcDialogEndTime;
	}

	public void setNpcDialogEndTime(int val)
	{
		_npcDialogEndTime = val;
	}

	@Override
	public boolean useItem(ItemInstance item, boolean ctrl, boolean sendMsg)
	{
		if(!_isUsingItem.compareAndSet(false, true))
			return false;

		try
		{
			if(!ItemFunctions.checkForceUseItem(this, item, sendMsg))
				return false;

			ItemTemplate template = item.getTemplate();

			if(template.useItem(this, item, ctrl, true)) // force use
				return true;

			if(!ItemFunctions.checkUseItem(this, item, sendMsg))
				return false;

			if(template.useItem(this, item, ctrl, false))
			{
				long nextTimeUse = template.getReuseType().next(item);
				if(nextTimeUse > System.currentTimeMillis())
				{
					TimeStamp timeStamp = new TimeStamp(item.getItemId(), nextTimeUse, template.getReuseDelay());
					addSharedGroupReuse(template.getReuseGroup(), timeStamp);
					if (item.getCount() > 1)
						sendPacket(new InventoryUpdatePacket().addModifiedItem(this, item));

					if(template.getReuseDelay() > 0)
						sendPacket(new ExUseSharedGroupItem(template.getDisplayReuseGroup(), timeStamp));
				}
				return true;
			}
		}
		finally
		{
			_isUsingItem.set(false);
		}
		return false;
	}

	public Location getStablePoint()
	{
		return _stablePoint;
	}

	public void setStablePoint(Location point)
	{
		_stablePoint = point;
	}

	public boolean isInSameParty(Player target)
	{
		return getParty() != null && target.getParty() != null && getParty() == target.getParty();
	}

	public boolean isInSameChannel(Player target)
	{
		Party activeCharP = getParty();
		Party targetP = target.getParty();
		if(activeCharP != null && targetP != null)
		{
			CommandChannel chan = activeCharP.getCommandChannel();
			if(chan != null && chan == targetP.getCommandChannel())
				return true;
		}
		return false;
	}

	public boolean isInSameClan(Player target)
	{
		return getClanId() != 0 && getClanId() == target.getClanId();
	}

	public final boolean isInSameAlly(Player target)
	{
		return getAllyId() != 0 && getAllyId() == target.getAllyId();
	}

	public boolean isInPvPEvent()
	{
		PvPEvent event = getEvent(PvPEvent.class);
		if(event != null && event.isBattleActive())
			return true;
		return false;
	}

	public boolean isRelatedTo(Creature character)
	{
		if(character == this)
			return true;

		if(character.isServitor())
		{
			if(isMyServitor(character.getObjectId()))
				return true;
			else if(character.getPlayer() != null)
			{
				Player Spc = character.getPlayer();
				if(isInSameParty(Spc) || isInSameChannel(Spc) || isInSameClan(Spc) || isInSameAlly(Spc))
					return true;
			}
		}
		else if(character.isPlayer())
		{
			Player pc = character.getPlayer();
			if(isInSameParty(pc) || isInSameChannel(pc) || isInSameClan(pc) || isInSameAlly(pc))
				return true;
		}
		return false;
	}

	public boolean isAutoSearchParty()
	{
		return _autoSearchParty;
	}

	public void enableAutoSearchParty()
	{
		_autoSearchParty = true;
		PartySubstituteManager.getInstance().addWaitingPlayer(this);
		sendPacket(ExWaitWaitingSubStituteInfo.OPEN);
	}

	public void disablePartySearch(boolean disableFlag)
	{
		if(_autoSearchParty)
		{
			PartySubstituteManager.getInstance().removeWaitingPlayer(this);
			sendPacket(ExWaitWaitingSubStituteInfo.CLOSE);
			_autoSearchParty = !disableFlag;
		}
	}

	public boolean refreshPartySearchStatus(boolean sendMsg)
	{
		if(!mayPartySearch(false, sendMsg))
		{
			disablePartySearch(false);
			return false;
		}

		if(isAutoSearchParty())
		{
			enableAutoSearchParty();
			return true;
		}
		return false;
	}

	public boolean mayPartySearch(boolean first, boolean msg)
	{
		if(getParty() != null)
			return false;

		if(isPK())
		{
			if(msg)
			{
				if(first)
					sendPacket(SystemMsg.WAITING_LIST_REGISTRATION_IS_NOT_ALLOWED_WHILE_THE_CURSED_SWORD_IS_BEING_USED_OR_THE_STATUS_IS_IN_A_CHAOTIC_STATE);
				else
					sendPacket(SystemMsg.WAITING_LIST_REGISTRATION_IS_CANCELLED_BECAUSE_THE_CURSED_SWORD_IS_BEING_USED_OR_THE_STATUS_IS_IN_A_CHAOTIC_STATE);
			}
			return false;
		}

		if(isInDuel() && getTeam() != TeamType.NONE)
		{
			if(msg)
			{
				if(first)
					sendPacket(SystemMsg.YOU_CANNOT_REGISTER_IN_THE_WAITING_LIST_DURING_A_DUEL);
				else
					sendPacket(SystemMsg.WAITING_LIST_REGISTRATION_IS_CANCELLED_BECAUSE_YOU_ARE_IN_A_DUEL);
			}
			return false;
		}

		if(isInOlympiadMode())
		{
			if(msg)
			{
				if(first)
					sendPacket(SystemMsg.YOU_CANNOT_REGISTER_IN_THE_WAITING_LIST_WHILE_PARTICIPATING_IN_OLYMPIAD);
				else
					sendPacket(SystemMsg.WAITING_LIST_REGISTRATION_IS_CANCELLED_BECAUSE_YOU_ARE_CURRENTLY_PARTICIPATING_IN_OLYMPIAD);
			}
			return false;
		}

		if(isInSiegeZone())
		{
			if(msg && first)
				sendPacket(SystemMsg.YOU_CANNOT_REGISTER_IN_THE_WAITING_LIST_WHILE_BEING_INSIDE_OF_A_BATTLEGROUND_CASTLE_SIEGEFORTRESS_SIEGETERRITORY_WAR);

			return false;
		}

		if(isInZoneBattle() || getReflectionId() != 0)
		{
			if(msg && first)
				sendPacket(SystemMsg.YOU_CANNOT_REGISTER_IN_THE_WAITING_LIST_WHILE_PARTICIPATING_IN_BLOCK_CHECKERCOLISEUMKRATEIS_CUBE);

			return false;
		}

		if(isInZone(ZoneType.no_escape) || isInZone(ZoneType.epic))
			return false;

		if(!Config.ENABLE_PARTY_SEARCH)
			return false;
		return true;
	}

	public void startSubstituteTask()
	{
		if(!isPartySubstituteStarted())
		{
			_substituteTask = PartySubstituteManager.getInstance().SubstituteSearchTask(this);
			sendUserInfo();
			if(isInParty())
				getParty().getPartyLeader().sendPacket(new PartySmallWindowUpdatePacket(this));
		}
	}

	public void stopSubstituteTask()
	{
		if(isPartySubstituteStarted())
		{
			PartySubstituteManager.getInstance().removePartyMember(this);
			_substituteTask.cancel(true);
			sendUserInfo();
			if(isInParty())
				getParty().getPartyLeader().sendPacket(new PartySmallWindowUpdatePacket(this));
		}
	}

	public boolean isPartySubstituteStarted()
	{
		return getParty() != null && _substituteTask != null && !_substituteTask.isDone() && !_substituteTask.isCancelled();
	}

	@Override
	public int getSkillLevel(int skillId)
	{
		switch(skillId)
		{
			case 1566: // ?????????? ????????????
			case 1567: // ?????????? ????????????
			case 1568: // ?????????? ????????????
			case 1569: // ?????????? ????????????
			case 17192: // ?????????????????????? ?????????????????? ??????????
				return 1;
		}
		return super.getSkillLevel(skillId);
	}

	public SymbolInstance getSymbol()
	{
		return _symbol;
	}

	public void setSymbol(SymbolInstance symbol)
	{
		_symbol = symbol;
	}

	public void setRegisteredInEvent(boolean inEvent)
	{
		_registeredInEvent = inEvent;
	}

	public boolean isRegisteredInEvent()
	{
		return _registeredInEvent;
	}

	private boolean checkActiveToggleEffects()
	{
		boolean dispelled = false;
		for(Abnormal effect : getAbnormalList())
		{
			Skill skill = effect.getSkill();
			if(skill == null)
				continue;

			if(!skill.isToggle())
				continue;

			if(getAllSkills().contains(skill))
				continue;

			effect.exit();
		}
		return dispelled;
	}

	@Override
	public Servitor getServitorForTransfereDamage(double transferDamage)
	{
		SummonInstance summon = getSummon();
		if(summon == null || summon.isDead())
			return null; //try next summon

		if(summon.isInRangeZ(this, 1200))
			return summon;

		//getAbnormalList().stop("AbsorbDamageToSummon"); ???? ???????? ???????????? ???? ????????????????????.
		return null;
	}

	@Override
	public double getDamageForTransferToServitor(double damage)
	{
		double transferToSummonDam = getStat().getValue(DoubleStat.TRANSFER_DAMAGE_SUMMON_PERCENT, 0.0);
		return damage * transferToSummonDam / 100.0;
	}

	@Override
	public double reduceDamageByMp(Creature attacker, double damage)
	{
		if(damage == 0)
			return 0;

		int mpDam = (int) (((int) damage * (int) getStat().getValue(DoubleStat.MANA_SHIELD_PERCENT, 0)) / 100.0);

		if (mpDam > 0)
		{
			mpDam = (int) (damage - mpDam);
			if (mpDam > getCurrentMp())
			{
				sendPacket(SystemMsg.MP_BECAME_0_AND_THE_ARCANE_SHIELD_IS_DISAPPEARING);
				getAbnormalList().stop(AbnormalType.MP_SHIELD);
				damage = mpDam - getCurrentMp();
				setCurrentMp(0);
			}
			else if (!isHpBlocked())
			{
				reduceCurrentMp(mpDam, null);
				sendPacket(new SystemMessagePacket(SystemMsg.ARCANE_SHIELD_DECREASED_YOUR_MP_BY_S1_INSTEAD_OF_HP).addInteger(mpDam));
				return 0.0;
			}
		}

		return damage;
	}

	@Override
	public double transferDamage(Creature attacker, double damage) {
		final Player caster = getTransferingDamageTo();
		Party party = getParty();
		if (caster != null && party != null && isInRangeZ(caster, 1000) && !caster.isDead() && this != caster && party.containsMember(caster))
		{
			double transferDmg = ((int) damage * (int) getStat().getValue(DoubleStat.TRANSFER_DAMAGE_TO_PLAYER, 0)) / 100.0;
			transferDmg = Math.min((int) caster.getCurrentHp() - 1, transferDmg);
			if (transferDmg > 0)
			{
				int membersInRange = 0;
				for (Player member : party)
				{
					if (member.isInRange(caster, 1000) && !member.equals(caster))
					{
						membersInRange++;
					}
				}

				if (attacker.isPlayable() && caster.getCurrentCp() > 0)
				{
					if (caster.getCurrentCp() > transferDmg)
					{
						caster.reduceCurrentCp((int) transferDmg);
					}
					else
					{
						transferDmg = (int) (transferDmg - caster.getCurrentCp());
						caster.reduceCurrentCp((int) caster.getCurrentCp());
					}
				}

				if (membersInRange > 0)
				{
					caster.reduceCurrentHp(transferDmg / membersInRange, attacker, null);
					damage -= transferDmg;
				}
			}
		}

		return damage;
	}


	public boolean canFixedRessurect()
	{
		if(getPlayerAccess().ResurectFixed)
			return true;

		if(!isInSiegeZone())
		{
			if(getInventory().getCountOf(10649) > 0)
				return true;
		}

		return false;
	}

	@Override
	public double getLevelBonus()
	{
		if(getTransform() != null && getTransform().getLevelBonus(getLevel()) > 0)
			return getTransform().getLevelBonus(getLevel());

		return super.getLevelBonus();
	}

	@Override
	public PlayerBaseStats getBaseStats()
	{
		if(_baseStats == null)
			_baseStats = new PlayerBaseStats(this);
		return (PlayerBaseStats) _baseStats;
	}

	@Override
	public PlayerStat getStat()
	{
		if(_stat == null)
			_stat = new PlayerStat(this);
		return (PlayerStat) _stat;
	}

	@Override
	public PlayerFlags getFlags()
	{
		if(_statuses == null)
			_statuses = new PlayerFlags(this);
		return (PlayerFlags) _statuses;
	}

	@Override
	public final String getVisibleName(Player receiver)
	{
		String name;
		for(Event event : getEvents())
		{
			name = event.getVisibleName(this, receiver);
			if(name != null)
				return name;
		}
		return getName();
	}

	@Override
	public final String getVisibleTitle(Player receiver)
	{
		if(isInBuffStore())
		{
			BufferData bufferData = OfflineBufferManager.getInstance().getBuffStore(getObjectId());
			if(bufferData != null)
				return bufferData.getSaleTitle();
		}

		if(getPrivateStoreType() != STORE_PRIVATE_NONE)
		{
			if(getReflection() == ReflectionManager.GIRAN_HARBOR)
				return "";

			if(getReflection() == ReflectionManager.PARNASSUS)
				return "";
		}

		if(isInAwayingMode())
		{
			String awayText = AwayManager.getInstance().getAwayText(this);
			// TODO: ?????????????? ?? ???? ??????????????????.
			if(awayText == null || awayText.length() <= 1)
				return isLangRus() ? "<????????????>" : "<Away>";
			else
				return (isLangRus() ? "<????????????>" : "<Away>") + " - " + awayText + "*";
		}

		String title;
		for(Event event : getEvents())
		{
			title = event.getVisibleTitle(this, receiver);
			if(title != null)
				return title;
		}
		return getTitle();
	}

	public final int getVisibleNameColor(Player receiver)
	{
		if(isInBuffStore())
		{
			BufferData bufferData = OfflineBufferManager.getInstance().getBuffStore(getObjectId());
			if(bufferData != null)
			{
				if(isInOfflineMode())
					return Config.BUFF_STORE_OFFLINE_NAME_COLOR;
				return Config.BUFF_STORE_NAME_COLOR;
			}
		}

		if(isInStoreMode())
		{
			if(isInOfflineMode())
				return Config.SERVICES_OFFLINE_TRADE_NAME_COLOR;
		}

		Integer color;
		for(Event event : getEvents())
		{
			color = event.getVisibleNameColor(this, receiver);
			if(color != null)
				return color.intValue();
		}

		int premiumNameColor = getPremiumAccount().getNameColor();
		if(premiumNameColor != -1)
			return premiumNameColor;

		int vipNameColor = getVIP().getTemplate().getNameColor();
		if(vipNameColor != -1)
			return vipNameColor;

		return getNameColor();
	}

	public final int getVisibleTitleColor(Player receiver)
	{
		if(isInBuffStore())
		{
			BufferData bufferData = OfflineBufferManager.getInstance().getBuffStore(getObjectId());
			if(bufferData != null)
			{
				if(!isInOfflineMode())
					return Config.BUFF_STORE_TITLE_COLOR;
			}
		}

		if(isInAwayingMode())
			return Config.AWAY_TITLE_COLOR;

		Integer color;
		for(Event event : getEvents())
		{
			color = event.getVisibleTitleColor(this, receiver);
			if(color != null)
				return color.intValue();
		}

		int premiumTitleColor = getPremiumAccount().getTitleColor();
		if(premiumTitleColor != -1)
			return premiumTitleColor;

		int vipTitleColor = getVIP().getTemplate().getTitleColor();
		if(vipTitleColor != -1)
			return vipTitleColor;

		return getTitleColor();
	}

	public final boolean isPledgeVisible(Player receiver)
	{
		if(getPrivateStoreType() != STORE_PRIVATE_NONE)
		{
			if(getReflection() == ReflectionManager.GIRAN_HARBOR)
				return false;

			if(getReflection() == ReflectionManager.PARNASSUS)
				return false;
		}

		for(Event event : getEvents())
		{
			if(!event.isPledgeVisible(this, receiver))
				return false;
		}
		return true;
	}

	public void checkAndDeleteOlympiadItems()
	{
		if(!isHero())
		{
			ItemFunctions.deleteItemsEverywhere(this, ItemTemplate.ITEM_ID_HERO_WING); // TODO: ???????????? ???? ???????????????
			int rank = Olympiad.getRank(this);
			if(rank != 2 && rank != 3)
				ItemFunctions.deleteItemsEverywhere(this, ItemTemplate.ITEM_ID_FAME_CLOAK);

			for(int itemId : ItemTemplate.HERO_WEAPON_IDS)
				ItemFunctions.deleteItemsEverywhere(this, itemId);
		}
	}

	@Override
	public boolean isSpecialAbnormal(Skill skill)
	{
		if(getClan() != null && getClan().isSpecialAbnormal(skill))
			return true;

		if(skill.isNecessaryToggle())
			return true;

		int skillId = skill.getId();
		if(skillId == 7008 || skillId == 6038 || skillId == 6039 || skillId == 6040 || skillId == 6055 || skillId == 6056 || skillId == 6057 || skillId == 6058 || skillId == 51152) // TODO[Bonux]: ?????????????? ?? ???????????????
			return true;

		return false;
	}

	@Override
	public void removeAllSkills()
	{
		_dontRewardSkills = true;

		super.removeAllSkills();

		_dontRewardSkills = false;
	}

	public void setLastMultisellBuyTime(long val)
	{
		_lastMultisellBuyTime = val;
	}

	public long getLastMultisellBuyTime()
	{
		return _lastMultisellBuyTime;
	}

	public void setLastEnchantItemTime(long val)
	{
		_lastEnchantItemTime = val;
	}

	public long getLastEnchantItemTime()
	{
		return _lastEnchantItemTime;
	}

	public void setLastAttributeItemTime(long val)
	{
		_lastAttributeItemTime = val;
	}

	public long getLastAttributeItemTime()
	{
		return _lastAttributeItemTime;
	}

	public void checkLevelUpReward(boolean onRestore)
	{
		int lastRewarded = getVarInt(LVL_UP_REWARD_VAR);
		int lastRewardedByClass = getVarInt(LVL_UP_REWARD_VAR + "_" + getActiveSubClass().getIndex());
		int playerLvl = getLevel();

		boolean rewarded = false;

		int clanPoints = 0;

		if(playerLvl > lastRewarded)
		{
			for(int i = playerLvl; i > lastRewarded; i--)
			{
				TIntLongMap items = LevelUpRewardHolder.getInstance().getRewardData(i);
				if(items != null)
				{
					for(TIntLongIterator iterator = items.iterator(); iterator.hasNext();)
					{
						iterator.advance();
						getPremiumItemList().add(new PremiumItem(iterator.key(), iterator.value(), ""));
						rewarded = true;
					}
				}
			}

			setVar(LVL_UP_REWARD_VAR, playerLvl);
		}

		if(playerLvl > lastRewardedByClass)
		{
			for(int i = playerLvl; i > lastRewardedByClass; i--)
			{
				if(getClan() != null && getClan().getLevel() >= 3)
				{
					int earnedPoints = 0;
					if (i >= 20 && i <= 25)
						earnedPoints = 4;
					else if (i >= 26 && i <= 30)
						earnedPoints = 8;
					else if (i >= 31 && i <= 35)
						earnedPoints = 12;
					else if (i >= 36 && i <= 40)
						earnedPoints = 16;
					else if (i >= 41 && i <= 45)
						earnedPoints = 25;
					else if (i >= 46 && i <= 50)
						earnedPoints = 30;
					else if (i >= 51 && i <= 55)
						earnedPoints = 35;
					else if (i >= 56 && i <= 60)
						earnedPoints = 40;
					else if (i >= 61 && i <= 65)
						earnedPoints = 54;
					else if (i >= 66 && i <= 70)
						earnedPoints = 63;
					else if (i >= 71 && i <= 75)
						earnedPoints = 75;
					else if (i >= 76 && i <= 80)
						earnedPoints = 90;
					else if (i >= 81)
						earnedPoints = 120;

					if (earnedPoints > 0) {
						clanPoints += earnedPoints;
					}
				}
			}

			setVar(LVL_UP_REWARD_VAR + "_" + getActiveSubClass().getIndex(), playerLvl);
		}

		if(rewarded)
			sendPacket(ExNotifyPremiumItem.STATIC);

		if(clanPoints > 0)
			getClan().incReputation(clanPoints, true, "ClanMemberLvlUp");
	}

	public void checkHeroSkills()
	{
		final boolean hero = isHero() && isBaseClassActive();
		for(SkillLearn sl : SkillAcquireHolder.getInstance().getAvailableMaxLvlSkills(hero ? this : null, AcquireType.HERO))
		{
			SkillEntry skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.NONE, sl.getId(), sl.getLevel());
			if(skillEntry == null)
				continue;

			if(hero)
			{
				if(getSkillLevel(skillEntry.getId()) < skillEntry.getLevel())
					addSkill(skillEntry, true);
			}
			else
				removeSkill(skillEntry, true);
		}
	}

	public void activateHeroSkills(boolean activate)
	{
		for(SkillLearn sl : SkillAcquireHolder.getInstance().getAvailableMaxLvlSkills(null, AcquireType.HERO))
		{
			Skill skill = SkillHolder.getInstance().getSkill(sl.getId(), sl.getLevel());
			if(skill == null)
				continue;

			if(!activate)
				addUnActiveSkill(skill);
			else
				removeUnActiveSkill(skill);
		}
	}

	public void giveGMSkills()
	{
		if(!isGM())
			return;

		for(SkillLearn sl : SkillAcquireHolder.getInstance().getAvailableMaxLvlSkills(this, AcquireType.GM))
		{
			SkillEntry skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.NONE, sl.getId(), sl.getLevel());
			if(skillEntry == null)
				continue;

			if(getSkillLevel(skillEntry.getId()) < skillEntry.getLevel())
				addSkill(skillEntry, true);
		}
	}

	private long _blockUntilTime = 0;

	public void setblockUntilTime(long time)
	{
		_blockUntilTime = time;
	}

	public long getblockUntilTime()
	{
		return _blockUntilTime;
	}

	public int getWorldChatPoints()
	{
		int points = (hasPremiumAccount() ? Config.WORLD_CHAT_POINTS_PER_DAY_PA : Config.WORLD_CHAT_POINTS_PER_DAY) - _usedWorldChatPoints;
		points = (int) getStat().getValue(DoubleStat.WORLD_CHAT_POINTS, points);
		points = Math.max(0, points);
		return points;
	}

	public int getUsedWorldChatPoints()
	{
		return _usedWorldChatPoints;
	}

	public void setUsedWorldChatPoints(int value)
	{
		_usedWorldChatPoints = value;
	}

	public int getArmorSetEnchant()
	{
		return _armorSetEnchant;
	}

	public void setArmorSetEnchant(int value)
	{
		_armorSetEnchant = value;
	}

	public boolean hideHeadAccessories()
	{
		return _hideHeadAccessories;
	}

	public void setHideHeadAccessories(boolean value)
	{
		_hideHeadAccessories = value;
	}

	public ItemInstance getSynthesisItem1()
	{
		return _synthesisItem1;
	}

	public void setSynthesisItem1(ItemInstance value)
	{
		_synthesisItem1 = value;
	}

	public ItemInstance getSynthesisItem2()
	{
		return _synthesisItem2;
	}

	public void setSynthesisItem2(ItemInstance value)
	{
		_synthesisItem2 = value;
	}

	private static final int[] ADDITIONAL_SS_EFFECTS = new int[]
	{
		70455, // Ruby - Lv. 5
		70454, // Ruby - Lv. 4
		70453, // Ruby - Lv. 3
		70452, // Ruby - Lv. 2
		70451, // Ruby - Lv. 1
		90332, // Ruby - Lv. 5
		90331, // Ruby - Lv. 4
		90330, // Ruby - Lv. 3
		90329, // Ruby - Lv. 2
		90328, // Ruby - Lv. 1
		70460, // Sapphire - Lv. 5
		70459, // Sapphire - Lv. 4
		70458, // Sapphire - Lv. 3
		70457, // Sapphire - Lv. 2
		70456, // Sapphire - Lv. 1
		90337, // Sapphire - Lv. 5
		90336, // Sapphire - Lv. 4
		90335, // Sapphire - Lv. 3
		90334, // Sapphire - Lv. 2
		90333 // Sapphire - Lv. 1
	};

	@Override
	public int getAdditionalVisualSSEffect()
	{
		for(int id : ADDITIONAL_SS_EFFECTS)
		{
			if(ItemFunctions.checkIsEquipped(this, -1, id, 0))
				return id;
		}
		return 0;
	}

	@Override
	public SkillEntry getAdditionalSSEffect(boolean spiritshot, boolean blessed)
	{
		if(!spiritshot)
		{
			if(!blessed)
			{
				// Ruby's
				if(ItemFunctions.checkIsEquipped(this, -1, 70455, 0)) // Ruby - Lv. 5
					return SkillEntry.makeSkillEntry(SkillEntryType.CUNSUMABLE_ITEM, 17817, 1);
				if(ItemFunctions.checkIsEquipped(this, -1, 70454, 0)) // Ruby - Lv. 4
					return SkillEntry.makeSkillEntry(SkillEntryType.CUNSUMABLE_ITEM, 17816, 1);
				if(ItemFunctions.checkIsEquipped(this, -1, 70453, 0)) // Ruby - Lv. 3
					return SkillEntry.makeSkillEntry(SkillEntryType.CUNSUMABLE_ITEM, 17815, 1);
				if(ItemFunctions.checkIsEquipped(this, -1, 70452, 0)) // Ruby - Lv. 2
					return SkillEntry.makeSkillEntry(SkillEntryType.CUNSUMABLE_ITEM, 17814, 2);
				if(ItemFunctions.checkIsEquipped(this, -1, 70451, 0)) // Ruby - Lv. 1
					return SkillEntry.makeSkillEntry(SkillEntryType.CUNSUMABLE_ITEM, 17814, 1);
				//
				if(ItemFunctions.checkIsEquipped(this, -1, 90332, 0)) // Ruby - Lv. 5
					return SkillEntry.makeSkillEntry(SkillEntryType.CUNSUMABLE_ITEM, 17817, 1);
				if(ItemFunctions.checkIsEquipped(this, -1, 90331, 0)) // Ruby - Lv. 4
					return SkillEntry.makeSkillEntry(SkillEntryType.CUNSUMABLE_ITEM, 17816, 1);
				if(ItemFunctions.checkIsEquipped(this, -1, 90330, 0)) // Ruby - Lv. 3
					return SkillEntry.makeSkillEntry(SkillEntryType.CUNSUMABLE_ITEM, 17815, 1);
				if(ItemFunctions.checkIsEquipped(this, -1, 90329, 0)) // Ruby - Lv. 2
					return SkillEntry.makeSkillEntry(SkillEntryType.CUNSUMABLE_ITEM, 17814, 2);
				if(ItemFunctions.checkIsEquipped(this, -1, 90328, 0)) // Ruby - Lv. 1
					return SkillEntry.makeSkillEntry(SkillEntryType.CUNSUMABLE_ITEM, 17814, 1);
			}
			else
			{
				// Ruby's
				if(ItemFunctions.checkIsEquipped(this, -1, 70455, 0)) // Ruby - Lv. 5
					return SkillEntry.makeSkillEntry(SkillEntryType.CUNSUMABLE_ITEM, 17817, 2);
				if(ItemFunctions.checkIsEquipped(this, -1, 70454, 0)) // Ruby - Lv. 4
					return SkillEntry.makeSkillEntry(SkillEntryType.CUNSUMABLE_ITEM, 17816, 2);
				if(ItemFunctions.checkIsEquipped(this, -1, 70453, 0)) // Ruby - Lv. 3
					return SkillEntry.makeSkillEntry(SkillEntryType.CUNSUMABLE_ITEM, 17815, 2);
				if(ItemFunctions.checkIsEquipped(this, -1, 70452, 0)) // Ruby - Lv. 2
					return SkillEntry.makeSkillEntry(SkillEntryType.CUNSUMABLE_ITEM, 17814, 4);
				if(ItemFunctions.checkIsEquipped(this, -1, 70451, 0)) // Ruby - Lv. 1
					return SkillEntry.makeSkillEntry(SkillEntryType.CUNSUMABLE_ITEM, 17814, 3);
				//
				if(ItemFunctions.checkIsEquipped(this, -1, 90332, 0)) // Ruby - Lv. 5
					return SkillEntry.makeSkillEntry(SkillEntryType.CUNSUMABLE_ITEM, 17817, 2);
				if(ItemFunctions.checkIsEquipped(this, -1, 90331, 0)) // Ruby - Lv. 4
					return SkillEntry.makeSkillEntry(SkillEntryType.CUNSUMABLE_ITEM, 17816, 2);
				if(ItemFunctions.checkIsEquipped(this, -1, 90330, 0)) // Ruby - Lv. 3
					return SkillEntry.makeSkillEntry(SkillEntryType.CUNSUMABLE_ITEM, 17815, 2);
				if(ItemFunctions.checkIsEquipped(this, -1, 90329, 0)) // Ruby - Lv. 2
					return SkillEntry.makeSkillEntry(SkillEntryType.CUNSUMABLE_ITEM, 17814, 4);
				if(ItemFunctions.checkIsEquipped(this, -1, 90328, 0)) // Ruby - Lv. 1
					return SkillEntry.makeSkillEntry(SkillEntryType.CUNSUMABLE_ITEM, 17814, 3);
			}
		}
		else
		{
			if(!blessed)
			{
				// Sapphire's
				if(ItemFunctions.checkIsEquipped(this, -1, 70460, 0)) // Sapphire - Lv. 5
					return SkillEntry.makeSkillEntry(SkillEntryType.CUNSUMABLE_ITEM, 17821, 1);
				if(ItemFunctions.checkIsEquipped(this, -1, 70459, 0)) // Sapphire - Lv. 4
					return SkillEntry.makeSkillEntry(SkillEntryType.CUNSUMABLE_ITEM, 17820, 1);
				if(ItemFunctions.checkIsEquipped(this, -1, 70458, 0)) // Sapphire - Lv. 3
					return SkillEntry.makeSkillEntry(SkillEntryType.CUNSUMABLE_ITEM, 17819, 1);
				if(ItemFunctions.checkIsEquipped(this, -1, 70457, 0)) // Sapphire - Lv. 2
					return SkillEntry.makeSkillEntry(SkillEntryType.CUNSUMABLE_ITEM, 17818, 2);
				if(ItemFunctions.checkIsEquipped(this, -1, 70456, 0)) // Sapphire - Lv. 1
					return SkillEntry.makeSkillEntry(SkillEntryType.CUNSUMABLE_ITEM, 17818, 1);
				//
				if(ItemFunctions.checkIsEquipped(this, -1, 90337, 0)) // Sapphire - Lv. 5
					return SkillEntry.makeSkillEntry(SkillEntryType.CUNSUMABLE_ITEM, 17821, 1);
				if(ItemFunctions.checkIsEquipped(this, -1, 90336, 0)) // Sapphire - Lv. 4
					return SkillEntry.makeSkillEntry(SkillEntryType.CUNSUMABLE_ITEM, 17820, 1);
				if(ItemFunctions.checkIsEquipped(this, -1, 90335, 0)) // Sapphire - Lv. 3
					return SkillEntry.makeSkillEntry(SkillEntryType.CUNSUMABLE_ITEM, 17819, 1);
				if(ItemFunctions.checkIsEquipped(this, -1, 90334, 0)) // Sapphire - Lv. 2
					return SkillEntry.makeSkillEntry(SkillEntryType.CUNSUMABLE_ITEM, 17818, 2);
				if(ItemFunctions.checkIsEquipped(this, -1, 90333, 0)) // Sapphire - Lv. 1
					return SkillEntry.makeSkillEntry(SkillEntryType.CUNSUMABLE_ITEM, 17818, 1);
			}
			else
			{
				// Sapphire's
				if(ItemFunctions.checkIsEquipped(this, -1, 70460, 0)) // Sapphire - Lv. 5
					return SkillEntry.makeSkillEntry(SkillEntryType.CUNSUMABLE_ITEM, 17821, 2);
				if(ItemFunctions.checkIsEquipped(this, -1, 70459, 0)) // Sapphire - Lv. 4
					return SkillEntry.makeSkillEntry(SkillEntryType.CUNSUMABLE_ITEM, 17820, 2);
				if(ItemFunctions.checkIsEquipped(this, -1, 70458, 0)) // Sapphire - Lv. 3
					return SkillEntry.makeSkillEntry(SkillEntryType.CUNSUMABLE_ITEM, 17819, 2);
				if(ItemFunctions.checkIsEquipped(this, -1, 70457, 0)) // Sapphire - Lv. 2
					return SkillEntry.makeSkillEntry(SkillEntryType.CUNSUMABLE_ITEM, 17818, 4);
				if(ItemFunctions.checkIsEquipped(this, -1, 70456, 0)) // Sapphire - Lv. 1
					return SkillEntry.makeSkillEntry(SkillEntryType.CUNSUMABLE_ITEM, 17818, 3);
				//
				if(ItemFunctions.checkIsEquipped(this, -1, 90337, 0)) // Sapphire - Lv. 5
					return SkillEntry.makeSkillEntry(SkillEntryType.CUNSUMABLE_ITEM, 17821, 2);
				if(ItemFunctions.checkIsEquipped(this, -1, 90336, 0)) // Sapphire - Lv. 4
					return SkillEntry.makeSkillEntry(SkillEntryType.CUNSUMABLE_ITEM, 17820, 2);
				if(ItemFunctions.checkIsEquipped(this, -1, 90335, 0)) // Sapphire - Lv. 3
					return SkillEntry.makeSkillEntry(SkillEntryType.CUNSUMABLE_ITEM, 17819, 2);
				if(ItemFunctions.checkIsEquipped(this, -1, 90334, 0)) // Sapphire - Lv. 2
					return SkillEntry.makeSkillEntry(SkillEntryType.CUNSUMABLE_ITEM, 17818, 4);
				if(ItemFunctions.checkIsEquipped(this, -1, 90333, 0)) // Sapphire - Lv. 1
					return SkillEntry.makeSkillEntry(SkillEntryType.CUNSUMABLE_ITEM, 17818, 3);
			}
		}

		return null;
	}

	public String getHWID()
	{
		// TODO save in field, because getNetConnection() can return null
		final GameClient connection = getNetConnection();
		if (connection == null) {
			return CharacterDAO.getInstance().getLastHWIDByName(_name);
		}
		return connection.getHWID();
	}

	public boolean isInAwayingMode()
	{
		return _awaying;
	}

	public void setAwayingMode(boolean awaying)
	{
		_awaying = awaying;
	}

	public double getMPCostDiff(Skill.SkillMagicType type)
	{
		// correct ?
		return getStat().getMpConsumeTypeValue(type) * 100 - 100;
	}

	public int getExpertiseIndex()
	{
		return getSkillLevel(239, 0);
	}

	//------------------------------------------------------------------------------------------------------------------

	private final Map<ListenerHookType, Set<ListenerHook>> scriptHookTypeList = new ConcurrentHashMap<>();

	public void addListenerHook(ListenerHookType type, ListenerHook hook)
	{
		if(!scriptHookTypeList.containsKey(type))
		{
			Set<ListenerHook> hooks = ConcurrentHashMap.newKeySet();
			hooks.add(hook);
			scriptHookTypeList.put(type, hooks);
		}
		else
		{
			Set<ListenerHook> hooks = scriptHookTypeList.get(type);
			hooks.add(hook);
		}
	}

	public void removeListenerHookType(ListenerHookType type, ListenerHook hook)
	{
		if(scriptHookTypeList.containsKey(type))
		{
			Set<ListenerHook> hooks = scriptHookTypeList.get(type);
			hooks.remove(hook);
		}
	}

	public Set<ListenerHook> getListenerHooks(ListenerHookType type)
	{
		Set<ListenerHook> hooks = scriptHookTypeList.get(type);
		if(hooks == null)
		{
			hooks = Collections.emptySet();
		}
		return hooks;
	}

	//------------------------------------------------------------------------------------------------------------------

	@Override
	public boolean isFakePlayer()
	{
		return getAI() != null && getAI().isFake();
	}

	public OptionDataTemplate addOptionData(OptionDataTemplate optionData)
	{
		if(optionData == null)
			return null;

		OptionDataTemplate oldOptionData = _options.get(optionData.getId());
		if(optionData == oldOptionData)
			return oldOptionData;

		_options.put(optionData.getId(), optionData);

		addTriggers(optionData);

		optionData.apply(this);

		for(SkillEntry skillEntry : optionData.getSkills())
			addSkill(skillEntry);

		return oldOptionData;
	}

	public OptionDataTemplate removeOptionData(int id)
	{
		OptionDataTemplate oldOptionData = _options.remove(id);
		if(oldOptionData != null)
		{
			removeTriggers(oldOptionData);

			oldOptionData.remove(this);

			for(SkillEntry skillEntry : oldOptionData.getSkills())
				removeSkill(skillEntry);
		}
		return oldOptionData;
	}

	public long getReceivedExp()
	{
		return _receivedExp;
	}

	@Override
	protected void onSpawn()
	{
		super.onSpawn();
		getAI().notifyEvent(CtrlEvent.EVT_SPAWN);
	}

	@Override
	protected void onDespawn()
	{
		getAI().notifyEvent(CtrlEvent.EVT_DESPAWN);
		super.onDespawn();
	}

	public void setQuestZoneId(int id)
	{
		_questZoneId = id;
	}

	public int getQuestZoneId()
	{
		return _questZoneId;
	}

	@Override
	protected void onAddSkill(SkillEntry skillEntry)
	{
		Skill skill = skillEntry.getTemplate();
		if(skill.isNecessaryToggle())
		{
			if(skill.isToggleGrouped() && skill.getToggleGroupId() > 0)
			{
				for(Abnormal abnormal : getAbnormalList())
				{
					if(abnormal.getSkill().isToggleGrouped() && abnormal.getSkill().getToggleGroupId() == skill.getToggleGroupId())
						return;
				}
			}
			forceUseSkill(skillEntry, this);
		}
	}

	public void setCustomHero(int hours)
	{
		setHero(true);
		updatePledgeRank();
		broadcastPacket(new SocialActionPacket(getObjectId(), SocialActionPacket.GIVE_HERO));
		checkHeroSkills();
		int time = hours == -1 ? -1 : (int) (System.currentTimeMillis() / 1000) + (hours * 60 * 60);
		CustomHeroDAO.getInstance().addCustomHero(getObjectId(), time);
	}

	public void setSelectedMultiClassId(ClassId classId)
	{
		_selectedMultiClassId = classId;
	}

	public ClassId getSelectedMultiClassId()
	{
		return _selectedMultiClassId;
	}

	@Override
	public int getPAtk(Creature target)
	{
		return (int) (super.getPAtk(target) * Config.PLAYER_P_ATK_MODIFIER);
	}

	@Override
	public int getMAtk(Creature target, Skill skill)
	{
		return (int) (super.getMAtk(target, skill) * Config.PLAYER_M_ATK_MODIFIER);
	}

	@Override
	public void onZoneEnter(Zone zone)
	{
		boolean sendBuffStore = true;
		boolean sendEnterMessage = true;
		boolean blockActions = true;
		for(Zone z : getZones())
		{
			if(z == zone)
				continue;

			if(zone.getType() == ZoneType.buff_store && z.getType() == ZoneType.buff_store)
				sendBuffStore = false;
			if(zone.getEnteringMessageId() == z.getEnteringMessageId())
				sendEnterMessage = false;
			if(Arrays.equals(zone.getTemplate().getBlockedActions(), z.getTemplate().getBlockedActions()))
				blockActions = false;
		}

		if(zone.getType() == ZoneType.SIEGE)
		{
			for(CastleSiegeEvent siegeEvent : zone.getEvents(CastleSiegeEvent.class))
			{
				// ????????????????????, ?????? ?????????? ???????????????????? ?? ??????????.
				if(containsEvent(siegeEvent))
					siegeEvent.addVisitedParticipant(this);
			}
		}
		if(sendBuffStore)
		{
			if(zone.getType() == ZoneType.buff_store && Config.BUFF_STORE_ALLOWED_CLASS_LIST.contains(getClassId().getId()))
				sendPacket(new SayPacket2(0, ChatType.BATTLEFIELD, getName(), new CustomMessage("l2s.gameserver.model.Player.EnterOfflineBufferZone").toString(this)));
		}
		if(sendEnterMessage)
		{
			if(zone.getEnteringMessageId() != 0)
				sendPacket(new SystemMessage(zone.getEnteringMessageId()));
		}
		if(blockActions)
		{
			if(zone.getTemplate().getBlockedActions() != null)
				blockActions(zone.getTemplate().getBlockedActions());
		}
		if(zone.getType() == ZoneType.peace_zone)
		{
			DuelEvent duel = getEvent(DuelEvent.class);
			if(duel != null)
				duel.abortDuel(this);
		}
	}

	@Override
	public void onZoneLeave(Zone zone)
	{
		boolean sendBuffStore = true;
		boolean sendLeavingMessage = true;
		boolean unblockActions = true;
		for(Zone z : getZones())
		{
			if(z == zone)
				continue;

			if(zone.getType() == ZoneType.buff_store && z.getType() == ZoneType.buff_store)
				sendBuffStore = false;
			if(zone.getLeavingMessageId() == z.getLeavingMessageId())
				sendLeavingMessage = false;
			if(Arrays.equals(zone.getTemplate().getBlockedActions(), z.getTemplate().getBlockedActions()))
				unblockActions = false;
		}

		if(sendBuffStore)
		{
			if(zone.getType() == ZoneType.buff_store && Config.BUFF_STORE_ALLOWED_CLASS_LIST.contains(getClassId().getId()))
				sendPacket(new SayPacket2(0, ChatType.BATTLEFIELD, getName(), new CustomMessage("l2s.gameserver.model.Player.ExitOfflineBufferZone").toString(this)));
		}
		if(sendLeavingMessage)
		{
			if(zone.getLeavingMessageId() != 0)
				sendPacket(new SystemMessage(zone.getLeavingMessageId()));
		}
		if(unblockActions)
		{
			if(zone.getTemplate().getBlockedActions() != null)
				unblockActions(zone.getTemplate().getBlockedActions());
		}
	}

	@Override
	public boolean hasBasicPropertyResist()
	{
		return false;
	}

	public void addTask(Object owner, ScheduledFuture<?> task)
	{
		if(task != null)
			_tasks.put(owner, task);
	}

	public void removeTask(Object owner, ScheduledFuture<?> task)
	{
		task.cancel(false);
		_tasks.remove(owner, task);
	}

	public void removeTasksByOwner(Object owner)
	{
		ScheduledFuture<?> future = _tasks.remove(owner);
		if (future != null) {
			future.cancel(false);
		}
	}

	public boolean canReceiveStatusUpdate(Creature creature, UpdateType updateType, int field) {
		if (creature == this)
			return true;

		boolean isRegenOrDamage = updateType == UpdateType.REGEN || /*updateType == UpdateType.CONSUME || */updateType == UpdateType.DAMAGED;

		if (creature.isNpc() || creature.isDoor()) {
			if (isRegenOrDamage || getTarget() == creature || getDistance(creature) < 700) {
				if(field == StatusUpdatePacket.CUR_HP)
					return true;
				if(field == StatusUpdatePacket.MAX_HP)
					return true;
			}
			if (isRegenOrDamage) {
				if(field == StatusUpdatePacket.CUR_MP)
					return true;
				if(field == StatusUpdatePacket.MAX_MP)
					return true;
			}
		} else if (creature.isPlayable()) {
			if (field == StatusUpdatePacket.KARMA)
				return true;
			if (field == StatusUpdatePacket.PVP_FLAG)
				return true;

			if (creature.isServitor()) {
				if (isRegenOrDamage || getTarget() == creature || getDistance(creature) < 700) {
					if (field == StatusUpdatePacket.CUR_HP)
						return true;
					if (field == StatusUpdatePacket.MAX_HP)
						return true;
				}
				if (isRegenOrDamage || getTarget() == creature) {
					if (field == StatusUpdatePacket.CUR_MP)
						return true;
					if (field == StatusUpdatePacket.MAX_MP)
						return true;
				}
			} else {
				Player player = creature.getPlayer();

				boolean canReceiveHpMp;
				if (player == null || player == this)
					canReceiveHpMp = true;
				else {
					canReceiveHpMp = isRegenOrDamage;
					if (!canReceiveHpMp && isInSameParty(player))
						canReceiveHpMp = true;
					if (!canReceiveHpMp && isInSameChannel(player))
						canReceiveHpMp = true;
					if (!canReceiveHpMp && isInSameClan(player))
						canReceiveHpMp = true;
				}

				if (canReceiveHpMp) {
					if (field == StatusUpdatePacket.CUR_HP)
						return true;
					if (field == StatusUpdatePacket.MAX_HP)
						return true;
					if (field == StatusUpdatePacket.CUR_MP)
						return true;
					if (field == StatusUpdatePacket.MAX_MP)
						return true;
					if(creature.isPlayer()) {
						if (field == StatusUpdatePacket.CUR_CP)
							return true;
						if (field == StatusUpdatePacket.MAX_CP)
							return true;
					}
				}
			}
		}
		return false;
	}

	private final Map<BanBindType, Pair<Integer, Future<?>>> banTasks = new HashMap<>();

	public boolean startBanEndTask(BanBindType bindType, int endTime) {
		Pair<Integer, Future<?>> taskInfo = banTasks.get(bindType);
		if(taskInfo != null) {
			if(taskInfo.getKey() == endTime)
				return false;

			Future<?> task = taskInfo.getValue();
			if(task != null)
				task.cancel(false);
		}

		Future<?> task = null;
		if(endTime != -1) {
			long delay = (endTime * 1000L) - System.currentTimeMillis();
			if (delay <= 0)
				return false;

			if (bindType == BanBindType.CHAT)
				task = ThreadPoolManager.getInstance().schedule(() -> GameBanManager.onUnban(bindType, getObjectId(), false), delay);

			if (task == null)
				return false;
		}
		banTasks.put(bindType, Pair.of(endTime, task));
		return true;
	}

	public boolean stopBanEndTask(BanBindType bindType) {
		Pair<Integer, Future<?>> taskInfo = banTasks.remove(bindType);
		if(taskInfo == null)
			return false;

		Future<?> task = taskInfo.getValue();
		if(task == null)
			return false;

		task.cancel(false);
		return true;
	}

	public void stopBanEndTasks() {
		for(Pair<Integer, Future<?>> taskInfo : banTasks.values()) {
			Future<?> task = taskInfo.getValue();
			if(task != null)
				task.cancel(false);
		}
		banTasks.clear();
	}

	@Override
	public void sendElementalInfo()
	{
		sendPacket(new UIPacket(this, false).addComponentType(UserInfoType.ATT_SPIRITS));
	}

	@Override
	public void sendSlotsInfo() {
		sendPacket(new UIPacket(this, false).addComponentType(UserInfoType.SLOTS));
	}

	@Override
	public void sendCurrentHpMpCpExpSp() {
		sendPacket(new UIPacket(this, false).addComponentType(UserInfoType.CURRENT_HPMPCP_EXP_SP));
	}

	public boolean changeActiveElement(ElementalElement element)
	{
		if(getActiveElement() == element)
			return false;

		int leftTime = (int) TimeUnit.MILLISECONDS .toSeconds(_lastChangeActiveElementTime - System.currentTimeMillis());
		if(leftTime > CHANGE_ACTIVE_ELEMENT_DELAY)
		{
			sendPacket(new SystemMessagePacket(SystemMsg.ATTACK_ATTRIBUTE_CAN_BE_CHANGED_AFTER_S1_SECONDS).addInteger(leftTime));
			return false;
		}

		_lastChangeActiveElementTime = System.currentTimeMillis();
		setActiveElement(element);
		sendPacket(new SystemMessagePacket(SystemMsg.S1_WILL_BE_YOUR_ATTRIBUTE_ATTACK_FROM_NOW_ON).addElementName(element.getId() - 1));
		return true;
	}

	public boolean canUseElementals()
	{
		return getActiveElement() != ElementalElement.NONE;
	}

	public void setActiveElement(ElementalElement element)
	{
		if(getActiveSubClass() != null)
			getActiveSubClass().setActiveElement(element);
	}

	@Override
	public ElementalElement getActiveElement()
	{
		if(!Config.ELEMENTAL_SYSTEM_ENABLED || getClassLevel().ordinal() < ClassLevel.THIRD.ordinal())
			return ElementalElement.NONE;

		return getActiveSubClass() == null ? ElementalElement.NONE : getActiveSubClass().getActiveElement();
	}

	public Elemental getActiveElemental()
	{
		return getElementalList().get(getActiveElement());
	}

	public DamageInfo getDamageInfo() {
		return damageInfo;
	}

	@Override
	public MoveType getMoveType()
	{
		if (isSitting())
		{
			return MoveType.SIT;
		}
		return super.getMoveType();
	}

    public boolean checkClassMinLevel(boolean forNextClass) {
        return getLevel() >= getClassId().getClassMinLevel(forNextClass);
    }
}