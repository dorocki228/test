package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.model.base.ShotType
import l2s.gameserver.skills.AbnormalType
import l2s.gameserver.stats.Formulas
import l2s.gameserver.templates.skill.EffectTemplate
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Magical Attack By Abnormal Slot effect implementation.
 * @author Sdw
 * @author Java-man
 */
class i_m_attack_by_abnormal_slot(template: EffectTemplate) : i_abstract_effect(template) {

    private val _power = params.getDouble("i_m_attack_by_abnormal_slot_param2")
    private val abnormalType: AbnormalType

    init {
        val name = params.getString("i_m_attack_by_abnormal_slot_param1").toUpperCase()
        abnormalType = AbnormalType.valueOf(name)
        require(abnormalType != AbnormalType.NONE) {
            "Skill ${skill.id} abnormal_type should not be NONE."
        }
    }

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        val targetCreature = target.asCreature() ?: return

        if (caster.isAlikeDead || targetCreature.abnormalList.stop(abnormalType) > 0) {
            return
        }

        if (targetCreature.isFakeDeath) {
            targetCreature.breakFakeDeath()
        }

        val sps = skill.useSpiritShot() && caster.isChargedShot(ShotType.SPIRITSHOT)
        val bss = skill.useSpiritShot() && caster.isChargedShot(ShotType.BLESSED_SPIRITSHOT)
        val mcrit = Formulas.calcCrit(skill.magicCriticalRate, caster, targetCreature, skill)
        var damage = Formulas.calcMagicDam(
                caster,
                targetCreature,
                skill,
                caster.stat.getMAtk(),
                _power,
                targetCreature.stat.getMDef(),
                sps,
                bss,
                mcrit
        )

        caster.doAttack(damage, targetCreature, skill, false, false, mcrit, false)
    }
}