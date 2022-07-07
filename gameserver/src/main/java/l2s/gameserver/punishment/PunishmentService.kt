package l2s.gameserver.punishment

import com.google.common.collect.ImmutableTable
import com.google.common.collect.Table
import l2s.gameserver.model.Player
import l2s.gameserver.network.l2.GameClient
import l2s.gameserver.utils.ItemFunctions
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/*
 * @author Java-man
 * @since 07.06.2019
 *
 * TODO: add punishments log
 */
object PunishmentService {

    private var holder: Table<PunishmentType, String, MutableList<PunishmentData>> = ImmutableTable.of()
    private val lock = ReentrantLock()

    fun load() {
        lock.withLock {
            holder = PunishmentDao.load(false)
        }
    }

    fun reload() {
        lock.withLock {
            holder = PunishmentDao.load(false)
        }
    }

    /**
     * Добавляем новый бан, указывая тип. Само значение вытаскиваем из объекта игрока.
     * @param type
     * @param player
     * @param reason
     */
    fun addPunishment(
        type: PunishmentType,
        player: Player,
        endDate: ZonedDateTime,
        punishedBy: String,
        reason: String
    ) {
        addPunishment(type, type.extractPunishmentTarget(player), endDate, punishedBy, reason)
    }

    /**
     * Добавляем новый бан, указывая тип и само значение.
     * @param type
     * @param target
     * @param reason
     */
    fun addPunishment(
        type: PunishmentType,
        target: String,
        endDate: ZonedDateTime,
        punishedBy: String,
        reason: String
    ) {
        lock.withLock {
            val data = PunishmentData(endDate, punishedBy, reason)
            val list = holder.get(type, target)
            if (list == null) {
                val mutableList = mutableListOf<PunishmentData>()
                mutableList.add(data)
                holder.put(type, target, mutableList)
            } else {
                list.add(data)
            }
        }

        PunishmentDao.insert(type, target, endDate, punishedBy, reason)

        type.onAddition(target, endDate, punishedBy, reason)
    }

    /**
     * Удаляем бан по его значению.
     * @param type
     * @param target
     */
    fun removePunishment(type: PunishmentType, target: String, endDate: ZonedDateTime) {
        val removed = lock.withLock {
            val list = holder.get(type, target) ?: return
            val elementToRemove = list.find { it.endDate == endDate } ?: return
            list.remove(elementToRemove)
        }

        if (removed) {
            PunishmentDao.delete(type, target, endDate)
        }

        type.onRemoval(target)
    }

    fun removePunishments(type: PunishmentType, player: Player) {
        removePunishments(type, type.extractPunishmentTarget(player))
    }

    fun removePunishments(type: PunishmentType, target: String) {
        lock.withLock {
            holder.remove(type, target)
        }

        PunishmentDao.deleteAll(type, target)

        type.onRemoval(target)
    }

    /**
     * Проверяем, забанены ли владелец данного соединения.
     * @return
     */
    fun isPunished(type: PunishmentType, player: Player): Boolean {
        val punishments = lock.withLock {
            removeExpiredPunishments(type, type.extractPunishmentTarget(player))
        }

        if (punishments != null && punishments.isNotEmpty()) {
            return true
        }

        return false
    }

    /**
     * Проверяем, забанены ли владелец данного соединения.
     * @return
     */
    fun isPunished(type: PunishmentType, target: String): Boolean {
        val punishments = lock.withLock {
            removeExpiredPunishments(type, target)
        }

        if (punishments != null && punishments.isNotEmpty()) {
            return true
        }

        return false
    }

    /**
     * Проверяем, забанены ли владелец данного соединения.
     * @return
     */
    fun isPunished(client: GameClient): Boolean {
        lock.withLock {
            // На всякий случай проверяем, есть ли нормальный HWID,
            // на тот случай, если залетит бан по NO_HWID, а то без защиты всех перебанит.
            val hwidHolder = client.hwidHolder
            if (hwidHolder != null) {
                if (isPunished(PunishmentType.HWID, hwidHolder.asString())) {
                    return true
                }
            }

            if (isPunished(PunishmentType.ACCOUNT, client.login)) {
                return true
            }

            if (isPunished(PunishmentType.IP, client.ipAddr)) {
                return true
            }
        }

        return false
    }

    private fun removeExpiredPunishments(type: PunishmentType, target: String): List<PunishmentData>? {
        val list = holder.get(type, target) ?: return null

        val now = ZonedDateTime.now()
        val elementsToRemove = list.filter { it.endDate.isBefore(now) }

        list.removeAll(elementsToRemove)

        return list
    }

    fun remainingTime(type: PunishmentType, target: String): Duration? {
        val punishments = lock.withLock {
            removeExpiredPunishments(type, target)
        }

        if (punishments != null && punishments.isNotEmpty()) {
            val endDate = punishments.maxBy { it.endDate }?.endDate
            return Duration.between(ZonedDateTime.now(), endDate)
        }

        return null
    }

    fun removeItemsFromAllCharacters(gameClient: GameClient, itemsId: IntArray): String {
        val builder = StringBuilder("Remove items from client ")
        builder.append(gameClient).append("|")
        for (objectId in gameClient.charSlotMapping) {
            val player = Player.restore(objectId, gameClient.hwidHolder)
            builder.append("char [").append(player.objectId).append("] items: ")
            for (itemId in itemsId) {
                val count = ItemFunctions.getItemCount(player, itemId)
                ItemFunctions.deleteItem(player, itemId, count, false)
                builder.append(itemId).append("-").append(count).append(" ")
            }
            player.kick()
            builder.append("|")
        }

        return builder.toString()
    }

}
