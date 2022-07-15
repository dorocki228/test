package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.impl.AbstractBooleanStatEffect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.stats.BooleanStat
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * Protection Blessing effect implementation.
 * @author kerberos_20
 * @author Java-man
 */
class p_pk_protect(template: EffectTemplate) :
        AbstractBooleanStatEffect(template, BooleanStat.PROTECTION_BLESSING) {

    override fun checkPumpCondition(abnormal: Abnormal?, caster: Creature, target: Creature): Boolean {
        return target.isPlayer
    }

}