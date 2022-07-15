package l2s.gameserver.model.skill

/**
 * @author NosBit
 */
enum class SkillConditionScope(val xmlNodeName: String) {
    GENERAL("operate_cond"),
    TARGET("target_operate_cond"),
    PASSIVE("passive_conditions");

    companion object {
        private val XML_NODE_NAME_TO_SKILL_CONDITION_SCOPE: Map<String, SkillConditionScope> =
                values().associateBy { it.xmlNodeName }

        fun findByXmlNodeName(xmlNodeName: String): SkillConditionScope? {
            return XML_NODE_NAME_TO_SKILL_CONDITION_SCOPE[xmlNodeName]
        }
    }

}