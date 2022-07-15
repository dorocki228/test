package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.impl.AbstractBooleanStatEffect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.stats.BooleanStat
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * Block Chat effect implementation.
 * @author BiggBoss
 * @author Java-man
 *
 * @since 19.10.2019
 */
class p_block_chat(template: EffectTemplate) :
        AbstractBooleanStatEffect(template, BooleanStat.BLOCK_CHAT) {

    override fun checkPumpCondition(abnormal: Abnormal?, caster: Creature, target: Creature): Boolean {
        return target.isPlayer
    }

}