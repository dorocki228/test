package l2s.gameserver.model.entity.events.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import l2s.commons.collections.MultiValueSet;
import l2s.commons.time.cron.SchedulingPattern;
import l2s.commons.util.Rnd;
import l2s.gameserver.Announcements;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.instancemanager.ServerVariables;
import l2s.gameserver.instancemanager.SpawnManager;
import l2s.gameserver.listener.zone.OnZoneTickListener;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.base.RestartType;
import l2s.gameserver.model.bbs.EventTeleportationCommunityBoardEntry;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.EventRestartLoc;
import l2s.gameserver.model.entity.events.EventType;
import l2s.gameserver.model.entity.events.impl.upgrading.UpgradingEventLocation;
import l2s.gameserver.model.entity.events.impl.upgrading.listener.UpgradingEventOnTickListener;
import l2s.gameserver.model.entity.events.objects.SpawnSimpleObject;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.security.HwidUtils;
import l2s.gameserver.service.ConfrontationService;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.utils.Location;

/**
 * @author KRonst
 */
public class UpgradingEvent extends Event {

    private static final int ARTIFACT_ID = 41902;
    private static final String TELEPORT_NAME = "upgrading_event_teleport";
    private static final String LAST_LOCATION = "UPGRADING_EVENT_LAST_LOCATION";
    private final AtomicReference<Fraction> artifactOwner = new AtomicReference<>(Fraction.NONE);
    private final SchedulingPattern pattern;

    private SkillEntry winnerBuff;
    private SkillEntry loserBuff;
    private Instant startTime;
    private boolean isInProgress;
    private UpgradingEventLocation currentLocation = null;
    private OnZoneTickListener artifactCapturedListener = null;
    private SpawnSimpleObject artifact = null;

    public UpgradingEvent(MultiValueSet<String> set) {
        super(set);

        pattern = new SchedulingPattern(set.getString("pattern"));
        initBuffs(set);
    }

