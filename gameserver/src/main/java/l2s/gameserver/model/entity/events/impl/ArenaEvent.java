package l2s.gameserver.model.entity.events.impl;

import l2s.commons.collections.MultiValueSet;
import l2s.commons.util.Rnd;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.model.*;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.base.SpecialEffectState;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.model.entity.events.EventType;
import l2s.gameserver.model.entity.events.actions.StartStopAction;
import l2s.gameserver.model.entity.events.impl.arena.ArenaPlayer;
import l2s.gameserver.model.entity.events.impl.arena.ArenaTeam;
import l2s.gameserver.model.entity.events.impl.arena.action.EventScreenCustomMessage;
import l2s.gameserver.model.entity.events.impl.arena.enums.EArenaStatusType;
import l2s.gameserver.model.entity.events.impl.arena.enums.EArenaType;
import l2s.gameserver.model.entity.events.impl.arena.listener.ArenaOnDeathFromUndyingListenerImpl;
import l2s.gameserver.model.entity.events.impl.arena.listener.ArenaOnTeleportListenerImpl;
import l2s.gameserver.model.entity.events.impl.arena.listener.ArenaPlayerExitListener;
import l2s.gameserver.model.entity.events.impl.arena.listener.ArenaZoneListener;
import l2s.gameserver.model.entity.events.impl.arena.permission.ArenaAttackTeamPvpZoneImpl;
import l2s.gameserver.model.entity.events.impl.arena.permission.ArenaIncomingAttack;
import l2s.gameserver.model.entity.events.objects.ZoneObject;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.IBroadcastPacket;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.service.ArenaEventService;
import l2s.gameserver.templates.item.data.ItemData;
import l2s.gameserver.templates.item.data.RewardItemData;
import l2s.gameserver.utils.Functions;
import l2s.gameserver.utils.Location;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

/**
 * @author mangol
 */
public class ArenaEvent extends SingleMatchEvent {
	private final Map<TeamType, Location[]> spawnLocationMap = new HashMap<>();
	private final int[] arenaRestrictedItems;
	private final int[] arenaRestrictedSkills;
	private final int[] arenaDispelSkills;
	private final boolean arenaAllowCommunityBoard;
	private final double arenaRewardTax;
	private final List<RewardItemData> rewardItemsData = new ArrayList<>();
	private volatile EArenaStatusType statusType = EArenaStatusType.NONE;
	private final EnumMap<TeamType, ArenaTeam> arenaTeamMap = new EnumMap<>(TeamType.class);
	private final ArenaOnDeathFromUndyingListenerImpl deathFromUndyingListener = new ArenaOnDeathFromUndyingListenerImpl(this);
	private final ArenaOnTeleportListenerImpl arenaOnTeleportListener = new ArenaOnTeleportListenerImpl(this);
	private final ArenaPlayerExitListener arenaPlayerExitListener = new ArenaPlayerExitListener(this);
	private ZoneObject zoneBattle;
	private EArenaType arenaType;

	public ArenaEvent(MultiValueSet<String> set) {
		super(set);
		arenaRestrictedItems = set.getIntegerArray("ArenaRestrictedItems", new int[0], ",");
		arenaRestrictedSkills = set.getIntegerArray("ArenaRestrictedSkills", new int[0], ",");
		arenaDispelSkills = set.getIntegerArray("ArenaDispelSkills", new int[0], ",");
		arenaAllowCommunityBoard = set.getBool("ArenaAllowCommunityBoard", false);
		arenaRewardTax = set.getDouble("ArenaRewardTax", 5.);
		String arenaRewardItem = set.getString("ArenaRewardItem", "");
		if(!arenaRewardItem.isEmpty()) {
			for(String s : arenaRewardItem.split(";")) {
				String[] reward = s.split(",");
				int itemId = Integer.parseInt(reward[0]);
				int count = Integer.parseInt(reward[1]);
				double chance = Double.parseDouble(reward[2]);
				RewardItemData itemData = new RewardItemData(itemId, count, count, chance);
				rewardItemsData.add(itemData);
			}
		}

		Location[] teamLocation1 = {new Location(13141, 183903, -3712),
				new Location(13144, 183768, -3712),
				new Location(13144, 183608, -3712),
				new Location(13144, 183480, -3712)};

		Location[] teamLocation2 = {new Location(11928, 183480, -3712),
				new Location(11944, 183592, -3712),
				new Location(11944, 183784, -3712),
				new Location(11944, 183912, -3712)};
		spawnLocationMap.put(TeamType.BLUE, teamLocation1);
		spawnLocationMap.put(TeamType.RED, teamLocation2);
	}

