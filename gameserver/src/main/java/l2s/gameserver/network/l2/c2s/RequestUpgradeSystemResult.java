package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.data.xml.holder.UpgradeSystemHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExUpgradeSystemResult;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.templates.item.upgrade.UpgradeItemData;
import l2s.gameserver.templates.item.upgrade.rare.RareUpgradeData;
import l2s.gameserver.utils.ItemFunctions;

import java.util.List;

/**
 * @author Bonux
 **/

public class RequestUpgradeSystemResult implements IClientIncomingPacket {
	private int targetItemObjectId, upgradeId;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet) {
		targetItemObjectId = packet.readD();
		upgradeId = packet.readD();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client) {
		Player activeChar = client.getActiveChar();
		if (activeChar == null)
			return;

		if (activeChar.isActionsDisabled()) {
			activeChar.sendPacket(new ExUpgradeSystemResult(0, targetItemObjectId));
			activeChar.sendPacket(SystemMsg.FAILED_THE_OPERATION);
			return;
		}

		if (activeChar.isInStoreMode()) {
			activeChar.sendPacket(new ExUpgradeSystemResult(0, targetItemObjectId));
			activeChar.sendPacket(SystemMsg.FAILED_THE_OPERATION);
			return;
		}

		if (activeChar.isProcessingRequest()) {
			activeChar.sendPacket(new ExUpgradeSystemResult(0, targetItemObjectId));
			activeChar.sendPacket(SystemMsg.FAILED_THE_OPERATION);
			return;
		}

		if (activeChar.isFishing()) {
			activeChar.sendPacket(new ExUpgradeSystemResult(0, targetItemObjectId));
			activeChar.sendPacket(SystemMsg.FAILED_THE_OPERATION);
			return;
		}

		if (activeChar.isInTrainingCamp()) {
			activeChar.sendPacket(new ExUpgradeSystemResult(0, targetItemObjectId));
			activeChar.sendPacket(SystemMsg.FAILED_THE_OPERATION);
			return;
		}

		RareUpgradeData upgradeData = UpgradeSystemHolder.getInstance().getRareUpgradeData(upgradeId);
		if (upgradeData == null) {
			activeChar.sendPacket(new ExUpgradeSystemResult(0, targetItemObjectId));
			activeChar.sendPacket(SystemMsg.FAILED_THE_OPERATION);
			return;
		}

		activeChar.getInventory().writeLock();
		try {
			ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(targetItemObjectId);
			if (targetItem == null) { // Улучшаемый итем не найден.
				activeChar.sendPacket(new ExUpgradeSystemResult(0, targetItemObjectId));
				activeChar.sendPacket(SystemMsg.FAILED_BECAUSE_THE_TARGET_ITEM_DOES_NOT_EXIST);
				return;
			}

			if (targetItem.getEnchantLevel() != upgradeData.getEnchantLevel()) { // Заточка улучшаемого итема не соответствует требованиям.
				activeChar.sendPacket(new ExUpgradeSystemResult(0, targetItemObjectId));
				activeChar.sendPacket(SystemMsg.FAILED_BECAUSE_THE_TARGET_ITEM_DOES_NOT_EXIST);
				return;
			}

			if (activeChar.getAdena() < upgradeData.getPrice()) { // Проверяем наличие адены.
				activeChar.sendPacket(new ExUpgradeSystemResult(0, targetItemObjectId));
				activeChar.sendPacket(SystemMsg.FAILED_BECAUSE_THERES_NOT_ENOUGH_ADENA);
				return;
			}

			loop1:
			for (UpgradeItemData requiredItem : upgradeData.getRequiredItems()) { // Проверяем наличие требуемых предметов.
				if (requiredItem.getCount() == 0)
					continue;

				List<ItemInstance> items = activeChar.getInventory().getItemsByItemId(requiredItem.getId());
				for (ItemInstance item : items) {
					if (item == null || item.getCount() < requiredItem.getCount() || item.getEnchantLevel() != requiredItem.getEnchantLevel())
						continue;
					continue loop1;
				}

				activeChar.sendPacket(new ExUpgradeSystemResult(0, targetItemObjectId));
				activeChar.sendPacket(SystemMsg.FAILED_BECAUSE_THERE_ARE_NOT_ENOUGH_INGREDIENTS);
				return;
			}

			activeChar.getInventory().destroyItem(targetItem, 1);
			activeChar.sendPacket(SystemMessagePacket.removeItems(targetItem.getItemId(), 1));
			activeChar.reduceAdena(upgradeData.getPrice(), true); // Забираем оплату.

			loop2:
			for (UpgradeItemData requiredItem : upgradeData.getRequiredItems()) { // Забираем требуемые предметы.
				if (requiredItem.getCount() == 0)
					continue;

				List<ItemInstance> items = activeChar.getInventory().getItemsByItemId(requiredItem.getId());
				for (ItemInstance item : items) {
					if (item == null || item.getCount() < requiredItem.getCount() || item.getEnchantLevel() != requiredItem.getEnchantLevel())
						continue;

					if (!activeChar.getInventory().destroyItemByObjectId(item.getObjectId(), requiredItem.getCount()))
						continue;//TODO audit

					activeChar.sendPacket(SystemMessagePacket.removeItems(requiredItem.getId(), requiredItem.getCount()));
					continue loop2;
				}
			}
		} finally {
			activeChar.getInventory().writeUnlock();
		}

		List<ItemInstance> items = ItemFunctions.addItem(activeChar, upgradeData.getResultItemId(), upgradeData.getResultItemCount(), upgradeData.getResultItemEnchant(), true); // Выдаем предмет разыгранного результата.
		activeChar.sendPacket(new ExUpgradeSystemResult(1, items.get(0).getObjectId()));
	}
}
