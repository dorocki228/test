package events;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.MultimapBuilder;
import l2s.commons.collections.MultiValueSet;
import l2s.commons.lang.reference.HardReference;
import l2s.commons.threading.RunnableImpl;
import l2s.commons.time.cron.SchedulingPattern;
import l2s.commons.util.Rnd;
import l2s.gameserver.Announcements;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.listener.actor.OnCurrentHpDamageListener;
import l2s.gameserver.listener.actor.OnDeathListener;
import l2s.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2s.gameserver.model.*;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.base.RestartType;
import l2s.gameserver.model.bbs.EventTeleportationCommunityBoardEntry;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.EventCustomReflection;
import l2s.gameserver.model.entity.events.EventRestartLoc;
import l2s.gameserver.model.entity.events.EventType;
import l2s.gameserver.model.entity.events.objects.SpawnExObject;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.anticheat.AAScreenStringPacketPresets;
import l2s.gameserver.service.BroadcastService;
import l2s.gameserver.service.ConfrontationService;
import l2s.gameserver.templates.item.data.ItemData;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.time.counter.TimeCounter;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.NpcUtils;
import l2s.gameserver.utils.ReflectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Java-man
 * @since 28.03.2018
 */
public class BossSpawnEvent extends Event {
    private static final String BOSS = "boss";

    private static final Zone RESPAWN_ZONE = ReflectionUtils.getZone("[gve_epic_loft]");

    private final OnDeathListenerImpl deathListener = new OnDeathListenerImpl();
    private final OnZoneEnterLeaveListener zoneListener = new OnZoneEnterLeaveListenerImpl();
    private final OnZoneEnterLeaveListener respawnZoneListener = new OnRespawnZoneEnterLeaveListenerImpl();
    private final OnCurrentHpDamageListenerImpl damageListener = new OnCurrentHpDamageListenerImpl();

    private final SchedulingPattern pattern;
    private final ListMultimap<DayOfWeek, BossInfo> bosses;

    private Instant startTime;
    private boolean isInProgress;

    private ScheduledFuture<?> teleportTask;

    private BossInfo currentBossInfo;

    private final Set<HardReference<Player>> participants;

    private Fraction winner;
    private final Map<BossType, EventCustomReflection> reflections = new HashMap<>();
    private final Map<Integer, NpcInstance> currentBosses = new ConcurrentHashMap<>();
    private final Map<Integer, Integer> playerPriceSnapshotMap = new ConcurrentHashMap<>();
    private final AtomicReference<BossStatus> bossStatus = new AtomicReference<>(BossStatus.NONE);
    private ScheduledFuture<?> stopTask;

    public BossSpawnEvent(MultiValueSet<String> set) {
        super(set);

        bosses = MultimapBuilder.enumKeys(DayOfWeek.class).arrayListValues().build();
        participants = new CopyOnWriteArraySet<>();

        pattern = new SchedulingPattern(set.getString("pattern"));
        Arrays.stream(DayOfWeek.values())
                .forEach(dayOfWeek -> {
                    String string = set.getString("bosses_" + dayOfWeek.name().toLowerCase(), StringUtils.EMPTY);
                    Arrays.stream(string.split("\\|"))
                            .forEach(s -> bosses.put(dayOfWeek, new BossInfo(s.split(";"))));
                });

        startTime = pattern.next(Instant.now());

        DayOfWeek dayOfWeek = ZonedDateTime.ofInstant(startTime, ZoneId.systemDefault()).getDayOfWeek();
        currentBossInfo = Rnd.get(bosses.get(dayOfWeek));
    }

    @Override
    public void reCalcNextTime(boolean onInit) {
        RESPAWN_ZONE.removeListener(respawnZoneListener);
        playerPriceSnapshotMap.clear();
        clearActions();

        startTime = pattern.next(Instant.now());

        DayOfWeek dayOfWeek = ZonedDateTime.ofInstant(startTime, ZoneId.systemDefault()).getDayOfWeek();
        currentBossInfo = Rnd.get(bosses.get(dayOfWeek));
        if (currentBossInfo == null) {
            error("Can't find bosses for " + dayOfWeek + " day of week.", new IllegalArgumentException());
        }
        initReflections();
        bossStatus.set(BossStatus.NONE);
        reflections.values().forEach(it -> {
            Zone zone = it.getZone(currentBossInfo.zone.getName());
            zone.addEvent(this);
        });
        registerActions();

        if (!onInit) {
            printInfo();
        }
    }

