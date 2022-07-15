package l2s.gameserver.handler.effects.impl.pump

import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.handler.effects.impl.AbstractBooleanStatEffect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.stats.BooleanStat
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * @author Java-man
 */
class p_autoloot_custom(template: EffectTemplate) : EffectHandler(template) {

    override fun pumpStart(abnormal: Abnormal?, caster: Creature, target: Creature) {
        val player = target.player ?: return
        player.setAutoLoot(true)
    }

    override fun pumpEnd(abnormal: Abnormal?, caster: Creature, target: Creature) {
        val player = target.player ?: return
        player.setAutoLoot(false)
    }

}