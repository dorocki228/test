package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.commons.util.Rnd
import l2s.gameserver.data.xml.holder.SkillHolder
import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.listener.actor.OnCurrentHpDamageListener
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.model.base.DamageByAttackType
import l2s.gameserver.model.skill.SkillCaster
import l2s.gameserver.skills.SkillEntry
import l2s.gameserver.skills.SkillEntryType
import l2s.gameserver.skills.targets.TargetType
import l2s.gameserver.templates.item.WeaponTemplate.WeaponType
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * Trigger Skill By Damage effect implementation.
 * @author UnAfraid
 * @author Java-man
 */
class p_trigger_skill_by_dmg(template: EffectTemplate) : EffectHandler(template) {

    private val minAttackerLevel: Int
    private val maxAttackerLevel: Int
    private val attackerType: DamageByAttackType

    private val minDamage: Int
    private val chance: Int

    private val skillEntry: SkillEntry

    private val targetType: TargetType

    private var allowWeapons: Long = 0

    private val listener: ListenerImpl

    init {
        val param1 = params.getString("p_trigger_skill_by_dmg_param1")
                .split(";")
        attackerType = DamageByAttackType.valueOf(param1[0].toUpperCase())
        minAttackerLevel = param1[1].toInt()
        maxAttackerLevel = param1[2].toInt()

        val param3 = params.getString("p_trigger_skill_by_dmg_param2")
                .split(";")
        minDamage = param3[0].toInt()
        chance = param3[1].toInt()

        val skillParam = params.getString("p_trigger_skill_by_dmg_param3")
                .split(":")
        val skill = SkillHolder.getInstance().getSkill(skillParam[0].toInt(), skillParam[1].toInt())
        skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.TRIGGER, skill)

        targetType = params.getEnum(
                "p_trigger_skill_by_dmg_param4",
                TargetType::class.java,
                true
        )
        if (targetType == null) {
            throw RuntimeException("Target Type not found for effect[" + javaClass.simpleName + "] TargetType[" + targetType + "].")
        }

        val param6 = params.getString("p_trigger_skill_by_dmg_param5")
        if (param6 == "all") {
            allowWeapons = 0
        } else {
            for (s in param6.split(";")) {
                allowWeapons = allowWeapons or WeaponType.valueOf(s.toUpperCase()).mask()
            }
        }

        listener = ListenerImpl(
                minAttackerLevel,
                maxAttackerLevel,
                attackerType,
                minDamage,
                chance,
                skillEntry,
                targetType,
                allowWeapons
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
                private val minAttackerLevel: Int,
                private val maxAttackerLevel: Int,
                private val attackerType: DamageByAttackType,
                private val minDamage: Int,
                private val chance: Int,
                private val skillEntry: SkillEntry,
                private val targetType: TargetType,
                private val allowWeapons: Long
        ) : OnCurrentHpDamageListener {

            override fun onCurrentHpDamage(
                    attacker: Creature,
                    target: Creature,
                    damage: Double,
                    skill: Skill?,
                    crit: Boolean,
                    damageOverTime: Boolean,
                    reflect: Boolean
            ) {
                if (reflect) {
                    return
                }

                if (attacker == target) {
                    return
                }

                if (attacker.level < minAttackerLevel || attacker.level > maxAttackerLevel) {
                    return
                }

                if (damage < minDamage || Rnd.get(100) > chance) {
                    return
                }

                if (!attackerType.check(attacker)) {
                    return
                }

                if (allowWeapons > 0) {
                    if (attacker.activeWeaponTemplate == null) {
                        return
                    }
                    if (attacker.activeWeaponTemplate.itemType.mask() and allowWeapons == 0L) {
                        return
                    }
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