package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.impl.AbstractBooleanStatEffect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.stats.BooleanStat
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * Cubic Block Move effect implementation.
 * @author Sdw
 * @author Java-man
 */
class cub_block_move(template: EffectTemplate) :
        AbstractBooleanStatEffect(template, BooleanStat.BLOCK_MOVE) {

    override fun pumpStart(abnormal: Abnormal?, caster: Creature, target: Creature) {
        target.movement.stopMove()
    }

}