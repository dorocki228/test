package l2s.gameserver.network.l2.c2s.augmentation

import l2s.commons.dao.JdbcEntityState
import l2s.gameserver.model.actor.instances.player.ShortCut
import l2s.gameserver.model.items.ItemInstance
import l2s.gameserver.network.l2.c2s.L2GameClientPacket
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.network.l2.s2c.InventoryUpdatePacket
import l2s.gameserver.network.l2.s2c.ShortCutRegisterPacket
import l2s.gameserver.network.l2.s2c.SystemMessagePacket
import l2s.gameserver.network.l2.s2c.augmentation.ExVariationCancelResult
import l2s.gameserver.utils.NpcUtils
import org.slf4j.LoggerFactory

class RequestRefineCancel : L2GameClientPacket() {

    private var targetItemObjId: Int = 0

    override fun readImpl() {
        targetItemObjId = readD()
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        if (NpcUtils.canPassPacket(activeChar, this) == null) {
            activeChar.sendPacket(ExVariationCancelResult.CLOSE)
            return
        }

        if (activeChar.isActionsDisabled) {
            activeChar.sendPacket(ExVariationCancelResult.CLOSE)
            return
        }

        if (activeChar.isInStoreMode) {
            activeChar.sendPacket(ExVariationCancelResult.CLOSE)
            return
        }

        if (activeChar.isInTrade) {
            activeChar.sendPacket(ExVariationCancelResult.CLOSE)
            return
        }

        val targetItem = activeChar.inventory.getItemByObjectId(targetItemObjId)

        // cannot remove augmentation from a not augmented item
        if (targetItem == null || !targetItem.isAugmented) {
            activeChar.sendPacket(
                ExVariationCancelResult.FAIL,
                SystemMsg.AUGMENTATION_REMOVAL_CAN_ONLY_BE_DONE_ON_AN_AUGMENTED_ITEM
            )
            return
        }

        val mineralId = targetItem.augmentationMineralId
        if (mineralId > 0)
        // DS: генератор аугментации создает предмет с mineralId = -1
        {
            val augmentationInfo = targetItem.template.augmentationInfos.get(mineralId)
            if (augmentationInfo == null) {
                _log.warn("Player: $activeChar, cancel item with mineral: $mineralId item: $targetItem")
            } else if (!activeChar.reduceAdena(augmentationInfo.cancelFee, true)) {
                activeChar.sendPacket(ExVariationCancelResult.FAIL, SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA)
                return
            }
        }

        val equipped = targetItem.isEquipped
        if (equipped)
            activeChar.inventory.unEquipItem(targetItem)

        // remove the augmentation
        targetItem.setAugmentation(0, ItemInstance.EMPTY_AUGMENTATIONS)
        targetItem.jdbcState = JdbcEntityState.UPDATED
        targetItem.update()

        if (equipped)
            activeChar.inventory.equipItem(targetItem)

        activeChar.sendPacket(
            ExVariationCancelResult.SUCCESS,
            InventoryUpdatePacket().addModifiedItem(activeChar, targetItem),
            SystemMessagePacket(SystemMsg.AUGMENTATION_HAS_BEEN_SUCCESSFULLY_REMOVED_FROM_YOUR_S1)
                .addItemName(targetItem.itemId)
        )

        for (sc in activeChar.allShortCuts)
            if (sc.id == targetItem.objectId && sc.type == ShortCut.TYPE_ITEM)
                activeChar.sendPacket(ShortCutRegisterPacket(activeChar, sc))
        activeChar.sendChanges()
    }

    companion object {
        private val _log = LoggerFactory.getLogger(RequestRefineCancel::class.java)
    }

}