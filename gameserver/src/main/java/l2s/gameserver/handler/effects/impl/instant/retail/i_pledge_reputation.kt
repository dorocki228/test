package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.templates.skill.EffectTemplate
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author Bonux
 * @author Java-man
 */
class i_pledge_reputation(template: EffectTemplate) : i_abstract_effect(template) {

    private val count = params.getInteger("i_pledge_reputation_param1")

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        val clan = target.clan ?: return

        clan.incReputation(count, false, "Using skill ID[" + skill.id + "] LEVEL[" + skill.level + "]")
    }

}