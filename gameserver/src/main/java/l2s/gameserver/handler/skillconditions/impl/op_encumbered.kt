package l2s.gameserver.handler.skillconditions.impl

import l2s.gameserver.handler.skillconditions.SkillCondition
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.templates.StatsSet

/**
 * TODO: Verify me, also should Quest items be counted?
 * @author UnAfraid
 * @author Java-man
 */
class op_encumbered(params: StatsSet) : SkillCondition(params) {

    private val _slotsPercent = params.getInteger("op_encumbered_param1")
    private val _weightPercent = params.getInteger("op_encumbered_param2")

    override fun canUse(caster: Creature, skill: Skill, target: Creature?): Boolean {
        val player = caster.player ?: return false

        val inventory = player.inventory
        val currentSlotsPercent = calcPercent(player.inventoryLimit, inventory.size)
        if (currentSlotsPercent < _slotsPercent) {
            return false
        }
        val currentWeightPercent = calcPercent(player.maxLoad, player.currentLoad)
        if (currentWeightPercent < _weightPercent) {
            return false
        }
        return true
    }

    private fun calcPercent(max: Int, current: Int): Int {
        return 100 - current * 100 / max
    }

}