	public boolean isParticipant(int objId) {
		return arenaTeamMap.values().stream().flatMap(e -> e.getPlayers().stream()).map(ArenaPlayer::getPlayer).filter(Objects::nonNull).anyMatch(e -> {
			if(e.getObjectId() != objId) {
				return e.getServitor(objId) != null;
			}
			return true;
		});
	}

	public ArenaTeam getTeamFromPlayer(int objId) {
		for(ArenaTeam arenaTeam : arenaTeamMap.values()) {
			boolean team = arenaTeam.getPlayers().stream().anyMatch(e -> e.getObjId() == objId);
			if(team) {
				return arenaTeam;
			}
		}
		return null;
	}

	@Override
	public void initEvent() {
		callActions(_onInitActions);

		addOnTimeAction(0, new EventScreenCustomMessage("arena.s17", 30));
		addOnTimeAction(10, new EventScreenCustomMessage("arena.s17", 20));
		addOnTimeAction(20, new EventScreenCustomMessage("arena.s17", 10));
		for(int i = 0; i < 5; i++) {
			addOnTimeAction(25 + i, new EventScreenCustomMessage("arena.s17", 5 - i));
		}

		addOnTimeAction(30, new StartStopAction("teleports", false));
		addOnTimeAction(35, new EventScreenCustomMessage("arena.s18", 30));
		addOnTimeAction(45, new EventScreenCustomMessage("arena.s18", 20));
		addOnTimeAction(55, new EventScreenCustomMessage("arena.s18", 10));
		for(int i = 0; i < 5; i++) {
			addOnTimeAction(60 + i, new EventScreenCustomMessage("arena.s18", 5 - i));
		}
		addOnTimeAction(65, new StartStopAction("battle", false));
		addOnTimeAction(485, new StartStopAction("endMatch", false));

		startRegistration();
		zoneBattle = getFirstObject("zone_battle");
		zoneBattle.getZone().addListener(new ArenaZoneListener(this));
		zoneBattle.getZone().addPermission(new ArenaAttackTeamPvpZoneImpl(zoneBattle.getZone(), this));
		zoneBattle.getZone().addPermission(new ArenaIncomingAttack(this));
		printInfo();
	}

	@Override
	public void reCalcNextTime(boolean init) {
		clearActions();
		registerActions(false);
	}

	private Location getSpawnLocation(TeamType teamType) {
		Location[] locations = spawnLocationMap.get(teamType);
		return Rnd.get(locations);
	}

	private void startRegistration() {
		if(statusType != EArenaStatusType.REGISTRATION) {
			statusType = EArenaStatusType.REGISTRATION;
			arenaTeamMap.clear();
			arenaType = null;
		}
	}

	@Override
	public boolean isRegistrationOver() {
		return statusType != EArenaStatusType.ABORT && statusType != EArenaStatusType.NONE;
	}

	public synchronized void startEvent(ArenaTeam firstTeam, ArenaTeam secondTeam, EArenaType type) {
		if(statusType != EArenaStatusType.REGISTRATION) {
			return;
		}
		arenaType = type;
		statusType = EArenaStatusType.BEGINNING;
		arenaTeamMap.put(firstTeam.getTeamType(), firstTeam);
		arenaTeamMap.put(secondTeam.getTeamType(), secondTeam);
		arenaTeamMap.values().stream().flatMap(e -> e.getPlayers().stream()).map(ArenaPlayer::getPlayer).filter(Objects::nonNull).forEach(r -> r.getPlayer().addEvent(this));
		zoneBattle.setActive(true);
		startEvent();
	}

