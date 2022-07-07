package l2s.gameserver.network.l2.c2s.augmentation

import l2s.gameserver.network.l2.c2s.L2GameClientPacket
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.network.l2.s2c.augmentation.ExPutItemResultForVariationMake

class RequestConfirmTargetItem : L2GameClientPacket() {

    private var itemObjId: Int = 0

    override fun readImpl() {
        itemObjId = readD()
    }

    override fun runImpl() {
        val activeChar = client.activeChar
        val item = activeChar.inventory.getItemByObjectId(itemObjId)

        if (item == null) {
            activeChar.sendActionFailed()
            return
        }

        // check if the item is augmentable
        if (item.isAugmented) {
            activeChar.sendPacket(ExPutItemResultForVariationMake(item.objectId, item.itemId, true),
                    SystemMsg.ONCE_AN_ITEM_IS_AUGMENTED_IT_CANNOT_BE_AUGMENTED_AGAIN)
            return
        } else if (item.template.augmentationInfos.isEmpty) {
            activeChar.sendPacket(SystemMsg.THIS_IS_NOT_A_SUITABLE_ITEM)
            return
        }

        // check if the player can augment
        if (activeChar.isInStoreMode || activeChar.isPrivateBuffer) {
            activeChar.sendPacket(SystemMsg.YOU_CANNOT_AUGMENT_ITEMS_WHILE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP_IS_IN_OPERATION)
            return
        }
        if (activeChar.isInTrade) {
            activeChar.sendActionFailed()
            return
        }
        if (activeChar.isDead) {
            activeChar.sendPacket(SystemMsg.YOU_CANNOT_AUGMENT_ITEMS_WHILE_DEAD)
            return
        }
        if (activeChar.isParalyzed) {
            activeChar.sendPacket(SystemMsg.YOU_CANNOT_AUGMENT_ITEMS_WHILE_PARALYZED)
            return
        }
        if (activeChar.isFishing) {
            activeChar.sendPacket(SystemMsg.YOU_CANNOT_AUGMENT_ITEMS_WHILE_FISHING)
            return
        }
        if (activeChar.isSitting) {
            activeChar.sendPacket(SystemMsg.YOU_CANNOT_AUGMENT_ITEMS_WHILE_SITTING_DOWN)
            return
        }
        if (activeChar.isActionsDisabled) {
            activeChar.sendActionFailed()
            return
        }

        activeChar.sendPacket(
            ExPutItemResultForVariationMake(item.objectId, item.itemId, true),
            SystemMsg.SELECT_THE_CATALYST_FOR_AUGMENTATION
        )
    }

}
