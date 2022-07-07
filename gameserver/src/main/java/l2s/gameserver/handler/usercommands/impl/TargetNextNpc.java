package l2s.gameserver.handler.usercommands.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import l2s.gameserver.Config;
import l2s.gameserver.handler.usercommands.IUserCommandHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.service.ActionUseService;

/**
 * @author KRonst
 */
public class TargetNextNpc implements IUserCommandHandler {

    private static final int[] COMMAND_IDS;

    @Override
    public boolean useUserCommand(int id, Player player) {
        if(id != COMMAND_IDS[0])
            return false;

        /*
         * If you want to change this logic, please don't forget
         * to change it also in {@link l2s.gameserver.network.l2.c2s.RequestActionUse.Action.ACTION10000}
         */
        Comparator<Creature> comparator = Comparator.comparingDouble(player::getDistance);
        GameObject currentTarget = player.getTarget();
        if(currentTarget == null) {
            Optional<NpcInstance> nextTarget = World.getAroundNpc(player, Config.CUSTOM_NEXT_TARGET_RADIUS, 100).stream()
                .filter(npc -> ActionUseService.getInstance().isNextTargetNpc(player, npc))
                .filter(npc -> !npc.isDead())
                .filter(npc -> !npc.isInvisible(player))
                .filter(GameObject::isVisible)
                .min(comparator);
            nextTarget.ifPresent(player::setTarget);
        }
        else {
            List<NpcInstance> list = World.getAroundNpc(player, Config.CUSTOM_NEXT_TARGET_RADIUS, 100).stream()
                .filter(npc -> ActionUseService.getInstance().isNextTargetNpc(player, npc))
                .filter(npc -> !npc.isDead())
                .filter(npc -> !npc.isInvisible(player))
                .filter(GameObject::isVisible)
                .sorted(comparator)
                .collect(Collectors.toUnmodifiableList());

            if(list.isEmpty()) {
                return true;
            }
            int nextIndex = 0;
            if(currentTarget instanceof NpcInstance) {
                nextIndex = list.indexOf(currentTarget) + 1;
                if(nextIndex >= list.size()) {
                    nextIndex = 0;
                }
            }
            player.setTarget(null);
            player.setTarget(list.get(nextIndex));
        }
        return true;
    }

    @Override
    public int[] getUserCommandList() {
        return COMMAND_IDS;
    }

    static
    {
        COMMAND_IDS = new int[] { 168 };
    }
}