    private void initReflections() {
        reflections.clear();
        for (int i = 0; i < BossType.values().length; i++) {
            BossType type = BossType.values()[i];
            EventCustomReflection eventCustomReflection = new EventCustomReflection(1003);
            reflections.put(type, eventCustomReflection);
        }
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
    public Optional<String> getOnScreenMessage(Player player) {
        if (!isInProgress()) {
            return Optional.empty();
        }

        return Optional.of(currentBossInfo.getName() + " [Event]");
    }

    @Override
    public boolean isInProgress() {
        return isInProgress;
    }

    @Override
    public void startEvent() {
        playerPriceSnapshotMap.clear();
        if (stopTask != null) {
            stopTask.cancel(false);
            stopTask = null;
        }
        TimeCounter.INSTANCE.start(this, "reward");
        RESPAWN_ZONE.addListener(respawnZoneListener);

        super.startEvent();
        var delay = 2;
        bossStatus.set(BossStatus.ALIVE);
        teleportTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Teleport(), delay, delay, TimeUnit.MINUTES);
        isInProgress = true;

        String visibleName = _name + " [" + currentBossInfo.getName() + ']';
        addCommunityBoardEntry("teleport", new EventTeleportationCommunityBoardEntry(this, visibleName));
    }

    @Override
    public void stopEvent(boolean force) {
        RESPAWN_ZONE.removeListener(respawnZoneListener);
        playerPriceSnapshotMap.clear();

        removeCommunityBoardEntry("teleport");

        isInProgress = false;

        if (teleportTask != null) {
            teleportTask.cancel(true);
        }

        var players = participants.stream()
                .map(HardReference::get)
                .filter(Objects::nonNull)
                .filter(p -> p.getFraction() == winner)
                .filter(p -> p.getLevel() >= Config.FACTION_WAR_MIN_LEVEL)
                .collect(Collectors.toUnmodifiableList());

        if (bossStatus.get() == BossStatus.DEAD) {
            ConfrontationService.getInstance().bossKill(players);
        }

        participants.forEach(playerRef -> {
            Player player = playerRef.get();
            if (player == null) {
                return;
            }
            player.removeEvent(this);
        });
        participants.clear();

        var elapsedTimeMap = TimeCounter.INSTANCE.stop(this, "reward");
        elapsedTimeMap.forEach(playerWithTime ->
                playerWithTime.ifPlayerSpendEnoughTimeOrElse(Duration.ofSeconds(1),
                        player -> {
                            rewardPlayer(player);
                            _log.info("Reward sent to player " + player.getPlayerObjectId());
                            return null;
                        },
                        player -> {
                            player.sendMessage(new CustomMessage("BossSpawnEvent.reward.time"));
                            return null;
                        })
        );

        winner = Fraction.NONE;
        reflections.values().forEach(it -> {
            Zone zone = it.getZone(currentBossInfo.zone.getName());
            if (zone != null) {
                zone.setActive(false);
                zone.removeListener(zoneListener);
            }
            it.startCollapse(60_000 * 2);
        });
        reflections.clear();
        currentBosses.clear();
        if (!force) {
            reCalcNextTime(false);
        }

        super.stopEvent(force);
    }

    private void rewardPlayer(OfflinePlayer player) {
        player.addItem(currentBossInfo.reward.getId(), currentBossInfo.reward.getCount(), "BossSpawnEventWinner");
        player.addItem(57, 350, "BossSpawnEventParticipation");
        player.sendMessage("You received a reward for participating in the event.");
    }

    @Override
    public void teleportPlayerToEvent(Player player) {
        player.teleToLocation(new Location(50856, -12232, -9384), getReflection());
    }

    @Override
    public void spawnAction(Object name, int delay, boolean spawn) {
        if (Objects.equals(name, BOSS)) {
            if (spawn) {
                currentBosses.values().forEach(GameObject::deleteMe);
                reflections.values().forEach(it -> {
                    Zone zone = it.getZone(currentBossInfo.zone.getName());
                    if (zone != null) {
                        zone.setActive(true);
                        zone.addListener(zoneListener);
                    }
                    NpcInstance npc = NpcUtils.spawnSingle(currentBossInfo.getId(), currentBossInfo.bossSpawnLocation, it);
                    npc.addEvent(this);
                    npc.addListener(damageListener);
                    currentBosses.put(npc.getObjectId(), npc);
                });
            } else {
                currentBosses.values().forEach(GameObject::deleteMe);
            }
        } else {
            super.spawnAction(name, delay, spawn);
        }
    }

