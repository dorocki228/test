package l2s.gameserver.network.l2.s2c.augmentation

import l2s.gameserver.model.items.ItemInstance
import l2s.gameserver.network.l2.s2c.L2GameServerPacket

class ExVariationResult(private val options: IntArray = ItemInstance.EMPTY_AUGMENTATIONS) : L2GameServerPacket() {

    private val openWindow: Boolean = options.isNotEmpty()

    override fun writeImpl() {
        writeD(options[0])
        writeD(options[1])
        writeD(openWindow)
    }

    companion object {
        val CLOSE = ExVariationResult()
        val FAIL = ExVariationResult(ItemInstance.EMPTY_AUGMENTATIONS)
    }

}