package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.ThreadPoolManager
import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.model.base.Race
import l2s.gameserver.model.skill.SkillCaster
import l2s.gameserver.skills.SkillEntry
import l2s.gameserver.skills.SkillEntryType
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * Call Skill effect implementation.
 * @author Java-man
 */
class p_call_skill(template: EffectTemplate) : EffectHandler(template) {

    private val skillEntry: SkillEntry

    init {
        val skill = params.getIntegerArray("p_call_skill_param1", ":")
        skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.TRIGGER, skill[0], skill[1])
    }

    override fun checkPumpCondition(abnormal: Abnormal?, caster: Creature, target: Creature): Boolean {
        return target.isPlayer
    }

    override fun pumpStart(abnormal: Abnormal?, caster: Creature, target: Creature) {
        val player = target.player

        SkillCaster.triggerCast(player, player, skillEntry)

        /*val delay = skillEntry.template.reuseDelay + 1
        val future = ThreadPoolManager.getInstance().schedule({
            SkillCaster.triggerCast(player, player, skillEntry)
        }, delay.toLong())

        player.addTask(this, future)*/
    }

    override fun pumpEnd(abnormal: Abnormal?, caster: Creature, target: Creature) {
        //val player = target.player
        //player.removeTasksByOwner(this)

        target.abnormalList.stop(skillEntry, false)
    }

}
