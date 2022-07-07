package l2s.gameserver.network.l2.c2s.augmentation

import l2s.commons.dao.JdbcEntityState
import l2s.gameserver.model.actor.instances.player.ShortCut
import l2s.gameserver.network.l2.c2s.L2GameClientPacket
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.network.l2.s2c.InventoryUpdatePacket
import l2s.gameserver.network.l2.s2c.ShortCutRegisterPacket
import l2s.gameserver.network.l2.s2c.augmentation.ExVariationResult
import l2s.gameserver.utils.NpcUtils

class RequestRefine : L2GameClientPacket() {

    private var targetItemObjId: Int = 0
    private var refinerItemObjId: Int = 0
    private var gemstoneItemObjId: Int = 0
    private var gemstoneCount: Long = 0

    override fun readImpl() {
        targetItemObjId = readD()
        refinerItemObjId = readD()
        gemstoneItemObjId = readD()
        gemstoneCount = readQ()
    }

    override fun runImpl() {
        val player = client.activeChar ?: return

        if (NpcUtils.canPassPacket(player, this) == null) {
            player.sendPacket(ExVariationResult.CLOSE)
            return
        }

        if (player.isActionsDisabled) {
            player.sendPacket(ExVariationResult.CLOSE)
            return
        }

        if (player.isInStoreMode) {
            player.sendPacket(ExVariationResult.CLOSE)
            return
        }

        if (player.isInTrade) {
            player.sendPacket(ExVariationResult.CLOSE)
            return
        }

        val targetItem = player.inventory.getItemByObjectId(targetItemObjId)
        val refinerItem = player.inventory.getItemByObjectId(refinerItemObjId)
        val gemstoneItem = player.inventory.getItemByObjectId(gemstoneItemObjId)

        if (targetItem == null || refinerItem == null || gemstoneItem == null || player.level < 46 || targetItem.template.augmentationInfos.isEmpty) {
            player.sendPacket(ExVariationResult.FAIL, SystemMsg.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS)
            return
        }

        val augmentationInfos = targetItem.template.augmentationInfos

        val augmentationInfo = augmentationInfos.get(refinerItem.itemId)
        if (augmentationInfo == null || gemstoneItem.count < augmentationInfo.feeItemCount || gemstoneItem.itemId != augmentationInfo.feeItemId) {
            player.sendPacket(ExVariationResult.FAIL, SystemMsg.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS)
            return
        }

        if (gemstoneCount != augmentationInfo.feeItemCount) {
            player.sendPacket(ExVariationResult.FAIL, SystemMsg.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS)
            return
        }

        val options = augmentationInfo.randomOption(targetItem.template)
        if (options == null || options.all { it == 0 }) {
            player.sendPacket(ExVariationResult.FAIL, SystemMsg.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS)
            return
        }

        if (targetItem.isAugmented) {
            val mineralId = targetItem.augmentationMineralId
            if (mineralId > 0)
            // DS: генератор аугментации создает предмет с mineralId = -1
            {
                val augmentationInfo = targetItem.template.augmentationInfos.get(mineralId)
                requireNotNull(augmentationInfo) { "Player: $player, cancel item with mineral: $mineralId item: $targetItem" }

                if (!player.reduceAdena(augmentationInfo.cancelFee, true)) {
                    player.sendPacket(ExVariationResult.FAIL, SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA)
                    return
                }
            }
        }

        if (!player.inventory.destroyItemByObjectId(gemstoneItemObjId, augmentationInfo.feeItemCount))
            return

        if (!player.inventory.destroyItemByObjectId(refinerItemObjId, 1L))
            return

        val equipped = targetItem.isEquipped
        if (equipped)
            player.inventory.unEquipItem(targetItem)

        targetItem.setAugmentation(augmentationInfo.mineralId, options)
        targetItem.jdbcState = JdbcEntityState.UPDATED
        targetItem.update()

        if (equipped)
            player.inventory.equipItem(targetItem)

        for (sc in player.allShortCuts)
            if (sc.id == targetItem.objectId && sc.type == ShortCut.TYPE_ITEM)
                player.sendPacket(ShortCutRegisterPacket(player, sc))
        player.sendChanges()

        player.sendPacket(ExVariationResult(options), SystemMsg.THE_ITEM_WAS_SUCCESSFULLY_AUGMENTED)

        player.sendPacket(InventoryUpdatePacket().addModifiedItem(player, targetItem))
    }
}