package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * Crystal Grade Modify effect implementation.
 * @author Zoey76
 * @author Java-man
 *
 * @since 19.10.2019
 */
class p_crystal_grade_modify(template: EffectTemplate) : EffectHandler(template) {

    private val _amount = params.getInteger("p_crystal_grade_modify_param1")

    override fun checkPumpCondition(abnormal: Abnormal?, caster: Creature, target: Creature): Boolean {
        return target.isPlayer
    }

    override fun pumpStart(abnormal: Abnormal?, caster: Creature, target: Creature) {
        val player = target.player
        if (player != null) {
            player.stat.setExpertisePenaltyBonus(_amount)
        }
    }

    override fun pumpEnd(abnormal: Abnormal?, caster: Creature, target: Creature) {
        val player = target.player
        if (player != null) {
            player.stat.setExpertisePenaltyBonus(0)
        }
    }

}