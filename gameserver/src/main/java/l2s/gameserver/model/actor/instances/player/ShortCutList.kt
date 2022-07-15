package l2s.gameserver.model.actor.instances.player

import com.google.common.flogger.FluentLogger
import l2s.commons.dbutils.DbUtils
import l2s.gameserver.database.DatabaseFactory
import l2s.gameserver.model.Player
import l2s.gameserver.model.actor.instances.player.tasks.AutoShortcutTask
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.network.l2.s2c.SExActivateAutoShortcut
import l2s.gameserver.taskmanager.AiTaskManager

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.atomic.AtomicBoolean

class ShortCutList(private val player: Player) {
    private val shortCuts = ConcurrentHashMap<Int, ShortCut>()

    @Volatile
    private var autoShortcutsTask: ScheduledFuture<*>? = null
    private val autoShortcutsTaskDelay: Long = 500

    @Volatile
    private var autoShortcutsType = AutoShortCutType.NONE
    val autoShortcutsCast = AtomicBoolean(false)

    val allShortCuts: Collection<ShortCut>
        get() = shortCuts.values

    fun validate() {
        // Проверка ярлыков
        for (sc in shortCuts.values)
        // Удаляем ярлыки на предметы, которых нету в инвентаре
            if (sc.type == ShortCut.ShortCutType.ITEM)
                if (player.inventory.getItemByObjectId(sc.id) == null)
                    deleteShortCut(sc.slot, sc.page)
    }

    fun getShortCut(slot: Int, page: Int): ShortCut? {
        var sc: ShortCut? = shortCuts[slot + page * 12]
        // verify shortcut
        if (sc != null && sc.type == ShortCut.ShortCutType.ITEM)
            if (player.inventory.getItemByObjectId(sc.id) == null) {
                player.sendPacket(SystemMsg.THERE_ARE_NO_MORE_ITEMS_IN_THE_SHORTCUT)
                deleteShortCut(sc.slot, sc.page)
                sc = null
            }
        return sc
    }

    fun registerShortCut(shortcut: ShortCut) {
        val oldShortCut = shortCuts.put(shortcut.slot + 12 * shortcut.page, shortcut)
        registerShortCutInDb(shortcut, oldShortCut)
    }

    @Synchronized
    private fun registerShortCutInDb(shortcut: ShortCut, oldShortCut: ShortCut?) {
        if (oldShortCut != null)
            deleteShortCutFromDb(oldShortCut)

        var con: Connection? = null
        var statement: PreparedStatement? = null
        try {
            con = DatabaseFactory.getInstance().connection
            statement =
                con!!.prepareStatement("REPLACE INTO character_shortcuts SET object_id=?,slot=?,page=?,type=?,shortcut_id=?,level=?,character_type=?,class_index=?")
            statement!!.setInt(1, player.objectId)
            statement.setInt(2, shortcut.slot)
            statement.setInt(3, shortcut.page)
            statement.setInt(4, shortcut.type.ordinal)
            statement.setInt(5, shortcut.id)
            statement.setInt(6, shortcut.level)
            statement.setInt(7, shortcut.characterType)
            statement.setInt(8, player.activeClassId)
            statement.execute()
        } catch (e: Exception) {
            _log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log("could not store shortcuts:")
        } finally {
            DbUtils.closeQuietly(con, statement)
        }
    }

    /**
     * @param shortcut
     */
    private fun deleteShortCutFromDb(shortcut: ShortCut) {
        var con: Connection? = null
        var statement: PreparedStatement? = null
        try {
            con = DatabaseFactory.getInstance().connection
            statement =
                con!!.prepareStatement("DELETE FROM character_shortcuts WHERE object_id=? AND slot=? AND page=? AND class_index=?")
            statement!!.setInt(1, player.objectId)
            statement.setInt(2, shortcut.slot)
            statement.setInt(3, shortcut.page)
            statement.setInt(4, player.activeClassId)
            statement.execute()
        } catch (e: Exception) {
            _log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log("could not delete shortcuts:")
        } finally {
            DbUtils.closeQuietly(con, statement)
        }
    }

