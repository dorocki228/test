package l2s.gameserver.service;

import l2s.commons.util.Rnd;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.data.xml.holder.EventHolder;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.instancemanager.SpawnManager;
import l2s.gameserver.listener.actor.player.OnAnswerListener;
import l2s.gameserver.model.*;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.base.SpecialEffectState;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.EventType;
import l2s.gameserver.model.entity.events.impl.SingleMatchEvent;
import l2s.gameserver.model.entity.events.impl.ArenaEvent;
import l2s.gameserver.model.entity.events.impl.arena.ArenaPlayer;
import l2s.gameserver.model.entity.events.impl.arena.ArenaRequest;
import l2s.gameserver.model.entity.events.impl.arena.ArenaTeam;
import l2s.gameserver.model.entity.events.impl.arena.enums.EArenaType;
import l2s.gameserver.model.entity.events.impl.arena.listener.request.ArenaRequestListeners;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.snapshot.SnapshotPlayer;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ChangeWaitTypePacket;
import l2s.gameserver.network.l2.s2c.ConfirmDlgPacket;
import l2s.gameserver.network.l2.s2c.RevivePacket;
import l2s.gameserver.network.l2.s2c.SkillListPacket;
import l2s.gameserver.templates.item.data.ItemData;
import l2s.gameserver.utils.Functions;
import l2s.gameserver.utils.Location;
import org.napile.pair.primitive.IntObjectPair;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * @author mangol
 */
public class ArenaEventService {
	private static final ArenaEventService instance = new ArenaEventService();
	private final AtomicInteger requestGenerator = new AtomicInteger();
	private final ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
	private final Map<Integer, ArenaRequest> arenaRequestMap = new HashMap<>();

	private ReentrantReadWriteLock.WriteLock writeLock() {
		return reentrantReadWriteLock.writeLock();
	}

	private ReentrantReadWriteLock.ReadLock readLock() {
		return reentrantReadWriteLock.readLock();
	}

	public static ArenaEventService getInstance() {
		return instance;
	}

	public void init() {
		ThreadPoolManager.getInstance().scheduleAtFixedDelay(() -> {
			long currentTimeMillis = System.currentTimeMillis();
			long l = TimeUnit.MINUTES.toMillis(15);
			writeLock().lock();
			try {
				List<ArenaRequest> requestList = getRequestList();
				for(ArenaRequest dto : requestList) {
					if(dto.getCreateTimeStamp() + l <= currentTimeMillis) {
						removeRequest(dto.getRequestId(), true);
					}
				}
			} finally {
				writeLock().unlock();
			}
		}, 60000, 60000);
	}

	public List<ArenaRequest> getRequestList() {
		readLock().lock();
		try {
			return new ArrayList<>(arenaRequestMap.values());
		} finally {
			readLock().unlock();
		}
	}

	public int getRequestIdFromPlayer(Player player) {
		if(player == null) {
			return -1;
		}
		readLock().lock();
		try {
			final int objectId = player.getObjectId();
			return getRequestList().stream().filter(e -> e.getArenaParty().getLeaderId() == objectId).findFirst().map(ArenaRequest::getRequestId).orElse(-1);
		} finally {
			readLock().unlock();
		}
	}

	public boolean removeRequestFromPlayer(Player player, boolean returnBid) {
		final int requestIdFromPlayer = getRequestIdFromPlayer(player);
		return removeRequest(requestIdFromPlayer, returnBid) != null;
	}

	public void removeAllRequest(boolean returnBid) {
		writeLock().lock();
		try {
			for(ArenaRequest arenaRequest : getRequestList()) {
				removeRequest(arenaRequest.getRequestId(), returnBid);
			}
		} finally {
			writeLock().unlock();
		}
	}

