package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.skills.SkillEntry
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.stats.StatModifierType
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * @author Sdw
 * @author Java-man
 *
 * @since 13.10.2019
 */
class p_speed_out_of_fight(template: EffectTemplate) : EffectHandler(template) {

    private val amount = params.getDouble("p_speed_out_of_fight_param1")
    private val mode = params.getEnum(
            "p_speed_out_of_fight_param2",
            StatModifierType::class.java,
            true
    )

    override fun checkPumpCondition(abnormal: Abnormal?, caster: Creature, target: Creature): Boolean {
        return !target.isInCombat
    }

    override fun pump(
            target: Creature,
            skillEntry: SkillEntry?
    ) {
        when (mode) {
            StatModifierType.DIFF -> {
                if (skillEntry != null) {
                    target.stat.mergeAdd(DoubleStat.RUN_SPEED, amount, skillEntry)
                } else {
                    target.stat.mergeAdd(DoubleStat.RUN_SPEED, amount, skill)
                }
                if (skillEntry != null) {
                    target.stat.mergeAdd(DoubleStat.WALK_SPEED, amount, skillEntry)
                } else {
                    target.stat.mergeAdd(DoubleStat.WALK_SPEED, amount, skill)
                }
                if (skillEntry != null) {
                    target.stat.mergeAdd(DoubleStat.SWIM_RUN_SPEED, amount, skillEntry)
                } else {
                    target.stat.mergeAdd(DoubleStat.SWIM_RUN_SPEED, amount, skill)
                }
                if (skillEntry != null) {
                    target.stat.mergeAdd(DoubleStat.SWIM_WALK_SPEED, amount, skillEntry)
                } else {
                    target.stat.mergeAdd(DoubleStat.SWIM_WALK_SPEED, amount, skill)
                }
                if (skillEntry != null) {
                    target.stat.mergeAdd(DoubleStat.FLY_RUN_SPEED, amount, skillEntry)
                } else {
                    target.stat.mergeAdd(DoubleStat.FLY_RUN_SPEED, amount, skill)
                }
                if (skillEntry != null) {
                    target.stat.mergeAdd(DoubleStat.FLY_WALK_SPEED, amount, skillEntry)
                } else {
                    target.stat.mergeAdd(DoubleStat.FLY_WALK_SPEED, amount, skill)
                }
            }
            StatModifierType.PER -> {
                val mul = amount / 100.0 + 1
                if (skillEntry != null) {
                    target.stat.mergeMul(DoubleStat.RUN_SPEED, mul, skillEntry)
                } else {
                    target.stat.mergeMul(DoubleStat.RUN_SPEED, mul, skill)
                }
                if (skillEntry != null) {
                    target.stat.mergeMul(DoubleStat.WALK_SPEED, mul, skillEntry)
                } else {
                    target.stat.mergeMul(DoubleStat.WALK_SPEED, mul, skill)
                }
                if (skillEntry != null) {
                    target.stat.mergeMul(DoubleStat.SWIM_RUN_SPEED, mul, skillEntry)
                } else {
                    target.stat.mergeMul(DoubleStat.SWIM_RUN_SPEED, mul, skill)
                }
                if (skillEntry != null) {
                    target.stat.mergeMul(DoubleStat.SWIM_WALK_SPEED, mul, skillEntry)
                } else {
                    target.stat.mergeMul(DoubleStat.SWIM_WALK_SPEED, mul, skill)
                }
                if (skillEntry != null) {
                    target.stat.mergeMul(DoubleStat.FLY_RUN_SPEED, mul, skillEntry)
                } else {
                    target.stat.mergeMul(DoubleStat.FLY_RUN_SPEED, mul, skill)
                }
                if (skillEntry != null) {
                    target.stat.mergeMul(DoubleStat.FLY_WALK_SPEED, mul, skillEntry)
                } else {
                    target.stat.mergeMul(DoubleStat.FLY_WALK_SPEED, mul, skill)
                }
            }
        }
    }

}