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
class p_max_hp(template: EffectTemplate) :
        AbstractDoubleStatConditionalItemTypeEffect(template, DoubleStat.MAX_HP) {

    private val heal = params.getInteger("p_max_hp_param4", 0) == 1

    override fun pumpStart(abnormal: Abnormal?, caster: Creature, target: Creature) {
        if (!heal || target.isHpBlocked) {
            return
        }

        when (modifierType) {
            StatModifierType.DIFF -> {
                val newHp = target.currentHp + amount
                target.setCurrentHp(newHp, false)
                //effected.sendPacket(new SystemMessagePacket(SystemMsg.S1_HP_HAS_BEEN_RESTORED).addInteger(newHp)); TODO: Проверить на оффе, должно ли быть сообщение.
            }
            StatModifierType.PER -> {
                val newMaxHp = target.maxHp * (1 + (amount / 100.0))
                val newHp = target.currentHp + newMaxHp * (amount / 100.0)
                target.setCurrentHp(newHp, false)
                //effected.sendPacket(new SystemMessagePacket(SystemMsg.S1_HP_HAS_BEEN_RESTORED).addInteger(newHp)); TODO: Проверить на оффе, должно ли быть сообщение.
            }
        }
    }

}