package l2s.gameserver.handler.effects.impl.pump

import l2s.gameserver.handler.effects.impl.AbstractBooleanStatEffect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.skills.AbnormalType
import l2s.gameserver.stats.BooleanStat
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * Custom effect.
 * @author Java-man
 */
class p_disable_invis(template: EffectTemplate) :
        AbstractBooleanStatEffect(template, BooleanStat.INVIS_DISABLED) {

    override fun pumpStart(abnormal: Abnormal?, caster: Creature, target: Creature) {
        target.abnormalList.stop(AbnormalType.HIDE)
    }

}
