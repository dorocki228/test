package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.impl.AbstractBooleanStatEffect
import l2s.gameserver.listener.actor.OnReviveListener
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.model.skill.SkillCaster
import l2s.gameserver.skills.SkillEntry
import l2s.gameserver.skills.SkillEntryType
import l2s.gameserver.stats.BooleanStat
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * Resurrection Special effect implementation.
 * @author Zealar
 * @author Java-man
 *
 * TODO use other params
 */
class p_resurrection_special(template: EffectTemplate) :
        AbstractBooleanStatEffect(template, BooleanStat.RESURRECTION_SPECIAL) {

    private val skillEntry: SkillEntry

    private val listener: ListenerImpl

    init {
        val param4 = params.getString("p_resurrection_special_param4")
                .split(":")
                .map { it.toInt() }
        skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.TRIGGER, param4[0], param4[1])

        listener = ListenerImpl(skillEntry)
    }

    override fun pumpStart(abnormal: Abnormal?, caster: Creature, target: Creature) {
        target.addListener(listener)
    }

    override fun pumpEnd(abnormal: Abnormal?, caster: Creature, target: Creature) {
        target.removeListener(listener)
    }

    companion object {
        class ListenerImpl(
                private val skillEntry: SkillEntry
        ) : OnReviveListener {

            override fun onRevive(actor: Creature) {
                SkillCaster.triggerCast(actor, actor, skillEntry)
            }

        }
    }

}