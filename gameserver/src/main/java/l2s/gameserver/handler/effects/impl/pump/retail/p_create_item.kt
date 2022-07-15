package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * An effect that allows the player to create dwarven recipe items up to a certain level.
 * @author Nik
 * @author Java-man
 */
class p_create_item(template: EffectTemplate) : EffectHandler(template) {

    private val recipeLevel = params.getInteger("p_create_item_param1")

    override fun checkPumpCondition(abnormal: Abnormal?, caster: Creature, target: Creature): Boolean {
        return target.isPlayer
    }

    override fun pumpStart(abnormal: Abnormal?, caster: Creature, target: Creature) {
        target.player.stat.createItemLevel = recipeLevel
    }

    override fun pumpEnd(abnormal: Abnormal?, caster: Creature, target: Creature) {
        target.player.stat.createItemLevel = 0
    }

}