package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.GameTimeController
import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.model.base.BaseStats
import l2s.gameserver.skills.SkillEntry
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * @author Sdw
 * @author Java-man
 *
 * @since 12.10.2019
 */
class p_stat_up_at_night(template: EffectTemplate) : EffectHandler(template) {

    private val stat = BaseStats.values()[params.getInteger("p_stat_up_at_night_param1")]
    private val amount = params.getDouble("p_stat_up_at_night_param2")

    override fun checkPumpCondition(abnormal: Abnormal?, caster: Creature, target: Creature): Boolean {
        return GameTimeController.getInstance().isNowNight
    }

    override fun pump(
            target: Creature,
            skillEntry: SkillEntry?
    ) {
        if (skillEntry != null) {
            target.stat.mergeAdd(stat.stat, amount, skillEntry)
        } else {
            target.stat.mergeAdd(stat.stat, amount, skill)
        }
    }

}