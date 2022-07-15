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
 * Delete Hate effect implementation.
 * @author Adry_85
 * @author Java-man
 */
class i_delete_hate(template: EffectTemplate) : i_abstract_effect(template) {

    private val _chance = params.getDouble("i_delete_hate_param1")

    override fun calcSuccess(caster: Creature, target: Creature, skill: Skill): Boolean {
        if (!target.isMonster) {
            return false
        }

        return Formulas.calcProbability(_chance, caster, target.asMonster(), skill)
    }

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        val monster = target.asMonster() ?: return
        monster.aggroList.clear(true)
        monster.setWalking()
        //if (monster.ai is DefaultAI<*>)
        //    (monster.ai as DefaultAI<*>).setGlobalAggro(System.currentTimeMillis() + monster.getParameter("globalAggro", 10000L))    //TODO: Check this.
        monster.ai.intention = CtrlIntention.AI_INTENTION_ACTIVE
    }
}