    @Override
    public void reCalcNextTime(boolean init) {
        clearActions();
        startTime = pattern.next(Instant.now());
        registerActions();

        if(!init) {
            printInfo();
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
    public void startEvent() {
        super.startEvent();
        initLocation();
        teleportPlayersToTowns();
        initSpawns();

        isInProgress = true;
        addCommunityBoardEntry(TELEPORT_NAME, new EventTeleportationCommunityBoardEntry(this, _name));
    }

    @Override
    public void stopEvent(boolean force) {
        if (artifactOwner.get() == Fraction.NONE) {
            Announcements.announceToAllFromStringHolder("events.upgrading.stop");
        } else {
            Announcements.announceToAllFromStringHolder("events.upgrading.stop.artifact", artifactOwner);
        }
        removeCommunityBoardEntry(TELEPORT_NAME);
        cleanSpawns();
        teleportPlayersToTowns();
        if (artifactCapturedListener != null) {
            currentLocation.getZone().removeListener(artifactCapturedListener);
        }
        currentLocation.getZone().setActive(false);

        artifactOwner.set(Fraction.NONE);
        isInProgress = false;
        if (!force) {
            reCalcNextTime(false);
        }
        super.stopEvent(force);
    }

    @Override
    public boolean isInProgress() {
        return isInProgress;
    }

    @Override
    public Optional<String> getOnScreenMessage(Player player) {
        if (!isInProgress()) {
            return Optional.empty();
        } else {
            return Optional.of(getName());
        }
    }

    @Override
    public void announce(int time, SystemMsg msgId) {
        if(time == 0) {
            Announcements.announceToAllFromStringHolder("events.upgrading.start");
        }
    }

    @Override
    public void checkRestartLocs(Player player, Map<RestartType, Boolean> r) {
        r.put(RestartType.FIXED, true);
    }

    @Override
    public EventRestartLoc getRestartLoc(Player player, RestartType type) {
        if(type == RestartType.FIXED) {
            Fraction fraction = player.getFraction();
            if (currentLocation != null) {
                List<Location> points = currentLocation.getRespawnPoints().getOrDefault(fraction, new ArrayList<>());
                return new EventRestartLoc(Rnd.get(points));
            }
        }
        return null;
    }

    @Override
    public void teleportPlayerToEvent(Player player) {
        EventRestartLoc eventRestartLoc = getRestartLoc(player, RestartType.FIXED);
        if (eventRestartLoc != null && eventRestartLoc.getLoc() != null) {
            player.teleToLocation(eventRestartLoc.getLoc());
        }
    }

    /**
     * Set owner of the artifact after the capturing
     * @param fraction - faction that has captured the artifact
     */
    public void setArtifactOwner(Fraction fraction) {
        if (!artifactOwner.compareAndSet(Fraction.NONE, fraction)) {
            return;
        }

        Announcements.announceToAllFromStringHolder("events.upgrading.artifact.captured", fraction);
        
        List<Player> aroundCharacters = World.getAroundCharacters(artifact.getNpc(), Config.GVE_UPGRADING_EVENT_CAPTURE_REWARD_RADIUS, 2000)
            .stream()
            .filter(GameObject::isPlayer)
            .map(creature -> (Player) creature)
            .collect(Collectors.toList());
        
        artifact.despawnObject(this);

        HwidUtils.INSTANCE.filterSameHwids(aroundCharacters).stream()
            .filter(p -> p.getFraction() == fraction)
            .filter(p -> p.getLevel() >= Config.FACTION_WAR_MIN_LEVEL)
            .map(Player::getConfrontationComponent)
            .forEach(c -> {
                int points = Config.FACTION_WAR_UPGRADING_ARTIFACT_CAPTURE_POINTS;
                c.incrementPoints(points);
                c.getObject().sendMessage(new CustomMessage("faction.war.s2").addNumber(points));
                ConfrontationService.getInstance().getTotalPoints(fraction).addAndGet(points);
            });

        currentLocation.getZone().refreshListeners();
    }

    public Fraction getArtifactOwner() {
        return artifactOwner.get();
    }

    private void initBuffs(MultiValueSet<String> set) {
        int[] wBuffInfo = set.getIntegerArray("winner_buff", ":");
        int[] lBuffInfo = set.getIntegerArray("loser_buff", ":");
        if (wBuffInfo.length == 2) {
            winnerBuff = SkillHolder.getInstance().getSkillEntry(wBuffInfo[0], wBuffInfo[1]);
        }
        if (lBuffInfo.length == 2) {
            loserBuff = SkillHolder.getInstance().getSkillEntry(lBuffInfo[0], lBuffInfo[1]);
        }
        artifactCapturedListener = new UpgradingEventOnTickListener(this, winnerBuff, loserBuff);
    }

    /**
     * Select and initialize new location for event
     */
    private void initLocation() {
        currentLocation = initCurrentLocation();
        if (currentLocation == null) {
            throw new IllegalArgumentException("Can't find any available location for Upgrading Event!");
        }

        currentLocation.getZone().addListener(artifactCapturedListener);
        currentLocation.getZone().setActive(true);
    }

    private void initSpawns() {
        SpawnManager.getInstance().spawn(currentLocation.getNpcGroup());
        artifact = new SpawnSimpleObject(ARTIFACT_ID, currentLocation.getArtifactLocation());
        addObject("ARTIFACT", artifact);
        artifact.spawnObject(this);
    }

    private void cleanSpawns() {
        SpawnManager.getInstance().despawn(currentLocation.getNpcGroup());
        if (artifact != null) {
            artifact.despawnObject(this);
        }
    }

    private void teleportPlayersToTowns() {
        currentLocation.getZone().getInsidePlayers().forEach(Player::teleToClosestTown);
    }

    private UpgradingEventLocation initCurrentLocation() {
        String lastLocation = ServerVariables.getString(LAST_LOCATION, UpgradingEventLocation.RUINS_OF_DESPAIR.name());
        try {
            UpgradingEventLocation location = UpgradingEventLocation.valueOf(lastLocation);
            switch (location) {
                case RUINS_OF_DESPAIR:
                    location = UpgradingEventLocation.ALLIGATOR_ISLAND;
                    break;
                case ALLIGATOR_ISLAND:
                    location = UpgradingEventLocation.RUINS_OF_DESPAIR;
                    break;
            }

            ServerVariables.set(LAST_LOCATION, location.name());
            return location;
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
