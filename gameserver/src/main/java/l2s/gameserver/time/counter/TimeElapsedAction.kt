package l2s.gameserver.time.counter

import l2s.gameserver.model.OfflinePlayer

/**
 * @author Java-man
 * @since 25.05.2019
 */
abstract class TimeElapsedAction(val owner: Any, val counter: Any) {

    abstract fun action(player: OfflinePlayer)

}