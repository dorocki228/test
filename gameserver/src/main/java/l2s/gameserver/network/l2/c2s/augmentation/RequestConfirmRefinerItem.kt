package l2s.gameserver.network.l2.c2s.augmentation

import l2s.gameserver.network.l2.c2s.L2GameClientPacket
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.network.l2.s2c.SystemMessagePacket
import l2s.gameserver.network.l2.s2c.augmentation.ExPutIntensiveResultForVariationMake
import l2s.gameserver.templates.augmentation.AugmentationInfo

class RequestConfirmRefinerItem : L2GameClientPacket() {

    private var targetItemObjId: Int = 0
    private var refinerItemObjId: Int = 0

    override fun readImpl() {
        targetItemObjId = readD()
        refinerItemObjId = readD()
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        val targetItem = activeChar.inventory.getItemByObjectId(targetItemObjId)
        val refinerItem = activeChar.inventory.getItemByObjectId(refinerItemObjId)

        if (targetItem == null || refinerItem == null) {
            activeChar.sendPacket(SystemMsg.THIS_IS_NOT_A_SUITABLE_ITEM)
            return
        }

        val augmentationInfos = targetItem.template.augmentationInfos

        val refinerItemId = refinerItem.template.itemId

        val augmentationInfo: AugmentationInfo? = augmentationInfos.get(refinerItemId)
        if (augmentationInfos.isEmpty || augmentationInfo == null) {
            activeChar.sendPacket(SystemMsg.THIS_IS_NOT_A_SUITABLE_ITEM)
            return
        }

        val sm = SystemMessagePacket(SystemMsg.YOU_NEED_S2_S1)
            .addItemName(augmentationInfo.feeItemId)
            .addNumber(augmentationInfo.feeItemCount)
        activeChar.sendPacket(
            ExPutIntensiveResultForVariationMake(
                refinerItemObjId,
                refinerItemId,
                augmentationInfo.feeItemId,
                augmentationInfo.feeItemCount
            ), sm
        )
    }
}