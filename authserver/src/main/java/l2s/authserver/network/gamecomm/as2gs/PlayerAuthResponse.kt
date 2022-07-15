package l2s.authserver.network.gamecomm.as2gs

import io.netty.buffer.ByteBuf
import l2s.authserver.accounts.SessionManager.Session
import l2s.authserver.network.gamecomm.SendablePacket

class PlayerAuthResponse : SendablePacket {

    private var login: String?
    private var authed: Boolean
    private var playOkID1 = 0
    private var playOkID2 = 0
    private var loginOkID1 = 0
    private var loginOkID2 = 0
    private var bonus = 0
    private var bonusExpire = 0
    private var points = 0
    private var hwid: String? = null
    private var phoneNumber: Long = 0

    constructor(session: Session, authed: Boolean) {
        val account = session.account
        login = account.login
        this.authed = authed
        if (authed) {
            val skey = session.sessionKey
            playOkID1 = skey.playOkID1
            playOkID2 = skey.playOkID2
            loginOkID1 = skey.loginOkID1
            loginOkID2 = skey.loginOkID2
            bonus = account.bonus
            bonusExpire = account.bonusExpire
            points = account.points
            hwid = account.allowedHwid
            phoneNumber = account.phoneNumber
        }
    }

    constructor(account: String) {
        login = account
        authed = false
    }

    override val opCode: Byte = 0x02

    override fun writeImpl(byteBuf: ByteBuf): ByteBuf {
        writeString(byteBuf, login ?: "")

        byteBuf.writeByte(if (authed) 1 else 0)

        if (authed) {
            byteBuf.writeInt(playOkID1)
            byteBuf.writeInt(playOkID2)
            byteBuf.writeInt(loginOkID1)
            byteBuf.writeInt(loginOkID2)
            byteBuf.writeInt(bonus)
            byteBuf.writeInt(bonusExpire)
            byteBuf.writeInt(points)
            writeString(byteBuf, hwid ?: "")
            byteBuf.writeLong(phoneNumber)
        }

        return byteBuf
    }

}