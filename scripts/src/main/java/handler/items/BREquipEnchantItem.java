package handler.items;

import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.brevent.BREvent;
import l2s.gameserver.model.entity.events.impl.brevent.BREventConfig;
import l2s.gameserver.model.entity.events.impl.brevent.model.BRItemUpgradeableSet;
import l2s.gameserver.model.entity.events.objects.EventPlayerObject;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.templates.item.ItemGrade;
import l2s.gameserver.utils.Functions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * @author : Nami
 * @author Java-man
 * @date : 21.06.2018
 * @time : 12:15
 * <p/>
 */
public class BREquipEnchantItem extends ScriptItemHandler {
    private static final Logger log = LoggerFactory.getLogger(BREquipEnchantItem.class);

    @Override
    public boolean useItem(Playable playable, ItemInstance item, boolean ctrl) {
        if (!playable.isPlayer()) {
            return false;
        }

        Player activeChar = playable.getPlayer();
        BREvent event = activeChar.getEvent(BREvent.class);
        if(event == null)
            return false;

        var playerObject = event.getEventPlayerObject(activeChar);

        if(!playerObject.isPresent())
            return false;

        showPage(activeChar, playerObject.get());

        return true;
    }

    private void showPage(Player player, EventPlayerObject playerObject) {
        StringBuilder append = new StringBuilder();
        append.append("<br>You can upgrade your equipment:<br>");
        append.append("PC points: ").append(playerObject.getPoints("BATTLE_ROYAL_POINTS")).append("<br>");
        // weapon
        append.append("<br>Weapon:<br>");
        ItemGrade currentGrade = ItemGrade.valueOf(player.getVar("w_gr"));
        Optional<BRItemUpgradeableSet> brItemUpgradeableSet = BREventConfig.getInstance().getNextItemByGrade(currentGrade, 0, player.getClassId().getId());
        append.append("Your grade: ").append(currentGrade.name()).append("<br>");
        if(brItemUpgradeableSet.isPresent())
        {
            append.append("Next grade price: ").append(brItemUpgradeableSet.get().getPrice()).append(" PC points<br>");
            append.append("<Button ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h htmbypass_services.BREquipEnchantItemService:upgrade 0").append("\">").append("Upgrade").append("</button><br>");
        }
        else
        {
            append.append("No any upgrades are available<br>");
        }
        // armor
        append.append("<br>Armor:<br>");
        currentGrade = ItemGrade.valueOf(player.getVar("a_gr"));
        brItemUpgradeableSet = BREventConfig.getInstance().getNextItemByGrade(currentGrade, 1, player.getClassId().getId());
        append.append("Your grade: ").append(currentGrade.name()).append("<br>");
        if(brItemUpgradeableSet.isPresent())
        {
            append.append("Next grade price: ").append(brItemUpgradeableSet.get().getPrice()).append(" PC points<br>");
            append.append("<Button ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h htmbypass_services.BREquipEnchantItemService:upgrade 1").append("\">").append("Upgrade").append("</button><br>");
        }
        else
        {
            append.append("No any upgrades are available<br>");
        }
        // jewel
        append.append("<br>Jewel:<br>");
        currentGrade = ItemGrade.valueOf(player.getVar("j_gr"));
        brItemUpgradeableSet = BREventConfig.getInstance().getNextItemByGrade(currentGrade, 2, player.getClassId().getId());
        append.append("Your grade: ").append(currentGrade.name()).append("<br>");
        if(brItemUpgradeableSet.isPresent())
        {
            append.append("Next grade price: ").append(brItemUpgradeableSet.get().getPrice()).append(" PC points<br>");
            append.append("<Button ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h htmbypass_services.BREquipEnchantItemService:upgrade 2").append("\">").append("Upgrade").append("</button><br>");
        }
        else
        {
            append.append("No any upgrades are available<br>");
        }

        Functions.show(append.toString(), player);
    }
}