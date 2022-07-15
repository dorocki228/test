package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.skills.SkillEntry
import l2s.gameserver.skills.SkillEntryType
import l2s.gameserver.templates.skill.EffectTemplate
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Set Skill effect implementation.
 * @author Zoey76
 */
class i_set_skill(template: EffectTemplate) : i_abstract_effect(template) {

    private val skill: SkillEntry

    init {
        val param = params.getIntegerArray("skill", "-")
        val level = if (param.size >= 2) param[1] else 1
        val temp = SkillEntry.makeSkillEntry(SkillEntryType.NONE, param[0], level)
        skill = requireNotNull(temp)
    }

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        val player = target.player ?: return

        player.addSkill(skill, true)
        player.sendSkillList()
        player.updateSkillShortcuts(skill.id, skill.level)
    }
}