	@Override
	public boolean isPlayerRegistered(Player player) {
		return ArenaEventService.getInstance().isParticipantPlayer(player);
	}

	@Override
	public boolean isInZoneBattle(int x, int y, int z) {
		if(zoneBattle.getZone().isActive()) {
			return zoneBattle.getZone().checkIfInZone(x, y, z);
		}
		return super.isInZoneBattle(x, y, z);
	}

	@Override
	public void startEvent() {
		clearActions();
		reCalcNextTime(false);
		super.startEvent();
	}

	@Override
	public EventType getType() {
		return EventType.PVP_EVENT;
	}

	public boolean isInBattle() {
		return statusType == EArenaStatusType.BATTLE;
	}

	@Override
	public boolean isInProgress() {
		return statusType != EArenaStatusType.REGISTRATION && statusType != EArenaStatusType.NONE;
	}

	@Override
	protected long startTimeMillis() {
		return 0;
	}

	@Override
	public void action(String name, boolean start) {
		super.action(name, start);
		if(name.equalsIgnoreCase("battle")) {
			startBattle();
		}
		else if(name.equalsIgnoreCase("teleports")) {
			teleportPlayers();
		}
		else if(name.equalsIgnoreCase("endMatch")) {
			endMatch();
		}
	}

	private void teleportPlayers() {
		for(ArenaTeam value : arenaTeamMap.values()) {
			value.generateSnapshots();
			for(ArenaPlayer arenaPlayer : value.getPlayers()) {
				Location spawnLocation = getSpawnLocation(value.getTeamType());
				Player player = arenaPlayer.getPlayer();
				if(arenaType == EArenaType.ONE_VS_ONE) {
					player.leaveParty();
				}
				player.setTeam(value.getTeamType());
				if(arenaDispelSkills.length > 0) {
					player.getAbnormalList().getEffects().stream().filter(e -> ArrayUtils.contains(arenaDispelSkills, e.getSkill().getId())).forEach(Abnormal::exit);
				}
				if(player.isDead()) {
					player.setPendingRevive(true);
				}
				if(player.isSitting()) {
					player.standUp();
				}
				player.setTransform(null);
				player.setTarget(null);
/*				for(Servitor servitor : player.getServitors()) {
					servitor.unSummon(false);
				}*/
				player.abortAttack(true, false);
				if(player.isCastingNow()) {
					player.abortCast(true, true);
				}
				player.stopMove();
				player.setUndying(SpecialEffectState.TRUE);
				if(player.isInObserverMode()) {
					player.leaveObserverMode();
				}
				if(!player.isFrozen()) {
					player.startFrozen();
				}
				ArenaEventService.getInstance().cpHpMpHeal(player);
				for(Servitor servitor : player.getServitors()) {
					servitor.teleToLocation(spawnLocation, ReflectionManager.MAIN);
				}
				player.teleToLocation(spawnLocation, ReflectionManager.MAIN);
				player.addListener(arenaOnTeleportListener);
				player.addListener(arenaPlayerExitListener);
			}
		}
		statusType = EArenaStatusType.PREPARATION;
	}

	private synchronized void startBattle() {
		statusType = EArenaStatusType.BATTLE;
		arenaTeamMap.values().stream().
				flatMap(e -> e.getPlayers().stream()).
				map(ArenaPlayer::getPlayer).
				filter(e -> e != null && !e.isLogoutStarted()).
				forEach(e -> {
					if(e.isFrozen()) {
						e.stopFrozen();
					}
				});
	}

	@Override
	public SystemMsg canUseItem(Player player, ItemInstance item) {
		int itemId = item.getItemId();
		if(ArrayUtils.contains(arenaRestrictedItems, itemId)) {
			return SystemMsg.THIS_IS_NOT_A_SUITABLE_ITEM;
		}
		return super.canUseItem(player, item);
	}

