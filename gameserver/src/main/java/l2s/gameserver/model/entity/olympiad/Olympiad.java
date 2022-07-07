package l2s.gameserver.model.entity.olympiad;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import l2s.commons.configuration.PropertiesParser;
import l2s.gameserver.Config;
import l2s.gameserver.GameServer;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.dao.OlympiadParticipantsDAO;
import l2s.gameserver.instancemanager.OlympiadHistoryManager;
import l2s.gameserver.instancemanager.ServerVariables;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.ObservePoint;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.actor.listener.PlayerListenerList;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.entity.Hero;
import l2s.gameserver.model.entity.events.impl.SingleMatchEvent;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.components.hwid.HwidHolder;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import org.napile.pair.primitive.IntObjectPair;
import org.napile.primitive.maps.IntIntMap;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntIntMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.IntStream;

public class Olympiad
{
	private static final Logger _log = LoggerFactory.getLogger(Olympiad.class);
	private static final IntObjectMap<OlympiadParticipiantData> _participants = new CHashIntObjectMap<>();
	public static final IntIntMap _participantRank = new CHashIntIntMap();
	public static List<Integer> _nonClassBasedRegisters = new CopyOnWriteArrayList<>();
	public static ListMultimap<Integer, Integer> _classBasedRegisters = Multimaps.newListMultimap(
			new ConcurrentHashMap<>(), CopyOnWriteArrayList::new);
	public static Map<Integer, HwidHolder> _playersHWID = new HashMap<>();
	public static List<Integer> _uniqueParticipantsInComp = new CopyOnWriteArrayList<>();
	public static final String OLYMPIAD_HTML_PATH = "olympiad/";
	public static final int[][] BUFFS_LIST = {
			{ 1086, 2 },
			{ 1085, 3 },
			{ 1204, 2 },
			{ 1068, 3 },
			{ 1040, 3 },
			{ 1036, 2 },
			{ 1045, 4 },
			{ 1048, 4 },
			{ 1062, 2 } };

	public static final int[] INSTANCES = { 147, 149, 150 };
	private static long _olympiadPeriodStartTime;
	private static long _validationStartTime;
	private static long _weekStartTime;
	public static int _period;
	public static int _currentCycle;
	private static long _compEnd;
	private static final Calendar _compStart = Calendar.getInstance();
	public static boolean _inCompPeriod;
	public static boolean _isOlympiadEnd;
	private static ScheduledFuture<?> _scheduledOlympiadEnd;
	public static ScheduledFuture<?> _scheduledManagerTask;
	public static ScheduledFuture<?> _scheduledWeeklyTask;
	public static ScheduledFuture<?> _scheduledValdationTask;
	public static ScheduledFuture<?> _scheduledCompStartTask;
	public static ScheduledFuture<?> _scheduledCompEndTask;
	public static final Stadia[] STADIUMS = new Stadia[Config.OLYMPIAD_STADIAS_COUNT];
	public static OlympiadManager _manager;
	private static final List<NpcInstance> _npcs = new ArrayList<>();

