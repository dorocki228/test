package l2s.gameserver.network.l2.c2s.coupon

import l2s.gameserver.network.l2.c2s.L2GameClientPacket

class RequestPCCafeCouponUse : L2GameClientPacket() {

    private lateinit var enteredCode: String

    override fun readImpl() {
        enteredCode = readS()
    }

    override fun runImpl() {}

}
