package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.skills.SkillEntry
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.stats.calculators.CalculationType
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * @author Sdw
 * @author Java-man
 *
 * @since 10.10.2019
 */
class p_ability_change(template: EffectTemplate) : EffectHandler(template) {

    private val sharedStats: Map<DoubleStat, Double>

    init {
        val params = params.getString("p_ability_change_param1")
        val regex = """\{(\w+;\d+)\}""".toRegex()
        sharedStats = regex.findAll(params)
                .map { it.groups[1] }
                .filterNotNull()
                .map { it.value }
                .map { it.split(";") }
                .map { it[0] to it[1] }
                .associate {
                    DoubleStat.find(it.first) to it.second.toDouble() / 100.0
                }

        require(params.isNotEmpty()) { "Must have parameters!" }
    }

    override fun checkPumpCondition(abnormal: Abnormal?, caster: Creature, target: Creature): Boolean {
        return target.isSummon
    }

    override fun pump(
            target: Creature,
            skillEntry: SkillEntry?
    ) {
        val owner = target.player ?: return

        for ((key, value) in sharedStats) {
            val baseValue = owner.stat.getValue(key, CalculationType.BASE_VALUE)
            if (skillEntry != null) {
                target.stat.mergeAdd(key, baseValue * value, skillEntry)
            } else {
                target.stat.mergeAdd(key, baseValue * value, skill)
            }
        }
    }

}