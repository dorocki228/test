package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.skills.SkillEntry
import l2s.gameserver.stats.StatModifierType
import l2s.gameserver.templates.skill.EffectTemplate
import org.apache.velocity.runtime.parser.node.MathUtils

/**
 * Wrong Casting effect implementation.
 *
 * @since 21.10.2019
 */
class p_magic_mp_cost(template: EffectTemplate) : EffectHandler(template) {

    private val magicType = Skill.SkillMagicType.values()[params.getInteger("p_magic_mp_cost_param1")]
    private val amount: Double = params.getDouble("p_magic_mp_cost_param2")
    private val modifierType: StatModifierType =
            params.getEnum(
                    "p_magic_mp_cost_param3",
                    StatModifierType::class.java,
                    true
            )

    override fun pump(
            target: Creature,
            skillEntry: SkillEntry?
    ) {
        val value = amount / 100.0
        target.stat.mergeMpConsumeTypeValue(magicType, value, MathUtils::add)
    }

}