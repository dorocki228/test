package l2s.gameserver.service;

import l2s.commons.dao.JdbcEntityState;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.templates.item.support.VisualChange;

/**
 * @author mangol
 */
public class ItemService {
	private static final ItemService ourInstance = new ItemService();

	public static ItemService getInstance() {
		return ourInstance;
	}

	private ItemService() {
	}

	public boolean enableVisualChanges(ItemInstance item, Player player) {
		if(item == null) {
			return false;
		}
		var visualChanges = item.getTemplate().getVisualChanges();
		if(visualChanges.isEmpty()) {
			return false;
		}

		var inventory = player.getInventory();
		inventory.writeLock();
		try {
			boolean success = true;
			for(VisualChange visualChange : visualChanges) {
				var paperdollIndex = Inventory.getPaperdollIndex(visualChange.getSlot().mask());
				var paperdollItem = inventory.getPaperdollItem(paperdollIndex);
				if(paperdollItem == null || paperdollItem.getVisualItemObjId() != 0) {
					success = false;
					break;
				}
			}
			if(!success) {
				return false;
			}
			for(VisualChange visualChange : visualChanges) {
				var paperdollIndex = Inventory.getPaperdollIndex(visualChange.getSlot().mask());
				var paperdollItem = inventory.getPaperdollItem(paperdollIndex);
				if(paperdollItem != null) {
					paperdollItem.setVisualId(visualChange.getToId());
					paperdollItem.setVisualItemObjId(item.getObjectId());
					inventory.sendEquipInfo(paperdollIndex);
					inventory.sendModifyItem(paperdollItem);
				}
			}
		} finally {
			inventory.writeUnlock();
		}
		return true;
	}

	public boolean disableVisualChanges(ItemInstance i, Player player) {
		if(i == null) {
			return false;
		}
		var visualChanges = i.getTemplate().getVisualChanges();
		if(visualChanges.isEmpty()) {
			return false;
		}

		var inventory = player.getInventory();
		inventory.writeLock();
		try {
			boolean success = true;
			for(VisualChange visualChange : visualChanges) {
				var paperdollIndex = Inventory.getPaperdollIndex(visualChange.getSlot().mask());
				var paperdollItem = inventory.getPaperdollItem(paperdollIndex);
				if(paperdollItem == null || paperdollItem.getVisualItemObjId() != i.getObjectId() || paperdollItem.getVisualId() != visualChange.getToId()) {
					success = false;
					break;
				}
			}
			if(!success) {
				return false;
			}
			for(VisualChange visualChange : visualChanges) {
				var paperdollIndex = Inventory.getPaperdollIndex(visualChange.getSlot().mask());
				var paperdollItem = inventory.getPaperdollItem(paperdollIndex);
				if(paperdollItem != null) {
					paperdollItem.setVisualId(0);
					paperdollItem.setVisualItemObjId(0);
					paperdollItem.setJdbcState(JdbcEntityState.UPDATED);
					paperdollItem.update();
					if(paperdollItem.isEquipped()) {
						inventory.sendEquipInfo(paperdollItem.getEquipSlot());
						inventory.sendModifyItem(paperdollItem);
					}
				}
			}
		} finally {
			inventory.writeUnlock();
		}
		return true;
	}

	public void disableAllVisualChanges(ItemInstance i, Player player) {
		if(i == null) {
			return;
		}
		var visualChanges = i.getTemplate().getVisualChanges();
		if(visualChanges.isEmpty()) {
			return;
		}

		var inventory = player.getInventory();
		inventory.writeLock();
		try {
			for(ItemInstance item : inventory.getItems()) {
				if(item.getVisualItemObjId() == i.getObjectId()) {
					item.setVisualId(0);
					item.setVisualItemObjId(0);
					item.setJdbcState(JdbcEntityState.UPDATED);
					item.update();

					if(item.isEquipped()) {
						inventory.sendEquipInfo(item.getEquipSlot());
						inventory.sendModifyItem(item);
					}
				}
			}
		} finally {
			inventory.writeUnlock();
		}
	}
}
