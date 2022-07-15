package l2s.gameserver.handler.effects.impl.pump

import l2s.gameserver.handler.effects.impl.AbstractDoubleStatConditionalItemTypeEffect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.skills.SkillEntry
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * @author Java-man
 */
sealed class p_elemental_defence(stat: DoubleStat, template: EffectTemplate) : AbstractDoubleStatConditionalItemTypeEffect(template, stat) {

    override fun pump(
            target: Creature,
            skillEntry: SkillEntry?
    ) {
        target.sendElementalInfo()
    }

    override fun pumpEnd(abnormal: Abnormal?, caster: Creature, target: Creature) {
        target.sendElementalInfo()
    }

}

class p_fire_elemental_defence(template: EffectTemplate) : p_elemental_defence(DoubleStat.FIRE_ELEMENTAL_DEFENCE, template)
class p_water_elemental_defence(template: EffectTemplate) : p_elemental_defence(DoubleStat.WATER_ELEMENTAL_DEFENCE, template)
class p_wind_elemental_defence(template: EffectTemplate) : p_elemental_defence(DoubleStat.WIND_ELEMENTAL_DEFENCE, template)
class p_earth_elemental_defence(template: EffectTemplate) : p_elemental_defence(DoubleStat.EARTH_ELEMENTAL_DEFENCE, template)