package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.skills.SkillEntry
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * @author Sdw
 * @author Java-man
 *
 * @since 13.10.2019
 */
class p_attack_attribute_add(template: EffectTemplate) : EffectHandler(template) {

    private val amount = params.getDouble("p_attack_attribute_add_param1")

    override fun pump(
            target: Creature,
            skillEntry: SkillEntry?
    ) {
        val weapon = target.activeWeaponInstance ?: return
        if (skillEntry != null) {
            target.stat.mergeAdd(weapon.attributeElement.attack, amount, skillEntry)
        } else {
            target.stat.mergeAdd(weapon.attributeElement.attack, amount, skill)
        }
    }

}