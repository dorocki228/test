package l2s.gameserver.model

import l2s.gameserver.model.items.ItemInstance

/**
 * @author VISTALL
 * @since 03.02.2012
 */
class CharSelectInfoPaperdollItem(item: ItemInstance) {

    val objectId: Int = item.objectId
    // fix for hair appearance conflicting with original model
    val itemId: Int = if (item.visualId <= 0) item.itemId else item.visualId
    val enchantLevel: Int = item.enchantLevel
    val augmentations: IntArray = item.augmentations
    val visualId: Int = item.visualId

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CharSelectInfoPaperdollItem) return false

        if (objectId != other.objectId) return false

        return true
    }

    override fun hashCode(): Int {
        return objectId
    }

}