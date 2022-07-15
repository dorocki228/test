package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.stats.Formulas
import l2s.gameserver.templates.skill.EffectTemplate
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author Sdw
 * @author Java-man
 */
class i_skill_turning(template: EffectTemplate) : i_abstract_effect(template) {

    private val _magicType = Skill.SkillMagicType.values()[params.getInteger("i_skill_turning_param1")]
    private val chance = params.getDouble("i_skill_turning_param2")

    override fun calcSuccess(caster: Creature, target: Creature, skill: Skill): Boolean {
        if (!target.isCreature) {
            return false
        }

        if (!Formulas.calcProbability(chance, caster, target.asCreature(), skill)) {
            return false
        }

        return true
    }

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        val targetCreature = target.asCreature() ?: return

        if (caster == targetCreature || targetCreature.isRaid) {
            return
        }

        if (targetCreature.isCastingNow { skillCaster ->
                    skillCaster.skillEntry.template.magicType == _magicType
                }) {
            targetCreature.abortCast(true, true)
        }
    }

}
