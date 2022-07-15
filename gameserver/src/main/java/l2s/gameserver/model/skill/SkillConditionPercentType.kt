package l2s.gameserver.model.skill

/**
 * @author Sdw
 */
enum class SkillConditionPercentType {
    UP {
        override fun test(x1: Int, x2: Int): Boolean {
            return x1 >= x2
        }
    },
    DOWN {
        override fun test(x1: Int, x2: Int): Boolean {
            return x1 <= x2
        }
    };

    abstract fun test(x1: Int, x2: Int): Boolean
}