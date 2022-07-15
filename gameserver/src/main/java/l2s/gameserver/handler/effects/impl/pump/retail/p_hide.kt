package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.stats.BooleanStat
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * Hide effect implementation.
 * @author ZaKaX, nBd
 * @author Java-man
 */
class p_hide(template: EffectTemplate) : EffectHandler(template) {

    override fun checkPumpCondition(abnormal: Abnormal?, caster: Creature, target: Creature): Boolean {
        if (target.isPlayer) {
            val player = target.player ?: return false

            if (player.activeWeaponFlagAttachment != null) {
                return false
            }

            if (player.stat.has(BooleanStat.INVIS_DISABLED)) {
                return false
            }
        }

        return true
    }

    override fun pumpStart(abnormal: Abnormal?, caster: Creature, target: Creature) {
        if (!target.isPlayer) {
            return
        }

        target.startInvisible(this, true)
    }

    override fun pumpEnd(abnormal: Abnormal?, caster: Creature, target: Creature) {
        if (target.isPlayer) {
            target.stopInvisible(this, true)

            for (servitor in target.servitors)
                servitor.abnormalList.stop(skill, false)
        } else if (target.isServitor) {
            target.player.abnormalList.stop(skill, false)
        }
    }

}