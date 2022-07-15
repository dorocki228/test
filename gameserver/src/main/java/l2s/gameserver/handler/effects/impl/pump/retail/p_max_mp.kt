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
class p_max_mp(template: EffectTemplate) :
        AbstractDoubleStatConditionalItemTypeEffect(template, DoubleStat.MAX_MP) {

    private val heal = params.getInteger("p_max_mp_param4", 0) == 1

    override fun pumpStart(abnormal: Abnormal?, caster: Creature, target: Creature) {
        if (!heal || target.isMpBlocked) {
            return
        }

        when (modifierType) {
            StatModifierType.DIFF -> {
                val newMp = target.currentMp + amount
                target.setCurrentMp(newMp, false)
                //effected.sendPacket(new SystemMessagePacket(SystemMsg.S1_MP_HAS_BEEN_RESTORED).addInteger(newHp)); TODO: Проверить на оффе, должно ли быть сообщение.
            }
            StatModifierType.PER -> {
                val newMp = target.currentMp + target.maxMp * (amount / 100.0)
                target.setCurrentMp(newMp, false)
                //effected.sendPacket(new SystemMessagePacket(SystemMsg.S1_MP_HAS_BEEN_RESTORED).addInteger(newHp)); TODO: Проверить на оффе, должно ли быть сообщение.
            }
        }
    }

}