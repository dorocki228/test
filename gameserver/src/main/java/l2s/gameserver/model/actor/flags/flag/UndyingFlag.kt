package l2s.gameserver.model.actor.flags.flag

import l2s.commons.util.Chance
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author Bonux
 * @author Java-man
 */
class UndyingFlag : DefaultFlag() {

    val flag = AtomicBoolean(false)

    override fun start(): Boolean {
        flag.set(false)
        return super.start()
    }

    override fun start(owner: Any): Boolean {
        flag.set(false)
        return super.start(owner)
    }

    override fun start(owner: Any, chance: Chance): Boolean {
        flag.set(false)
        return super.start(owner, chance)
    }

    override fun stop(): Boolean {
        flag.set(false)
        return super.stop()
    }

    override fun stop(owner: Any): Boolean {
        flag.set(false)
        return super.stop(owner)
    }

    override fun clear() {
        flag.set(false)
        super.clear()
    }

}