	@Override
	public boolean canUseSkill(Creature caster, Creature target, Skill skill) {
		if(statusType.ordinal() > EArenaStatusType.BEGINNING.ordinal()) {
			int id = skill.getId();
			if(ArrayUtils.contains(arenaRestrictedSkills, id)) {
				return false;
			}
		}
		return super.canUseSkill(caster, target, skill);
	}

	@Override
	public boolean canUseCommunityFunctions(Player player) {
		return arenaAllowCommunityBoard;
	}

	@Override
	protected void shutdownServer() {
		super.shutdownServer();
		ArenaEventService.getInstance().removeAllRequest(true);
		if(isInProgress()) {
			abortEvent();
		}
	}

	private void abortEvent() {
		if(!isInProgress()) {
			return;
		}
		if(statusType == EArenaStatusType.BEGINNING || statusType == EArenaStatusType.PREPARATION || statusType == EArenaStatusType.BATTLE) {
			arenaTeamMap.values().forEach(ArenaEventService.getInstance()::returnBind);
		}
		arenaTeamMap.values().stream().flatMap(e -> e.getPlayers().stream()).forEach(p -> {
			clearPlayerEvent(p);
			ArenaEventService.getInstance().teleportToBack(p);
		});
	}

	public void sendScreenCustomMessage(String customMessageName, int time) {
		arenaTeamMap.values().stream().flatMap(e -> e.getPlayers().stream()).map(ArenaPlayer::getPlayer).forEach(e -> {
			CustomMessage customMessage = new CustomMessage(customMessageName).addNumber(time);
			ExShowScreenMessage sm = new ExShowScreenMessage(customMessage.toString(e), 2000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true);
			e.sendPacket(sm);
		});
	}

	@Override
	public boolean canJoinParty(Player inviter, Player invited) {
		return false;
	}

	@Override
	public void onAddEvent(GameObject o) {
		if(o.isPlayer()) {
			o.getPlayer().addListener(deathFromUndyingListener);
			o.getPlayer().addListener(arenaPlayerExitListener);
		}
	}

	@Override
	public void onRemoveEvent(GameObject o) {
		if(o.isPlayer()) {
			o.getPlayer().removeListener(deathFromUndyingListener);
			o.getPlayer().removeListener(arenaOnTeleportListener);
			o.getPlayer().removeListener(arenaPlayerExitListener);
		}
	}

	@Override
	public boolean canUseTeleport(Player player) {
		return false;
	}

	public void logoutOrTeleport(Player player) {
		ArenaTeam teamFromPlayer = getTeamFromPlayer(player.getObjectId());
		if(teamFromPlayer != null) {
			teamFromPlayer.removePlayer(player.getObjectId());
		}
		player.removeEvent(this);
		checkWinner();
	}

	@Override
	public void stopEvent(boolean force) {
		super.stopEvent(force);
		clearActions();
		zoneBattle.setActive(false);
		arenaTeamMap.clear();
		arenaType = null;
	}

	public synchronized void endMatch() {
		if(statusType == EArenaStatusType.COMPLETION) {
			return;
		}
		tieScreenMessage();
		arenaTeamMap.values().forEach(e -> ArenaEventService.getInstance().returnBind(e));
		finishEvent();
	}

	private void finishEvent() {
		statusType = EArenaStatusType.COMPLETION;
		ThreadPoolManager.getInstance().schedule(() -> {
			clearActions();
			arenaTeamMap.values().stream().flatMap(e -> e.getPlayers().stream()).forEach(e -> {
				clearPlayerEvent(e);
				ArenaEventService.getInstance().teleportToBack(e);
			});
			stopEvent(false);
			startRegistration();
		}, 10000);
	}

	private void clearPlayerEvent(ArenaPlayer arenaPlayer) {
		Player player = arenaPlayer.getPlayer();
		if(player == null) {
			return;
		}
		player.removeEvent(this);
	}

