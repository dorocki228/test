package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.commons.util.Rnd
import l2s.gameserver.data.xml.holder.SkillHolder
import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.listener.actor.OnCreatureDamageDealtListener
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
 * Trigger Skill By Attack effect implementation.
 * @author Zealar
 * @author Java-man
 */
class p_trigger_skill_by_attack(template: EffectTemplate) : EffectHandler(template) {

    private val minAttackerLevel: Int
    private val maxAttackerLevel: Int
    private val attackerType: DamageByAttackType

    private val isCritical: Int
    private val allowNormalAttack: Boolean
    private val allowSkillAttack: Boolean
    private val allowReflect: Boolean

    private val minDamage: Int
    private val chance: Int

    private val skillEntry: SkillEntry

    private val targetType: TargetType

    private var allowWeapons: Long = 0

    private val listener: OnCreatureDamageDealtListenerImpl

    init {
        val param1 = params.getString("p_trigger_skill_by_attack_param1")
                .split(";")
        attackerType = DamageByAttackType.valueOf(param1[0].toUpperCase())
        minAttackerLevel = param1[1].toInt()
        maxAttackerLevel = param1[2].toInt()

        val param2 = params.getIntegerArray("p_trigger_skill_by_attack_param2")
        isCritical = param2.getOrNull(0) ?: -1
        allowNormalAttack = param2.getOrNull(1) ?: 1 == 1
        allowSkillAttack = param2.getOrNull(2) ?: 0 == 1
        allowReflect = param2.getOrNull(3) ?: 0 == 1

        val param3 = params.getString("p_trigger_skill_by_attack_param3")
                .split(";")
        minDamage = param3[0].toInt()
        chance = param3[1].toInt()

        val skillParam = params.getString("p_trigger_skill_by_attack_param4")
                .split(":")
        val skill = SkillHolder.getInstance().getSkill(skillParam[0].toInt(), skillParam[1].toInt())
        skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.TRIGGER, skill)

        targetType = params.getEnum(
                "p_trigger_skill_by_attack_param5",
                TargetType::class.java,
                true
        )
        if (targetType == null) {
            throw RuntimeException("Target Type not found for effect[" + javaClass.simpleName + "] TargetType[" + targetType + "].")
        }

        val param6 = params.getString("p_trigger_skill_by_attack_param6")
        if (param6 == "all") {
            allowWeapons = 0
        } else {
            for (s in param6.split(";")) {
                allowWeapons = allowWeapons or WeaponType.valueOf(s.toUpperCase()).mask()
            }
        }

        listener = OnCreatureDamageDealtListenerImpl(
                minAttackerLevel,
                maxAttackerLevel,
                attackerType,
                isCritical,
                allowNormalAttack,
                allowSkillAttack,
                allowReflect,
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
        class OnCreatureDamageDealtListenerImpl(
                private val minAttackerLevel: Int,
                private val maxAttackerLevel: Int,
                private val attackerType: DamageByAttackType,
                private val critical: Int,
                private val allowNormalAttack: Boolean,
                private val allowSkillAttack: Boolean,
                private val allowReflect: Boolean,
                private val minDamage: Int,
                private val chance: Int,
                private val skillEntry: SkillEntry,
                private val targetType: TargetType,
                private val allowWeapons: Long
        ) : OnCreatureDamageDealtListener {

            override fun onCreatureDamageDealt(
                    attacker: Creature,
                    target: Creature,
                    damage: Double,
                    skill: Skill?,
                    crit: Boolean,
                    damageOverTime: Boolean,
                    reflect: Boolean
            ) {
                if (damageOverTime || !allowNormalAttack && !allowSkillAttack) {
                    return
                }

                // Check if there is dependancy on critical.
                if (critical == 0 && crit || critical == 1 && !crit) {
                    return
                }

                // When no skill attacks are allowed.
                if (!allowSkillAttack && skill != null) {
                    return
                }

                // When no normal attacks are allowed.
                if (!allowNormalAttack && skill == null) {
                    return
                }

                if (!allowReflect && reflect) {
                    return
                }

                if (attacker == target) {
                    return
                }

                if (attacker.level < minAttackerLevel || attacker.level > maxAttackerLevel) {
                    return
                }

                if (damage < minDamage || !Rnd.chance(chance)) {
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