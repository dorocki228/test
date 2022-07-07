package l2s.gameserver.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import javax.annotation.ParametersAreNonnullByDefault;
import l2s.gameserver.Announcements;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.cache.player.PlayerCache;
import l2s.gameserver.cache.player.PlayerData;
import l2s.gameserver.dao.CharacterDAO;
import l2s.gameserver.dao.CharacterVariablesDAO;
import l2s.gameserver.dao.FactionLeaderDAO;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.enums.FactionLeaderPrivilegesType;
import l2s.gameserver.enums.FactionLeaderStateType;
import l2s.gameserver.instancemanager.SpawnManager;
import l2s.gameserver.listener.actor.player.OnActiveClassListener;
import l2s.gameserver.listener.actor.player.OnPlayerEnterListener;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.listener.CharListenerList;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.factionleader.FactionLeader;
import l2s.gameserver.model.factionleader.FactionLeaderPrivileges;
import l2s.gameserver.model.factionleader.FactionLeaderRequest;
import l2s.gameserver.model.factionleader.FactionLeaderVote;
import l2s.gameserver.model.factionleader.task.EndCycleTask;
import l2s.gameserver.model.factionleader.task.StateTask;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.time.Interval;
import l2s.gameserver.utils.Functions;
import l2s.gameserver.utils.Language;
import l2s.gameserver.utils.Pagination;
import org.apache.commons.lang3.tuple.Pair;

public class FactionLeaderService {
	private static final FactionLeaderService INSTANCE = new FactionLeaderService();
	private static final LocalTime START_VOTE_LOCAL_TIME = LocalTime.of(14, 0, 0, 0);
	private static final LocalTime START_REQUEST_LOCAL_TIME = LocalTime.of(14, 0, 0, 0);
	private static final LocalTime END_CYCLE_LOCAL_TIME = LocalTime.of(19, 0, 0, 0);
	private static final LocalTime NEXT_CYCLE_LOCAL_TIME = END_CYCLE_LOCAL_TIME;

	private final Map<Fraction, FactionLeader> factionMap;
	private final AtomicInteger cycle = new AtomicInteger(0);
	private volatile FactionLeaderStateType state = FactionLeaderStateType.NONE;
	private long startCycle;
	private long endCycle;
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	private Future<?> endCycleTask;
	private Future<?> stateTask;

	private final LoadingCache<Fraction, List<Pair<Integer, String>>> cache = CacheBuilder.newBuilder().
			expireAfterWrite(Duration.ofHours(2)).
			build(new CacheLoader<>() {
				@Override
				@ParametersAreNonnullByDefault
				public List<Pair<Integer, String>> load(Fraction key) {
					return FactionLeaderDAO.getInstance().selectLeaderNamesFromFaction(key);
				}
			});

	private FactionLeaderService() {
		factionMap = Arrays.stream(Fraction.VALUES_WITH_NONE).collect(Collectors.toUnmodifiableMap(Function.identity(), FactionLeader::new));
		CharListenerList.addGlobal((OnPlayerEnterListener) player -> {
			if(player.getFraction() == Fraction.NONE) {
				return;
			}
			if(isFactionLeader(player)) {
				Announcements.announceToFractionFromStringHolder(player.getFraction(), "faction.leader.s13", player.getName());
			}
			FactionLeader factionLeader = getFactionLeader(player.getFraction());
			if(factionLeader == null) {
				return;
			}

			privilegeProcessing(player, factionLeader);

			if(getState() == FactionLeaderStateType.VOTE) {
				ScheduledFuture<?> task = ThreadPoolManager.getInstance().schedule(
						new VoteTask(player), 60000);
				if(task != null) {
					player.addTask("faction_leader_votestate", task);
				}
			}
			else if(getState() == FactionLeaderStateType.INNINGS_REQUEST) {
				ScheduledFuture<?> task = ThreadPoolManager.getInstance().schedule(
						new RequestTask(player), 60000);
				if(task != null) {
					player.addTask("faction_leader_requeststate", task);
				}
			}
		});
		CharListenerList.addGlobal((OnActiveClassListener) (p0, p1, p2, onRestore) -> {
			if(onRestore) {
				return;
			}
			getInstance().privilegeProcessing(p0);
		});
	}

