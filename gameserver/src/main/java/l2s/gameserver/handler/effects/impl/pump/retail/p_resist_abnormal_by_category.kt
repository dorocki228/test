package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.model.base.DispelSlotType
import l2s.gameserver.skills.SkillEntry
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.stats.StatModifierType
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * @author Sdw
 * @author Java-man
 *
 * @since 13.10.2019
 */
class p_resist_abnormal_by_category(template: EffectTemplate) : EffectHandler(template) {

    private val slot =
            params.getEnum(
                    "p_resist_abnormal_by_category_param1",
                    DispelSlotType::class.java,
                    true
            )
    private val amount = params.getDouble("p_resist_abnormal_by_category_param2")
    private val modifierType: StatModifierType =
            params.getEnum(
                    "p_resist_abnormal_by_category_param3",
                    StatModifierType::class.java,
                    true
            )

    override fun pump(
            target: Creature,
            skillEntry: SkillEntry?
    ) {
        when (slot) {
            DispelSlotType.SLOT_BUFF -> {
                val mul = 1 + amount / 100.0
                if (skillEntry != null) {
                    target.stat.mergeMul(DoubleStat.RESIST_ABNORMAL_BUFF, mul, skillEntry)
                } else {
                    target.stat.mergeMul(DoubleStat.RESIST_ABNORMAL_BUFF, mul, skill)
                }
            }
            DispelSlotType.SLOT_DEBUFF -> {
                val mul = 1 + amount / 100.0
                if (skillEntry != null) {
                    target.stat.mergeMul(DoubleStat.RESIST_ABNORMAL_DEBUFF, mul, skillEntry)
                } else {
                    target.stat.mergeMul(DoubleStat.RESIST_ABNORMAL_DEBUFF, mul, skill)
                }
            }
            DispelSlotType.MULTI_BUFF -> {
                val mul = 1 + amount / 100.0
                if (skillEntry != null) {
                    target.stat.mergeMul(DoubleStat.RESIST_ABNORMAL_MULTI_BUFF, mul, skillEntry)
                } else {
                    target.stat.mergeMul(DoubleStat.RESIST_ABNORMAL_MULTI_BUFF, mul, skill)
                }
            }
            else -> error("$slot")
        }
    }

}