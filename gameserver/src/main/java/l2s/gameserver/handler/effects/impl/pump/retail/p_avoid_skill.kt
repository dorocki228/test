package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * Note: In retail this effect doesn't stack.
 * It appears that the active value is taken from the last such effect.
 *
 * @author Sdw
 * @author Java-man
 *
 * @since 17.10.2019
 */
class p_avoid_skill(template: EffectTemplate) : EffectHandler(template) {

    private val magicType = Skill.SkillMagicType.values()[params.getInteger("p_avoid_skill_param1")]
    private val amount = params.getDouble("p_avoid_skill_param2")

    override fun pumpStart(abnormal: Abnormal?, caster: Creature, target: Creature) {
        target.stat.addSkillEvasionTypeValue(magicType, amount)
    }

    override fun pumpEnd(abnormal: Abnormal?, caster: Creature, target: Creature) {
        target.stat.removeSkillEvasionTypeValue(magicType, amount)
    }

}