	public void privilegeProcessing(Player player) {
		FactionLeader factionLeader = getFactionLeader(player.getFraction());
		if(factionLeader == null) {
			return;
		}
		privilegeProcessing(player, factionLeader);
	}

	public void privilegeProcessing(Player player, FactionLeader factionLeader) {
		Optional.ofNullable(factionLeader.getLeaderPrivileges(player.getObjectId())).ifPresent(p -> {
			//Возможно нужно будет сделать распределение..
			if((p.getPrivileges() & FactionLeaderPrivilegesType.FULL.getMask()) == FactionLeaderPrivilegesType.FULL.getMask()) {
				giveSkill(player);
			}
		});
	}

	public static FactionLeaderService getInstance() {
		return INSTANCE;
	}

	public void sendMain(Player player) {
		if(player == null) {
			return;
		}
		HtmlMessage htmlMessage = new HtmlMessage(0).setFile("gve/leader/index.htm");
		htmlMessage.addVar("minEfficiency", Config.LEADER.minPersonalFactionEfficiency());
		htmlMessage.addVar("efficiency", FractionService.getInstance().getPersonalFactionEfficiency(player));
		htmlMessage.addVar("state", getInstance().getState());
		player.sendPacket(htmlMessage);
	}

	public void sendCandidates(Player player, int page) {
		if(player == null) {
			return;
		}
		if(state != FactionLeaderStateType.VOTE) {
			player.sendMessage(new CustomMessage("faction.leader.s11"));
			return;
		}
		Pagination<Pair<Integer, String>> candidates = new Pagination<>(cache.getUnchecked(player.getFraction()), 8);
		candidates.setPage(page);
		HtmlMessage htmlMessage = new HtmlMessage(0).setFile("gve/leader/candidates.vm");
		htmlMessage.addVar("candidates", candidates);
		player.sendPacket(htmlMessage);
	}

	private void giveSkill(Player player) {
		Config.LEADER.leaderSkillSet().stream()
				.map(skills -> SkillHolder.getInstance().getSkillEntry(skills.getId(), skills.getLevel()))
				.filter(Objects::nonNull)
				.forEach(skillEntry -> player.addSkill(skillEntry, false));
	}

