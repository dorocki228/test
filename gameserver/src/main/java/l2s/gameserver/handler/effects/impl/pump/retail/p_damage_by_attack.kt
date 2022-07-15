package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.model.base.DamageByAttackType
import l2s.gameserver.skills.SkillEntry
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.stats.StatModifierType
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * An effect that changes damage taken from an attack. <br>
 * The retail implementation seems to be altering whatever damage is taken after the attack has been done and not when attack is being done. <br>
 * Exceptions for this effect appears to be DOT effects and terrain damage, they are unaffected by this stat.<br>
 * As for example in retail this effect does reduce reflected damage taken (because it is received damage), as well as it does not decrease reflected damage done,<br>
 * because reflected damage is being calculated with the original attack damage and not this altered one.<br>
 * Multiple values of this effect add-up to each other rather than multiplying with each other. Be careful, there were cases in retail where damage is deacreased to 0.
 *
 * @author Nik
 * @author Java-man
 *
 * @since 12.10.2019
 */
class p_damage_by_attack(template: EffectTemplate) : EffectHandler(template) {

    private val type = params.getEnum(
            "p_damage_by_attack_param1",
            DamageByAttackType::class.java,
            true
    )
    private val amount = params.getDouble("p_damage_by_attack_param2")
    private val modifierType: StatModifierType =
            params.getEnum(
                    "p_damage_by_attack_param3",
                    StatModifierType::class.java,
                    true
            )

    init {
        require(type == DamageByAttackType.PK || type == DamageByAttackType.ENEMY_ALL)
    }

    override fun pump(
            target: Creature,
            skillEntry: SkillEntry?
    ) {
        when (type) {
            DamageByAttackType.PK -> {
                if (skillEntry != null) {
                    target.stat.mergeAdd(DoubleStat.PVP_DAMAGE_TAKEN, amount, skillEntry)
                } else {
                    target.stat.mergeAdd(DoubleStat.PVP_DAMAGE_TAKEN, amount, skill)
                }
            }
            DamageByAttackType.ENEMY_ALL -> {
                if (skillEntry != null) {
                    target.stat.mergeAdd(DoubleStat.PVE_DAMAGE_TAKEN, amount, skillEntry)
                } else {
                    target.stat.mergeAdd(DoubleStat.PVE_DAMAGE_TAKEN, amount, skill)
                }
            }
        }
    }

}