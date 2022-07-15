package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.commons.util.Rnd
import l2s.gameserver.data.xml.holder.SkillHolder
import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.listener.actor.OnCreatureSkillFinishCastListener
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.model.skill.SkillCaster
import l2s.gameserver.skills.SkillEntry
import l2s.gameserver.skills.SkillEntryType
import l2s.gameserver.skills.targets.TargetType
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * Trigger Skill By Skill effect implementation.
 * @author Zealar
 * @author Java-man
 */
class p_trigger_skill_by_skill(template: EffectTemplate) : EffectHandler(template) {

    private val castSkillId: Int
    private val chance: Int

    private val skillEntry: SkillEntry

    private val targetType: TargetType

    private val listener: ListenerImpl

    init {
        castSkillId = params.getInteger("p_trigger_skill_by_skill_param1")
        chance = params.getInteger("p_trigger_skill_by_skill_param2")

        val skillParam = params.getString("p_trigger_skill_by_skill_param3")
                .split(":")
        val skill = SkillHolder.getInstance().getSkill(skillParam[0].toInt(), skillParam[1].toInt())
        skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.TRIGGER, skill)

        targetType = params.getEnum(
                "p_trigger_skill_by_skill_param4",
                TargetType::class.java,
                true
        )
        if (targetType == null) {
            throw RuntimeException("Target Type not found for effect[" + javaClass.simpleName + "] TargetType[" + targetType + "].")
        }

        listener = ListenerImpl(
                castSkillId,
                chance,
                skillEntry,
                targetType
        )
    }

    override fun pumpStart(abnormal: Abnormal?, caster: Creature, target: Creature) {
        target.addListener(listener)
    }

    override fun pumpEnd(abnormal: Abnormal?, caster: Creature, target: Creature) {
        target.removeListener(listener)
    }

    companion object {
        class ListenerImpl(
                private val castSkillId: Int,
                private val chance: Int,
                private val skillEntry: SkillEntry,
                private val targetType: TargetType
        ) : OnCreatureSkillFinishCastListener {
            override fun onCreatureSkillFinishCast(
                    caster: Creature,
                    target: Creature,
                    skill: Skill
            ) {
                if (castSkillId != skill.getId()) {
                    return
                }

                if (!target.isCreature) {
                    return
                }

                if (!Rnd.chance(chance)) {
                    return
                }

                val target = targetType.getTarget(caster, target, skillEntry.template, false, false, false)
                if (target != null && target.isCreature) {
                    SkillCaster.triggerCast(caster, target, skillEntry)
                }
            }

        }
    }

}