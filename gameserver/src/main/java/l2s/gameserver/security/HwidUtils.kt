package l2s.gameserver.security

import l2s.gameserver.GameServer
import l2s.gameserver.model.HaveHwid
import l2s.gameserver.model.Player
import l2s.gameserver.network.l2.GameClient
import l2s.gameserver.network.l2.components.hwid.DefaultHwidHolder
import l2s.gameserver.network.l2.components.hwid.EmptyHwidHolder
import l2s.gameserver.network.l2.components.hwid.HwidHolder
import org.apache.logging.log4j.LogManager

object HwidUtils {
    private val LOGGER = LogManager.getLogger(HwidUtils::class.java)

    fun createHwidHolder(hwid: String): HwidHolder {
        return if (GameServer.DEVELOP) {
            EmptyHwidHolder(hwid)
        } else {
            DefaultHwidHolder(hwid)
        }
    }

    fun isSameHWID(player1: Player, player2: Player): Boolean {
        return if (player1 == player2) true else isSameHWID(player1.hwidHolder, player2.hwidHolder)
    }

    fun isSameHWID(client1: GameClient, hwid2: String): Boolean {
        return isSameHWID(client1.hwidString, hwid2)
    }

    fun isSameHWID(client1: GameClient, client2: GameClient): Boolean {
        return isSameHWID(client1.hwidHolder, client2.hwidHolder)
    }

    fun isSameHWID(hwid1: String?, hwid2: String?): Boolean {
        if (GameServer.DEVELOP)
            return false

        if (hwid1 == null || hwid2 == null)
            return true

        val hwidHolder11 = DefaultHwidHolder(hwid1)
        val hwidHolder22 = DefaultHwidHolder(hwid2)

        return hwidHolder11 == hwidHolder22
    }

    fun isSameHWID(haveHwid1: HaveHwid, haveHwid2: HaveHwid): Boolean {
        return isSameHWID(haveHwid1.hwidHolder, haveHwid2.hwidHolder)
    }

    fun isSameHWID(hwidHolder1: HwidHolder?, hwidHolder2: HwidHolder?): Boolean {
        if (GameServer.DEVELOP)
            return false

        return if (hwidHolder1 == null || hwidHolder2 == null) true else hwidHolder1 == hwidHolder2

    }

    fun <T : HaveHwid> filterSameHwids(players: Collection<T>): Collection<T> {
        if (GameServer.DEVELOP)
            return players

        return players
            .onEach { player ->
                if (player.hwidHolder == null)
                    LOGGER.error("{} don't have hwid for some reason.", player)
            }
            .filter { player -> player.hwidHolder != null }
            .distinctBy { it.hwidHolder }
            .toList()

    }

    fun <T : HaveHwid> haveSameHwids(players: Collection<T>): Boolean {
        if (GameServer.DEVELOP)
            return false

        val distinctCount = players
            .onEach { player ->
                if (player.hwidHolder == null)
                    LOGGER.error("{} don't have hwid for some reason.", player)
            }
            .filter { player -> player.hwidHolder != null }
            .distinctBy { it.hwidHolder }
            .count()
        return distinctCount < players.size
    }
}
