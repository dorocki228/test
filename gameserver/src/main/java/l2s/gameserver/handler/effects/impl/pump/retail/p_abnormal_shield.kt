package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.impl.AbstractBooleanStatEffect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.stats.BooleanStat
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * An effect that blocks a debuff. Acts like DOTA's Linken Sphere.
 * @author Nik
 * @author Java-man
 *
 * @since 19.10.2019
 */
class p_abnormal_shield(template: EffectTemplate) :
        AbstractBooleanStatEffect(template, BooleanStat.ABNORMAL_SHIELD) {

    private val times = params.getInteger("times")

    override fun pumpStart(abnormal: Abnormal?, caster: Creature, target: Creature) {
        target.stat.setAbnormalShieldBlocks(times)
    }

    override fun pumpEnd(abnormal: Abnormal?, caster: Creature, target: Creature) {
        target.stat.setAbnormalShieldBlocks(Integer.MIN_VALUE)
    }

}