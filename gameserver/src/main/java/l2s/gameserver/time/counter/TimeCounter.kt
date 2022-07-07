package l2s.gameserver.time.counter

import com.google.common.base.Stopwatch
import l2s.gameserver.ThreadPoolManager
import l2s.gameserver.logging.LogService
import l2s.gameserver.logging.LoggerType
import l2s.gameserver.model.OfflinePlayer
import l2s.gameserver.model.Player
import l2s.gameserver.network.l2.components.hwid.HwidHolder
import l2s.gameserver.security.HwidUtils
import org.apache.logging.log4j.message.SimpleMessage
import java.time.Duration
import java.util.concurrent.Future
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.withLock
import kotlin.concurrent.write

/**
 * @author Java-man
 * @since 25.05.2018
 */
object TimeCounter {

    private val threadPoolManager = ThreadPoolManager.getInstance()

    private val timers = mutableMapOf<Any, Counters>()
    private val lock = ReentrantReadWriteLock()

    @JvmOverloads
    fun start(
        owner: Any,
        counter: Any,
        action: TimeElapsedAction? = null,
        delay: Duration = Duration.ZERO,
        repeatable: Boolean = false
    ) {
        val counters = lock.write {
            timers.getOrPut(owner) { Counters() }
        }
        counters.start(counter, action, delay, repeatable)

        LogService.getInstance().log(LoggerType.TIME_COUNTER, SimpleMessage("$owner.$counter is started."))
    }

    fun stop(owner: Any, counter: Any): Collection<OfflinePlayer> {
        val counters = lock.read {
            timers[owner]
        } ?: return emptyList()

        val temp = counters.stop(counter)

        LogService.getInstance().log(LoggerType.TIME_COUNTER,
            SimpleMessage("$owner.$counter is stopped with origin result: $temp."))

        val result = HwidUtils.filterSameHwids(temp)

        LogService.getInstance().log(LoggerType.TIME_COUNTER,
            SimpleMessage("$owner.$counter is stopped with filtered result: $result."))

        return result
    }

    fun addPlayer(owner: Any, counter: Any, player: Player) {
        //val counters = requireNotNull(timers[owner]) { "Trying to add player to not exist timer." }
        val counters = lock.read { timers[owner] }
        val added = counters?.addPlayer(counter, player) ?: false
        if (added) {
            LogService.getInstance().log(LoggerType.TIME_COUNTER, SimpleMessage("$owner.$counter have $player added."))
        } else {
            LogService.getInstance().log(LoggerType.TIME_COUNTER,
                SimpleMessage("$owner.$counter don't have $player added: counter not exist."))
        }
    }

    @JvmOverloads
    fun removePlayer(owner: Any, counter: Any, player: Player, reset: Boolean = false) {
        //val counters = requireNotNull(timers[owner]) { "Trying to remove player to not exist timer." }
        val counters = lock.read { timers[owner] }
        val removed = counters?.removePlayer(counter, player, reset) ?: false
        if (removed) {
            LogService.getInstance().log(LoggerType.TIME_COUNTER, SimpleMessage("$owner.$counter have $player removed."))
        } else {
            LogService.getInstance().log(LoggerType.TIME_COUNTER,
                SimpleMessage("$owner.$counter don't have $player removed: counter not exist."))
        }
    }

    private class Counters {

        private val list = mutableMapOf<Any, Counter>()
        private val lock = ReentrantReadWriteLock()

        fun start(
            counter: Any,
            action: TimeElapsedAction? = null,
            delay: Duration = Duration.ZERO,
            repeatable: Boolean = false
        ) {
            //require(!list.containsKey(counter)) { "Trying to start already added counter." }

            lock.write {
                list.computeIfAbsent(counter) { Counter(action, delay, repeatable) }
            }
        }

        fun stop(counter: Any): Collection<OfflinePlayer> {
            //val temp = requireNotNull(list[counter]) { "Trying to stop not exist counter." }
            return lock.write {
                val temp = list.remove(counter) ?: return emptyList()
                return temp.stop()
            }
        }

