package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.skills.SkillEntry
import l2s.gameserver.templates.skill.EffectTemplate
import org.apache.velocity.runtime.parser.node.MathUtils

/**
 * @author Sdw
 * @author Java-man
 *
 * @since 21.10.2019
 */
class p_wrong_casting(template: EffectTemplate) : EffectHandler(template) {

    private val magicType = Skill.SkillMagicType.values()[params.getInteger("p_wrong_casting_param1")]
    private val chance = params.getDouble("p_wrong_casting_param2")

    override fun pump(
            target: Creature,
            skillEntry: SkillEntry?
    ) {
        target.stat.mergeCastChanceValue(magicType, chance / 100.0, MathUtils::add)
    }

}