package l2s.gameserver.network.l2.s2c.augmentation

import l2s.gameserver.network.l2.s2c.L2GameServerPacket

/**
 * @author Java-man
 * @since 21.05.2019
 */
class ExPutIntensiveResultForVariationMake(
    private val refinerItemObjId: Int,
    private val lifestoneItemId: Int,
    private val gemstoneItemId: Int,
    private val gemstoneCount: Long
) : L2GameServerPacket() {

    override fun writeImpl() {
        writeD(refinerItemObjId)
        writeD(lifestoneItemId)
        writeD(gemstoneItemId)
        writeQ(gemstoneCount)
        writeD(1) // TODO unknown
    }

}