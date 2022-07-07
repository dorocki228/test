package l2s.gameserver.network.l2.c2s.augmentation

import l2s.gameserver.network.l2.c2s.L2GameClientPacket
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.network.l2.s2c.augmentation.ExPutItemResultForVariationCancel

class RequestConfirmCancelItem : L2GameClientPacket() {

    private var objectId: Int = 0

    override fun readImpl() {
        objectId = readD()
    }

    override fun runImpl() {
        val activeChar = client.activeChar
        val item = activeChar.inventory.getItemByObjectId(objectId)

        if (item == null) {
            activeChar.sendActionFailed()
            return
        }

        if (!item.isAugmented) {
            activeChar.sendPacket(SystemMsg.AUGMENTATION_REMOVAL_CAN_ONLY_BE_DONE_ON_AN_AUGMENTED_ITEM)
            return
        }

        activeChar.sendPacket(ExPutItemResultForVariationCancel(item))
    }

}