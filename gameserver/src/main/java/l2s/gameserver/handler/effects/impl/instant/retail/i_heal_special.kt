package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.model.base.BaseStats
import l2s.gameserver.model.base.ShotType
import l2s.gameserver.network.l2.components.StatusUpdate
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.network.l2.s2c.ExMagicAttackInfo
import l2s.gameserver.network.l2.s2c.StatusUpdatePacket
import l2s.gameserver.network.l2.s2c.SystemMessagePacket
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.stats.Formulas
import l2s.gameserver.templates.skill.EffectTemplate
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * HpCpHeal effect implementation.
 * @author Sdw
 * @author Java-man
 */
class i_heal_special(template: EffectTemplate) : i_abstract_effect(template) {

    private val _power = params.getDouble("i_heal_param1")

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        val targetCreature = target.asCreature() ?: return

        if (targetCreature.isDead || targetCreature.isDoor || targetCreature.isHpBlocked) {
            return
        }

        /*if (caster.equals(targetCreature) && targetCreature.stat.has(BooleanStat.FACE_OFF)) {
            return
        }*/

        // TODO: Shots bonus needs more propper formula.
        val shotsBonus = when {
            skill.isMagic -> {
                val ssBonus = caster.stat.getValue(DoubleStat.SPIRITSHOTS_BONUS)
                val ssMod = when {
                    caster.isChargedShot(ShotType.BLESSED_SPIRITSHOT) -> 36.0
                    caster.isChargedShot(ShotType.SPIRITSHOT) -> 18.0
                    else -> 1.0
                }
                ssBonus * ssMod
            }
            else -> 1.0
        }
        val mAtkBonus = sqrt(caster.stat.getMAtk() * shotsBonus)
        val healEffectAdd = caster.stat.getAdd(DoubleStat.HEAL_EFFECT)
        val healEffectMul = targetCreature.stat.getMul(DoubleStat.HEAL_EFFECT)
        val baseAmount = (_power + mAtkBonus + healEffectAdd) * healEffectMul
        val levelMod = (caster.level + 89 + 5.5 * max(caster.level - 99, 0)) / 100.0
        val weaponBaseValue = DoubleStat.weaponBaseValue(caster, DoubleStat.MAGICAL_ATTACK)
        val menBonus = BaseStats.MEN.calcBonus(caster)
        val weaponBonus = min(levelMod * weaponBaseValue * menBonus, baseAmount)
        var amount = baseAmount + weaponBonus

        // Heal critic, since CT2.3 Gracia Final
        if (skill.isMagic && Formulas.calcCrit(skill.magicCriticalRate, caster, targetCreature, skill)) {
            amount *= 3.0
            caster.sendPacket(SystemMsg.MAGIC_CRITICAL_HIT)
            caster.sendPacket(ExMagicAttackInfo(caster.objectId, targetCreature.objectId, ExMagicAttackInfo.CRITICAL_HEAL))
            if (targetCreature.isPlayer && targetCreature != caster) {
                targetCreature.sendPacket(ExMagicAttackInfo(caster.objectId, targetCreature.objectId, ExMagicAttackInfo.CRITICAL_HEAL))
            }
        }

        // Prevents overheal
        val hpAmount = min(amount, targetCreature.stat.getMaxRecoverableHp() - targetCreature.currentHp)
        if (hpAmount != 0.0) {
            val newHp = hpAmount + targetCreature.currentHp
            targetCreature.setCurrentHp(newHp, false, false)
            // TODO targetCreature.broadcastStatusUpdate(caster)

            val su = StatusUpdate(target, caster, StatusUpdatePacket.UpdateType.REGEN, StatusUpdatePacket.CUR_HP)
            caster.sendPacket(su)
            target.sendPacket(su)
            target.broadcastStatusUpdate()
        }

        if (targetCreature.isPlayer) {
            if (caster.isPlayer && caster != targetCreature) {
                val sm = SystemMessagePacket(SystemMsg.S2_HP_HAS_BEEN_RESTORED_BY_C1)
                sm.addString(caster.name)
                sm.addInteger(hpAmount)
                targetCreature.sendPacket(sm)
            } else {
                val sm = SystemMessagePacket(SystemMsg.S1_HP_HAS_BEEN_RESTORED)
                sm.addInteger(hpAmount)
                targetCreature.sendPacket(sm)
            }

            val cpAmount = max(min(amount - hpAmount, targetCreature.stat.getMaxRecoverableCp() - targetCreature.currentCp), 0.0)
            if (cpAmount != 0.0) {
                val newCp = cpAmount + targetCreature.currentCp
                targetCreature.setCurrentCp(newCp, false)
                // TODO targetCreature.broadcastStatusUpdate(caster)

                val su = StatusUpdate(target, caster, StatusUpdatePacket.UpdateType.REGEN, StatusUpdatePacket.CUR_CP)
                caster.sendPacket(su)
                target.sendPacket(su)
                target.broadcastStatusUpdate()
            }

            if (cpAmount > 0 || hpAmount == 0.0) {
                if (caster.isPlayer && caster != targetCreature) {
                    val sm = SystemMessagePacket(SystemMsg.S2_CP_HAS_BEEN_RESTORED_BY_C1)
                    sm.addName(caster)
                    sm.addInteger(cpAmount)
                    targetCreature.sendPacket(sm)
                } else {
                    val sm = SystemMessagePacket(SystemMsg.S1_CP_HAS_BEEN_RESTORED)
                    sm.addInteger(cpAmount)
                    targetCreature.sendPacket(sm)
                }
            }
        }
    }

}
