package l2s.gameserver.network.l2.s2c.augmentation

import l2s.gameserver.network.l2.s2c.L2GameServerPacket

class ExPutItemResultForVariationMake(private val itemObjId: Int, private val itemId: Int,
                                      private val unknown: Boolean) : L2GameServerPacket() {

    override fun writeImpl() {
        writeD(itemObjId)
        writeD(itemId)
        writeD(unknown) // TODO: unknown
    }

}