	public static void load()
	{
		_participants.clear();

		PropertiesParser olympiadProperties = Config.load("config/olympiad.properties");

		_currentCycle = ServerVariables.getInt("Olympiad_CurrentCycle", olympiadProperties.getProperty("CurrentCycle", 1));
		_period = ServerVariables.getInt("Olympiad_Period", olympiadProperties.getProperty("Period", 0));
		_olympiadPeriodStartTime = ServerVariables.getLong("olympiad_period_start_time", olympiadProperties.getProperty("period_start_time", System.currentTimeMillis()));
		_validationStartTime = ServerVariables.getLong("olympiad_validation_start_time", olympiadProperties.getProperty("validation_start_time", getOlympiadPeriodEndTime()));
		_weekStartTime = ServerVariables.getLong("olympiad_week_start_time", olympiadProperties.getProperty("week_start_time", 0));

		initStadiums();
		OlympiadHistoryManager.getInstance();
		OlympiadParticipantsDAO.getInstance().select();
		OlympiadDatabase.loadParticipantsRank();

		PlayerListenerList.addGlobal(OlympiadParticipantListener.LISTENER);

		switch(_period)
		{
			case 0:
			{
				if(getOlympiadPeriodEndTime() < System.currentTimeMillis())
				{
					OlympiadDatabase.setNewOlympiadStartTime();
					break;
				}
				_isOlympiadEnd = false;
				break;
			}
			case 1:
			{
				_isOlympiadEnd = true;
				_scheduledValdationTask = ThreadPoolManager.getInstance().schedule(new ValidationTask(), getMillisToValidationEnd());
				break;
			}
			default:
			{
				_log.warn("Olympiad System: Omg something went wrong in loading!! Period = " + _period);
				return;
			}
		}

		_log.info("Olympiad System: Loading Olympiad System....");

		if(_period == 0)
			_log.info("Olympiad System: Currently in Olympiad Period");
		else
			_log.info("Olympiad System: Currently in Validation Period");

		_log.info("Olympiad System: Period Ends....");

		long milliToEnd = _period == 0 ? getMillisToOlympiadEnd() : getMillisToValidationEnd();
		double numSecs = milliToEnd / 1000 % 60;
		double countDown = (milliToEnd / 1000 - numSecs) / 60.0;
		int numMins = (int) Math.floor(countDown % 60.0);
		countDown = (countDown - numMins) / 60.0;
		int numHours = (int) Math.floor(countDown % 24.0);
		int numDays = (int) Math.floor((countDown - numHours) / 24.0);

		_log.info("Olympiad System: In " + numDays + " days, " + numHours + " hours and " + numMins + " mins.");

		if(_period == 0)
		{
			_log.info("Olympiad System: Next Weekly Change is in....");

			milliToEnd = getMillisToWeekChange();
			numSecs = milliToEnd / 1000 % 60;
			countDown = (milliToEnd / 1000 - numSecs) / 60.0;
			numMins = (int) Math.floor(countDown % 60.0);
			countDown = (countDown - numMins) / 60.0;
			numHours = (int) Math.floor(countDown % 24.0);
			numDays = (int) Math.floor((countDown - numHours) / 24.0);

			_log.info("Olympiad System: In " + numDays + " days, " + numHours + " hours and " + numMins + " mins.");
		}

		_log.info("Olympiad System: Loaded " + _participants.size() + " participants.");

		if(_period == 0)
			init();
	}

	private static void initStadiums()
	{
		IntStream.range(0, STADIUMS.length)
				.filter(i -> STADIUMS[i] == null)
				.forEach(i -> STADIUMS[i] = new Stadia());
	}