        fun addPlayer(counter: Any, player: Player): Boolean {
            //val temp = requireNotNull(list[counter]) { "Trying to add player to not exist counter." }
            val temp = lock.read { list[counter] }
            return temp?.addPlayer(player) ?: false
        }

        fun removePlayer(counter: Any, player: Player, reset: Boolean): Boolean {
            //val temp = requireNotNull(list[counter]) { "Trying to add player to not exist counter." }
            val temp = lock.read { list[counter] }
            return temp?.removePlayer(player, reset) ?: false
        }

    }

    private class Counter(action: TimeElapsedAction?, delay: Duration, repeatable: Boolean) {

        private val players = mutableMapOf<Int, Stopwatch>()
        private val awardedHwids = mutableMapOf<HwidHolder, Int>()
        private val lock = ReentrantLock()

        private val task: Future<*>?

        init {
            if (action != null) {
                require(delay.toNanos() > 0) { "Delay should be positive." }

                val playerEntries = lock.withLock {
                    players.entries
                }
                val checkTask = CheckTask(action, delay, repeatable, playerEntries, awardedHwids)
                task = threadPoolManager.scheduleAtFixedRate(checkTask, delay.toMillis(), 5000)
            } else {
                task = null
            }
        }

        fun stop(): Collection<OfflinePlayer> {
            task?.cancel(false)

            return lock.withLock {
                players.entries
                    .onEach { entry ->
                        if (entry.value.isRunning) {
                            entry.value.stop()
                        }
                    }
                    .map { entry -> OfflinePlayer(entry.key, entry.value) }
                    .toList()
            }
        }

        fun addPlayer(player: Player): Boolean {
            val stopwatch = lock.withLock {
                players.getOrPut(player.objectId) { Stopwatch.createStarted() }
            }
            if (!stopwatch.isRunning) {
                stopwatch.start()
            }

            return true
        }

        fun removePlayer(player: Player, reset: Boolean): Boolean {
            val stopwatch = lock.withLock {
                players[player.objectId]
            }
            if (stopwatch != null) {
                if (stopwatch.isRunning) {
                    stopwatch.stop()
                }
                if (reset) {
                    stopwatch.reset()
                }

                return true
            } else {
                return false
            }
        }

        class CheckTask(
            private val action: TimeElapsedAction,
            private val delay: Duration,
            private val repeatable: Boolean,
            private val players: MutableSet<MutableMap.MutableEntry<Int, Stopwatch>>,
            private val awardedHwids: Map<HwidHolder, Int>,
            private val lock: ReentrantLock = ReentrantLock()

        ) : Runnable {

            override fun run() {
                var list: List<Pair<Stopwatch, OfflinePlayer>> = emptyList()
                lock.withLock {
                    list = players.map { (k, v) -> Pair(v, OfflinePlayer(k, v.elapsed())) }.toList()
                }
                for (pair: Pair<Stopwatch, OfflinePlayer> in list) {
                    val stopwatch = pair.first
                    val offlinePlayer = pair.second
                    if (canExecuteAction(offlinePlayer)) {
                        if (offlinePlayer.duration >= delay) {
                            stopwatch.reset()
                            if (repeatable) {
                                stopwatch.start()
                            }

                            action.action(offlinePlayer)
                        }
                    }
                }
            }

            private fun canExecuteAction(offlinePlayer: OfflinePlayer): Boolean {
                val awardedPlayer = awardedHwids[offlinePlayer.hwidHolder]
                if (awardedPlayer != null) {
                    if (awardedPlayer == offlinePlayer.playerObjectId) {
                        if (!repeatable) {
                            return false
                        }
                    } else {
                        LogService.getInstance().log(LoggerType.TIME_COUNTER,
                            SimpleMessage("${action.owner}.${action.counter} have not given reward to $offlinePlayer:" +
                                    " already awarded hwid."
                        ))

                        return false
                    }
                }

                val message = SimpleMessage("${action.owner}.${action.counter} have given reward to $offlinePlayer.")
                LogService.getInstance().log(LoggerType.TIME_COUNTER, message)

                return true
            }

        }

    }

}
