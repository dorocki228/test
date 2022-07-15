package l2s.gameserver.network.authcomm.as2gs

import io.netty.buffer.ByteBuf
import l2s.commons.ban.BanBindType
import l2s.commons.ban.BanInfo
import l2s.gameserver.instancemanager.AuthBanManager
import l2s.gameserver.instancemanager.GameBanManager
import l2s.gameserver.network.authcomm.ReceivablePacket
import l2s.gameserver.network.authcomm.vertx.AuthServerConnection
import java.util.*

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 10.04.2019
 * Developed for L2-Scripts.com
 */
class CheckBans(client: AuthServerConnection, byteBuf: ByteBuf) : ReceivablePacket(client, byteBuf) {

    private val bindType: BanBindType
    private val bans: MutableMap<String, BanInfo>

    init {
        try {
            bindType = BanBindType.VALUES[byteBuf.readUnsignedByte().toInt()]
        } catch (e: Exception) {
            throw IllegalArgumentException("Can't find ban type", e)
        }
        val size: Int = byteBuf.readUnsignedShort()
        bans = HashMap(size)
        for (i in 0 until size) {
            val bindValue: String = readString(byteBuf)
            val endTime: Int = byteBuf.readInt()
            val reason: String = readString(byteBuf)
            bans[bindValue] = BanInfo(endTime, reason)
        }
    }

    override fun runImpl(client: AuthServerConnection) {
        if (!bindType.isAuth) return
        AuthBanManager.getInstance().cachedBans[bindType] = bans
        for ((key, value) in bans) {
            GameBanManager.onBan(bindType, key, value, true)
        }
    }
}