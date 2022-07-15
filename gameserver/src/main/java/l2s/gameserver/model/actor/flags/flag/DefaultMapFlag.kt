package l2s.gameserver.model.actor.flags.flag

import l2s.gameserver.model.Creature
import java.util.concurrent.ConcurrentHashMap

/**
 * @author Java-man
 */
class DefaultMapFlag {

    private val statusOwners = ConcurrentHashMap<Any, Creature>(1)

    fun get(creature: Creature): Boolean {
        if (statusOwners.isEmpty()) {
            return false
        }

        return statusOwners.values.contains(creature)
    }

    @JvmOverloads
    fun start(owner: Any = NO_OWNER, creature: Creature): Boolean {
        return statusOwners.put(owner, creature) == null
    }

    @JvmOverloads
    fun stop(owner: Any = NO_OWNER): Boolean {
        return statusOwners.remove(owner) != null
    }

    fun clear() {
        statusOwners.clear()
    }

    companion object {

        private const val NO_OWNER = "NO_OWNER"

    }

}