	public ArenaTeam removeRequest(int requestId, boolean returnBid) {
		writeLock().lock();
		try {
			ArenaRequest requestDto = arenaRequestMap.remove(requestId);
			if(requestDto == null) {
				return null;
			}
			Player leader = requestDto.getArenaParty().getLeader();
			if(leader != null) {
				leader.removeListener(ArenaRequestListeners.getInstance().getPlayerExit());
				Party party = leader.getParty();
				if(party != null) {
					party.removeListener(ArenaRequestListeners.getInstance().getLeavePlayerParty());
				}
			}
			if(returnBid) {
				returnBind(requestDto.getArenaParty());
			}
			return requestDto.getArenaParty();
		} finally {
			writeLock().unlock();
		}
	}

	public void askStartBattle(int requestId, Player player) {
		Event event = EventHolder.getInstance().getEvent(EventType.PVP_EVENT, 1000);
		if(event == null) {
			player.sendMessage(new CustomMessage("arena.s8"));
			return;
		}
		ArenaEvent arenaEvent = (ArenaEvent) event;
		if(arenaEvent.isInProgress()) {
			player.sendMessage(new CustomMessage("arena.s9"));
			return;
		}
		writeLock().lock();
		try {
			if(arenaEvent.isInProgress()) {
				player.sendMessage(new CustomMessage("arena.s9"));
				return;
			}
			ArenaRequest arenaRequestDto = arenaRequestMap.get(requestId);
			if(arenaRequestDto == null) {
				player.sendMessage(new CustomMessage("arena.s10"));
				return;
			}
			Player firstTeamLeader = arenaRequestDto.getArenaParty().getLeader();
			if(firstTeamLeader == null) {
				player.sendMessage(new CustomMessage("arena.s15"));
				return;
			}
			if(isParticipantPlayer(player)) {
				player.sendMessage(new CustomMessage("arena.s27"));
				return;
			}
			if(!checkCreateOrStartBattle(arenaRequestDto.getType(), player)) {
				return;
			}
			Request playerRequestRequestor = player.getRequest();
			if(playerRequestRequestor != null && playerRequestRequestor.isInProgress()) {
				player.sendMessage(new CustomMessage("arena.s31"));
				return;
			}
			Request playerRequestReceiver = firstTeamLeader.getRequest();
			if(playerRequestReceiver != null) {
				if(!playerRequestReceiver.isInProgress()) {
					playerRequestReceiver.cancel();
				}
				else if(playerRequestReceiver.getRequestor() != player) {
					player.sendMessage(new CustomMessage("arena.s32"));
					return;
				}
			}
			IntObjectPair<OnAnswerListener> askListener = firstTeamLeader.getAskListener(false);
			if(askListener != null) {
				player.sendMessage(new CustomMessage("arena.s28"));
				return;
			}
			Request request = new Request(Request.L2RequestType.CUSTOM, player, firstTeamLeader).setTimeout(7000);
			AnswerRequestStartBattle answerRequestStartBattle = new AnswerRequestStartBattle(requestId, player, firstTeamLeader, request);
			String message = new CustomMessage("arena.s30").addString(player.getName()).toString(firstTeamLeader.getLanguage());
			ConfirmDlgPacket packet = new ConfirmDlgPacket(SystemMsg.S1, 7000).addString(message);
			firstTeamLeader.ask(packet, answerRequestStartBattle);
		} finally {
			writeLock().unlock();
		}
	}

