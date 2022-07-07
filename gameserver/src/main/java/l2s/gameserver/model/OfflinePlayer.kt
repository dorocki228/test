package l2s.gameserver.model

import com.google.common.base.Stopwatch
import l2s.gameserver.dao.CharacterVariablesDAO
import l2s.gameserver.model.actor.instances.player.CharacterVariable
import l2s.gameserver.model.base.Fraction
import l2s.gameserver.network.l2.components.CustomMessage
import l2s.gameserver.network.l2.components.hwid.HwidHolder
import l2s.gameserver.security.HwidUtils
import l2s.gameserver.taskmanager.DelayedItemsManager
import l2s.gameserver.utils.ItemFunctions
import java.time.Duration

/**
 * Maybe this player is online, maybe not.
 *
 * @author Java-man
 * @since 03.02.2019
 */
data class OfflinePlayer(val playerObjectId: Int, val duration: Duration) : HaveHwid {

    private val lazyHwidHolder: HwidHolder by lazy {
        val hwid = ifPlayerOnlineOrOffline({
            it.hwidHolder.asString()
        }, {
            CharacterVariablesDAO.getInstance().getVarFromPlayer(playerObjectId, "last_hwid")
        })

        requireNotNull(hwid) { "Hwid for player $playerObjectId is null." }

        HwidUtils.createHwidHolder(hwid)
    }

    val fraction: Fraction by lazy {
        val result = ifPlayerOnlineOrOffline({
            it.fraction
        }, {
            val variable = CharacterVariablesDAO.getInstance().getVarFromPlayer(playerObjectId, "fraction")
            if (variable != null) {
                Fraction.getIfPresent(variable.toInt())
            } else {
                Fraction.NONE
            }
        })

        requireNotNull(result) { "Fraction for player $playerObjectId is null." }

        result
    }

    constructor(playerObjectId: Int, stopwatch: Stopwatch) : this(playerObjectId, stopwatch.elapsed())

    override fun getHwidHolder(): HwidHolder {
        return lazyHwidHolder
    }

    @JvmOverloads
    fun addItem(itemId: Int, count: Long, enchantLevel: Int = 0, notify: Boolean = true, description: String) {
        ifPlayerOnlineOrOffline({
            ItemFunctions.addItem(it, itemId, count, enchantLevel, notify)
        }, {
            DelayedItemsManager.addDelayed(playerObjectId, itemId, count, enchantLevel, description)
        })
    }

    fun sendMessage(message: String) {
        val player = GameObjectsStorage.getPlayer(playerObjectId)
        player?.sendMessage(message)
    }

    fun sendMessage(message: CustomMessage) {
        val player = GameObjectsStorage.getPlayer(playerObjectId)
        player?.sendPacket(message)
    }

    @JvmOverloads
    fun setVar(name: String, value: String, expireTime: Long = -1) {
        ifPlayerOnlineOrOffline({
            it.setVar(name, value)
        }, {
            val variable = CharacterVariable(name, value, expireTime)
            CharacterVariablesDAO.getInstance().insert(playerObjectId, variable)
        })
    }

    @JvmOverloads
    fun setVar(name: String, value: Int, expireTime: Long = -1) {
        ifPlayerOnlineOrOffline({
            it.setVar(name, value)
        }, {
            val variable = CharacterVariable(name, value.toString(), expireTime)
            CharacterVariablesDAO.getInstance().insert(playerObjectId, variable)
        })
    }

    fun unsetVar(name: String) {
        ifPlayerOnlineOrOffline({
            it.unsetVar(name)
        }, {
            CharacterVariablesDAO.getInstance().delete(playerObjectId, name)
        })
    }

    fun getVarInt(name: String, default: Int): Int {
        return ifPlayerOnlineOrOffline({
            it.getVarInt(name, default)
        }, {
            CharacterVariablesDAO.getInstance().getVarFromPlayer(playerObjectId, name)?.toInt()
                ?: default
        })
    }

    inline fun ifPlayerSpendEnoughTimeOrElse(
        minimumTime: Duration, onSuccess: (OfflinePlayer) -> Unit,
        onFail: (OfflinePlayer) -> Unit
    ) {
        if (duration >= minimumTime) {
            onSuccess(this)
        } else {
            onFail(this)
        }
    }

    inline fun ifPlayerOnline(onlineAction: (Player) -> Unit) {
        val player = GameObjectsStorage.getPlayer(playerObjectId)
        if (player != null) {
            onlineAction(player)
        }
    }

    inline fun <T> ifPlayerOnlineOrOffline(
        onlineAction: (Player) -> T,
        offlineAction: (OfflinePlayer) -> T
    ): T {
        val player = GameObjectsStorage.getPlayer(playerObjectId)
        return if (player != null && player.isOnline && !player.isInOfflineMode
            && player.isConnected && !player.isLogoutStarted
        ) {
            onlineAction(player)
        } else {
            offlineAction(this)
        }
    }

    override fun toString(): String {
        return "OfflinePlayer{" +
                "playerObjectId=" + playerObjectId +
                ", duration=" + duration +
                ", hwid=" + lazyHwidHolder.asString() +
                '}'.toString()
    }
}
