package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.templates.item.ItemGrade
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * An effect that allows the player to crystallize items up to a certain grade.
 * @author Nik
 * @author Java-man
 */
class p_crystallize(template: EffectTemplate) : EffectHandler(template) {

    private val grade = params.getEnum(
            "p_crystallize_param1",
            ItemGrade::class.java,
            true
    )

    override fun checkPumpCondition(abnormal: Abnormal?, caster: Creature, target: Creature): Boolean {
        return target.isPlayer
    }

    override fun pumpStart(abnormal: Abnormal?, caster: Creature, target: Creature) {
        target.player.stat.crystallizeGrade = grade
    }

    override fun pumpEnd(abnormal: Abnormal?, caster: Creature, target: Creature) {
        target.player.stat.crystallizeGrade = ItemGrade.NONE
    }

}