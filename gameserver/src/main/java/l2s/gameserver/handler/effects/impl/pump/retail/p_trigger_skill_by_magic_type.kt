package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.commons.util.Rnd
import l2s.gameserver.data.xml.holder.SkillHolder
import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.listener.actor.OnCreatureSkillFinishCastListener
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.model.Skill.SkillMagicType
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.model.skill.SkillCaster
import l2s.gameserver.skills.SkillEntry
import l2s.gameserver.skills.SkillEntryType
import l2s.gameserver.skills.targets.TargetType
import l2s.gameserver.templates.skill.EffectTemplate
import kotlin.math.min

/**
 * Trigger skill by isMagic type.
 * @author Nik
 * @author Java-man
 */
class p_trigger_skill_by_magic_type(template: EffectTemplate) : EffectHandler(template) {

    private val magicTypes: List<SkillMagicType>
    private val skillLevelScaleTo: Int
    private val chance: Int

    private val skillEntry: SkillEntry

    private val targetType: TargetType

    private val listener: ListenerImpl

    init {
        magicTypes = params.getIntegerArray("p_trigger_skill_by_magic_type_param1", ";")
                .map { SkillMagicType.values()[it] }
        skillLevelScaleTo = params.getInteger("p_trigger_skill_by_magic_type_param2")
        chance = params.getInteger("p_trigger_skill_by_magic_type_param3")

        val skillParam = params.getString("p_trigger_skill_by_magic_type_param4")
                .split(":")
        val skill = SkillHolder.getInstance().getSkill(skillParam[0].toInt(), skillParam[1].toInt())
        skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.TRIGGER, skill)

        targetType = params.getEnum(
                "p_trigger_skill_by_magic_type_param5",
                TargetType::class.java,
                true
        )
        if (targetType == null) {
            throw RuntimeException("Target Type not found for effect[" + javaClass.simpleName + "] TargetType[" + targetType + "].")
        }

        listener = ListenerImpl(
                magicTypes,
                skillLevelScaleTo,
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
                private val magicTypes: List<SkillMagicType>,
                private val chance: Int,
                private val skillLevelScaleTo: Int,
                private val skillEntry: SkillEntry,
                private val targetType: TargetType
        ) : OnCreatureSkillFinishCastListener {
            override fun onCreatureSkillFinishCast(
                    caster: Creature,
                    target: Creature,
                    skill: Skill
            ) {
                if (!target.isCreature) {
                    return
                }

                if (!magicTypes.contains(skill.magicType)) {
                    return
                }

                if (!Rnd.chance(chance)) {
                    return
                }

                val triggerSkill: SkillEntry
                triggerSkill = if (skillLevelScaleTo <= 0) {
                    skillEntry
                } else {
                    val abnormalLevel = target.abnormalList.getAbnormalLevel(skillEntry.id)
                    if (abnormalLevel != -1) {
                        val level = min(skillLevelScaleTo, abnormalLevel + 1)
                        val skill = SkillHolder.getInstance().getSkill(skillEntry.id, level)
                        SkillEntry.makeSkillEntry(SkillEntryType.TRIGGER, skill)
                    } else {
                        skillEntry
                    }
                }

                val target = targetType.getTarget(caster, target, triggerSkill.template, false, false, false)
                if (target != null && target.isCreature) {
                    SkillCaster.triggerCast(caster, target, triggerSkill)
                }
            }

        }
    }

}