	public void requestStartBattle(int requestId, Player player) {
		Event event = EventHolder.getInstance().getEvent(EventType.PVP_EVENT, 1000);
		if(event == null) {
			player.sendMessage(new CustomMessage("arena.s8"));
			return;
		}
		ArenaEvent arenaEvent = (ArenaEvent) event;
		if(arenaEvent.isInProgress()) {
			player.sendMessage(new CustomMessage("arena.s9"));
			return;
		}
		writeLock().lock();
		try {
			if(arenaEvent.isInProgress()) {
				player.sendMessage(new CustomMessage("arena.s9"));
				return;
			}
			ArenaRequest arenaRequestDto = arenaRequestMap.get(requestId);
			if(arenaRequestDto == null) {
				player.sendMessage(new CustomMessage("arena.s10"));
				return;
			}
			Player firstTeamLeader = arenaRequestDto.getArenaParty().getLeader();
			if(firstTeamLeader == null) {
				player.sendMessage(new CustomMessage("arena.s15"));
				return;
			}
			if(isParticipantPlayer(player)) {
				player.sendMessage(new CustomMessage("arena.s27"));
				return;
			}
			if(!checkCreateOrStartBattle(arenaRequestDto.getType(), firstTeamLeader)) {
				player.sendMessage(new CustomMessage("arena.s16"));
				return;
			}
			if(!checkCreateOrStartBattle(arenaRequestDto.getType(), player)) {
				return;
			}
			ArenaTeam firsTeam = arenaRequestDto.getArenaParty();
			if(!player.reduceAdena(firsTeam.getBidCount(), true)) {
				player.sendMessage(new CustomMessage("arena.s11"));
				return;
			}
			TeamType secondaryTeam = firsTeam.getTeamType().revert();
			ArenaTeam secondTeam = new ArenaTeam(secondaryTeam, player.getObjectId(), player.getName());
			secondTeam.addPlayer(player);
			secondTeam.setBidCount(firsTeam.getBidCount());
			if(arenaRequestDto.getType() != EArenaType.ONE_VS_ONE) {
				addPlayersForArenaTeam(secondTeam, player);
				addPlayersForArenaTeam(firsTeam, firstTeamLeader);
			}
			else {
				Player leader = firsTeam.getLeader();
				if(leader != null) {
					leader.leaveParty();
				}
				player.leaveParty();
			}

			removeRequest(arenaRequestDto.getRequestId(), false);
			arenaEvent.startEvent(firsTeam, secondTeam, arenaRequestDto.getType());
		} finally {
			writeLock().unlock();
		}
	}

	private void addPlayersForArenaTeam(ArenaTeam team, Player player) {
		Party party = player.getParty();
		if(party == null) {
			return;
		}
		for(Player partyMember : party.getPartyMembers()) {
			if(partyMember.getObjectId() == team.getLeaderId()) {
				continue;
			}
			team.addPlayer(partyMember);
		}
	}

	public void createBattle(EArenaType type, Player player, int price) {
		if(!checkCreateOrStartBattle(type, player)) {
			return;
		}
		writeLock().lock();
		try {
			if(!checkCreateOrStartBattle(type, player)) {
				return;
			}
			if(isParticipantPlayer(player)) {
				player.sendMessage(new CustomMessage("arena.s23"));
				return;
			}
			if(!player.reduceAdena(price, true)) {
				player.sendMessage(new CustomMessage("arena.s11"));
				return;
			}
			Party party = player.getParty();
			if(party != null) {
				party.addListener(ArenaRequestListeners.getInstance().getLeavePlayerParty());
			}
			player.addListener(ArenaRequestListeners.getInstance().getPlayerExit());

			TeamType teamType = Rnd.get(TeamType.VALUES);
			ArenaTeam arenaParty = new ArenaTeam(teamType, player.getObjectId(), player.getName());
			arenaParty.setBidCount(price);
			arenaParty.addPlayer(player);

			ArenaRequest arenaRequestDto = new ArenaRequest(requestGenerator.incrementAndGet(), type, arenaParty);
			arenaRequestMap.put(arenaRequestDto.getRequestId(), arenaRequestDto);
			player.sendMessage(new CustomMessage("arena.s12"));
		} finally {
			writeLock().unlock();
		}
	}