    /**
     * Удаляет ярлык с пользовательской панели по номеру страницы и слота.
     * @param slot
     * @param page
     */
    fun deleteShortCut(slot: Int, page: Int) {
        val old = shortCuts.remove(slot + page * 12) ?: return
        deleteShortCutFromDb(old)

        disableAutoShortcut(player, old)

        // При удалении с панели скила, на оффе шлется полный инит ярлыков
        // Обработка удаления предметных ярлыков - клиент сайд.
        /*if(old.getType() == ShortCut.ShortCutType.SKILL)
		{
			player.sendPacket(new ShortCutInitPacket(player));
			player.sendActiveAutoShots();
		}*/
    }

    /**
     * Удаляет ярлык предмета с пользовательской панели.
     * @param objectId
     */
    fun deleteShortCutByObjectId(objectId: Int) {
        for (shortcut in shortCuts.values)
            if (shortcut.type == ShortCut.ShortCutType.ITEM && shortcut.id == objectId)
                deleteShortCut(shortcut.slot, shortcut.page)
    }

    /**
     * Удаляет ярлык скила с пользовательской панели.
     * @param skillId
     */
    fun deleteShortCutBySkillId(skillId: Int) {
        for (shortcut in shortCuts.values)
            if (shortcut.type == ShortCut.ShortCutType.SKILL && shortcut.id == skillId)
                deleteShortCut(shortcut.slot, shortcut.page)
    }

    fun restore() {
        shortCuts.clear()
        var con: Connection? = null
        var statement: PreparedStatement? = null
        var rset: ResultSet? = null
        try {
            con = DatabaseFactory.getInstance().connection
            statement =
                con!!.prepareStatement("SELECT character_type, slot, page, type, shortcut_id, level FROM character_shortcuts WHERE object_id=? AND class_index=?")
            statement!!.setInt(1, player.objectId)
            statement.setInt(2, player.activeClassId)
            rset = statement.executeQuery()
            while (rset!!.next()) {
                val type: ShortCut.ShortCutType
                try {
                    type = ShortCut.ShortCutType.VALUES[rset.getInt("type")]
                } catch (e: Exception) {
                    continue
                }

                val slot = rset.getInt("slot")
                val page = rset.getInt("page")
                val id = rset.getInt("shortcut_id")
                val level = rset.getInt("level")
                val character_type = rset.getInt("character_type")

                val shortcut = ShortCut(player, slot, page, type, id, level, character_type)
                shortCuts[slot + page * 12] = shortcut
            }
        } catch (e: Exception) {
            _log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log("could not store shortcuts:")
        } finally {
            DbUtils.closeQuietly(con, statement, rset)
        }
    }

    fun getEnabledAutoShortcuts(type: AutoShortCutType): List<ShortCut> {
        return shortCuts.values
            .filter { shortCut -> shortCut.isAutoUseEnabled && type.check(shortCut) }
    }

    fun getDisabledAutoShortcuts(type: AutoShortCutType): List<ShortCut> {
        return shortCuts.values
            .filter { shortCut -> !shortCut.isAutoUseEnabled && type.check(shortCut) }
    }

    @Synchronized
    private fun startAutoShortcutsTask() {
        if (autoShortcutsTask == null || autoShortcutsTask!!.isCancelled) {
            val task = AutoShortcutTask(player.ref)
            autoShortcutsTask = AiTaskManager.getInstance().scheduleAtFixedRate(task, 10L, autoShortcutsTaskDelay)
            task.setTask(autoShortcutsTask)
        }
    }

    @Synchronized
    private fun stopAutoShortcutsTask() {
        if (autoShortcutsTask != null && !autoShortcutsTask!!.isCancelled) {
            autoShortcutsTask!!.cancel(false)
        }
    }

    fun startAutoShortcuts(type: AutoShortCutType) {
        /*if (autoShortcutsTask != null && !autoShortcutsTask!!.isCancelled) {
            return
        }*/

        val autoShortcuts = getDisabledAutoShortcuts(type)
        if (autoShortcuts.isEmpty()) {
            return
        }

        autoShortcutsType = autoShortcutsType.upgrade(type, true)

        autoShortcuts.forEach { autoShortcut ->
            enableAutoShortcut(player, autoShortcut, false)
        }

        startAutoShortcutsTask()
    }