	public void restore() {
		if(!Config.LEADER.enabled()) {
			return;
		}
		SpawnManager.getInstance().spawn("faction_vote_manager");
		factionMap.values().forEach(FactionLeader::restore);
		FactionLeaderDAO.getInstance().selectState(this);
		ZonedDateTime current = ZonedDateTime.now(ZoneId.systemDefault());
		if(endCycle == 0 || cycle.get() == 0) {
			newCycle();
		}
		else if(endCycle <= current.toInstant().toEpochMilli()) {
			endCycle();
		}
		else {
			ZonedDateTime startCycleTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(startCycle), ZoneId.systemDefault());
			if(state == FactionLeaderStateType.NONE) {
				ZonedDateTime requestTime = startCycleTime.plusDays(cycle.get() == 1 ? 0 : 1).with(START_REQUEST_LOCAL_TIME);
				stateTask = ThreadPoolManager.getInstance().schedule(new StateTask(this, FactionLeaderStateType.INNINGS_REQUEST),
						Duration.between(current, requestTime).toMillis());
			}
			else if(state == FactionLeaderStateType.INNINGS_REQUEST) {
				ZonedDateTime startVoteTime = startCycleTime.plusDays(cycle.get() == 1 ? 1 : 2).with(START_VOTE_LOCAL_TIME);
				stateTask = ThreadPoolManager.getInstance().schedule(new StateTask(this, FactionLeaderStateType.VOTE),
						Duration.between(ZonedDateTime.now(ZoneId.systemDefault()), startVoteTime).toMillis());
			}
			endCycleTask = ThreadPoolManager.getInstance().schedule(new EndCycleTask(this),
					Duration.between(current, ZonedDateTime.ofInstant(Instant.ofEpochMilli(endCycle), ZoneId.systemDefault())).toMillis());
		}
	}

	private void stopEndCycleTask() {
		if(endCycleTask != null) {
			endCycleTask.cancel(false);
		}
		endCycleTask = null;
	}

	private void stopStateTask() {
		if(stateTask != null) {
			stateTask.cancel(false);
		}
		stateTask = null;
	}

	public void changeState(FactionLeaderStateType nextState) {
		lock.writeLock().lock();
		try {
			state = nextState;
			if(nextState == FactionLeaderStateType.INNINGS_REQUEST) {
				Announcements.announceToAllFromStringHolder("faction.leader.s1");
				GameObjectsStorage.getPlayersStream().forEach(p ->
						p.sendPacket(new HtmlMessage(0).setFile("gve/leader/announce.htm").
								addVar("minEfficiency", Config.LEADER.minPersonalFactionEfficiency()).
								addVar("efficiency", FractionService.getInstance().getPersonalFactionEfficiency(p))));
				ZonedDateTime startCycleTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(startCycle), ZoneId.systemDefault());
				ZonedDateTime startVoteTime = startCycleTime.plusDays(cycle.get() == 1 ? 1 : 2).with(START_VOTE_LOCAL_TIME);
				stateTask = ThreadPoolManager.getInstance().schedule(new StateTask(this, FactionLeaderStateType.VOTE),
						Duration.between(ZonedDateTime.now(ZoneId.systemDefault()), startVoteTime).toMillis());
			}
			else if(nextState == FactionLeaderStateType.VOTE) {
				Announcements.announceToAllFromStringHolder("faction.leader.s2");
				cache.invalidateAll();
			}
			FactionLeaderDAO.getInstance().insertFactionState(this);
		} finally {
			lock.writeLock().unlock();
		}
	}


	private void newCycle() {
		factionMap.values().forEach(p -> {
			p.getLeaderVotes().clear();
			p.getRequestMap().clear();
			p.getLeaderVotes().clear();
		});
		ZonedDateTime current = ZonedDateTime.now(ZoneId.systemDefault());
		ZonedDateTime startCycle = current.with(LocalTime.of(0, 0, 0, 0));
		ZonedDateTime requestTime = startCycle.with(START_REQUEST_LOCAL_TIME);
		if(startCycle.isBefore(current)) {
			startCycle = startCycle.plusDays(1);
			requestTime = requestTime.plusDays(1);
		}
		ZonedDateTime endCycle = startCycle.plusDays(1).with(END_CYCLE_LOCAL_TIME);
		cycle.incrementAndGet();
		setEndCycle(endCycle.toInstant().toEpochMilli());
		setStartCycle(startCycle.toInstant().toEpochMilli());
		stopEndCycleTask();
		stopStateTask();
		endCycleTask = ThreadPoolManager.getInstance().schedule(new EndCycleTask(this), Duration.between(current, endCycle).toMillis());
		stateTask = ThreadPoolManager.getInstance().schedule(new StateTask(this, FactionLeaderStateType.INNINGS_REQUEST), Duration.between(current, requestTime).toMillis());
		FactionLeaderDAO dao = FactionLeaderDAO.getInstance();
		dao.requestForDelete();
		dao.insertFactionState(this);
	}

	private void nextCycle() {
		cycle.incrementAndGet();
		ZonedDateTime current = ZonedDateTime.now(ZoneId.systemDefault());
		ZonedDateTime start = current.with(NEXT_CYCLE_LOCAL_TIME);
		ZonedDateTime requestTime = current.plusDays(1).with(START_REQUEST_LOCAL_TIME);
		if(start.getDayOfYear() != current.getDayOfYear() || start.getYear() != current.getYear()) {
			start = start.plusDays(1);
			requestTime = requestTime.plusDays(1);
		}
		ZonedDateTime endCycle = start.plusDays(2).with(END_CYCLE_LOCAL_TIME);
		stopEndCycleTask();
		stopStateTask();
		setEndCycle(endCycle.toInstant().toEpochMilli());
		setStartCycle(start.toInstant().toEpochMilli());
		endCycleTask = ThreadPoolManager.getInstance().schedule(new EndCycleTask(this), Duration.between(current, endCycle).toMillis());
		stateTask = ThreadPoolManager.getInstance().schedule(new StateTask(this, FactionLeaderStateType.INNINGS_REQUEST), Duration.between(current, requestTime).toMillis());
	}

	public void endCycle() {
		lock.writeLock().lock();
		try {
			state = FactionLeaderStateType.NONE;
			stopStateTask();
			stopEndCycleTask();
			factionMap.values().forEach(p -> {
				p.getPrivilegesMap().values().stream().
						map(pl -> GameObjectsStorage.getPlayer(pl.getObjId())).
						filter(Objects::nonNull).
						forEach(this::removePlayerSkills);
				int leaderObjId = getLeaderFromVote(p);
				p.getPrivilegesMap().clear();
				p.getRequestMap().clear();
				p.getLeaderVotes().clear();
				if(leaderObjId != 0) {
					p.getPrivilegesMap().put(leaderObjId, new FactionLeaderPrivileges(leaderObjId, FactionLeaderPrivilegesType.FULL.getMask()));
					String leaderName = CharacterDAO.getInstance().getNameByObjectId(leaderObjId);
					Announcements.announceToFractionFromStringHolder(p.getFaction(), "faction.leader.s3", leaderName);
					Language language = CharacterVariablesDAO.getInstance().getPlayerLanguageFromObjId(leaderObjId);
					if(!Config.LEADER.leaderItemSet().isEmpty()) {
						String body = language == Language.RUSSIAN ? "Вы выиграли в выборах лидера фракции!" : "You won the election of the faction leader!";
						String title = language == Language.RUSSIAN ? "Выборы лидера" : "Election of the leader";
						Functions.sendSystemMail(leaderObjId, leaderName, title, body, Config.LEADER.leaderItemSet());
					}
				}
			});
			nextCycle();
			FactionLeaderDAO dao = FactionLeaderDAO.getInstance();
			dao.insertFactionState(this);
			dao.requestForDelete();
			factionMap.values().forEach(p -> dao.batchLeaderPrivileges(p.getFaction(), new ArrayList<>(p.getPrivilegesMap().values())));
		} finally {
			lock.writeLock().unlock();
		}
	}

	private int getLeaderFromVote(FactionLeader p) {
		class Counting {
			private int objId;
			private long count;

			private Counting add(long count) {
				this.count += count;
				return this;
			}
		}
		return p.getLeaderVotes().stream().
				collect(Collectors.collectingAndThen(
						Collectors.groupingBy(FactionLeaderVote::getVotedForObjId, Collector.of(
								Counting::new, (a, t) -> {
									a.add(1);
									a.objId = t.getVotedForObjId();
								}, (a, b) -> a.add(b.count), a -> a)),
						map -> map.values().stream().
								max(Comparator.comparingLong(f -> f.count)).
								map(f -> f.objId).
								orElse(0)));
	}

	private void removePlayerSkills(Player player) {
		Config.LEADER.leaderSkillSet()
				.forEach(s -> player.removeSkill(s.getId(), false));
	}

	public void vote(Player voted, int votedForObjId) {
		if(voted == null) {
			return;
		}
		FactionLeader factionLeader = getFactionLeader(voted.getFraction());
		if(factionLeader == null) {
			return;
		}
		if(!factionLeader.isRequest(votedForObjId)) {
			return;
		}
		lock.writeLock().lock();
		try {
			if(state != FactionLeaderStateType.VOTE) {
				voted.sendMessage(new CustomMessage("faction.leader.s11"));
				return;
			}

			if(voted.getLevel() < Config.LEADER.minLevelForVoting()) {
				voted.sendMessage(new CustomMessage("faction.leader.min_level_for_voting")
						.addNumber(Config.LEADER.minLevelForVoting()));
				return;
			}

			boolean voteFromHwid = factionLeader.getLeaderVotes().stream()
					.anyMatch(p -> p.getHwid().equals(voted.getHwidHolder()));
			if(voteFromHwid && Config.LEADER.checkHWID()) {
				voted.sendMessage(new CustomMessage("faction.leader.s4"));
				return;
			}
			boolean voteFromObjId = factionLeader.getLeaderVotes().stream().anyMatch(p -> p.getVotedObjId() == voted.getObjectId());
			if(voteFromObjId) {
				voted.sendMessage(new CustomMessage("faction.leader.s4"));
				return;
			}
			if(voted.getObjectId() == votedForObjId && !Config.LEADER.selfVoteAvailable()) {
				voted.sendMessage(new CustomMessage("faction.leader.s5"));
				return;
			}
			voted.sendMessage(new CustomMessage("faction.leader.s6"));
			FactionLeaderVote e = new FactionLeaderVote(voted.getObjectId(), votedForObjId, voted.getHwidHolder());
			factionLeader.getLeaderVotes().add(e);
			FactionLeaderDAO.getInstance().insertVote(voted.getFraction(), e);
		} finally {
			lock.writeLock().unlock();
		}
	}

	public boolean request(Player player) {
		if(player == null) {
			return false;
		}
		FactionLeader factionLeader = getFactionLeader(player.getFraction());
		if(factionLeader == null) {
			return false;
		}
		int personalFactionEfficiency = FractionService.getInstance().getPersonalFactionEfficiency(player);
		if(personalFactionEfficiency < Config.LEADER.minPersonalFactionEfficiency()) {
			player.sendMessage(new CustomMessage("faction.leader.s8")
					.addNumber(personalFactionEfficiency)
					.addNumber(Config.LEADER.minPersonalFactionEfficiency()));
			return false;
		}
		if(state != FactionLeaderStateType.INNINGS_REQUEST) {
			player.sendMessage(new CustomMessage("faction.leader.s10"));
			return false;
		}
		if(player.isMercenary()) {
			player.sendMessage(new CustomMessage("mercenary.s3"));
			return false;
		}
		lock.writeLock().lock();
		try {
			if(state != FactionLeaderStateType.INNINGS_REQUEST) {
				player.sendMessage(new CustomMessage("faction.leader.s10"));
				return false;
			}
			if(factionLeader.isRequest(player.getObjectId())) {
				player.sendMessage(new CustomMessage("faction.leader.s9"));
				return false;
			}
			FactionLeaderRequest factionLeaderRequest = new FactionLeaderRequest(player.getObjectId(), player.getHwidHolder());
			factionLeader.getRequestMap().put(player.getObjectId(), factionLeaderRequest);
			FactionLeaderDAO.getInstance().insertRequest(player.getFraction(), factionLeaderRequest);
			player.sendMessage(new CustomMessage("faction.leader.s7"));
			return true;
		} finally {
			lock.writeLock().unlock();
		}
	}

	public long getStartCycle() {
		return startCycle;
	}

	public int getCycle() {
		return cycle.get();
	}

	public FactionLeaderStateType getState() {
		return state;
	}

	public long getEndCycle() {
		return endCycle;
	}

	public void setStartCycle(long startCycle) {
		this.startCycle = startCycle;
	}

	public void setCycle(int cycle) {
		this.cycle.set(cycle);
	}

	public void setState(FactionLeaderStateType state) {
		this.state = state;
	}

	public void setEndCycle(long endCycle) {
		this.endCycle = endCycle;
	}

	public FactionLeader getFactionLeader(Fraction faction) {
		return factionMap.get(faction);
	}

	public boolean isFactionLeader(Player player) {
		if(player == null) {
			return false;
		}
		return getFactionLeader(player.getFraction()).getPrivilegesMap().containsKey(player.getObjectId());
	}

	public boolean isFactionLeader(int objId) {
		for(Fraction fraction : Fraction.VALUES_WITH_NONE) {
			FactionLeader factionLeader = getFactionLeader(fraction);
			if(factionLeader == null) {
				continue;
			}
			if(factionLeader.getPrivilegesMap().containsKey(objId)) {
				return true;
			}
		}
		return false;
	}

	public boolean isRequest(Player player) {
		if(player == null) {
			return false;
		}
		return getFactionLeader(player.getFraction()).isRequest(player.getObjectId());
	}

	public boolean isVote(Player player) {
		if(player == null) {
			return false;
		}
		FactionLeader factionLeader = getFactionLeader(player.getFraction());
		if(factionLeader == null) {
			return false;
		}
		lock.readLock().lock();
		try {
			return factionLeader.getLeaderVotes().stream().anyMatch(p -> p.getVotedObjId() == player.getObjectId());
		} finally {
			lock.readLock().unlock();
		}
	}

	public void sendInfo(Player player) {
		FactionLeader factionLeader = factionMap.get(player.getFraction());
		List<String> list = factionLeader.getPrivilegesMap().values().stream().map(p -> {
			PlayerCache playerCache = PlayerCache.getInstance();
			return Optional.ofNullable(playerCache.get(p.getObjId())).map(PlayerData::getName).orElse("No name");
		}).collect(Collectors.toList());
		HtmlMessage message = new HtmlMessage(0);
		message.setFile("gve/leader/info.htm");
		message.addVar("leaders", list);
		player.sendPacket(message);
	}

	public boolean isAllOnlineLeaders() {
		if(!Config.LEADER.enabled()) {
			return false;
		}
		return factionMap.values().stream().flatMap(e -> e.getPrivilegesMap().values().stream()).
				filter(e -> e.getPrivileges() == FactionLeaderPrivilegesType.FULL.getMask()).
				map(e -> GameObjectsStorage.getPlayer(e.getObjId())).
				allMatch(player -> player != null && !player.isLogoutStarted());
	}

	public boolean isLeaderTime() {
		if (!Config.LEADER.enabled()) {
			return false;
		}

		if (!Config.LEADER.availableIntervalsEnabled()) {
			return true;
		}

		final LocalTime now = LocalTime.now();

		for (Interval interval : Config.LEADER.availableIntervals()) {
			final boolean inRange = (now.isAfter(interval.getFrom()) && now.isBefore(interval.getTo()))
				|| now.equals(interval.getFrom())
				|| now.equals(interval.getTo());
			if (inRange) {
				return true;
			}
		}
		return false;
	}

	private class VoteTask implements Runnable {
		private final Player player;

		VoteTask(Player player) {
			this.player = player;
		}

		@Override
		public void run() {
			FactionLeader factionLeader = getFactionLeader(player.getFraction());
			if(factionLeader == null) {
				return;
			}
			if(factionLeader.getFaction() != player.getFraction()) {
				return;
			}
			if(isVote(player)) {
				return;
			}
			sendCandidates(player, 0);
		}
	}

	private class RequestTask implements Runnable {
		private final Player player;

		RequestTask(Player player) {
			this.player = player;
		}

		@Override
		public void run() {
			FactionLeader factionLeader = getFactionLeader(player.getFraction());
			if(factionLeader == null) {
				return;
			}
			if(factionLeader.getFaction() != player.getFraction()) {
				return;
			}
			if(isRequest(player)) {
				return;
			}
			player.sendPacket(new HtmlMessage(0).setFile("gve/leader/announce.htm").
					addVar("minEfficiency", Config.LEADER.minPersonalFactionEfficiency()).
					addVar("efficiency", FractionService.getInstance().getPersonalFactionEfficiency(player)));
		}
	}
}
