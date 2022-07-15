package l2s.gameserver.model

import l2s.commons.network.PacketWriter
import l2s.gameserver.model.items.ItemInstance
import l2s.gameserver.templates.item.ItemClassId

/**
 * @author Java-man
 * @since 17.08.2019
 */
data class LostItems(val entries: List<LostItem>) {

    fun write(packetWriter: PacketWriter, onlyClassId: Boolean) {
        packetWriter.writeH(entries.size) // dropped item list size
        entries.forEach {
            it.write(packetWriter, onlyClassId)
        }
    }

    companion object {

        val EMPTY = LostItems(emptyList())

    }

}

data class LostItem(
    private val classId: ItemClassId,
    private val enchant: Int,
    private val amount: Int
) {

    constructor(item: ItemInstance) : this(
        ItemClassId(item.itemId), item.enchantLevel, item.count.toInt()
    )

    fun write(packetWriter: PacketWriter, onlyClassId: Boolean) {
        if (onlyClassId) {
            packetWriter.writeD(classId.value) // item class id
        } else {
            packetWriter.writeD(classId.value) // item class id
            packetWriter.writeD(enchant) // item enchant
            packetWriter.writeD(amount) // item amount
        }
    }

}