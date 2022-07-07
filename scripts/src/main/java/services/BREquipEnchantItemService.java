package services;

import l2s.gameserver.handler.bypass.Bypass;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.brevent.BREvent;
import l2s.gameserver.model.entity.events.impl.brevent.BREventConfig;
import l2s.gameserver.model.entity.events.impl.brevent.model.BRItemUpgradeableSet;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.item.ItemGrade;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * @author : Nami
 * @author Java-man
 * @date : 09.08.2018
 * @time : 18:14
 * <p/>
 */
public class BREquipEnchantItemService {
    private static final Logger log = LoggerFactory.getLogger(BREquipEnchantItemService.class);
    /**
     * 0 - weapon
     * 1 - armor
     * 2 - jewel
     */
    @Bypass("services.BREquipEnchantItemService:upgrade")
    public void upgrade(Player player, NpcInstance npc, String args[]) {
        if(args == null || args.length < 1) {
            return;
        }
        if(!NumberUtils.isDigits(args[0])) {
            return;
        }

        int type = Integer.parseInt(args[0]);
        if(type < 0 || type > 2) {
            return;
        }
        if(player == null || player.isInOfflineMode()) {
            return;
        }

        var event = player.getEvent(BREvent.class);
        if(event == null) {
            return;
        }

        ItemGrade currentGrade = ItemGrade.valueOf(type == 0 ? player.getVar("w_gr") : type == 1 ? player.getVar("a_gr") : player.getVar("j_gr"));
        Optional<BRItemUpgradeableSet> brItemUpgradeableSet = BREventConfig.getInstance().getNextItemByGrade(currentGrade, type, player.getClassId().getId());
        if(!brItemUpgradeableSet.isPresent()) {
            log.warn("Hack attempt! Player {} trying to upgrade non-upgradeable item with grade={} and type={}", player.getObjectId(), currentGrade.name(), type);
            return;
        }

        var playerObject = event.getEventPlayerObject(player);
        Integer points = playerObject.map(temp -> temp.getPoints("BATTLE_ROYAL_POINTS")).orElse(0);
        if(brItemUpgradeableSet.get().getPrice() > points) {
            player.sendMessage("You have not get enough PC points.");
            return;
        }

        playerObject.ifPresent(temp ->
                temp.reducePoints("BATTLE_ROYAL_POINTS", brItemUpgradeableSet.get().getPrice()));

        Optional<BRItemUpgradeableSet> currentItemSet = BREventConfig.getInstance().getItemByGrade(currentGrade, type, player.getClassId().getId());
        if(!currentItemSet.isPresent()) {
            log.warn("Hack attempt-2! Player {} trying to upgrade non-upgradeable item with grade={} and type={}", player.getObjectId(), currentGrade.name(), type);
            return;
        }

        player.getInventory().writeLock();
        try {
            currentItemSet.get().destroyPlayerItems(player);
            brItemUpgradeableSet.get().addPlayerItems(player, event);
            if(type == 0) {
                player.setVar("w_gr", brItemUpgradeableSet.get().getGrade().name());
            } else if(type == 1) {
                player.setVar("a_gr", brItemUpgradeableSet.get().getGrade().name());
            } else {
                player.setVar("j_gr", brItemUpgradeableSet.get().getGrade().name());
            }
        }
        finally {
            player.getInventory().writeUnlock();
        }
    }
}
