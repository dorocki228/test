package l2s.gameserver.model.entity.events.impl;

import l2s.commons.collections.MultiValueSet;
import l2s.commons.threading.RunnableImpl;
import l2s.commons.time.cron.SchedulingPattern;
import l2s.commons.util.Rnd;
import l2s.gameserver.Announcements;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.data.xml.holder.EventHolder;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.model.*;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.base.SpecialEffectState;
import l2s.gameserver.model.bbs.EventRegistrationCommunityBoardEntry;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.entity.events.EventCustomReflection;
import l2s.gameserver.model.entity.events.EventItemData;
import l2s.gameserver.model.entity.events.EventType;
import l2s.gameserver.model.entity.events.actions.EventScreenCustomMessage;
import l2s.gameserver.model.entity.events.actions.StartStopAction;
import l2s.gameserver.model.entity.events.impl.liberation.LiberationRoomTeam;
import l2s.gameserver.model.entity.events.EventPlayer;
import l2s.gameserver.model.entity.events.impl.liberation.LiberationStatusType;
import l2s.gameserver.model.entity.events.impl.liberation.listeners.*;
import l2s.gameserver.model.entity.events.impl.service.EventService;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.snapshot.SnapshotPlayer;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.components.hwid.HwidHolder;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.service.ConfrontationService;
import l2s.gameserver.service.PlayerService;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Location;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class LiberationFortressEvent extends SingleMatchEvent implements IRegistrationEvent {

    private static final EventItemData[] EMPTY_ITEM_ID_COUNT_HOLDER = new EventItemData[0];
    private final Map<HwidHolder, Player> registeredPlayers = new HashMap<>();
    private final SchedulingPattern pattern;
    private final Duration registrationTime;
    private final Duration eventTime;
    private final Duration timeResurrectionSeconds;
    private final EventItemData[] loserRewards;
    private final EventItemData[] winnerRewards;
    private final EventItemData[] tieRewards;
    private final Duration spawnTimeWaveAfterTeleport;
    private final Duration spawnTimeNextWave;
    private final int minPlayers;
    private final double bonusWaveChance;
    private final String zoneName;
    private final boolean confrontationRewards;
    private volatile LiberationStatusType status = LiberationStatusType.NONE;
    private Instant startTime;
    private int maxWave;
    private Map<Fraction, LiberationRoomTeam> roomMap = new HashMap<>();
    private ScheduledFuture<?> nextWaveTick;
    private ScheduledFuture<?> messagePointsTick;
    private final Map<Integer, Future<?>> resurrectTaskMap = new ConcurrentHashMap<>();

    public LiberationFortressEvent(MultiValueSet<String> set) {
        super(set);
        pattern = new SchedulingPattern(set.getString("pattern"));
        registrationTime = Duration.ofMinutes(set.getInteger("registrationTime", 5));
        eventTime = Duration.ofMinutes(set.getInteger("eventTime", 20));
        spawnTimeWaveAfterTeleport = Duration.ofSeconds(set.getInteger("spawnTimeWaveAfterTeleport", 25));
        spawnTimeNextWave = Duration.ofSeconds(set.getInteger("spawnTimeNextWave", 30));
        timeResurrectionSeconds = Duration.ofSeconds(set.getInteger("timeResurrectionSeconds", 5));
        loserRewards = parseRewards(set.getString("loserRewards"));
        tieRewards = parseRewards(set.getString("tieRewards"));
        winnerRewards = parseRewards(set.getString("winnerRewards"));
        minPlayers = set.getInteger("minPlayers", 3);
        bonusWaveChance = set.getDouble("bonusWaveChance", 10);
        zoneName = set.getString("zoneName", "");
        confrontationRewards = set.getBool("confrontationRewards", false);
    }

    @Override
    public EventType getType() {
        return EventType.FUN_EVENT;
    }

    @Override
    protected long startTimeMillis() {
        return startTime.toEpochMilli();
    }

    @Override
    public void reCalcNextTime(boolean onInit) {
        if (status != LiberationStatusType.NONE) {
            return;
        }
        startTime = pattern.next(Instant.now());
        clearActions();
        registerActions();
    }

    @Override
    public void initEvent() {
        clearActions();
        callActions(_onInitActions);
        addOnTimeAction(0, new StartStopAction("registration", false));
        addOnTimeAction((int) registrationTime.getSeconds(), new StartStopAction("endRegistration", false));
        int teleportationToInstance = (int) registrationTime.getSeconds() + 1;
        addOnTimeAction(teleportationToInstance, new StartStopAction("startWave", false));
        int time0 = (int) (teleportationToInstance + eventTime.getSeconds());
        addOnTimeAction(time0, new StartStopAction("endBattle", false));
        reCalcNextTime(true);
        printInfo();
    }

    @Override
    public synchronized void action(String name, boolean start) {
        super.action(name, start);
        if (name.equalsIgnoreCase("registration")) {
            startRegistration();
        } else if (name.equalsIgnoreCase("endRegistration")) {
            endRegistration();
        } else if (name.equalsIgnoreCase("endEvent")) {
            endEvent();
        } else if (name.equalsIgnoreCase("endBattle")) {
            endBattle();
        } else if (name.equalsIgnoreCase("abortEvent")) {
            abortEvent();
        } else if (name.equalsIgnoreCase("startWave")) {
            startWave();
        } else if (name.equalsIgnoreCase("nextWaveTick")) {
            nextWaveTick();
        } else if (name.equalsIgnoreCase("broadcastMessagePoints")) {
            broadcastMessagePoints();
        }
    }

    @Override
    public void startEvent() {
        if (status != LiberationStatusType.NONE) {
            return;
        }
        super.startEvent();
        startRegistration();
    }

    @Override
    public void stopEvent(boolean force) {
        if (status == LiberationStatusType.NONE) {
            return;
        }
        super.stopEvent(force);
        abortEvent();
    }

    @Override
    protected void shutdownServer() {
        super.shutdownServer();
        abortEvent();
    }

    @Override
    public boolean isRegistrationOver() {
        return status != LiberationStatusType.REGISTRATION;
    }

    @Override
    public boolean isInProgress() {
        return status != LiberationStatusType.REGISTRATION && status != LiberationStatusType.NONE;
    }

    @Override
    public synchronized boolean registerPlayer(Player player) {
        if (player == null) {
            return false;
        }
        if (status != LiberationStatusType.REGISTRATION) {
            player.sendMessage(new CustomMessage("liberation.s7"));
            return false;
        }
        Fraction fraction = player.getFraction();
        if (fraction == Fraction.NONE) {
            return false;
        }
        if (player.getReflectionId() != ReflectionManager.MAIN.getId()) {
            player.sendMessage(new CustomMessage("liberation.s5"));
            return false;
        }
        if (registeredPlayers.containsKey(player.getHwidHolder())) {
            player.sendMessage(new CustomMessage("liberation.s8"));
            return false;
        }
        if (Olympiad.isRegistered(player) || Olympiad.isRegisteredInComp(player)) {
            player.sendMessage(new CustomMessage("liberation.s4"));
            return false;
        } else if (player.isRegisteredInEvent() || player.containsEvent(SingleMatchEvent.class)) {
            player.sendMessage(new CustomMessage("liberation.s4"));
            return false;
        }
        player.addEvent(this);
        player.addListener(LiberationRegistered.getInstance());
        registeredPlayers.put(player.getHwidHolder(), player);
        return true;
    }

    @Override
    public synchronized boolean unregisterPlayer(Player player) {
        if (player == null) {
            return false;
        }
        if (status != LiberationStatusType.REGISTRATION) {
            return false;
        }
        player.removeEvent(this);
        player.removeListener(LiberationRegistered.getInstance());
        registeredPlayers.remove(player.getHwidHolder());
        return true;
    }

    @Override
    public boolean isPlayerRegistered(Player player) {
        return registeredPlayers.containsKey(player.getHwidHolder());
    }

    @Override
    public void onAddEvent(GameObject o) {
        super.onAddEvent(o);
    }

    @Override
    public void onRemoveEvent(GameObject o) {
        super.onRemoveEvent(o);
        if (o.isPlayer()) {
            Player player = (Player) o;
            playerRemoveListeners(player);
        }
    }

    private void abortEvent() {
        if (status == LiberationStatusType.ABORT) {
            return;
        }
        LiberationStatusType oldStatus = status;
        status = LiberationStatusType.ABORT;
        stopMessagePointsTick();
        stopNextWaveTick();
        clearActions();
        Announcements.announceToAllFromStringHolder("liberation.s9");
        resurrectTaskMap.values().forEach(p -> p.cancel(true));
        resurrectTaskMap.clear();
        roomMap.values().forEach(e -> e.abortEvent(this, oldStatus));
        if (!registeredPlayers.isEmpty()) {
            registeredPlayers.values().forEach(p -> p.removeEvent(this));
        }
        roomMap.clear();
        registeredPlayers.clear();
        status = LiberationStatusType.NONE;
        reCalcNextTime(false);
    }

    private void broadcastMessagePoints() {
        if (status != LiberationStatusType.BATTLE) {
            return;
        }
        final Consumer<EventScreenCustomMessage.ScreenDto> screenConsumer = screenDto -> {
            LiberationFortressEvent event = ((LiberationFortressEvent) screenDto.getEvent());
            event.sendScreenCustomMessage(screenDto.getCustomMessage(), screenDto.getTexts());
        };
        LiberationRoomTeam fireTeam = getRoomTeam(Fraction.FIRE);
        LiberationRoomTeam waterTeam = getRoomTeam(Fraction.WATER);
        EventScreenCustomMessage action = new EventScreenCustomMessage(
                "liberation.s12",
                new String[]{
                        fireTeam != null ? "" + fireTeam.getPoints() : "0",
                        waterTeam != null ? "" + waterTeam.getPoints() : "0",
                },
                screenConsumer
        );
        action.call(this);
    }

    public void sendScreenCustomMessage(String customMessageName, String[] texts) {
        roomMap.values().stream()
                .flatMap(e -> e.getPlayers().stream())
                .map(EventPlayer::getPlayer)
                .forEach(e -> EventService.getInstance().sendScreenCustomMessage(customMessageName, texts, e, 5000));
    }

    private void nextWaveTick() {
        if (status != LiberationStatusType.BATTLE) {
            return;
        }
        roomMap.values().stream()
                .filter(LiberationRoomTeam::canNextWave)
                .forEach(r -> r.nextWave(LiberationFortressEvent.this));
    }

    private void endBattle() {
        if (status != LiberationStatusType.BATTLE) {
            return;
        }
        LiberationRoomTeam liberationRoomTeam = roomMap.values().stream()
                .filter(t -> t.getPoints() > 0)
                .filter(t -> t.isWavesOver() && t.getLastKillTimestamp() != 0)
                .min(Comparator.comparingLong(LiberationRoomTeam::getLastKillTimestamp))
                .orElse(null);
        if (liberationRoomTeam == null) {
            liberationRoomTeam = roomMap.values().stream()
                    .filter(t -> t.getPoints() > 0)
                    .max(Comparator.comparingLong(LiberationRoomTeam::getLastKillTimestamp))
                    .orElse(null);
        }
        final Map<Integer, List<LiberationRoomTeam>> tieTeams = roomMap.values()
                .stream().collect(Collectors.groupingBy(LiberationRoomTeam::getPoints));
        if (tieTeams.size() == 1) {
            Announcements.announceToAllFromStringHolder("liberation.s11");
            tieTeams.values().stream()
                    .flatMap(Collection::stream)
                    .flatMap(t -> t.getPlayers().stream())
                    .map(EventPlayer::getPlayer)
                    .filter(Objects::nonNull)
                    .filter(p -> !p.isLogoutStarted())
                    .forEach(p -> Arrays.stream(tieRewards)
                            .forEach(reward -> ItemFunctions.addItem(p, reward.getId(), reward.getCount()))
                    );
        } else if (liberationRoomTeam != null) {
            List<Player> winPlayers = liberationRoomTeam.getPlayers().stream()
                    .map(EventPlayer::getPlayer)
                    .filter(Objects::nonNull)
                    .filter(p -> !p.isLogoutStarted())
                    .collect(Collectors.toList());
            if (confrontationRewards && !winPlayers.isEmpty()) {
                int count = (int) Math.ceil(liberationRoomTeam.getPoints() / (double) winPlayers.size());
                winPlayers.forEach(v -> ConfrontationService.getInstance().addPoints(v, count));
            }
            // winner
            winPlayers.forEach(p ->
                    Arrays.stream(winnerRewards).forEach(reward -> ItemFunctions.addItem(p, reward.getId(), reward.getCount()))
            );

            Fraction loseFaction = liberationRoomTeam.getFraction().revert();
            // loser
            LiberationRoomTeam loseRoomTeam = getRoomTeam(loseFaction);
            if (loseRoomTeam != null) {
                loseRoomTeam.getPlayers().stream()
                        .map(EventPlayer::getPlayer)
                        .filter(Objects::nonNull)
                        .filter(p -> !p.isLogoutStarted())
                        .forEach(p -> Arrays.stream(loserRewards)
                                .forEach(reward -> ItemFunctions.addItem(p, reward.getId(), reward.getCount()))
                        );
                Announcements.announceToAllFromStringHolder(
                        "liberation.s10",
                        liberationRoomTeam.getFraction().toString(),
                        liberationRoomTeam.getPoints(),
                        loseRoomTeam.getPoints()
                );
            }
        }
        resurrectTaskMap.values().forEach(p -> p.cancel(true));
        resurrectTaskMap.clear();
        status = LiberationStatusType.COMPLETION;
        ThreadPoolManager.getInstance().schedule(new RunnableImpl() {
            @Override
            protected void runImpl() throws Exception {
                action("endEvent", false);
            }
        }, 5000);
    }

    public Reflection getReflectionFromFaction(Fraction fraction) {
        LiberationRoomTeam liberationRoomTeam = getRoomTeam(fraction);
        if (liberationRoomTeam == null) {
            return null;
        }
        return liberationRoomTeam.getReflection();
    }

    private void startRegistration() {
        if (status != LiberationStatusType.NONE) {
            return;
        }
        status = LiberationStatusType.REGISTRATION;
        Announcements.announceToAllFromStringHolder("liberation.s2", registrationTime.toMinutes());
        addCommunityBoardEntry("registration", new EventRegistrationCommunityBoardEntry(this));
    }

    private void endEvent() {
        stopMessagePointsTick();
        stopNextWaveTick();
        roomMap.values().forEach(t -> t.endEvent(this));
        roomMap.clear();
        status = LiberationStatusType.NONE;
        reCalcNextTime(false);
    }

    private void endRegistration() {
        if (status != LiberationStatusType.REGISTRATION) {
            return;
        }
        status = LiberationStatusType.PREPARATION;
        removeCommunityBoardEntry("registration");
        Map<Fraction, List<Player>> fractionMap = registeredPlayers.values().stream().collect(Collectors.groupingBy(Player::getFraction));
        boolean checkMinPlayers = fractionMap.entrySet().stream().anyMatch(v -> v.getValue().size() < minPlayers);
        if (checkMinPlayers || fractionMap.size() <= 1) {
            Announcements.announceToAllFromStringHolder("liberation.s3");
            action("abortEvent", false);
            return;
        }
        roomMap.clear();
        registeredPlayers.clear();
        maxWave = Rnd.chance(bonusWaveChance) ? 11 : 10;
        roomMap = createTeams(fractionMap);
    }

    private void startWave() {
        if (status != LiberationStatusType.PREPARATION) {
            return;
        }
        status = LiberationStatusType.BATTLE;
        final List<EventPlayer> playerList = roomMap.values().stream().flatMap(p -> p.getPlayers().stream()).collect(Collectors.toList());
        List<Location> points = getObjects("teleport_points");
        final List<EventPlayer> incorrectPlayerList = roomMap.entrySet().stream()
                .flatMap(t -> t.getValue().teleportPlayers(points, true).stream())
                .collect(Collectors.toList());
        final Set<Integer> incorrectPlayerObjIds = incorrectPlayerList.stream().map(EventPlayer::getObjId).collect(Collectors.toSet());
        playerList.stream()
                .filter(p -> !incorrectPlayerObjIds.contains(p.getObjId()))
                .forEach(p -> {
                    Player player = p.getPlayer();
                    playerAddListeners(player);
                });
        if (!incorrectPlayerList.isEmpty()) {
            if (incorrectPlayerList.size() >= playerList.size()) {
                Announcements.announceToAllFromStringHolder("liberation.s3");
                action("abortEvent", false);
                return;
            }
            incorrectPlayerList.forEach(p -> logoutOrTeleportFromEvent(p.getPlayer(), false));
        }
        roomMap.values().forEach(r -> r.startWave(this));
        nextWaveTick = ThreadPoolManager.getInstance().scheduleAtFixedDelay(new RunnableImpl() {
            @Override
            protected void runImpl() {
                Optional<LiberationRoomTeam> liberationRoomTeamStream = roomMap.values().stream()
                        .filter(LiberationRoomTeam::isWavesOver)
                        .findFirst();
                if (liberationRoomTeamStream.isPresent()) {
                    action("endBattle", false);
                    return;
                }
                action("nextWaveTick", false);
            }
        }, Duration.ofSeconds(10).toMillis(), Duration.ofSeconds(10).toMillis());
        messagePointsTick = ThreadPoolManager.getInstance().scheduleAtFixedDelay(new RunnableImpl() {
            @Override
            protected void runImpl() {
                action("broadcastMessagePoints", false);
            }
        }, 30000, 30000);
        Announcements.announceToAllFromStringHolder("liberation.s6", eventTime.toMinutes());
        checkCompletionFromFaction();
    }

    public void logoutOrTeleportFromEvent(Player player, boolean b) {
        if (player == null) {
            return;
        }
        if (!isInProgress()) {
            return;
        }
        Fraction fraction = player.getFraction();
        LiberationRoomTeam factionTeam = roomMap.get(fraction);
        if (factionTeam == null) {
            return;
        }
        EventPlayer eventPlayer = factionTeam.removePlayer(player.getObjectId());
        if (eventPlayer != null) {
            SnapshotPlayer snapshotPlayer = eventPlayer.getSnapshotPlayer();
            if (eventPlayer.getPlayer().isUndyingFlag()) {
                EventService.getInstance().removeFakeDeath(player);
            }
            EventService.getInstance().restorePlayerAndTeleportToBack(player, snapshotPlayer);
            player.removeEvent(this);
        }
    }

    public Map<Fraction, LiberationRoomTeam> createTeams(Map<Fraction, List<Player>> fractionMap) {
        return fractionMap
                .entrySet()
                .stream()
                .map(i -> {
                    Map<Integer, EventPlayer> map = i.getValue().stream()
                            .collect(Collectors.toMap(GameObject::getObjectId, p -> {
                                SnapshotPlayer snapshot = PlayerService.getInstance().createSnapshot(p);
                                return new EventPlayer(p, snapshot);
                            }));
                    EventCustomReflection reflection = new EventCustomReflection(1004);
                    Zone zone = reflection.getZone(zoneName);
                    if (zone != null) {
                        LiberationZoneListener listener = LiberationZoneListener.getInstance();
                        zone.addListener(listener);
                    }
                    return new LiberationRoomTeam(
                            reflection,
                            i.getKey(),
                            map,
                            maxWave,
                            spawnTimeNextWave,
                            spawnTimeWaveAfterTeleport
                    );
                })
                .collect(Collectors.toMap(LiberationRoomTeam::getFraction, p -> p));
    }

    public void teleportPlayerToEventZone(Player player) {
        LiberationRoomTeam liberationRoomTeam = roomMap.get(player.getFraction());
        if (liberationRoomTeam == null) {
            return;
        }
        EventPlayer eventPlayer = liberationRoomTeam.getPlayer(player.getObjectId());
        if (eventPlayer == null) {
            return;
        }
        List<Location> points = getObjects("teleport_points");
        liberationRoomTeam.teleportPlayers(Collections.singletonList(eventPlayer), points, false);
    }

    private void checkCompletionFromFaction() {
        long count = roomMap.values().stream().filter(p -> p.getPlayers().size() > 0).count();
        if (count <= 1) {
            action("endEvent", false);
        }
    }

    private void playerAddListeners(Player player) {
        if (player == null) {
            return;
        }
        player.addListener(LiberationOnDeathFromUndyingListenerImpl.getInstance());
        player.addListener(LiberationOnTeleportListenerImpl.getInstance());
        player.addListener(LiberationPlayerExitListener.getInstance());
    }

    private void playerRemoveListeners(Player player) {
        if (player == null) {
            return;
        }
        player.removeListener(LiberationOnDeathFromUndyingListenerImpl.getInstance());
        player.removeListener(LiberationOnTeleportListenerImpl.getInstance());
        player.removeListener(LiberationPlayerExitListener.getInstance());
        player.removeListener(LiberationRegistered.getInstance());
    }

    public LiberationRoomTeam getRoomTeam(Fraction fraction) {
        return roomMap.get(fraction);
    }

    public LiberationStatusType getStatus() {
        return status;
    }

    @Override
    public void onDie(Player actor, Creature killer) {
        List<Servitor> servitors = actor.getServitors();
        for (Servitor servitor : servitors) {
            servitor.abortAttack(true, false);
            servitor.abortCast(true, false);
        }
        actor.setTarget(null);
        actor.abortCast(true, false);
        actor.abortAttack(true, false);
        actor.stopMove();
        actor.stopAttackStanceTask();
        EventService.getInstance().setFakeDeath(actor);
        for (NpcInstance npc : killer.getAroundNpc(5000, 5000)) {
            npc.getAI().notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, actor);
        }
        long seconds = timeResurrectionSeconds.toSeconds();
        actor.setNonAggroTime(System.currentTimeMillis() + seconds * 1000);
        actor.sendPacket(new SystemMessagePacket(SystemMsg.RESURRECTION_WILL_TAKE_PLACE_IN_THE_WAITING_ROOM_AFTER_S1_SECONDS).addNumber(seconds));
        Future<?> future = resurrectTaskMap.put(actor.getObjectId(), ThreadPoolManager.getInstance().schedule(new RessurectTask(actor), seconds, TimeUnit.SECONDS));
        if (future != null) {
            future.cancel(true);
        }
    }

    private void ressurect(Player player) {
        if (player == null) {
            return;
        }
        if (!isInProgress() || status != LiberationStatusType.BATTLE) {
            return;
        }
        Fraction fraction = player.getFraction();
        LiberationRoomTeam room = getRoomTeam(fraction);
        if (room == null) {
            return;
        }
        if (!room.isParticipant(player.getObjectId())) {
            return;
        }
        EventPlayer eventPlayer = room.getPlayer(player.getObjectId());
        if (eventPlayer == null) {
            return;
        }
        List<Location> points = getObjects("teleport_points");
        room.teleportPlayers(Collections.singletonList(eventPlayer), points, false);
        EventService.getInstance().cpHpMpHeal(player);
        if (player.isUndyingFlag()) {
            player.setUndying(SpecialEffectState.TRUE);
        }
        EventService.getInstance().removeFakeDeath(player);
        player.dispelDebuffs();
    }

    private void stopNextWaveTick() {
        ScheduledFuture<?> nextWaveTick0 = nextWaveTick;
        if (nextWaveTick0 != null) {
            nextWaveTick0.cancel(true);
            nextWaveTick = null;
        }
    }

    private void stopMessagePointsTick() {
        ScheduledFuture<?> messagePointsTick0 = messagePointsTick;
        if (messagePointsTick0 != null) {
            messagePointsTick0.cancel(true);
            messagePointsTick = null;
        }
    }

    private EventItemData[] parseRewards(String rewards) {
        return rewards.isEmpty()
                ? EMPTY_ITEM_ID_COUNT_HOLDER
                : Arrays.stream(rewards.split(";")).map(EventItemData::new).toArray(EventItemData[]::new);
    }

    @Override
    public boolean canUseAcp(Player actor) {
        return true;
    }

    private static class RessurectTask implements Runnable {
        private final Player player;

        public RessurectTask(Player player) {
            this.player = player;
        }

        @Override
        public void run() {
            LiberationFortressEvent event = EventHolder.getInstance().getEvent(EventType.FUN_EVENT, 1004);
            if (event == null) {
                return;
            }
            event.ressurect(player);
        }
    }
}
