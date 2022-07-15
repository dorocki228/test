package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.impl.AbstractDoubleStatAddEffect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Playable
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * Transfer Damage effect implementation.
 *
 * @author UnAfraid
 * @author Java-man
 *
 * @since 15.10.2019
 */
class p_transfer_damage_pc(template: EffectTemplate) :
        AbstractDoubleStatAddEffect(template, DoubleStat.TRANSFER_DAMAGE_TO_PLAYER) {

    override fun pumpStart(abnormal: Abnormal?, caster: Creature, target: Creature) {
        if (target.isPlayable && caster.isPlayer) {
            (target as Playable).setTransferDamageTo(caster.player)
        }
    }

    override fun pumpEnd(abnormal: Abnormal?, caster: Creature, target: Creature) {
        if (target.isPlayable && caster.isPlayer) {
            (target as Playable).setTransferDamageTo(null)
        }
    }

}