package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.templates.skill.EffectTemplate
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Call Party effect implementation.
 * @author Adry_85
 * @author Java-man
 */
class i_call_party(template: EffectTemplate) : i_abstract_effect(template) {

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        val party = caster.party ?: return

        for (partyMember in party) {
            if (i_call_pc.checkSummonTargetStatus(partyMember, caster.player)) {
                if (caster != partyMember) {
                    partyMember.teleToLocation(caster, 0, 50)
                }
            }
        }
    }
}
