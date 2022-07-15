package l2s.gameserver.templates.item

import l2s.gameserver.handler.items.IItemHandler
import l2s.gameserver.templates.item.WeaponTemplate.WeaponType

interface ItemType {

    val handler: IItemHandler

    val exType: ExItemType

    fun mask(): Long

}

fun ItemType.isRanged(): Boolean {
    return when (this) {
        WeaponType.BOW -> true
        WeaponType.CROSSBOW -> true
        WeaponType.TWOHANDCROSSBOW -> true
        else -> false
    }
}