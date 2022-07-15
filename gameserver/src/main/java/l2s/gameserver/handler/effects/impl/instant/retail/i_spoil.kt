package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.stats.Formulas
import l2s.gameserver.templates.skill.EffectTemplate
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Spoil effect implementation.
 * @author _drunk_, Ahmed, Zoey76
 * @author Java-man
 */
class i_spoil(template: EffectTemplate) : i_abstract_effect(template) {

    override fun calcSuccess(caster: Creature, target: Creature, skill: Skill): Boolean {
        if (!target.isMonster) {
            return false
        }

        if (!Formulas.calcMagicSuccess(caster, target.asMonster(), skill)) {
            return false
        }

        return true
    }

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        val targetMonster = target.asMonster() ?: return

        if (targetMonster.isDead) {
            caster.sendPacket(SystemMsg.INVALID_TARGET)
            return
        }

        if (targetMonster.isSpoiled) {
            caster.sendPacket(SystemMsg.IT_HAS_ALREADY_BEEN_SPOILED)
            return
        }

        targetMonster.setSpoiled(caster.player)
        caster.sendPacket(SystemMsg.THE_SPOIL_CONDITION_HAS_BEEN_ACTIVATED)
    }

}