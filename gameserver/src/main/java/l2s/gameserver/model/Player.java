package l2s.gameserver.model;

import gnu.trove.iterator.TIntLongIterator;
import gnu.trove.map.TIntLongMap;
import gnu.trove.map.hash.TIntLongHashMap;
import gve.buffer.BuffProfileHolder;
import gve.zones.GveZoneManager;
import l2s.Phantoms.objects.TrafficScheme.PUseItem;
import l2s.Phantoms.PhantomParams;
import l2s.Phantoms.RouteRecord;
import l2s.Phantoms.enums.PhantomType;
import l2s.commons.dbutils.DbUtils;
import l2s.commons.lang.reference.HardReference;
import l2s.commons.lang.reference.HardReferences;
import l2s.commons.util.Rnd;
import l2s.commons.util.concurrent.atomic.AtomicState;
import l2s.gameserver.Announcements;
import l2s.gameserver.Config;
import l2s.gameserver.GameServer;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.ai.PlayableAI;
import l2s.gameserver.ai.PlayerAI;
import l2s.gameserver.component.player.ConfrontationComponent;
import l2s.gameserver.component.player.MercenaryComponent;
import l2s.gameserver.config.xml.holder.GveRewardHolder;
import l2s.gameserver.dao.*;
import l2s.gameserver.data.QuestHolder;
import l2s.gameserver.data.xml.holder.*;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.database.mysql;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.geodata.GeoMove;
import l2s.gameserver.handler.items.IItemHandler;
import l2s.gameserver.handler.onshiftaction.OnShiftActionHolder;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.instancemanager.*;
import l2s.gameserver.listener.actor.player.OnAnswerListener;
import l2s.gameserver.listener.actor.player.impl.BotCheckAnswerListner;
import l2s.gameserver.listener.actor.player.impl.ReviveAnswerListener;
import l2s.gameserver.listener.actor.player.impl.SummonAnswerListener;
import l2s.gameserver.logging.ItemLogProcess;
import l2s.gameserver.logging.LogService;
import l2s.gameserver.logging.LoggerType;
import l2s.gameserver.logging.message.ItemLogMessage;
import l2s.gameserver.model.actor.basestats.PlayerBaseStats;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.actor.instances.player.FriendList;
import l2s.gameserver.model.actor.instances.player.*;
import l2s.gameserver.model.actor.instances.player.tasks.EnableUserRelationTask;
import l2s.gameserver.model.actor.listener.PlayerListenerList;
import l2s.gameserver.model.actor.recorder.PlayerStatsChangeRecorder;
import l2s.gameserver.model.base.*;
import l2s.gameserver.model.entity.Hero;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.impl.DuelEvent;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.entity.events.impl.SingleMatchEvent;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.model.entity.olympiad.OlympiadGame;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.entity.residence.ClanHall;
import l2s.gameserver.model.entity.residence.Residence;
import l2s.gameserver.model.entity.residence.ResidenceSide;
import l2s.gameserver.model.instances.*;
import l2s.gameserver.model.instances.residences.SiegeToggleNpcInstance;
import l2s.gameserver.model.items.*;
import l2s.gameserver.model.items.attachment.FlagItemAttachment;
import l2s.gameserver.model.items.attachment.PickableAttachment;
import l2s.gameserver.model.matching.MatchingRoom;
import l2s.gameserver.model.packet.PacketThrottler;
import l2s.gameserver.model.petition.PetitionMainGroup;
import l2s.gameserver.model.pledge.*;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestEventType;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.authcomm.gs2as.BonusRequest;
import l2s.gameserver.network.authcomm.gs2as.ReduceAccountPoints;
import l2s.gameserver.network.authcomm.vertx.AuthServerCommunication;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.components.*;
import l2s.gameserver.network.l2.components.hwid.DefaultHwidHolder;
import l2s.gameserver.network.l2.components.hwid.EmptyHwidHolder;
import l2s.gameserver.network.l2.components.hwid.HwidHolder;
import l2s.gameserver.network.l2.s2c.*;
import l2s.gameserver.network.l2.s2c.ExMagicAttackInfo.MagicAttackType;
import l2s.gameserver.network.l2.s2c.anticheat.AAScreenStringPacketPresets;
import l2s.gameserver.network.l2.s2c.updatetype.IUpdateTypeComponent;
import l2s.gameserver.punishment.PunishmentService;
import l2s.gameserver.punishment.PunishmentType;
import l2s.gameserver.security.HwidUtils;
import l2s.gameserver.service.ArtifactService;
import l2s.gameserver.service.ConfrontationService;
import l2s.gameserver.service.FractionService;
import l2s.gameserver.service.PartyClassLimitService;
import l2s.gameserver.service.SubsSkillsService;
import l2s.gameserver.skills.*;
import l2s.gameserver.skills.effects.EffectCubic;
import l2s.gameserver.skills.skillclasses.Summon;
import l2s.gameserver.stats.Formulas;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.stats.triggers.TriggerInfo;
import l2s.gameserver.tables.ClanTable;
import l2s.gameserver.taskmanager.AutoSaveManager;
import l2s.gameserver.taskmanager.LazyPrecisionTaskManager;
import l2s.gameserver.templates.CreatureTemplate;
import l2s.gameserver.templates.InstantZone;
import l2s.gameserver.templates.OptionDataTemplate;
import l2s.gameserver.templates.gve.SimpleReward;
import l2s.gameserver.templates.item.*;
import l2s.gameserver.templates.item.ArmorTemplate.ArmorType;
import l2s.gameserver.templates.item.data.ItemData;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.templates.pet.PetData;
import l2s.gameserver.templates.player.PlayerTemplate;
import l2s.gameserver.templates.player.transform.TransformTemplate;
import l2s.gameserver.templates.premiumaccount.PremiumAccountTemplate;
import l2s.gameserver.time.GameTimeService;
import l2s.gameserver.utils.*;
import l2s.gameserver.utils.spamfilter.SpamFilterManager;
import l2s.gameserver.utils.spamfilter.SpamFilterManager.SpamType;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.napile.pair.primitive.IntObjectPair;
import org.napile.pair.primitive.impl.IntObjectPairImpl;
import org.napile.primitive.Containers;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;
import org.napile.primitive.maps.impl.CTreeIntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class Player extends Playable implements PlayerGroup, HaveHwid {
	public static final int DEFAULT_NAME_COLOR = 16777215;
	public static final int DEFAULT_TITLE_COLOR = 16777079;
	public static final int MAX_POST_FRIEND_SIZE = 100;
	private static final Logger _log = LoggerFactory.getLogger(Player.class);
	public static final String NO_TRADERS_VAR = "notraders";
	public static final String NO_PRIVATEBUFFERS_VAR = "nopbuffers";
	public static final String NO_ANIMATION_OF_CAST_VAR = "notShowBuffAnim";
	public static final String MY_BIRTHDAY_RECEIVE_YEAR = "MyBirthdayReceiveYear";
	private static final String NOT_CONNECTED = "<not connected>";
	private static final String RECENT_PRODUCT_LIST_VAR = "recentProductList";
	private static final String LVL_UP_REWARD_VAR = "@lvl_up_reward";
	private static final String ACADEMY_GRADUATED_VAR = "@academy_graduated";
	private static final String JAILED_VAR = "jailed";
	private static final String PA_ITEMS_RECIEVED = "pa_items_recieved";
	private static final String FREE_PA_RECIEVED = "free_pa_recieved";
	private static final String ACTIVE_SHOT_ID_VAR = "@active_shot_id";
	private static final String PC_BANG_POINTS_VAR = "pc_bang_poins";
	private static final String PK_KILL_VAR = "@pk_kill";
	public static final int OBSERVER_NONE = 0;
	public static final int OBSERVER_STARTING = 1;
	public static final int OBSERVER_STARTED = 3;
	public static final int OBSERVER_LEAVING = 2;
	public static final int STORE_PRIVATE_NONE = 0;
	public static final int STORE_PRIVATE_SELL = 1;
	public static final int STORE_PRIVATE_BUY = 3;
	public static final int STORE_PRIVATE_MANUFACTURE = 5;
	public static final int STORE_OBSERVING_GAMES = 7;
	public static final int STORE_PRIVATE_SELL_PACKAGE = 8;
	public static final int[] EXPERTISE_LEVELS = new int[]{0, 20, 40, 52, 61, 76, 80, 84, 85, 95, 99, Integer.MAX_VALUE};

	private final ConfrontationComponent confrontationComponent;
	private final MercenaryComponent mercenaryComponent;
	private final PacketThrottler packetThrottler;

	private PlayerTemplate _baseTemplate;

	private GameClient _connection;
	private final HwidHolder hwidHolder;

	private String _login;
	private int _karma;
	private int _pkKills;
	private int _pvpKills;
	private int _face;
	private int _hairStyle;
	private int _hairColor;
	private int _beautyFace;
	private int _beautyHairStyle;
	private int _beautyHairColor;
	private int _recomHave;
	private int _recomLeftToday;
	private int _fame;
	private int _raidPoints;
	private int _recomLeft;
	private int _deleteTimer;
	private final boolean _isVoting;
	private long _createTime;
	private long _onlineTime;
	private long _onlineBeginTime;
	private long _leaveClanTime;
	private long _deleteClanTime;

	private long _uptime;
	private long _lastAccess;
	private boolean _overloaded;
	boolean sittingTaskLaunched;
	private int _waitTimeWhenSit;
	private boolean _autoLoot;
	private boolean AutoLootHerbs;
	private boolean _autoLootOnlyAdena;
	private final PcInventory _inventory;
	private final Warehouse _warehouse;
	private final ItemContainer _refund;
	private final PcFreight _freight;
	private final BookMarkList _bookmarks;
	public Location bookmarkLocation;
	private final AntiFlood _antiFlood;
	private final Map<Integer, RecipeTemplate> _recipebook;
	private final Map<Integer, RecipeTemplate> _commonrecipebook;
	private final IntObjectMap<QuestState> _quests;
	private final ShortCutList _shortCuts;
	private final MacroList _macroses;
	private final SubClassList _subClassList;
	private int _privatestore;
	private String _manufactureName;
	private boolean _manufactureNameSpam;
	private List<ManufactureItem> _createList;
	private String _sellStoreName;
	private boolean _sellStoreNameSpam;
	private List<TradeItem> _sellList;
	private List<TradeItem> _packageSellList;
	private String _buyStoreName;
	private boolean _buyStoreNameSpam;
	private List<TradeItem> _buyList;
	private List<TradeItem> _tradeList;
	private Party _party;
	private Location _lastPartyPosition;
	private Clan _clan;
	private PledgeRank _pledgeRank;
	private int _pledgeType;
	private int _powerGrade;
	private int _lvlJoinedAcademy;
	private int _apprentice;
	private int _accessLevel;
	private PlayerAccess _playerAccess;
	private boolean _messageRefusal;
	private boolean _tradeRefusal;
	private boolean _blockAll;
	public static final int MAX_SUMMON_COUNT = 4;
	private final IntObjectMap<SummonInstance> _summons;
	private PetInstance _pet;
	private SymbolInstance _symbol;
	private boolean _riding;
	private int _botRating;
	private final List<DecoyInstance> _decoys;
	private Map<Integer, EffectCubic> _cubics;
	private int _agathionId;
	private Request _request;
	private ItemInstance _arrowItem;
	private WeaponTemplate _fistsWeaponItem;
	private Map<Integer, String> _chars;
	private ItemInstance _enchantScroll;
	private Warehouse.WarehouseType _usingWHType;

	private boolean _isOnline;

	private final AtomicBoolean _isLogout;
	private HardReference<NpcInstance> _lastNpc;
	private volatile HardReference<? extends Creature> _aggressionTarget;
	private MultiSellListContainer _multisell;
	private ObservePoint _observePoint;
	private final AtomicInteger _observerMode;
	private ObservableArena _observableArena;
	public int _telemode;
	public boolean entering;
	private Location _stablePoint;
	public int[] _loto;
	public int[] _race;
	private final BlockList _blockList;
	private final FriendList _friendList;
	private final ProductHistoryList _productHistoryList;
	private final HennaList _hennaList;
	private final AttendanceRewards _attendanceRewards;
	private final DailyMissionList _dailiyMissionList;
	private PremiumAccountTemplate _premiumAccount;
	private Future<?> _premiumAccountExpirationTask;
	private boolean _isSitting;
	private StaticObjectInstance _sittingObject;
	private int _varka;
	private int _ketra;
	private int _ram;
	private byte[] _keyBindings;
	private Future<?> _taskWater;
	private Future<?> _autoSaveTask;
	private Future<?> _kickTask;
	private Future<?> _pcCafePointsTask;
	private Future<?> _unjailTask;
	private final Lock _storeLock;
	private int _zoneMask;
	private boolean _offline;
	private boolean _awaying;
	private boolean _registeredInEvent;
	private int _pcBangPoints;
	private int _expandInventory;
	private int _expandWarehouse;
	private int _battlefieldChatId;
	private int _lectureMark;
	private final AtomicState _gmInvisible;
	private IntObjectMap<String> _postFriends;
	private final List<String> _blockedActions;
	private final BypassStorage _bypassStorage;
	private boolean _notShowBuffAnim;
	private boolean _notShowTraders;
	private boolean notShowPrivateBuffers;
	private boolean _canSeeAllShouts;
	private boolean _debug;
	private final AtomicState _chatBlocked;
	private final AtomicState _partyBlocked;
	private final AtomicState _violetBoy;
	private long _dropDisabled;
	private long _lastItemAuctionInfoRequest;
	private IntObjectPair<OnAnswerListener> _askDialog;
	private boolean _matchingRoomWindowOpened;
	private MatchingRoom _matchingRoom;
	private PetitionMainGroup _petitionGroup;
	private final Map<Integer, Long> _instancesReuses;
	private Language _language;
	private int _npcDialogEndTime;
	private Mount _mount;
	private final Map<String, CharacterVariable> _variables;
	private List<SummonInstance.RestoredSummon> _restoredSummons;
	private boolean _autoSearchParty;
	private Future<?> _substituteTask;

	private TransformTemplate _transform;
	private final Lock transformLock = new ReentrantLock();
	private final IntObjectMap<SkillEntry> _transformSkills;

	private long _lastMultisellBuyTime;
	private long _lastEnchantItemTime;
	private long _lastAttributeItemTime;
	private Future<?> _enableRelationTask;
	private boolean _isInReplaceTeleport;
	private int _armorSetEnchant;
	private int _usedWorldChatPoints;
	private boolean _hideHeadAccessories;
	private final List<TrapInstance> _traps;
	private boolean _isInJail;
	private final IntObjectMap<OptionDataTemplate> _options;
	private long _receivedExp;

	private int _questZoneId;
	private boolean _dontRewardSkills;
	private ScheduledFuture<?> _broadcastCharInfoTask;
	private int _polyNpcId;
	private Future<?> _userInfoTask;
	private boolean _maried;
	private int _partnerId;
	private int _coupleId;
	private boolean _maryrequest;
	private boolean _maryaccepted;
	private final List<Player> _snoopListener;
	private final List<Player> _snoopedPlayer;
	private boolean _charmOfCourage;
	private int _increasedForce;
	private int _consumedSouls;
	private long _lastFalling;
	private int _useSeed;
	protected int _pvpFlag;
	private Future<?> _PvPRegTask;
	private long _lastPvPAttack;
	private long _lastAttackPacket;
	private long _lastMovePacket;
	private Location _groundSkillLoc;
	private int _buyListId;
	private final int _incorrectValidateCount = 0;
	private int _movieId;
	private ItemInstance _petControlItem;
	private final AtomicBoolean isActive;
	private Future<?> _hourlyTask;
	private int _hoursInGame;
	private boolean _agathionResAvailable;
	private int[] _recentProductList;
	private long _blockUntilTime;

	// sessionVars - испольюзуется для хранения временных переменных.
	private final Map<String, Object> sessionVars = new ConcurrentHashMap<>();
	private boolean _isSpamer;
	private Future<?> _clanRewardLoginTask;
	private boolean _hero;
	private Future<?> customHeroTask;
	private int _olympiadSide;
	private OlympiadGame _olympiadGame;
	private OlympiadGame _olympiadObserveGame;
	private boolean _inOlympiadMode;
	private final Fishing _fishing;
	private ItemInstance _synthesisItem1;
	private ItemInstance _synthesisItem2;
	private final DamageList _damageList;
	private int _comboKills;
	private final IntObjectMap<SoulShotType> _activeAutoShots;
	private final Map<String, Future<?>> tasks = new ConcurrentHashMap<>();
	private boolean ignoreValidatePosition;
	private final PrivateBuffer privateBuffer = new PrivateBuffer(this);

	public PhantomParams phantom_params;
	public RouteRecord tScheme_record; 
	
	public Player(final int objectId, final PlayerTemplate template, final String accountName, HwidHolder hwidHolder)
	{
		super(objectId, template);
		phantom_params = new PhantomParams(this);
		tScheme_record = new RouteRecord(this);
		this.hwidHolder = hwidHolder;
		_recomLeft = 0;
		_isVoting = false;
		_autoLoot = Config.AUTO_LOOT;
		AutoLootHerbs = Config.AUTO_LOOT_HERBS;
		_autoLootOnlyAdena = Config.AUTO_LOOT_ONLY_ADENA;
		_inventory = new PcInventory(this);
		_warehouse = new PcWarehouse(this);
		_refund = new PcRefund(this);
		_freight = new PcFreight(this);
		_bookmarks = new BookMarkList(this, 0);
		bookmarkLocation = null;
		_antiFlood = new AntiFlood(this);
		_recipebook = new TreeMap<>();
		_commonrecipebook = new TreeMap<>();
		_quests = new HashIntObjectMap<>();
		_traps = new CopyOnWriteArrayList<>();
		_shortCuts = new ShortCutList(this);
		_macroses = new MacroList(this);
		_subClassList = new SubClassList(this);
		_createList = Collections.emptyList();
		_sellList = Collections.emptyList();
		_packageSellList = Collections.emptyList();
		_buyList = Collections.emptyList();
		_tradeList = Collections.emptyList();
		_pledgeRank = PledgeRank.VAGABOND;
		_pledgeType = -128;
		_powerGrade = 0;
		_olympiadSide = -1;
		_lvlJoinedAcademy = 0;
		_apprentice = 0;
		_playerAccess = new PlayerAccess();
		_messageRefusal = false;
		_tradeRefusal = false;
		_blockAll = false;
		_summons = new HashIntObjectMap<>(4);
		_pet = null;
		_symbol = null;
		_decoys = new CopyOnWriteArrayList<>();
		_cubics = null;
		_agathionId = 0;
		_chars = new HashMap<>(8);
		_enchantScroll = null;
		_isOnline = false;
		_isLogout = new AtomicBoolean();
		_lastNpc = HardReferences.emptyRef();
		_aggressionTarget = HardReferences.emptyRef();
		_multisell = null;
		_activeAutoShots = new CHashIntObjectMap<>();
		_observerMode = new AtomicInteger(0);
		_telemode = 0;
		entering = true;
		_stablePoint = null;
		_loto = new int[5];
		_race = new int[2];
		_blockList = new BlockList(this);
		_friendList = new FriendList(this);
		_productHistoryList = new ProductHistoryList(this);
		_hennaList = new HennaList(this);
		_attendanceRewards = new AttendanceRewards(this);
		_dailiyMissionList = new DailyMissionList(this);
		_fishing = new Fishing(this);
		_premiumAccount = PremiumAccountHolder.getInstance().getPremiumAccount(0);
		_varka = 0;
		_ketra = 0;
		_ram = 0;
		_keyBindings = ArrayUtils.EMPTY_BYTE_ARRAY;
		_storeLock = new ReentrantLock();
		_offline = false;
		_awaying = false;
		_registeredInEvent = false;
		_expandInventory = 0;
		_expandWarehouse = 0;
		_gmInvisible = new AtomicState();
		_postFriends = Containers.emptyIntObjectMap();
		_blockedActions = new ArrayList<>();
		_bypassStorage = new BypassStorage();
		_notShowBuffAnim = false;
		_notShowTraders = false;
		_canSeeAllShouts = false;
		_debug = false;
		_chatBlocked = new AtomicState();
		_partyBlocked = new AtomicState();
		_violetBoy = new AtomicState();
		_askDialog = null;
		_matchingRoomWindowOpened = false;
		_instancesReuses = new ConcurrentHashMap<>();
		_language = Config.DEFAULT_LANG;
		_npcDialogEndTime = 0;
		_mount = null;
		_variables = new ConcurrentHashMap<>();
		_restoredSummons = null;
		_transform = null;
		_transformSkills = new CHashIntObjectMap<>();
		_lastMultisellBuyTime = 0L;
		_lastEnchantItemTime = 0L;
		_lastAttributeItemTime = 0L;
		_isInReplaceTeleport = false;
		_armorSetEnchant = 0;
		_usedWorldChatPoints = 0;
		_hideHeadAccessories = false;
		_isInJail = false;
		_options = new CTreeIntObjectMap<>();
		_receivedExp = 0L;
		_questZoneId = -1;
		_dontRewardSkills = false;
		_maried = false;
		_partnerId = 0;
		_coupleId = 0;
		_maryrequest = false;
		_maryaccepted = false;
		_snoopListener = new ArrayList<>();
		_snoopedPlayer = new ArrayList<>();
		_charmOfCourage = false;
		_increasedForce = 0;
		_consumedSouls = 0;
		_useSeed = 0;
		_lastAttackPacket = 0L;
		_lastMovePacket = 0L;
		_movieId = 0;
		_petControlItem = null;
		isActive = new AtomicBoolean();
		_hoursInGame = 0;
		_agathionResAvailable = false;
		_recentProductList = null;
		_blockUntilTime = 0L;
		_baseTemplate = template;
		_login = accountName;
		_damageList = new DamageList(this);
		confrontationComponent = new ConfrontationComponent(this);
		mercenaryComponent = new MercenaryComponent(this);
		packetThrottler = new PacketThrottler(this);
	}

	private Player(final int objectId, final PlayerTemplate template, HwidHolder hwidHolder)
	{
		this(objectId, template, null, hwidHolder);
		if(GameObjectsStorage.getPlayers().size() >= GameServer.getInstance().getOnlineLimit())
		{
			kick();
			return;
		}
		_baseTemplate = template;
		_ai = new PlayerAI(this);

		if(!Config.EVERYBODY_HAS_ADMIN_RIGHTS)
			setPlayerAccess(Config.gmlist.get(objectId));
		else
			setPlayerAccess(Config.gmlist.get(0));
	}

	@Override
	public HardReference<Player> getRef()
	{
		return (HardReference<Player>) super.getRef();
	}

	public String getAccountName()
	{
		final GameClient connection = _connection;
		if(connection == null)
			return _login;
		return connection.getLogin();
	}

	public String getIP()
	{
		final GameClient connection = _connection;
		if(connection == null)
			return NOT_CONNECTED;
		return connection.getIpAddr();
	}

	public String getLogin()
	{
		return _login;
	}

	public void setLogin(final String val)
	{
		_login = val;
	}

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
	public final void setTemplate(final CreatureTemplate template)
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
		transformLock.lock();
		try
		{
			return _transform != null;
		}
		finally
		{
			transformLock.unlock();
		}
	}

	@Override
	public final TransformTemplate getTransform()
	{
		transformLock.lock();
		try
		{
			return _transform;
		}
		finally
		{
			transformLock.unlock();
		}
	}

	@Override
	public final void setTransform(final int id)
	{
		TransformTemplate template = id > 0 ? TransformTemplateHolder.getInstance().getTemplate(getSex(), id) : null;
		if(id > 0 && template == null)
		{
			_log.info("Can't find template {} for player {}", id, this);
			return;
		}
		setTransform(template);
	}

	@Override
	public final void setTransform(final TransformTemplate transform)
	{
		boolean isFlying;
		boolean isVisible;

		transformLock.lock();
		try
		{
			if(transform == _transform || transform != null && _transform != null)
				return;

			isVisible = isVisible();

			if(transform == null)
			{
				isFlying = _transform.getType() == TransformType.FLYING;
				if(isFlying)
				{
					decayMe();
					setFlying(false);
					setLoc(getLoc().correctGeoZ());
				}
				if(!_transformSkills.isEmpty())
				{
					for(final SkillEntry skillEntry : _transformSkills.values())
						if(!SkillAcquireHolder.getInstance().isSkillPossible(this, skillEntry.getTemplate()))
							removeSkill(skillEntry);
					_transformSkills.clear();
				}
				if(_transform.getItemCheckType() != LockType.NONE)
					getInventory().unlock();
				_transform = transform;
				checkActiveToggleEffects();
				getAbnormalList().stopEffects(EffectType.Transformation);
			}
			else
			{
				isFlying = transform.getType() == TransformType.FLYING;
				if(isFlying)
				{
					for(final Servitor servitor : getServitors())
						servitor.unSummon(false);
					decayMe();
					setFlying(true);
					setLoc(getLoc().changeZ(transform.getSpawnHeight()));
				}
				for(final SkillLearn sl : transform.getSkills())
				{
					final SkillEntry skillEntry2 = SkillHolder.getInstance().getSkillEntry(sl.getId(), sl.getLevel());
					if(skillEntry2 != null)
						_transformSkills.put(skillEntry2.getId(), skillEntry2);
				}
				for(final SkillLearn sl : transform.getAddtionalSkills())
					if(sl.getMinLevel() <= getLevel())
					{
						SkillEntry skillEntry2 = _transformSkills.get(sl.getId());
						if(skillEntry2 == null || skillEntry2.getLevel() < sl.getLevel())
						{
							skillEntry2 = SkillHolder.getInstance().getSkillEntry(sl.getId(), sl.getLevel());
							if(skillEntry2 != null)
								_transformSkills.put(skillEntry2.getId(), skillEntry2);
						}
					}

				for(final SkillEntry skillEntry : _transformSkills.values())
					addSkill(skillEntry, false);
				if(transform.getItemCheckType() != LockType.NONE)
				{
					getInventory().unlock();
					getInventory().lockItems(transform.getItemCheckType(), transform.getItemCheckIDs());
				}

				checkActiveToggleEffects();

				_transform = transform;
			}
		}
		finally
		{
			transformLock.unlock();
		}

		sendPacket(new ExBasicActionList(this));
		sendSkillList();
		sendPacket(new ShortCutInitPacket(this));
		if(isFlying && isVisible)
			spawnMe();
		sendChanges();
	}

	public void changeSex()
	{
		final PlayerTemplate template = PlayerTemplateHolder.getInstance().getPlayerTemplate(getRace(), getClassId(), getSex().revert());
		if(template == null)
			return;
		setTemplate(template);
		if(isTransformed())
		{
			final int transformId = getTransform().getId();
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
	public void doCast(final SkillEntry skillEntry, final Creature target, final boolean forceUse)
	{
		if(skillEntry == null)
			return;
		super.doCast(skillEntry, target, forceUse);
	}

	@Override
	public void sendReuseMessage(final Skill skill)
	{
		if(isCastingNow())
			return;
		final TimeStamp sts = getSkillReuse(skill);
		if(sts == null || !sts.hasNotPassed())
			return;
		final long timeleft = sts.getReuseCurrent();
		if(!Config.ALT_SHOW_REUSE_MSG && timeleft < 10000L || timeleft < 500L)
			return;
		final long hours = timeleft / 3600000L;
		final long minutes = (timeleft - hours * 3600000L) / 60000L;
		final long seconds = (long) Math.ceil((timeleft - hours * 3600000L - minutes * 60000L) / 1000.0);
		if(hours > 0L)
			sendPacket(new SystemMessage(2305).addSkillName(skill.getId(), skill.getDisplayLevel()).addNumber(hours).addNumber(minutes).addNumber(seconds));
		else if(minutes > 0L)
			sendPacket(new SystemMessage(2304).addSkillName(skill.getId(), skill.getDisplayLevel()).addNumber(minutes).addNumber(seconds));
		else
			sendPacket(new SystemMessage(2303).addSkillName(skill.getId(), skill.getDisplayLevel()).addNumber(seconds));
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

	public void setFace(final int face)
	{
		_face = face;
	}

	public int getBeautyFace()
	{
		return _beautyFace;
	}

	public void setBeautyFace(final int face)
	{
		_beautyFace = face;
	}

	public int getHairColor()
	{
		return _hairColor;
	}

	public void setHairColor(final int hairColor)
	{
		_hairColor = hairColor;
	}

	public int getBeautyHairColor()
	{
		return _beautyHairColor;
	}

	public void setBeautyHairColor(final int hairColor)
	{
		_beautyHairColor = hairColor;
	}

	public int getHairStyle()
	{
		return _hairStyle;
	}

	public void setHairStyle(final int hairStyle)
	{
		_hairStyle = hairStyle;
	}

	public int getBeautyHairStyle()
	{
		return _beautyHairStyle;
	}

	public void setBeautyHairStyle(final int hairStyle)
	{
		_beautyHairStyle = hairStyle;
	}

	public void offline()
	{
		offline(Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK);
	}

	public void offline(final int delay)
	{
		final GameClient connection = _connection;
		if(connection != null)
		{
			connection.setActiveChar(null);
			connection.close(ServerCloseSocketPacket.STATIC);
			setNetConnection(null);
		}

		for(AbnormalEffect ae : Config.SERVICES_OFFLINE_TRADE_ABNORMAL_EFFECT)
			startAbnormalEffect(ae);

		setOfflineMode(true);
		if(delay > 0)
		{
			setVar("offline", delay + System.currentTimeMillis() / 1000L);
			startKickTask(delay * 1000L);
		}
		else
			setVar("offline", Integer.MAX_VALUE);
		final Party party = getParty();
		if(party != null)
			leaveParty();
		if(isAutoSearchParty())
			PartySubstituteManager.getInstance().removeWaitingPlayer(this);
		for(final Servitor servitor : getServitors())
			servitor.unSummon(false);
		if(isInOlympiadMode())
			Olympiad.logoutPlayer(this);
		if(isFishing())
			getFishing().stop();

		MatchingRoomManager.getInstance().removeFromWaitingList(this);
		broadcastCharInfo();
		stopBroadcastAvailableActivities();
		stopWaterTask();
		stopPremiumAccountTask();
		stopHourlyTask();
		stopPcBangPointsTask();
		stopAutoSaveTask();
		stopQuestTimers();
		stopEnableUserRelationTask();
		stopCustomHeroTask();
		try
		{
			getInventory().store();
		}
		catch(Throwable t)
		{
			Player._log.error("", t);
		}
		try
		{
			store(false);
		}
		catch(Throwable t)
		{
			Player._log.error("", t);
		}
	}

	public void kick()
	{
		if (this.isPhantom())
		{
			if (this.phantom_params.getPhantomAI() != null)
			{
				this.phantom_params.getPhantomAI().abortAITask();
				this.phantom_params.getPhantomAI().abortBuffTask();
				this.phantom_params.stopSupportBuffTask();
			}

			if (this.phantom_params.getPhantomPartyAI()!=null)
			{
				this.phantom_params.getPhantomPartyAI().abortSubTask();
				this.phantom_params.getPhantomPartyAI().abortPRTask();
				this.phantom_params.getPhantomPartyAI().abortMainTask();
			}
		}

		prepareToLogout1();
		final GameClient connection = _connection;
		if(connection != null)
		{
			connection.close(LogOutOkPacket.STATIC);
			setNetConnection(null);
		}
		prepareToLogout2();
		deleteMe();
	}

	public void restart()
	{
		prepareToLogout1();
		final GameClient connection = _connection;
		if(connection != null)
		{
			connection.setActiveChar(null);
			setNetConnection(null);
		}
		prepareToLogout2();
		deleteMe();
	}

	public void logout()
	{
		prepareToLogout1();
		final GameClient connection = _connection;
		if(connection != null)
		{
			connection.close(ServerCloseSocketPacket.STATIC);
			setNetConnection(null);
		}
		prepareToLogout2();
		deleteMe();
	}

	private void prepareToLogout1()
	{
		for(final Servitor servitor : getServitors())
			sendPacket(new PetDeletePacket(servitor.getObjectId(), servitor.getServitorType()));
		if(isProcessingRequest())
		{
			final Request request = getRequest();
			if(isInTrade())
			{
				final Player parthner = request.getOtherPlayer(this);
				parthner.sendPacket(SystemMsg.THAT_PLAYER_IS_NOT_ONLINE);
				parthner.sendPacket(TradeDonePacket.FAIL);
			}
			request.cancel(new IBroadcastPacket[0]);
		}

		World.removeObjectsFromPlayer(this);
	}

	private void removeAllScreenStrings() {
		Arrays.stream(AAScreenStringPacketPresets.values())
				.map(AAScreenStringPacketPresets::remove)
				.forEach(this::sendPacket);
	}

	private void prepareToLogout2()
	{
		if(_isLogout.getAndSet(true))
			return;

		if(this.isPlayer() && !this.isPhantom() && this.tScheme_record.isLogging())
			this.tScheme_record.stopRecord(true);

		final FlagItemAttachment attachment = getActiveWeaponFlagAttachment();
		if(attachment != null)
			attachment.onLogout(this);
		setNetConnection(null);
		setIsOnline(false);
		getListeners().onExit();
		if(isFlying() && !checkLandingState())
			_stablePoint = TeleportUtils.getRestartPoint(this, RestartType.TO_VILLAGE).getLoc();
		if(isCastingNow())
			abortCast(true, true);
		final Party party = getParty();
		if(party != null)
			leaveParty();

		if(_observableArena != null)
			_observableArena.removeObserver(_observePoint);

		if(isInOlympiadMode())
			Olympiad.logoutPlayer(this);
		if(isFishing())
			getFishing().stop();
		if (isPrivateBuffer() && !isInOfflineMode())
			cancelPrivateBuffer();

		if(_stablePoint != null)
			teleToLocation(_stablePoint);
		for(final Servitor servitor : getServitors())
			servitor.unSummon(true);
		if(isMounted())
			_mount.onLogout();
		_friendList.notifyFriends(false);
		if(getClan() != null)
			getClan().loginClanCond(this, false);
		if(isProcessingRequest())
			getRequest().cancel(new IBroadcastPacket[0]);
		stopAllTimers();
		if(isInBoat())
			getBoat().removePlayer(this);
		final SubUnit unit = getSubUnit();
		final UnitMember member = unit == null ? null : unit.getUnitMember(getObjectId());
		if(member != null)
		{
			final int sponsor = member.getSponsor();
			final int apprentice = getApprentice();
			final PledgeShowMemberListUpdatePacket memberUpdate = new PledgeShowMemberListUpdatePacket(this);
			for(final Player clanMember : _clan.getOnlineMembers(getObjectId()))
			{
				clanMember.sendPacket(memberUpdate);
				if(clanMember.getObjectId() == sponsor)
					clanMember.sendPacket(new SystemMessage(1757).addString(_name));
				else
				{
					if(clanMember.getObjectId() != apprentice)
						continue;
					clanMember.sendPacket(new SystemMessage(1759).addString(_name));
				}
			}
			member.setPlayerInstance(this, true);
		}
		final MatchingRoom room = getMatchingRoom();
		if(room != null)
			if(room.getLeader() == this)
				room.disband();
			else
				room.removeMember(this, false);
		setMatchingRoom(null);
		MatchingRoomManager.getInstance().removeFromWaitingList(this);
		destroyAllTraps();
		if(!_decoys.isEmpty())
			for(final DecoyInstance decoy : getDecoys())
			{
				decoy.unSummon();
				removeDecoy(decoy);
			}
		stopPvPFlag();

		getDamageList().clear();

		final Reflection ref = getReflection();
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
			Player._log.error("", t);
		}
		try
		{
			store(false);
		}
		catch(Throwable t)
		{
			Player._log.error("", t);
		}
		mercenaryComponent.logout();
	}

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

	public void registerRecipe(final RecipeTemplate recipe, final boolean saveDB)
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
			Player._log.warn("Attempted to remove unknown RecipeList" + RecipeID);
	}

	public QuestState getQuestState(final int id)
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

	public QuestState getQuestState(final Quest quest)
	{
		return getQuestState(quest.getId());
	}

	public boolean isQuestCompleted(final int id)
	{
		final QuestState qs = getQuestState(id);
		return qs != null && qs.isCompleted();
	}

	public boolean isQuestCompleted(final Quest quest)
	{
		return isQuestCompleted(quest.getId());
	}

	public void setQuestState(final QuestState qs)
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

	public void removeQuestState(final int id)
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

	public void removeQuestState(final Quest quest)
	{
		removeQuestState(quest.getId());
	}

	public Quest[] getAllActiveQuests()
	{
		final List<Quest> quests = new ArrayList<>(_quests.size());
		questRead.lock();
		try
		{
			for(final QuestState qs : _quests.values())
				if(qs.isStarted())
					quests.add(qs.getQuest());
		}
		finally
		{
			questRead.unlock();
		}
		return quests.toArray(new Quest[0]);
	}

	public QuestState[] getAllQuestsStates()
	{
		questRead.lock();
		try
		{
			return _quests.values().toArray(new QuestState[_quests.size()]);
		}
		finally
		{
			questRead.unlock();
		}
	}

	public List<QuestState> getQuestsForEvent(final NpcInstance npc, final QuestEventType event)
	{
		final List<QuestState> states = new ArrayList<>();
		final Set<Quest> quests = npc.getTemplate().getEventQuests(event);
		if(quests != null)
			for(final Quest quest : quests)
			{
				final QuestState qs = getQuestState(quest);
				if(qs != null && !qs.isCompleted())
					states.add(getQuestState(quest));
			}
		return states;
	}

	public void processQuestEvent(final int questId, String event, final NpcInstance npc)
	{
		if(event == null)
			event = "";
		QuestState qs = getQuestState(questId);
		if(qs == null)
		{
			final Quest q = QuestHolder.getInstance().getQuest(questId);
			if(q == null)
			{
				Player._log.warn("Quest ID[" + questId + "] not found!");
				return;
			}
			qs = q.newQuestState(this);
		}
		if(qs == null || qs.isCompleted())
			return;
		qs.getQuest().notifyEvent(event, qs, npc);
		sendPacket(new QuestListPacket(this));
	}

	public boolean isInventoryFull()
	{
		return getWeightPenalty() >= 3 || getInventoryLimit() * 0.8 < getInventory().getSize();
	}

	public boolean isQuestContinuationPossible(final boolean msg)
	{
		if(isInventoryFull() || Config.QUEST_INVENTORY_MAXIMUM * 0.8 < getInventory().getQuestSize())
		{
			if(msg)
				sendPacket(SystemMsg.PROGRESS_IN_A_QUEST_IS_POSSIBLE_ONLY_WHEN_YOUR_INVENTORYS_WEIGHT_AND_SLOT_COUNT_ARE_LESS_THAN_80_PERCENT_OF_CAPACITY);
			return false;
		}
		return true;
	}

	public void stopQuestTimers()
	{
		for(final QuestState qs : getAllQuestsStates())
			if(qs.isStarted())
				qs.pauseQuestTimers();
			else
				qs.stopQuestTimers();
	}

	public void resumeQuestTimers()
	{
		for(final QuestState qs : getAllQuestsStates())
			qs.resumeQuestTimers();
	}

	public Collection<ShortCut> getAllShortCuts()
	{
		return _shortCuts.getAllShortCuts();
	}

	public ShortCut getShortCut(final int slot, final int page)
	{
		return _shortCuts.getShortCut(slot, page);
	}

	public void registerShortCut(final ShortCut shortcut)
	{
		_shortCuts.registerShortCut(shortcut);
	}

	public void deleteShortCut(final int slot, final int page)
	{
		_shortCuts.deleteShortCut(slot, page);
	}

	public void restoreShortCuts(Iterable<ShortCut> shortCuts)
	{
		_shortCuts.restore(shortCuts);
	}

	public void registerMacro(final Macro macro)
	{
		_macroses.registerMacro(macro);
	}

	public void deleteMacro(final int id)
	{
		_macroses.deleteMacro(id);
	}

	public MacroList getMacroses()
	{
		return _macroses;
	}

	public boolean isCastleLord(final int castleId)
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

	public void setLastAccess(final long value)
	{
		_lastAccess = value;
	}

	public int getRecomHave()
	{
		return _recomHave;
	}

	public void setRecomHave(final int value)
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
		final int targetRecom = target.getRecomHave();
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

	public void setKarma(final int karma)
	{
		if(_karma == karma)
			return;
		_karma = Math.min(0, karma);
		sendChanges();
		for(final Servitor servitor : getServitors())
			servitor.broadcastCharInfo();
	}

	@Override
	public int getMaxLoad()
	{
		return (int) calcStat(Stats.MAX_LOAD, 69000.0, this, null);
	}

	@Override
	public void updateEffectIcons()
	{
		if(entering || isLogoutStarted())
			return;
		super.updateEffectIcons();
	}

	@Override
	public void updateEffectIconsImpl()
	{
		final Abnormal[] effects = getAbnormalList().getFirstEffects();
		Arrays.sort(effects, EffectsComparator.getInstance());
		final PartySpelledPacket ps = new PartySpelledPacket(this, false);
		final AbnormalStatusUpdatePacket abnormalStatus = new AbnormalStatusUpdatePacket();
		for(final Abnormal effect : effects)
		{
			if(effect.checkAbnormalType(AbnormalType.hp_recover))
				sendPacket(new ShortBuffStatusUpdatePacket(effect));
			else
				effect.addIcon(abnormalStatus);
			if(_party != null)
				effect.addPartySpelledIcon(ps);
		}
		sendPacket(abnormalStatus);
		if(_party != null)
			_party.broadCast(ps);

		if(isInOlympiadMode() && isOlympiadCompStart() && _olympiadGame != null)
		{
			ExOlympiadSpelledInfoPacket olympiadSpelledInfo = new ExOlympiadSpelledInfoPacket();
			for(Abnormal effect : effects)
			{
				if(effect == null)
					continue;

				effect.addOlympiadSpelledIcon(this, olympiadSpelledInfo);
			}

			sendPacket(olympiadSpelledInfo);
			for(ObservePoint member : _olympiadGame.getObservers())
				member.sendPacket(olympiadSpelledInfo);
		}

		final List<SingleMatchEvent> events = getEvents(SingleMatchEvent.class);
		for(final SingleMatchEvent event : events)
			event.onEffectIconsUpdate(this, effects);
		super.updateEffectIconsImpl();
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
		final double weightproc = 100.0 * (getCurrentLoad() - calcStat(Stats.MAX_NO_PENALTY_LOAD, 0.0, this, null)) / getMaxLoad();
		int newWeightPenalty = 0;
		if(weightproc < 50.0)
			newWeightPenalty = 0;
		else if(weightproc < 66.6)
			newWeightPenalty = 1;
		else if(weightproc < 80.0)
			newWeightPenalty = 2;
		else if(weightproc < 100.0)
			newWeightPenalty = 3;
		else
			newWeightPenalty = 4;
		final int current = getWeightPenalty();
		if(current == newWeightPenalty)
			return;
		if(newWeightPenalty > 0)
			addSkill(SkillHolder.getInstance().getSkillEntry(4270, newWeightPenalty));
		else
			removeSkill(getKnownSkill(4270));
		sendSkillList();
		sendEtcStatusUpdate();
		updateStats();
	}

	public int getArmorsExpertisePenalty()
	{
		return getSkillLevel(6213, 0);
	}

	public int getWeaponsExpertisePenalty()
	{
		return getSkillLevel(6209, 0);
	}

	public int getExpertisePenalty(final ItemInstance item)
	{
		if(item.getTemplate().getType2() == 0)
			return getWeaponsExpertisePenalty();
		if(item.getTemplate().getType2() == 1 || item.getTemplate().getType2() == 2)
			return getArmorsExpertisePenalty();
		return 0;
	}

	public void refreshExpertisePenalty()
	{
		if(isLogoutStarted())
			return;
		int level;
		int skillLvl;
		for(level = (int) calcStat(Stats.GRADE_EXPERTISE_LEVEL, getLevel(), null, null), skillLvl = 0, skillLvl = 0; skillLvl < Player.EXPERTISE_LEVELS.length && level >= Player.EXPERTISE_LEVELS[skillLvl + 1]; ++skillLvl)
		{}
		skillLvl = Math.max(skillLvl, (int) calcStat(Stats.ADDITIONAL_EXPERTISE_INDEX));
		if(skillLvl == 7)
			--skillLvl;
		boolean skillUpdate = false;
		if(skillLvl > 0)
			while(skillLvl >= 1)
			{
				final SkillEntry skill = SkillHolder.getInstance().getSkillEntry(239, skillLvl);
				if(skill != null)
				{
					if(addSkill(skill, false) != skill)
					{
						skillUpdate = true;
						break;
					}
					break;
				}
				else
					--skillLvl;
			}
		final int expertiseIndex = getExpertiseIndex();
		int newWeaponPenalty = 0;
		int newArmorPenalty = 0;
		final ItemInstance[] paperdollItems;
		final ItemInstance[] items = paperdollItems = getInventory().getPaperdollItems();
		for(final ItemInstance item : paperdollItems)
			if(item != null)
			{
				final int crystaltype = item.getTemplate().getGrade().ordinal();
				if(item.getTemplate().getType2() == 0)
				{
					if(crystaltype > newWeaponPenalty)
						newWeaponPenalty = crystaltype;
				}
				else if((item.getTemplate().getType2() == 1 || item.getTemplate().getType2() == 2) && crystaltype > expertiseIndex)
				{
					if(item.getBodyPart() == 32768)
						++newArmorPenalty;
					++newArmorPenalty;
				}
			}
		newWeaponPenalty -= expertiseIndex;
		newWeaponPenalty = Math.max(0, Math.min(4, newWeaponPenalty));
		newArmorPenalty = Math.max(0, Math.min(4, newArmorPenalty));
		int weaponExpertise = getWeaponsExpertisePenalty();
		int armorExpertise = getArmorsExpertisePenalty();
		if(weaponExpertise != newWeaponPenalty)
		{
			if((weaponExpertise = newWeaponPenalty) > 0)
				addSkill(SkillHolder.getInstance().getSkillEntry(6209, weaponExpertise));
			else
				removeSkill(getKnownSkill(6209));
			skillUpdate = true;
		}
		if(armorExpertise != newArmorPenalty)
		{
			if((armorExpertise = newArmorPenalty) > 0)
				addSkill(SkillHolder.getInstance().getSkillEntry(6213, armorExpertise));
			else
				removeSkill(getKnownSkill(6213));
			skillUpdate = true;
		}
		if(skillUpdate)
		{
			getInventory().validateItemsSkills();
			sendSkillList();
			sendEtcStatusUpdate();
			updateStats();
		}
	}

	public int getPvpKills()
	{
		return _pvpKills;
	}

	public void setPvpKills(final int pvpKills)
	{
		_pvpKills = pvpKills;
		calcPvpReward();
	}

    public int getPvpDeaths()
    {
        return _pkKills;
    }

    public double getPvpKDRatio()
    {
		return getPvpDeaths() == 0 ? getPvpKills() : getPvpKills() / getPvpDeaths();
	}

	public ClassLevel getClassLevel()
	{
		return getClassId().getClassLevel();
	}

	public boolean isAcademyGraduated()
	{
		return getVarBoolean(ACADEMY_GRADUATED_VAR, false);
	}

	public void setClassId(final int id, final boolean noban) {
		setClassId(id, noban, false);
	}

	public synchronized void setClassId(final int id, final boolean noban, boolean onRestore)
	{
		PartyClassLimitService.getInstance().checkKickFromParty(this, id);
		final ClassId classId = ClassId.VALUES[id];
		if(!noban && !classId.equalsOrChildOf(getClassId()) && !getPlayerAccess().CanChangeClass && !Config.EVERYBODY_HAS_ADMIN_RIGHTS)
		{
			Thread.dumpStack();
			return;
		}
		final PlayerTemplate template = PlayerTemplateHolder.getInstance().getPlayerTemplate(getRace(), classId, getSex());
		if(template == null)
		{
			Player._log.error("Missing template for classId: " + id);
			return;
		}
		setTemplate(template);
		if(!_subClassList.containsClassId(id))
		{
			final SubClass cclass = getActiveSubClass();
			final ClassId oldClass = ClassId.VALUES[cclass.getClassId()];
			_subClassList.changeSubClassId(oldClass.getId(), id);
			changeClassInDb(oldClass.getId(), id);
			onReceiveNewClassId(oldClass, classId);
			storeCharSubClasses();
			getListeners().onClassChange(oldClass, classId, onRestore);
			for(final QuestState qs : getAllQuestsStates())
				qs.getQuest().notifyTutorialEvent("CE", "100", qs);
		}
		else
			getListeners().onClassChange(null, classId, onRestore);
		broadcastUserInfo(true);
		if(isInParty())
			getParty().broadCast(new PartySmallWindowUpdatePacket(this));
		if(getClan() != null)
			getClan().broadcastToOnlineMembers(new PledgeShowMemberListUpdatePacket(this));
		if(_matchingRoom != null)
			_matchingRoom.broadcastPlayerUpdate(this);
	}

	private void onReceiveNewClassId(final ClassId oldClass, final ClassId newClass)
	{
		if(oldClass != null)
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
				updateStats();
			}
			else
				rewardSkills(true);
	}

	public long getExp()
	{
		return getActiveSubClass() == null ? 0L : getActiveSubClass().getExp();
	}

	public long getMaxExp()
	{
		return getActiveSubClass() == null ? Experience.LEVEL[Experience.getMaxLevel() + 1] : getActiveSubClass().getMaxExp();
	}

	public void setEnchantScroll(final ItemInstance scroll)
	{
		_enchantScroll = scroll;
	}

	public ItemInstance getEnchantScroll()
	{
		return _enchantScroll;
	}

	public void addExpAndCheckBonus(final MonsterInstance mob, final double noRateExp, final double noRateSp)
	{
		if(getActiveSubClass() == null)
			return;

		if(isInClan())
			addClanRewardExp((int) (noRateExp / 72));

		final double neededExp = calcStat(Stats.SOULS_CONSUME_EXP, 0.0, mob, null);
		if(neededExp > 0.0 && noRateExp > neededExp)
		{
			mob.broadcastPacket(new ExSpawnEmitterPacket(mob, this));
			ThreadPoolManager.getInstance().schedule(new GameObjectTasks.SoulConsumeTask(this), 1000L);
		}
		final long normalExp = (long) (noRateExp * getRateExp() * (mob.isRaid() ? Config.RATE_XP_RAIDBOSS_MODIFIER : 1.0));
		final long normalSp = (long) (noRateSp * getRateSp());
		final long expWithoutBonus = (long) (noRateExp * Config.RATE_XP_BY_LVL[getLevel()]);
		final long spWithoutBonus = (long) (noRateSp * Config.RATE_SP_BY_LVL[getLevel()]);
		addExpAndSp(normalExp, normalSp, normalExp - expWithoutBonus, normalSp - spWithoutBonus, false, true, false);
	}

	@Override
	public void addExpAndSp(final long exp, final long sp)
	{
		addExpAndSp(exp, sp, -1L, -1L, false, false, Config.ALT_DELEVEL_ON_DEATH_PENALTY_MIN_LEVEL > -1 && getLevel() >= Config.ALT_DELEVEL_ON_DEATH_PENALTY_MIN_LEVEL);
	}

	public void addExpAndSp(final long exp, final long sp, final boolean delevel)
	{
		addExpAndSp(exp, sp, -1L, -1L, false, false, delevel);
	}

	public void addExpAndSp(long addToExp, long addToSp, final long bonusAddExp, final long bonusAddSp, final boolean applyRate, final boolean applyToPet, final boolean delevel)
	{
		if(getActiveSubClass() == null)
			return;
		if(applyRate)
		{
			addToExp = (long) (addToExp * getRateExp());
			addToSp = (long) (addToSp * getRateSp());
		}
		final PetInstance pet = getPet();
		if(addToExp > 0L)
		{
			if(applyToPet && pet != null && !pet.isDead() && !pet.getData().isOfType(PetType.SPECIAL))
				if(pet.getData().isOfType(PetType.KARMA))
				{
					pet.addExpAndSp(addToExp, 0L);
					addToExp = 0L;
				}
				else if(pet.getExpPenalty() > 0.0)
				{
					if(pet.getLevel() > getLevel() - 20 && pet.getLevel() < getLevel() + 5)
					{
						pet.addExpAndSp((long) (addToExp * pet.getExpPenalty()), 0L);
						addToExp = (long) (addToExp * (1.0 - pet.getExpPenalty()));
					}
					else
					{
						pet.addExpAndSp((long) (addToExp * pet.getExpPenalty() / 5.0), 0L);
						addToExp = (long) (addToExp * (1.0 - pet.getExpPenalty() / 5.0));
					}
				}
				else if(pet.isSummon())
					addToExp = (long) (addToExp * (1.0 - pet.getExpPenalty()));
			if(isPK() && !isInZoneBattle() && !isDead())
			{
				final int karmaLost = Formulas.calculateKarmaLost(this, addToExp);
				if(karmaLost > 0)
				{
					_karma += karmaLost;
					if(_karma > 0)
						_karma = 0;
					sendPacket(new SystemMessagePacket(SystemMsg.YOUR_FAME_HAS_BEEN_CHANGED_TO_S1).addNumber(_karma));
				}
			}
			final long max_xp = getVarBoolean("NoExp") || isInDuel() ? Experience.LEVEL[getLevel() + 1] - 1L : getMaxExp();
			addToExp = Math.min(addToExp, max_xp - getExp());
		}
		final int oldLvl = getActiveSubClass().getLevel();
		getActiveSubClass().addExp(addToExp, delevel);
		getActiveSubClass().addSp(addToSp);
		if(addToExp > 0L)
			_receivedExp += addToExp;
		if((addToExp > 0L || addToSp > 0L) && bonusAddExp >= 0L && bonusAddSp >= 0L)
			sendPacket(new SystemMessagePacket(SystemMsg.YOU_HAVE_ACQUIRED_S1_EXP_BONUS_S2_AND_S3_SP_BONUS_S4).addNumber(addToExp).addNumber(bonusAddExp).addNumber(addToSp).addNumber((int) bonusAddSp));
		else if(addToSp > 0L && addToExp == 0L)
			sendPacket(new SystemMessage(331).addNumber(addToSp));
		else if(addToSp > 0L && addToExp > 0L)
			sendPacket(new SystemMessage(95).addNumber(addToExp).addNumber(addToSp));
		else if(addToSp == 0L && addToExp > 0L)
			sendPacket(new SystemMessage(45).addNumber(addToExp));
		final int level = getActiveSubClass().getLevel();
		if(level != oldLvl)
		{
			levelSet(level - oldLvl);
			getListeners().onLevelChange(oldLvl, level);
		}
		if(pet != null && pet.getData().isOfType(PetType.SPECIAL))
		{
			pet.setLevel(getLevel());
			pet.setExp(pet.getExpForNextLevel());
			pet.broadcastStatusUpdate();
		}
		updateStats();
	}

	public void rewardSkills(final boolean send)
	{
		rewardSkills(send, true, Config.AUTO_LEARN_SKILLS);
	}

	public int rewardSkills(final boolean send, final boolean checkShortCuts, final boolean learnAllSkills)
	{
		if(_dontRewardSkills)
			return 0;
		final List<SkillLearn> skillLearns = new ArrayList<>(SkillAcquireHolder.getInstance().getAvailableNextLevelsSkills(this, AcquireType.NORMAL));
		Collections.sort(skillLearns);
		Collections.reverse(skillLearns);
		final IntObjectMap<SkillLearn> skillsToLearnMap = new HashIntObjectMap<>();
		for(final SkillLearn sl : skillLearns)
			if(!sl.isAutoGet() || !learnAllSkills && !sl.isFreeAutoGet())
				skillsToLearnMap.remove(sl.getId());
			else
			{
				if(skillsToLearnMap.containsKey(sl.getId()))
					continue;
				skillsToLearnMap.put(sl.getId(), sl);
			}
		boolean update = false;
		int addedSkillsCount = 0;
		for(final SkillLearn sl2 : skillsToLearnMap.values())
		{
			final SkillEntry skillEntry = SkillHolder.getInstance().getSkillEntry(sl2.getId(), sl2.getLevel());
			if(skillEntry == null)
				continue;

			if(addSkill(skillEntry, true) == null)
				++addedSkillsCount;
			if(checkShortCuts && !getAllShortCuts().isEmpty() && skillEntry.getLevel() > 1)
				updateSkillShortcuts(skillEntry.getId(), skillEntry.getLevel());
			update = true;
		}

		transformLock.lock();
		try
		{
			if(isTransformed())
			{
				boolean added = false;
				for(final SkillLearn sl3 : _transform.getAddtionalSkills())
					if(sl3.getMinLevel() <= getLevel())
					{
						SkillEntry skillEntry2 = _transformSkills.get(sl3.getId());
						if(skillEntry2 == null || skillEntry2.getLevel() < sl3.getLevel())
						{
							skillEntry2 = SkillHolder.getInstance().getSkillEntry(sl3.getId(), sl3.getLevel());
							if(skillEntry2 != null)
							{
								_transformSkills.remove(skillEntry2.getId());
								_transformSkills.put(skillEntry2.getId(), skillEntry2);
								update = true;
								added = true;
							}
						}
					}
				if(added)
					for(final SkillEntry skillEntry : _transformSkills.values())
						if(addSkill(skillEntry, false) == null)
							++addedSkillsCount;
			}
		}
		finally
		{
			transformLock.unlock();
		}

		updateStats();
		if(send && update)
			sendSkillList();
		return addedSkillsCount;
	}

	public Race getRace()
	{
		return ClassId.VALUES[getBaseClassId()].getRace();
	}

	public ClassType getBaseClassType()
	{
		return ClassId.VALUES[getBaseClassId()].getType();
	}

	public long getSp()
	{
		return getActiveSubClass() == null ? 0L : getActiveSubClass().getSp();
	}

	public void setSp(final long sp)
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
		return (int) (_onlineBeginTime > 0L ? (_onlineTime + System.currentTimeMillis() - _onlineBeginTime) / 1000L : _onlineTime / 1000L);
	}

	public long getOnlineBeginTime()
	{
		return _onlineBeginTime;
	}

	public boolean startChatBlock()
	{
		return _chatBlocked.getAndSet(true);
	}

	public boolean stopChatBlock()
	{
		return _chatBlocked.setAndGet(false);
	}

	public boolean isChatBlocked()
	{
		return _chatBlocked.get();
	}

	public boolean startPartyBlock()
	{
		return _partyBlocked.getAndSet(true);
	}

	public boolean stopPartyBlock()
	{
		return _partyBlocked.setAndGet(false);
	}

	public boolean isPartyBlocked()
	{
		return _partyBlocked.get();
	}

	public boolean startVioletBoy()
	{
		return _violetBoy.getAndSet(true);
	}

	public boolean stopVioletBoy()
	{
		return _violetBoy.setAndGet(false);
	}

	public boolean isVioletBoy()
	{
		return _violetBoy.get();
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
		if(_leaveClanTime == 0L)
			return true;
		if(System.currentTimeMillis() - _leaveClanTime >= Config.ALT_CLAN_LEAVE_PENALTY_TIME * 60 * 60 * 1000L)
		{
			_leaveClanTime = 0L;
			return true;
		}
		return false;
	}

	public boolean canCreateClan()
	{
		if(_deleteClanTime == 0L)
			return true;
		if(System.currentTimeMillis() - _deleteClanTime >= Config.ALT_CLAN_CREATE_PENALTY_TIME * 60 * 60 * 1000L)
		{
			_deleteClanTime = 0L;
			return true;
		}
		return false;
	}

	public IBroadcastPacket canJoinParty(final Player inviter)
	{
		final Request request = getRequest();
		if(request != null && request.isInProgress() && request.getOtherPlayer(this) != inviter)
			return SystemMsg.WAITING_FOR_ANOTHER_REPLY.packet(inviter);
		if(isBlockAll() || getMessageRefusal())
			return SystemMsg.THAT_PERSON_IS_IN_MESSAGE_REFUSAL_MODE.packet(inviter);
		if(isInParty())
			return new SystemMessagePacket(SystemMsg.C1_IS_A_MEMBER_OF_ANOTHER_PARTY_AND_CANNOT_BE_INVITED).addName(this);
		if(isPartyBlocked())
			return new SystemMessagePacket(SystemMsg.C1_HAS_BEEN_REPORTED_AS_AN_ILLEGAL_PROGRAM_USER_AND_CANNOT_JOIN_A_PARTY).addName(this);
		if(inviter.getReflection() != getReflection() && !inviter.getReflection().isMain() && !getReflection().isMain())
			return SystemMsg.INVALID_TARGET.packet(inviter);
		if(inviter.isInOlympiadMode() || isInOlympiadMode())
			return SystemMsg.A_USER_CURRENTLY_PARTICIPATING_IN_THE_OLYMPIAD_CANNOT_SEND_PARTY_AND_FRIEND_INVITATIONS.packet(inviter);
		if(!inviter.getPlayerAccess().CanJoinParty || !getPlayerAccess().CanJoinParty)
			return SystemMsg.INVALID_TARGET.packet(inviter);
		return null;
	}

	@Override
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

	public void setSitting(final boolean val)
	{
		_isSitting = val;
	}

	public boolean getSittingTask()
	{
		return sittingTaskLaunched;
	}

	@Override
	public void sitDown(final StaticObjectInstance throne)
	{
		if(isSitting() || sittingTaskLaunched || isAlikeDead())
			return;
		if(isStunned() || isSleeping() || isDecontrolled() || isAttackingNow() || isCastingNow() || isMoving())
		{
			getAI().setNextAction(PlayableAI.AINextAction.REST, null, null, false, false);
			return;
		}
		resetWaitSitTime();
		getAI().setIntention(CtrlIntention.AI_INTENTION_REST, null, null);
		if(throne == null)
			broadcastPacket(new ChangeWaitTypePacket(this, 0));
		else
			broadcastPacket(new ChairSitPacket(this, throne));
		_sittingObject = throne;
		setSitting(true);
		sittingTaskLaunched = true;
		ThreadPoolManager.getInstance().schedule(new GameObjectTasks.EndSitDownTask(this), 2500L);
	}

	@Override
	public void standUp()
	{
		if(!isSitting() || sittingTaskLaunched || isInStoreMode() || isPrivateBuffer() || isAlikeDead())
			return;
		getAbnormalList().stopEffects(EffectType.Relax);
		getAI().clearNextAction();
		broadcastPacket(new ChangeWaitTypePacket(this, 1));
		_sittingObject = null;
		sittingTaskLaunched = true;
		ThreadPoolManager.getInstance().schedule(new GameObjectTasks.EndStandUpTask(this), 2500L);
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

	public boolean reduceAdena(final long adena)
	{
		return reduceAdena(adena, false);
	}

	public boolean reduceAdena(final long adena, final boolean notify)
	{
		if(adena < 0L)
			return false;
		if(adena == 0L)
			return true;
		final boolean result = getInventory().reduceAdena(adena);
		if(notify && result)
			sendPacket(SystemMessagePacket.removeItems(57, adena));
		return result;
	}

	public ItemInstance addAdena(final long adena)
	{
		return addAdena(adena, false);
	}

	public ItemInstance addAdena(final long adena, final boolean notify)
	{
		if(adena < 1L)
			return null;
		final ItemInstance item = getInventory().addAdena(adena);
		if(item != null && notify)
			sendPacket(SystemMessagePacket.obtainItems(57, adena, 0));
		return item;
	}

	public GameClient getNetConnection()
	{
		return _connection;
	}

	public int getRevision()
	{
		final GameClient connection = _connection;
		return connection == null ? 0 : connection.getRevision();
	}

	public void setNetConnection(final GameClient connection)
	{
		_connection = connection;
	}

	public boolean isConnected()
	{
		if(this.isPhantom())
			return true;
		final GameClient connection = _connection;
		return connection != null && connection.isConnected();
	}

	@Override
	public void onAction(final Player player, final boolean shift)
	{
		if(!isTargetable(player))
		{
			player.sendActionFailed();
			return;
		}
		if(isFrozen())
		{
			player.sendPacket(ActionFailPacket.STATIC);
			return;
		}
		if(shift && OnShiftActionHolder.getInstance().callShiftAction(player, Player.class, this, true))
			return;
		if(player.getTarget() != this)
		{
			player.setTarget(this);
			if(player.getTarget() != this)
				player.sendPacket(ActionFailPacket.STATIC);
		}
		else if(getPrivateStoreType() != 0 || isPrivateBuffer())
		{
			if(!checkInteractionDistance(player) && player.getAI().getIntention() != CtrlIntention.AI_INTENTION_INTERACT)
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
		sendPacket(makeStatusUpdate(10, 12, 34, 9, 11, 33, 35));
		broadcastPacketToOthers(makeStatusUpdate(10, 12, 34, 9, 11, 33, 35));

		if(isInParty())
			getParty().broadcastToPartyMembers(this, new PartySmallWindowUpdatePacket(this));

		final List<SingleMatchEvent> events = getEvents(SingleMatchEvent.class);
		for(final SingleMatchEvent event : events)
			event.onStatusUpdate(this);

		if(isInOlympiadMode() && isOlympiadCompStart() && _olympiadGame != null)
			_olympiadGame.broadcastInfo(this, null, false);
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
		if(Config.BROADCAST_CHAR_INFO_INTERVAL == 0L)
			force = true;
		if(force)
		{
			if(_broadcastCharInfoTask != null)
			{
				_broadcastCharInfoTask.cancel(false);
				_broadcastCharInfoTask = null;
			}
			broadcastCharInfoImpl(new IUpdateTypeComponent[0]);
			return;
		}
		if(_broadcastCharInfoTask != null)
			return;
		_broadcastCharInfoTask = ThreadPoolManager.getInstance().schedule(new BroadcastCharInfoTask(), Config.BROADCAST_CHAR_INFO_INTERVAL);
	}

	public void setPolyId(final int polyid)
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
	public void broadcastCharInfoImpl(final IUpdateTypeComponent... components)
	{
		if(!isVisible())
			return;
		for(final Player target : World.getAroundObservers(this))
		{
			if(isInvisible(target))
				continue;
			target.sendPacket(isPolymorphed() ? new NpcInfoPoly(this) : new CIPacket(this, target));
			target.sendPacket(new RelationChangedPacket(this, target));
		}
	}

	public void sendEtcStatusUpdate()
	{
		if(!isVisible())
			return;
		sendPacket(new EtcStatusUpdatePacket(this));
	}

	private void sendUserInfoImpl()
	{
		sendPacket(new UIPacket(this));
	}

	public void sendUserInfo()
	{
		sendUserInfo(false);
	}

	public void sendUserInfo(final boolean force)
	{
		if(!isVisible() || entering || isLogoutStarted())
			return;
		if(Config.USER_INFO_INTERVAL == 0L || force)
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

	public void sendSkillList(final int learnedSkillId)
	{
		sendPacket(new SkillListPacket(this, learnedSkillId));
		sendPacket(new AcquireSkillListPacket(this));
	}

	public void sendSkillList()
	{
		sendSkillList(0);
	}

	public void updateSkillShortcuts(final int skillId, final int skillLevel)
	{
		for(final ShortCut sc : getAllShortCuts())
			if(sc.getId() == skillId && sc.getType() == 2)
			{
				final ShortCut newsc = new ShortCut(sc.getSlot(), sc.getPage(), sc.getType(), sc.getId(), skillLevel, 1);
				sendPacket(new ShortCutRegisterPacket(this, newsc));
				registerShortCut(newsc);
			}
	}

	@Override
	public StatusUpdatePacket makeStatusUpdate(final int... fields)
	{
		final StatusUpdatePacket su = new StatusUpdatePacket(getObjectId(), getObjectId());
		for(final int field : fields)
			switch(field)
			{
				case 9:
				{
					su.addAttribute(field, (int) getCurrentHp());
					break;
				}
				case 10:
				{
					su.addAttribute(field, getMaxHp());
					break;
				}
				case 11:
				{
					su.addAttribute(field, (int) getCurrentMp());
					break;
				}
				case 12:
				{
					su.addAttribute(field, getMaxMp());
					break;
				}
				case 14:
				{
					su.addAttribute(field, getCurrentLoad());
					break;
				}
				case 15:
				{
					su.addAttribute(field, getMaxLoad());
					break;
				}
				case 26:
				{
					su.addAttribute(field, getPvpFlag());
					break;
				}
				case 27:
				{
					su.addAttribute(field, getKarma());
					break;
				}
				case 33:
				{
					su.addAttribute(field, (int) getCurrentCp());
					break;
				}
				case 34:
				{
					su.addAttribute(field, getMaxCp());
					break;
				}
			}
		return su;
	}

	public void sendStatusUpdate(final boolean broadCast, final boolean withPet, final int... fields)
	{
		if(fields.length == 0 || entering && !broadCast)
			return;
		final StatusUpdatePacket su = makeStatusUpdate(fields);
		if(!su.hasAttributes())
			return;
		final List<L2GameServerPacket> packets = new ArrayList<>(withPet ? 2 : 1);
		if(withPet)
			for(final Servitor servitor : getServitors())
				packets.add(servitor.makeStatusUpdate(fields));
		packets.add(su);
		if(!broadCast)
			sendPacket(packets);
		else if(entering)
			broadcastPacketToOthers(packets);
		else
			broadcastPacket(packets);
	}

	public int getAllyId()
	{
		return _clan == null ? 0 : _clan.getAllyId();
	}

	@Override
	public void sendPacket(final IBroadcastPacket p)
	{
		if(p == null)
			return;

		if(!isConnected())
			return;

		L2GameServerPacket gsp = p.packet(this);
		if(gsp == null)
			return;

		final GameClient connection = _connection;
		if (connection != null && connection.isConnected()) {
			gsp.onSendPacket(this);
			connection.sendPacket(gsp);
		}
	}

	@Override
	public void sendPacket(final IBroadcastPacket... packets)
	{
		if(!isConnected())
			return;

		for(IBroadcastPacket p : packets)
			sendPacket(p);
	}

	@Override
	public void sendPacket(final List<? extends IBroadcastPacket> packets)
	{
		if(!isConnected())
			return;

        for(IBroadcastPacket p : packets)
			sendPacket(p);
	}

	public void doInteract(final GameObject target)
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
				final Player temp = (Player) target;

				if(!getFraction().canAttack(temp.getFraction()))
				{
					if(temp.getPrivateStoreType() == 1 || temp.getPrivateStoreType() == 8)
						sendPacket(new PrivateStoreList(this, temp));
					else if(temp.getPrivateStoreType() == 3)
						sendPacket(new PrivateStoreBuyList(this, temp));
					else if(temp.getPrivateStoreType() == 5)
						sendPacket(new RecipeShopSellListPacket(this, temp));
					else if (temp.isPrivateBuffer()) {
						temp.getPrivateBuffer().sendList(this, 0);
					}
				}
				sendActionFailed();
			}
			else if(getAI().getIntention() != CtrlIntention.AI_INTENTION_INTERACT)
				getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this, null);
		}
		else
			target.onAction(this, false);
	}

	public void doAutoLootOrDrop(final ItemInstance item, final NpcInstance fromNpc)
	{
		if(fromNpc.dropOnTheGround())
		{
			item.dropToTheGround(this, fromNpc);
			return;
		}

		final boolean forceAutoloot = fromNpc.isFlying() || getReflection().isAutolootForced();
		if((fromNpc.isRaid() || fromNpc instanceof ReflectionBossInstance) && !Config.AUTO_LOOT_FROM_RAIDS && !item.isHerb() && !forceAutoloot)
		{
			item.dropToTheGround(this, fromNpc);
			return;
		}
		if(item.isHerb())
		{
			if(!AutoLootHerbs && !forceAutoloot)
			{
				item.dropToTheGround(this, fromNpc);
				return;
			}
			for(final SkillEntry skillEntry : item.getTemplate().getAttachedSkills())
			{
				altUseSkill(skillEntry.getTemplate(), this);
				for(final Servitor servitor : getServitors())
					if(servitor.isSummon() && !servitor.isDead())
						servitor.altUseSkill(skillEntry.getTemplate(), servitor);
			}
			item.deleteMe();
		}
		else
		{
			if(!forceAutoloot && (!_autoLoot || !Config.AUTO_LOOT_ITEM_ID_LIST.isEmpty() && !Config.AUTO_LOOT_ITEM_ID_LIST.contains(item.getItemId())) && (!_autoLootOnlyAdena || !item.getTemplate().isAdena()))
			{
				item.dropToTheGround(this, fromNpc);
				return;
			}
			if(!isInParty())
			{
				if(!pickupItem(item, ItemLogProcess.Pickup))
				{
					item.dropToTheGround(this, fromNpc);
					return;
				}
			}
			else
				getParty().distributeItem(this, item, fromNpc);
			broadcastPickUpMsg(item);
		}
	}

	@Override
	public void doPickupItem(final GameObject object)
	{
		if(!object.isItem())
		{
			Player._log.warn("trying to pickup wrong target." + getTarget());
			return;
		}
		sendActionFailed();
		stopMove();
		final ItemInstance item = (ItemInstance) object;
		synchronized (item)
		{
			if(!item.isVisible())
				return;
			if(!ItemFunctions.checkIfCanPickup(this, item))
			{
				SystemMessage sm;
				if(item.getItemId() == 57)
				{
					sm = new SystemMessage(55);
					sm.addNumber(item.getCount());
				}
				else
				{
					sm = new SystemMessage(56);
					sm.addItemName(item.getItemId());
				}
				sendPacket(sm);
				return;
			}
			if(item.isHerb())
			{
				for(final SkillEntry skillEntry : item.getTemplate().getAttachedSkills())
					altUseSkill(skillEntry.getTemplate(), this);
				broadcastPacket(new GetItemPacket(item, getObjectId()));
				item.deleteMe();
				return;
			}
			final FlagItemAttachment attachment = item.getAttachment() instanceof FlagItemAttachment ? (FlagItemAttachment) item.getAttachment() : null;
			if(!isInParty() || attachment != null)
			{
				if(pickupItem(item, ItemLogProcess.Pickup))
				{
					broadcastPacket(new GetItemPacket(item, getObjectId()));
					broadcastPickUpMsg(item);
					item.pickupMe();
				}
			}
			else
				getParty().distributeItem(this, item, null);
		}
	}

	public boolean pickupItem(final ItemInstance item, final ItemLogProcess itemLogProcess)
	{
		final PickableAttachment attachment = item.getAttachment() instanceof PickableAttachment ? (PickableAttachment) item.getAttachment() : null;
		if(!ItemFunctions.canAddItem(this, item))
			return false;

		ItemLogMessage message = new ItemLogMessage(this, itemLogProcess, item);
		LogService.getInstance().log(LoggerType.ITEM, message);

		sendPacket(SystemMessagePacket.obtainItems(item));
		getInventory().addItem(item);
		if(attachment != null)
			attachment.pickUp(this);
		getListeners().onPickupItem(item);
		sendChanges();
		return true;
	}

	public void setNpcTarget(final GameObject target)
	{
		setTarget(target);
		if(target == null)
			return;
		if(target == getTarget() && target.isNpc())
		{
			final NpcInstance npc = (NpcInstance) target;
			sendPacket(npc.makeStatusUpdate(9, 10));
			sendPacket(new ValidateLocationPacket(npc), ActionFailPacket.STATIC);
		}
	}

	@Override
	public void setTarget(GameObject newTarget)
	{
		if(newTarget != null && !newTarget.isVisible())
			newTarget = null;
		final GameObject oldTarget = getTarget();
		if(oldTarget != null)
		{
			if(oldTarget.equals(newTarget))
				return;
			broadcastPacket(new TargetUnselectedPacket(this));
		}
		if(newTarget != null)
		{
			broadcastTargetSelected(newTarget);
			if(newTarget.isCreature())
				sendPacket(((Creature) newTarget).getAbnormalStatusUpdate());
		}
		if(newTarget != null && newTarget != this && getDecoys() != null && !getDecoys().isEmpty() && newTarget.isCreature())
			for(final DecoyInstance dec : getDecoys())
			{
				if(dec == null)
					continue;
				if(dec.getAI() == null)
					Player._log.info("This decoy has NULL AI");
				else
				{
					if(newTarget.isCreature())
					{
						final Creature _nt = (Creature) newTarget;
						if(_nt.isInPeaceZone())
							continue;
					}
					dec.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, newTarget, 1000);
				}
			}
		super.setTarget(newTarget);
	}

	public void broadcastTargetSelected(final GameObject newTarget)
	{
		sendPacket(new MyTargetSelectedPacket(this, newTarget));
		broadcastPacket(new TargetSelectedPacket(getObjectId(), newTarget.getObjectId(), getLoc()));
	}

	@Override
	public ItemInstance getActiveWeaponInstance()
	{
		return getInventory().getPaperdollItem(7);
	}

	@Override
	public WeaponTemplate getActiveWeaponTemplate()
	{
		final ItemInstance weapon = getActiveWeaponInstance();
		if(weapon == null)
			return null;
		final ItemTemplate template = weapon.getTemplate();
		if(template == null)
			return null;
		if(!(template instanceof WeaponTemplate))
		{
			Player._log.warn("Template in active weapon not WeaponTemplate! (Item ID[" + weapon.getItemId() + "])");
			return null;
		}
		return (WeaponTemplate) template;
	}

	@Override
	public ItemInstance getSecondaryWeaponInstance()
	{
		return getInventory().getPaperdollItem(8);
	}

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

	public ArmorTemplate.ArmorType getWearingArmorType()
	{
		final ItemInstance chest = getInventory().getPaperdollItem(10);
		if(chest == null)
			return ArmorTemplate.ArmorType.NONE;
		final ItemType chestItemType = chest.getItemType();
		if(!(chestItemType instanceof ArmorTemplate.ArmorType))
			return ArmorTemplate.ArmorType.NONE;
		final ArmorTemplate.ArmorType chestArmorType = (ArmorTemplate.ArmorType) chestItemType;
		if(chest.getBodyPart() == 32768)
			return chestArmorType;
		final ItemInstance legs = getInventory().getPaperdollItem(11);
		if(legs == null)
			return ArmorTemplate.ArmorType.NONE;
		if(legs.getItemType() != chestArmorType)
			return ArmorTemplate.ArmorType.NONE;
		return chestArmorType;
	}

	@Override
	public void reduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp,
								boolean directHp, boolean canReflectAndAbsorb, boolean transferDamage, boolean isDot,
								boolean sendReceiveMessage, boolean sendGiveMessage, boolean crit, boolean miss,
								boolean shld, boolean magic)
	{
		if(attacker == null || isDead() || attacker.isDead() && !isDot)
			return;
		if(attacker.isPlayer() && Math.abs(attacker.getLevel() - getLevel()) > 10)
		{
			if(attacker.isPK() && getAbnormalList().containsEffects(5182) && !isInSiegeZone())
				return;
			if(isPK() && attacker.getAbnormalList().containsEffects(5182) && !attacker.isInSiegeZone())
				return;
		}

		super.reduceCurrentHp(damage, attacker, skill, awake, standUp, directHp, canReflectAndAbsorb, transferDamage, isDot, sendReceiveMessage, sendGiveMessage, crit, miss, shld, magic);
	}

	@Override
	protected void onReduceCurrentHp(double damage, final Creature attacker, final Skill skill, final boolean awake, final boolean standUp, final boolean directHp, final boolean isDot)
	{
		if(damage <= 0.0)
			return;
		if(standUp)
		{
			standUp();
			if(isFakeDeath())
				breakFakeDeath();
		}
		final double originDamage = damage;
		if(attacker.isPlayable() && !directHp && getCurrentCp() > 0.0)
		{
			double cp = getCurrentCp();
			if(cp >= damage)
			{
				cp -= damage;
				damage = 0.0;
			}
			else
			{
				damage -= cp;
				cp = 0.0;
			}
			setCurrentCp(cp);
		}
		double hp = getCurrentHp();

		if(isInOlympiadMode())
		{
			OlympiadGame game = _olympiadGame;
			if(this != attacker && (skill == null || skill.isOffensive()))
			{
				game.addDamage(this, Math.min(hp, originDamage));
			}
			if(hp <= damage && !isDeathImmune())
			{
				game.setWinner(getOlympiadSide() == 1 ? 2 : 1);
				game.endGame(20, false);
				setCurrentHp(1.0, false);
				attacker.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				attacker.sendActionFailed();
				return;
			}
		}

		if(attacker.isPlayable())
			getDamageList().addDamage(attacker.getPlayable(), (int) originDamage);

		if(calcStat(Stats.RestoreHPGiveDamage) == 1.0 && Rnd.chance(1))
			setCurrentHp(getCurrentHp() + getMaxHp() / 10.0D, false);
		super.onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp, isDot);
	}

	private void altDeathPenalty(final Creature killer)
	{
		if(!Config.ALT_GAME_DELEVEL)
			return;
		if(isInZoneBattle())
			return;
		deathPenalty(killer);
	}

	public final boolean atWarWith(final Player player)
	{
		return _clan != null && player.getClan() != null && getPledgeType() != -1 && player.getPledgeType() != -1 && (_clan.isAtWarWith(player.getClan()) || player.getClan().isAtWarWith(_clan));
	}

	public boolean atMutualWarWith(final Player player)
	{
		return _clan != null && player.getClan() != null && getPledgeType() != -1 && player.getPledgeType() != -1 && _clan.isAtWarWith(player.getClan()) && player.getClan().isAtWarWith(_clan);
	}

	public final void doPurePk(final Player killer)
	{
		final int pkCountMulti = (int) Math.max(killer.getPkKills() * Config.KARMA_PENALTY_DURATION_INCREASE, 1.0);
		killer.decreaseKarma(Config.KARMA_MIN_KARMA_PC * pkCountMulti);
		killer.setPkKills(killer.getPkKills() + 1);
	}

	public final void doKillInPeace(final Player killer)
	{
		if(!isPK())
			doPurePk(killer);
		else
		{
			final String var = "@pk_kill_" + getObjectId();
			if(!killer.getVarBoolean(var))
			{
				final long expirationTime = System.currentTimeMillis() + 1800000L;
				killer.setVar(var, true, expirationTime);
			}
		}
	}

	public void checkAddItemToDrop(final List<ItemInstance> array, final List<ItemInstance> items, final int maxCount)
	{
		for(int i = 0; i < maxCount && !items.isEmpty(); ++i)
			array.add(items.remove(Rnd.get(items.size())));
	}

	public FlagItemAttachment getActiveWeaponFlagAttachment()
	{
		final ItemInstance item = getActiveWeaponInstance();
		if(item == null || !(item.getAttachment() instanceof FlagItemAttachment))
			return null;
		return (FlagItemAttachment) item.getAttachment();
	}

	protected void doPKPVPManage(Creature killer)
	{
		final FlagItemAttachment attachment = getActiveWeaponFlagAttachment();

		if(attachment != null)
			attachment.onDeath(this, killer);
		if(killer == null || isMyServitor(killer.getObjectId()) || killer == this)
			return;
		if(killer.isServitor() && (killer = killer.getPlayer()) == null)
			return;
		if(isInZoneBattle() || killer.isInZoneBattle())
			return;
		if(killer.getTeam() != TeamType.NONE && getTeam() != TeamType.NONE)
			return;

		if(killer.isPlayer() || killer instanceof FakePlayer)
		{
			final Player pk = killer.getPlayer();
			final boolean war = atWarWith(pk);
			final boolean mutalWar = atMutualWarWith(pk);
			if(war || mutalWar)
			{
				final ClanWar clanWar = _clan.getClanWar(pk.getClan());
				if(clanWar != null)
					clanWar.onKill(pk, this);
			}

			Player assistant = getDamageList().getAssistant(pk);

			long currentTime = System.currentTimeMillis();
			var debuffers = getAbnormalList().getDebuffs().entrySet().stream()
					.filter(entry -> currentTime - entry.getValue() <= TimeUnit.SECONDS.toMillis(60))
					.map(entry -> entry.getKey().getEffector())
					.map(Creature::getPlayer)
					.distinct()
					.filter(player -> player != null && !Objects.equals(player, this)
							&& !Objects.equals(player, pk) && !Objects.equals(player, assistant))
					.collect(Collectors.toUnmodifiableList());

			GveRewardManager.getInstance().tryGiveReward(pk, assistant, debuffers, this);

			setPkKills(getPkKills() + 1);

/*			if(isInSiegeZone())
				return;*/
			final Castle castle = getCastle();
			if(getPvpFlag() > 0 || mutalWar || castle != null && castle.getResidenceSide() == ResidenceSide.DARK || getFraction().canAttack(killer.getFraction()) || isInZone(Zone.ZoneType.NoLimitPvp) && killer.isInZone(Zone.ZoneType.NoLimitPvp))
				pk.setPvpKills(pk.getPvpKills() + 1);

			pk.sendChanges();
		}
		final int karma = _karma;
		if(isPK())
		{
			increaseKarma(Config.KARMA_LOST_BASE);
			if(_karma > 0)
				_karma = 0;
		}

		boolean isPvP = killer.isPlayable() || killer instanceof GuardInstance;

		if(killer.isMonster() && !Config.DROP_ITEMS_ON_DIE || killer.isRaid() || isPvP && (_pkKills < Config.MIN_PK_TO_ITEMS_DROP || karma >= 0 && Config.KARMA_NEEDED_TO_DROP) || !killer.isMonster() && !isPvP)
			return;

		if(!Config.KARMA_DROP_GM && isGM())
			return;

		int max_drop_count = isPvP ? Config.KARMA_DROP_ITEM_LIMIT : 1;

		double dropRate;

		if(isPvP)
			dropRate = _pkKills * Config.KARMA_DROPCHANCE_MOD + Config.KARMA_DROPCHANCE_BASE;
		else
			dropRate = Config.NORMAL_DROPCHANCE_BASE;

		int dropEquipCount = 0;
		int dropWeaponCount = 0;
		int dropItemCount = 0;

		for(int i = 0; i < Math.ceil(dropRate / 100.0) && i < max_drop_count; ++i)
			if(Rnd.chance(dropRate))
			{
				final int rand = Rnd.get(Config.DROPCHANCE_EQUIPPED_WEAPON + Config.DROPCHANCE_EQUIPMENT + Config.DROPCHANCE_ITEM) + 1;
				if(rand > Config.DROPCHANCE_EQUIPPED_WEAPON + Config.DROPCHANCE_EQUIPMENT)
					++dropItemCount;
				else if(rand > Config.DROPCHANCE_EQUIPPED_WEAPON)
					++dropEquipCount;
				else
					++dropWeaponCount;
			}
		getInventory().writeLock();
		try
		{
			final List<ItemInstance> dropWeapon = new ArrayList<>();
			final List<ItemInstance> dropEquip = new ArrayList<>();
			final List<ItemInstance> dropItem = new ArrayList<>();
			for(final ItemInstance item : getInventory().getItems())
				if(item.canBeDropped(this, true))
					if(!Config.KARMA_LIST_NONDROPPABLE_ITEMS.contains(item.getItemId()))
						if(item.getTemplate().getType2() == 0)
							dropWeapon.add(item);
						else if(item.getTemplate().getType2() == 1 || item.getTemplate().getType2() == 2)
							dropEquip.add(item);
						else if(item.getTemplate().getType2() == 5)
							dropItem.add(item);
			final List<ItemInstance> drop = new ArrayList<>();
			checkAddItemToDrop(drop, dropWeapon, dropWeaponCount);
			checkAddItemToDrop(drop, dropEquip, dropEquipCount);
			checkAddItemToDrop(drop, dropItem, dropItemCount);
			if(drop.isEmpty())
				return;
			for(ItemInstance item2 : drop)
			{
				item2 = getInventory().removeItem(item2);

				ItemLogMessage message = new ItemLogMessage(this, ItemLogProcess.PvPDrop, item2);
				LogService.getInstance().log(LoggerType.ITEM, message);

				if(item2.getEnchantLevel() > 0)
					sendPacket(new SystemMessage(375).addNumber(item2.getEnchantLevel()).addItemName(item2.getItemId()));
				else
					sendPacket(new SystemMessage(298).addItemName(item2.getItemId()));
				if(killer.isPlayable() && (Config.AUTO_LOOT && Config.AUTO_LOOT_PK || isInFlyingTransform()))
				{
					killer.getPlayer().getInventory().addItem(item2);

					message = new ItemLogMessage(this, ItemLogProcess.Pickup, item2);
					LogService.getInstance().log(LoggerType.ITEM, message);

					killer.getPlayer().sendPacket(SystemMessagePacket.obtainItems(item2));
				}
				else
					item2.dropToTheGround(this, Location.findAroundPosition(this, Config.KARMA_RANDOM_DROP_LOCATION_LIMIT));
			}
		}
		finally
		{
			getInventory().writeUnlock();
		}
	}

	@Override
	protected void onDeath(final Creature killer)
	{
		if(isInStoreMode())
		{
			if(!isInOfflineMode())
				setPrivateStoreType(0);
			else
			{
				ThreadPoolManager.getInstance().schedule(() ->
				{
                    if(isInOfflineMode() && isDead())
                        doRevive();
				}, 10, TimeUnit.MINUTES);
			}
		}
		if(isProcessingRequest())
		{
			final Request request = getRequest();
			if(isInTrade())
			{
				final Player parthner = request.getOtherPlayer(this);
				sendPacket(TradeDonePacket.FAIL);
				parthner.sendPacket(TradeDonePacket.FAIL);
			}
			request.cancel(new IBroadcastPacket[0]);
		}
		if(isFishing())
			getFishing().stop();

		if (isPrivateBuffer())
			cancelPrivateBuffer();

		_comboKills = 0;

		//setAgathion(0);
		doPKPVPManage(killer);
		altDeathPenalty(killer);
		setIncreasedForce(0);
		stopWaterTask();
		if(!isSalvation() && isInSiegeZone() && isCharmOfCourage())
		{
			ask(new ConfirmDlgPacket(SystemMsg.YOUR_CHARM_OF_COURAGE_IS_TRYING_TO_RESURRECT_YOU, 60000), new ReviveAnswerListener(this, 100.0, false));
			setCharmOfCourage(false);
		}
		for(final QuestState qs : getAllQuestsStates())
			qs.getQuest().notifyTutorialEvent("CE", "200", qs);
		if(isMounted())
			_mount.onDeath();
		for(final Servitor servitor : getServitors())
			servitor.notifyMasterDeath();

		getDamageList().clear();

		super.onDeath(killer);
	}

	public void restoreExp()
	{
		restoreExp(100.0);
	}

	public void restoreExp(final double percent)
	{
		if(percent == 0.0)
			return;
		long lostexp = 0L;
		final String lostexps = getVar("lostexp");
		if(lostexps != null)
		{
			lostexp = Long.parseLong(lostexps);
			unsetVar("lostexp");
		}
		if(lostexp != 0L)
			addExpAndSp((long) (lostexp * percent / 100.0), 0L);
	}

	public void deathPenalty(final Creature killer)
	{
		if(killer == null)
			return;
		final boolean atwar = killer.getPlayer() != null && atMutualWarWith(killer.getPlayer());
		final int level = getLevel();
		double percentLost = Config.PERCENT_LOST_ON_DEATH[getLevel()];
		if(isPK())
			percentLost *= Config.PERCENT_LOST_ON_DEATH_MOD_FOR_PK;
		else if(isInPeaceZone())
			percentLost *= Config.PERCENT_LOST_ON_DEATH_MOD_IN_PEACE_ZONE;
		else if(atwar)
			percentLost *= Config.PERCENT_LOST_ON_DEATH_MOD_IN_WAR;
		else if(killer.getPlayer() != null && killer.getPlayer() != this)
			percentLost *= Config.PERCENT_LOST_ON_DEATH_MOD_IN_PVP;

		if(percentLost <= 0.0)
			return;

		long lostexp = (long) ((Experience.LEVEL[level + 1] - Experience.LEVEL[level]) * percentLost / 100.0);
		lostexp = (long) calcStat(Stats.EXP_LOST, lostexp, killer, null);
		lostexp *= getPremiumAccount().getRates().getExpLoss();

		if(lostexp <= 0)
			return;

		final long before = getExp();
		addExpAndSp(-lostexp, 0L);
		final long lost = before - getExp();
		if(lost > 0L)
			setVar("lostexp", lost);
	}

	public void setRequest(final Request transaction)
	{
		_request = transaction;
	}

	public Request getRequest()
	{
		return _request;
	}

	public boolean isBusy()
	{
		return isProcessingRequest() || isOutOfControl() || isInOlympiadMode() || getTeam() != TeamType.NONE || isInStoreMode() || isPrivateBuffer() || isInDuel() || getMessageRefusal() || isBlockAll() || isInvisible(null);
	}

	public boolean isProcessingRequest()
	{
		return _request != null && _request.isInProgress();
	}

	public boolean isInTrade()
	{
		return isProcessingRequest() && getRequest().isTypeOf(Request.L2RequestType.TRADE);
	}

	public List<L2GameServerPacket> addVisibleObject(final GameObject object, final Creature dropper)
	{
		if(isLogoutStarted() || object == null || object.getObjectId() == getObjectId() || !object.isVisible() || object.isObservePoint())
			return Collections.emptyList();
		return object.addPacketList(this, dropper);
	}

	@Override
	public List<L2GameServerPacket> addPacketList(final Player forPlayer, final Creature dropper)
	{
		if(isInvisible(forPlayer) && forPlayer.getObjectId() != getObjectId())
			return Collections.emptyList();
		if(isInStoreMode() && forPlayer.getVarBoolean("notraders"))
			return Collections.emptyList();
		if (isPrivateBuffer() && forPlayer.isNotShowPrivateBuffers())
			return Collections.emptyList();
		final List<L2GameServerPacket> list = new ArrayList<>();
		if(forPlayer.getObjectId() != getObjectId())
			list.add(isPolymorphed() ? new NpcInfoPoly(this) : new CIPacket(this, forPlayer));
		if(isSitting() && _sittingObject != null)
			list.add(new ChairSitPacket(this, _sittingObject));
		if(isInStoreMode())
			list.add(getPrivateStoreMsgPacket(forPlayer));
		if(isCastingNow())
		{
			final Creature castingTarget = getCastingTarget();
			final Skill castingSkill = getCastingSkill();
			final long animationEndTime = getAnimationEndTime();
			if(castingSkill != null && !castingSkill.isNotBroadcastable() && castingTarget != null && castingTarget.isCreature() && animationEndTime > 0L)
				list.add(new MagicSkillUse(this, castingTarget, castingSkill.getId(), castingSkill.getLevel(), (int) (animationEndTime - System.currentTimeMillis()), 0L));
		}
		if(isInCombat())
			list.add(new AutoAttackStartPacket(getObjectId()));
		list.add(new RelationChangedPacket(this, forPlayer));
		if(isInBoat())
			list.add(getBoat().getOnPacket(this, getInBoatPosition()));
		else if(isMoving() || isFollowing())
			list.add(movePacket());
		if(isInStoreMode() && entering)
			list.add(new CIPacket(this, forPlayer));
		return list;
	}

	public List<L2GameServerPacket> removeVisibleObject(final GameObject object, final List<L2GameServerPacket> list)
	{
		if(isLogoutStarted() || object == null || object.getObjectId() == getObjectId() || object.isObservePoint())
			return Collections.emptyList();
		final List<L2GameServerPacket> result = list == null ? object.deletePacketList(this) : list;
		if(getParty() != null && object instanceof Creature)
			getParty().removeTacticalSign((Creature) object);
		if(!isInObserverMode())
			getAI().notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, object);
		return result;
	}

	private void levelSet(final int levels)
	{
		if(levels > 0)
		{
			final int level = getLevel();
			checkLevelUpReward(false);
			sendPacket(SystemMsg.YOUR_LEVEL_HAS_INCREASED);
			broadcastPacket(new SocialActionPacket(getObjectId(), 2122));
			setCurrentHpMp(getMaxHp(), getMaxMp());
			setCurrentCp(getMaxCp());
			for(final QuestState qs : getAllQuestsStates())
				qs.getQuest().notifyTutorialEvent("CE", "300", qs);
			rewardSkills(false);
			notifyNewSkills();
		}
		else if(levels < 0)
			checkSkills();
		calcLevelReward();
		sendUserInfo(true);
		sendSkillList();
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
		for(final SkillLearn s : skills)
		{
			if(s.isFreeAutoGet())
				continue;
			final Skill sk = SkillHolder.getInstance().getSkill(s.getId(), s.getLevel());
			if(sk == null)
				continue;
			sendPacket(ExNewSkillToLearnByLevelUp.STATIC);
			return true;
		}
		return false;
	}

	public boolean checkSkills()
	{
        return getAllSkillsStream().anyMatch(sk -> SkillUtils.checkSkill(this, sk));
	}

	public void startTimers()
	{
		startClanRewardLoginTask();
		startAutoSaveTask();
		startPcBangPointsTask();
		startPremiumAccountTask();
		getInventory().startTimers();
		resumeQuestTimers();
		getAttendanceRewards().startTasks();
		startCustomHeroTask();
		startBroadcastAvailableActivities(0L);
	}

	public void stopAllTimers()
	{
		setAgathion(0);
		stopBroadcastAvailableActivities();
		stopWaterTask();
		stopPremiumAccountTask();
		stopHourlyTask();
		stopKickTask();
		stopPcBangPointsTask();
		stopAutoSaveTask();
		getInventory().stopAllTimers();
		stopQuestTimers();
		stopEnableUserRelationTask();
		getHennaList().stopHennaRemoveTask();
		getAttendanceRewards().stopTasks();
		stopCustomHeroTask();
		stopClanRewardLoginTask();
		tasks.values().forEach(f-> f.cancel(false));
		tasks.clear();
	}

	@Override
	public boolean isMyServitor(final int objId)
	{
		return _pet != null && _pet.getObjectId() == objId || _summons.containsKey(objId);
	}

	public int getServitorsCount()
	{
		int count = _summons.size();
		if(_pet != null)
			++count;
		return count;
	}

	public boolean hasServitor()
	{
		return getServitorsCount() > 0;
	}

	@Override
	public List<Servitor> getServitors()
	{
		final List<Servitor> servitors = new ArrayList<>(_summons.values());
		if(_pet != null)
			servitors.add(_pet);
		servitors.sort(Servitor.ServitorComparator.getInstance());
		return servitors;
	}

	public Servitor getAnyServitor()
	{
		return getServitors().stream().findAny().orElse(null);
	}

	public Servitor getFirstServitor()
	{
		return getServitors().stream().findFirst().orElse(null);
	}

	public Servitor getServitor(final int objId)
	{
		if(_pet != null && _pet.getObjectId() == objId)
			return _pet;
		return getSummon(objId);
	}

	public int getSummonsCount()
	{
		return _summons.size();
	}

	public boolean hasSummon()
	{
		return getSummonsCount() > 0;
	}

	public List<SummonInstance> getSummons()
	{
		final List<SummonInstance> summons = new ArrayList<>(_summons.values());
		summons.sort(Servitor.ServitorComparator.getInstance());
		return summons;
	}

	public SummonInstance getAnySummon()
	{
		return getSummons().stream().findAny().orElse(null);
	}

	public SummonInstance getFirstSummon()
	{
		return getSummons().stream().findFirst().orElse(null);
	}

	public SummonInstance getSummon(final int objId)
	{
		return _summons.get(objId);
	}

	public void addSummon(final SummonInstance summon)
	{
		_summons.put(summon.getObjectId(), summon);
		autoShot();
	}

	public void deleteServitor(final int objId)
	{
		if(_summons.containsKey(objId))
			deleteSummon(objId);
		else if(_pet != null && _pet.getObjectId() == objId)
			setPet(null);
	}

	public void deleteSummon(final int objId)
	{
		_summons.remove(objId);
		autoShot();
		getAbnormalList().stopEffects(4140);
	}

	public PetInstance getPet()
	{
		return _pet;
	}

	public void setPet(final PetInstance pet)
	{
		final boolean petDeleted = _pet != null;
		_pet = pet;
		unsetVar("pet");
		if(pet == null)
		{
			if(petDeleted)
			{
				if(isLogoutStarted() && getPetControlItem() != null)
					setVar("pet", getPetControlItem().getObjectId());
				setPetControlItem(null);
			}
			getAbnormalList().stopEffects(4140);
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

	public void scheduleDelete(final long time)
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
		super.onDelete();
		if(_observePoint != null)
			_observePoint.deleteMe();
		_friendList.notifyFriends(false);
		getBookMarkList().clear();
		_inventory.clear();
		_warehouse.clear();
		_summons.clear();
		_pet = null;
		_arrowItem = null;
		_fistsWeaponItem = null;
		_chars = null;
		_enchantScroll = null;
		_lastNpc = HardReferences.emptyRef();
		_observePoint = null;
	}

	public void setTradeList(final List<TradeItem> list)
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
		name = Strings.stripToSingleLine(name);
		setSellStoreName(name, SpamFilterManager.getInstance().isSpam(this, name, SpamType.STORE_SELL));
	}

	private void setSellStoreName(String name, boolean isSpam)
	{
		_sellStoreName = name;
		_sellStoreNameSpam = isSpam;
	}

	public boolean isSellStoreNameSpam()
	{
		return _sellStoreNameSpam;
	}

	public void setSellList(boolean packageSell, List<TradeItem> list)
	{
		if(packageSell)
			_packageSellList = list;
		else
			_sellList = list;
	}

	public List<TradeItem> getSellList()
	{
		return getSellList(_privatestore == 8);
	}

	public List<TradeItem> getSellList(boolean packageSell)
	{
		return packageSell ? _packageSellList : _sellList;
	}

	public String getBuyStoreName()
	{
		return _buyStoreName;
	}

	public boolean isBuyStoreNameSpam()
	{
		return _buyStoreNameSpam;
	}

	public void setBuyStoreName(String name)
	{
		name = Strings.stripToSingleLine(name);
		setBuyStoreName(name, SpamFilterManager.getInstance().isSpam(this, name, SpamType.STORE_BUY));
	}

	private void setBuyStoreName(String name, boolean isSpam)
	{
		_buyStoreName = name;
		_buyStoreNameSpam = isSpam;
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
		name = Strings.stripToSingleLine(name);
		setManufactureName(name, SpamFilterManager.getInstance().isSpam(this, name, SpamType.STORE_MANUFACTURE));
	}

	public boolean isManufactureNameSpam()
	{
		return _manufactureNameSpam;
	}

	private void setManufactureName(String name, boolean isSpam)
	{
		_manufactureName = name;
		_manufactureNameSpam = isSpam;
	}

	public String getManufactureName()
	{
		return _manufactureName;
	}

	public List<ManufactureItem> getCreateList()
	{
		return _createList;
	}

	public void setCreateList(List<ManufactureItem> list)
	{
		_createList = list;
	}

	public void setPrivateStoreType(int type)
	{
		_privatestore = type;
		if(type != 0)
			setVar("storemode", type);
		else
			unsetVar("storemode");
	}

	public boolean isInStoreMode()
	{
		return _privatestore != 0;
	}

	public int getPrivateStoreType()
	{
		return _privatestore;
	}

	public L2GameServerPacket getPrivateStoreMsgPacket(final Player forPlayer)
	{

		boolean isSpam = false;

		switch(getPrivateStoreType())
		{
			case STORE_PRIVATE_BUY:
			{
				isSpam = isBuyStoreNameSpam();
				return new PrivateStoreBuyMsg(this, canTalkWith(forPlayer) && (!isSpam || Config.SPAM_FILTER_DUMMY_SPAM && HwidUtils.INSTANCE.isSameHWID(this, forPlayer)));
			}
			case STORE_PRIVATE_SELL:
			{
				isSpam = isSellStoreNameSpam();
				return new PrivateStoreMsg(this, canTalkWith(forPlayer) && (!isSpam || Config.SPAM_FILTER_DUMMY_SPAM && HwidUtils.INSTANCE.isSameHWID(this, forPlayer)));
			}
			case STORE_PRIVATE_SELL_PACKAGE:
			{
				isSpam = isSellStoreNameSpam();
				return new ExPrivateStoreWholeMsg(this, canTalkWith(forPlayer) && (!isSpam || Config.SPAM_FILTER_DUMMY_SPAM && HwidUtils.INSTANCE.isSameHWID(this, forPlayer)));
			}
			case STORE_PRIVATE_MANUFACTURE:
			{
				isSpam = isManufactureNameSpam();
				return new RecipeShopMsgPacket(this, canTalkWith(forPlayer) && (!isSpam || Config.SPAM_FILTER_DUMMY_SPAM && HwidUtils.INSTANCE.isSameHWID(this, forPlayer)));
			}
			default:
			{
				return null;
			}
		}
	}

	public void broadcastPrivateStoreInfo()
	{
		if(!isVisible() || _privatestore == 0)
			return;
		sendPacket(getPrivateStoreMsgPacket(this));
		for(final Player target : World.getAroundObservers(this))
			target.sendPacket(getPrivateStoreMsgPacket(target));
	}

	public void setClan(final Clan clan)
	{
		if(_clan != clan && _clan != null)
			unsetVar("canWhWithdraw");
		final Clan oldClan = _clan;
		if(oldClan != null && clan == null)
			for(final SkillEntry skillEntry : oldClan.getAllSkills())
				removeSkill(skillEntry, false);
		if((_clan = clan) == null)
		{
			_pledgeType = -128;
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

	public boolean isInClan()
	{
		return _clan != null;
	}

	public SubUnit getSubUnit()
	{
		return _clan == null ? null : _clan.getSubUnit(_pledgeType);
	}

	public ClanHall getClanHall()
	{
		final int id = _clan != null ? _clan.getHasHideout() : 0;
		return ResidenceHolder.getInstance().getResidence(ClanHall.class, id);
	}

	public Castle getCastle()
	{
		final int id = _clan != null ? _clan.getCastle() : 0;
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
		if(!getInventory().destroyItemByObjectId(getInventory().getPaperdollObjectId(8), 1L))
		{
			getInventory().setPaperdollItem(8, null);
			_arrowItem = null;
		}
	}

	public boolean checkAndEquipArrows()
	{
		if(getInventory().getPaperdollItem(8) == null)
		{
			final ItemInstance activeWeapon = getActiveWeaponInstance();
			if(activeWeapon != null && activeWeapon.getItemType() == WeaponTemplate.WeaponType.BOW)
				_arrowItem = getInventory().findArrowForBow(activeWeapon.getTemplate());
			if(_arrowItem != null)
				getInventory().setPaperdollItem(8, _arrowItem);
		}
		else
			_arrowItem = getInventory().getPaperdollItem(8);
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

	public void joinParty(final Party party)
	{
		if(party != null)
			party.addPartyMember(this);
	}

	public void leaveParty()
	{
		if(isInParty())
			_party.removePartyMember(this, false);
	}

	public Party getParty()
	{
		return _party;
	}

	public void setLastPartyPosition(final Location loc)
	{
		_lastPartyPosition = loc;
	}

	public Location getLastPartyPosition()
	{
		return _lastPartyPosition;
	}

	public boolean isGM()
	{
		return _playerAccess != null && _playerAccess.IsGM;
	}

	public void setAccessLevel(final int level)
	{
		_accessLevel = level;
	}

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

	@Override
	public void updateStats()
	{
		if(entering || isLogoutStarted())
			return;
		refreshOverloaded();
		refreshExpertisePenalty();
		super.updateStats();
	}

	@Override
	public void sendChanges()
	{
		if(entering || isLogoutStarted())
			return;
		super.sendChanges();
	}

	public void updateKarma(final boolean flagChanged)
	{
		sendStatusUpdate(true, true, 27);
		if(flagChanged)
			broadcastRelation();
	}

	public boolean isOnline()
	{
		return _isOnline;
	}

	public void setIsOnline(final boolean isOnline)
	{
		_isOnline = isOnline;
	}

	public void setOnlineStatus(final boolean isOnline)
	{
		_isOnline = isOnline;
		updateOnlineStatus();
	}

	private void updateOnlineStatus()
	{

		String hwid = getHwidHolder() == null ? null : getHwidHolder().asString();
		String online = "UPDATE characters SET online=?, lastAccess=?, last_hwid=? WHERE obj_id=?";
		String offline = "UPDATE characters SET online=?, lastAccess=? WHERE obj_id=?";
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(hwid == null ? offline : online);
			statement.setInt(1, isOnline() && !isInOfflineMode() ? 1 : 0);
			statement.setLong(2, System.currentTimeMillis() / 1000L);
			if (hwid == null) {
				statement.setInt(3, getObjectId());
			} else {
				statement.setString(3, hwid);
				statement.setInt(4, getObjectId());
			}
			statement.execute();
		}
		catch(Exception e)
		{
			Player._log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void decreaseKarma(final long val)
	{
		final boolean flagChanged = _karma >= 0;
		long new_karma = _karma - val;
		if(new_karma < -2147483648L)
			new_karma = -2147483648L;
		if(_karma >= 0 && new_karma < 0L && _pvpFlag > 0)
		{
			_pvpFlag = 0;
			if(_PvPRegTask != null)
			{
				_PvPRegTask.cancel(true);
				_PvPRegTask = null;
			}
			sendStatusUpdate(true, true, 26);
		}
		setKarma((int) new_karma);
		updateKarma(flagChanged);
	}

	public void increaseKarma(final int val)
	{
		final boolean flagChanged = _karma < 0;
		long new_karma = _karma + val;
		if(new_karma > 2147483647L)
			new_karma = 2147483647L;
		setKarma((int) new_karma);
		if(_karma > 0)
			updateKarma(flagChanged);
		else
			updateKarma(false);
	}

	public static Player create(HwidHolder hwidHolder, final int classId, final int sex, final String accountName,
								final String name, final int hairStyle, final int hairColor, final int face)
	{
		final ClassId class_id = ClassId.VALUES[classId];
		final PlayerTemplate template = PlayerTemplateHolder.getInstance().getPlayerTemplate(class_id.getRace(), class_id, Sex.VALUES[sex]);
		final Player player = new Player(IdFactory.getInstance().getNextId(), template, accountName, hwidHolder);
		player.setName(name);
		player.setTitle("");
		player.setHairStyle(hairStyle);
		player.setHairColor(hairColor);
		player.setFace(face);
		player.setCreateTime(System.currentTimeMillis());
		if(!CharacterDAO.getInstance().insert(player))
			return null;
		final int level = Config.STARTING_LVL;
		final double hp = class_id.getBaseHp(level);
		final double mp = class_id.getBaseMp(level);
		final double cp = class_id.getBaseCp(level);
		final long exp = Experience.getExpForLevel(level);
		final long sp = Config.STARTING_SP;
		final boolean active = true;
		final SubClassType type = SubClassType.BASE_CLASS;
		if(!CharacterSubclassDAO.getInstance().insert(player.getObjectId(), classId, exp, sp, hp, mp, cp, hp, mp, cp, level, active, type))
			return null;
		return player;
	}

	public static Player restore(final int objectId, HwidHolder hwidHolder)
	{
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
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			statement2 = con.createStatement();
			rset = statement.executeQuery("SELECT * FROM `characters` WHERE `obj_Id`=" + objectId + " LIMIT 1");
			rset2 = statement2.executeQuery("SELECT `class_id` FROM `character_subclasses` WHERE `char_obj_id`=" + objectId + " AND `type`=" + SubClassType.BASE_CLASS.ordinal() + " LIMIT 1");
			if(rset.next() && rset2.next())
			{
				final ClassId classId = ClassId.VALUES[rset2.getInt("class_id")];
				final PlayerTemplate template = PlayerTemplateHolder.getInstance().getPlayerTemplate(classId.getRace(), classId, Sex.VALUES[rset.getInt("sex")]);
				player = new Player(objectId, template, hwidHolder);
				player.getSubClassList().restore();
				player.restoreVariables();

				player.setFraction(Fraction.VALUES_WITH_NONE[player.getVarInt("fraction", 0)]);

				player.setVar("last_hwid", hwidHolder.asString());

				player.loadInstanceReuses();
				player.getBookMarkList().setCapacity(rset.getInt("bookmarks"));
				player.getBookMarkList().restore();
				player.setBotRating(rset.getInt("bot_rating"));
				player.getFriendList().restore();
				player.getBlockList().restore();
				player.getProductHistoryList().restore();
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
				if(player.getLeaveClanTime() > 0L && player.canJoinClan())
					player.setLeaveClanTime(0L);
				player.setDeleteClanTime(rset.getLong("deleteclan") * 1000L);
				if(player.getDeleteClanTime() > 0L && player.canCreateClan())
					player.setDeleteClanTime(0L);

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

				if(Config.AUTO_LOOT_INDIVIDUAL)
				{
					player._autoLoot = player.getVarBoolean("AutoLoot", Config.AUTO_LOOT);
					player._autoLootOnlyAdena = player.getVarBoolean("AutoLootOnlyAdena", Config.AUTO_LOOT);
					player.AutoLootHerbs = player.getVarBoolean("AutoLootHerbs", Config.AUTO_LOOT_HERBS);
				}

				player.setSpamer(player.getVarBoolean("is_spamer", false), false);

				player.setUptime(System.currentTimeMillis());
				player.setLastAccess(rset.getLong("lastAccess"));
				player.setRecomHave(rset.getInt("rec_have"));
				player.setRecomLeft(rset.getInt("rec_left"));
				if(!Config.USE_CLIENT_LANG)
					player.setLanguage(player.getVar("lang@"));
				player.setKeyBindings(rset.getBytes("key_bindings"));
				if(Config.PC_BANG_POINTS_BY_ACCOUNT)
					player.setPcBangPoints(Integer.parseInt(AccountVariablesDAO.getInstance().select(player.getAccountName(), PC_BANG_POINTS_VAR, "0")));
				else
					player.setPcBangPoints(rset.getInt("pcBangPoints"));
				player.setFame(rset.getInt("fame"), null, false);
				player.setUsedWorldChatPoints(rset.getInt("used_world_chat_points"));
				player.setHideHeadAccessories(rset.getInt("hide_head_accessories") > 0);
				player.restoreRecipeBook();

				if(Config.ENABLE_OLYMPIAD)
					player.setHero(Hero.getInstance().isHero(player.getObjectId()));
				if(!player.isHero() && player.isCustomHero())
					player.startCustomHeroTask();

				player.updatePledgeRank();
				player.setXYZ(rset.getInt("x"), rset.getInt("y"), rset.getInt("z"));
				int reflection = 0;
				final long jailExpireTime = player.getVarExpireTime(JAILED_VAR);
				if(jailExpireTime > System.currentTimeMillis())
				{
					reflection = ReflectionManager.JAIL.getId();
					if(!player.isInZone("[gm_prison]"))
						player.setLoc(Location.findPointToStay(player, AdminFunctions.JAIL_SPAWN, 50, 200));
					player.setIsInJail(true);
					player.startUnjailTask(player, (int) (jailExpireTime - System.currentTimeMillis() / 60000L));
				}
				else
				{
					final String jumpSafeLoc = player.getVar("@safe_jump_loc");
					if(jumpSafeLoc != null)
					{
						player.setLoc(Location.parseLoc(jumpSafeLoc));
						player.unsetVar("@safe_jump_loc");
					}
					final String ref = player.getVar("reflection");
					if(ref != null)
					{
						reflection = Integer.parseInt(ref);
						if(reflection != ReflectionManager.PARNASSUS.getId() && reflection != ReflectionManager.GIRAN_HARBOR.getId())
						{
							final String back = player.getVar("backCoords");
							if(back != null)
							{
								player.setLoc(Location.parseLoc(back));
								player.unsetVar("backCoords");
							}
							reflection = 0;
						}
					}
				}
				player.setReflection(reflection);
				EventHolder.getInstance().findEvent(player);
				Quest.restoreQuestStates(player);
				player.getInventory().restore();
				player.setActiveSubClass(player.getActiveClassId(), false, true);
				player.getAttendanceRewards().restore();
				player.restoreSummons();

				try
				{
					final String var = player.getVar("ExpandInventory");
					if(var != null)
						player.setExpandInventory(Integer.parseInt(var));
				}
				catch(Exception e)
				{
					Player._log.error("", e);
				}
				try
				{
					final String var = player.getVar("ExpandWarehouse");
					if(var != null)
						player.setExpandWarehouse(Integer.parseInt(var));
				}
				catch(Exception e)
				{
					Player._log.error("", e);
				}
				try
				{
					final String var = player.getVar("notShowBuffAnim");
					if(var != null)
						player.setNotShowBuffAnim(Boolean.parseBoolean(var));
				}
				catch(Exception e)
				{
					Player._log.error("", e);
				}
				try
				{
					final String var = player.getVar("notraders");
					if(var != null)
						player.setNotShowTraders(Boolean.parseBoolean(var));
					player.setNotShowPrivateBuffers(player.getVarBoolean(NO_PRIVATEBUFFERS_VAR, false));
				}
				catch(Exception e)
				{
					Player._log.error("", e);
				}
				try
				{
					final String var = player.getVar("pet");
					if(var != null)
						player.setPetControlItem(Integer.parseInt(var));
				}
				catch(Exception e)
				{
					Player._log.error("", e);
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
				List<Zone> zones = new ArrayList<>();
				World.getZones(zones, player.getLoc(), player.getReflection());
				if(!zones.isEmpty())
					for(final Zone zone : zones)
						if(zone.getType() == Zone.ZoneType.SIEGE)
						{
							SiegeEvent<?, ?> siegeEvent = player.getEvent(SiegeEvent.class);
							if(siegeEvent != null)
								player.setLoc(siegeEvent.getEnterLoc(player, zone));
							else
							{
								int id = zone.getParams().getInteger("residence");
								Residence r = ResidenceHolder.getInstance().getResidence(id);
								if (r == null)
									throw new IllegalArgumentException("Can't find residence with id " + id);
								player.setLoc(r.getNotOwnerRestartPoint(player));
							}
						}
						else if(zone.getType() == Zone.ZoneType.no_restart)
						{
							if(System.currentTimeMillis() / 1000L - player.getLastAccess() <= zone.getRestartTime())
								continue;
							player.sendMessage(new CustomMessage("l2s.gameserver.network.l2.c2s.EnterWorld.TeleportedReasonNoRestart"));

							player.setLoc(TeleportUtils.getRestartPoint(player, RestartType.TO_VILLAGE).getLoc());
						}

				player.getMacroses().restore();
				player.refreshExpertisePenalty();
				player.refreshOverloaded();
				player.getWarehouse().restore();
				player.getFreight().restore();
				player.restoreTradeList();
				if(player.getVar("storemode") != null)
				{
					player.setPrivateStoreType(Integer.parseInt(player.getVar("storemode")));
					player.setSitting(true);
				}
				if (player.getVarBoolean(PrivateBuffer.VAR_PBUFFER, false)) {
					player.getPrivateBuffer().restore();
				}
				player.updateKetraVarka();
				player.updateRam();
				player.checkDailyCounters();
				player.checkWeeklyCounters();
				player.restoreBuffProfiles();
				player.getConfrontationComponent().restore();
				player.getMercenaryComponent().restore();
				player.calcLevelReward();
			}
		}
		catch(Exception e2)
		{
			Player._log.error("Could not restore char data!", e2);
		}
		finally
		{
			DbUtils.closeQuietly(statement2, rset2);
			DbUtils.closeQuietly(statement3, rset3);
			DbUtils.closeQuietly(con, statement, rset);
		}
		player.tScheme_record = new RouteRecord(player);
		return player;
	}

	public void store(final boolean fast)
	{
		if (isPhantom() && getPhantomType() !=PhantomType.PHANTOM_CLAN_MEMBER)
			return;
		if(!_storeLock.tryLock())
			return;
		try
		{
			Connection con = null;
			PreparedStatement statement = null;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("UPDATE characters SET face=?,beautyFace=?,hairStyle=?,beautyHairStyle=?,hairColor=?,beautyHairColor=?,sex=?,x=?,y=?,z=?,karma=?,pvpkills=?,pkkills=?,rec_have=?,rec_left=?,clanid=?,deletetime=?,title=?,accesslevel=?,online=?,leaveclan=?,deleteclan=?,nochannel=?,onlinetime=?,pledge_type=?,pledge_rank=?,lvl_joined_academy=?,apprentice=?,key_bindings=?,pcBangPoints=?,char_name=?,fame=?,bookmarks=?,bot_rating=?,used_world_chat_points=?,hide_head_accessories=? WHERE obj_Id=? LIMIT 1");
				statement.setInt(1, getFace());
				statement.setInt(2, getBeautyFace());
				statement.setInt(3, getHairStyle());
				statement.setInt(4, getBeautyHairStyle());
				statement.setInt(5, getHairColor());
				statement.setInt(6, getBeautyHairColor());
				statement.setInt(7, getSex().ordinal());
				if(_stablePoint == null)
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
				statement.setLong(23, 0);
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
					storeBuffProfiles();
				}
				storeCharSubClasses();
				getBookMarkList().store();
				getDailyMissionList().store();
				if(Config.PC_BANG_POINTS_BY_ACCOUNT)
					AccountVariablesDAO.getInstance().insert(getAccountName(), PC_BANG_POINTS_VAR, String.valueOf(getPcBangPoints()));
			}
			catch(Exception e)
			{
				Player._log.error("Could not store char data: " + this + "!", e);
			}
			finally
			{
				DbUtils.closeQuietly(con, statement);
			}
			getConfrontationComponent().store();
		}
		finally
		{
			_storeLock.unlock();
		}
	}

	public SkillEntry addSkill(final SkillEntry newSkillEntry, final boolean store)
	{
		if(newSkillEntry == null)
			return null;
		final SkillEntry oldSkillEntry = addSkill(newSkillEntry);
		if(newSkillEntry.equals(oldSkillEntry))
			return oldSkillEntry;
		if(store)
			storeSkill(newSkillEntry);
		return oldSkillEntry;
	}

	public SkillEntry removeSkill(final SkillEntry skillEntry, final boolean fromDB)
	{
		if(skillEntry == null)
			return null;
		return removeSkill(skillEntry.getId(), fromDB);
	}

	public SkillEntry removeSkill(final int id, final boolean fromDB)
	{
		final SkillEntry oldSkillEntry = removeSkillById(id);
		if(!fromDB)
			return oldSkillEntry;
		if(oldSkillEntry != null)
		{
			Connection con = null;
			PreparedStatement statement = null;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("DELETE FROM character_skills WHERE skill_id=? AND char_obj_id=? AND (class_index=? OR class_index=-1 OR class_index=-2)");
				statement.setInt(1, oldSkillEntry.getId());
				statement.setInt(2, getObjectId());
				statement.setInt(3, getActiveClassId());
				statement.execute();
			}
			catch(Exception e)
			{
				Player._log.error("Could not delete skill!", e);
			}
			finally
			{
				DbUtils.closeQuietly(con, statement);
			}
		}
		return oldSkillEntry;
	}

	private void storeSkill(final SkillEntry newSkillEntry)
	{
		if(newSkillEntry == null)
		{
			Player._log.warn("could not store new skill. its NULL");
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
			statement.setInt(4, getActiveClassId());
			statement.execute();
		}
		catch(Exception e)
		{
			Player._log.error("Error could not store skills!", e);
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
				final SkillEntry skillEntry = SkillHolder.getInstance().getSkillEntry(rset.getInt("skill_id"), rset.getInt("skill_level"));
				if(skillEntry == null)
					continue;
				if(!isGM())
				{
					final Skill skill = skillEntry.getTemplate();
					if(!SkillAcquireHolder.getInstance().isSkillPossible(this, skill))
					{
						removeSkill(skillEntry, true);
						continue;
					}
				}
				addSkill(skillEntry);
			}

			checkHeroSkills();

			if(_clan != null)
				_clan.addSkillsQuietly(this);
			if(Config.UNSTUCK_SKILL && getSkillLevel(1050) < 0)
				addSkill(SkillHolder.getInstance().getSkillEntry(2099, 1));
			if(isGM())
				giveGMSkills();
		}
		catch(Exception e)
		{
			Player._log.warn("Could not restore skills for player objId: " + getObjectId());
			Player._log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public void storeDisableSkills()
	{
		if (isPhantom())
			return;
		Connection con = null;
		Statement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			statement.executeUpdate("DELETE FROM character_skills_save WHERE char_obj_id = " + getObjectId() + " AND (class_index=" + getActiveClassId() + " OR class_index=-1) AND `end_time` < " + System.currentTimeMillis());
			if(_skillReuses.isEmpty())
				return;
			final SqlBatch b = new SqlBatch("REPLACE INTO `character_skills_save` (`char_obj_id`,`skill_id`,`skill_level`,`class_index`,`end_time`,`reuse_delay_org`) VALUES");
			synchronized (_skillReuses)
			{
				for(final TimeStamp timeStamp : _skillReuses.values())
					if(timeStamp.hasNotPassed())
					{
						final StringBuilder sb = new StringBuilder("(");
						sb.append(getObjectId()).append(",");
						sb.append(timeStamp.getId()).append(",");
						sb.append(timeStamp.getLevel()).append(",");
						sb.append(timeStamp.isGlobalReuse() ? -1 : getActiveClassId()).append(",");
						sb.append(timeStamp.getEndTime()).append(",");
						sb.append(timeStamp.getReuseBasic()).append(")");
						b.write(sb.toString());
					}
			}
			if(!b.isEmpty())
				statement.executeUpdate(b.close());
		}
		catch(Exception e)
		{
			Player._log.warn("Could not store disable skills data: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void restoreDisableSkills()
	{
		_skillReuses.clear();
		if (isPhantom())
			return;
		Connection con = null;
		Statement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			rset = statement.executeQuery("SELECT skill_id,skill_level,end_time,reuse_delay_org FROM character_skills_save WHERE char_obj_id=" + getObjectId() + " AND (class_index=" + getActiveClassId() +" OR class_index=-1)");
			while(rset.next())
			{
				final int skillId = rset.getInt("skill_id");
				final int skillLevel = rset.getInt("skill_level");
				final long endTime = rset.getLong("end_time");
				final long rDelayOrg = rset.getLong("reuse_delay_org");
				final long curTime = System.currentTimeMillis();
				final Skill skill = SkillHolder.getInstance().getSkill(skillId, skillLevel);
				if(skill != null && endTime - curTime > 500L)
					_skillReuses.put(skill.getReuseHash(), new TimeStamp(skill, endTime, rDelayOrg));
			}
			DbUtils.close(statement);
			statement = con.createStatement();
			statement.executeUpdate("DELETE FROM character_skills_save WHERE char_obj_id = " + getObjectId() + " AND (class_index=" + getActiveClassId() + " OR class_index=-1) AND `end_time` < " + System.currentTimeMillis());
		}
		catch(Exception e)
		{
			Player._log.error("Could not restore active skills data!", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	@Override
	public boolean consumeItem(final int itemConsumeId, final long itemCount, final boolean sendMessage)
	{
		return ItemFunctions.deleteItem(this, itemConsumeId, itemCount, sendMessage);
	}

	@Override
	public boolean consumeItemMp(final int itemId, final int mp)
	{
		final ItemInstance[] paperdollItems = getInventory().getPaperdollItems();
		final int length = paperdollItems.length;
		int i = 0;
		while(i < length)
		{
			final ItemInstance item = paperdollItems[i];
			if(item != null && item.getItemId() == itemId)
			{
				final int newMp = item.getLifeTime() - mp;
				if(newMp >= 0)
				{
					item.setLifeTime(newMp);
					sendPacket(new InventoryUpdatePacket().addModifiedItem(this, item));
					return true;
				}
				break;
			}
			else
				++i;
		}
		return false;
	}

	@Override
	public boolean isMageClass()
	{
		return getClassId().isMage();
	}

	public boolean checkLandingState()
	{
		if(isInZone(Zone.ZoneType.no_landing))
			return false;
		final SiegeEvent<?, ?> siege = getEvent(SiegeEvent.class);
		if(siege != null)
		{
			final Residence unit = siege.getResidence();
			return unit != null && getClan() != null && isClanLeader() && getClan().getCastle() == unit.getId();
		}
		return true;
	}

	public void setMount(final int controlItemObjId, final int npcId, final int level, final int currentFeed)
	{
		final Mount mount = Mount.create(this, controlItemObjId, npcId, level, currentFeed);
		if(mount != null)
			setMount(mount);
	}

	public void setMount(final Mount mount)
	{
		if(_mount == mount)
			return;
		final Mount oldMount = _mount;
		_mount = null;
		if(oldMount != null)
			oldMount.onUnride();
		if(mount != null)
			(_mount = mount).onRide();
	}

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

	public void sendDisarmMessage(final ItemInstance wpn)
	{
		if(wpn.getEnchantLevel() > 0)
		{
			final SystemMessage sm = new SystemMessage(1064);
			sm.addNumber(wpn.getEnchantLevel());
			sm.addItemName(wpn.getItemId());
			sendPacket(sm);
		}
		else
		{
			final SystemMessage sm = new SystemMessage(417);
			sm.addItemName(wpn.getItemId());
			sendPacket(sm);
		}
	}

	public void setUsingWarehouseType(final Warehouse.WarehouseType type)
	{
		_usingWHType = type;
	}

	public Warehouse.WarehouseType getUsingWarehouseType()
	{
		return _usingWHType;
	}

	public Collection<EffectCubic> getCubics()
	{
		return _cubics == null ? Collections.emptyList() : _cubics.values();
	}

	public void addCubic(final EffectCubic cubic)
	{
		if(_cubics == null)
			_cubics = new ConcurrentHashMap<>(3);
		_cubics.put(cubic.getId(), cubic);
		sendPacket(new ExUserInfoCubic(this));
	}

	public void removeCubic(final int id)
	{
		if(_cubics != null)
			_cubics.remove(id);
		sendPacket(new ExUserInfoCubic(this));
	}

	public EffectCubic getCubic(final int id)
	{
		return _cubics == null ? null : _cubics.get(id);
	}

	@Override
	public String toString()
	{
		return getName() + "[id=" + getObjectId() + ", hwid=" + getHwidHolder() + "]";
	}

	@Override
	public int getEnchantEffect()
	{
		final ItemInstance wpn = getActiveWeaponInstance();
		if(wpn == null)
			return 0;
		return Math.min(127, wpn.getFixedEnchantLevel(this));
	}

	public void setLastNpc(final NpcInstance npc)
	{
		if(npc == null)
			_lastNpc = HardReferences.emptyRef();
		else
			_lastNpc = npc.getRef();
	}

	public NpcInstance getLastNpc()
	{
		return _lastNpc.get();
	}

	public void setMultisell(final MultiSellListContainer multisell)
	{
		_multisell = multisell;
	}

	public MultiSellListContainer getMultisell()
	{
		return _multisell;
	}

	@Override
	public boolean unChargeShots(final boolean spirit)
	{
		final ItemInstance weapon = getActiveWeaponInstance();
		if(weapon == null)
			return false;
		if(spirit)
			weapon.setChargedSpiritshotPower(0.0);
		else
			weapon.setChargedSoulshotPower(0.0);
		autoShot();
		return true;
	}

	@Override
	public double getChargedSoulshotPower()
	{
		final ItemInstance weapon = getActiveWeaponInstance();
		if(weapon != null && weapon.getChargedSoulshotPower() > 0.0)
			return calcStat(Stats.SOULSHOT_POWER, weapon.getChargedSoulshotPower());
		return 0.0;
	}

	@Override
	public void setChargedSoulshotPower(final double val)
	{
		final ItemInstance weapon = getActiveWeaponInstance();
		if(weapon != null)
			weapon.setChargedSoulshotPower(val);
	}

	@Override
	public double getChargedSpiritshotPower()
	{
		final ItemInstance weapon = getActiveWeaponInstance();
		if(weapon != null && weapon.getChargedSpiritshotPower() > 0.0)
			return calcStat(Stats.SPIRITSHOT_POWER, weapon.getChargedSpiritshotPower());
		return 0.0;
	}

	@Override
	public void setChargedSpiritshotPower(final double val)
	{
		final ItemInstance weapon = getActiveWeaponInstance();
		if(weapon != null)
			weapon.setChargedSpiritshotPower(val);
	}

	@Override
	public boolean isInvisible(final GameObject observer)
	{
		if(observer != null)
		{
			if(isMyServitor(observer.getObjectId()))
				return false;
			if(observer.isPlayer())
			{
				final Player observPlayer = (Player) observer;
				if(isInSameParty(observPlayer))
					return false;
			}
		}
		return super.isInvisible(observer) || isGMInvisible();
	}

	public boolean isGMInvisible()
	{
		return getPlayerAccess().GodMode && _gmInvisible.get();
	}

	public boolean setGMInvisible(final boolean value)
	{
		if(value)
			return _gmInvisible.getAndSet(true);
		return _gmInvisible.setAndGet(false);
	}

	public int getClanPrivileges()
	{
		if(_clan == null)
			return 0;
		if(isClanLeader())
			return 16777214;
		if(_powerGrade < 1 || _powerGrade > 9)
			return 0;
		final RankPrivs privs = _clan.getRankPrivs(_powerGrade);
		if(privs != null)
			return privs.getPrivs();
		return 0;
	}

	public void teleToClosestTown()
	{
		final TeleportPoint teleportPoint = TeleportUtils.getRestartPoint(this, RestartType.TO_VILLAGE);
		teleToLocation(teleportPoint.getLoc(), teleportPoint.getReflection());
		if(this.isPhantom())
		{
			this.phantom_params.getPhantomAI().teleToClosestTown();
		}
	}

	public void teleToCastle()
	{
		final TeleportPoint teleportPoint = TeleportUtils.getRestartPoint(this, RestartType.TO_CASTLE);
		teleToLocation(teleportPoint.getLoc(), teleportPoint.getReflection());
	}

	public void teleToClanhall()
	{
		final TeleportPoint teleportPoint = TeleportUtils.getRestartPoint(this, RestartType.TO_CLANHALL);
		teleToLocation(teleportPoint.getLoc(), teleportPoint.getReflection());
	}

	@Override
	public void sendMessage(final CustomMessage message)
	{
		sendPacket(message);
	}

	public void teleToLocation(final Location loc, final boolean replace)
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
		setNonAggroTime(System.currentTimeMillis() + Config.NONAGGRO_TIME_ONTELEPORT);
		setNonPvpTime(System.currentTimeMillis() + Config.NONPVP_TIME_ONTELEPORT);
		spawnMe();
		setLastClientPosition(getLoc());
		setLastServerPosition(getLoc());
		if(isPendingRevive())
			doRevive();
		sendActionFailed();
		getAI().notifyEvent(CtrlEvent.EVT_TELEPORTED);
		if(isLockedTarget() && getTarget() != null)
			sendPacket(new MyTargetSelectedPacket(this, getTarget()));
		sendUserInfo(true);
		getListeners().onTeleported();

		getDamageList().clear();

		return true;
	}

	public boolean enterObserverMode(Location loc)
	{
		WorldRegion observerRegion = World.getRegion(loc);
		if(observerRegion == null)
			return false;

		if(!_observerMode.compareAndSet(0, 1))
			return false;

		setTarget(null);
		stopMove();
		sitDown(null);
		setFlying(true);
		World.removeObjectsFromPlayer(this);

		_observePoint = new ObservePoint(this);
		_observePoint.setLoc(loc);
		_observePoint.startImmobilized();

		broadcastCharInfoImpl(new IUpdateTypeComponent[0]);
		sendPacket(new ObserverStartPacket(loc));
		return true;
	}

	public void appearObserverMode()
	{
		if(!_observerMode.compareAndSet(1, 3))
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
		if(!_observerMode.compareAndSet(3, 2))
			return;

		ObservableArena arena = _observableArena;
		if(arena != null)
		{
			sendPacket(new TeleportToLocationPacket(this, getLoc()));
			_observableArena.removeObserver(_observePoint);
			_observableArena = null;
		}

		_observePoint.deleteMe();
		_observePoint = null;

		setTarget(null);
		stopMove();

		if(arena != null)
		{
			arena.onExitObserverArena(this);
			sendPacket(new ExTeleportToLocationActivate(this, getLoc()));
		}
		else
		{
			sendPacket(new ObserverEndPacket(getLoc()));
		}
	}

	public void returnFromObserverMode()
	{
		if(!_observerMode.compareAndSet(2, 0))
			return;

		setLastClientPosition(null);
		setLastServerPosition(null);
		standUp();
		setFlying(false);
		broadcastUserInfo(true);
		World.showObjectsToPlayer(this);
	}

	public boolean isInObserverMode()
	{
		return getObserverMode() > 0;
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

	public int getSubLevel()
	{
		return isBaseClassActive() ? 0 : getLevel();
	}

	public void updateKetraVarka()
	{
		if(ItemFunctions.getItemCount(this, 7215) > 0L)
			_ketra = 5;
		else if(ItemFunctions.getItemCount(this, 7214) > 0L)
			_ketra = 4;
		else if(ItemFunctions.getItemCount(this, 7213) > 0L)
			_ketra = 3;
		else if(ItemFunctions.getItemCount(this, 7212) > 0L)
			_ketra = 2;
		else if(ItemFunctions.getItemCount(this, 7211) > 0L)
			_ketra = 1;
		else if(ItemFunctions.getItemCount(this, 7225) > 0L)
			_varka = 5;
		else if(ItemFunctions.getItemCount(this, 7224) > 0L)
			_varka = 4;
		else if(ItemFunctions.getItemCount(this, 7223) > 0L)
			_varka = 3;
		else if(ItemFunctions.getItemCount(this, 7222) > 0L)
			_varka = 2;
		else if(ItemFunctions.getItemCount(this, 7221) > 0L)
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
		if(ItemFunctions.getItemCount(this, 7247) > 0L)
			_ram = 2;
		else if(ItemFunctions.getItemCount(this, 7246) > 0L)
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

	public void setLvlJoinedAcademy(final int lvl)
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
		if(isGM())
		{
			_pledgeRank = PledgeRank.EMPEROR;
			return;
		}
		final int CLAN_LEVEL = _clan == null ? -1 : _clan.getLevel();
		final boolean IN_ACADEMY = _clan != null && Clan.isAcademy(_pledgeType);
		final boolean IS_GUARD = _clan != null && Clan.isRoyalGuard(_pledgeType);
		final boolean IS_KNIGHT = _clan != null && Clan.isOrderOfKnights(_pledgeType);
		boolean IS_GUARD_CAPTAIN = false;
		boolean IS_KNIGHT_COMMANDER = false;
		boolean IS_LEADER = false;
		final SubUnit unit = getSubUnit();
		if(unit != null)
		{
			final UnitMember unitMember = unit.getUnitMember(getObjectId());
			if(unitMember == null)
			{
				Player._log.warn("Player: unitMember null, clan: " + _clan.getClanId() + "; pledgeType: " + unit.getType());
				return;
			}
			IS_GUARD_CAPTAIN = Clan.isRoyalGuard(unitMember.isLeaderOf());
			IS_KNIGHT_COMMANDER = Clan.isOrderOfKnights(unitMember.isLeaderOf());
			IS_LEADER = unitMember.isLeaderOf() == 0;
		}
		switch(CLAN_LEVEL)
		{
			case -1:
			{
				_pledgeRank = PledgeRank.VAGABOND;
				break;
			}
			case 0:
			case 1:
			case 2:
			case 3:
			{
				_pledgeRank = PledgeRank.VASSAL;
				break;
			}
			case 4:
			{
				if(IS_LEADER)
				{
					_pledgeRank = PledgeRank.KNIGHT;
					break;
				}
				_pledgeRank = PledgeRank.VASSAL;
				break;
			}
			case 5:
			{
				if(IS_LEADER)
				{
					_pledgeRank = PledgeRank.WISEMAN;
					break;
				}
				if(IN_ACADEMY)
				{
					_pledgeRank = PledgeRank.VASSAL;
					break;
				}
				_pledgeRank = PledgeRank.HEIR;
				break;
			}
			case 6:
			{
				if(IS_LEADER)
				{
					_pledgeRank = PledgeRank.BARON;
					break;
				}
				if(IN_ACADEMY)
				{
					_pledgeRank = PledgeRank.VASSAL;
					break;
				}
				if(IS_GUARD_CAPTAIN)
				{
					_pledgeRank = PledgeRank.WISEMAN;
					break;
				}
				if(IS_GUARD)
				{
					_pledgeRank = PledgeRank.HEIR;
					break;
				}
				_pledgeRank = PledgeRank.KNIGHT;
				break;
			}
			case 7:
			{
				if(IS_LEADER)
				{
					_pledgeRank = PledgeRank.COUNT;
					break;
				}
				if(IN_ACADEMY)
				{
					_pledgeRank = PledgeRank.VASSAL;
					break;
				}
				if(IS_GUARD_CAPTAIN)
				{
					_pledgeRank = PledgeRank.VISCOUNT;
					break;
				}
				if(IS_GUARD)
				{
					_pledgeRank = PledgeRank.KNIGHT;
					break;
				}
				if(IS_KNIGHT_COMMANDER)
				{
					_pledgeRank = PledgeRank.BARON;
					break;
				}
				if(IS_KNIGHT)
				{
					_pledgeRank = PledgeRank.HEIR;
					break;
				}
				_pledgeRank = PledgeRank.WISEMAN;
				break;
			}
			case 8:
			{
				if(IS_LEADER)
				{
					_pledgeRank = PledgeRank.MARQUIS;
					break;
				}
				if(IN_ACADEMY)
				{
					_pledgeRank = PledgeRank.VASSAL;
					break;
				}
				if(IS_GUARD_CAPTAIN)
				{
					_pledgeRank = PledgeRank.COUNT;
					break;
				}
				if(IS_GUARD)
				{
					_pledgeRank = PledgeRank.WISEMAN;
					break;
				}
				if(IS_KNIGHT_COMMANDER)
				{
					_pledgeRank = PledgeRank.VISCOUNT;
					break;
				}
				if(IS_KNIGHT)
				{
					_pledgeRank = PledgeRank.KNIGHT;
					break;
				}
				_pledgeRank = PledgeRank.BARON;
				break;
			}
			case 9:
			{
				if(IS_LEADER)
				{
					_pledgeRank = PledgeRank.DUKE;
					break;
				}
				if(IN_ACADEMY)
				{
					_pledgeRank = PledgeRank.VASSAL;
					break;
				}
				if(IS_GUARD_CAPTAIN)
				{
					_pledgeRank = PledgeRank.MARQUIS;
					break;
				}
				if(IS_GUARD)
				{
					_pledgeRank = PledgeRank.BARON;
					break;
				}
				if(IS_KNIGHT_COMMANDER)
				{
					_pledgeRank = PledgeRank.COUNT;
					break;
				}
				if(IS_KNIGHT)
				{
					_pledgeRank = PledgeRank.WISEMAN;
					break;
				}
				_pledgeRank = PledgeRank.VISCOUNT;
				break;
			}
			case 10:
			{
				if(IS_LEADER)
				{
					_pledgeRank = PledgeRank.GRAND_DUKE;
					break;
				}
				if(IN_ACADEMY)
				{
					_pledgeRank = PledgeRank.VASSAL;
					break;
				}
				if(IS_GUARD)
				{
					_pledgeRank = PledgeRank.VISCOUNT;
					break;
				}
				if(IS_KNIGHT)
				{
					_pledgeRank = PledgeRank.BARON;
					break;
				}
				if(IS_GUARD_CAPTAIN)
				{
					_pledgeRank = PledgeRank.DUKE;
					break;
				}
				if(IS_KNIGHT_COMMANDER)
				{
					_pledgeRank = PledgeRank.MARQUIS;
					break;
				}
				_pledgeRank = PledgeRank.COUNT;
				break;
			}
			case 11:
			{
				if(IS_LEADER)
				{
					_pledgeRank = PledgeRank.DISTINGUISHED_KING;
					break;
				}
				if(IN_ACADEMY)
				{
					_pledgeRank = PledgeRank.VASSAL;
					break;
				}
				if(IS_GUARD)
				{
					_pledgeRank = PledgeRank.COUNT;
					break;
				}
				if(IS_KNIGHT)
				{
					_pledgeRank = PledgeRank.VISCOUNT;
					break;
				}
				if(IS_GUARD_CAPTAIN)
				{
					_pledgeRank = PledgeRank.GRAND_DUKE;
					break;
				}
				if(IS_KNIGHT_COMMANDER)
				{
					_pledgeRank = PledgeRank.DUKE;
					break;
				}
				_pledgeRank = PledgeRank.MARQUIS;
				break;
			}
		}

		if((isHero() || isCustomHero()) && _pledgeRank.ordinal() < PledgeRank.MARQUIS.ordinal())
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
		if(getVarBoolean("factionColor")) {
			int color = Config.DONATE_FACTION_COLOR.getOrDefault(getFraction(), 0);
			if(color != 0) {
				return color;
			}
		}
		return getFraction().getNameColor();
	}

	@Override
	public Fraction getFraction() {
		if(isMercenary()) {
			return super.getFraction().revert();
		}
		return super.getFraction();
	}

	private void restoreVariables()
	{
		final List<CharacterVariable> variables = CharacterVariablesDAO.getInstance().restore(getObjectId());
		for(final CharacterVariable var : variables)
			_variables.put(var.getName(), var);
	}

	public Collection<CharacterVariable> getVariables()
	{
		return _variables.values();
	}

	public boolean setVar(final String name, final String value)
	{
		return setVar(name, value, -1L);
	}

	public boolean setVar(final String name, final String value, final long expirationTime)
	{
		final CharacterVariable var = new CharacterVariable(name, value, expirationTime);
		if(CharacterVariablesDAO.getInstance().insert(getObjectId(), var))
		{
			_variables.put(name, var);
			return true;
		}
		return false;
	}

	public boolean setVar(final String name, final int value)
	{
		return setVar(name, value, -1L);
	}

	public boolean setVar(final String name, final int value, final long expirationTime)
	{
		return setVar(name, String.valueOf(value), expirationTime);
	}

	public boolean setVar(final String name, final long value)
	{
		return setVar(name, value, -1L);
	}

	public boolean setVar(final String name, final long value, final long expirationTime)
	{
		return setVar(name, String.valueOf(value), expirationTime);
	}

	public boolean setVar(final String name, final double value)
	{
		return setVar(name, value, -1L);
	}

	public boolean setVar(final String name, final double value, final long expirationTime)
	{
		return setVar(name, String.valueOf(value), expirationTime);
	}

	public boolean setVar(final String name, final boolean value)
	{
		return setVar(name, value, -1L);
	}

	public boolean setVar(final String name, final boolean value, final long expirationTime)
	{
		return setVar(name, String.valueOf(value), expirationTime);
	}

	public boolean unsetVar(final String name)
	{
		return name != null && !name.isEmpty() && CharacterVariablesDAO.getInstance().delete(getObjectId(), name) && _variables.remove(name) != null;
	}

	public String getVar(final String name)
	{
		return getVar(name, null);
	}

	public String getVar(final String name, final String defaultValue)
	{
		final CharacterVariable var = _variables.get(name);
		if(var != null && !var.isExpired())
			return var.getValue();
		return defaultValue;
	}

	public long getVarExpireTime(final String name)
	{
		final CharacterVariable var = _variables.get(name);
		if(var != null)
			return var.getExpireTime();
		return 0L;
	}

	public int getVarInt(final String name)
	{
		return getVarInt(name, 0);
	}

	public int getVarInt(final String name, final int defaultValue)
	{
		final String var = getVar(name);
		if(var != null)
			return Integer.parseInt(var);
		return defaultValue;
	}

	public long getVarLong(final String name)
	{
		return getVarLong(name, 0L);
	}

	public long getVarLong(final String name, final long defaultValue)
	{
		final String var = getVar(name);
		if(var != null)
			return Long.parseLong(var);
		return defaultValue;
	}

	public double getVarDouble(final String name)
	{
		return getVarDouble(name, 0.0);
	}

	public double getVarDouble(final String name, final double defaultValue)
	{
		final String var = getVar(name);
		if(var != null)
			return Double.parseDouble(var);
		return defaultValue;
	}

	public boolean getVarBoolean(final String name)
	{
		return getVarBoolean(name, false);
	}

	public boolean getVarBoolean(final String name, final boolean defaultValue)
	{
		final String var = getVar(name);
		if(var != null)
			return !"0".equals(var) && !"false".equalsIgnoreCase(var);
		return defaultValue;
	}

	public void setLanguage(final String val)
	{
		_language = Language.getLanguage(val);
		setVar("lang@", _language.getShortName(), -1L);
	}

	public Language getLanguage()
	{
		if(Config.USE_CLIENT_LANG && getNetConnection() != null)
			return getNetConnection().getLanguage();
		return _language;
	}

	public int getLocationId()
	{
		if(getNetConnection() != null)
			return getNetConnection().getLanguage().getId();
		return -1;
	}

	public boolean isLangRus()
	{
		return getLanguage() == Language.RUSSIAN;
	}

	public int isAtWarWith(final Clan otherClan)
	{
		return _clan != null && _clan.isAtWarWith(otherClan) ? 1 : 0;
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

	@Override
	public boolean isDead()
	{
		return isInOlympiadMode() || isInDuel() ? getCurrentHp() <= 1.0 : super.isDead();
	}

	public void startWaterTask()
	{
		if(isDead())
			stopWaterTask();
		else if(Config.ALLOW_WATER && _taskWater == null)
		{
			final int timeinwater = (int) (calcStat(Stats.BREATH, getBaseStats().getBreathBonus(), null, null) * 1000.0);
			sendPacket(new SetupGaugePacket(this, SetupGaugePacket.Colors.BLUE, timeinwater));
			if(isTransformed() && !getTransform().isCanSwim())
				setTransform(null);
			_taskWater = ThreadPoolManager.getInstance().scheduleAtFixedRate(new GameObjectTasks.WaterTask(this), timeinwater, 1000L);
			sendChanges();
		}
	}

	public void doRevive(final double percent)
	{
		restoreExp(percent);
		doRevive();
	}

	@Override
	public void doRevive()
	{
		super.doRevive();
		unsetVar("lostexp");
		updateEffectIcons();
		autoShot();
		if(isMounted())
			_mount.onRevive();
		for(Servitor servitor : getServitors())
			servitor.notifyMasterRevival();
	}

	public void reviveRequest(final Player reviver, final double percent, final boolean pet)
	{
		final ReviveAnswerListener reviveAsk = _askDialog != null && _askDialog.getValue() instanceof ReviveAnswerListener ? (ReviveAnswerListener) _askDialog.getValue() : null;
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
			final ConfirmDlgPacket pkt = new ConfirmDlgPacket(SystemMsg.C1_IS_MAKING_AN_ATTEMPT_TO_RESURRECT_YOU_IF_YOU_CHOOSE_THIS_PATH_S2_EXPERIENCE_WILL_BE_RETURNED_FOR_YOU, 0);
			pkt.addName(reviver).addNumber(Math.round(percent));
			ask(pkt, new ReviveAnswerListener(this, percent, pet));
		}
	}

	public void requestCheckBot()
	{
		final BotCheckManager.BotCheckQuestion question = BotCheckManager.generateRandomQuestion();
		final int qId = question.getId();
		final String qDescr = question.getDescr(isLangRus());
		final ConfirmDlgPacket pkt = new ConfirmDlgPacket(SystemMsg.S1, 60000).addString(qDescr);
		ask(pkt, new BotCheckAnswerListner(this, qId));
	}

	public void increaseBotRating()
	{
		final int bot_points = getBotRating();
		if(bot_points + 1 >= Config.MAX_BOT_POINTS)
			return;
		setBotRating(bot_points + 1);
	}

	public void decreaseBotRating()
	{
		final int bot_points = getBotRating();
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

	public void setBotRating(final int rating)
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

	public void setIsInJail(final boolean value)
	{
		_isInJail = value;
	}

	public boolean toJail(final int time)
	{
		if(isInJail())
			return false;
		setIsInJail(true);
		setVar(JAILED_VAR, true, System.currentTimeMillis() + time * 60000);
		startUnjailTask(this, time);
		if(getReflection().isMain())
			setVar("backCoords", getLoc().toXYZString(), -1L);
		if(isInStoreMode())
			setPrivateStoreType(0);
		cancelPrivateBuffer();
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
		final String back = getVar("backCoords");
		if(back != null)
		{
			teleToLocation(Location.parseLoc(back), ReflectionManager.MAIN);
			unsetVar("backCoords");
		}
		return true;
	}

	public void summonCharacterRequest(final Creature summoner, final Location loc, final int summonConsumeCrystal)
	{
		final ConfirmDlgPacket cd = new ConfirmDlgPacket(SystemMsg.C1_WISHES_TO_SUMMON_YOU_FROM_S2, 60000);
		cd.addName(summoner).addZoneName(loc);
		ask(cd, new SummonAnswerListener(this, loc, summonConsumeCrystal));
	}

	public boolean canTalkWith(final Player player)
	{
		if (player.equals(this)) {
			return true;
		}

		if (PunishmentService.INSTANCE.isPunished(PunishmentType.CHAT, player)) {
			return false;
		}

		return true;
	}

	private void checkDailyCounters()
	{
		final Calendar temp = Calendar.getInstance();
		temp.set(11, 6);
		temp.set(12, 30);
		temp.set(13, 0);
		temp.set(14, 0);
		long daysPassed = Math.round((System.currentTimeMillis() / 1000L - _lastAccess) / 86400L);
		if(daysPassed == 0L && _lastAccess < temp.getTimeInMillis() / 1000L && System.currentTimeMillis() > temp.getTimeInMillis())
			++daysPassed;
		if(daysPassed > 0L)
			restartDailyCounters(true);
	}

	public void restartDailyCounters(final boolean onRestore)
	{
		if(Config.ALLOW_FRACTION_WORLD_CHAT)
		{
			setUsedWorldChatPoints(0);
			if(!onRestore)
				sendPacket(new ExWorldChatCnt(this));
		}
	}

	private void checkWeeklyCounters()
	{
		final Calendar temp = Calendar.getInstance();
		if(temp.get(7) > 4)
			temp.add(5, 7);
		temp.set(7, 4);
		temp.set(11, 6);
		temp.set(12, 30);
		temp.set(13, 0);
		temp.set(14, 0);
		if(_lastAccess < temp.getTimeInMillis() / 1000L && System.currentTimeMillis() > temp.getTimeInMillis())
			restartWeeklyCounters(true);
	}

	public void restartWeeklyCounters(final boolean onRestore)
	{}

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
		if(getActiveSubClass() != null)
			return getActiveSubClass().getClassId();
		return -1;
	}

	public boolean isBaseClassActive()
	{
		return getActiveSubClass().isBase();
	}

	public ClassId getClassId()
	{
		return ClassId.VALUES[getActiveClassId()];
	}

	public int getMaxLevel()
	{
		if(getActiveSubClass() != null)
			return getActiveSubClass().getMaxLevel();
		return Experience.getMaxLevel();
	}

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
		catch(SQLException e)
		{
			Player._log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void storeCharSubClasses()
	{
		final SubClass main = getActiveSubClass();
		if(main != null)
		{
			main.setCp(getCurrentCp());
			main.setHp(getCurrentHp());
			main.setMp(getCurrentMp());
		}
		else
			Player._log.warn("Could not store char sub data, main class " + getActiveClassId() + " not found for " + this);
			CharacterSubclassDAO.getInstance().store(this);
	}

	public boolean addSubClass(final int classId, final boolean storeOld, final long exp, final long sp)
	{
		return addSubClass(classId, storeOld, SubClassType.SUBCLASS, exp, sp);
	}

	public boolean addSubClass(final int classId, final boolean storeOld, final SubClassType type, final long exp, final long sp)
	{
		return addSubClass(-1, classId, storeOld, type, exp, sp);
	}

	private boolean addSubClass(final int oldClassId, final int classId, final boolean storeOld, final SubClassType type, final long exp, final long sp)
	{
		final ClassId newId = ClassId.VALUES[classId];
		if(newId.isDummy() || newId.isOfLevel(ClassLevel.NONE) || newId.isOfLevel(ClassLevel.FIRST))
			return false;
		final SubClass newClass = new SubClass(this);
		newClass.setType(type);
		newClass.setClassId(classId);
		if(exp > 0L)
			newClass.setExp(exp, true);
		if(sp > 0L)
			newClass.setSp(sp);
		if(!getSubClassList().add(newClass))
			return false;
		final int level = newClass.getLevel();
		final double hp = newId.getBaseHp(level);
		final double mp = newId.getBaseMp(level);
		final double cp = newId.getBaseCp(level);
		newClass.setHp(hp);
		newClass.setMp(mp);
		newClass.setCp(cp);
		if(!CharacterSubclassDAO.getInstance().insert(getObjectId(), newClass.getClassId(), newClass.getExp(), newClass.getSp(), hp, mp, cp, hp, mp, cp, level, false, type))
			return false;
		setActiveSubClass(classId, storeOld, false);
		rewardSkills(true, false, true);
		sendSkillList();
		sendSkillList();
		setCurrentHpMp(getMaxHp(), getMaxMp(), true);
		setCurrentCp(getMaxCp());
		final ClassId oldId = oldClassId >= 0 ? ClassId.VALUES[oldClassId] : null;
		onReceiveNewClassId(oldId, newId);
		return true;
	}

	public boolean modifySubClass(final int oldClassId, final int newClassId, final boolean safeExpSp)
	{
		final SubClass originalClass = getSubClassList().getByClassId(oldClassId);
		if(originalClass == null || originalClass.isBase())
			return false;
		final SubClassType type = originalClass.getType();
		long exp = 0L;
		long sp = 0L;
		if(safeExpSp)
		{
			exp = originalClass.getExp();
			sp = originalClass.getSp();
		}
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM character_subclasses WHERE char_obj_id=? AND class_id=? AND type != " + SubClassType.BASE_CLASS.ordinal());
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DbUtils.close(statement);
			statement = con.prepareStatement("DELETE FROM character_skills WHERE char_obj_id=? AND class_index=? ");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DbUtils.close(statement);
			statement = con.prepareStatement("DELETE FROM character_skills_save WHERE char_obj_id=? AND class_index=? ");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DbUtils.close(statement);
			statement = con.prepareStatement("DELETE FROM character_effects_save WHERE object_id=? AND id=? ");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DbUtils.close(statement);
			statement = con.prepareStatement("DELETE FROM character_hennas WHERE char_obj_id=? AND class_index=? ");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DbUtils.close(statement);
			statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE object_id=? AND class_index=? ");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DbUtils.close(statement);
		}
		catch(Exception e)
		{
			Player._log.warn("Could not delete char sub-class: " + e);
			Player._log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		getSubClassList().removeByClassId(oldClassId);
		return newClassId <= 0 || addSubClass(oldClassId, newClassId, false, type, exp, sp);
	}

	public void setActiveSubClass(final int subId, final boolean store, final boolean onRestore)
	{
		if(!onRestore)
		{
			final SubClass oldActiveSub = getActiveSubClass();
			if(oldActiveSub != null)
			{
				storeDisableSkills();
				if(store)
				{
					oldActiveSub.setCp(getMaxCp());
					oldActiveSub.setHp(getMaxHp());
					oldActiveSub.setMp(getMaxMp());
				}
			}
		}
		PartyClassLimitService.getInstance().checkKickFromParty(this, subId);
		final SubClass newActiveSub = _subClassList.changeActiveSubClass(subId);
		setClassId(subId, false, onRestore);
		removeAllSkills();
		abortCast(true, false);
		getAbnormalList().stopAllEffects();
		for(final Servitor servitor : getServitors())
			if(servitor != null && servitor.isSummon())
				servitor.unSummon(false);
		restoreSkills();
		SubsSkillsService.getInstance().restoreSkills(this);
		rewardSkills(false);
		checkSkills();
		refreshExpertisePenalty();
		getInventory().refreshEquip();
		getInventory().validateItems();
		getHennaList().restore();
		getDailyMissionList().restore();
		EffectsDAO.getInstance().restoreEffects(this);
		restoreDisableSkills();
		if(!onRestore && (newActiveSub.getHp() <= 0 || newActiveSub.getMp() <= 0)) {
			ClassId classId = ClassId.VALUES[newActiveSub.getClassId()];
			final int level = newActiveSub.getLevel();
			final double hp = classId.getBaseHp(level);
			final double mp = classId.getBaseMp(level);
			setCurrentHpMp(hp, mp);
		}
		else {
			setCurrentHpMp(newActiveSub.getHp(), newActiveSub.getMp());
		}
		setCurrentCp(newActiveSub.getCp());

		_shortCuts.restore();
		sendPacket(new ShortCutInitPacket(this));
		broadcastPacket(new SocialActionPacket(getObjectId(), 2122));
		setIncreasedForce(0);
		startHourlyTask();
		sendSkillList();
		broadcastCharInfo();
		updateEffectIcons();
		updateStats();
		getListeners().onActiveClass(getClassId(), newActiveSub, onRestore);
	}

	public void startKickTask(final long delayMillis)
	{
		stopKickTask();
		_kickTask = ThreadPoolManager.getInstance().schedule(new GameObjectTasks.KickTask(this), delayMillis);
	}

	public void stopKickTask()
	{
		if(_kickTask != null)
		{
			_kickTask.cancel(false);
			_kickTask = null;
		}
	}

	public boolean givePremiumAccount(final PremiumAccountTemplate premiumAccount, final int delay)
	{
		if(getNetConnection() == null)
			return false;
		final int type = premiumAccount.getType();
		if(type == 0)
			return false;
		int expireTime = delay > 0 ? (int) (delay * 60 * 60 + System.currentTimeMillis() / 1000L) : Integer.MAX_VALUE;
		boolean extended = false;
		final int oldAccountType = getNetConnection().getPremiumAccountType();
		final int oldAccountExpire = getNetConnection().getPremiumAccountExpire();
		if(oldAccountType == type && oldAccountExpire > System.currentTimeMillis() / 1000L)
		{
			expireTime += (int) (oldAccountExpire - System.currentTimeMillis() / 1000L);
			extended = true;
		}
		if(Config.PREMIUM_ACCOUNT_BASED_ON_GAMESERVER)
			PremiumAccountDAO.getInstance().insert(getAccountName(), type, expireTime);
		else
		{
			AuthServerCommunication authServerCommunication = GameServer.getInstance().getAuthServerCommunication();
			if(authServerCommunication.isShutdown())
				return false;
			authServerCommunication.sendPacket(new BonusRequest(getAccountName(), type, expireTime));
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
		final PremiumAccountTemplate oldPremiumAccount = getPremiumAccount();
		if(oldPremiumAccount.getType() == 0)
			return false;
		final double currentHpRatio = getCurrentHpRatio();
		final double currentMpRatio = getCurrentMpRatio();
		final double currentCpRatio = getCurrentCpRatio();
		removeStatsOwner(oldPremiumAccount);
		removeTriggers(oldPremiumAccount);
		final SkillEntry[] attachedSkills;
		final SkillEntry[] skills = attachedSkills = _premiumAccount.getAttachedSkills();
		for(final SkillEntry skill : attachedSkills)
			removeSkill(skill);
		if(skills.length > 0)
			sendSkillList();
		setCurrentHp(getMaxHp() * currentHpRatio, false);
		setCurrentMp(getMaxMp() * currentMpRatio);
		setCurrentCp(getMaxCp() * currentCpRatio);
		updateStats();
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
		final PremiumAccountTemplate premiumAccount = PremiumAccountHolder.getInstance().getPremiumAccount(Config.FREE_PA_TYPE);
		if(premiumAccount == null)
			return false;
		final boolean recieved = Boolean.parseBoolean(AccountVariablesDAO.getInstance().select(getAccountName(), FREE_PA_RECIEVED, "false"));
		if(recieved)
			return false;
		if(givePremiumAccount(premiumAccount, Config.FREE_PA_DELAY))
		{
			AccountVariablesDAO.getInstance().insert(getAccountName(), FREE_PA_RECIEVED, "true");
			if(Config.ENABLE_FREE_PA_NOTIFICATION)
			{
				CustomMessage message = null;
				final int accountExpire = getNetConnection().getPremiumAccountExpire();
				if(accountExpire != Integer.MAX_VALUE)
				{
					message = new CustomMessage("l2s.gameserver.model.Player.GiveFreePA");
					message.addString(TimeUtils.toSimpleFormat(accountExpire * 1000L));
				}
				else
					message = new CustomMessage("l2s.gameserver.model.Player.GiveUnlimFreePA");
				sendPacket(new ExShowScreenMessage(message.toString(this), 15000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true));
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
		final int accountType = getNetConnection().getPremiumAccountType();
		final PremiumAccountTemplate premiumAccount = accountType == 0 ? null : PremiumAccountHolder.getInstance().getPremiumAccount(accountType);
		if(premiumAccount != null)
		{
			final int accountExpire = getNetConnection().getPremiumAccountExpire();
			if(accountExpire > System.currentTimeMillis() / 1000L)
			{
				_premiumAccount = premiumAccount;
				final double currentHpRatio = getCurrentHpRatio();
				final double currentMpRatio = getCurrentMpRatio();
				final double currentCpRatio = getCurrentCpRatio();
				addTriggers(_premiumAccount);
				addStatFuncs(_premiumAccount.getStatFuncs());
				final SkillEntry[] attachedSkills;
				final SkillEntry[] skills = attachedSkills = _premiumAccount.getAttachedSkills();
				for(final SkillEntry skill : attachedSkills)
					addSkill(skill);
				if(skills.length > 0)
					sendSkillList();
				setCurrentHp(getMaxHp() * currentHpRatio, false);
				setCurrentMp(getMaxMp() * currentMpRatio);
				setCurrentCp(getMaxCp() * currentCpRatio);
				updateStats();
				final int itemsReceivedType = getVarInt(PA_ITEMS_RECIEVED);
				if(itemsReceivedType != premiumAccount.getType())
				{
					removePremiumAccountItems(false);
					final ItemData[] items = premiumAccount.getGiveItemsOnStart();
					if(items.length > 0)
						if(!isInventoryFull())
						{
							sendPacket(SystemMsg.THE_PREMIUM_ITEM_FOR_THIS_ACCOUNT_WAS_PROVIDED_IF_THE_PREMIUM_ACCOUNT_IS_TERMINATED_THIS_ITEM_WILL_BE_DELETED);
							for(final ItemData item : items)
								ItemFunctions.addItem(this, item.getId(), item.getCount(), true);
							setVar(PA_ITEMS_RECIEVED, accountType);
						}
						else
							sendPacket(SystemMsg.THE_PREMIUM_ITEM_CANNOT_BE_RECEIVED_BECAUSE_THE_INVENTORY_WEIGHTQUANTITY_LIMIT_HAS_BEEN_EXCEEDED);
				}
				if(accountExpire != Integer.MAX_VALUE)
					_premiumAccountExpirationTask = LazyPrecisionTaskManager.getInstance().startPremiumAccountExpirationTask(this, accountExpire);
				return true;
			}
			if(!Config.PREMIUM_ACCOUNT_BASED_ON_GAMESERVER)
				GameServer.getInstance().getAuthServerCommunication().sendPacket(new BonusRequest(getAccountName(), 0, 0));
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

	private void removePremiumAccountItems(final boolean notify)
	{
		final PremiumAccountTemplate premiumAccount = PremiumAccountHolder.getInstance().getPremiumAccount(getVarInt(PA_ITEMS_RECIEVED));
		if(premiumAccount != null)
		{
			final ItemData[] items = premiumAccount.getTakeItemsOnEnd();
			if(items.length > 0)
			{
				if(notify)
					sendPacket(SystemMsg.THE_PREMIUM_ACCOUNT_HAS_BEEN_TERMINATED_THE_PROVIDED_PREMIUM_ITEM_WAS_DELETED);
				for(final ItemData item : items)
					ItemFunctions.deleteItem(this, item.getId(), item.getCount(), notify);
				for(final ItemData item : items)
					ItemFunctions.deleteItemsEverywhere(this, item.getId());
			}
		}
		unsetVar(PA_ITEMS_RECIEVED);
	}

	@Override
	public int getInventoryLimit()
	{
		return (int) calcStat(Stats.INVENTORY_LIMIT, 0.0, null, null);
	}

	public int getWarehouseLimit()
	{
		return (int) calcStat(Stats.STORAGE_LIMIT, 0.0, null, null);
	}

	public int getTradeLimit()
	{
		return (int) calcStat(Stats.TRADE_LIMIT, 0.0, null, null);
	}

	public int getDwarvenRecipeLimit()
	{
		return (int) calcStat(Stats.DWARVEN_RECIPE_LIMIT, 50.0, null, null) + Config.ALT_ADD_RECIPES;
	}

	public int getCommonRecipeLimit()
	{
		return (int) calcStat(Stats.COMMON_RECIPE_LIMIT, 50.0, null, null) + Config.ALT_ADD_RECIPES;
	}

	public Element getAttackElement()
	{
		return Formulas.getAttackElement(this, null);
	}

	public int getAttack(final Element element)
	{
		if(element == Element.NONE)
			return 0;
		return (int) calcStat(element.getAttack(), 0.0, null, null);
	}

	public int getDefence(final Element element)
	{
		if(element == Element.NONE)
			return 0;
		return (int) calcStat(element.getDefence(), 0.0, null, null);
	}

	public boolean getAndSetLastItemAuctionRequest()
	{
		if(_lastItemAuctionInfoRequest + 2000L < System.currentTimeMillis())
		{
			_lastItemAuctionInfoRequest = System.currentTimeMillis();
			return true;
		}
		_lastItemAuctionInfoRequest = System.currentTimeMillis();
		return false;
	}

	@Override
	public int getNpcId()
	{
		return -2;
	}

	public GameObject getVisibleObject(final int id)
	{
		if(getObjectId() == id)
			return this;
		GameObject target = null;
		if(getTargetId() == id)
			target = getTarget();
		if(target == null && isInParty())
			for(final Player p : _party.getPartyMembers())
				if(p != null && p.getObjectId() == id)
				{
					target = p;
					break;
				}
		if(target == null)
			target = World.getAroundObjectById(this, id);
		return target == null || target.isInvisible(this) ? null : target;
	}

	public int getTitleColor()
	{
		return GveRewardHolder.getInstance().getTitleColor(getAdenaReward());
	}

	@Override
	public boolean isImmobilized()
	{
		return super.isImmobilized() || isOverloaded() || isSitting() || isFishing();
	}

	@Override
	public boolean isBlocked()
	{
		return super.isBlocked() || isInMovie() || isInObserverMode() || isTeleporting() || isLogoutStarted();
	}

	@Override
	public boolean isInvul()
	{
		return super.isInvul() || isInMovie() || isPrivateBuffer() || isInOfflineMode();
	}

	public void setOverloaded(final boolean overloaded)
	{
		_overloaded = overloaded;
	}

	public boolean isOverloaded()
	{
		return _overloaded;
	}

	public PremiumAccountTemplate getPremiumAccount()
	{
		return _premiumAccount;
	}

	public boolean hasPremiumAccount()
	{
		return _premiumAccount.getType() > 0;
	}

	public double getRateAdena()
	{
		return getRateAdena(false);
	}

	public double getRateAdena(boolean recalculatePartyData)
	{
		if(isInParty() && !recalculatePartyData)
			return _party._rateAdena;

		double rate = Config.RATE_DROP_ADENA_BY_LVL[getLevel()];
		rate *= getPremiumAccount().getRates().getAdena()
				+ calcStat(Stats.ADENA_RATE_MULTIPLIER, 0.0, null, null) + (isMercenary() ? 0.1 : 0);
		return rate;
	}

	public double getRateItems()
	{
		return getRateItems(false);
	}

	public double getRateItems(boolean recalculatePartyData)
	{
		if(isInParty() && !recalculatePartyData)
			return _party._rateDrop;

		double rate = Config.RATE_DROP_ITEMS_BY_LVL[getLevel()];
		rate *= getPremiumAccount().getRates().getDrop()
				+ calcStat(Stats.DROP_RATE_MULTIPLIER, 0.0, null, null);
		return rate;
	}

	public double getRateExp()
	{
		return getRateExp(false);
	}

	public double getRateExp(boolean recalculatePartyData)
	{
		if(isInParty() && !recalculatePartyData)
			return _party._rateExp;

		double rate = Config.RATE_XP_BY_LVL[getLevel()];
		rate *= getPremiumAccount().getRates().getExp()
				+ calcStat(Stats.EXP_RATE_MULTIPLIER, 0.0, null, null);
		return rate;
	}

	public double getRateSp()
	{
		return getRateSp(false);
	}

	public double getRateSp(boolean recalculatePartyData)
	{
		if(isInParty() && !recalculatePartyData)
			return _party._rateSp;

		double rate = Config.RATE_SP_BY_LVL[getLevel()];
		rate *= getPremiumAccount().getRates().getSp()
				+ calcStat(Stats.SP_RATE_MULTIPLIER, 0.0, null, null);
		return rate;
	}

	public double getRateSpoil()
	{
		return getRateSpoil(false);
	}

	public double getRateSpoil(boolean recalculatePartyData)
	{
		if(isInParty() && !recalculatePartyData)
			return _party._rateSpoil;

		double rate = Config.RATE_DROP_SPOIL_BY_LVL[getLevel()];
		rate *= getPremiumAccount().getRates().getSpoil()
				+ calcStat(Stats.SPOIL_RATE_MULTIPLIER, 0.0, null, null);
		return rate;
	}

	public double getRateQuestsDrop()
	{
		double rate = Config.RATE_QUESTS_DROP;
		rate *= getPremiumAccount().getRates().getQuestDrop();
		return rate;
	}

	public double getRateQuestsReward()
	{
		double rate = Config.RATE_QUESTS_REWARD;
		rate *= getPremiumAccount().getRates().getQuestReward();
		return rate;
	}

	public double getConfrontationPointsRate()
	{
		double rate = 1.0D;
		rate *= getPremiumAccount().getRates().getConfrontationPoints();
		return rate;
	}

	public double getDropChanceMod()
	{
		return getDropChanceMod(false);
	}

	public double getDropChanceMod(boolean recalculatePartyData)
	{
		if(isInParty() && !recalculatePartyData)
			return _party._dropChanceMod;

		double mod = Config.DROP_CHANCE_MODIFIER;
		mod *= getPremiumAccount().getModifiers().getDropChance()
				+ calcStat(Stats.DROP_CHANCE_MODIFIER, 0.0, null, null);
		return mod;
	}

	public double getSpoilChanceMod()
	{
		return getSpoilChanceMod(false);
	}

	public double getSpoilChanceMod(boolean recalculatePartyData)
	{
		if(isInParty() && !recalculatePartyData)
			return _party._spoilChanceMod;

		double mod = Config.SPOIL_CHANCE_MODIFIER;
		mod *= getPremiumAccount().getModifiers().getSpoilChance()
				+ calcStat(Stats.SPOIL_CHANCE_MODIFIER, 0.0, null, null);
		return mod;
	}

	public boolean isMaried()
	{
		return _maried;
	}

	public void setMaried(final boolean state)
	{
		_maried = state;
	}

	public void setMaryRequest(final boolean state)
	{
		_maryrequest = state;
	}

	public boolean isMaryRequest()
	{
		return _maryrequest;
	}

	public void setMaryAccepted(final boolean state)
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

	public void setPartnerId(final int partnerid)
	{
		_partnerId = partnerid;
	}

	public int getCoupleId()
	{
		return _coupleId;
	}

	public void setCoupleId(final int coupleId)
	{
		_coupleId = coupleId;
	}

	public void broadcastSnoop(final ChatType type, final String name, final String _text)
	{
		if(!_snoopListener.isEmpty())
		{
			final Snoop sn = new Snoop(getObjectId(), getName(), type.ordinal(), name, _text);
			for(final Player pci : _snoopListener)
				if(pci != null)
					pci.sendPacket(sn);
		}
	}

	public void addSnooper(final Player pci)
	{
		if(!_snoopListener.contains(pci))
			_snoopListener.add(pci);
	}

	public void removeSnooper(final Player pci)
	{
		_snoopListener.remove(pci);
	}

	public void addSnooped(final Player pci)
	{
		if(!_snoopedPlayer.contains(pci))
			_snoopedPlayer.add(pci);
	}

	public void removeSnooped(final Player pci)
	{
		_snoopedPlayer.remove(pci);
	}

	public void resetReuse()
	{
		_skillReuses.clear();
		_sharedGroupReuses.clear();
	}

	public boolean isCharmOfCourage()
	{
		return _charmOfCourage;
	}

	public void setCharmOfCourage(final boolean val)
	{
		_charmOfCourage = val;
		sendEtcStatusUpdate();
	}

	@Override
	public int getIncreasedForce()
	{
		return _increasedForce;
	}

	@Override
	public int getConsumedSouls()
	{
		return _consumedSouls;
	}

	@Override
	public void setConsumedSouls(int i, final NpcInstance monster)
	{
		if(i == _consumedSouls)
			return;
		final int max = (int) calcStat(Stats.SOULS_LIMIT, 0.0, monster, null);
		if(i > max)
			i = max;
		if(i <= 0)
		{
			_consumedSouls = 0;
			sendEtcStatusUpdate();
			return;
		}
		if(_consumedSouls != i)
		{
			final int diff = i - _consumedSouls;
			if(diff > 0)
			{
				final SystemMessage sm = new SystemMessage(2162);
				sm.addNumber(diff);
				sm.addNumber(i);
				sendPacket(sm);
			}
		}
		else if(max == i)
		{
			sendPacket(SystemMsg.SOUL_CANNOT_BE_ABSORBED_ANYMORE);
			return;
		}
		_consumedSouls = i;
		sendPacket(new EtcStatusUpdatePacket(this));
	}

	@Override
	public void setIncreasedForce(int i)
	{
		i = Math.min(i, getSkillLevel(10301) != -1 ? 15 : 10);
		i = Math.max(i, 0);
		if(i != 0 && i > _increasedForce)
			sendPacket(new SystemMessage(323).addNumber(i));
		_increasedForce = i;
		sendEtcStatusUpdate();
	}

	public boolean isFalling()
	{
		return System.currentTimeMillis() - _lastFalling < 5000L;
	}

	public void falling(final int height)
	{
		if(!Config.DAMAGE_FROM_FALLING || isDead() || isFlying() || isInWater() || isInBoat())
			return;
		_lastFalling = System.currentTimeMillis();
		final int damage = (int) calcStat(Stats.FALL, getMaxHp() / 2000.0 * height, null, null);
		if(damage > 0)
		{
			final int curHp = (int) getCurrentHp();
			if(curHp - damage < 1)
				setCurrentHp(1.0, false);
			else
				setCurrentHp(curHp - damage, false);
			sendPacket(new SystemMessage(296).addNumber(damage));
		}
	}

	@Override
	public void checkHpMessages(final double curHp, final double newHp)
	{
		final int[] _hp = { 30, 30 };
		final int[] skills = { 290, 291 };
		final double percent = getMaxHp() / 100;
		final double _curHpPercent = curHp / percent;
		final double _newHpPercent = newHp / percent;
		boolean needsUpdate = false;
		for(int i = 0; i < skills.length; ++i)
		{
			final int level = getSkillLevel(skills[i]);
			if(level > 0)
				if(_curHpPercent > _hp[i] && _newHpPercent <= _hp[i])
				{
					sendPacket(new SystemMessage(1133).addSkillName(skills[i], level));
					needsUpdate = true;
				}
				else if(_curHpPercent <= _hp[i] && _newHpPercent > _hp[i])
				{
					sendPacket(new SystemMessage(1134).addSkillName(skills[i], level));
					needsUpdate = true;
				}
		}
		final int[] _effects_hp = {30, 30, 30, 60, 30};
		final int[] _effects_skills_id = {139, 176, 292, 292, 420};
		for(Integer j = 0; j < _effects_skills_id.length; ++j)
			if(getAbnormalList().containsEffects(_effects_skills_id[j]))
				if(_curHpPercent > _effects_hp[j] && _newHpPercent <= _effects_hp[j])
				{
					sendPacket(new SystemMessage(1133).addSkillName(_effects_skills_id[j], 1));
					needsUpdate = true;
				}
				else if(_curHpPercent <= _effects_hp[j] && _newHpPercent > _effects_hp[j])
				{
					sendPacket(new SystemMessage(1134).addSkillName(_effects_skills_id[j], 1));
					needsUpdate = true;
				}
		if(needsUpdate)
			sendChanges();
	}

	public void checkDayNightMessages()
	{
		final int level = getSkillLevel(294);
		if(level > 0)
			if(GameTimeService.INSTANCE.isNowNight())
				sendPacket(new SystemMessage(1131).addSkillName(294, level));
			else
				sendPacket(new SystemMessage(1132).addSkillName(294, level));
		sendChanges();
	}

	public int getZoneMask()
	{
		return _zoneMask;
	}

	@Override
	protected void onUpdateZones(final List<Zone> leaving, final List<Zone> entering)
	{
		super.onUpdateZones(leaving, entering);
		if((leaving == null || leaving.isEmpty()) && (entering == null || entering.isEmpty()))
			return;
		final boolean lastInCombatZone = (_zoneMask & 0x4000) == 0x4000;
		final boolean lastInDangerArea = (_zoneMask & 0x100) == 0x100;
		final boolean lastOnSiegeField = (_zoneMask & 0x800) == 0x800;
		final boolean lastInPeaceZone = (_zoneMask & 0x1000) == 0x1000;
		final boolean isInCombatZone = isInZoneBattle();
		final boolean isInDangerArea = isInDangerArea() || isInZone(Zone.ZoneType.CHANGED_ZONE);
		final boolean isOnSiegeField = isInSiegeZone();
		final boolean isInPeaceZone = isInPeaceZone();
		final boolean isInSSQZone = isInSSQZone();
		final int lastZoneMask = _zoneMask;
		_zoneMask = 0;
		if(isInCombatZone)
			_zoneMask |= 0x4000;
		if(isInDangerArea)
			_zoneMask |= 0x100;
		if(isOnSiegeField)
			_zoneMask |= 0x800;
		if(isInPeaceZone)
			_zoneMask |= 0x1000;
		if(isInSSQZone)
			_zoneMask |= 0x2000;
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
				final FlagItemAttachment attachment = getActiveWeaponFlagAttachment();
				if(attachment != null)
					attachment.onLeaveSiegeZone(this);
				sendPacket(SystemMsg.YOU_HAVE_LEFT_A_COMBAT_ZONE);
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

	public void startUnjailTask(final Player player, final int time)
	{
		if(_unjailTask != null)
			_unjailTask.cancel(false);
		_unjailTask = ThreadPoolManager.getInstance().schedule(new GameObjectTasks.UnJailTask(player), time * 60000);
	}

	public void stopUnjailTask()
	{
		if(_unjailTask != null)
			_unjailTask.cancel(false);
		_unjailTask = null;
	}

	@Override
	public void sendMessage(final String message)
	{
        sendPacket(new SystemMessagePacket(SystemMsg.S1).addString(message));
	}
	
	public void setUseSeed(final int id)
	{
		_useSeed = id;
	}

	public int getUseSeed()
	{
		return _useSeed;
	}

	@Override
	public int getRelation(final Player target)
	{
		int result = 0;

		if(getClan() != null)
		{
			result |= RelationChangedPacket.RELATION_CLAN_MEMBER;
			if(getClan() == target.getClan())
				result |= RelationChangedPacket.RELATION_CLAN_MATE;
			if(getClan().getAllyId() != 0)
				result |= RelationChangedPacket.RELATION_ALLY_MEMBER;
		}
		if(isClanLeader())
			result |= RelationChangedPacket.RELATION_LEADER;

		final Party party = getParty();
		if(party != null && party == target.getParty())
		{
			result |= RelationChangedPacket.RELATION_HAS_PARTY;

			switch(party.getPartyMembers().indexOf(this))
			{
				case 0:
					result |= RelationChangedPacket.RELATION_PARTYLEADER; // 0x10
					break;
				case 1:
					result |= RelationChangedPacket.RELATION_PARTY4; // 0x8
					break;
				case 2:
					result |= RelationChangedPacket.RELATION_PARTY3 + RelationChangedPacket.RELATION_PARTY2 + RelationChangedPacket.RELATION_PARTY1; // 0x7
					break;
				case 3:
					result |= RelationChangedPacket.RELATION_PARTY3 + RelationChangedPacket.RELATION_PARTY2; // 0x6
					break;
				case 4:
					result |= RelationChangedPacket.RELATION_PARTY3 + RelationChangedPacket.RELATION_PARTY1; // 0x5
					break;
				case 5:
					result |= RelationChangedPacket.RELATION_PARTY3; // 0x4
					break;
				case 6:
					result |= RelationChangedPacket.RELATION_PARTY2 + RelationChangedPacket.RELATION_PARTY1; // 0x3
					break;
				case 7:
					result |= RelationChangedPacket.RELATION_PARTY2; // 0x2
					break;
				case 8:
					result |= RelationChangedPacket.RELATION_PARTY1; // 0x1
					break;
			}
		}

		final Clan clan1 = getClan();
		final Clan clan2 = target.getClan();

		if(clan1 != null && clan2 != null)
		{
			if(target.getPledgeType() != Clan.SUBUNIT_ACADEMY && getPledgeType() != Clan.SUBUNIT_ACADEMY)
			{
				if(clan2.isAtWarWith(clan1))
				{
					result |= RelationChangedPacket.RELATION_1SIDED_WAR;
					if(clan1.isAtWarWith(clan2))
						result |= RelationChangedPacket.RELATION_MUTUAL_WAR;
				}
			}
		}

		for(final Event e : getEvents())
			result = e.getRelation(this, target, result);
		return result;
	}

	public long getLastPvPAttack()
	{
		return isVioletBoy() ? System.currentTimeMillis() : _lastPvPAttack;
	}

	public void setLastPvPAttack(final long time)
	{
		_lastPvPAttack = time;
	}

	@Override
	public void startPvPFlag(final Creature target)
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
			_PvPRegTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new GameObjectTasks.PvPFlagTask(this), 1000L, 1000L);
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

	public void updatePvPFlag(final int value)
	{
		if(getPvpFlag() == value)
			return;
		setPvpFlag(value);
		sendStatusUpdate(true, true, 26);
		broadcastRelation();
	}

	public void setPvpFlag(final int pvpFlag)
	{
		_pvpFlag = pvpFlag;
	}

	@Override
	public int getPvpFlag()
	{
		return isVioletBoy() ? 1 : _pvpFlag;
	}

	public long getLastAttackPacket()
	{
		return _lastAttackPacket;
	}

	public void setLastAttackPacket()
	{
		_lastAttackPacket = System.currentTimeMillis();
	}

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

	@Override
	public final Collection<SkillEntry> getAllSkills()
	{
		if(!isTransformed())
			return super.getAllSkills();
		final IntObjectMap<SkillEntry> temp = new HashIntObjectMap<>();
		for(final SkillEntry skillEntry : super.getAllSkills())
		{
			final Skill skill = skillEntry.getTemplate();
			if(!skill.isActive() && !skill.isToggle())
				temp.put(skillEntry.getId(), skillEntry);
		}
		temp.putAll(_transformSkills);
		return temp.values();
	}

	public final void addTransformSkill(final SkillEntry skillEntry)
	{
		_transformSkills.put(skillEntry.getId(), skillEntry);
	}

	public final void removeTransformSkill(final SkillEntry skillEntry)
	{
		_transformSkills.remove(skillEntry.getId());
	}

	public void setAgathion(final int id)
	{
		if(_agathionId == id)
			return;
		_agathionId = id;
		sendPacket(new ExUserInfoCubic(this));
		broadcastCharInfo();
	}

	public int getAgathionId()
	{
		return _agathionId;
	}

	public int getPcBangPoints()
	{
		return _pcBangPoints;
	}

	public void setPcBangPoints(final int val)
	{
		_pcBangPoints = val;
	}

	public void addPcBangPoints(int count, final boolean doublePoints, final boolean notify)
	{
		if(doublePoints)
			count *= 2;
		_pcBangPoints += count;
		if(count > 0 && notify)
			sendPacket(new SystemMessage(doublePoints ? 1708 : 1707).addNumber(count));
		sendPacket(new ExPCCafePointInfoPacket(this, count, 1, 2, 12));
	}

	public boolean reducePcBangPoints(final int count)
	{
		if(_pcBangPoints < count)
			return false;
		_pcBangPoints -= count;
		sendPacket(new SystemMessage(1709).addNumber(count));
		sendPacket(new ExPCCafePointInfoPacket(this, 0, 1, 2, 12));
		return true;
	}

	public void setGroundSkillLoc(final Location location)
	{
		_groundSkillLoc = location;
	}

	public Location getGroundSkillLoc()
	{
		return _groundSkillLoc;
	}

	public boolean isLogoutStarted()
	{
		return _isLogout.get();
	}

	public void setOfflineMode(final boolean val)
	{
		if(!val)
			unsetVar("offline");
		_offline = val;
	}

	public boolean isInOfflineMode()
	{
		return _offline;
	}

	public void saveTradeList()
	{
		String val = "";
		if(_sellList == null || _sellList.isEmpty())
			unsetVar("selllist");
		else
		{
			for(final TradeItem i : _sellList)
				val = val + i.getObjectId() + ";" + i.getCount() + ";" + i.getOwnersPrice() + ";" + i.getItemId() + ":";
			setVar("selllist", val, -1L);
			val = "";
			if(_tradeList != null && getSellStoreName() != null)
				setVar("sellstorename", getSellStoreName(), -1L);
		}
		if(_packageSellList == null || _packageSellList.isEmpty())
			unsetVar("packageselllist");
		else
		{
			for(final TradeItem i : _packageSellList)
				val = val + i.getObjectId() + ";" + i.getCount() + ";" + i.getOwnersPrice() + ";" + i.getItemId() + ":";
			setVar("packageselllist", val, -1L);
			val = "";
			if(_tradeList != null && getSellStoreName() != null)
				setVar("sellstorename", getSellStoreName(), -1L);
		}
		if(_buyList == null || _buyList.isEmpty())
			unsetVar("buylist");
		else
		{
			for(final TradeItem i : _buyList)
				val = val + i.getItemId() + ";" + i.getCount() + ";" + i.getOwnersPrice() + ":";
			setVar("buylist", val, -1L);
			val = "";
			if(_tradeList != null && getBuyStoreName() != null)
				setVar("buystorename", getBuyStoreName(), -1L);
		}
		if(_createList == null || _createList.isEmpty())
			unsetVar("createlist");
		else
		{
			for(final ManufactureItem j : _createList)
				val = val + j.getRecipeId() + ";" + j.getCost() + ":";
			setVar("createlist", val, -1L);
			if(getManufactureName() != null)
				setVar("manufacturename", getManufactureName(), -1L);
		}
	}

	public void restoreTradeList()
	{
		String var = getVar("selllist");
		if(var != null)
		{
			_sellList = new CopyOnWriteArrayList<>();
			final String[] items = var.split(":");
			for(final String item : items)
				if(!"".equals(item))
				{
					final String[] values = item.split(";");
					if(values.length >= 3)
					{
						final int oId = Integer.parseInt(values[0]);
						long count = Long.parseLong(values[1]);
						final long price = Long.parseLong(values[2]);
						final ItemInstance itemToSell = getInventory().getItemByObjectId(oId);
						if(count >= 1L)
							if(itemToSell != null)
							{
								if(count > itemToSell.getCount())
									count = itemToSell.getCount();
								final TradeItem i = new TradeItem(itemToSell);
								i.setCount(count);
								i.setOwnersPrice(price);
								_sellList.add(i);
							}
					}
				}
			var = getVar("sellstorename");
			if(var != null)
				setSellStoreName(var);
		}
		var = getVar("packageselllist");
		if(var != null)
		{
			_packageSellList = new CopyOnWriteArrayList<>();
			final String[] items = var.split(":");
			for(final String item : items)
				if(!"".equals(item))
				{
					final String[] values = item.split(";");
					if(values.length >= 3)
					{
						final int oId = Integer.parseInt(values[0]);
						long count = Long.parseLong(values[1]);
						final long price = Long.parseLong(values[2]);
						final ItemInstance itemToSell = getInventory().getItemByObjectId(oId);
						if(count >= 1L)
							if(itemToSell != null)
							{
								if(count > itemToSell.getCount())
									count = itemToSell.getCount();
								final TradeItem i = new TradeItem(itemToSell);
								i.setCount(count);
								i.setOwnersPrice(price);
								_packageSellList.add(i);
							}
					}
				}
			var = getVar("sellstorename");
			if(var != null)
				setSellStoreName(var);
		}
		var = getVar("buylist");
		if(var != null)
		{
			_buyList = new CopyOnWriteArrayList<>();
			final String[] items = var.split(":");
			for(final String item : items)
				if(!"".equals(item))
				{
					final String[] values = item.split(";");
					if(values.length >= 3)
					{
						final TradeItem j = new TradeItem();
						j.setItemId(Integer.parseInt(values[0]));
						j.setCount(Long.parseLong(values[1]));
						j.setOwnersPrice(Long.parseLong(values[2]));
						_buyList.add(j);
					}
				}
			var = getVar("buystorename");
			if(var != null)
				setBuyStoreName(var);
		}
		var = getVar("createlist");
		if(var != null)
		{
			_createList = new CopyOnWriteArrayList<>();
			final String[] items = var.split(":");
			for(final String item : items)
				if(!"".equals(item))
				{
					final String[] values = item.split(";");
					if(values.length >= 2)
					{
						final int recId = Integer.parseInt(values[0]);
						final long price2 = Long.parseLong(values[1]);
						if(findRecipe(recId))
							_createList.add(new ManufactureItem(recId, price2));
					}
				}
			var = getVar("manufacturename");
			if(var != null)
				setManufactureName(var);
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
				final int id = rset.getInt("id");
				final RecipeTemplate recipe = RecipeHolder.getInstance().getRecipeByRecipeId(id);
				registerRecipe(recipe, false);
			}
		}
		catch(Exception e)
		{
			Player._log.warn("count not recipe skills:" + e);
			Player._log.error("", e);
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

	public void addDecoy(final DecoyInstance decoy)
	{
		_decoys.add(decoy);
	}

	public void removeDecoy(final DecoyInstance decoy)
	{
		_decoys.remove(decoy);
	}

	public MountType getMountType()
	{
		return _mount == null ? MountType.NONE : _mount.getType();
	}

	@Override
	public void setReflection(final Reflection reflection)
	{
		if(getReflection() == reflection)
			return;
		super.setReflection(reflection);
		for(final Servitor servitor : getServitors())
			if(!servitor.isDead())
				servitor.setReflection(reflection);
		if(!reflection.isMain())
		{
			final String var = getVar("reflection");
			if(var == null || !var.equals(String.valueOf(reflection.getId())))
				setVar("reflection", String.valueOf(reflection.getId()), -1L);
		}
		else
			unsetVar("reflection");
	}

	public void setBuyListId(final int listId)
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

	public void setFame(int fame, final String log, final boolean notify)
	{
		//FIXME удалить что ли
		//		fame = Math.min(Config.LIM_FAME, fame);
		//		if(log != null && !log.isEmpty())
		//			LogService.add(_name + "|" + (fame - _fame) + "|" + fame + "|" + log, "fame");
		//		if(fame > _fame && notify)
		//			this.sendPacket(new SystemMessage(2319).addNumber(fame - _fame));
		//		_fame = fame;
		//		sendChanges();
	}

	public int getIncorrectValidateCount()
	{
		return 0;
	}

	public int setIncorrectValidateCount(final int count)
	{
		return 0;
	}

	public int getExpandInventory()
	{
		return _expandInventory;
	}

	public void setExpandInventory(final int inventory)
	{
		_expandInventory = inventory;
	}

	public int getExpandWarehouse()
	{
		return _expandWarehouse;
	}

	public void setExpandWarehouse(final int warehouse)
	{
		_expandWarehouse = warehouse;
	}

	public boolean isNotShowBuffAnim()
	{
		return _notShowBuffAnim;
	}

	public void setNotShowBuffAnim(final boolean value)
	{
		_notShowBuffAnim = value;
	}

	public boolean canSeeAllShouts()
	{
		return _canSeeAllShouts;
	}

	public void setCanSeeAllShouts(final boolean b)
	{
		_canSeeAllShouts = b;
	}

	public void enterMovieMode()
	{
		if(isInMovie())
			return;
		setTarget(null);
		stopMove();
		setMovieId(-1);
		sendPacket(new CameraModePacket(1));
	}

	public void leaveMovieMode()
	{
		setMovieId(0);
		sendPacket(new CameraModePacket(0));
		broadcastCharInfo();
	}

	public void specialCamera(final GameObject target, final int dist, final int yaw, final int pitch, final int time, final int duration)
	{
		sendPacket(new SpecialCameraPacket(target.getObjectId(), dist, yaw, pitch, time, duration));
	}

	public void specialCamera(final GameObject target, final int dist, final int yaw, final int pitch, final int time, final int duration, final int turn, final int rise, final int widescreen, final int unk)
	{
		sendPacket(new SpecialCameraPacket(target.getObjectId(), dist, yaw, pitch, time, duration, turn, rise, widescreen, unk));
	}

	public void setMovieId(final int id)
	{
		_movieId = id;
	}

	public int getMovieId()
	{
		return _movieId;
	}

	public boolean isInMovie()
	{
		return _movieId != 0;
	}

	public void startScenePlayer(final SceneMovie movie)
	{
		if(isInMovie())
			return;
		sendActionFailed();
		setTarget(null);
		stopMove();
		setMovieId(movie.getId());
		sendPacket(movie.packet(this));
	}

	public void startScenePlayer(final int movieId)
	{
		if(isInMovie())
			return;
		sendActionFailed();
		setTarget(null);
		stopMove();
		setMovieId(movieId);
		sendPacket(new ExStartScenePlayer(movieId));
	}

	public void endScenePlayer()
	{
		if(!isInMovie())
			return;
		setMovieId(0);
		decayMe();
		spawnMe();
	}

	public void setAutoLoot(final boolean enable)
	{
		if(Config.AUTO_LOOT_INDIVIDUAL)
		{
			_autoLoot = enable;
			setVar("AutoLoot", String.valueOf(enable), -1L);
		}
	}

	public void setAutoLootOnlyAdena(final boolean enable)
	{
		if(Config.AUTO_LOOT_INDIVIDUAL && Config.AUTO_LOOT_ONLY_ADENA)
		{
			_autoLootOnlyAdena = enable;
			setVar("AutoLootOnlyAdena", String.valueOf(enable), -1L);
		}
	}

	public void setAutoLootHerbs(final boolean enable)
	{
		if(Config.AUTO_LOOT_INDIVIDUAL)
		{
			AutoLootHerbs = enable;
			setVar("AutoLootHerbs", String.valueOf(enable), -1L);
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

	public final void reName(final String name, final boolean saveToDB)
	{
		setName(name);
		if(saveToDB)
			saveNameToDB();
		broadcastUserInfo(true);
	}

	public final void reName(final String name)
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
			Player._log.error("", e);
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

	public final void disableDrop(final int time)
	{
		_dropDisabled = System.currentTimeMillis() + time;
	}

	public final boolean isDropDisabled()
	{
		return _dropDisabled > System.currentTimeMillis();
	}

	public void setPetControlItem(final int itemObjId)
	{
		setPetControlItem(getInventory().getItemByObjectId(itemObjId));
	}

	public void setPetControlItem(final ItemInstance item)
	{
		_petControlItem = item;
	}

	public ItemInstance getPetControlItem()
	{
		return _petControlItem;
	}

	public boolean isActive()
	{
		return isActive.get();
	}

	public void setActive()
	{
		setNonAggroTime(0L);
		setNonPvpTime(0L);
		if(isActive.getAndSet(true))
			return;
		onActive();
	}

	private void onActive()
	{
		sendPacket(SystemMsg.YOU_ARE_NO_LONGER_PROTECTED_FROM_AGGRESSIVE_MONSTERS);
		if(getPetControlItem() != null || _restoredSummons != null && !_restoredSummons.isEmpty())
			ThreadPoolManager.getInstance().execute(() -> {
				if(getPetControlItem() != null)
					summonPet();
				if(_restoredSummons != null && !_restoredSummons.isEmpty())
					spawnRestoredSummons();
			});
	}

	public void summonPet()
	{
		if(getPet() != null)
			return;
		final ItemInstance controlItem = getInventory().getItemByObjectId(getPetControlItem().getObjectId());
		if(controlItem == null)
		{
			setPetControlItem(null);
			return;
		}
		final PetData petTemplate = PetDataHolder.getInstance().getTemplateByItemId(controlItem.getItemId());
		if(petTemplate == null)
		{
			setPetControlItem(null);
			return;
		}
		final NpcTemplate npcTemplate = NpcHolder.getInstance().getTemplate(petTemplate.getNpcId());
		if(npcTemplate == null)
		{
			setPetControlItem(null);
			return;
		}
		final PetInstance pet = PetInstance.restore(controlItem, npcTemplate, this);
		if(pet == null)
		{
			setPetControlItem(null);
			return;
		}
		setPet(pet);
		pet.setTitle("%OWNER_NAME%");
		if(!pet.isRespawned())
		{
			pet.setCurrentHp(pet.getMaxHp(), false);
			pet.setCurrentMp(pet.getMaxMp());
			pet.setCurrentFed(pet.getMaxFed(), false);
			pet.updateControlItem();
			pet.store();
		}
		pet.getInventory().restore();
		pet.setNonAggroTime(System.currentTimeMillis() + Config.NONAGGRO_TIME_ONTELEPORT);
		pet.setNonPvpTime(System.currentTimeMillis() + Config.NONPVP_TIME_ONTELEPORT);
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
		for(final SummonInstance.RestoredSummon summon : _restoredSummons)
		{
			final Skill skill = SkillHolder.getInstance().getSkill(summon.skillId, summon.skillLvl);
			if(skill == null)
				continue;
			if(!(skill instanceof Summon))
				continue;
			((Summon) skill).summon(this, null, summon);
		}
		_restoredSummons.clear();
		_restoredSummons = null;
	}

	public List<TrapInstance> getTraps()
	{
		return _traps;
	}

	public void addTrap(final TrapInstance trap)
	{
		_traps.add(trap);
	}

	public void removeTrap(final TrapInstance trap)
	{
		_traps.remove(trap);
	}

	public void destroyAllTraps()
	{
		for(final TrapInstance t : _traps)
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

	public int getHoursInGame()
	{
		return ++_hoursInGame;
	}

	public void startHourlyTask()
	{
		_hourlyTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new GameObjectTasks.HourlyTask(this), 3600000L, 3600000L);
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
		return 0L;
	}

	public boolean reducePremiumPoints(final int val)
	{
		if(Config.IM_PAYMENT_ITEM_ID > 0)
			return ItemFunctions.deleteItem(this, Config.IM_PAYMENT_ITEM_ID, val, true);
		if(getNetConnection() != null)
		{
			getNetConnection().setPoints((int) (getPremiumPoints() - val));
			GameServer.getInstance().getAuthServerCommunication().sendPacket(new ReduceAccountPoints(getAccountName(), val));
			return true;
		}
		return false;
	}

	public boolean isAgathionResAvailable()
	{
		return _agathionResAvailable;
	}

	public void setAgathionRes(final boolean val)
	{
		_agathionResAvailable = val;
	}

	public void addSessionVar(String name, Object value)
	{
		if(sessionVars.containsKey(name))
			sessionVars.remove(name);
		sessionVars.put(name, value);
	}

	public String getSessionVarS(String name, String... defaultValue)
	{
		if(!sessionVars.containsKey(name))
		{
			if(defaultValue.length > 0)
				return defaultValue[0];
			return null;
		}
		return (String) sessionVars.get(name);
	}

	public boolean getSessionVarB(String name, boolean... defaultValue)
	{
		if(!sessionVars.containsKey(name))
		{
			if(defaultValue.length > 0)
				return defaultValue[0];
			return false;
		}
		return (Boolean) sessionVars.get(name);
	}

	public int getSessionVarI(String name, int... defaultValue)
	{
		if(!sessionVars.containsKey(name))
		{
			if(defaultValue.length > 0)
				return defaultValue[0];
			return -1;
		}
		return (Integer) sessionVars.get(name);
	}

	public long getSessionVarL(String name, long... defaultValue)
	{
		if(!sessionVars.containsKey(name))
		{
			if(defaultValue.length > 0)
				return defaultValue[0];
			return -1L;
		}
		return (Long) sessionVars.get(name);
	}

	public Object getSessionVarO(String name, Object... defaultValue)
	{
		if(!sessionVars.containsKey(name))
		{
			if(defaultValue.length > 0)
				return defaultValue[0];
			return null;
		}
		return sessionVars.get(name);
	}

	public boolean containsSessionVar(String name)
	{
		return sessionVars.containsKey(name);
	}

	public void deleteSessionVar(String name)
	{
		sessionVars.remove(name);
	}

	public BlockList getBlockList()
	{
		return _blockList;
	}

	public FriendList getFriendList()
	{
		return _friendList;
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

	public boolean isNotShowTraders()
	{
		return _notShowTraders;
	}

	public void setNotShowTraders(final boolean notShowTraders)
	{
		_notShowTraders = notShowTraders;
	}

	public boolean isDebug()
	{
		return _debug;
	}

	public void setDebug(final boolean b)
	{
		_debug = b;
	}

	public void sendItemList(final boolean show)
	{
		final ItemInstance[] items = getInventory().getItems();
		final LockType lockType = getInventory().getLockType();
		final int[] lockItems = getInventory().getLockItems();
		final int allSize = items.length;
		int questItemsSize = 0;
		final int agathionItemsSize = 0;
		for(final ItemInstance item : items)
			if(item.getTemplate().isQuest())
				++questItemsSize;
		sendPacket(new ItemListPacket(this, allSize - questItemsSize, items, show, lockType, lockItems));
		sendPacket(new ExQuestItemListPacket(questItemsSize, items, lockType, lockItems));
	}

	@Override
	public boolean isPlayer()
	{
		return true;
	}

	public boolean checkCoupleAction(final Player target)
	{
		if(target.getPrivateStoreType() != 0)
		{
			sendPacket(new SystemMessage(3123).addName(target));
			return false;
		}
		if(target.isFishing())
		{
			sendPacket(new SystemMessage(3124).addName(target));
			return false;
		}
		if(target.isTransformed())
		{
			sendPacket(new SystemMessage(3133).addName(target));
			return false;
		}
		if(target.isInCombat() || target.isVisualTransformed())
		{
			sendPacket(new SystemMessage(3125).addName(target));
			return false;
		}
		if(target.isInOlympiadMode())
		{
			sendPacket(new SystemMessage(3128).addName(target));
			return false;
		}
		if(target.isInSiegeZone())
		{
			sendPacket(new SystemMessage(3130).addName(target));
			return false;
		}
		if(target.isInBoat() || target.getMountNpcId() != 0)
		{
			sendPacket(new SystemMessage(3131).addName(target));
			return false;
		}
		if(target.isTeleporting())
		{
			sendPacket(new SystemMessage(3132).addName(target));
			return false;
		}
		if(target.isDead())
		{
			sendPacket(new SystemMessage(3139).addName(target));
			return false;
		}
		return true;
	}

	@Override
	public void startAttackStanceTask()
	{
		startAttackStanceTask0();
		for(final Servitor servitor : getServitors())
			servitor.startAttackStanceTask0();
	}

	@Override
	public void displayGiveDamageMessage(Creature target, int damage, Servitor servitorTransferedDamage, int transferedDamage, boolean crit, boolean miss, boolean shld, boolean magic)
	{
		super.displayGiveDamageMessage(target, damage, servitorTransferedDamage, transferedDamage, crit, miss, shld, magic);

		if(miss)
		{
			sendPacket(new SystemMessage(2265).addName(this));
			return;
		}

		if(crit)
		{
			if(magic)
			{
				ExMagicAttackInfo.packet(this, target, MagicAttackType.CRITICAL);
				sendPacket(SystemMsg.MAGIC_CRITICAL_HIT);
			}
			else
				sendPacket(new SystemMessage(2266).addName(this));
		}

		if(target.isDamageBlocked(this, null))
		{
			ExMagicAttackInfo.packet(this, target, MagicAttackType.IMMUNE);
			sendPacket(SystemMsg.THE_ATTACK_HAS_BEEN_BLOCKED);
		}
		else if(target.isDoor() || target instanceof SiegeToggleNpcInstance)
			sendPacket(new SystemMessagePacket(SystemMsg.YOU_HIT_FOR_S1_DAMAGE).addNumber(damage));
		else
		{
			if(servitorTransferedDamage != null && transferedDamage > 0)
			{
				final SystemMessagePacket sm = new SystemMessagePacket(SystemMsg.C1_INFLICTED_S3_DAMAGE_ON_C2_AND_S4_DAMAGE_ON_THE_DAMAGE_TRANSFER_TARGET);
				sm.addName(this);
				sm.addNumber(damage);
				sm.addName(target);
				sm.addNumber(transferedDamage);
				sm.addHpChange(target.getObjectId(), getObjectId(), -damage);
				sm.addHpChange(servitorTransferedDamage.getObjectId(), getObjectId(), -transferedDamage);
				sendPacket(sm);
			}
			else
				sendPacket(new SystemMessagePacket(SystemMsg.C1_HAS_DONE_S3_POINTS_OF_DAMAGE_TO_C2).addName(this).addName(target).addNumber(damage).addHpChange(target.getObjectId(), getObjectId(), -damage));

			if(target.isPlayer())
				if(shld && damage > 1)
				{
					if(magic)
						ExMagicAttackInfo.packet(this, target, MagicAttackType.BLOCKED);

					target.sendPacket(SystemMsg.YOUR_SHIELD_DEFENSE_HAS_SUCCEEDED);
				}
				else if(shld && damage == 1)
				{
					if(magic)
						ExMagicAttackInfo.packet(this, target, MagicAttackType.BLOCKED);

					target.sendPacket(SystemMsg.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS);
				}
		}

	}

	@Override
	public void displayReceiveDamageMessage(final Creature attacker, final int damage)
	{
		if(attacker != this)
			sendPacket(new SystemMessagePacket(SystemMsg.C1_HAS_RECEIVED_S3_DAMAGE_FROM_C2).addName(this).addName(attacker).addNumber(damage).addHpChange(getObjectId(), attacker.getObjectId(), -damage));
	}

	public IntObjectMap<String> getPostFriends()
	{
		return _postFriends;
	}

	public void setPostFriends(final IntObjectMap<String> val)
	{
		_postFriends = val;
	}

	public void sendReuseMessage(final ItemInstance item)
	{
		final TimeStamp sts = getSharedGroupReuse(item.getTemplate().getReuseGroup());
		if(sts == null || !sts.hasNotPassed())
			return;
		final long timeleft = sts.getReuseCurrent();
		final long hours = timeleft / 3600000L;
		final long minutes = (timeleft - hours * 3600000L) / 60000L;
		final long seconds = (long) Math.ceil((timeleft - hours * 3600000L - minutes * 60000L) / 1000.0);
		if(hours > 0L)
			sendPacket(new SystemMessagePacket(item.getTemplate().getReuseType().getMessages()[2]).addItemName(item.getTemplate().getItemId()).addNumber(hours).addNumber(minutes).addNumber(seconds));
		else if(minutes > 0L)
			sendPacket(new SystemMessagePacket(item.getTemplate().getReuseType().getMessages()[1]).addItemName(item.getTemplate().getItemId()).addNumber(minutes).addNumber(seconds));
		else
			sendPacket(new SystemMessagePacket(item.getTemplate().getReuseType().getMessages()[0]).addItemName(item.getTemplate().getItemId()).addNumber(seconds));
	}

	public void ask(final ConfirmDlgPacket dlg, final OnAnswerListener listener)
	{
		if(_askDialog != null)
			return;
		final int rnd = Rnd.nextInt();
		_askDialog = new IntObjectPairImpl<>(rnd, listener);
		dlg.setRequestId(rnd);
		sendPacket(dlg);
	}

	public IntObjectPair<OnAnswerListener> getAskListener(final boolean clear)
	{
		if(!clear)
			return _askDialog;
		final IntObjectPair<OnAnswerListener> ask = _askDialog;
		_askDialog = null;
		return ask;
	}

	public boolean hasPrivilege(final Privilege privilege)
	{
		return _clan != null && (getClanPrivileges() & privilege.mask()) == privilege.mask();
	}

	public MatchingRoom getMatchingRoom()
	{
		return _matchingRoom;
	}

	public void setMatchingRoom(final MatchingRoom matchingRoom)
	{
		_matchingRoom = matchingRoom;
		if(matchingRoom == null)
			_matchingRoomWindowOpened = false;
	}

	public boolean isMatchingRoomWindowOpened()
	{
		return _matchingRoomWindowOpened;
	}

	public void setMatchingRoomWindowOpened(final boolean b)
	{
		_matchingRoomWindowOpened = b;
	}

	public void dispelBuffs()
	{
		for(final Abnormal e : getAbnormalList().getEffects())
			if(!e.isOffensive() && !e.getSkill().isNewbie() && e.isCancelable() && !e.getSkill().isPreservedOnDeath() && !isSpecialEffect(e.getSkill()))
			{
				sendPacket(new SystemMessagePacket(SystemMsg.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED).addSkillName(e.getSkill().getId(), e.getSkill().getLevel()));
				e.exit();
			}
		for(final Servitor servitor : getServitors())
			for(final Abnormal e2 : servitor.getAbnormalList().getEffects())
				if(!e2.isOffensive() && !e2.getSkill().isNewbie() && e2.isCancelable() && !e2.getSkill().isPreservedOnDeath() && !servitor.isSpecialEffect(e2.getSkill()))
					e2.exit();
	}

	public void dispelDebuffs()
	{
		getAbnormalList().getEffects().stream()
				.filter(Abnormal::isCancelable)
				.forEach(effect ->
				{
					Skill effectSkill = effect.getSkill();
					if(effectSkill == null)
						return;
					if(!effect.isOffensive())
						return;
					if(effectSkill.isToggle())
						return;
					if(effectSkill.isPassive())
						return;
					if(isSpecialEffect(effectSkill))
						return;
					if(effectSkill.getMagicLevel() <= 0)
						return;
					effect.exit();
					sendPacket(new SystemMessagePacket(SystemMsg.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED).addSkillName(effectSkill));
				});
	}

	public void setInstanceReuse(final int id, final long time)
	{
		final SystemMessage msg = new SystemMessage(2720).addString(getName());
		sendPacket(msg);
		_instancesReuses.put(id, time);
		mysql.set("REPLACE INTO character_instances (obj_id, id, reuse) VALUES (?,?,?)", getObjectId(), id, time);
	}

	public void removeInstanceReuse(final int id)
	{
		if(_instancesReuses.remove(id) != null)
			mysql.set("DELETE FROM `character_instances` WHERE `obj_id`=? AND `id`=? LIMIT 1", getObjectId(), id);
	}

	public void removeAllInstanceReuses()
	{
		_instancesReuses.clear();
		mysql.set("DELETE FROM `character_instances` WHERE `obj_id`=?", getObjectId());
	}

	public void removeInstanceReusesByGroupId(final int groupId)
	{
		for(final int i : InstantZoneHolder.getInstance().getSharedReuseInstanceIdsByGroup(groupId))
			if(getInstanceReuse(i) != null)
				removeInstanceReuse(i);
	}

	public Long getInstanceReuse(final int id)
	{
		return _instancesReuses.get(id);
	}

	public Map<Integer, Long> getInstanceReuses()
	{
		return _instancesReuses;
	}

	public void loadInstanceReuses()
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
				final int id = rs.getInt("id");
				final long reuse = rs.getLong("reuse");
				_instancesReuses.put(id, reuse);
			}
		}
		catch(Exception e)
		{
			Player._log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, offline, rs);
		}
	}

	public Optional<Reflection> getActiveReflection()
	{
		return ReflectionManager.getInstance().getAll().valueCollection().stream()
				.filter(r -> r != null && r.isVisitor(this))
				.findFirst();
	}

	public boolean canEnterInstance(final int instancedZoneId)
	{
		final InstantZone iz = InstantZoneHolder.getInstance().getInstantZone(instancedZoneId);
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

	public boolean canReenterInstance(final int instancedZoneId)
	{
		Optional<Reflection> activeReflection = getActiveReflection();
		if(activeReflection.isPresent() && activeReflection.get().getInstancedZoneId() != instancedZoneId || !getReflection().isMain())
		{
			sendPacket(SystemMsg.YOU_HAVE_ENTERED_ANOTHER_INSTANCE_ZONE_THEREFORE_YOU_CANNOT_ENTER_CORRESPONDING_DUNGEON);
			return false;
		}
		final InstantZone iz = InstantZoneHolder.getInstance().getInstantZone(instancedZoneId);
		if(iz.isDispelBuffs())
			dispelBuffs();
		return iz.getEntryType(this).canReEnter(this, iz);
	}

	public int getBattlefieldChatId()
	{
		return _battlefieldChatId;
	}

	public void setBattlefieldChatId(final int battlefieldChatId)
	{
		_battlefieldChatId = battlefieldChatId;
	}

	@Override
	public void broadCast(final IBroadcastPacket... packet)
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
		if(getParty() == null)
			return this;
		if(getParty().getCommandChannel() != null)
			return getParty().getCommandChannel();
		return getParty();
	}

	public boolean isActionBlocked(final String action)
	{
		return _blockedActions.contains(action);
	}

	public void blockActions(final String... actions)
	{
		Collections.addAll(_blockedActions, actions);
	}

	public void unblockActions(final String... actions)
	{
		for(final String action : actions)
			_blockedActions.remove(action);
	}

	public void addRadar(final int x, final int y, final int z)
	{
		sendPacket(new RadarControlPacket(0, 1, x, y, z));
	}

	public void addRadarWithMap(final int x, final int y, final int z)
	{
		sendPacket(new RadarControlPacket(0, 2, x, y, z));
	}

	public void removeRadar()
	{
		sendPacket(new RadarControlPacket(2, 2, 0, 0, 0));
	}

	public PetitionMainGroup getPetitionGroup()
	{
		return _petitionGroup;
	}

	public void setPetitionGroup(final PetitionMainGroup petitionGroup)
	{
		_petitionGroup = petitionGroup;
	}

	public int getLectureMark()
	{
		return _lectureMark;
	}

	public void setLectureMark(final int lectureMark)
	{
		_lectureMark = lectureMark;
	}

	public boolean isUserRelationActive()
	{
		return _enableRelationTask == null;
	}

	public void startEnableUserRelationTask(final long time, final SiegeEvent<?, ?> siegeEvent)
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

	public int[] getRecentProductList()
	{
		if(_recentProductList == null)
		{
			final String value = getVar(RECENT_PRODUCT_LIST_VAR);
			if(value == null)
				return null;
			final String[] products_str = value.split(";");
			int[] result = new int[0];
			for(int i = 0; i < products_str.length; ++i)
			{
				final int productId = Integer.parseInt(products_str[i]);
				if(ProductDataHolder.getInstance().getProduct(productId) != null)
					result = ArrayUtils.add(result, productId);
			}
			_recentProductList = result;
		}
		return _recentProductList;
	}

	public void updateRecentProductList(final int productId)
	{
		if(_recentProductList == null)
			(_recentProductList = new int[1])[0] = productId;
		else
		{
			int[] newProductList = { productId };
			for(int i = 0; i < _recentProductList.length && newProductList.length < Config.IM_MAX_ITEMS_IN_RECENT_LIST; ++i)
			{
				final int itemId = _recentProductList[i];
				if(!ArrayUtils.contains(newProductList, itemId))
					newProductList = ArrayUtils.add(newProductList, itemId);
			}
			_recentProductList = newProductList;
		}
		String valueToUpdate = "";
		for(final int itemId2 : _recentProductList)
			valueToUpdate = valueToUpdate + itemId2 + ";";
		setVar("recentProductList", valueToUpdate, -1L);
	}

	@Override
	public int getINT()
	{
		return Math.max(getTemplate().getMinINT(), Math.min(getTemplate().getMaxINT(), super.getINT()));
	}

	@Override
	public int getSTR()
	{
		return Math.max(getTemplate().getMinSTR(), Math.min(getTemplate().getMaxSTR(), super.getSTR()));
	}

	@Override
	public int getCON()
	{
		return Math.max(getTemplate().getMinCON(), Math.min(getTemplate().getMaxCON(), super.getCON()));
	}

	@Override
	public int getMEN()
	{
		return Math.max(getTemplate().getMinMEN(), Math.min(getTemplate().getMaxMEN(), super.getMEN()));
	}

	@Override
	public int getDEX()
	{
		return Math.max(getTemplate().getMinDEX(), Math.min(getTemplate().getMaxDEX(), super.getDEX()));
	}

	@Override
	public int getWIT()
	{
		return Math.max(getTemplate().getMinWIT(), Math.min(getTemplate().getMaxWIT(), super.getWIT()));
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

	public void setNpcDialogEndTime(final int val)
	{
		_npcDialogEndTime = val;
	}

	@Override
	public boolean useItem(final ItemInstance item, final boolean ctrlPressed, final boolean force)
	{
		if(item == null)
			return false;
		final ItemTemplate template = item.getTemplate();
		final IItemHandler handler = template.getHandler();
		if(handler == null)
			return false;
		final boolean success = force ? handler.forceUseItem(this, item, ctrlPressed) : handler.useItem(this, item, ctrlPressed);
		if(success)
		{
			if (tScheme_record.isLogging())
			tScheme_record.setUseItem(new PUseItem(item.getItemId())); 
			
			final long nextTimeUse = template.getReuseType().next(item);
			if(nextTimeUse > System.currentTimeMillis())
			{
				final TimeStamp timeStamp = new TimeStamp(item.getItemId(), nextTimeUse, template.getReuseDelay());
				addSharedGroupReuse(template.getReuseGroup(), timeStamp);
				if(template.getReuseDelay() > 0)
					sendPacket(new ExUseSharedGroupItem(template.getDisplayReuseGroup(), timeStamp));
			}
		}
		return success;
	}

	public int getSkillsElementID()
	{
		return (int) calcStat(Stats.SKILLS_ELEMENT_ID, -1.0, null, null);
	}

	public int getAvailableSummonPoints()
	{
		int usedSummonPoints = 0;
		for(final SummonInstance summon : getSummons())
			usedSummonPoints += summon.getSummonPoints();
		return getMaxSummonPoints() - usedSummonPoints;
	}

	public int getMaxSummonPoints()
	{
		return (int) calcStat(Stats.SUMMON_POINTS, 0.0, null, null);
	}

	public int getUsedSummonPoints()
	{
		return getMaxSummonPoints() - getAvailableSummonPoints();
	}

	public Location getStablePoint()
	{
		return _stablePoint;
	}

	public void setStablePoint(final Location point)
	{
		_stablePoint = point;
	}

	public boolean isInSameParty(final Player target)
	{
		return getParty() != null && target.getParty() != null && getParty() == target.getParty();
	}

	public boolean isInSameChannel(final Player target)
	{
		final Party activeCharP = getParty();
		final Party targetP = target.getParty();
		if(activeCharP != null && targetP != null)
		{
			final CommandChannel chan = activeCharP.getCommandChannel();
			if(chan != null && chan == targetP.getCommandChannel())
				return true;
		}
		return false;
	}

	public boolean isInSameClan(final Player target)
	{
		return getClanId() != 0 && getClanId() == target.getClanId();
	}

	public boolean isInSameClan(int clanId)
	{
		return getClanId() != 0 && getClanId() == clanId;
	}

	public final boolean isInSameAlly(final Player target)
	{
		return getAllyId() != 0 && getAllyId() == target.getAllyId();
	}

	public boolean isRelatedTo(final Creature character)
	{
		if(character == this)
			return true;
		if(character.isServitor())
		{
			if(isMyServitor(character.getObjectId()))
				return true;
			if(character.getPlayer() != null)
			{
				final Player Spc = character.getPlayer();
				if(isInSameParty(Spc) || isInSameChannel(Spc) || isInSameClan(Spc) || isInSameAlly(Spc))
					return true;
			}
		}
		else if(character.isPlayer())
		{
			final Player pc = character.getPlayer();
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

	public void disablePartySearch(final boolean disableFlag)
	{
		if(_autoSearchParty)
		{
			PartySubstituteManager.getInstance().removeWaitingPlayer(this);
			sendPacket(ExWaitWaitingSubStituteInfo.CLOSE);
			_autoSearchParty = !disableFlag;
		}
	}

	public boolean refreshPartySearchStatus(final boolean sendMsg)
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

	public boolean mayPartySearch(final boolean first, final boolean msg)
	{
		if(getParty() != null)
			return false;

		if(isPK())
		{
			if(msg)
				if(first)
					sendPacket(SystemMsg.WAITING_LIST_REGISTRATION_IS_NOT_ALLOWED_WHILE_THE_CURSED_SWORD_IS_BEING_USED_OR_THE_STATUS_IS_IN_A_CHAOTIC_STATE);
				else
					sendPacket(SystemMsg.WAITING_LIST_REGISTRATION_IS_CANCELLED_BECAUSE_THE_CURSED_SWORD_IS_BEING_USED_OR_THE_STATUS_IS_IN_A_CHAOTIC_STATE);
			return false;
		}

		if(isInDuel() && getTeam() != TeamType.NONE)
		{
			if(msg)
				if(first)
					sendPacket(SystemMsg.YOU_CANNOT_REGISTER_IN_THE_WAITING_LIST_DURING_A_DUEL);
				else
					sendPacket(SystemMsg.WAITING_LIST_REGISTRATION_IS_CANCELLED_BECAUSE_YOU_ARE_IN_A_DUEL);
			return false;
		}
		if(isInOlympiadMode())
		{
			if(msg)
				if(first)
					sendPacket(SystemMsg.YOU_CANNOT_REGISTER_IN_THE_WAITING_LIST_WHILE_PARTICIPATING_IN_OLYMPIAD);
				else
					sendPacket(SystemMsg.WAITING_LIST_REGISTRATION_IS_CANCELLED_BECAUSE_YOU_ARE_CURRENTLY_PARTICIPATING_IN_OLYMPIAD);
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
		return !isInZone(Zone.ZoneType.no_escape) && !isInZone(Zone.ZoneType.epic) && Config.ENABLE_PARTY_SEARCH;
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
	public int getSkillLevel(final int skillId)
	{
		switch(skillId)
		{
			case 1566:
			case 1567:
			case 1568:
			case 1569:
			case 17192:
			{
				return 1;
			}
			default:
			{
				return super.getSkillLevel(skillId);
			}
		}
	}

	public SymbolInstance getSymbol()
	{
		return _symbol;
	}

	public void setSymbol(final SymbolInstance symbol)
	{
		_symbol = symbol;
	}

	public void setRegisteredInEvent(final boolean inEvent)
	{
		_registeredInEvent = inEvent;
	}

	public boolean isRegisteredInEvent()
	{
		return _registeredInEvent;
	}

	private boolean checkActiveToggleEffects()
	{
		for(final Abnormal effect : getAbnormalList().getEffects())
		{
			final Skill skill = effect.getSkill();
			if(skill == null)
				continue;
			if(!skill.isToggle())
				continue;
			if(getAllSkills().contains(skill))
				continue;
			effect.exit();
		}
		final boolean dispelled = false;
		return dispelled;
	}

	@Override
	public Servitor getServitorForTransfereDamage(final double transferDamage)
	{
		for(final Servitor servitor : getSummons())
			if(servitor != null && !servitor.isDead())
			{
				if(servitor.getCurrentHp() < transferDamage)
					continue;
				if(servitor.isInRangeZ(this, 1200L))
					return servitor;
				continue;
			}
		return null;
	}

	@Override
	public double getDamageForTransferToServitor(final double damage)
	{
		final double transferToSummonDam = calcStat(Stats.TRANSFER_TO_SUMMON_DAMAGE_PERCENT, 0.0);
		if(transferToSummonDam > 0.0)
			return damage * transferToSummonDam * 0.01;
		return 0.0;
	}

	public boolean canFixedRessurect()
	{
		if(getPlayerAccess().ResurectFixed)
			return true;
		if(!isInSiegeZone())
		{
			if(getInventory().getCountOf(10649) > 0L)
				return true;
			if(getInventory().getCountOf(13300) > 0L)
				return true;
		}

		return GveZoneManager.getInstance().getClosestRespawnLoc(this) != null;
	}

	@Override
	public double getLevelBonus()
	{
		if(getTransform() != null && getTransform().getLevelBonus(getLevel()) > 0.0)
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

	public final String getVisibleName(final Player receiver)
	{
		for(final Event event : getEvents())
		{
			final String name = event.getVisibleName(this, receiver);
			if(name != null)
				return name;
		}
		return getName();
	}

	public final String getVisibleTitle(final Player receiver)
	{
		if(getPrivateStoreType() != 0)
		{
			if(getReflection() == ReflectionManager.GIRAN_HARBOR)
				return "";
			if(getReflection() == ReflectionManager.PARNASSUS)
				return "";
		}
		if(!isInAwayingMode())
		{
			for(final Event event : getEvents())
			{
				final String title = event.getVisibleTitle(this, receiver);
				if(title != null)
					return title;
			}
			return getTitle();
		}
		final String awayText = AwayManager.getInstance().getAwayText(this);
		if(awayText == null || awayText.length() <= 1)
			return isLangRus() ? "<\u041e\u0442\u043e\u0448\u0435\u043b>" : "<Away>";
		return (isLangRus() ? "<\u041e\u0442\u043e\u0448\u0435\u043b>" : "<Away>") + " - " + awayText + "*";
	}

	public final int getVisibleNameColor(final Player receiver)
	{
		for(final Event event : getEvents())
		{
			final Integer color = event.getVisibleNameColor(this, receiver);
			if(color != null)
				return color;
		}
		return getNameColor();
	}

	public final int getVisibleTitleColor(final Player receiver)
	{
		if(isInAwayingMode())
			return Config.AWAY_TITLE_COLOR;
		for(final Event event : getEvents())
		{
			final Integer color = event.getVisibleTitleColor(this, receiver);
			if(color != null)
				return color;
		}
		return getTitleColor();
	}

	public final boolean isPledgeVisible(final Player receiver)
	{
		if(getPrivateStoreType() != 0)
		{
			if(getReflection() == ReflectionManager.GIRAN_HARBOR)
				return false;
			if(getReflection() == ReflectionManager.PARNASSUS)
				return false;
		}
		for(final Event event : getEvents())
			if(!event.isPledgeVisible(this, receiver))
				return false;
		return true;
	}

	public double getEnchantChanceModifier()
	{
		return calcStat(Stats.ENCHANT_CHANCE_MODIFIER);
	}

	@Override
	public boolean isSpecialEffect(final Skill skill)
	{
		if(getClan() != null && getClan().isSpecialEffect(skill))
			return true;
		final int skillId = skill.getId();
		return skillId == 7008 || skillId == 6038 || skillId == 6039 || skillId == 6040 || skillId == 6055 || skillId == 6056 || skillId == 6057 || skillId == 6058 || skillId == 35018 || skillId == 35020;
	}

	@Override
	public void removeAllSkills()
	{
		_dontRewardSkills = true;
		super.removeAllSkills();
		_dontRewardSkills = false;
	}

	public void setLastMultisellBuyTime(final long val)
	{
		_lastMultisellBuyTime = val;
	}

	public long getLastMultisellBuyTime()
	{
		return _lastMultisellBuyTime;
	}

	public void setLastEnchantItemTime(final long val)
	{
		_lastEnchantItemTime = val;
	}

	public long getLastEnchantItemTime()
	{
		return _lastEnchantItemTime;
	}

	public void setLastAttributeItemTime(final long val)
	{
		_lastAttributeItemTime = val;
	}

	public long getLastAttributeItemTime()
	{
		return _lastAttributeItemTime;
	}

	public void checkLevelUpReward(final boolean onRestore)
	{
		final int lastRewarded = getVarInt(LVL_UP_REWARD_VAR);
		final int lastRewardedByClass = getVarInt("@lvl_up_reward_" + getActiveSubClass().getIndex());
		final int playerLvl = getLevel();
		final TIntLongMap rewardItems = new TIntLongHashMap();
		if(playerLvl > lastRewarded)
		{
			for(int i = playerLvl; i > lastRewarded; --i)
			{
				final TIntLongMap items = LevelUpRewardHolder.getInstance().getRewardData(i);
				if(items != null)
				{
					final TIntLongIterator iterator = items.iterator();
					while(iterator.hasNext())
					{
						iterator.advance();
						rewardItems.put(iterator.key(), rewardItems.get(iterator.key()) + iterator.value());
					}
				}
			}
			setVar(LVL_UP_REWARD_VAR, playerLvl);
		}

		int clanPoints = 0;
		if(playerLvl > lastRewardedByClass)
		{
			clanPoints = IntStream.iterate(playerLvl, i -> i > lastRewardedByClass, i -> i - 1)
					.filter(i -> getClan() != null && getClan().getLevel() >= 3)
					.flatMap(i -> ClanTable.getInstance().getLevelUpReward(i).stream())
					.sum();
			setVar("@lvl_up_reward_" + getActiveSubClass().getIndex(), playerLvl);
		}
		final TIntLongIterator iterator2 = rewardItems.iterator();
		while(iterator2.hasNext())
		{
			iterator2.advance();
			ItemFunctions.addItem(this, iterator2.key(), iterator2.value(), !onRestore);
		}
		if(clanPoints > 0)
			getClan().incReputation(clanPoints, true, "ClanMemberLvlUp");
	}

	public void giveGMSkills()
	{
		if(!isGM())
			return;
		for(final SkillLearn sl : SkillAcquireHolder.getInstance().getAvailableMaxLvlSkills(this, AcquireType.GM))
		{
			final SkillEntry skillEntry = SkillHolder.getInstance().getSkillEntry(sl.getId(), sl.getLevel());
			if(skillEntry == null)
				continue;
			if(getSkillLevel(skillEntry.getId()) >= skillEntry.getLevel())
				continue;
			addSkill(skillEntry, true);
		}
	}

	public void setblockUntilTime(final long time)
	{
		_blockUntilTime = time;
	}

	public long getblockUntilTime()
	{
		return _blockUntilTime;
	}

	public int getWorldChatPoints()
	{
		if(hasPremiumAccount())
			return Math.max(0, Config.WORLD_CHAT_POINTS_PER_DAY_PA - _usedWorldChatPoints);
		return Math.max(0, Config.WORLD_CHAT_POINTS_PER_DAY - _usedWorldChatPoints);
	}

	public int getUsedWorldChatPoints()
	{
		return _usedWorldChatPoints;
	}

	public void setUsedWorldChatPoints(final int value)
	{
		_usedWorldChatPoints = value;
	}

	public int getArmorSetEnchant()
	{
		return _armorSetEnchant;
	}

	public void setArmorSetEnchant(final int value)
	{
		_armorSetEnchant = value;
	}

	public boolean hideHeadAccessories()
	{
		return _hideHeadAccessories;
	}

	public void setHideHeadAccessories(final boolean value)
	{
		_hideHeadAccessories = value;
	}

	@Override
	public HwidHolder getHwidHolder()
	{
		return hwidHolder;
	}

	public boolean isInAwayingMode()
	{
		return _awaying;
	}

	public void setAwayingMode(final boolean awaying)
	{
		_awaying = awaying;
	}

	public double getMPCostDiff(final Skill.SkillMagicType type)
	{
		double value = 0.0;
		switch(type)
		{
			case PHYSIC:
			{
				value = calcStat(Stats.MP_PHYSICAL_SKILL_CONSUME, 10000.0) / 10000.0 * 100.0 - 100.0;
				break;
			}
			case MAGIC:
			{
				value = calcStat(Stats.MP_MAGIC_SKILL_CONSUME, 10000.0) / 10000.0 * 100.0 - 100.0;
				break;
			}
			case MUSIC:
			{
				value = calcStat(Stats.MP_DANCE_SKILL_CONSUME, 10000.0) / 10000.0 * 100.0 - 100.0;
				break;
			}
		}
		return value;
	}

	public int getExpertiseIndex()
	{
		return getSkillLevel(239, 0);
	}

	public OptionDataTemplate addOptionData(final OptionDataTemplate optionData)
	{
		if(optionData == null)
			return null;
		final OptionDataTemplate oldOptionData = _options.get(optionData.getId());
		if(optionData.equals(oldOptionData))
			return oldOptionData;
		_options.put(optionData.getId(), optionData);
		addTriggers(optionData);
		addStatFuncs(optionData.getStatFuncs(optionData));
		for(final SkillEntry skillEntry : optionData.getSkills())
			addSkill(skillEntry);
		return oldOptionData;
	}

	public OptionDataTemplate removeOptionData(final int id)
	{
		final OptionDataTemplate oldOptionData = _options.remove(id);
		if(oldOptionData != null)
		{
			removeTriggers(oldOptionData);
			removeStatsOwner(oldOptionData);
			for(final SkillEntry skillEntry : oldOptionData.getSkills())
				removeSkill(skillEntry);
		}
		return oldOptionData;
	}

	public long getReceivedExp()
	{
		return _receivedExp;
	}

	public void setQuestZoneId(final int id)
	{
		_questZoneId = id;
	}

	public int getQuestZoneId()
	{
		return _questZoneId;
	}

	public boolean isMercenary() {
		return getMercenaryComponent().isMercenary();
	}

	public void enterWorld() {
		getConfrontationComponent().enterWorld();
		getMercenaryComponent().enterWorld();
	}

	public class BroadcastCharInfoTask implements Runnable
	{
		@Override
		public void run()
		{
			broadcastCharInfoImpl(new IUpdateTypeComponent[0]);
			_broadcastCharInfoTask = null;
		}
	}

	private class UserInfoTask implements Runnable
	{
		@Override
		public void run()
		{
			sendUserInfoImpl();
			_userInfoTask = null;
		}
	}

	public void setAggressionTarget(final Creature target)
	{
		if(target == null)
			_aggressionTarget = HardReferences.emptyRef();
		else
			_aggressionTarget = target.getRef();
	}

	public Creature getAggressionTarget()
	{
		return _aggressionTarget.get();
	}

	public boolean isSpamer()
	{
		return _isSpamer;
	}

	public void setSpamer(boolean b, boolean store)
	{
		_isSpamer = b;
		if(store)
			setVar("is_spamer", b);
	}

	public void setClanRewardLoginTime(long time)
	{
		setVar("PledgeBonusOnline", time);
	}

	public long getClanRewardLoginTime()
	{
		return getVarLong("PledgeBonusOnline", 0);
	}

	public boolean startClanRewardLoginTask()
	{
		if(getClan() == null || getClanRewardLoginTime() == -1 || isNewClanMember())
			return false;
		stopClanRewardLoginTask();
		setClanRewardLoginTime(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(30));
		_clanRewardLoginTask = startClanRewardLogin(this, getClanRewardLoginTime());
		return true;
	}

	public Future<?> startClanRewardLogin(Player player, long delay)
	{
		return ThreadPoolManager.getInstance().schedule(() -> {
			ClanRewardManager.getInstance().addLogin(player.getClanId(), player);
		}, delay - System.currentTimeMillis());
	}

	private void stopClanRewardLoginTask()
	{
		if(_clanRewardLoginTask != null)
		{
			_clanRewardLoginTask.cancel(false);
			_clanRewardLoginTask = null;
		}
	}

	private void addClanRewardExp(int exp)
	{
		if(isNewClanMember())
			return;

		ClanRewardManager.getInstance().addExp(getClanId(), exp);
	}

	public boolean isNewClanMember()
	{
		return getVarBoolean("isNewClanMember", false);
	}

	@Override
	public boolean isHero()
	{
		return _hero;
	}

	public void setHero(boolean hero)
	{
		_hero = hero;
		calcHeroReward();
	}

	public void checkAndDeleteHeroWpn()
	{
		if(isHero() || isCustomHero())
			return;

		getInventory().writeLock();
		try
		{
			for(ItemInstance item : getInventory().getItems())
			{
				if(!item.isHeroItem())
					continue;

				getInventory().destroyItem(item);
			}
		}
		finally
		{
			getInventory().writeUnlock();
		}
	}

	public void checkHeroSkills()
	{
		boolean hero = isHero() && isBaseClassActive() || isCustomHero();
		for(SkillLearn sl : SkillAcquireHolder.getInstance().getAvailableMaxLvlSkills(hero ? this : null, AcquireType.HERO))
		{
			SkillEntry skillEntry = SkillHolder.getInstance().getSkillEntry(sl.getId(), sl.getLevel());
			if(skillEntry == null)
				continue;
			if(hero)
			{
				if(getSkillLevel(skillEntry.getId()) >= skillEntry.getLevel())
					continue;
				addSkill(skillEntry, true);
				continue;
			}
			removeSkill(skillEntry, true);
		}
	}

	public void startCustomHeroTask()
	{
		if(!isCustomHero())
			return;

		long endTime = getVarExpireTime("isCustomHero");
		if(endTime != -1)
			customHeroTask = ThreadPoolManager.getInstance().schedule(this::stopCustomHero, endTime - System.currentTimeMillis());
		setHero(true);
		sendItemList(false);
		checkHeroSkills();
		sendSkillList();
		broadcastUserInfo(true);
	}

	public void stopCustomHeroTask()
	{
		if(customHeroTask != null)
		{
			customHeroTask.cancel(false);
			customHeroTask = null;
		}
	}

	public void stopCustomHero()
	{
		setHero(false);
		unsetVar("isCustomHero");
		getInventory().validateItems();
		checkHeroSkills();
		sendSkillList();
		broadcastUserInfo(true);
	}

	public boolean isCustomHero()
	{
		return getVarBoolean("isCustomHero", false);
	}

	public void setCustomHero(int hour)
	{
		stopCustomHeroTask();
		stopCustomHero();
		long endTime = -1;
		if(hour > 0)
			endTime = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(hour);

		if(endTime > 0)
			setVar("isCustomHero", true, endTime);
		else
			setVar("isCustomHero", true);

		startCustomHeroTask();
		broadcastPacket(new SocialActionPacket(getObjectId(), 20016));
	}

	public void setOlympiadSide(int side)
	{
		_olympiadSide = side;
	}

	public int getOlympiadSide()
	{
		return _olympiadSide;
	}

	public void setOlympiadGame(OlympiadGame game)
	{
		_olympiadGame = game;
	}

	public OlympiadGame getOlympiadGame()
	{
		return _olympiadGame;
	}

	public OlympiadGame getOlympiadObserveGame()
	{
		return _olympiadObserveGame;
	}

	private void setOlympiadObserveGame(OlympiadGame game)
	{
		_olympiadObserveGame = game;
	}

	public void setIsInOlympiadMode(boolean b)
	{
		_inOlympiadMode = b;
	}

	public boolean isInOlympiadMode()
	{
		return _inOlympiadMode;
	}

	public void activateHeroSkills(boolean activate)
	{
		for(SkillLearn sl : SkillAcquireHolder.getInstance().getAvailableMaxLvlSkills(null, AcquireType.HERO))
		{
			Skill skill = SkillHolder.getInstance().getSkill(sl.getId(), sl.getLevel());
			if(skill == null)
				continue;

			if(!activate)
			{
				addUnActiveSkill(skill);
				continue;
			}
			removeUnActiveSkill(skill);
		}
	}

	public boolean isOlympiadCompStart()
	{
		return _olympiadGame != null && _olympiadGame.getState() == 2;
	}

	public int getTalismanCount()
	{
		return (int) calcStat(Stats.TALISMANS_LIMIT, 0.0, null, null);
	}

	public boolean isFishing()
	{
		return _fishing.inStarted();
	}

	public Fishing getFishing()
	{
		return _fishing;
	}

	public double getChargedFishshotPower()
	{
		ItemInstance weapon = getActiveWeaponInstance();
		if(weapon != null)
			return weapon.getChargedFishshotPower();
		return 0.0;
	}

	public boolean unChargeFishShot()
	{
		ItemInstance weapon = getActiveWeaponInstance();
		if(weapon == null)
			return false;

		weapon.setChargedFishshotPower(0.0);
		autoShot();
		return true;
	}

	public void setChargedFishshotPower(double val)
	{
		ItemInstance weapon = getActiveWeaponInstance();
		if(weapon != null)
			weapon.setChargedFishshotPower(val);
	}

	public boolean isInDuel()
	{
		return getEvent(DuelEvent.class) != null;
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

	@Override
	public double getColRadius()
	{
		if(isTransformed())
			getTransform().getCollisionRadius();
		else if(isMounted())
		{
			NpcTemplate mountTemplate = NpcHolder.getInstance().getTemplate(getMountNpcId());
			return mountTemplate.getCollisionRadius();
		}
		return super.getColRadius();
	}

	@Override
	public double getColHeight()
	{
		if(isTransformed())
			getTransform().getCollisionHeight();
		else if(isMounted())
		{
			NpcTemplate mountTemplate = NpcHolder.getInstance().getTemplate(getMountNpcId());
			return mountTemplate.getCollisionHeight();
		}
		return super.getColHeight();
	}

	private final SimpleReward _itemReward = new SimpleReward();
	private final SimpleReward _levelReward = new SimpleReward();
	private final SimpleReward _pvpReward = new SimpleReward();
	private final SimpleReward _setReward = new SimpleReward();
	private final SimpleReward _enchantReward = new SimpleReward();
	private final SimpleReward _nobleReward = new SimpleReward();
	private final SimpleReward _heroReward = new SimpleReward();

	public SimpleReward get_itemReward()
	{
		return _itemReward;
	}
	public SimpleReward get_levelReward() {
		return _levelReward;
	}

	public SimpleReward get_pvpReward() {
		return _pvpReward;
	}

	public SimpleReward get_setReward() {
		return _setReward;
	}

	public SimpleReward get_enchantReward() {
		return _enchantReward;
	}

	public SimpleReward get_nobleReward() {
		return _nobleReward;
	}

	public SimpleReward get_heroReward() {
		return _heroReward;
	}
	
	public int getExpReward()
	{
		return _itemReward.getExp() + _levelReward.getExp() + _pvpReward.getExp() + _setReward.getExp() + _enchantReward.getExp() + _nobleReward.getExp() + _heroReward.getExp();
	}

	public int getSpReward()
	{
		return _itemReward.getSp() + _levelReward.getSp() + _pvpReward.getSp() + _setReward.getSp() + _enchantReward.getSp() + _nobleReward.getSp() + _heroReward.getSp();
	}

	public int getAdenaReward()
	{
		return _itemReward.getAdena() + _levelReward.getAdena() + _pvpReward.getAdena() + _setReward.getAdena() + _enchantReward.getAdena() + _nobleReward.getAdena() + _heroReward.getAdena();
	}

	public int getReward(Player target)
	{
		if(!GveRewardManager.getInstance().checkCondition(target, this))
			return 0;

		return getAdenaReward();
	}

	public void calcHeroReward()
	{
		_heroReward.clear();

		SimpleReward r = GveRewardHolder.getInstance().getHeroReward();

		if(r != null && isHero())
			_heroReward.addReward(r);
	}

	public void calcNobleReward()
	{
		_nobleReward.clear();

		SimpleReward r = GveRewardHolder.getInstance().getNobleReward();

		if(r != null && isNoble())
			_nobleReward.addReward(r);
	}

	public void calcEnchantReward()
	{
		_enchantReward.clear();

		for(ItemInstance item : getInventory().getPaperdollItems())
		{
			if(item != null && item.getEnchantLevel() > 0)
			{
				SimpleReward r = GveRewardHolder.getInstance().getEnchantReward(item.getEquipSlot(), item.getGrade(), item.getEnchantLevel());
				if(r != null)
					_enchantReward.addReward(r);
			}
		}
	}

	public void calcSetReward()
	{
		_setReward.clear();

		ItemInstance chest = getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
		if(chest != null)
		{
			List<ArmorSet> sets = ArmorSetsHolder.getInstance().getArmorSets(chest.getItemId());
			if(sets != null)
			{
				for(ArmorSet set : sets)
					if(set.containAll(this))
					{
						SimpleReward r = GveRewardHolder.getInstance().getSetReward(chest.getItemId());
						if(r != null)
							_setReward.addReward(r);
					}
			}
		}
	}

	public void calcPvpReward()
	{
		_pvpReward.clear();

		SimpleReward r = GveRewardHolder.getInstance().getPvpReward(getPvpKills());
		if(r != null)
			_pvpReward.addReward(r);
	}

	public void calcLevelReward()
	{
		_levelReward.clear();

		SimpleReward r = GveRewardHolder.getInstance().getLevelReward(getLevel());
		if(r != null)
			_levelReward.addReward(r);
	}


	
	public void calcItemReward()
	{
		_itemReward.clear();

		for(ItemInstance item : getInventory().getPaperdollItems())
		{
			if(item != null)
			{
				SimpleReward r = GveRewardHolder.getInstance().getItemReward(item);
				if(r != null)
					_itemReward.addReward(r);
			}
		}
	}

	private boolean isNoble()
	{
		// TODO Auto-generated method stub
		return false;
	}

	public DamageList getDamageList()
	{
		return _damageList;
	}

	private final String LOAD_PROFILE = "SELECT profile_name, skills FROM player_buff_profiles WHERE object_id=?";
	private final String DELETE_PROFILES = "DELETE FROM player_buff_profiles WHERE object_id=?";
	private final String INSERT_SCHEME = "INSERT INTO player_buff_profiles (object_id, profile_name, skills) VALUES (?,?,?)";

	private List<BuffProfileHolder> _buffprofiles = Collections.emptyList();

	private void restoreBuffProfiles()
	{
		_buffprofiles.clear();

		Connection con = null;
		ResultSet rs = null;
		PreparedStatement stm = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			stm = con.prepareStatement(LOAD_PROFILE);
			stm.setInt(1, getObjectId());
			rs = stm.executeQuery();

			while(rs.next())
			{
			    if (_buffprofiles.isEmpty())
                    _buffprofiles = new ArrayList<>();

				String schemeName = rs.getString("profile_name");

				List<Integer> skillsList;
				if(!"".equals(rs.getString("skills")))
				{
					String[] skills = rs.getString("skills").split(",");
					skillsList = Arrays.stream(skills).mapToInt(Integer::valueOf).boxed()
							.collect(Collectors.toList());
				}
				else
					skillsList = new ArrayList<>();

				BuffProfileHolder bph = new BuffProfileHolder(schemeName, skillsList);
				bph.setName(schemeName);

				createNewProfile(bph);
			}
		}
		catch(SQLException e)
		{
			_log.error("Buffer: Can't load buf profiles", e);
		}
		finally
		{
			DbUtils.closeQuietly(con);
			DbUtils.closeQuietly(stm);
			DbUtils.closeQuietly(rs);
		}

	}

	private void storeBuffProfiles()
	{
		Connection con = null;
		PreparedStatement st = null;
		try
		{
			if(!_buffprofiles.isEmpty())
			{
				con = DatabaseFactory.getInstance().getConnection();
				st = con.prepareStatement(DELETE_PROFILES);
				st.setInt(1, getObjectId());
				st.execute();
				st.close();

				st = con.prepareStatement(INSERT_SCHEME);

				// Save _schemesTable content.
				for(BuffProfileHolder bp : _buffprofiles)
				{
					String buffs = bp.getBuffs().stream()
							.map(i -> i.toString())
							.collect(Collectors.joining(","));
					st.setInt(1, getObjectId());
					st.setString(2, bp.getName());
					st.setString(3, buffs);
					st.execute();
				}
			}
		}
		catch(SQLException e)
		{
			_log.warn("Buffer: Can't save profiles " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con);
			DbUtils.closeQuietly(st);
		}
	}

	private void createNewProfile(BuffProfileHolder bph)
	{
		for(BuffProfileHolder bp : _buffprofiles)
            if(bp.isDefault(bph.getName()))
                return;

		if(_buffprofiles.isEmpty())
			_buffprofiles = new ArrayList<>();

		_buffprofiles.add(bph);
	}

	public boolean createNewProfile(String name)
	{
		for(BuffProfileHolder bp : _buffprofiles)
            if(bp.isDefault(name))
                return false;

		if(_buffprofiles.isEmpty())
			_buffprofiles = new ArrayList<>();

		_buffprofiles.add(new BuffProfileHolder(name, new ArrayList<>()));

		return true;
	}

	@Nullable
	public BuffProfileHolder getDefaultProfile()
	{
		String defaultProfile = getDefaultProfileName();

		if (defaultProfile == null)
		    return null;

		for(BuffProfileHolder bp : _buffprofiles)
			if(bp.isDefault(defaultProfile))
				return bp;

		return null;
	}

	public void deleteProfile(String name)
	{
		BuffProfileHolder profil = null;

		for(BuffProfileHolder bp : _buffprofiles)
			if(bp.getName().equals(name))
			{
				profil = bp;
				break;
			}

		_buffprofiles.remove(profil);

		if(_buffprofiles.isEmpty())
		{
			createNewProfile("default");
			setDefaultProfile("default");
		}
		else
		{
			setDefaultProfile(_buffprofiles.get(0).getName());
		}
	}

	public List<BuffProfileHolder> getBuffProfiles()
	{
		return _buffprofiles;
	}

	public String getDefaultProfileName()
	{
		for(BuffProfileHolder profil : _buffprofiles)
		{
			if(profil.getName().equals(getVar("defaultPlayerProfile", "default")))
			{
				setDefaultProfile(profil.getName());
				return profil.getName();
			}
		}
		return null;
	}

	public void setDefaultProfile(String pname)
	{
		setVar("defaultPlayerProfile", pname);
	}

	public void manageComboKill()
	{
		_comboKills++;

		String message = Config.GVE_COMBO_KILL_MESSAGES.get(_comboKills);

		if(message != null)
		{
			String text = message.replace("<?winner?>", getName())
					.replace("<?count?>", String.valueOf(_comboKills));
			var packet = AAScreenStringPacketPresets.COMBO_KILL.addOrUpdate(text);

			getAroundCharacters(1500, 500).stream()
					.filter(GameObject::isPlayer)
					.forEach(cha -> cha.sendPacket(packet));

			sendPacket(packet);
		}
	}

	@Override
	protected void triggerCancelEffects(TriggerInfo trigger)
	{
		super.triggerCancelEffects(trigger);

		SkillEntry skillEntry = trigger.getSkill();
		if(skillEntry == null)
			return;

		List<SummonInstance> summons = getSummons();

		for(SummonInstance s : summons)
			s.getAbnormalList().stopEffects(skillEntry.getTemplate());

	}

	public void enableOnUnequipSkill(final Skill skill)
	{
		TimeStamp stamp = _skillReuses.get(skill.getReuseHash());
		if(stamp != null && stamp.isAddedOnEquip())
			_skillReuses.remove(skill.getReuseHash());
	}

	@Override
	public void addEvent(Event event)
	{
		super.addEvent(event);
		for(SummonInstance f : getSummons())
			f.addEvent(event);
	}

	@Override
	public void removeEvent(Event event)
	{
		super.removeEvent(event);
		for(SummonInstance f : getSummons())
			f.removeEvent(event);
	}

	@Override
	public void setTeam(TeamType t)
	{
		super.setTeam(t);
		for(SummonInstance f : getSummons())
			f.setTeam(t);
	}

	public void removeAutoShots(boolean uncharge)
	{
		if(Config.EX_USE_AUTO_SOUL_SHOT)
			return;

		for(IntObjectPair entry : _activeAutoShots.entrySet())
			removeAutoShot(entry.getKey(), false, (SoulShotType) entry.getValue());

		ItemInstance weapon = getActiveWeaponInstance();
		if(uncharge && weapon != null)
		{
			weapon.setChargedSoulshotPower(0.0);
			weapon.setChargedSpiritshotPower(0.0);
			weapon.setChargedFishshotPower(0.0);
		}
	}

	public boolean manuallyAddAutoShot(int itemId, SoulShotType type, boolean save)
	{
		if(addAutoShot(itemId, true, type))
		{
			if(Config.EX_USE_AUTO_SOUL_SHOT)
			{
				if(save)
					setVar("@active_shot_id_" + type.ordinal(), itemId);
				else
					unsetVar("@active_shot_id_" + type.ordinal());
			}
			return true;
		}
		return false;
	}

	public boolean addAutoShot(int itemId, boolean sendMessage, SoulShotType type)
	{
		if(Config.EX_USE_AUTO_SOUL_SHOT)
		{
			for(IntObjectPair<SoulShotType> entry : _activeAutoShots.entrySet())
			{
				if(entry.getValue() != type)
					continue;

				_activeAutoShots.remove(entry.getKey());
			}

			if(type == SoulShotType.SOULSHOT || type == SoulShotType.SPIRITSHOT)
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
			else if((type == SoulShotType.BEAST_SOULSHOT || type == SoulShotType.BEAST_SPIRITSHOT) && getServitorsCount() == 0)
				return false;
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

	public boolean manuallyRemoveAutoShot(int itemId, SoulShotType type, boolean save)
	{
		if(removeAutoShot(itemId, true, type))
		{
			if(Config.EX_USE_AUTO_SOUL_SHOT)
			{
				if(save)
					setVar("@active_shot_id_" + type.ordinal(), -1);
				else
					unsetVar("@active_shot_id_" + type.ordinal());
			}
			return true;
		}
		return false;
	}

	public boolean removeAutoShot(int itemId, boolean sendMessage, SoulShotType type)
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

	public boolean isAutoShot(int itemId)
	{
		return _activeAutoShots.containsKey(itemId);
	}

	public boolean isAutoShot(SoulShotType type)
	{
		return _activeAutoShots.containsValue(type);
	}

	public void autoShot()
	{
		for(IntObjectPair entry : _activeAutoShots.entrySet())
		{
			int shotId = entry.getKey();
			ItemInstance item = getInventory().getItemByItemId(shotId);
			if(item == null)
			{
				removeAutoShot(shotId, false, (SoulShotType) entry.getValue());
				continue;
			}

			useItem(item, false, false);
		}
	}

	public void initActiveAutoShots()
	{
		if(!Config.EX_USE_AUTO_SOUL_SHOT)
			return;

		for(SoulShotType type : SoulShotType.VALUES)
		{
			if(initSavedActiveShot(type))
				continue;
			sendPacket(new ExAutoSoulShot(0, 1, type));
		}
	}

	public boolean initSavedActiveShot(SoulShotType type)
	{
		if(!Config.EX_USE_AUTO_SOUL_SHOT)
			return false;

		int shotId = getVarInt("@active_shot_id_" + type.ordinal(), 0);
		if(shotId > 0)
		{
			IItemHandler handler;
			ItemInstance item = getInventory().getItemByItemId(shotId);
			if(item != null && (handler = item.getTemplate().getHandler()) != null && handler.isAutoUse() && addAutoShot(shotId, true, type))
			{
				sendPacket(new ExAutoSoulShot(shotId, 3, type));
				ItemFunctions.useItem(this, item, false, false);
				return true;
			}
		}
		else if(shotId == -1)
		{
			sendPacket(new ExAutoSoulShot(0, 2, type));
			return true;
		}
		return false;
	}

	public void checkAndDeleteOlympiadItems()
	{
		//int rank = Olympiad.getRank(this);
		//if(rank != 2 && rank != 3)
		//	ItemFunctions.deleteItemsEverywhere(this, 30373);

		if(!isHero())
		{
			ItemFunctions.deleteItemsEverywhere(this, 6842);
			ItemFunctions.deleteItemsEverywhere(this, 30372);
			for(int itemId : ItemTemplate.HERO_WEAPON_IDS)
				ItemFunctions.deleteItemsEverywhere(this, itemId);
		}
	}

	public boolean enterArenaObserverMode(ObservableArena arena)
	{
		Location enterPoint = arena.getObserverEnterPoint(this);
		WorldRegion observerRegion = World.getRegion(enterPoint);

		if(observerRegion == null)
			return false;

		if(!_observerMode.compareAndSet(isInArenaObserverMode() ? 3 : 0, 1))
			return false;

		sendPacket(new TeleportToLocationPacket(this, enterPoint));
		setTarget(null);
		stopMove();
		World.removeObjectsFromPlayer(this);

		if(_observableArena != null)
		{
			_observableArena.removeObserver(_observePoint);
			_observableArena.onChangeObserverArena(this);
			_observePoint.decayMe();
		}
		else
		{
			broadcastCharInfoImpl(new IUpdateTypeComponent[0]);
			arena.onEnterObserverArena(this);
			_observePoint = new ObservePoint(this);
		}

		_observePoint.setLoc(enterPoint);
		_observePoint.setReflection(arena.getReflection());
		_observableArena = arena;

		sendPacket(new ExTeleportToLocationActivate(this, enterPoint));
		return true;
	}

	public boolean isInArenaObserverMode()
	{
		return _observableArena != null;
	}

	public ObservableArena getObservableArena()
	{
		return _observableArena;
	}

	public void changeFraction(Fraction newFraction)
	{
        Fraction oldFraction = getFraction();
        setFraction(newFraction);
		setVar("fraction", newFraction.ordinal());
		broadcastUserInfo(true);
		ArtifactService.getInstance().changeFraction(this, oldFraction, newFraction);
		ConfrontationService.getInstance().changeFraction(this, oldFraction, newFraction);
	}

	public void addTask(String name, Future<?> task){
		final Future<?> oldTask = tasks.put(name, task);
		if(oldTask != null)
			oldTask.cancel(false);
	}

	public void removeTask(String name) {
		final Future<?> future = tasks.remove(name);
		if(future != null)
			future.cancel(false);
	}

	public ConfrontationComponent getConfrontationComponent() {
		return confrontationComponent;
	}

	public void startBroadcastAvailableActivities(long delay)
	{
		stopBroadcastAvailableActivities();
		var task = LazyPrecisionTaskManager.getInstance().startBroadcastAvailableActivitiesTask(this, delay);
		addTask("BroadcastAvailableActivities", task);
	}

	public void stopBroadcastAvailableActivities()
	{
		removeTask("BroadcastAvailableActivities");
	}

	public boolean isIgnoreValidatePosition() {
		return ignoreValidatePosition;
	}

	public void setIgnoreValidatePosition(boolean ignoreValidatePosition) {
		this.ignoreValidatePosition = ignoreValidatePosition;
	}

	public MercenaryComponent getMercenaryComponent() {
		return mercenaryComponent;
	}

	public boolean isNotShowPrivateBuffers() {
		return notShowPrivateBuffers;
	}

	public void setNotShowPrivateBuffers(boolean notShow) {
		notShowPrivateBuffers = notShow;
	}

	public boolean isPrivateBuffer() {
		return privateBuffer.isEnabled();
	}

	public void cancelPrivateBuffer() {
        privateBuffer.cancel();
    }

    public PrivateBuffer getPrivateBuffer() {
        return privateBuffer;
    }

    public PacketThrottler getPacketThrottler() {
        return packetThrottler;
    }

    public void waterFall() {
        int z = GeoEngine.getHeight(getX(), getY(), getZ(), getGeoIndex());
        if (z > getWaterBottomZ() && z < getWaterZ()) {
            setXYZ(getX(), getY(), z);
        }
    }

  	// TODO: 4
  	public static Player restorePhantom(String char_name, int classId, long exp_add)
  	{
  		Player player = null;
  		Connection con = null;
  		Statement statement = null;
  		ResultSet rset = null;

  		try
  		{
  			// Retrieve the L2Player from the characters table of the database
  			con = DatabaseFactory.getInstance().getConnection();
  			statement = con.createStatement();
			int objectId = CharacterDAO.getInstance().getObjectIdByName(char_name);
  			rset = statement.executeQuery("SELECT * FROM `characters` WHERE `obj_Id`=" + objectId + " LIMIT 1");
  			if (rset.next())
  			{				
  				Sex playerSex = Sex.values()[Rnd.get(0, 1)];
  				ClassId cl = ClassId.VALUES[classId];
  				
  				final PlayerTemplate template = PlayerTemplateHolder.getInstance().getPlayerTemplate(cl.getRace(), cl, playerSex);
  				if(template == null)
  					return null;
  				
  				player = new Player(objectId, template, new DefaultHwidHolder(char_name.getBytes()));

				List<SubClass> result = new ArrayList<>();
				SubClass subClass = new SubClass(player);
				subClass.setType(SubClassType.BASE_CLASS);
				subClass.setClassId(classId);
				subClass.setExp(exp_add, false);
				subClass.setSp(1000);
				subClass.setHp(50);
				subClass.setMp(50);
				subClass.setCp(50);
				subClass.setActive(true);
				result.add(subClass);

  				player.getSubClassList().restorePhantom(result);
  				

  				//player.addExpAndSp(exp_add,exp_add);
  				
  		        if( FractionService.getInstance().getFractionPlayersCountPercentage(Fraction.WATER)>=FractionService.getInstance().getFractionPlayersCountPercentage(Fraction.FIRE) )
  		        	player.setFraction(Fraction.FIRE);
  		        else
  		        	player.setFraction(Fraction.WATER);
  		        
  				//player.setFraction(Fraction.VALUES_WITH_NONE[Rnd.get(1,2)]);

  				player.setIsPhantom(true);
  				player.setLogin(Config.PHANTOM_PLAYERS_AKK);
  				player.setName(char_name);
  				
  				player.setFace(Rnd.get(0, 2));
  				
  				if (player.getSex().ordinal() == 0)
  					player.setHairStyle(Rnd.get(0, 4));
  				else
  					player.setHairStyle(Rnd.get(0, 6));

  				player.setHairColor(Rnd.get(0, 3));
  				
  				player.setHeading(0);
  				player.setPvpKills(0);
  				player.setPkKills(0);
  				
  				player.setOnlineTime(0);
  				
  				player.setCreateTime(rset.getLong("createtime")*1000L);
  				player.setDeleteTimer(rset.getInt("deletetime"));
  				player.setUptime(System.currentTimeMillis());
  				player.setLastAccess(rset.getLong("lastAccess"));
  				
  				player.setRecomHave(rset.getInt("rec_have"));
  				player.setRecomLeft(rset.getInt("rec_left"));

  				player.setReflection(0);
  				
  				EventHolder.getInstance().findEvent(player);

  				int BaseClassId = player.getBaseClassId();
  				if (BaseClassId == -1)
  				{
  					throw new Exception("There are no base subclass for phantom: "+player);
  				}
  				
  				player.getInventory().restore();
  				player.setActiveSubClass(player.getActiveClassId(), false, true);
  				player.getAttendanceRewards().restore();
  				
  			}
  			
  		}catch(final Exception e)
  		{
  			_log.error("Could not restore phantom char data! " + classId, e);
  		}finally
  		{
  			DbUtils.closeQuietly(con, statement, rset);
  		}

  		return player;
  	}
  	private ItemInstance phantom_weapon = null;
  	
  	public void setPhantomWeapon(ItemInstance weap)
  	{
  		phantom_weapon = weap;
  	}
  	
  	public ItemInstance getPhantomWeapon()
  	{
  		return phantom_weapon;
  	}

  	public int getArmorType()
  	{
  		ItemInstance item = this.getInventory().getPaperdollItem(10);
  		if (item==null)
  			return 4;
  		if (item.getTemplate().getItemType() == ArmorType.MAGIC)
  			return 3;
  		else
  		if (item.getTemplate().getItemType() == ArmorType.LIGHT)
  			return 2;
  		else
  		if (item.getTemplate().getItemType() == ArmorType.HEAVY)
  			return 1;
  		return 4;
  	}

  	public void sendAdminMessage(final String message)
  	{
  		sendPacket(new SayPacket2(0, ChatType.ALL, "SYS", NpcString.NONE, message));
  	}
  	
	private long _afterTeleportPortectionTime;
	
	public long getAfterTeleportPortectionTime()
	{
		return _afterTeleportPortectionTime;
	}
	
	public void setAfterTeleportPortectionTime(final long afterTeleportPortectionTime)
	{
		_afterTeleportPortectionTime = afterTeleportPortectionTime;
	}
	
	@Override
	protected L2GameServerPacket stopMovePacket()
	{
		if (isInBoat())
		{
			getBoat().inStopMovePacket(this);
		}
		return super.stopMovePacket();
	}
	
	private final AtomicReference <MoveToLocationOffloadData> _mtlOffloadData = new AtomicReference <>(null);
	
	@Override
	protected MoveActionBase createMoveToLocation(final Location dest, int indent, final boolean pathFind)
	{
		boolean ignoreGeo = !Config.ALLOW_GEODATA;
		final Location from = getLoc();
		final Location to = dest.clone();
		if (isInBoat())
		{
			indent += (int) (from.distance(to)-3*getBoat().getActingRange());
			ignoreGeo = true;
		}
		if (Config.MOVE_OFFLOAD_MTL_PC)
		{
			return new MoveToLocationActionForOffload(this, from, to, ignoreGeo, indent, pathFind);
		}
		return new MoveToLocationAction(this, from, to, ignoreGeo, indent, pathFind);
	}
	
	public void moveBackwardToLocationForPacket(final Location loc, final boolean pathfinding)
	{
		if (isMoving() && Config.MOVE_OFFLOAD_MTL_PC)
		{
			_mtlOffloadData.set(new MoveToLocationOffloadData(loc, 0, pathfinding));
			return;
		}
		moveToLocation(loc, 0, pathfinding);
	}
	
	private static class MoveToLocationOffloadData
	{
		private final Location _dest;
		private final int _indent;
		private final boolean _pathfind;
		
		public MoveToLocationOffloadData(final Location dest,final int indent,final boolean pathfind)
		{
			_dest = dest;
			_indent = indent;
			_pathfind = pathfind;
		}
		
		public Location getDest()
		{
			return _dest;
		}
		
		public int getIndent()
		{
			return _indent;
		}
		
		public boolean isPathfind()
		{
			return _pathfind;
		}
	}
	
	private static class MoveToLocationActionForOffload extends MoveToLocationAction
	{
		public MoveToLocationActionForOffload(final Creature actor,final Location moveFrom,final Location moveTo,final boolean ignoreGeo,final int indent,final boolean pathFind)
		{
			super(actor, moveFrom, moveTo, ignoreGeo, indent, pathFind);
		}
		
		private void tryOffloadedMove()
		{
			final Player player = (Player) getActor();
			MoveToLocationOffloadData mtlOffloadData;
			if (player != null && (mtlOffloadData = player._mtlOffloadData.get()) != null && player._mtlOffloadData.compareAndSet(mtlOffloadData, null))
			{
				player.moveToLocation(mtlOffloadData.getDest(), mtlOffloadData.getIndent(), mtlOffloadData.isPathfind());
			}
		}
		
		@Override
		protected boolean onTick(final double done)
		{
			boolean result;
			try
			{
				result = super.onTick(done);
			}finally
			{
				tryOffloadedMove();
			}
			return result;
		}
		
		@Override
		protected void onFinish(final boolean finishedWell, final boolean isInterrupted)
		{
			try
			{
				super.onFinish(finishedWell, isInterrupted);
			}finally
			{
				tryOffloadedMove();
			}
		}
	}
	
}
