package l2s.gameserver.model.entity.events.impl.brevent.model;

import l2s.commons.util.Rnd;
import l2s.gameserver.Announcements;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.data.xml.holder.InstantZoneHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Territory;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.entity.events.impl.brevent.BREvent;
import l2s.gameserver.model.entity.events.impl.brevent.BREventConfig;
import l2s.gameserver.model.entity.events.impl.brevent.enums.BRCircleColor;
import l2s.gameserver.model.entity.events.impl.brevent.enums.EBREventState;
import l2s.gameserver.model.entity.events.impl.brevent.tasks.BRCircleTimerRunnable;
import l2s.gameserver.model.entity.events.objects.EventPlayerObject;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.s2c.SayPacket2;
import l2s.gameserver.utils.Location;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * @author : Nami
 * @author Java-man
 * @date : 19.06.2018
 * @time : 21:07
 * <p/>
 */
public class BREventCore {
    private List<EventPlayerObject> playerList;
    private final Reflection reflection;

    private int playerCount;

    private BRStage stage;
    private boolean isLastStage = false;

    private BRCircleZone safeZone;
    private BRCircleZone nextSafeZone;

    private ScheduledFuture<?> circleTimerRunnable;
    private ScheduledFuture<?> damageTimerRunnable;

    private boolean isRunStage = false;

    private int second;
    private int currentStageTime;

    public BREventCore() {
        playerList = new CopyOnWriteArrayList<>();
        reflection = new Reflection();
        reflection.init(InstantZoneHolder.getInstance().getInstantZone(BREventConfig.INSTANT_ZONE_ID));
        second = 0;
        stage = BREventConfig.STAGE_LIST.stream()
                .filter(e -> e.getStageNumber() == 1)
                .findFirst().orElseThrow();
    }

    public Reflection getReflection()
    {
        return reflection;
    }

    public BRCircleZone getNextSafeZone()
    {
        return nextSafeZone;
    }

    public int getSecond()
    {
        return second;
    }

    public void teleportPlayerToEvent(EventPlayerObject player) {
        Territory randomArea = Rnd.get(BREventConfig.PLAYER_SPAWNS);
        Location pos = randomArea.getRandomLoc(reflection.getGeoIndex());
        player.ifPlayerExist(temp -> temp.teleToLocation(pos, reflection));
    }

    public void showSafeZoneCircle(EventPlayerObject player) {
        if(safeZone == null || !safeZone.isActive())
            return;
        safeZone.showCircle(player);
    }

    public void hideSafeZoneCircle(EventPlayerObject player) {
        if(safeZone == null)
            return;
        safeZone.hideCircle(player);
        safeZone.removeRadar(player);
    }

    public void showNextSafeZoneCircle(EventPlayerObject player) {
        if(nextSafeZone == null || !nextSafeZone.isActive())
            return;
        nextSafeZone.showCircle(player);
    }

    public void hideNextSafeZoneCircle(EventPlayerObject player) {
        if(nextSafeZone == null)
            return;
        nextSafeZone.hideCircle(player);
        nextSafeZone.removeRadar(player);
    }

    public int[] getCircleIds()
    {
        return Stream.of(safeZone, nextSafeZone)
                .filter(zone -> zone != null && zone.isActive())
                .mapToInt(BRCircleZone::getCircleId)
                .toArray();
    }

    public void startEvent(BREvent brEvent) {
        playerCount = playerList.size();

        circleTimerRunnable = ThreadPoolManager.getInstance().schedule(new BRCircleTimerRunnable(brEvent),
                60 * 1000L);    //через минуту начинается первый анонс о сужении
    }

    public void nextCircle(BREvent brEvent) {
        for(EventPlayerObject player : playerList)
        {
            hideSafeZoneCircle(player);
            hideNextSafeZoneCircle(player);
        }

        if(safeZone != null)
            safeZone.setActive(false);
        if(nextSafeZone != null)
            nextSafeZone.setActive(false);

        if(stage == null)
            return;

        int stageNumber = stage.getStageNumber();

        if(stageNumber == 1)
        {
            var randomArea = BREventConfig.getInstance().getRandomArea();
            var randomLoc = randomArea.getRandomLoc(reflection.getGeoIndex());
            var safeZoneCircle = new BRCircle(randomLoc.getX(), randomLoc.getY(), stage.getRadius(), BRCircleColor.WHITE, 1);
            nextSafeZone = BRCircleZone.createZone(safeZoneCircle, reflection);
        }
        else if(stageNumber > 1 && !isLastStage)
        {
            var safeZoneCircle = new BRCircle(nextSafeZone.getCircle(), BRCircleColor.BLUE, stageNumber - 1);
            safeZone = BRCircleZone.createZone(safeZoneCircle, reflection);
            nextSafeZone = nextSafeZone.getNextCircle(BRCircleColor.WHITE, stageNumber, stage.getRadius());
        }
        else if(isLastStage)
        {
            var safeZoneCircle = new BRCircle(nextSafeZone.getCircle(), BRCircleColor.BLUE, stageNumber);
            safeZone = BRCircleZone.createZone(safeZoneCircle, reflection);
            nextSafeZone = null;
        }

        if(safeZone != null)
            safeZone.setActive(true);
        if(nextSafeZone != null)
            nextSafeZone.setActive(true);

        currentStageTime = isRunStage ? stage.getRunTime() : stage.getSafeTime();
        for(EventPlayerObject player : playerList) {
            showSafeZoneCircle(player);
            showNextSafeZoneCircle(player);

            var minutes = TimeUnit.SECONDS.toMinutes(currentStageTime);
            announceToParticipator(player, "Next safe zone will be in " + minutes + " minutes.");
        }

        if(circleTimerRunnable != null)
            circleTimerRunnable.cancel(false);
        circleTimerRunnable = ThreadPoolManager.getInstance().schedule(new BRCircleTimerRunnable(brEvent), currentStageTime * 1000L);
        isRunStage = !isRunStage;
        second = currentStageTime;
        stopCircleTask();
        damageTimerRunnable = ThreadPoolManager.getInstance().scheduleAtFixedRate(new BRCircleTickRunnable(stage), 1000L, 1000L);

        if(!isLastStage)
        {
            Optional<BRStage> optional = BREventConfig.STAGE_LIST.stream()
                    .filter(e -> e.getStageNumber() == stage.getStageNumber() + 1)
                    .findFirst();
            if(optional.isPresent())
            {
                stage = optional.get();
            }
            else
            {
                isLastStage = true;
            }
        }
        else
            stage = null;
    }

