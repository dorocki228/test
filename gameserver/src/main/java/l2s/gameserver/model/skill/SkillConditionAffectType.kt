package l2s.gameserver.model.skill

/**
 * @author Sdw
 */
enum class SkillConditionAffectType {
    SELF,
    TARGET,
    BOTH;

    companion object {
        fun find(name: String): SkillConditionAffectType {
            return when (name) {
                "self" -> SELF
                "target" -> TARGET
                "oct_caster" -> SELF
                "oct_target" -> TARGET
                "oct_both" -> BOTH
                else -> error("Can't find $name")
            }
        }
    }

}