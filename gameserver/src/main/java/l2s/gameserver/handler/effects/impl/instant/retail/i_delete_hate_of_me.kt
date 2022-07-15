package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.gameserver.ai.CtrlIntention
import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.stats.Formulas
import l2s.gameserver.templates.skill.EffectTemplate
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Delete Hate Of Me effect implementation.
 * @author Adry_85
 * @author Java-man
 */
class i_delete_hate_of_me(template: EffectTemplate) : i_abstract_effect(template) {

    private val _chance = params.getDouble("i_delete_hate_of_me_param1")

    override fun calcSuccess(caster: Creature, target: Creature, skill: Skill): Boolean {
        if (!target.isMonster) {
            return false
        }

        return Formulas.calcProbability(_chance, caster, target.asMonster(), skill)
    }

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        val monster = target.asMonster() ?: return

        monster.aggroList.remove(caster, true)
        monster.setWalking()
        monster.ai.intention = CtrlIntention.AI_INTENTION_ACTIVE
    }
}