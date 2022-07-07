package handler.dailymissions;

import java.util.List;
import l2s.gameserver.listener.CharListener;
import l2s.gameserver.listener.actor.OnKillListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.hwid.HwidHolder;

/**
 * @author KRonst
 */
public class KillAnakimLilithDailyMissionHandler extends ProgressDailyMissionHandler {

    private class HandlerListeners implements OnKillListener {
        @Override
        public void onKill(Creature killer, Creature victim) {
            if(killer.isPlayable()) {
                int npcId = victim.getNpcId();
                if (npcId == 25283 || npcId == 25286) {
                    final List<Creature> characters = victim.getAroundCharacters(2000, 5000);
                    StringBuilder sb = new StringBuilder();
                    for (Creature character : characters) {
                        if (!character.isPlayable()) {
                            continue;
                        }
                        final String name = character.getName();
                        String hwid = "NO_HWID";
                        String status = character.isDead() ? "DEAD" : "LIVE";
                        final Player player = character.getPlayer();
                        if (player != null) {
                            final HwidHolder hwidHolder = player.getHwidHolder();
                            if (hwidHolder != null) {
                                hwid = hwidHolder.asString();
                            }
                        }
                        sb.append(name).append("[").append(hwid).append("]").append(status).append("\n");
                    }
                    logger.info("\n" + sb.toString() + "\n");
                    characters.stream()
                        .filter(c -> c.isPlayable() && !c.isDead())
                        .forEach(c -> progressMission(c.getPlayer(), 1));
                }
            }
        }

        @Override
        public boolean ignorePetOrSummon() {
            return false;
        }
    }

    private final HandlerListeners listeners = new HandlerListeners();

    @Override
    public CharListener getListener() {
        return listeners;
    }
}
