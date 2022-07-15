package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.model.base.DamageByAttackType
import l2s.gameserver.skills.SkillEntry
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.stats.StatModifierType
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * @author Nik
 * @author Java-man
 *
 * @since 18.10.2019
 */
class p_pve_physical_skill_defence_bonus(template: EffectTemplate) : EffectHandler(template) {

    private val amount = params.getDouble("amount")
    private val type = params.getEnum("type", DamageByAttackType::class.java, true)
    private val mode = params.getEnum("mode", StatModifierType::class.java, StatModifierType.DIFF, true)

    override fun pump(
            target: Creature,
            skillEntry: SkillEntry?
    ) {
        when (type) {
            DamageByAttackType.MOB -> {
                when (mode) {
                    StatModifierType.DIFF -> {
                        if (skillEntry != null) {
                            target.stat.mergeAdd(DoubleStat.PVE_PHYSICAL_SKILL_DEFENCE, amount, skillEntry)
                        } else {
                            target.stat.mergeAdd(DoubleStat.PVE_PHYSICAL_SKILL_DEFENCE, amount, skill)
                        }
                    }
                    StatModifierType.PER -> {
                        if (skillEntry != null) {
                            target.stat.mergeMul(DoubleStat.PVE_PHYSICAL_SKILL_DEFENCE, amount / 100 + 1, skillEntry)
                        } else {
                            target.stat.mergeMul(DoubleStat.PVE_PHYSICAL_SKILL_DEFENCE, amount / 100 + 1, skill)
                        }
                    }
                }
            }
            DamageByAttackType.BOSS -> {
                when (mode) {
                    StatModifierType.DIFF -> {
                        if (skillEntry != null) {
                            target.stat.mergeAdd(DoubleStat.PVE_RAID_PHYSICAL_SKILL_DEFENCE, amount, skillEntry)
                        } else {
                            target.stat.mergeAdd(DoubleStat.PVE_RAID_PHYSICAL_SKILL_DEFENCE, amount, skill)
                        }
                    }
                    StatModifierType.PER -> {
                        if (skillEntry != null) {
                            target.stat.mergeMul(DoubleStat.PVE_RAID_PHYSICAL_SKILL_DEFENCE, amount / 100 + 1, skillEntry)
                        } else {
                            target.stat.mergeMul(DoubleStat.PVE_RAID_PHYSICAL_SKILL_DEFENCE, amount / 100 + 1, skill)
                        }
                    }
                }
            }
            DamageByAttackType.ENEMY_ALL -> {
                when (mode) {
                    StatModifierType.DIFF -> {
                        if (skillEntry != null) {
                            target.stat.mergeAdd(DoubleStat.PVE_PHYSICAL_SKILL_DEFENCE, amount, skillEntry)
                        } else {
                            target.stat.mergeAdd(DoubleStat.PVE_PHYSICAL_SKILL_DEFENCE, amount, skill)
                        }
                        if (skillEntry != null) {
                            target.stat.mergeAdd(DoubleStat.PVE_RAID_PHYSICAL_SKILL_DEFENCE, amount, skillEntry)
                        } else {
                            target.stat.mergeAdd(DoubleStat.PVE_RAID_PHYSICAL_SKILL_DEFENCE, amount, skill)
                        }
                    }
                    StatModifierType.PER -> {
                        if (skillEntry != null) {
                            target.stat.mergeMul(DoubleStat.PVE_PHYSICAL_SKILL_DEFENCE, amount / 100 + 1, skillEntry)
                        } else {
                            target.stat.mergeMul(DoubleStat.PVE_PHYSICAL_SKILL_DEFENCE, amount / 100 + 1, skill)
                        }
                        if (skillEntry != null) {
                            target.stat.mergeMul(DoubleStat.PVE_RAID_PHYSICAL_SKILL_DEFENCE, amount / 100 + 1, skillEntry)
                        } else {
                            target.stat.mergeMul(DoubleStat.PVE_RAID_PHYSICAL_SKILL_DEFENCE, amount / 100 + 1, skill)
                        }
                    }
                }
            }
        }
    }

}