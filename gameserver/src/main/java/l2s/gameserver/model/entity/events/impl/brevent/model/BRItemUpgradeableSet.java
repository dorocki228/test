package l2s.gameserver.model.entity.events.impl.brevent.model;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.brevent.BREvent;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.templates.item.ItemGrade;
import l2s.gameserver.templates.item.data.CapsuledItemData;
import l2s.gameserver.utils.ItemFunctions;

import java.util.List;

/**
 * @author : Nami
 * @date : 21.06.2018
 * @time : 12:48
 * <p/>
 */
public class BRItemUpgradeableSet {
    private ItemGrade grade;
    private int price;
    private List<CapsuledItemData> idEnchantList;

    public BRItemUpgradeableSet(ItemGrade grade, int price, List<CapsuledItemData> idEnchantList)
    {
        this.grade = grade;
        this.price = price;
        this.idEnchantList = idEnchantList;
    }

    public ItemGrade getGrade()
    {
        return grade;
    }

    public void setGrade(ItemGrade grade)
    {
        this.grade = grade;
    }

    public int getPrice()
    {
        return price;
    }

    public void setPrice(int price)
    {
        this.price = price;
    }

    public void destroyPlayerItems(Player player) {
        for(CapsuledItemData itemData : idEnchantList) {
            var count = ItemFunctions.getItemCount(player, itemData.getId());
            ItemFunctions.deleteItem(player, itemData.getId(), count);
        }
    }

    public void addPlayerItems(Player player, BREvent event) {
        for(CapsuledItemData itemData : idEnchantList) {
            var items = event.addItem(player, itemData.getId(), itemData.getCount(), itemData.getEnchantLevel());
            items.stream()
                    .filter(ItemInstance::isEquipable)
                    .forEach(item -> player.getInventory().equipItem(item));
        }
    }
}
