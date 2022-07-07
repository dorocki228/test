package l2s.gameserver.model.entity.events.impl;

import l2s.commons.collections.MultiValueSet;
import l2s.commons.time.cron.SchedulingPattern;
import l2s.commons.util.Rnd;
import l2s.gameserver.Announcements;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.data.xml.holder.EventHolder;
import l2s.gameserver.instancemanager.GveRewardManager;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.FakePlayer;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.base.SpecialEffectState;
import l2s.gameserver.model.bbs.EventRegistrationCommunityBoardEntry;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.entity.events.EventCustomReflection;
import l2s.gameserver.model.entity.events.EventType;
import l2s.gameserver.model.entity.events.actions.EventScreenCustomMessage;
import l2s.gameserver.model.entity.events.actions.StartStopAction;
import l2s.gameserver.model.entity.events.impl.pvparena.PvpArenaFactionTeam;
import l2s.gameserver.model.entity.events.impl.pvparena.PvpArenaPlayer;
import l2s.gameserver.model.entity.events.impl.pvparena.PvpArenaStatusType;
import l2s.gameserver.model.entity.events.impl.pvparena.PvpArenaZone;
import l2s.gameserver.model.entity.events.impl.pvparena.listeners.*;
import l2s.gameserver.model.entity.events.impl.service.EventService;
import l2s.gameserver.model.entity.events.impl.service.PvpArenaService;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.snapshot.SnapshotPlayer;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.service.ConfrontationService;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Location;
import org.apache.commons.lang3.ArrayUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PvpArenaEvent extends SingleMatchEvent {
    private static final ItemHolder[] EMPTY_ITEM_ID_COUNT_HOLDER = new ItemHolder[0];

    private final SchedulingPattern pattern;
    private final Duration registrationTime;
    private final Duration battleTime;
    private final int minPlayers;
    private final int maxPlayers;
    private final Duration timeResurrectionSeconds;
    private final ItemHolder[] tieRewards;
    private final ItemHolder[] loserRewards;
    private final ItemHolder[] winnerRewards;
    private final int winnerConfrontation;
    private final String[] activeZones;
    private final double adenaMod;
    private final int[] disableSkills;
    private final int[] selfUseSkills;
    private Instant startTime;
    private volatile PvpArenaStatusType status = PvpArenaStatusType.NONE;
    private List<PvpArenaZone> pvpArenaZones = Collections.emptyList();
    private final Map<Integer, Player> registeredPlayers = new HashMap<>();
    private PvpArenaZone currentArena;
    private Map<Fraction, PvpArenaFactionTeam> teamMap = new HashMap<>();
    private Reflection reflection;
    private final Map<Integer, Future<?>> ressurectTaskMap = new ConcurrentHashMap<>();

    public PvpArenaEvent(MultiValueSet<String> set) {
        super(set);
        pattern = new SchedulingPattern(set.getString("pattern"));
        registrationTime = Duration.ofMinutes(set.getInteger("registrationTime", 7));
        battleTime = Duration.ofMinutes(set.getInteger("battleTime", 15));
        timeResurrectionSeconds = Duration.ofSeconds(set.getInteger("timeResurrectionSeconds", 5));
        minPlayers = set.getInteger("minPlayers", 10);
        maxPlayers = set.getInteger("maxPlayers", 100);
        tieRewards = parseRewards(set.getString("tieRewards"));
        loserRewards = parseRewards(set.getString("loserRewards"));
        winnerRewards = parseRewards(set.getString("winnerRewards"));
        winnerConfrontation = set.getInteger("winnerConfrontation", 0);
        activeZones = set.getString("activeZones").split(",");
        adenaMod = set.getDouble("adenaMod", 0.7);
        disableSkills = set.getIntegerArray("disableSkills", ",");
        selfUseSkills = set.getIntegerArray("selfUseSkills", ",");
    }

    @Override
    public EventType getType() {
        return EventType.CUSTOM_PVP_EVENT;
    }

    @Override
    protected long startTimeMillis() {
        return startTime.toEpochMilli();
    }

    @Override
    public void reCalcNextTime(boolean onInit) {
        if (status != PvpArenaStatusType.NONE) {
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
        reflection = new EventCustomReflection(-getId(), getId());
        pvpArenaZones = PvpArenaService.getInstance().getZonesFromConfiguration(this, activeZones);
        for (String activeZone : activeZones) {
            initAction(activeZone);
        }
        addOnTimeAction(0, new StartStopAction("registration", false));
        addOnTimeAction((int) registrationTime.getSeconds(), new StartStopAction("endRegistration", false));
        int teleportationToArena = (int) registrationTime.getSeconds() + 1;
        addOnTimeAction(teleportationToArena, new StartStopAction("createBattle", false));

        final int startBattle15 = teleportationToArena + 5;
        final int startBattle10 = startBattle15 + 5;
        final int startBattle5 = startBattle10 + 5;
        final int startBattle0 = startBattle5 + 5;

        final Consumer<EventScreenCustomMessage.ScreenDto> screenConsumer = screenDto -> {
            PvpArenaEvent event = ((PvpArenaEvent) screenDto.getEvent());
            event.sendScreenCustomMessage(screenDto.getCustomMessage(), screenDto.getTexts());
        };
        addOnTimeAction(startBattle15, new EventScreenCustomMessage("pvp.arena.s1", new String[]{"15"}, screenConsumer));
        addOnTimeAction(startBattle10, new EventScreenCustomMessage("pvp.arena.s1", new String[]{"10"}, screenConsumer));
        addOnTimeAction(startBattle5, new EventScreenCustomMessage("pvp.arena.s1", new String[]{"5"}, screenConsumer));
        addOnTimeAction(startBattle0, new EventScreenCustomMessage("pvp.arena.s1", new String[]{"GO"}, screenConsumer));

        addOnTimeAction(startBattle0, new StartStopAction("startBattle", false));

        final long minutes = battleTime.toMinutes();
        for (long i = 1; i < minutes; i++) {
            addOnTimeAction((int) (startBattle0 + Duration.ofMinutes(i).toSeconds()), new EventScreenCustomMessage("pvp.arena.s15", new String[]{String.valueOf(minutes - i)}, screenConsumer));
        }

        addOnTimeAction((int) (startBattle0 + battleTime.getSeconds()), new StartStopAction("endBattle", false));
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
        } else if (name.equalsIgnoreCase("createBattle")) {
            createBattle();
        } else if (name.equalsIgnoreCase("endBattle")) {
            endBattle();
        } else if (name.equalsIgnoreCase("abortEvent")) {
            abortEvent();
        } else if (name.equalsIgnoreCase("startBattle")) {
            startBattle();
        }
    }

    @Override
    public void startEvent() {
        if (status != PvpArenaStatusType.NONE) {
            return;
        }
        super.startEvent();
        startRegistration();
    }

    @Override
    public void stopEvent(boolean force) {
        if (status == PvpArenaStatusType.NONE) {
            return;
        }
        super.stopEvent(force);
        abortEvent();
    }

    @Override
    public boolean isRegistrationOver() {
        return status != PvpArenaStatusType.REGISTRATION;
    }

    private void startRegistration() {
        if (status != PvpArenaStatusType.NONE) {
            return;
        }
        status = PvpArenaStatusType.REGISTRATION;
        Announcements.announceToAllFromStringHolder("pvp.arena.s2");
        addCommunityBoardEntry("registration", new EventRegistrationCommunityBoardEntry(this));
    }

    private void endRegistration() {
        if (status != PvpArenaStatusType.REGISTRATION) {
            return;
        }
        status = PvpArenaStatusType.BEGINNING;
        removeCommunityBoardEntry("registration");
        Map<Fraction, List<Player>> fractionMap = registeredPlayers.values().stream().collect(Collectors.groupingBy(Player::getFraction));
        Collection<Player> players = registeredPlayers.values();
        if (players.size() < minPlayers || fractionMap.size() <= 1) {
            Announcements.announceToAllFromStringHolder("pvp.arena.s3");
            action("abortEvent", false);
            return;
        }
        teamMap.clear();
        registeredPlayers.clear();
        teamMap = PvpArenaService.getInstance().createTeams(fractionMap);
        currentArena = Rnd.get(pvpArenaZones);
        pvpArenaZones.forEach(p -> p.getZoneObject().setActive(false));
        currentArena.getZoneObject().setActive(true);
    }

    private void createBattle() {
        if (status != PvpArenaStatusType.BEGINNING) {
            return;
        }
        status = PvpArenaStatusType.PREPARATION;
        final List<PvpArenaPlayer> playerList = teamMap.values().stream().flatMap(p -> p.getPvpArenaPlayers().stream()).collect(Collectors.toList());
        final List<PvpArenaPlayer> incorrectPlayerList = teleportPlayers(playerList, currentArena.getPoints());
        final Set<Integer> incorrectPlayerObjIds = incorrectPlayerList.stream().map(PvpArenaPlayer::getObjId).collect(Collectors.toSet());
        playerList.stream()
                .filter(p -> !incorrectPlayerObjIds.contains(p.getObjId()))
                .forEach(p -> {
                    Player player = p.getPlayer();
                    playerAddListeners(player);
                });

        Announcements.announceToAllFromStringHolder("pvp.arena.s10");
        if (!incorrectPlayerList.isEmpty()) {
            if (incorrectPlayerList.size() >= playerList.size()) {
                action("abortEvent", false);
                return;
            }
            incorrectPlayerList.forEach(p -> logoutOrTeleportFromBattle(p.getPlayer(), false));
            checkCompletionFromTeam();
        }
    }

    private void playerAddListeners(Player player) {
        if (player == null) {
            return;
        }
        player.addListener(PvpArenaOnDeathFromUndyingListenerImpl.getInstance());
        player.addListener(PvpArenaOnTeleportListenerImpl.getInstance());
        player.addListener(PvpArenaPlayerExitListener.getInstance());
        player.addListener(PvpArenaZoneListener.getInstance());
    }

    private void playerRemoveListeners(Player player) {
        if (player == null) {
            return;
        }
        player.removeListener(PvpArenaOnDeathFromUndyingListenerImpl.getInstance());
        player.removeListener(PvpArenaOnTeleportListenerImpl.getInstance());
        player.removeListener(PvpArenaPlayerExitListener.getInstance());
        player.removeListener(PvpArenaZoneListener.getInstance());
        player.removeListener(PvpArenaListenRegistered.getInstance());
    }

    private void startBattle() {
        if (status != PvpArenaStatusType.PREPARATION) {
            return;
        }
        status = PvpArenaStatusType.BATTLE;
        removeBlockAllPlayers();
    }

    private void endBattle() {
        if (status != PvpArenaStatusType.BATTLE) {
            return;
        }
        Optional<PvpArenaFactionTeam> winTeamOptional = teamMap.values().stream().max(Comparator.comparingInt(PvpArenaFactionTeam::getAllKillCount));
        if (winTeamOptional.isPresent()) {
            PvpArenaFactionTeam winTeam = winTeamOptional.get();
            List<PvpArenaFactionTeam> winTeams = teamMap.values().stream().filter(p -> p.getAllKillCount() >= winTeam.getAllKillCount()).collect(Collectors.toList());
            // tie
            if (winTeams.size() > 1) {
                teamMap.values().stream()
                        .flatMap(p -> p.getPvpArenaPlayers().stream())
                        .map(PvpArenaPlayer::getPlayer)
                        .filter(Objects::nonNull)
                        .filter(p -> !p.isLogoutStarted())
                        .forEach(p ->
                                Arrays.stream(tieRewards).forEach(reward -> ItemFunctions.addItem(p, reward.getId(), reward.getCount()))
                        );
                Announcements.announceToAllFromStringHolder("pvp.arena.s13", winTeam.getAllKillCount());
            } else {
                winTeam.getPvpArenaPlayers().stream()
                        .map(PvpArenaPlayer::getPlayer)
                        .filter(Objects::nonNull)
                        .filter(p -> !p.isLogoutStarted())
                        .forEach(p -> {
                                    ConfrontationService.getInstance().addPoints(p, winnerConfrontation);
                                    Arrays.stream(winnerRewards).forEach(reward -> ItemFunctions.addItem(p, reward.getId(), reward.getCount()));
                                }
                        );

                teamMap.values().stream()
                        .filter(t -> t.getFraction() != winTeam.getFraction())
                        .flatMap(t -> t.getPvpArenaPlayers().stream())
                        .map(PvpArenaPlayer::getPlayer)
                        .filter(Objects::nonNull)
                        .filter(p -> !p.isLogoutStarted())
                        .forEach(p ->
                                Arrays.stream(loserRewards).forEach(reward -> ItemFunctions.addItem(p, reward.getId(), reward.getCount()))
                        );
                Announcements.announceToAllFromStringHolder("pvp.arena.s12", winTeam.getFraction(), winTeam.getAllKillCount());
            }
        }
        restorePlayersAndTeleportToBack();
        currentArena.getZoneObject().setActive(false);
        teamMap.clear();
        currentArena = null;
        registeredPlayers.clear();
        status = PvpArenaStatusType.NONE;
        reCalcNextTime(false);
    }

    private void restorePlayersAndTeleportToBack() {
        teamMap.values().stream()
                .flatMap(t -> t.getPvpArenaPlayers().stream())
                .forEach(p -> {
                    Player player = p.getPlayer();
                    if (player != null) {
                        player.removeEvent(this);
                        EventService.getInstance().restorePlayerAndTeleportToBack(player, p.getSnapshotPlayer());
                    }
                });
    }

    private void abortEvent() {
        if (status == PvpArenaStatusType.ABORT) {
            return;
        }
        PvpArenaStatusType oldStatus = status;
        status = PvpArenaStatusType.ABORT;
        clearActions();
        Announcements.announceToAllFromStringHolder("pvp.arena.s11");
        if (oldStatus.ordinal() >= PvpArenaStatusType.BEGINNING.ordinal()) {
            ressurectTaskMap.values().forEach(p -> p.cancel(true));
            ressurectTaskMap.clear();
            restorePlayersAndTeleportToBack();
        }
        if (currentArena != null) {
            currentArena.getZoneObject().setActive(false);
        }
        if (!registeredPlayers.isEmpty()) {
            registeredPlayers.values().forEach(p -> p.removeEvent(this));
        }
        teamMap.clear();
        currentArena = null;
        registeredPlayers.clear();
        status = PvpArenaStatusType.NONE;
        reCalcNextTime(false);
    }

    @Override
    protected void shutdownServer() {
        super.shutdownServer();
        abortEvent();
    }

    public void sendScreenCustomMessage(String customMessageName, String[] texts) {
        teamMap.values().stream()
                .flatMap(e -> e.getPvpArenaPlayers().stream())
                .map(PvpArenaPlayer::getPlayer)
                .forEach(e -> EventService.getInstance().sendScreenCustomMessage(customMessageName, texts, e));
    }

    @Override
    public Optional<String> getOnScreenMessage(Player player) {
        if (status != PvpArenaStatusType.REGISTRATION) {
            return Optional.empty();
        }
        return Optional.of(getName() + " [Reg]");
    }

    @Override
    public boolean isInProgress() {
        return status != PvpArenaStatusType.REGISTRATION && status != PvpArenaStatusType.NONE;
    }

    @Override
    public synchronized boolean registerPlayer(Player player) {
        if (player == null) {
            return false;
        }
        if (status != PvpArenaStatusType.REGISTRATION) {
            player.sendMessage(new CustomMessage("pvp.arena.s8"));
            return false;
        }
        Fraction fraction = player.getFraction();
        if (fraction == Fraction.NONE) {
            return false;
        }
        if (player.getReflectionId() != ReflectionManager.MAIN.getId()) {
            player.sendMessage(new CustomMessage("pvp.arena.s6"));
            return false;
        }
        if (registeredPlayers.size() >= maxPlayers) {
            player.sendMessage(new CustomMessage("pvp.arena.s4"));
            return false;
        }
        if (registeredPlayers.containsKey(player.getObjectId())) {
            player.sendMessage(new CustomMessage("pvp.arena.s9"));
            return false;
        }
        if (Olympiad.isRegistered(player) || Olympiad.isRegisteredInComp(player)) {
            player.sendMessage(new CustomMessage("pvp.arena.s5"));
            return false;
        } else if (player.isRegisteredInEvent() || player.containsEvent(SingleMatchEvent.class)) {
            player.sendMessage(new CustomMessage("pvp.arena.s5"));
            return false;
        }
        if (!checkBalanceFromFaction(fraction)) {
            player.sendMessage(new CustomMessage("pvp.arena.s7"));
            return false;
        }
        player.addEvent(this);
        player.addListener(PvpArenaListenRegistered.getInstance());
        registeredPlayers.put(player.getObjectId(), player);
        return true;
    }

    @Override
    public boolean isPlayerRegistered(Player player) {
        return registeredPlayers.containsKey(player.getObjectId());
    }

    private boolean checkBalanceFromFaction(Fraction fraction) {
        if (fraction == null) {
            return false;
        }
        Map<Fraction, Long> map = Stream.of(Fraction.values()).filter(i -> i != Fraction.NONE).collect(Collectors.groupingBy(f -> f, Collectors.summingLong(i -> 0L)));
        Map<Fraction, Long> fractionLongMap =
                registeredPlayers.values().stream()
                        .collect(Collectors.groupingBy(Player::getFraction, Collectors.counting()));
        map.putAll(fractionLongMap);
        final long playerFactionCount = fractionLongMap.getOrDefault(fraction, 0L);
        for (Map.Entry<Fraction, Long> entry : fractionLongMap.entrySet()) {
            Fraction key = entry.getKey();
            if (key == fraction) {
                continue;
            }
            long count = entry.getValue() == null ? 0 : entry.getValue();
            long diff = playerFactionCount - count;
            if (diff > 5) {
                return false;
            }
        }
        return true;
    }

    public synchronized boolean unregisterPlayer(Player player) {
        if (player == null) {
            return false;
        }
        if (status != PvpArenaStatusType.REGISTRATION) {
            return false;
        }
        player.removeEvent(this);
        player.removeListener(PvpArenaListenRegistered.getInstance());
        registeredPlayers.remove(player.getObjectId());
        return true;
    }

    private List<PvpArenaPlayer> teleportPlayers(List<PvpArenaPlayer> list, List<Location> points) {
        return list.stream()
                .map(p -> {
                    Player player = p.getPlayer();
                    if (player == null) {
                        return p;
                    }
                    final boolean teleport = EventService.getInstance().teleportPlayerToRandomPoint(player, points, getReflection());
                    if (!teleport) {
                        return p;
                    } else {
                        EventService.getInstance().setPlayerStatusFromEvent(player, true, true);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public boolean canUseTeleport(Player player) {
        return !(status.ordinal() >= PvpArenaStatusType.BEGINNING.ordinal());
    }

    @Override
    public boolean canUseCommunityFunctions(Player player) {
        return !(status.ordinal() >= PvpArenaStatusType.BEGINNING.ordinal());
    }

    @Override
    public boolean canJoinParty(Player inviter, Player invited) {
        return !(status.ordinal() >= PvpArenaStatusType.BEGINNING.ordinal());
    }

    @Override
    public void onDie(Player actor, Creature killer) {
        Player killerPlayer = killer.getPlayer();
        if (killerPlayer == null) {
            return;
        }

        PvpArenaFactionTeam killerFactionTeam = teamMap.get(killerPlayer.getFraction());
        PvpArenaFactionTeam actorFactionTeam = teamMap.get(actor.getFraction());
        if (killerFactionTeam == null || actorFactionTeam == null) {
            return;
        }
        if (!isInProgress()) {
            return;
        }
        if (!killerFactionTeam.isParticipant(killerPlayer.getObjectId()) || !actorFactionTeam.isParticipant(actor.getObjectId())) {
            return;
        }
        if (actor.isMyServitor(killer.getObjectId()) || killerPlayer == actor) {
            return;
        }
        if (killer.isServitor() && (killer = killer.getPlayer()) == null) {
            return;
        }
        if (killer.isPlayer() || killer instanceof FakePlayer) {
            Player assistant = actor.getDamageList().getAssistant(killerPlayer);
            long currentTime = System.currentTimeMillis();
            var debuffers = actor.getAbnormalList().getDebuffs().entrySet().stream()
                    .filter(entry -> currentTime - entry.getValue() <= TimeUnit.SECONDS.toMillis(60))
                    .map(entry -> entry.getKey().getEffector())
                    .map(Creature::getPlayer)
                    .distinct()
                    .filter(player -> player != null
                            && !Objects.equals(player, actor)
                            && !Objects.equals(player, killerPlayer)
                            && !Objects.equals(player, assistant)
                    )
                    .collect(Collectors.toUnmodifiableList());
            GveRewardManager.getInstance().tryGiveReward(killerPlayer, assistant, debuffers, actor, adenaMod);
            actor.setPkKills(actor.getPkKills() + 1);
            killerPlayer.setPvpKills(killerPlayer.getPvpKills() + 1);
            killerPlayer.sendChanges();
        }
        List<Servitor> servitors = actor.getServitors();
        for (Servitor servitor : servitors) {
            servitor.abortAttack(true, false);
            servitor.abortCast(true, false);
        }
        EventService.getInstance().setFakeDeath(actor);
        killerFactionTeam.incrementAllKillCount();
        long seconds = timeResurrectionSeconds.toSeconds();
        actor.sendPacket(new SystemMessagePacket(SystemMsg.RESURRECTION_WILL_TAKE_PLACE_IN_THE_WAITING_ROOM_AFTER_S1_SECONDS).addNumber(seconds));
        Future<?> future = ressurectTaskMap.put(actor.getObjectId(), ThreadPoolManager.getInstance().schedule(new RessurectTask(actor), seconds, TimeUnit.SECONDS));
        if (future != null) {
            future.cancel(true);
        }
        int waterCount = killerFactionTeam.getFraction() == Fraction.WATER ? killerFactionTeam.getAllKillCount() : actorFactionTeam.getAllKillCount();
        int fireCount = killerFactionTeam.getFraction() == Fraction.FIRE ? killerFactionTeam.getAllKillCount() : actorFactionTeam.getAllKillCount();
        actor.sendMessage(new CustomMessage("pvp.arena.s14").addNumber(waterCount).addNumber(fireCount));
    }

    @Override
    public boolean canUseSkill(Creature caster, Creature target, Skill skill) {
        if (status.ordinal() >= PvpArenaStatusType.BEGINNING.ordinal()) {
            if (caster == null || target == null) {
                return true;
            }
            if (target.isUndyingFlag() || caster.isUndyingFlag()) {
                return false;
            }
            if (caster.getObjectId() != target.getObjectId() && skill.getSkillType().isHeal()) {
                return false;
            }
            if (ArrayUtils.contains(disableSkills, skill.getId())) {
                return false;
            }
            if (caster.getObjectId() != target.getObjectId() && ArrayUtils.contains(selfUseSkills, skill.getId())) {
                return false;
            }
        }
        return super.canUseSkill(caster, target, skill);
    }

    @Override
    public Reflection getReflection() {
        return reflection;
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

    public void logoutOrTeleportFromBattle(Player player, boolean checkCompletion) {
        if (player == null) {
            return;
        }
        if (!isInProgress()) {
            return;
        }
        Fraction fraction = player.getFraction();
        PvpArenaFactionTeam pvpArenaFactionTeam = teamMap.get(fraction);
        if (pvpArenaFactionTeam == null) {
            return;
        }
        PvpArenaPlayer pvpArenaPlayer = pvpArenaFactionTeam.removeArenaPlayer(player.getObjectId());
        if (pvpArenaPlayer != null) {
            SnapshotPlayer snapshotPlayer = pvpArenaPlayer.getSnapshotPlayer();
            if (pvpArenaPlayer.getPlayer().isUndyingFlag()) {
                EventService.getInstance().removeFakeDeath(player);
            }
            EventService.getInstance().restorePlayerAndTeleportToBack(player, snapshotPlayer);
            if (checkCompletion) {
                checkCompletionFromTeam();
            }
            player.removeEvent(this);
        }
    }

    private void checkCompletionFromTeam() {
        long count = teamMap.values().stream().filter(p -> p.getPvpArenaPlayers().size() > 0).count();
        if (count <= 1) {
            action("endBattle", false);
        }
    }

    @Override
    public SystemMsg checkForAttack(Creature target, Creature attacker, Skill skill, boolean force) {
        if (isInProgress() && target.isUndyingFlag()) {
            return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
        }
        return super.checkForAttack(target, attacker, skill, force);
    }

    @Override
    public SystemMsg canUseItem(Player player, ItemInstance item) {
        if (isInProgress()) {
            if (item.getItemId() == 5592 || item.getItemId() == 75044) {
                return SystemMsg.THIS_IS_NOT_A_SUITABLE_ITEM;
            }
        }
        return super.canUseItem(player, item);
    }

    public void teleportPlayerToArena(Player player) {
        if (player == null) {
            return;
        }
        if (!isInProgress()) {
            return;
        }
        if (currentArena == null) {
            return;
        }
        List<Location> points = currentArena.getPoints();
        Reflection reflection = getReflection();
        Fraction fraction = player.getFraction();
        PvpArenaFactionTeam pvpArenaFactionTeam = teamMap.get(fraction);
        if (pvpArenaFactionTeam == null) {
            return;
        }
        if (pvpArenaFactionTeam.isParticipant(player.getObjectId()) || reflection == null) {
            return;
        }
        final boolean teleport = EventService.getInstance().teleportPlayerToRandomPoint(player, points, reflection);
        if (!teleport) {
            logoutOrTeleportFromBattle(player, true);
        }
    }

    private void ressurect(Player player) {
        if (player == null) {
            return;
        }
        if (!isInProgress() || status != PvpArenaStatusType.BATTLE) {
            return;
        }
        Fraction fraction = player.getFraction();
        PvpArenaFactionTeam pvpArenaFactionTeam = teamMap.get(fraction);
        if (pvpArenaFactionTeam == null) {
            return;
        }
        if (!pvpArenaFactionTeam.isParticipant(player.getObjectId())) {
            return;
        }
        if (currentArena == null) {
            return;
        }
        List<Location> points = currentArena.getPoints();
        Reflection reflection = getReflection();
        EventService.getInstance().teleportPlayerToRandomPoint(player, points, reflection);
        EventService.getInstance().cpHpMpHeal(player);
        if (player.isUndyingFlag()) {
            player.setUndying(SpecialEffectState.TRUE);
        }
        EventService.getInstance().removeFakeDeath(player);
        player.dispelDebuffs();
    }

    private void removeBlockAllPlayers() {
        teamMap.values().stream()
                .flatMap(i -> i.getPvpArenaPlayers().stream())
                .map(PvpArenaPlayer::getPlayer)
                .filter(Objects::nonNull)
                .forEach(p -> {
                    p.stopMoveBlock();
                    EventService.getInstance().removeInvisible(p, true);
                });
    }

    private ItemHolder[] parseRewards(String rewards) {
        return rewards.isEmpty()
                ? EMPTY_ITEM_ID_COUNT_HOLDER
                : Arrays.stream(rewards.split(";")).map(ItemHolder::new).toArray(ItemHolder[]::new);
    }

    public void onEnterZone(Player player) {
        if (player == null) {
            return;
        }
        Fraction fraction = player.getFraction();
        PvpArenaFactionTeam pvpArenaFactionTeam = teamMap.get(fraction);
        if (pvpArenaFactionTeam == null) {
            return;
        }
        if (!pvpArenaFactionTeam.isParticipant(player.getObjectId())) {
            player.setReflection(ReflectionManager.MAIN);
            player.teleToClosestTown();
        }
    }

    private static class RessurectTask implements Runnable {
        private final Player player;

        public RessurectTask(Player player) {
            this.player = player;
        }

        @Override
        public void run() {
            PvpArenaEvent event = EventHolder.getInstance().getEvent(EventType.CUSTOM_PVP_EVENT, 1002);
            if (event == null) {
                return;
            }
            event.ressurect(player);
        }
    }

    public static class ItemHolder {
        private final int id;
        private final long count;

        public ItemHolder(int id, long count) {
            this.id = id;
            this.count = count;
        }

        public ItemHolder(String str) {
            String[] split = str.split(":");
            id = Integer.parseInt(split[0]);
            count = Integer.parseInt(split[1]);
        }

        public int getId() {
            return id;
        }

        public long getCount() {
            return count;
        }
    }
}
