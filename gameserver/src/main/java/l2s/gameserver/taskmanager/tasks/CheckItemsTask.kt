package l2s.gameserver.taskmanager.tasks

import l2s.gameserver.model.GameObjectsStorage
import l2s.gameserver.tables.ClanTable
import java.util.concurrent.TimeUnit

/**
 * @author Bonux
 */
class CheckItemsTask : AutomaticTask() {

    override fun doTask() {
        for (player in GameObjectsStorage.getPlayers(true, true)) {
            player.inventory.checkItems()
            for (servitor in player.getServitors { it.isPet }) {
                servitor.inventory?.checkItems()
            }
            player.warehouse.checkItems()
        }

        for (clan in ClanTable.getInstance().clans) {
            clan.warehouse.checkItems()
        }
    }

    override fun reCalcTime(start: Boolean): Long {
        return System.currentTimeMillis() + TASK_DELAY
    }

    companion object {

        private val TASK_DELAY = TimeUnit.MINUTES.toMillis(1)

    }

}
