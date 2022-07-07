package l2s.gameserver.network.l2.s2c.augmentation

import l2s.gameserver.network.l2.s2c.L2GameServerPacket

class ExVariationCancelResult(private val openWindow: Boolean, private val result: Boolean) : L2GameServerPacket() {

    override fun writeImpl() {
        writeD(result)
        writeD(openWindow)
    }

    companion object {
        val CLOSE: L2GameServerPacket = ExVariationCancelResult(false, false)
        val FAIL: L2GameServerPacket = ExVariationCancelResult(true, false)
        val SUCCESS: L2GameServerPacket = ExVariationCancelResult(true, true)
    }

}