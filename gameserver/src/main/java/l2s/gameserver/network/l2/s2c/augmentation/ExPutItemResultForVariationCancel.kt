package l2s.gameserver.network.l2.s2c.augmentation

import l2s.gameserver.model.items.ItemInstance
import l2s.gameserver.network.l2.s2c.L2GameServerPacket

/**
 * @author VISTALL
 */
class ExPutItemResultForVariationCancel(item: ItemInstance) : L2GameServerPacket() {

    private val itemObjectId: Int = item.objectId
    private val itemId: Int = item.itemId
    private val options: IntArray = item.augmentations
    private val price: Long

    init {
        val augmentationInfo = item.template.augmentationInfos.get(item.augmentationMineralId)
        price = augmentationInfo?.cancelFee ?: 0
    }

    override fun writeImpl() {
        writeD(itemObjectId)
        writeD(itemId)
        writeD(options[0])
        writeD(options[1])
        writeQ(price)
        writeD(0x01)
    }

}