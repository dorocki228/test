package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.ai.CtrlIntention
import l2s.gameserver.handler.effects.impl.AbstractBooleanStatEffect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.stats.BooleanStat
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * Betray effect implementation.
 * @author decad
 * @author Java-man
 *
 * @since 01.11.2019
 */
class p_betray(template: EffectTemplate) :
        AbstractBooleanStatEffect(template, BooleanStat.BETRAYED) {

    override fun pumpEnd(abnormal: Abnormal?, caster: Creature, target: Creature) {
        val targetSummon = target.asServitor() ?: return

        targetSummon.ai.intention = CtrlIntention.AI_INTENTION_ACTIVE
    }

}