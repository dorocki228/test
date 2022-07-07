package l2s.gameserver.model.instances;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ReflectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

public class KamalokaInstance extends NpcInstance {
    private static final int[] instances = new int[]{79};
    public KamalokaInstance(int objectId, NpcTemplate template, MultiValueSet<String> set) {
        super(objectId, template, set);
    }

    @Override
    public void onBypassFeedback(Player player, String command) {
        if(!canBypassCheck(player))
            return;

        if(command.startsWith("enter")) {
            int instanceId = Integer.parseInt(command.substring(6));
            if(ArrayUtils.contains(instances, instanceId))
                ReflectionUtils.enterOrReenterInstance(player, instanceId);
        } else if(command.startsWith("escape")) {
            if(player.getParty() == null || !player.getParty().isLeader(player)) {
                showChatWindow(player, "not_party_leader.htm", true);
                return;
            }
            Reflection reflection = player.getReflection();
            if(reflection != null && !reflection.isDefault()) {
                for (Player p : reflection.getPlayers()) {
                    escapePlayer(p);
                }
            }
        } else if(command.startsWith("return"))
            escapePlayer(player);
        else
            super.onBypassFeedback(player, command);
    }

    private void escapePlayer(Player player) {
        Reflection r = player.getReflection();
        if(r.getReturnLoc() != null)
            player.teleToLocation(r.getReturnLoc(), ReflectionManager.MAIN);
        else
            player.setReflection(ReflectionManager.MAIN);
    }
}
