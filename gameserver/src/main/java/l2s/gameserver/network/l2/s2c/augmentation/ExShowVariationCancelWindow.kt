package l2s.gameserver.network.l2.s2c.augmentation

import l2s.gameserver.network.l2.s2c.L2GameServerPacket

class ExShowVariationCancelWindow : L2GameServerPacket() {

    override fun writeImpl() {}

    companion object {
        @JvmField
        val STATIC_PACKET: L2GameServerPacket = ExShowVariationCancelWindow()
    }

}