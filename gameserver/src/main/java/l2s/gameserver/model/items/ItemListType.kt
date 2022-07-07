package l2s.gameserver.model.items

import l2s.commons.bitmask.Flags

enum class ItemListType(override val bit: Int) : Flags {
    NONE(0x00),
    AUGMENT_BONUS(0x01),
    ELEMENTAL_ATTRIBUTE(0x02),
    ENCHANT_EFFECT(0x04),
    VISUAL_ID(0x08),
    SOUL_CRYSTAL(0x10)
}