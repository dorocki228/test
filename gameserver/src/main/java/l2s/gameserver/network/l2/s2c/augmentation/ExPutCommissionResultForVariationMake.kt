package l2s.gameserver.network.l2.s2c.augmentation

import l2s.gameserver.network.l2.s2c.L2GameServerPacket

class ExPutCommissionResultForVariationMake(
    private val gemstoneObjId: Int,
    private val gemstoneId: Int,
    private val gemstoneCount: Long
) :
    L2GameServerPacket() {

    override fun writeImpl() {
        writeD(gemstoneObjId)
        writeD(gemstoneId)
        writeQ(gemstoneCount)
        writeQ(0) // TODO: unknown
        writeD(1)
    }
}