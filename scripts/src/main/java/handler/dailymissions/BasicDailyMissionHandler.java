package handler.dailymissions;

import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import l2s.gameserver.GameServer;
import l2s.gameserver.instancemanager.ServerVariables;
import l2s.gameserver.listener.game.OnShutdownListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.DailyMission;
import l2s.gameserver.model.entity.events.impl.SingleMatchEvent;
import l2s.gameserver.network.l2.components.hwid.DefaultHwidHolder;
import l2s.gameserver.network.l2.components.hwid.HwidHolder;
import l2s.gameserver.templates.dailymissions.DailyMissionStatus;
import l2s.gameserver.templates.dailymissions.DailyMissionTemplate;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

/**
 * @author Java-man
 **/
public class BasicDailyMissionHandler extends ScriptDailyMissionHandler
{
    private static final SetMultimap<Integer, HwidHolder> receivedReward;

    static
    {
        receivedReward = Multimaps.newSetMultimap(
                new ConcurrentHashMap<>(), CopyOnWriteArraySet::new);

        // TODO: config
        if (!GameServer.DEVELOP) {
            var str = ServerVariables.getString("MISSION_HWIDS", "");
            var parts = str.split(";");
            Arrays.stream(parts)
                    .map(s -> s.split(","))
                    .filter(s -> s.length == 2)
                    .forEach(strings -> receivedReward.put(Integer.parseInt(strings[0]), new DefaultHwidHolder(strings[1])));

            GameServer.getInstance().addListener((OnShutdownListener) () ->
            {
                var temp = receivedReward.entries().stream()
                        .map(entry -> entry.getKey() + "," + entry.getValue().asString())
                        .collect(Collectors.joining(";"));
                ServerVariables.set("MISSION_HWIDS", temp);
            });
        }
    }

    @Override
    public DailyMissionStatus getStatus(Player player, DailyMission mission, DailyMissionTemplate missionTemplate)
    {
        if(mission != null && mission.isCompleted())
        {
            return DailyMissionStatus.COMPLETED;
        }

        if(missionTemplate.isCheckHwid())
        {
            if(receivedReward.containsEntry(missionTemplate.getId(), player.getHwidHolder()))
                return DailyMissionStatus.COMPLETED;
        }

        if(player.containsEvent(SingleMatchEvent.class))
            return DailyMissionStatus.NOT_AVAILABLE;

        if(player.getLevel() < 70) {
            return DailyMissionStatus.NOT_AVAILABLE;
        }

        return DailyMissionStatus.AVAILABLE;
    }

    @Override
    public void onComplete(DailyMission mission, Player player)
    {
        receivedReward.put(mission.getId(), player.getHwidHolder());
    }

    @Override
    public void onReset(DailyMission mission)
    {
        receivedReward.removeAll(mission.getId());
    }

    public boolean canBeDistributedToParty() {
        return false;
    }
}