    @Override
    public void onAddEvent(GameObject o) {
        if (o.isNpc() && ((Creature) o).getNpcId() == currentBossInfo.getId()) {
            ((Creature) o).addListener(deathListener);
            ((Creature) o).addListener(damageListener);
        }

        if (o.isPlayer()) {
            Player player = o.getPlayer();
            participants.add(player.getRef());
            TimeCounter.INSTANCE.addPlayer(this, "reward", player);
        }
    }

    @Override
    public void onRemoveEvent(GameObject o) {
        if (o.isNpc() && ((Creature) o).getNpcId() == currentBossInfo.getId()) {
            ((Creature) o).removeListener(deathListener);
            ((Creature) o).removeListener(damageListener);
        }

        if (o.isPlayer()) {
            Player player = o.getPlayer();
            TimeCounter.INSTANCE.removePlayer(this, "reward", player);
            participants.remove(player.getRef());
        }
    }

    @Override
    public void announce(int val, SystemMsg msgId) {
        if (val < 0) {
            int minutes = Math.abs(val / 60);
            Announcements.announceToAll("Epic Raid Boss " + currentBossInfo.getName() + " will be spawned in "
                    + minutes + " minutes.");
        } else if (val == 0) {
            Announcements.announceToAll("Epic Raid Boss " + currentBossInfo.getName()
                    + " was spawned in world. You have 60 minutes to kill him.");
            L2GameServerPacket packet = AAScreenStringPacketPresets.ANNOUNCE
                    .addOrUpdate("Epic Boss " + currentBossInfo.getName() + " was spawned in the world.");
            BroadcastService.getInstance().sendToAll(packet);
        } else if (val == 3600) {
            if (bossStatus.get() == BossStatus.DEAD) {
                return;
            }
            Announcements.announceToAll("Epic Boss " + currentBossInfo.getName()
                    + " was not killed. The event is over.");
        }
    }

    @Override
    public boolean ifVar(String name) {
        if ("teleporter_spawned".equals(name)) {
            SpawnExObject spawn = getFirstObject("teleporter");
            return spawn.isSpawned();
        }

        return super.ifVar(name);
    }

    @Override
    public void checkRestartLocs(Player player, Map<RestartType, Boolean> r) {
        r.put(RestartType.FIXED, true);
    }

    @Override
    public EventRestartLoc getRestartLoc(Player player, RestartType type) {
        if (type == RestartType.FIXED) {
            return new EventRestartLoc(RESPAWN_ZONE.getTerritory().getRandomLoc(player.getGeoIndex()), getReflection());
        } else {
            return null;
        }
    }

    @Override
    public boolean canResurrect(Creature active, Creature target, boolean force, boolean quiet) {
        boolean targetInZone = checkIfInZone(active, target);
        // если таргет вне осадный зоны - рес разрешен
        if (!targetInZone) {
            return true;
        }

        if (active.getFraction().canAttack(target.getFraction())) {
            if (!quiet) {
                active.sendPacket(SystemMsg.INVALID_TARGET);
            }
            return false;
        }

        Player activePlayer = active.getPlayer();
        Player targetPlayer = target.getPlayer();

        // если оба незареганы - невозможно ресать
        // если таргет незареган - невозможно ресать
        BossSpawnEvent event1 = activePlayer.getEvent(BossSpawnEvent.class);
        BossSpawnEvent event2 = targetPlayer.getEvent(BossSpawnEvent.class);
        if (!Objects.equals(event1, this) || !Objects.equals(event2, this)) {
            if (!quiet) {
                active.sendPacket(SystemMsg.INVALID_TARGET);
            }
            return false;
        }

        if (force) {
            return true;
        } else {
            if (!quiet) {
                active.sendPacket(SystemMsg.INVALID_TARGET);
            }
            return false;
        }
    }

    public boolean checkIfInZone(Creature active, Creature target) {
        BossInfo bossInfo = this.currentBossInfo;
        if (bossInfo == null) {
            return false;
        }
        return reflections.values().stream().anyMatch(it -> {
            Zone zone = it.getZone(bossInfo.zone.getName());
            if (zone == null) {
                return false;
            }
            return zone.checkIfInZone(active) && zone.checkIfInZone(target);
        });
    }

