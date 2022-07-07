package l2s.gameserver.model.entity.events.impl.brevent.listeners;

import com.google.common.collect.Range;
import l2s.gameserver.listener.actor.OnDeathListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.brevent.BREvent;
import l2s.gameserver.model.entity.events.impl.brevent.BREventConfig;
import l2s.gameserver.model.entity.events.impl.brevent.enums.EBREventState;
import l2s.gameserver.model.entity.events.objects.EventPlayerObject;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author : Nami
 * @author Java-man
 * @date : 19.06.2018
 * @time : 21:24
 * <p/>
 */
public class BREventDeathListener implements OnDeathListener
{
    private final BREvent event;

    private final Map<Range<Double>, AtomicBoolean> rewardedMap;

    public BREventDeathListener(BREvent event)
    {
        this.event = event;
        rewardedMap = Map.of(
                Range.closed(0.0D, 9.0D), new AtomicBoolean(false),
                Range.closed(10.0D, 19.0D), new AtomicBoolean(false),
                Range.closed(20.0D, 29.0D), new AtomicBoolean(false),
                Range.closed(30.0D, 39.0D), new AtomicBoolean(false),
                Range.closed(40.0D, 50.0D), new AtomicBoolean(false));
    }

    @Override
    public void onDeath(Creature victim, Creature killer) {
        if (victim == null || killer == null || victim.isPet() || victim.isSummon()) {
            return;
        }
        Player pVictim = victim.getPlayer();
        Player pKiller = killer.getPlayer();
        if (pVictim == null || pKiller == null) {
            return;
        }

        var playerObject = event.getEventPlayerObject(pVictim);

        var victimKills = playerObject.map(temp -> temp.getPoints("BATTLE_ROYAL_KILLS"));
        event.announceToParticipator(pVictim, pVictim.getName() + ", you placed #"
                + event.getPlayersLeft() + "/" + event.getPlayersCount()
                + " with " + victimKills + " kill(s). Better luck next time!");

        playerObject.ifPresent(temp ->
        {
            event.getEventHandler(EBREventState.END).invoke(event, temp);

            if(!Objects.equals(pKiller, pVictim))
            {
                addRewardFromGVE(temp);
            }
        });

        if (event.getPlayersLeft() <= 1) {
            event.stopEvent(false);
        }
    }

    private void addRewardFromGVE(EventPlayerObject playerObject) {
        var playersLeft = event.getPlayersLeft();
        var playersCount = event.getPlayersCount();

        var percent = playersLeft / (double) playersCount * 100.0D;
        rewardedMap.forEach((range, rewarded) ->
        {
            if(!range.contains(percent) || rewarded.getAndSet(true))
                return;

            event.getPlayersStream()
                    .forEach(temp -> temp.increasePoints("BATTLE_ROYAL_REWARD", 1));
        });

        playerObject.increasePoints("BATTLE_ROYAL_POINTS", BREventConfig.PLAYER_KILL_PC_POINTS_REWARD);
        playerObject.increasePoints("BATTLE_ROYAL_REWARD", 2);
        playerObject.increasePoints("BATTLE_ROYAL_KILLS", 1);
    }
}
