package l2s.gameserver.model.entity.events.impl.brevent.handlers;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.model.entity.events.impl.brevent.BREvent;
import l2s.gameserver.model.entity.events.impl.brevent.BREventConfig;
import l2s.gameserver.model.entity.events.impl.brevent.enums.EBREventState;
import l2s.gameserver.model.entity.events.impl.brevent.model.BRItemUpgradeableSet;
import l2s.gameserver.model.entity.events.impl.brevent.model.IBREventHandler;
import l2s.gameserver.model.entity.events.impl.brevent.util.Undresser;
import l2s.gameserver.model.entity.events.objects.EventPlayerObject;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.templates.item.ItemGrade;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.apache.logging.log4j.LogManager.getLogger;

/**
 * @author : Nami
 * @author Java-man
 * @date : 19.06.2018
 * @time : 22:14
 * <p/>
 */
public class BRPreparation implements IBREventHandler {
    private static final Logger LOGGER = getLogger(BRPreparation.class);

    @Override
    public EBREventState getState() {
        return EBREventState.PREPARATION;
    }

    @Override
    public boolean invoke(BREvent event, EventPlayerObject playerObject, String... args) {
        Player player = playerObject.getPlayer();
        if(player == null)
        {
            LOGGER.warn("Player {} is not in event", player);
            return false;
        }

        event.announceToParticipator(playerObject, "Preparation step begin...");

        player.unsetVar("BRLevel");
        player.unsetVar("BRExp");

        player.unsetVar("w_gr");
        player.unsetVar("a_gr");
        player.unsetVar("j_gr");

        player.unsetVar("brevent_backup_items_list");

        player.dispelDebuffs();

        for (Abnormal effect : player.getAbnormalList()) {  // обновляем время баффа только для баффов больше 10 минут
            if (effect.getSkill().getAbnormalTime() > 600) { // во избежание абуза УД, УЕ и т.д.
                effect.setDuration(effect.getDuration() + 2 * 60 * 60); // + 2 hours
            }
        }

        long exp_add = Experience.getExpForLevel(80) - player.getExp();
        player.addExpAndSp(exp_add, 0, true);
        player.setVar("BRExp", exp_add, event.getExpirationTime().toEpochMilli());

        // TODO Nami -> Добавляем новые скиллы?
        //player.rewardSkills(true, true, true, false);

        backupItems(event, player);

        for (Pair<Integer, Integer> pair : BREventConfig.EVENT_EQUIP_ITEMS) {
            event.addItem(player, pair.getKey(), pair.getValue(), 0);
        }
        player.setVar("w_gr", BREventConfig.START_GRADE, event.getExpirationTime().toEpochMilli());
        player.setVar("a_gr", BREventConfig.START_GRADE, event.getExpirationTime().toEpochMilli());
        player.setVar("j_gr", BREventConfig.START_GRADE, event.getExpirationTime().toEpochMilli());

        addEquip(event, player, BREventConfig.WEAPON_UPGRADE_LIST);
        addEquip(event, player, BREventConfig.ARMOR_UPGRADE_LIST);
        addEquip0(event, player, BREventConfig.JEWEL_UPGRADE_LIST);

        player.unblock();
        player.broadcastUserInfo(true);
        player.sendItemList(true);
        player.sendStatusUpdate(false, false, 14);

        return true;
    }

    private void addEquip(BREvent event, Player player, Map<Integer, List<BRItemUpgradeableSet>> map) {
        addEquip0(event, player, map.get(player.getClassId().getId()));
    }

    private void addEquip0(BREvent event, Player player, List<BRItemUpgradeableSet> list) {
        if(list == null)
            return;

        Optional<BRItemUpgradeableSet> optional = list.stream().filter(e -> e.getGrade() == ItemGrade.C).findFirst();
        if(optional.isPresent()) {
            BRItemUpgradeableSet itemSet = optional.get();
            itemSet.addPlayerItems(player, event);
        }
    }

    private void backupItems(BREvent event, Player player) {
        Undresser.undress(player);

        List<String> temp = new ArrayList<>(player.getInventory().getAllSize());

        player.getInventory().writeLock();
        player.getWarehouse().writeLock();
        try {
            for (ItemInstance ti : player.getInventory().getItems()) {
                ItemInstance item = player.getInventory().removeItemByObjectId(ti.getObjectId(), ti.getCount());
                player.getWarehouse().addItem(item);

                temp.add(item.getObjectId() + "," + item.getCount());
            }
        } finally {
            player.getInventory().writeUnlock();
            player.getWarehouse().writeUnlock();
        }

        String value = String.join(";", temp);
        player.setVar("brevent_backup_items_list", value, event.getExpirationTime().toEpochMilli());
    }
}