	private boolean checkCreateOrStartBattle(EArenaType type, Player player) {
		if(player == null) {
			return false;
		}
		Event event = EventHolder.getInstance().getEvent(EventType.PVP_EVENT, 1000);
		if(event == null) {
			player.sendMessage(new CustomMessage("arena.s8"));
			return false;
		}
		ArenaEvent arenaEvent = (ArenaEvent) event;
		if(!arenaEvent.isRegistrationOver()) {
			player.sendMessage(new CustomMessage("arena.s13"));
			return false;
		}
		if(type == EArenaType.ONE_VS_ONE) {
			return isValidPlayer(player, player);
		}
		Party party = player.getParty();
		if(party == null) {
			player.sendMessage(new CustomMessage("arena.s1"));
			return false;
		}
		if(!party.isLeader(player)) {
			player.sendMessage(new CustomMessage("arena.s2"));
			return false;
		}
		int needPartySize = type.getPlayerSize();
		if(needPartySize > party.getMemberCount() || party.getMemberCount() < needPartySize) {
			player.sendMessage(new CustomMessage("arena.s3"));
			return false;
		}
		Player partyLeader = party.getPartyLeader();
		for(Player partyMember : party.getPartyMembers()) {
			if(!isValidPlayer(partyLeader, partyMember)) {
				return false;
			}
		}
		boolean duplicateHwid = party.getPartyMembers().stream().collect(Collectors.collectingAndThen(Collectors.groupingBy(Player::getHwidHolder, Collectors.counting()), e -> e.values().stream().anyMatch(b -> b > 1)));
		if(duplicateHwid) {
			player.sendMessage(new CustomMessage("arena.s14"));
			return false;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	private boolean isValidPlayer(Player leaderParty, Player player) {
		if(Olympiad.isRegistered(player) || Olympiad.isRegisteredInComp(player)) {
			leaderParty.sendMessage(new CustomMessage("arena.s5").addString(player.getName()));
			return false;
		}
		else if(player.isRegisteredInEvent() || player.containsEvent(SingleMatchEvent.class) || PlayerService.getInstance().isPlayerRegisteredEvent(player, ArenaEvent.class)) {
			leaderParty.sendMessage(new CustomMessage("arena.s4").addString(player.getName()));
			return false;
		}
		else if(player.getReflectionId() != ReflectionManager.MAIN.getId()) {
			leaderParty.sendMessage(new CustomMessage("arena.s6").addString(player.getName()));
			return false;
		}
		boolean checkDistance = false;
		List<Spawner> list = SpawnManager.getInstance().getSpawners("arena_npc");
		for(Spawner spawner : list) {
			List<NpcInstance> listNpcs = spawner.getAllSpawned();
			for(NpcInstance npc : listNpcs) {
				if(npc == null) {
					continue;
				}
				if(npc.getLoc().distance(player.getLoc()) <= 500) {
					checkDistance = true;
					break;
				}
			}
		}
		if(!checkDistance) {
			player.sendMessage(new CustomMessage("arena.s7").addString(player.getName()));
			return false;
		}
		if(player.getLevel() < 76) {
			leaderParty.sendMessage(new CustomMessage("arena.s33"));
			return false;
		}
		return true;
	}

	public void returnBind(ArenaTeam arenaTeam) {
		int bidCount = arenaTeam.getBidCount();
		ItemData itemData = new ItemData(57, bidCount);
		Functions.sendSystemMail(arenaTeam.getLeaderId(), arenaTeam.getTeamName(), "Arena - Return bind", "", Collections.singletonList(itemData));
	}

	public boolean isParticipantPlayer(Player player) {
		return getRequestList().stream().map(e -> e.getArenaParty().getLeader()).anyMatch(e -> {
			if(e.getObjectId() == player.getObjectId()) {
				return true;
			}
			Party party = e.getParty();
			if(party == null) {
				return false;
			}
			return party.getPartyMembers().stream().mapToInt(GameObject::getObjectId).anyMatch(p -> p == player.getObjectId());
		});
	}

	public void teleportToBack(ArenaPlayer arenaPlayer) {
		final Player player = arenaPlayer.getPlayer();
		if(player == null) {
			return;
		}
		if(player.isLogoutStarted()) {
			return;
		}
		SnapshotPlayer snapshotPlayer = arenaPlayer.getSnapshotPlayer();
		if(snapshotPlayer != null) {
			player.setTeam(TeamType.NONE);

			player.abortAttack(true, false);
			if(player.isCastingNow()) {
				player.abortCast(true, true);
			}
			if(player.isDead() && !player.isUndying()) {
				player.setCurrentHp(player.getMaxHp(), true);
				player.broadcastPacket(new RevivePacket(player));
			}
			for(Servitor servitor : player.getServitors()) {
				servitor.unSummon(false);
			}
			player.setUndying(SpecialEffectState.FALSE);
			removeFakeDeath(player);
			recoverFromSnapshotEffect(player, snapshotPlayer);
			recoverFromSnapshotCpHpMp(player, snapshotPlayer);
			player.sendPacket(new SkillListPacket(player));
			player.teleToLocation(new Location(11752, 183096, -3552), snapshotPlayer.getReflection());
		}
		player.sendChanges();
	}


	private static void recoverFromSnapshotCpHpMp(Player player, SnapshotPlayer snapshotPlayer) {
		if(player == null || player.isLogoutStarted()) {
			return;
		}
		player.setCurrentCp(snapshotPlayer.getCurrentCp());
		player.setCurrentHp(snapshotPlayer.getCurrentHp(), false);
		player.setCurrentMp(snapshotPlayer.getCurrentMp());
	}

	private static void recoverFromSnapshotEffect(Player player, SnapshotPlayer snapshotPlayer) {
		if(player == null || player.isLogoutStarted()) {
			return;
		}
		for (Abnormal effect : snapshotPlayer.getAbnormals())
		{
			Abnormal e = effect.getTemplate().getEffect(player, player, effect.getSkill());
			if(e == null)
				return;
			
			e.setDuration(effect.getDuration());
			e.setTimeLeft(effect.getTimeLeft());
			player.getAbnormalList().addEffect(e);
		}
	}

	public static void setFakeDeath(Player player) {
		if(player.isLogoutStarted()) {
			return;
		}
		if(!player.isFrozen()) {
			player.startFrozen();
		}
		player.broadcastPacket(new ChangeWaitTypePacket(player, ChangeWaitTypePacket.WT_START_FAKEDEATH));
		player.broadcastCharInfo();
	}

	public static void removeFakeDeath(Player player) {
		if(player.isLogoutStarted()) {
			return;
		}
		if(player.isFrozen()) {
			player.stopFrozen();
		}
		player.broadcastPacket(new ChangeWaitTypePacket(player, ChangeWaitTypePacket.WT_STOP_FAKEDEATH));
		player.broadcastCharInfo();
	}

	public void cpHpMpHeal(Player player) {
		player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
		player.setCurrentCp(player.getMaxCp());
		player.broadcastUserInfo(true);
	}

	private static final class AnswerRequestStartBattle implements OnAnswerListener {
		private final int requestId;
		private final Player requestor;
		private final Player receiver;
		private final Request request;

		private AnswerRequestStartBattle(int requestId, Player requestor, Player receiver, Request request) {
			this.requestId = requestId;
			this.requestor = requestor;
			this.receiver = receiver;
			this.request = request;
		}

		@Override
		public void sayYes() {
			Request playerRequestRequestor = requestor.getRequest();
			if(playerRequestRequestor != null && request != playerRequestRequestor) {
				return;
			}
			Request playerRequestReceiver = receiver.getRequest();
			if(playerRequestReceiver == null || playerRequestReceiver != request) {
				return;
			}
			if(!request.isInProgress()) {
				request.cancel();
				return;
			}
			if(request.getRequestor() != requestor) {
				return;
			}
			ArenaEventService.getInstance().requestStartBattle(requestId, requestor);
		}

		@Override
		public void sayNo() {
			requestor.sendMessage(new CustomMessage("arena.s29"));
			request.cancel();
		}
	}
}
