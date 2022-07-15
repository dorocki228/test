package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.impl.AbstractBooleanStatEffect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.stats.BooleanStat
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * Disarm effect implementation.
 * @author nBd
 * @author Java-man
 */
class p_disarm(template: EffectTemplate) :
        AbstractBooleanStatEffect(template, BooleanStat.DISARMED) {

    override fun checkPumpCondition(abnormal: Abnormal?, caster: Creature, target: Creature): Boolean {
        return target.isPlayer
    }

    override fun pumpStart(abnormal: Abnormal?, caster: Creature, target: Creature) {
        target.player?.unEquipWeapon()
    }

}