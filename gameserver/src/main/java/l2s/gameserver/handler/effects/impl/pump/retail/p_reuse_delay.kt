package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.commons.math.MathUtils
import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill.SkillMagicType
import l2s.gameserver.skills.SkillEntry
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * @author Sdw
 * @author Java-man
 *
 * @since 13.10.2019
 */
class p_reuse_delay(template: EffectTemplate) : EffectHandler(template) {

    private val magicType = SkillMagicType.values()[params.getInteger("p_reuse_delay_param1")]
    private val amount = params.getDouble("p_reuse_delay_param2")

    override fun pump(
            target: Creature,
            skillEntry: SkillEntry?
    ) {
        val value = amount / 100.0 + 1
        target.stat.mergeReuseTypeValue(magicType, value, MathUtils::mul)
    }

}