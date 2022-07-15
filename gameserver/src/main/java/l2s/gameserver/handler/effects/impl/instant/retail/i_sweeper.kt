package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.templates.skill.EffectTemplate
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Sweeper effect implementation.
 * @author Zoey76
 * @author Java-man
 */
class i_sweeper(template: EffectTemplate) : i_abstract_effect(template) {

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        if (!target.isMonster || !target.isDead) {
            caster.sendPacket(SystemMsg.INVALID_TARGET)
            return
        }

        val monster = target.asMonster()

        if (monster.isSweeped) {
            return
        }

        if (!monster.isSpoiled) {
            caster.sendPacket(SystemMsg.SWEEPER_FAILED_TARGET_NOT_SPOILED)
            return
        }

        val player = caster.player
        if (!monster.isSpoiled(player)) {
            caster.sendPacket(SystemMsg.THERE_ARE_NO_PRIORITY_RIGHTS_ON_A_SWEEPER)
            return
        }

        monster.takeSweep(player)

        monster.endDecayTask()
    }

}