	public static void init()
	{
		if(isValidationPeriod())
			return;

		_compStart.setTimeInMillis(Config.OLYMPIAD_START_TIME.next(System.currentTimeMillis() - Config.ALT_OLY_CPERIOD));
		_compEnd = _compStart.getTimeInMillis() + Config.ALT_OLY_CPERIOD;

		if(_compEnd < System.currentTimeMillis())
		{
			_compStart.setTimeInMillis(Config.OLYMPIAD_START_TIME.next(System.currentTimeMillis()));
			_compEnd = _compStart.getTimeInMillis() + Config.ALT_OLY_CPERIOD;
		}

		startOlympiadEndTask(getMillisToOlympiadEnd());
		updateCompStatus();

		if(_scheduledWeeklyTask != null)
			_scheduledWeeklyTask.cancel(false);
		_scheduledWeeklyTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new WeeklyTask(), getMillisToWeekChange(), Config.ALT_OLY_WPERIOD);
	}

	public static void startOlympiadEndTask(long delay)
	{
		if(_scheduledOlympiadEnd != null)
			_scheduledOlympiadEnd.cancel(false);
		_scheduledOlympiadEnd = ThreadPoolManager.getInstance().schedule(new OlympiadEndTask(), delay);
	}

	public static void startCompStartTask(long delay)
	{
		if(_scheduledCompStartTask != null)
			_scheduledCompStartTask.cancel(false);
		_scheduledCompStartTask = ThreadPoolManager.getInstance().schedule(new CompStartTask(), delay);
	}

	public static void startCompEndTask(long delay)
	{
		if(_scheduledCompEndTask != null)
			_scheduledCompEndTask.cancel(false);
		_scheduledCompEndTask = ThreadPoolManager.getInstance().schedule(new CompEndTask(), delay);
	}

	public static int getCompWeek()
	{
		return _compStart.get(4);
	}

	public static boolean isClassedBattlesAllowed()
	{
		return Config.CLASSED_GAMES_ENABLED && getCompWeek() == 4;
	}

	public static boolean isRegistrationActive()
	{
		if(!_inCompPeriod || _isOlympiadEnd)
			return false;

		if(getMillisToOlympiadEnd() <= 600000)
			return false;

		if(getMillisToCompEnd() <= Config.OLYMPIAD_REGISTRATION_DELAY)
			return false;

		return true;
	}

	public static synchronized boolean registerParticipant(Player player, CompType type)
	{
		if(!isRegistrationActive())
		{
			player.sendPacket(SystemMsg.THE_GRAND_OLYMPIAD_GAMES_ARE_NOT_CURRENTLY_IN_PROGRESS);
			return false;
		}

		if(!validPlayer(player, player, type, false))
			return false;

		if(getParticipantPoints(player.getObjectId()) == 0)
			return false;

		if(player.getOlympiadGame() != null)
			return false;

		switch(type)
		{
			case CLASSED:
			{
				if(!isClassedBattlesAllowed())
					break;
				_classBasedRegisters.put(ClassId.VALUES[getParticipantClass(player.getObjectId())].getType2().ordinal(), player.getObjectId());
				if(Config.ALT_OLY_BY_SAME_BOX_NUMBER > 0)
					_playersHWID.put(player.getObjectId(), player.getNetConnection().getHwidHolder());
				player.sendPacket(SystemMsg.YOU_HAVE_BEEN_REGISTERED_FOR_THE_GRAND_OLYMPIAD_WAITING_LIST_FOR_A_CLASS_SPECIFIC_MATCH);
				break;
			}
			case NON_CLASSED:
			{
				_nonClassBasedRegisters.add(player.getObjectId());
				if(Config.ALT_OLY_BY_SAME_BOX_NUMBER > 0)
					_playersHWID.put(player.getObjectId(), player.getNetConnection().getHwidHolder());
				player.sendPacket(SystemMsg.YOU_ARE_CURRENTLY_REGISTERED_FOR_A_1V1_CLASS_IRRELEVANT_MATCH);
			}
		}

		_uniqueParticipantsInComp.add(player.getObjectId());

		return true;
	}

	public static boolean validPlayer(Player sendPlayer, Player validPlayer, CompType type, boolean gameValidation)
	{
		if(validPlayer.getLevel() < Config.OLYMPIAD_MIN_LEVEL)
			return false;

		if(validPlayer.getClassId().getClassLevel().ordinal() < ClassLevel.SECOND.ordinal())
			return false;

		if(!validPlayer.isBaseClassActive())
		{
			sendPlayer.sendPacket(new SystemMessagePacket(SystemMsg.C1_DOES_NOT_MEET_THE_PARTICIPATION_REQUIREMENTS_SUBCLASS_CHARACTER_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD).addName(validPlayer));
			return false;
		}

		if(validPlayer.isFishing())
		{
			if(validPlayer == sendPlayer)
				sendPlayer.sendPacket(SystemMsg.YOU_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD_WHILE_FISHING);
			return false;
		}

		if(validPlayer.containsEvent(SingleMatchEvent.class))
		{
			if(validPlayer == sendPlayer)
				sendPlayer.sendPacket(SystemMsg.YOU_CANNOT_BE_SIMULTANEOUSLY_REGISTERED_FOR_PVP_MATCHES_SUCH_AS_THE_OLYMPIAD_UNDERGROUND_COLISEUM_AERIAL_CLEFT_KRATEIS_CUBE_AND_HANDYS_BLOCK_CHECKERS);
			return false;
		}

		if(validPlayer.isRegisteredInEvent())
		{
			sendPlayer.sendMessage(new CustomMessage("l2s.gameserver.model.entity.Olympiad.isRegisteredInEvent"));
			return false;
		}

		addParticipant(validPlayer);

		if(!gameValidation)
		{
			int[] ar = getWeekGameCounts(validPlayer.getObjectId());
			switch(type)
			{
				case CLASSED:
				{
					if(_classBasedRegisters.containsValue(validPlayer.getObjectId()))
					{
						sendPlayer.sendPacket(new SystemMessagePacket(SystemMsg.C1_IS_ALREADY_REGISTERED_ON_THE_CLASS_MATCH_WAITING_LIST).addName(validPlayer));
						return false;
					}
					if(ar[1] != 0)
						break;
					validPlayer.sendPacket(SystemMsg.THE_TOTAL_NUMBER_OF_MATCHES_THAT_CAN_BE_ENTERED_IN_1_WEEK_IS_60_CLASS_IRRELEVANT_INDIVIDUAL_MATCHES_30_SPECIFIC_MATCHES_AND_10_TEAM_MATCHES);
					return false;
				}
				case NON_CLASSED:
				{
					if(_nonClassBasedRegisters.contains(validPlayer.getObjectId()))
					{
						sendPlayer.sendPacket(new SystemMessagePacket(SystemMsg.C1_IS_ALREADY_REGISTERED_ON_THE_WAITING_LIST_FOR_THE_CLASS_IRRELEVANT_INDIVIDUAL_MATCH).addName(validPlayer));
						return false;
					}
					if(ar[2] != 0)
						break;
					validPlayer.sendPacket(SystemMsg.THE_TOTAL_NUMBER_OF_MATCHES_THAT_CAN_BE_ENTERED_IN_1_WEEK_IS_60_CLASS_IRRELEVANT_INDIVIDUAL_MATCHES_30_SPECIFIC_MATCHES_AND_10_TEAM_MATCHES);
					return false;
				}
			}

			if(ar[0] == 0)
			{
				validPlayer.sendPacket(SystemMsg.THE_MAXIMUM_MATCHES_YOU_CAN_PARTICIPATE_IN_1_WEEK_IS_70);
				return false;
			}

			if(isRegisteredInComp(validPlayer))
			{
				sendPlayer.sendPacket(new SystemMessagePacket(SystemMsg.C1_IS_ALREADY_REGISTERED_ON_THE_MATCH_WAITING_LIST).addName(validPlayer));
				return false;
			}
		}
		return true;
	}

	public static synchronized void logoutPlayer(Player player)
	{
		removePlayerFromLists(player);

		OlympiadGame game = player.getOlympiadGame();

		if(game != null)
			try
			{
				game.logoutPlayer(player);
				if(!game.validated)
					game.endGame(20, true);
			}
			catch(Exception e)
			{
				_log.error("", e);
			}
	}

	static void removePlayerFromLists(Player player)
	{
		_classBasedRegisters.forEach((key, value) -> {
			if(value == player.getObjectId())
			{
				_classBasedRegisters.remove(key, value);
			}
		});
		_nonClassBasedRegisters.remove(Integer.valueOf(player.getObjectId()));
		_playersHWID.remove(player.getObjectId());
	}

	public static synchronized boolean unregisterParticipant(Player player)
	{
		if(!_inCompPeriod || _isOlympiadEnd)
		{
			player.sendPacket(SystemMsg.THE_GRAND_OLYMPIAD_GAMES_ARE_NOT_CURRENTLY_IN_PROGRESS);
			return false;
		}

		if(!isRegistered(player, true))
		{
			player.sendPacket(SystemMsg.YOU_ARE_NOT_CURRENTLY_REGISTERED_FOR_THE_GRAND_OLYMPIAD);
			return false;
		}

		OlympiadGame game = player.getOlympiadGame();

		if(game != null)
		{
			if(game.getStatus() == BattleStatus.Begin_Countdown)
			{
				player.sendMessage("Now you can't cancel participation in the Grand Olympiad.");
				return false;
			}

			try
			{
				game.logoutPlayer(player);
				if(!game.validated)
					game.endGame(20, true);
			}
			catch(Exception e)
			{
				_log.error("", e);
			}
		}

		removePlayerFromLists(player);

		player.sendPacket(SystemMsg.YOU_HAVE_BEEN_REMOVED_FROM_THE_GRAND_OLYMPIAD_WAITING_LIST);
		return true;
	}

	private static synchronized void updateCompStatus()
	{
		long milliToStart = getMillisToCompBegin();
		double numSecs = milliToStart / 1000 % 60;
		double countDown = (milliToStart / 1000 - numSecs) / 60.0;
		int numMins = (int) Math.floor(countDown % 60.0);
		countDown = (countDown - numMins) / 60.0;
		int numHours = (int) Math.floor(countDown % 24.0);
		int numDays = (int) Math.floor((countDown - numHours) / 24.0);

		_log.info("Olympiad System: Competition Period Starts in " + numDays + " days, " + numHours + " hours and " + numMins + " mins.");
		_log.info("Olympiad System: Event starts/started: " + _compStart.getTime());

		startCompStartTask(getMillisToCompBegin());
	}

	public static void setOlympiadPeriodStartTime(long value)
	{
		_olympiadPeriodStartTime = value;
	}

	public static long getOlympiadPeriodStartTime()
	{
		return _olympiadPeriodStartTime;
	}

	public static long getOlympiadPeriodEndTime()
	{
		return Config.OLYMIAD_END_PERIOD_TIME.next(_olympiadPeriodStartTime);
	}

	private static long getMillisToOlympiadEnd()
	{
		return Math.max(10, getOlympiadPeriodEndTime() - System.currentTimeMillis());
	}

	public static void setValidationStartTime(long value)
	{
		_validationStartTime = value;
	}

	public static long getValidationStartTime()
	{
		return _validationStartTime;
	}

	public static long getValidationEndTime()
	{
		return _validationStartTime + Config.ALT_OLY_VPERIOD;
	}

	static long getMillisToValidationEnd()
	{
		return Math.max(10, getValidationEndTime() - System.currentTimeMillis());
	}

	public static boolean isValidationPeriod()
	{
		return _period == 1;
	}

	public static boolean isOlympiadEnd()
	{
		return _isOlympiadEnd;
	}

	public static boolean inCompPeriod()
	{
		return _inCompPeriod;
	}

	private static long getMillisToCompBegin()
	{
		if(_compStart.getTimeInMillis() < System.currentTimeMillis())
		{
			if(_compEnd > System.currentTimeMillis())
				return 10;
			return setNewCompBegin();
		}
		return _compStart.getTimeInMillis() - System.currentTimeMillis();
	}

	private static long setNewCompBegin()
	{
		_compStart.setTimeInMillis(Config.OLYMPIAD_START_TIME.next(System.currentTimeMillis() - Config.ALT_OLY_CPERIOD));
		_compEnd = _compStart.getTimeInMillis() + Config.ALT_OLY_CPERIOD;

		if(_compEnd < System.currentTimeMillis())
		{
			_compStart.setTimeInMillis(Config.OLYMPIAD_START_TIME.next(System.currentTimeMillis()));
			_compEnd = _compStart.getTimeInMillis() + Config.ALT_OLY_CPERIOD;
		}

		_log.info("Olympiad System: New Schedule @ " + _compStart.getTime());

		return _compStart.getTimeInMillis() - System.currentTimeMillis();
	}

	public static long getMillisToCompEnd()
	{
		return _compEnd - System.currentTimeMillis();
	}

	public static void setWeekStartTime(long value)
	{
		_weekStartTime = value;
	}

	public static long getWeekStartTime()
	{
		return _weekStartTime;
	}

	public static long getWeekEndTime()
	{
		return _weekStartTime + Config.ALT_OLY_WPERIOD;
	}

	private static long getMillisToWeekChange()
	{
		return Math.max(10, getWeekEndTime() - System.currentTimeMillis());
	}

	public static synchronized void doWeekTasks()
	{
		if(isValidationPeriod())
			return;
		for(IntObjectPair<OlympiadParticipiantData> entry : _participants.entrySet())
		{
			OlympiadParticipiantData data = entry.getValue();
			data.setPoints(data.getPoints() + Config.OLYMPIAD_POINTS_WEEKLY);
			data.setClassedGamesCount(0);
			data.setNonClassedGamesCount(0);

			Player player = GameObjectsStorage.getPlayer(entry.getKey());
			if(player == null)
				continue;

			player.sendPacket(new SystemMessagePacket(SystemMsg.C1_HAS_EARNED_S2_POINTS_IN_THE_GRAND_OLYMPIAD_GAMES).addName(player).addNumber(Config.OLYMPIAD_POINTS_WEEKLY));
		}
	}

	public static int getCurrentCycle()
	{
		return _currentCycle;
	}

	public static synchronized void addObserver(int id, Player observer)
	{
		if(observer.getOlympiadGame() != null || isRegistered(observer, false) || isRegisteredInComp(observer))
		{
			observer.sendPacket(SystemMsg.YOU_MAY_NOT_OBSERVE_A_GRAND_OLYMPIAD_GAMES_MATCH_WHILE_YOU_ARE_ON_THE_WAITING_LIST);
			return;
		}

		if(_manager == null || _manager.getOlympiadInstance(id) == null || _manager.getOlympiadInstance(id).getStatus() == BattleStatus.Begining || _manager.getOlympiadInstance(id).getStatus() == BattleStatus.Begin_Countdown)
		{
			observer.sendPacket(SystemMsg.THE_GRAND_OLYMPIAD_GAMES_ARE_NOT_CURRENTLY_IN_PROGRESS);
			return;
		}

		if(observer.isInCombat() || observer.getPvpFlag() > 0 || observer.containsEvent(SingleMatchEvent.class))
		{
			observer.sendPacket(SystemMsg.YOU_CANNOT_OBSERVE_WHILE_YOU_ARE_IN_COMBAT);
			return;
		}

		for(Servitor servitor : observer.getServitors())
			servitor.unSummon(false);

		observer.enterArenaObserverMode(getOlympiadGame(id));
	}

	public static synchronized void removeObserver(int id, ObservePoint observer)
	{
		if(_manager == null || _manager.getOlympiadInstance(id) == null)
			return;
		_manager.getOlympiadInstance(id).removeObserver(observer);
	}

	public static List<ObservePoint> getObservers(int id)
	{
		if(_manager == null || _manager.getOlympiadInstance(id) == null)
			return null;
		return _manager.getOlympiadInstance(id).getObservers();
	}

	public static OlympiadGame getOlympiadGame(int gameId)
	{
		if(_manager == null || gameId < 0)
			return null;
		return _manager.getOlympiadGames().get(gameId);
	}

	public static synchronized int[] getWaitingList()
	{
		if(!inCompPeriod())
			return null;

		int[] array = new int[3];
		array[0] = _classBasedRegisters.values().size();
		array[1] = _nonClassBasedRegisters.size();
		return array;
	}

	public static synchronized int getParticipantRewardCount(Player player, boolean remove)
	{
		int objId = player.getObjectId();
		OlympiadParticipiantData participant = getParticipantInfo(objId);
		if(participant == null)
			return 0;

		int points = participant.getPointsPast();
		if(points == 0)
			return 0;

		int rank = _participantRank.get(objId) - 1;
		switch(rank)
		{
			case 0:
			case 1:
			{
				points = Config.ALT_OLY_RANK1_POINTS;
				break;
			}
			case 2:
			{
				points = Config.ALT_OLY_RANK2_POINTS;
				break;
			}
			case 3:
			{
				points = Config.ALT_OLY_RANK3_POINTS;
				break;
			}
			case 4:
			{
				points = Config.ALT_OLY_RANK4_POINTS;
				break;
			}
			default:
			{
				points = Config.ALT_OLY_RANK5_POINTS;
			}
		}

		if(Hero.getInstance().isInactiveHero(player.getObjectId()) || Hero.getInstance().isHero(player.getObjectId()))
			points += Config.ALT_OLY_HERO_POINTS;

		if(remove)
		{
			participant.setPointsPast(0);
			OlympiadDatabase.saveParticipantData(objId);
		}

		return points * Config.ALT_OLY_GP_PER_POINT;
	}

	public static synchronized int getRank(Player player)
	{
		if(_participantRank.containsKey(player.getObjectId()))
			return _participantRank.get(player.getObjectId());
		return 0;
	}

	public static synchronized boolean isRegistered(Player player)
	{
		return isRegistered(player, false);
	}

	public static synchronized boolean isRegistered(Player player, boolean unregister)
	{
		if(_classBasedRegisters.containsValue(player.getObjectId()))
			return true;

		if(_nonClassBasedRegisters.contains(player.getObjectId()))
			return true;

		if(!GameServer.DEVELOP && Config.ALT_OLY_BY_SAME_BOX_NUMBER > 0 && !unregister)
		{
			GameClient client = player.getNetConnection();
			if(client == null)
				return true;

			HwidHolder playerHWID = client.getHwidHolder();
			if(playerHWID == null || !_playersHWID.containsValue(playerHWID))
				return false;

			int boxesCount = 0;
			for(HwidHolder hwid : _playersHWID.values())
			{
				if(hwid == null || !hwid.equals(playerHWID))
					continue;
				++boxesCount;
			}

			if(boxesCount >= Config.ALT_OLY_BY_SAME_BOX_NUMBER)
			{
				player.sendMessage(new CustomMessage("l2s.gameserver.model.entity.Olympiad.isRegistered.ActiveBoxesLimit").addNumber(Config.ALT_OLY_BY_SAME_BOX_NUMBER));
				return true;
			}
		}

		return false;
	}

	public static synchronized boolean isRegisteredInComp(Player player)
	{
		if(isRegistered(player, false))
			return true;

		if(_manager == null || _manager.getOlympiadGames() == null)
			return false;

		for(OlympiadGame g : _manager.getOlympiadGames().values())
		{
			if(g == null || !g.isRegistered(player.getObjectId()))
				continue;
			return true;
		}
		return false;
	}

	public static synchronized int getParticipantPoints(int objId)
	{
		OlympiadParticipiantData participant = getParticipantInfo(objId);
		if(participant == null)
			return 0;
		return participant.getPoints();
	}

	public static synchronized int getParticipantPointsPast(int objId)
	{
		OlympiadParticipiantData participant = getParticipantInfo(objId);
		if(participant == null)
			return 0;
		return participant.getPointsPast();
	}

	public static synchronized int getCompetitionDone(int objId)
	{
		OlympiadParticipiantData participant = getParticipantInfo(objId);
		if(participant == null)
			return 0;
		return participant.getCompDone();
	}

	public static synchronized int getCompetitionWin(int objId)
	{
		OlympiadParticipiantData participant = getParticipantInfo(objId);
		if(participant == null)
			return 0;
		return participant.getCompWin();
	}

	public static synchronized int getCompetitionLoose(int objId)
	{
		OlympiadParticipiantData participant = getParticipantInfo(objId);
		if(participant == null)
			return 0;
		return participant.getCompLoose();
	}

	public static synchronized int[] getWeekGameCounts(int objId)
	{
		int[] ar = new int[4];
		OlympiadParticipiantData participant = getParticipantInfo(objId);
		if(participant == null)
			return ar;

		ar[0] = Config.GAME_MAX_LIMIT - participant.getClassedGamesCount() - participant.getNonClassedGamesCount();
		ar[1] = Config.GAME_CLASSES_COUNT_LIMIT - participant.getClassedGamesCount();
		ar[2] = Config.GAME_NOCLASSES_COUNT_LIMIT - participant.getNonClassedGamesCount();
		return ar;
	}

	public static Stadia[] getStadiums()
	{
		return STADIUMS;
	}

	public static List<NpcInstance> getNpcs()
	{
		return _npcs;
	}

	public static void addOlympiadNpc(NpcInstance npc)
	{
		_npcs.add(npc);
	}

	public static IntObjectMap<OlympiadParticipiantData> getParticipantsMap()
	{
		return _participants;
	}

	public static OlympiadParticipiantData getParticipantInfo(int objectId)
	{
		return _participants.get(objectId);
	}

	public static void changeParticipantName(int objId, String newName)
	{
		OlympiadParticipiantData participant = getParticipantInfo(objId);
		if(participant == null)
			return;

		participant.setName(newName);
		OlympiadDatabase.saveParticipantData(objId);
	}

	public static String getParticipantName(int objId)
	{
		OlympiadParticipiantData participant = getParticipantInfo(objId);
		if(participant == null)
			return null;

		return participant.getName();
	}

	public static int getParticipantClass(int objId)
	{
		OlympiadParticipiantData participant = getParticipantInfo(objId);
		if(participant == null)
			return 0;

		return participant.getClassId();
	}

	public static void manualSetParticipantPoints(int objId, int points)
	{
		OlympiadParticipiantData participant = getParticipantInfo(objId);
		if(participant == null)
			return;

		participant.setPoints(points);
		OlympiadDatabase.saveParticipantData(objId);
	}

	public static int convertParticipantClassId(int baseClassId)
	{
		ClassId classId = ClassId.VALUES[baseClassId];
		if(classId.getClassLevel().ordinal() < ClassLevel.SECOND.ordinal())
			for(ClassId id : ClassId.VALUES)
			{
				if(!id.isOfLevel(ClassLevel.SECOND) || !id.childOf(classId))
					continue;
				return id.getId();
			}
		return classId.getId();
	}

	public static synchronized void addParticipant(Player participant)
	{
		OlympiadParticipiantData participantData = _participants.get(participant.getObjectId());
		if(participantData == null)
		{
			participantData = new OlympiadParticipiantData(participant.getObjectId(), participant.getName(), participant.getBaseClassId());
			participantData.setPoints(Config.OLYMPIAD_POINTS_DEFAULT);

			_participants.put(participant.getObjectId(), participantData);

			OlympiadDatabase.saveParticipantData(participant.getObjectId());
		}
	}

	public static synchronized void removeParticipant(Player participant)
	{
		if(_participants.remove(participant.getObjectId()) != null)
			OlympiadDatabase.deleteParticipantData(participant.getObjectId());
	}

	public static int getParticipantsCount()
	{
		return _participants.size();
	}

	public static int getUniqueParticipantsInCompCount()
	{
		return _uniqueParticipantsInComp.size();
	}

	public static boolean isEnableCloak()
	{
		return _period == 0;
	}
}
