package l2s.gameserver.model.entity.events.impl.brevent.util;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
/**
 * @author : Nami
 * @date : 08.08.2018
 * @time : 21:23
 * <p/>
 */
public class Undresser {
    public static void undress(Player player) {
        for (ItemInstance ti : player.getInventory().getItems()) {
            if(ti.isEquipped()) {
                player.getInventory().unEquipItem(ti);
            }
        }
    }
}
