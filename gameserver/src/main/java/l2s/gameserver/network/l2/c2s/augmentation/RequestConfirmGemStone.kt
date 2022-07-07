package l2s.gameserver.network.l2.c2s.augmentation

import l2s.gameserver.network.l2.c2s.L2GameClientPacket
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.network.l2.s2c.augmentation.ExPutCommissionResultForVariationMake
import l2s.gameserver.templates.augmentation.AugmentationInfo

class RequestConfirmGemStone : L2GameClientPacket() {

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
        if (gemstoneCount <= 0)
            return

        val activeChar = client.activeChar
        val targetItem = activeChar.inventory.getItemByObjectId(targetItemObjId)
        val refinerItem = activeChar.inventory.getItemByObjectId(refinerItemObjId)
        val gemstoneItem = activeChar.inventory.getItemByObjectId(gemstoneItemObjId)

        if (targetItem == null || refinerItem == null || gemstoneItem == null || targetItem.template.augmentationInfos.isEmpty) {
            activeChar.sendPacket(SystemMsg.THIS_IS_NOT_A_SUITABLE_ITEM)
            return
        }

        val augmentationInfos = targetItem.template.augmentationInfos

        val augmentationInfo: AugmentationInfo? = augmentationInfos.get(refinerItem.itemId)
        if (augmentationInfo == null) {
            activeChar.sendPacket(SystemMsg.THIS_IS_NOT_A_SUITABLE_ITEM)
            return
        }

        if (augmentationInfo.feeItemId != gemstoneItem.itemId
            || gemstoneItem.count < augmentationInfo.feeItemCount
            || augmentationInfo.feeItemCount != gemstoneCount) {
            activeChar.sendPacket(SystemMsg.GEMSTONE_QUANTITY_IS_INCORRECT)
            return
        }

        activeChar.sendPacket(
            ExPutCommissionResultForVariationMake(gemstoneItemObjId, gemstoneItem.itemId, gemstoneCount),
            SystemMsg.PRESS_THE_AUGMENT_BUTTON_TO_BEGIN
        )
    }

}