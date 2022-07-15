package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.impl.AbstractBooleanStatEffect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.skills.SkillCastingType
import l2s.gameserver.stats.BooleanStat
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * Mute effect implementation.
 */
class p_block_spell(template: EffectTemplate) :
        AbstractBooleanStatEffect(template, BooleanStat.BLOCK_SPELL) {

    override fun pumpStart(abnormal: Abnormal?, caster: Creature, target: Creature) {
        var castingSkillEntry = target.getSkillCast(SkillCastingType.NORMAL).skillEntry
        if (castingSkillEntry != null && castingSkillEntry.template.isMagic) {
            target.abortCast(true, true)
            return
        }

        castingSkillEntry = target.getSkillCast(SkillCastingType.NORMAL_SECOND).skillEntry
        if (castingSkillEntry != null && castingSkillEntry.template.isMagic) {
            target.abortCast(true, true)
        }
    }

}