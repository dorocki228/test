package l2s.gameserver.model.entity.events.impl;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import l2s.commons.collections.MultiValueSet;
import l2s.commons.time.cron.SchedulingPattern;
import l2s.gameserver.Announcements;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.instancemanager.GveRewardManager;
import l2s.gameserver.instancemanager.ServerVariables;
import l2s.gameserver.listener.actor.OnDeathListener;
import l2s.gameserver.listener.actor.player.OnAnswerListener;
import l2s.gameserver.listener.actor.player.impl.ReviveAnswerListener;
import l2s.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.bbs.EventTeleportationCommunityBoardEntry;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.EventType;
import l2s.gameserver.model.entity.events.objects.SpawnExObject;
import l2s.gameserver.model.entity.events.objects.ZoneObject;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.utils.Location;
import org.napile.pair.primitive.IntObjectPair;

/**
 * @author KRonst
 */
public class FactionBossEvent extends Event {

    private static final String TELEPORT_NAME = "lilith_anakim_event_teleport";
    private static final String LAST_BOSS_SV = "FACTION_BOSS_EVENT_LAST_ID";
    private static final String ANAKIM = "anakim";
    private static final String ANAKIM_ZONE = "anakim_zone";
    private static final String LILITH = "lilith";
    private static final String LILITH_ZONE = "lilith_zone";
    private static final int ANAKIM_ID = 25286;
    private static final int LILITH_ID = 25283;
    private static final int DEFAULT_BOSS = LILITH_ID;
    private final SchedulingPattern pattern;
    private final SkillEntry bossBuff;
    private Instant startTime;
    private boolean isInProgress;
    private NpcInstance currentBoss;
    private Zone bossZone;
    private FactionBossEventZoneListener bossZoneListener;

    public FactionBossEvent(MultiValueSet<String> set) {
        super(set);
        pattern = new SchedulingPattern(set.getString("pattern"));
        bossBuff = SkillHolder.getInstance().getSkillEntry(56016, 1);
    }

    @Override
    public void reCalcNextTime(boolean init) {
        clearActions();
        startTime = pattern.next(Instant.now());
        registerActions();

        if (!init) {
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
        isInProgress = true;
        initCurrentBoss();
        addCommunityBoardEntry(TELEPORT_NAME, new EventTeleportationCommunityBoardEntry(this, _name));
        Announcements.announceToAllFromStringHolder("events.faction.boss.start", currentBoss.getName());
    }

    @Override
    public void stopEvent(boolean force) {
        removeCommunityBoardEntry(TELEPORT_NAME);
        isInProgress = false;
        if (currentBoss.isDead()) {
            Announcements.announceToAllFromStringHolder("events.faction.boss.stop", currentBoss.getName());
        } else {
            Announcements.announceToAllFromStringHolder("events.faction.boss.draw", currentBoss.getName());
        }
        initNextBoss();
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
    public void teleportPlayerToEvent(Player player) {
        final Location location = Location.findAroundPosition(currentBoss, 5000);
        player.teleToLocation(location);
    }

    @Override
    public boolean ifVar(String name) {
        switch (name) {
            case ANAKIM:
                return ServerVariables.getInt(LAST_BOSS_SV, DEFAULT_BOSS) == LILITH_ID;
            case LILITH:
                return ServerVariables.getInt(LAST_BOSS_SV, DEFAULT_BOSS) == ANAKIM_ID;
            default:
                return super.ifVar(name);
        }
    }

    @Override
    public boolean handleRevive(Player player) {
        if (currentBoss != null) {
            player.sendMessage("You will be teleported in 5 seconds.");
            ThreadPoolManager.getInstance().schedule(() -> {
                if (!player.isDead()) {
                    return;
                }

                IntObjectPair<OnAnswerListener> ask = player.getAskListener(false);
                if (ask != null && ask.getValue() instanceof ReviveAnswerListener && !((ReviveAnswerListener) ask
                    .getValue()).isForPet()) {
                    player.getAskListener(true);
                }
                GveRewardManager.getInstance().manageRevivePenalty(player, false);
                player.dispelDebuffs();
                player.setPendingRevive(true);
                final Location loc = Location.findPointToStay(currentBoss, 2000);
                player.teleToLocation(loc);
            }, 5, TimeUnit.SECONDS);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean canResurrect(Creature active, Creature target, boolean force, boolean quiet) {
        if (active.getFraction().canAttack(target.getFraction())) {
            if (!quiet) {
                active.sendPacket(SystemMsg.INVALID_TARGET);
            }
            return false;
        }
        return true;
    }

    private void initCurrentBoss() {
        if (ifVar(ANAKIM)) {
            currentBoss = ((SpawnExObject) getFirstObject(ANAKIM)).getFirstSpawned();
        } else if (ifVar(LILITH)) {
            currentBoss = ((SpawnExObject) getFirstObject(LILITH)).getFirstSpawned();
        }
        currentBoss.addListener((OnDeathListener) (victim, killer) -> this.stopEvent(false));

        if (ifVar(ANAKIM)) {
            bossZone = ((ZoneObject) getFirstObject(ANAKIM_ZONE)).getZone();
        } else {
            bossZone = ((ZoneObject) getFirstObject(LILITH_ZONE)).getZone();
        }
        if (bossBuff != null && bossZone != null) {
            bossZoneListener = new FactionBossEventZoneListener(currentBoss, bossBuff, this);
            bossZone.addListener(bossZoneListener);
            for (Player player : bossZone.getInsidePlayers()) {
                if (!currentBoss.getFraction().canAttack(player)) {
                    bossBuff.getEffects(currentBoss, player);
                }
            }
        }
    }

    private void initNextBoss() {
        if (bossZoneListener != null && bossZone != null) {
            bossZone.removeListener(bossZoneListener);
            bossZoneListener = null;
            for (Player player : bossZone.getInsidePlayers()) {
                player.getAbnormalList().stopEffects(bossBuff);
                player.removeEvent(this);
            }
        }
        ServerVariables.set(LAST_BOSS_SV, currentBoss.getNpcId());
    }

    private static class FactionBossEventZoneListener implements OnZoneEnterLeaveListener {
        private final NpcInstance boss;
        private final SkillEntry skill;
        private final Event event;

        public FactionBossEventZoneListener(NpcInstance boss, SkillEntry skill, Event event) {
            this.boss = boss;
            this.skill = skill;
            this.event = event;
        }

        @Override
        public void onZoneEnter(Zone zone, Creature creature) {
            if (!boss.getFraction().canAttack(creature)) {
                skill.getEffects(boss, creature);
            }
            creature.addEvent(event);
        }

        @Override
        public void onZoneLeave(Zone zone, Creature creature) {
            creature.getAbnormalList().stopEffects(skill);
            creature.removeEvent(event);
        }
    }
}
