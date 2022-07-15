package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.skills.SkillEntry
import l2s.gameserver.skills.TraitType
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * Attack Trait effect implementation.
 * @author NosBit
 * @author Java-man
 *
 * @since 17.10.2019
 */
class p_attack_trait(template: EffectTemplate) : EffectHandler(template) {

    private val trait = TraitType.find(params.getString("p_attack_trait_param1"))
    private val amount = (params.getDouble("p_attack_trait_param2") + 100) / 100.0

    override fun pump(
            target: Creature,
            skillEntry: SkillEntry?
    ) {
        target.stat.mergeAttackTrait(trait, amount)
    }

}