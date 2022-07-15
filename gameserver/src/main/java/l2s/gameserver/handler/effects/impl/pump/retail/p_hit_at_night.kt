package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.GameTimeController
import l2s.gameserver.handler.effects.impl.AbstractDoubleStatEffect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * @author Sdw
 * @author Java-man
 *
 * @since 12.10.2019
 */
class p_hit_at_night(template: EffectTemplate) :
        AbstractDoubleStatEffect(template, DoubleStat.ACCURACY_COMBAT) {

    override fun checkPumpCondition(abnormal: Abnormal?, caster: Creature, target: Creature): Boolean {
        return super.checkPumpCondition(abnormal, caster, target) && GameTimeController.getInstance().isNowNight
    }

}