    public void addPlayer(EventPlayerObject playerObject) {
        playerList.add(playerObject);
    }

    public void removePlayer(EventPlayerObject player) {
        playerList.remove(player);
    }

    public void stopEvent(BREvent brEvent) {
        stopCircleTask();
        stopCircleTimerTask();

        var winner = playerList.stream().findAny();

        winner.ifPresent(playerObject ->
        {
            var player = playerObject.getPlayer();
            if(player == null)
                return;

            brEvent.announceToParticipator(player, "Battle Royal Event has finished! Winner winner chicken dinner!");

            var kills = playerObject.getPoints("BATTLE_ROYAL_KILLS");

            var playersCount = brEvent.getPlayersCount();
            Announcements.announceToAll(player.getName() + " placed #1/" + playersCount + " with "
                    + kills + " kill(s). Winner winner chicken dinner!");

            playerObject.increasePoints("BATTLE_ROYAL_REWARD", 10);

            brEvent.getEventHandler(EBREventState.END).invoke(brEvent, playerObject);
        });

        if(safeZone != null)
            safeZone.setActive(false);
        if(nextSafeZone != null)
            nextSafeZone.setActive(false);

        playerList.clear();

        reflection.collapse();
    }

    private void stopCircleTask(){
        if(damageTimerRunnable != null) {
            damageTimerRunnable.cancel(true);
            damageTimerRunnable = null;
        }
    }

    private void stopCircleTimerTask(){
        if(circleTimerRunnable != null) {
            circleTimerRunnable.cancel(true);
            circleTimerRunnable = null;
        }
    }

    public int getPlayersCount() {
        return playerCount;
    }

    public int getPlayersLeft() {
        return playerList.size();
    }

    public Stream<EventPlayerObject> getPlayersStream()
    {
        return playerList.stream();
    }

    public Optional<EventPlayerObject> getEventPlayerObject(Player player)
    {
        return getPlayersStream()
                .filter(temp -> Objects.equals(temp.getPlayer(), player))
                .findAny();
    }

    public void announceToParticipator(EventPlayerObject player, String message) {
        player.ifPlayerExist(temp ->
                temp.sendPacket(new SayPacket2(0, ChatType.CRITICAL_ANNOUNCE, "", message)));
    }

    public class BRCircleTickRunnable implements Runnable {
        private final BRStage stage;

        public BRCircleTickRunnable(BRStage stage)
        {
            this.stage = stage;
        }

        @Override
        public void run() {
            second--;
            for(EventPlayerObject player : playerList) {
                if(player.getPlayer() == null)
                    return;

                if(stage == null)
                {
                    if(second >= 0)
                    {
                        continue;
                    }
                }
                else
                {
                    if(nextSafeZone != null)
                    {
                        if(nextSafeZone.checkIfInZone(player))
                        {
                            nextSafeZone.removeRadar(player);
                            continue;
                        }
                        else
                        {
                            nextSafeZone.addRadar(player);
                        }
                    }
                    else if(safeZone != null)
                        if(safeZone.checkIfInZone(player))
                        {
                            safeZone.removeRadar(player);
                            continue;
                        }
                        else
                        {
                            safeZone.addRadar(player);
                        }

                    if(stage.getStageNumber() == 1 || safeZone != null && safeZone.checkIfInZone(player))
                    {
                        continue;
                    }
                }

                int damage = isRunStage ? stage.getDamageInitial() : stage.getDamageFinal();
                player.ifPlayerExist(temp ->
                {
                    temp.reduceCurrentHp(damage, temp,
                            null, false, false, true, false,
                            false, false, true);
                    temp.sendMessage("You have received " + damage + " damage from the battle zone.");
                });
            }
        }
    }
}