	private void clearArenaTeamEvent(ArenaTeam arenaTeam) {
		if(arenaTeam == null) {
			return;
		}
		arenaTeam.getPlayers().forEach(this::clearPlayerEvent);
	}

	private void tieScreenMessage() {
		arenaTeamMap.values().stream().flatMap(e -> e.getPlayers().stream()).map(ArenaPlayer::getPlayer).filter(e -> e != null && !e.isLogoutStarted()).forEach(e -> {
			CustomMessage customMessage = new CustomMessage("arena.s21");
			ExShowScreenMessage sm = new ExShowScreenMessage(customMessage.toString(), 5000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true);
			e.sendPacket(sm);
		});
	}

	public synchronized void checkWinner() {
		if(statusType == EArenaStatusType.COMPLETION || statusType == EArenaStatusType.ABORT) {
			return;
		}
		Optional<ArenaTeam> loseTeam = arenaTeamMap.values().stream().filter(e -> e.getPlayers().stream().map(ArenaPlayer::getPlayer).allMatch(p -> p == null || p.isLogoutStarted() || p.isDead() || p.isUndyingFlag())).findFirst();
		if(loseTeam.isPresent()) {
			if(statusType == EArenaStatusType.BEGINNING || statusType == EArenaStatusType.PREPARATION) {
				matchCancel();
				return;
			}
			ArenaTeam arenaTeam = loseTeam.get();
			TeamType teamType = arenaTeam.getTeamType();

			TeamType winnerTeamType = teamType.revert();
			ArenaTeam winnerTeam = arenaTeamMap.get(winnerTeamType);
			int winBidCount = arenaTeam.getBidCount() + winnerTeam.getBidCount();
			double tax = Math.max(0, 100. - arenaRewardTax);
			int adenaCount = (int) (winBidCount * tax / 100.);
			List<RewardItemData> list = new ArrayList<>(rewardItemsData);
			if(adenaCount > 0) {
				RewardItemData rewardItemDataAdena = new RewardItemData(57, adenaCount, adenaCount, 100);
				list.add(rewardItemDataAdena);
			}
			List<ItemData> items = new ArrayList<>();
			for(RewardItemData rewardItemData : list) {
				if(!Rnd.chance(rewardItemData.getChance())) {
					continue;
				}
				long count = Rnd.get(rewardItemData.getMinCount(), rewardItemData.getMaxCount());
				ItemData item = new ItemData(rewardItemData.getId(), count);
				items.add(item);
			}
			Functions.sendSystemMail(winnerTeam.getLeaderId(), winnerTeam.getTeamName(), "Arena - Winner reward", "", items);
			winScreenMessage(winnerTeam);
			finishEvent();
		}
	}

	@Override
	public void sendPackets(IBroadcastPacket... packet) {
		arenaTeamMap.values().stream().flatMap(e -> e.getPlayers().stream()).map(ArenaPlayer::getPlayer).filter(Objects::nonNull).forEach(e -> e.sendPacket(packet));
	}

	private void matchCancel() {
		sendPackets(new CustomMessage("arena.s24"));
		clearActions();
		arenaTeamMap.values().forEach(e -> {
			ArenaEventService.getInstance().returnBind(e);
			e.getPlayers().forEach(p -> {
				clearPlayerEvent(p);
				if(statusType == EArenaStatusType.PREPARATION) {
					ArenaEventService.getInstance().teleportToBack(p);
				}
			});
		});
		stopEvent(false);
		startRegistration();
	}

	private void winScreenMessage(ArenaTeam winnerTeam) {
		arenaTeamMap.values().stream().flatMap(e -> e.getPlayers().stream()).map(ArenaPlayer::getPlayer).filter(e -> e != null && !e.isLogoutStarted()).forEach(e -> {
			CustomMessage customMessage = new CustomMessage("arena.s20").addString(winnerTeam.getTeamName());
			ExShowScreenMessage sm = new ExShowScreenMessage(customMessage.toString(e), 5000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true);
			e.sendPacket(sm);
		});
	}
}
