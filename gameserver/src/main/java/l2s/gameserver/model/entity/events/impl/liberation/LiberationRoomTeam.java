package l2s.gameserver.model.entity.events.impl.liberation;

import l2s.commons.threading.RunnableImpl;
import l2s.gameserver.instancemanager.SpawnManager;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Spawner;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.EventPlayer;
import l2s.gameserver.model.entity.events.impl.liberation.listeners.LiberationOnDeathListenerImpl;
import l2s.gameserver.model.entity.events.impl.service.EventService;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.taskmanager.CommonTaskManager;
import l2s.gameserver.utils.Location;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class LiberationRoomTeam {
    private final Fraction fraction;
    private final Map<Integer, EventPlayer> playerMap;
    private final AtomicInteger points = new AtomicInteger(0);
    private final Reflection reflection;
    private final int maxWave;
    private final Duration spawnTimeNextWave;
    private final Duration spawnTimeWaveAfterTeleport;
    private int waveNumber = 0;
    private long lastKillTimestamp = 0;
    private boolean spawned = false;
    private volatile boolean close;
    private List<NpcInstance> currentSpawns = Collections.emptyList();
    private final List<Future<?>> tasks = new ArrayList<>();

    public LiberationRoomTeam(
            Reflection reflection,
            Fraction fraction,
            Map<Integer, EventPlayer> playerMap,
            int maxWave,
            Duration spawnTimeNextWave,
            Duration spawnTimeWaveAfterTeleport
    ) {
        this.reflection = reflection;
        this.fraction = fraction;
        this.playerMap = new ConcurrentHashMap<>(playerMap);
        this.maxWave = maxWave;
        this.spawnTimeNextWave = spawnTimeNextWave;
        this.spawnTimeWaveAfterTeleport = spawnTimeWaveAfterTeleport;
    }

    public void nextWave(Event event) {
        if (close)
            return;
        spawned = false;
        int oldWave = waveNumber;
        despawnWave(oldWave);
        currentSpawns.clear();
        currentSpawns = Collections.emptyList();
        waveNumber++;
        scheduleWaveTasks(spawnTimeNextWave, 5, "liberation.s15");
        tasks.add(CommonTaskManager.getInstance().schedule(new RunnableImpl() {
            @Override
            protected void runImpl() {
                spawnWave(event, waveNumber);
            }
        }, spawnTimeNextWave.toMillis()));
    }

    private void despawnAllWave() {
        for (int i = 1; i <= maxWave; i++) {
            despawnWave(i);
        }
    }

    private void despawnWave(int wave) {
        List<Spawner> spawners = SpawnManager.getInstance().getSpawners(fraction.name().toLowerCase() + "_lotf_wave_" + wave);
        spawners.forEach(Spawner::deleteAll);
    }

    public boolean canNextWave() {
        if (close)
            return false;
        List<NpcInstance> spawns = this.currentSpawns;
        if (spawns == null || spawns.isEmpty()) {
            return false;
        }
        return spawned && waveNumber < maxWave && spawns.stream().allMatch(Creature::isDead);
    }

    public boolean isWavesOver() {
        List<NpcInstance> spawns = this.currentSpawns;
        if (spawns == null || spawns.isEmpty()) {
            return false;
        }
        return spawned && waveNumber == maxWave && spawns.stream().allMatch(Creature::isDead);
    }

    public void startWave(Event event) {
        if (close || waveNumber != 0)
            return;
        waveNumber++;
        scheduleWaveTasks(spawnTimeWaveAfterTeleport, 5, "liberation.s14");
        tasks.add(CommonTaskManager.getInstance().schedule(new RunnableImpl() {
            @Override
            protected void runImpl() {
                startWave0(event);
            }
        }, spawnTimeWaveAfterTeleport.toMillis()));
    }

    private RunnableImpl createScreenMessageTask(String message, String... args) {
        return new RunnableImpl() {
            @Override
            protected void runImpl() {
                playerMap.values().stream()
                        .map(EventPlayer::getPlayer)
                        .filter(Objects::nonNull)
                        .forEach(p -> EventService.getInstance().sendScreenCustomMessage(message, args, p));
            }
        };
    }

    private void startWave0(Event event) {
        spawnWave(event, waveNumber);
    }

    private void spawnWave(Event event, int wave) {
        List<Spawner> spawners = SpawnManager.getInstance().getSpawners(fraction.name().toLowerCase() + "_lotf_wave_" + wave);
        List<NpcInstance> npcs = new ArrayList<>();
        for (Spawner spawn : spawners) {
            spawn.addEvent(event);
            spawn.setReflection(reflection);
            List<NpcInstance> list = spawn.initAndReturn();
            list.forEach(n -> n.addListener(LiberationOnDeathListenerImpl.getInstance()));
            npcs.addAll(list);
        }
        npcs.forEach(GameObject::spawnMe);
        currentSpawns = npcs;
        spawned = true;
    }

    public void endEvent(Event event) {
        restorePlayersAndTeleportToBack(event);
        close();
    }

    public void abortEvent(Event event, LiberationStatusType oldStatus) {
        if (oldStatus.ordinal() >= LiberationStatusType.PREPARATION.ordinal()) {
            restorePlayersAndTeleportToBack(event);
        }
        close();
    }

    private void restorePlayersAndTeleportToBack(Event event) {
        getPlayers().forEach(p -> {
            Player player = p.getPlayer();
            if (player != null) {
                player.removeEvent(event);
                EventService.getInstance().restorePlayerAndTeleportToBack(player, p.getSnapshotPlayer());
            }
        });
    }

    private void close() {
        close = true;
        despawnAllWave();
        reflection.collapse();
        tasks.forEach(t -> t.cancel(false));
        tasks.clear();
    }

    public List<EventPlayer> teleportPlayers(List<Location> points, boolean setStatus) {
        return teleportPlayers(playerMap.values(), points, setStatus);
    }

    public List<EventPlayer> teleportPlayers(Collection<EventPlayer> players, List<Location> points, boolean setStatus) {
        return players
                .stream()
                .map(p -> {
                    Player player = p.getPlayer();
                    if (player == null) return p;
                    final boolean teleport = EventService.getInstance().teleportPlayerToRandomPoint(player, points, reflection);
                    if (!teleport) return p;
                    else if (setStatus) {
                        EventService.getInstance().setPlayerStatusFromEvent(player, false, false);
                        return null;
                    } else return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private void scheduleWaveTasks(Duration spawnTimeNextWave, int step, String s) {
        final int actionCount = (int) (Math.max(5, spawnTimeNextWave.toSeconds()) / step);
        for (int i = 0; i < actionCount; i++) {
            final int delay = (i * step) + step;
            final String seconds = "" + (spawnTimeNextWave.toSeconds() - delay);
            if (i == 0) {
                tasks.add(CommonTaskManager.getInstance()
                        .schedule(
                                createScreenMessageTask(s, "" + spawnTimeNextWave.toSeconds()),
                                0
                        ));
            }
            if (i != (actionCount - 1)) {
                tasks.add(CommonTaskManager.getInstance()
                        .schedule(
                                createScreenMessageTask(s, seconds),
                                delay * 1000L
                        ));
            }
        }
    }

    public void setLastKillTimestamp(long lastKillTimestamp) {
        this.lastKillTimestamp = lastKillTimestamp;
    }

    public long getLastKillTimestamp() {
        return lastKillTimestamp;
    }

    public int getPoints() {
        return points.get();
    }

    public Fraction getFraction() {
        return fraction;
    }

    public void incrementPoints() {
        points.incrementAndGet();
    }

    public EventPlayer getPlayer(int objectId) {
        return playerMap.get(objectId);
    }

    public EventPlayer removePlayer(int objectId) {
        return playerMap.remove(objectId);
    }

    public Collection<EventPlayer> getPlayers() {
        return playerMap.values();
    }

    public Reflection getReflection() {
        return reflection;
    }

    public boolean isParticipant(int objectId) {
        return playerMap.containsKey(objectId);
    }
}
