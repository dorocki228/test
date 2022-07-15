package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.skills.SkillEntry
import l2s.gameserver.stats.BooleanStat
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * Block Actions except item id effect implementation.
 *
 * @author Sdw
 * @author Java-man
 */
class p_condition_block_act_item(template: EffectTemplate) : EffectHandler(template) {

    private val _allowedItems: List<Int> = params.getString("p_condition_block_act_item_param1")
            .split(";")
            .filter { it.isNotEmpty() }
            .map { it.toInt() }
            .distinct()

    override fun pumpStart(abnormal: Abnormal?, caster: Creature, target: Creature) {
        target.stopActions()
    }

    override fun pump(target: Creature, skillEntry: SkillEntry?) {
        target.stat.set(BooleanStat.BLOCK_ACTIONS)
        _allowedItems.forEach { target.stat.addBlockActionsAllowedItem(it) }
    }

}