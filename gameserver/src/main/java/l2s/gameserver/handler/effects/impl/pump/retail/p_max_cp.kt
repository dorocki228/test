package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.impl.AbstractDoubleStatConditionalItemTypeEffect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.stats.StatModifierType
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * @author Sdw
 * @author Java-man
 *
 * @since 12.10.2019
 */
class p_max_cp(template: EffectTemplate) :
        AbstractDoubleStatConditionalItemTypeEffect(template, DoubleStat.MAX_CP) {

    private val heal = params.getInteger("p_max_cp_param4") == 1

    override fun pumpStart(abnormal: Abnormal?, caster: Creature, target: Creature) {
        if (!heal) {
            return
        }

        when (modifierType) {
            StatModifierType.DIFF -> {
                val newCp = target.currentCp + amount
                target.setCurrentCp(newCp, false)
                //effected.sendPacket(new SystemMessagePacket(SystemMsg.S1_CP_HAS_BEEN_RESTORED).addInteger(newHp)); TODO: Проверить на оффе, должно ли быть сообщение.
            }
            StatModifierType.PER -> {
                val newCp = target.currentCp + target.maxCp * (amount / 100.0)
                target.setCurrentCp(newCp, false)
                //effected.sendPacket(new SystemMessagePacket(SystemMsg.S1_CP_HAS_BEEN_RESTORED).addInteger(newHp)); TODO: Проверить на оффе, должно ли быть сообщение.
            }
        }
    }

}