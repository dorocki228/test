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
class p_block_attack(template: EffectTemplate) :
        AbstractBooleanStatEffect(template, BooleanStat.ATTACK_MUTED) {

    override fun pumpStart(abnormal: Abnormal?, caster: Creature, target: Creature) {
        target.abortAttack(true, true)
    }

}