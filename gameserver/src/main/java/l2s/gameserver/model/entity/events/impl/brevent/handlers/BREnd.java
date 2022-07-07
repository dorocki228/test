package l2s.gameserver.model.entity.events.impl.brevent.handlers;

import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.RestartType;
import l2s.gameserver.model.entity.events.impl.brevent.BREvent;
import l2s.gameserver.model.entity.events.impl.brevent.enums.EBREventState;
import l2s.gameserver.model.entity.events.impl.brevent.model.IBREventHandler;
import l2s.gameserver.model.entity.events.impl.brevent.util.Undresser;
import l2s.gameserver.model.entity.events.objects.EventPlayerObject;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.TeleportUtils;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

import static org.apache.logging.log4j.LogManager.getLogger;

/**
 * @author : Nami
 * @author Java-man
 * @date : 19.06.2018
 * @time : 22:14
 * <p/>
 */
public class BREnd implements IBREventHandler {
    private static final Logger LOGGER = getLogger(BREnd.class);

    @Override
    public EBREventState getState() {
        return EBREventState.END;
    }

    @Override
    public boolean invoke(BREvent event, EventPlayerObject playerObject, String... args) {
        Player player = playerObject.getPlayer();
        if(player == null)
        {
            LOGGER.warn("Player {} is not in event", player);
            return false;
        }

        playerObject.clear();

        event.removePlayer(playerObject);
        player.removeEvent(event);

        event.removeRegistered(player);

        player.block();

        event.hideSafeZoneCircle(playerObject);
        event.hideNextSafeZoneCircle(playerObject);

        long exp = player.getVarLong("BRExp", 0L);
        player.addExpAndSp(-exp, 0, true);

        deleteItems(player);
        returnItems(player);

        player.unsetVar("BRLevel");
        player.unsetVar("BRExp");

        player.unsetVar("w_gr");
        player.unsetVar("a_gr");
        player.unsetVar("j_gr");

        player.unsetVar("brevent_backup_items_list");

        if(player.isDead())
            player.setPendingRevive(true);
        Location location = TeleportUtils.getRestartPoint(player, RestartType.TO_VILLAGE).getLoc();
        player.teleToLocation(location, ReflectionManager.MAIN);

        playerObject.teleportBack();
        playerObject.restore();

        player.unblock();

        var reward = playerObject.getPoints("BATTLE_ROYAL_REWARD");
        if(reward == 0)
            reward = 3;

        ItemFunctions.addItem(player, 75035, reward);

        return true;
    }

    private void deleteItems(Player player) {
        Undresser.undress(player);
        player.getInventory().writeLock();
        try {
            for (ItemInstance ti : player.getInventory().getItems()) {
                player.getInventory().destroyItemByObjectId(ti.getObjectId(), ti.getCount());
            }
        }
        finally
        {
            player.getInventory().writeUnlock();
        }
    }

    private void returnItems(Player player) {
        player.getInventory().writeLock();
        player.getWarehouse().writeLock();

        try {
            var itemList = player.getVar("brevent_backup_items_list", "");
            var items = itemList.split(";");

            Arrays.stream(items)
                    .map(item -> item.split(","))
                    .filter(item -> item.length == 2)
                    .map(parts -> player.getWarehouse().removeItemByObjectId(
                            Integer.parseInt(parts[0]), Integer.parseInt(parts[1])))
                    .forEach(item -> player.getInventory().addItem(item));
        }
        finally
        {
            player.getInventory().writeUnlock();
            player.getWarehouse().writeUnlock();
        }
    }
}
