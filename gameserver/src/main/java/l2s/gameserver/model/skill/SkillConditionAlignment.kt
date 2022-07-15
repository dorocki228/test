package l2s.gameserver.model.skill

import l2s.gameserver.model.Player

/**
 * @author Sdw
 */
enum class SkillConditionAlignment {
    LAWFUL {
        override fun test(player: Player): Boolean {
            return player.karma >= 0
        }
    },
    CHAOTIC {
        override fun test(player: Player): Boolean {
            return player.karma < 0
        }
    };

    abstract fun test(player: Player): Boolean

}