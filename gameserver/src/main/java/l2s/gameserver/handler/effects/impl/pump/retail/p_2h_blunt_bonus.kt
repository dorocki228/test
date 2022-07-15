package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.skills.SkillEntry
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.stats.StatModifierType
import l2s.gameserver.templates.item.ItemTemplate
import l2s.gameserver.templates.item.WeaponTemplate
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * @author Sdw
 * @author Java-man
 *
 * @since 12.10.2019
 */
class p_2h_blunt_bonus(template: EffectTemplate) : EffectHandler(template) {

    private val pAtkAmount = params.getDouble("p_2h_blunt_bonus_param1")
    private val pAtkmode = params.getEnum(
            "p_2h_blunt_bonus_param2",
            StatModifierType::class.java,
            true
    )

    private val accuracyAmount = params.getDouble("p_2h_blunt_bonus_param3")
    private val accuracyMode = params.getEnum(
            "p_2h_blunt_bonus_param4",
            StatModifierType::class.java,
            true
    )

    override fun checkPumpCondition(abnormal: Abnormal?, caster: Creature, target: Creature): Boolean {
        if (!caster.isPlayer) {
            return true
        }

        val player = caster.player
        val weaponTemplate = player.activeWeaponTemplate ?: return false
        val blunt = weaponTemplate.itemType == WeaponTemplate.WeaponType.BLUNT
                || weaponTemplate.itemType == WeaponTemplate.WeaponType.BIGBLUNT
                || weaponTemplate.itemType == WeaponTemplate.WeaponType.DUALBLUNT
        return blunt && weaponTemplate.bodyPart and ItemTemplate.SLOT_LR_HAND != 0.toLong()
    }

    override fun pump(
            target: Creature,
            skillEntry: SkillEntry?
    ) {
        when (pAtkmode) {
            StatModifierType.DIFF -> {
                if (skillEntry != null) {
                    target.stat.mergeAdd(DoubleStat.PHYSICAL_ATTACK, pAtkAmount, skillEntry)
                } else {
                    target.stat.mergeAdd(DoubleStat.PHYSICAL_ATTACK, pAtkAmount, skill)
                }
            }
            StatModifierType.PER -> {
                if (skillEntry != null) {
                    target.stat.mergeMul(DoubleStat.PHYSICAL_ATTACK, pAtkAmount / 100 + 1, skillEntry)
                } else {
                    target.stat.mergeMul(DoubleStat.PHYSICAL_ATTACK, pAtkAmount / 100 + 1, skill)
                }
            }
        }

        when (accuracyMode) {
            StatModifierType.DIFF -> {
                if (skillEntry != null) {
                    target.stat.mergeAdd(DoubleStat.ACCURACY_COMBAT, accuracyAmount, skillEntry)
                } else {
                    target.stat.mergeAdd(DoubleStat.ACCURACY_COMBAT, accuracyAmount, skill)
                }
            }
            StatModifierType.PER -> {
                if (skillEntry != null) {
                    target.stat.mergeMul(DoubleStat.ACCURACY_COMBAT, accuracyAmount / 100 + 1, skillEntry)
                } else {
                    target.stat.mergeMul(DoubleStat.ACCURACY_COMBAT, accuracyAmount / 100 + 1, skill)
                }
            }
        }
    }

}