    fun stopAutoShortcuts(type: AutoShortCutType) {
        if (autoShortcutsTask == null || autoShortcutsTask!!.isCancelled) {
            return
        }

        //autoShortcutsCast.set(false)

        autoShortcutsType = autoShortcutsType.upgrade(type, false)

        val autoShortcuts = getEnabledAutoShortcuts(type)
        if (autoShortcuts.isEmpty()) {
            return
        }

        autoShortcuts.forEach { autoShortcut ->
            disableAutoShortcut(player, autoShortcut, false)
        }

        if (autoShortcutsType == AutoShortCutType.NONE) {
            stopAutoShortcutsTask()
        }
    }

    @JvmOverloads
    fun enableAutoShortcut(player: Player, shortCut: ShortCut?, upgrade: Boolean = true) {
        if (shortCut == null || shortCut.autoShortCutType == AutoShortCutType.NONE || shortCut.isAutoUseEnabled) {
            return
        }

        if (upgrade) {
            autoShortcutsType = autoShortcutsType.upgrade(shortCut.autoShortCutType, true)
        }

        shortCut.isAutoUseEnabled = true
        player.sendPacket(SExActivateAutoShortcut.enable(shortCut))

        // enabled first shortcut
        if (upgrade && getEnabledAutoShortcuts(AutoShortCutType.ALL).size == 1) {
            startAutoShortcutsTask()
        }
    }

    @JvmOverloads
    fun disableAutoShortcut(player: Player, shortCut: ShortCut?, upgrade: Boolean = true) {
        if (shortCut == null || shortCut.autoShortCutType == AutoShortCutType.NONE || !shortCut.isAutoUseEnabled) {
            return
        }

        if (upgrade) {
            autoShortcutsType = autoShortcutsType.upgrade(shortCut.autoShortCutType, false)
        }

        shortCut.isAutoUseEnabled = false
        player.sendPacket(SExActivateAutoShortcut.disable(shortCut))

        if (upgrade && getEnabledAutoShortcuts(AutoShortCutType.ALL).isEmpty()) {
            stopAutoShortcutsTask()
        }
    }

    enum class AutoShortCutType {
        NONE {
            override fun upgrade(type: AutoShortCutType, enable: Boolean): AutoShortCutType {
                if (!enable) {
                    return NONE
                }

                return type
            }

            override fun check(shortCut: ShortCut): Boolean {
                return false
            }
        },
        SKILLS {
            override fun upgrade(type: AutoShortCutType, enable: Boolean): AutoShortCutType {
                if (enable && type == ITEMS) {
                    return ALL
                }

                if (!enable && type == SKILLS) {
                    return NONE
                }

                return SKILLS
            }

            override fun check(shortCut: ShortCut): Boolean {
                return shortCut.type == ShortCut.ShortCutType.SKILL
            }
        },
        ITEMS {
            override fun upgrade(type: AutoShortCutType, enable: Boolean): AutoShortCutType {
                if (enable && type == SKILLS) {
                    return ALL
                }

                if (!enable && type == ITEMS) {
                    return NONE
                }

                return ITEMS
            }

            override fun check(shortCut: ShortCut): Boolean {
                return shortCut.type == ShortCut.ShortCutType.ITEM
            }
        },
        ALL {
            override fun upgrade(type: AutoShortCutType, enable: Boolean): AutoShortCutType {
                if (!enable && type == ALL) {
                    return NONE
                }

                if (!enable && type == ITEMS) {
                    return SKILLS
                }

                if (!enable && type == SKILLS) {
                    return ITEMS
                }

                return ALL
            }

            override fun check(shortCut: ShortCut): Boolean {
                return true
            }
        };

        internal abstract fun check(shortCut: ShortCut): Boolean

        internal abstract fun upgrade(type: AutoShortCutType, enable: Boolean): AutoShortCutType

    }

    companion object {
        private val _log = FluentLogger.forEnclosingClass()
    }

}