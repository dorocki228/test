package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.commons.util.Rnd
import l2s.gameserver.data.xml.holder.SkillHolder
import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.listener.actor.OnCreatureAttackAvoidListener
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.model.skill.SkillCaster
import l2s.gameserver.skills.SkillEntry
import l2s.gameserver.skills.SkillEntryType
import l2s.gameserver.skills.targets.TargetType
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * Trigger Skill By Avoid effect implementation.
 * @author Zealar
 * @author Java-man
 */
class p_trigger_skill_by_avoid(template: EffectTemplate) : EffectHandler(template) {

    private val chance: Int

    private val skillEntry: SkillEntry

    private val targetType: TargetType

    private val listener: OnCreatureAttackAvoidListenerImpl

    init {
        chance = params.getInteger("p_trigger_skill_by_avoid_param1")

        val skillParam = params.getString("p_trigger_skill_by_avoid_param2")
                .split(":")
        val skill = SkillHolder.getInstance().getSkill(skillParam[0].toInt(), skillParam[1].toInt())
        skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.TRIGGER, skill)

        targetType = params.getEnum(
                "p_trigger_skill_by_avoid_param3",
                TargetType::class.java,
                true
        )
        if (targetType == null) {
            throw RuntimeException("Target Type not found for effect[" + javaClass.simpleName + "] TargetType[" + targetType + "].")
        }

        listener = OnCreatureAttackAvoidListenerImpl(
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
        class OnCreatureAttackAvoidListenerImpl(
                private val chance: Int,
                private val skillEntry: SkillEntry,
                private val targetType: TargetType
        ) : OnCreatureAttackAvoidListener {

            override fun onCreatureAttackAvoid(
                    attacker: Creature,
                    target: Creature,
                    damageOverTime: Boolean
            ) {
                if (damageOverTime) {
                    return
                }

                if (attacker == target) {
                    return
                }

                if (!Rnd.chance(chance)) {
                    return
                }

                val triggerSkill = skillEntry.template
                val target = targetType.getTarget(attacker, target, triggerSkill, false, false, false)

                if (target == null || !target.isCreature) {
                    return
                }

                SkillCaster.triggerCast(attacker, target, skillEntry)
            }

        }
    }

}