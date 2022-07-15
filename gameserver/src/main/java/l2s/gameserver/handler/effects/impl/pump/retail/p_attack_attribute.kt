package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.model.base.AttributeType
import l2s.gameserver.skills.SkillEntry
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * @author Sdw
 * @author Java-man
 *
 * @since 13.10.2019
 */
class p_attack_attribute(template: EffectTemplate) : EffectHandler(template) {

    private val attribute = AttributeType.find(params.getString("p_attack_attribute_param1"))
    private val amount = params.getDouble("p_attack_attribute_param2")

    override fun pump(
            target: Creature,
            skillEntry: SkillEntry?
    ) {
        if (skillEntry != null) {
            target.stat.mergeAdd(attribute.attack, amount, skillEntry)
        } else {
            target.stat.mergeAdd(attribute.attack, amount, skill)
        }
    }

}