package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.skills.SkillEntry
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * @author Sdw
 * @author Java-man
 *
 * @since 14.10.2019
 */
class p_vampiric_attack(template: EffectTemplate) : EffectHandler(template) {

    private val amount = params.getDouble("p_vampiric_attack_param1")
    private val sum = amount * params.getDouble("p_vampiric_attack_param2")

    override fun pump(
            target: Creature,
            skillEntry: SkillEntry?
    ) {
        if (skillEntry != null) {
            target.stat.mergeAdd(DoubleStat.ABSORB_DAMAGE_PERCENT, amount / 100.0, skillEntry)
        } else {
            target.stat.mergeAdd(DoubleStat.ABSORB_DAMAGE_PERCENT, amount / 100.0, skill)
        }
        target.stat.addToVampiricSum(sum)
    }

}