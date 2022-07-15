package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.model.base.ReduceDropType
import l2s.gameserver.skills.SkillEntry
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * @author Sdw
 * @author Java-man
 *
 * @since 16.10.2019
 */
class p_reduce_drop_penalty(template: EffectTemplate) : EffectHandler(template) {

    private val exp = params.getDouble("p_reduce_drop_penalty_param3")
    private val deathPenalty = params.getDouble("p_reduce_drop_penalty_param2")
    private val type = params.getEnum(
            "p_reduce_drop_penalty_param1",
            ReduceDropType::class.java,
            true
    )

    override fun pump(
            target: Creature,
            skillEntry: SkillEntry?
    ) {
        when (type) {
            ReduceDropType.MOB -> {
                if (skillEntry != null) {
                    target.stat.mergeMul(DoubleStat.REDUCE_EXP_LOST_BY_MOB, exp / 100.0 + 1, skillEntry)
                } else {
                    target.stat.mergeMul(DoubleStat.REDUCE_EXP_LOST_BY_MOB, exp / 100.0 + 1, skill)
                }
                if (skillEntry != null) {
                    target.stat.mergeMul(DoubleStat.REDUCE_DEATH_PENALTY_BY_MOB, deathPenalty / 100.0 + 1, skillEntry)
                } else {
                    target.stat.mergeMul(DoubleStat.REDUCE_DEATH_PENALTY_BY_MOB, deathPenalty / 100.0 + 1, skill)
                }
            }
            ReduceDropType.PK -> {
                if (skillEntry != null) {
                    target.stat.mergeMul(DoubleStat.REDUCE_EXP_LOST_BY_PVP, exp / 100.0 + 1, skillEntry)
                } else {
                    target.stat.mergeMul(DoubleStat.REDUCE_EXP_LOST_BY_PVP, exp / 100.0 + 1, skill)
                }
                if (skillEntry != null) {
                    target.stat.mergeMul(DoubleStat.REDUCE_DEATH_PENALTY_BY_PVP, deathPenalty / 100.0 + 1, skillEntry)
                } else {
                    target.stat.mergeMul(DoubleStat.REDUCE_DEATH_PENALTY_BY_PVP, deathPenalty / 100.0 + 1, skill)
                }
            }
            ReduceDropType.RAID -> {
                if (skillEntry != null) {
                    target.stat.mergeMul(DoubleStat.REDUCE_EXP_LOST_BY_RAID, exp / 100.0 + 1, skillEntry)
                } else {
                    target.stat.mergeMul(DoubleStat.REDUCE_EXP_LOST_BY_RAID, exp / 100.0 + 1, skill)
                }
                if (skillEntry != null) {
                    target.stat.mergeMul(DoubleStat.REDUCE_DEATH_PENALTY_BY_RAID, deathPenalty / 100.0 + 1, skillEntry)
                } else {
                    target.stat.mergeMul(DoubleStat.REDUCE_DEATH_PENALTY_BY_RAID, deathPenalty / 100.0 + 1, skill)
                }
            }
        }
    }

}