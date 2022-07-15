package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.model.base.ShotType
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.network.l2.s2c.SystemMessagePacket
import l2s.gameserver.stats.Formulas
import l2s.gameserver.templates.skill.EffectTemplate
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Magical Attack MP effect.
 * @author Adry_85
 * @author Java-man
 */
class i_m_attack_mp(template: EffectTemplate?) : i_abstract_effect(template) {

    private val _power = params.getDouble("i_m_attack_mp_param1")
    private val _critical = params.getInteger("i_m_attack_mp_param2") == 1
    private val _criticalLimit = params.getDouble("i_m_attack_mp_param3")

    override fun calcSuccess(caster: Creature, target: Creature, skill: Skill): Boolean {
        val targetCreature = target.asCreature() ?: return false

        if (targetCreature.isMpBlocked) {
            return false
        }

        /* TODO
        if (caster.isPlayer && targetCreature.isPlayer) {
            if (targetCreature.stat.has(BooleanStat.FACE_OFF) && targetCreature.player.getAttackerObjId() != caster.objectId) {
                return false
            }
        }*/

        if (!Formulas.calcMagicAffected(caster, targetCreature, skill)) {
            if (caster.isPlayer) {
                caster.sendPacket(SystemMsg.YOUR_ATTACK_HAS_FAILED)
            }
            if (targetCreature.isPlayer) {
                val sm = SystemMessagePacket(SystemMsg.C1_RESISTED_C2S_DRAIN)
                sm.addName(targetCreature)
                sm.addName(caster)
                targetCreature.sendPacket(sm)
            }
            return false
        }

        return true
    }

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        val targetCreature = target.asCreature() ?: return

        if (!targetCreature.isCreature || caster.isAlikeDead) {
            return
        }

        val sps = skill.useSpiritShot() && caster.isChargedShot(ShotType.SPIRITSHOT)
        val bss = skill.useSpiritShot() && caster.isChargedShot(ShotType.BLESSED_SPIRITSHOT)
        val shld = Formulas.calcShldUse(caster, targetCreature)
        val mcrit = if (_critical) Formulas.calcCrit(skill.magicCriticalRate, caster, targetCreature, skill) else false
        val damage = Formulas.calcManaDam(caster, targetCreature, skill, _power, shld, sps, bss, mcrit, _criticalLimit)
        val mp = Math.min(targetCreature.currentMp, damage)

        if (damage > 0) {
            targetCreature.abnormalList.stopEffectsOnDamage()
            targetCreature.currentMp = targetCreature.currentMp - mp
        }

        if (targetCreature.isPlayer) {
            val sm = SystemMessagePacket(SystemMsg.S2S_MP_HAS_BEEN_DRAINED_BY_C1)
            sm.addName(caster)
            sm.addInteger(mp)
            targetCreature.sendPacket(sm)
        }

        if (caster.isPlayer) {
            val sm2 = SystemMessagePacket(SystemMsg.YOUR_OPPONENTS_MP_WAS_REDUCED_BY_S1)
            sm2.addInteger(mp)
            caster.sendPacket(sm2)
        }
    }

}