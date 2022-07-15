package l2s.gameserver.model.actor.flags.flag

import l2s.commons.util.AlwaysSuccessfulChance
import l2s.commons.util.Chance
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author Java-man
 */
open class DefaultFlag {

    private val state = AtomicBoolean(false)
    private val statusOwners: MutableMap<Any, Chance> = ConcurrentHashMap(2)
    @Volatile
    private var disabledUntilNanos: Long = 0

    fun get(): Boolean {
        if (disabledUntilNanos >= System.nanoTime()) {
            return false
        }

        if (state.get()) {
            return true
        }

        if (statusOwners.isEmpty()) {
            return false
        }

        return statusOwners.values.find { it.roll() } != null
    }

    open fun start(): Boolean {
        if (disabledUntilNanos >= System.nanoTime()) {
            return false
        }

        return state.compareAndSet(false, true);
    }

    open fun start(owner: Any): Boolean {
        if (disabledUntilNanos >= System.nanoTime()) {
            return false
        }

        return statusOwners.put(owner, AlwaysSuccessfulChance) == null
    }

    open fun start(owner: Any, chance: Chance): Boolean {
        if (disabledUntilNanos >= System.nanoTime()) {
            return false
        }

        return statusOwners.put(owner, chance) == null
    }

    open fun stop(): Boolean {
        return state.compareAndSet(true, false);
    }

    open fun stop(owner: Any): Boolean {
        return statusOwners.remove(owner) != null
    }

    open fun clear() {
        state.set(false)
        statusOwners.clear()
    }

    fun isDisabled(): Boolean {
        return disabledUntilNanos >= System.nanoTime()
    }

    fun disableUntil(nanos: Long) {
        disabledUntilNanos = nanos
    }

}