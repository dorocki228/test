package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.impl.AbstractBooleanStatEffect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.stats.BooleanStat
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * Cubic Block Actions effect implementation.
 * @author mkizub
 * @author Java-man
 */
class cub_block_act(template: EffectTemplate) :
        AbstractBooleanStatEffect(template, BooleanStat.BLOCK_ACTIONS) {

    override fun pumpStart(abnormal: Abnormal?, caster: Creature, target: Creature) {
        target.stopActions()
    }

}