    public Zone getZone(Reflection reflection, String name) {
        if (reflection == null || name == null) {
            return null;
        }
        BossInfo bossInfo = this.currentBossInfo;
        if (bossInfo == null) {
            return null;
        }

        return reflections.values().stream()
                .filter(p -> p.getId() == reflection.getId())
                .findFirst()
                .map(r -> r.getZone(name))
                .orElse(null);
    }

    private class Teleport implements Runnable {
        private Teleport() {
        }

        @Override
        public void run() {
            List<Playable> list = RESPAWN_ZONE.getInsidePlayables();
            List<Playable> allPlayers = Arrays.stream(BossType.values())
                    .flatMap(e -> {
                        EventCustomReflection eventCustomReflection = reflections.get(e);
                        if (eventCustomReflection == null) {
                            return Stream.empty();
                        }
                        List<Playable> l = eventCustomReflection.getZones().stream()
                                .flatMap(z -> z.getInsidePlayables().stream().filter(GameObject::isPlayer).collect(Collectors.toList()).stream())
                                .collect(Collectors.toList());
                        return l.stream();
                    }).collect(Collectors.toList());
            if ((list.size() + allPlayers.size()) <= 12) {
                BossType top = BossType.TOP;
                EventCustomReflection eventCustomReflection = reflections.get(top);
                if (eventCustomReflection == null) {
                    return;
                }
                list.stream()
                        .map(GameObject::getPlayer)
                        .filter(Objects::nonNull)
                        .filter(it -> !it.isLogoutStarted())
                        .forEach(it -> teleport(it, eventCustomReflection));
                return;
            }
            Map<Integer, Playable> playableMap = list.stream().collect(Collectors.toMap(GameObject::getObjectId, Function.identity()));
            List<PlayerPrice> playersPrices = playersPrices(list, playableMap);
            if (playersPrices.isEmpty()) {
                return;
            }

            int chunkSize = (int) Math.ceil(playersPrices.size() / (double) BossType.values().length);
            List<List<PlayerPrice>> partition = Lists.partition(playersPrices, chunkSize);
            for (int i = 0; i < BossType.values().length; i++) {
                int size = partition.size();
                if (i >= size) {
                    break;
                }
                List<PlayerPrice> playerPrices = partition.get(i);
                BossType type = BossType.values()[i];
                EventCustomReflection eventCustomReflection = reflections.get(type);
                if (eventCustomReflection == null) {
                    continue;
                }
                playerPrices.stream()
                        .flatMap(it -> it.getObjectIds().stream())
                        .map(playableMap::get)
                        .filter(Objects::nonNull)
                        .map(GameObject::getPlayer)
                        .filter(Objects::nonNull)
                        .filter(it -> !it.isLogoutStarted())
                        .forEach(it -> teleport(it, eventCustomReflection));
            }
        }

        private List<PlayerPrice> playersPrices(List<Playable> list, Map<Integer, Playable> playableMap) {
            return list
                    .stream()
                    .map(playable -> {
                        if (!playable.isPlayer()) {
                            return null;
                        }
                        Player player = playable.getPlayer();
                        Party party = player.getParty();
                        List<Integer> players = new ArrayList<>();
                        int priceSum = 0;
                        if (party == null) {
                            players.add(player.getObjectId());
                            priceSum += playerPriceSnapshotMap.getOrDefault(player.getObjectId(), player.getAdenaReward());
                        } else {
                            for (Player partyMember : party.getPartyMembers()) {
                                if (partyMember == null) {
                                    continue;
                                }
                                if (playableMap.containsKey(partyMember.getObjectId())) {
                                    priceSum += playerPriceSnapshotMap.getOrDefault(player.getObjectId(), partyMember.getAdenaReward());
                                    players.add(partyMember.getObjectId());
                                }
                            }
                        }
                        return new PlayerPrice(players, priceSum / players.size());
                    })
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparingInt(PlayerPrice::getPrice).reversed())
                    .collect(Collectors.toList());
        }

