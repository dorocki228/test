package l2s.gameserver.handler.effects.impl.tick

import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.network.l2.s2c.EtcStatusUpdatePacket
import l2s.gameserver.network.l2.s2c.SystemMessagePacket
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.templates.skill.EffectTemplate
import kotlin.math.min

/**
 * @author Sdw
 * @author Java-man
 */
class t_get_energy(template: EffectTemplate) : EffectHandler(template) {

    override fun tick(abnormal: Abnormal?, caster: Creature, target: Creature) {
        if (!target.isPlayer) {
            return
        }

        val player = target.player
        val maxCharge = target.stat.getValue(DoubleStat.MAX_MOMENTUM, 0.0).toInt()
        val newCharge = min(player.charges + 1, maxCharge)

        player.charges = maxCharge

        if (newCharge == maxCharge) {
            player.sendPacket(SystemMsg.YOUR_FORCE_HAS_REACHED_MAXIMUM_CAPACITY_)
        } else {
            val sm = SystemMessagePacket(SystemMsg.YOUR_FORCE_HAS_INCREASED_TO_LEVEL_S1)
            sm.addInteger(newCharge.toDouble())
            player.sendPacket(sm)
        }

        player.sendPacket(EtcStatusUpdatePacket(player))
    }

}