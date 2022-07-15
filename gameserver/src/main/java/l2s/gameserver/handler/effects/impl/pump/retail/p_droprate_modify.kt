package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.skills.SkillEntry
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * @author Nik
 * @author Java-man
 */
class p_droprate_modify(template: EffectTemplate) : EffectHandler(template) {

    private val _dropRate = params.getInteger("drop")
    private val _spoilRate = params.getInteger("spoil")
    private val _adenaRate = params.getInteger("adena")

    override fun pump(
            target: Creature,
            skillEntry: SkillEntry?
    ) {
        if (_dropRate > 0) {
            if (skillEntry != null) {
                target.stat.mergeAdd(DoubleStat.BONUS_DROP, _dropRate.toDouble(), skillEntry)
            } else {
                target.stat.mergeAdd(DoubleStat.BONUS_DROP, _dropRate.toDouble(), skill)
            }
        }
        if (_spoilRate > 0) {
            if (skillEntry != null) {
                target.stat.mergeAdd(DoubleStat.BONUS_SPOIL, _spoilRate.toDouble(), skillEntry)
            } else {
                target.stat.mergeAdd(DoubleStat.BONUS_SPOIL, _spoilRate.toDouble(), skill)
            }
        }
        if (_adenaRate > 0) {
            if (skillEntry != null) {
                target.stat.mergeAdd(DoubleStat.BONUS_ADENA, _adenaRate.toDouble(), skillEntry)
            } else {
                target.stat.mergeAdd(DoubleStat.BONUS_ADENA, _adenaRate.toDouble(), skill)
            }
        }
    }

}