        private void teleport(Playable playable, Reflection reflection) {
            BossInfo bossInfo = BossSpawnEvent.this.currentBossInfo;
            if (bossInfo == null) {
                return;
            }
            Fraction fraction = playable.getFraction();
            Location location = Rnd.get(bossInfo.playerTeleportLocations.get(fraction));
            if (location == null) {
                return;
            }
            location = Location.coordsRandomize(location, 0, 50);
            playable.teleToLocation(location, reflection);
        }
    }

    private class OnDeathListenerImpl implements OnDeathListener {
        @Override
        public void onDeath(Creature victim, Creature killer) {
            BossInfo bossInfo = currentBossInfo;
            if (bossInfo == null) {
                return;
            }

            if (bossStatus.compareAndSet(BossStatus.ALIVE, BossStatus.DEAD)) {
                var fraction = killer.getFraction();
                winner = fraction;
                Announcements.announceToAll("Epic Raid Boss " + bossInfo.getName()
                        + " was killed by " + fraction + ". Congratulations to all participants!");
                for (Map.Entry<Integer, NpcInstance> entry : currentBosses.entrySet()) {
                    if (victim.getObjectId() != entry.getKey()) {
                        NpcInstance npc = entry.getValue();
                        if (npc.isDead()) {
                            continue;
                        }
                        npc.doDie(killer);
                    }
                }
                stopEvent(false);
            }
        }
    }

    private class OnCurrentHpDamageListenerImpl implements OnCurrentHpDamageListener {
        @Override
        public void onCurrentHpDamage(Creature actor, double damage, Creature attacker, Skill p3, boolean sharedDamage) {
            if (attacker == null || sharedDamage) {
                return;
            }
            if (bossStatus.get() == BossStatus.ALIVE) {
                int objectId = actor.getObjectId();
                for (Map.Entry<Integer, NpcInstance> entry : currentBosses.entrySet()) {
                    if (entry.getKey() != objectId) {
                        NpcInstance npc = entry.getValue();
                        if (npc.isDead()) {
                            continue;
                        }
                        npc.reduceCurrentHp(
                                damage,
                                attacker,
                                null,
                                false,
                                false,
                                false,
                                false,
                                false,
                                false,
                                false,
                                false,
                                false,
                                false,
                                false,
                                false,
                                true
                        );
                    }
                }
            }
        }
    }

    private class OnZoneEnterLeaveListenerImpl implements OnZoneEnterLeaveListener {
        @Override
        public void onZoneEnter(Zone zone, Creature creature) {
            if (creature.isPlayer() && !creature.containsEvent(BossSpawnEvent.this)) {
                creature.addEvent(BossSpawnEvent.this);
            }
        }

        @Override
        public void onZoneLeave(Zone zone, Creature creature) {
            if (creature.isPlayer() && creature.containsEvent(BossSpawnEvent.this)) {
                creature.removeEvent(BossSpawnEvent.this);
            }
        }
    }

    private class OnRespawnZoneEnterLeaveListenerImpl implements OnZoneEnterLeaveListener {
        @Override
        public void onZoneEnter(Zone zone, Creature creature) {
            if (creature == null || !creature.isPlayer()) {
                return;
            }
            Player player = creature.getPlayer();
            if (player == null) {
                return;
            }
            playerPriceSnapshotMap.put(player.getObjectId(), player.getAdenaReward());
        }

        @Override
        public void onZoneLeave(Zone zone, Creature creature) {
            if (creature == null) {
                return;
            }
            playerPriceSnapshotMap.remove(creature.getObjectId());
        }
    }

    private static class BossInfo {
        private final Zone zone;
        private final Location bossSpawnLocation;
        private final ItemData reward;
        private final ImmutableListMultimap<Fraction, Location> playerTeleportLocations;

        private final NpcTemplate template;

        public BossInfo(String[] str) {
            int npcId = Integer.parseInt(str[0]);
            template = NpcHolder.getInstance().getTemplate(npcId);
            zone = ReflectionUtils.getZone(str[1]);
            bossSpawnLocation = Location.parseLoc(str[2]);
            reward = new ItemData(str[3]);
            playerTeleportLocations = createPlayerTeleportLocations(zone);
        }

        public String getName() {
            return template.getName();
        }

        public int getId() {
            return template.getId();
        }

        private static ImmutableListMultimap<Fraction, Location> createPlayerTeleportLocations(Zone zone) {
            var result = ImmutableListMultimap.<Fraction, Location>builder();

            switch (zone.getName()) {
                case "[antharas_p]":
                    result.put(Fraction.FIRE, new Location(180344, 112616, -7704));
                    result.put(Fraction.FIRE, new Location(179416, 111960, -7704));
                    result.put(Fraction.FIRE, new Location(178200, 111880, -7704));
                    result.put(Fraction.FIRE, new Location(176488, 111736, -7704));
                    result.put(Fraction.WATER, new Location(175688, 117272, -7704));
                    result.put(Fraction.WATER, new Location(177240, 117784, -7704));
                    result.put(Fraction.WATER, new Location(178376, 117992, -7704));
                    result.put(Fraction.WATER, new Location(179464, 118024, -7704));
                    return result.build();
                case "[valakas_p]":
                    result.put(Fraction.FIRE, new Location(210824, -112376, -1616));
                    result.put(Fraction.FIRE, new Location(210584, -113080, -1600));
                    result.put(Fraction.FIRE, new Location(210376, -113976, -1600));
                    result.put(Fraction.FIRE, new Location(210280, -114904, -1616));
                    result.put(Fraction.WATER, new Location(214904, -116888, -1632));
                    result.put(Fraction.WATER, new Location(215016, -116152, -1632));
                    result.put(Fraction.WATER, new Location(215048, -115304, -1632));
                    result.put(Fraction.WATER, new Location(215160, -114344, -1632));
                    return result.build();
                case "[antqueen_p]":
                    result.put(Fraction.FIRE, new Location(-21560, 179192, -5832));
                    result.put(Fraction.FIRE, new Location(-21912, 179272, -5832));
                    result.put(Fraction.FIRE, new Location(-21240, 179319, -5832));
                    result.put(Fraction.FIRE, new Location(-21528, 178840, -5832));
                    result.put(Fraction.WATER, new Location(-21688, 185688, -5600));
                    result.put(Fraction.WATER, new Location(-21464, 185880, -5600));
                    result.put(Fraction.WATER, new Location(-21928, 185672, -5600));
                    result.put(Fraction.WATER, new Location(-21288, 186152, -5600));
                    return result.build();
                case "[baium_p]":
                    result.put(Fraction.FIRE, new Location(113144, 17656, 10080));
                    result.put(Fraction.FIRE, new Location(112920, 17288, 10080));
                    result.put(Fraction.FIRE, new Location(113464, 17816, 10080));
                    result.put(Fraction.FIRE, new Location(113303, 17496, 10080));
                    result.put(Fraction.WATER, new Location(116312, 14760, 10080));
                    result.put(Fraction.WATER, new Location(116008, 14488, 10080));
                    result.put(Fraction.WATER, new Location(116072, 14712, 10080));
                    result.put(Fraction.WATER, new Location(116264, 14520, 10080));
                    return result.build();
                case "[orfen_p]":
                    result.put(Fraction.FIRE, new Location(44840, 19272, -4200));
                    result.put(Fraction.FIRE, new Location(45160, 19112, -4264));
                    result.put(Fraction.FIRE, new Location(45448, 18984, -4240));
                    result.put(Fraction.FIRE, new Location(45232, 19627, -4312));
                    result.put(Fraction.WATER, new Location(45928, 17304, -4320));
                    result.put(Fraction.WATER, new Location(45896, 16808, -4304));
                    result.put(Fraction.WATER, new Location(46435, 17199, -4352));
                    result.put(Fraction.WATER, new Location(46728, 17288, -4392));
                    return result.build();
                case "[core_p]":
                    result.put(Fraction.FIRE, new Location(19128, 110296, -6440));
                    result.put(Fraction.FIRE, new Location(19128, 110040, -6440));
                    result.put(Fraction.FIRE, new Location(18776, 110056, -6440));
                    result.put(Fraction.FIRE, new Location(18760, 110296, -6440));
                    result.put(Fraction.WATER, new Location(16680, 110056, -6440));
                    result.put(Fraction.WATER, new Location(16680, 110280, -6440));
                    result.put(Fraction.WATER, new Location(16360, 110264, -6440));
                    result.put(Fraction.WATER, new Location(16376, 110040, -6440));
                    return result.build();
                default:
                    throw new IllegalArgumentException("Can't find zone " + zone.getName());
            }
        }
    }

    enum BossStatus {
        NONE,
        ALIVE,
        DEAD
    }

    enum BossType {
        TOP,
        MID,
        LOW
    }

    private static class PlayerPrice {
        private final int price;
        private final List<Integer> objectIds;

        public PlayerPrice(List<Integer> objectIds, int price) {
            this.objectIds = objectIds;
            this.price = price;
        }

        public List<Integer> getObjectIds() {
            return objectIds;
        }

        public int getPrice() {
            return price;
        }
    }
}
