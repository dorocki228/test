package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.templates.item.ItemGrade
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * An effect that removes equipment grade penalty. Its the base effect for the grade penalty mechanics.
 * @author Nik
 * @author Java-man
 *
 * @since 19.10.2019
 */
class p_remove_equip_penalty(template: EffectTemplate) : EffectHandler(template) {

    private val grade = params.getEnum(
            "p_remove_equip_penalty_param1",
            ItemGrade::class.java,
            true
    )

    override fun checkPumpCondition(abnormal: Abnormal?, caster: Creature, target: Creature): Boolean {
        return target.isPlayer
    }

    override fun pumpStart(abnormal: Abnormal?, caster: Creature, target: Creature) {
        val player = target.player
        if (player != null) {
            player.stat.setExpertiseLevel(grade)
        }
    }

    override fun pumpEnd(abnormal: Abnormal?, caster: Creature, target: Creature) {
        val player = target.player
        if (player != null) {
            player.stat.setExpertiseLevel(ItemGrade.NONE)
        }
    }

}