package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.network.l2.components.StatusUpdate
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.network.l2.s2c.StatusUpdatePacket
import l2s.gameserver.network.l2.s2c.SystemMessagePacket
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.templates.skill.EffectTemplate
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.min

/**
 * Heal effect implementation.<br>
 * Retail-like formula without shots: <br>
 * Heal = base_heal + weapon_bonus <br>
 * base_heal = (power + sqrt(mAtk) + staticHealBonus) * percentHealBonus <br>
 * weapon_bonus = lvlMod * weapon_mAtk * MENmod, highest can be as much as base_heal is. <br>
 * lvlMod =(lvl+89+5,5*(lvl-99))/100. if {lvl-99}<0, then {lvl-99}=0. <br>
 * weapon_mAtk - M.Atk of the weapon. Enchants are taken into account as well. <br>
 * <br>
 * heal_effect increasing buffs appear to be increasing healer's power if diff(static amount), and healed's received heal in per (percent amount). <br>
 * @author UnAfraid
 * @author Java-man
 */
class cub_heal(template: EffectTemplate) : i_abstract_effect(template) {

    private val _power = params.getDouble("cub_heal_param1")

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        val targetCreature = target.asCreature() ?: return

        if (targetCreature.isDead || targetCreature.isDoor || targetCreature.isHpBlocked) {
            return
        }

        /*if (caster.equals(targetCreature) && targetCreature.stat.has(BooleanStat.FACE_OFF)) {
            return
        }*/

        //val mAtkBonus = sqrt(caster.stat.getMAtk())
        //val mAtkBonus = sqrt(cubic.template.power)
        //val healEffectAdd = caster.stat.getAdd(DoubleStat.HEAL_EFFECT)
        val healEffectMul = targetCreature.stat.getMul(DoubleStat.HEAL_EFFECT)
        val baseAmount = (_power/* + mAtkBonus + healEffectAdd*/) * healEffectMul
        /*val levelMod = (caster.level + 89 + 5.5 * max(caster.level - 99, 0)) / 100.0
        val weaponBaseValue = DoubleStat.weaponBaseValue(caster, DoubleStat.MAGICAL_ATTACK)
        val menBonus = BaseStats.MEN.calcBonus(caster)
        val weaponBonus = min(levelMod * weaponBaseValue * menBonus, baseAmount)*/
        var amount = baseAmount //+ weaponBonus

        // Heal critic, since CT2.3 Gracia Final
        /*if (skill.isMagic && Formulas.calcCrit(skill.magicCriticalRate, caster, targetCreature, skill)) {
            amount *= 3.0
            caster.sendPacket(SystemMsg.MAGIC_CRITICAL_HIT)
            caster.sendPacket(ExMagicAttackInfo(caster.objectId, targetCreature.objectId, ExMagicAttackInfo.CRITICAL_HEAL))
            if (targetCreature.isPlayer && targetCreature != caster) {
                targetCreature.sendPacket(ExMagicAttackInfo(caster.objectId, targetCreature.objectId, ExMagicAttackInfo.CRITICAL_HEAL))
            }
        }*/

        // Prevents overheal
        amount = min(amount, targetCreature.stat.getMaxRecoverableHp() - targetCreature.currentHp)
        if (amount != 0.0) {
            val newHp = amount + targetCreature.currentHp
            targetCreature.setCurrentHp(newHp, false, false)
            // TODO targetCreature.broadcastStatusUpdate(caster)

            val su = StatusUpdate(target, caster, StatusUpdatePacket.UpdateType.REGEN, StatusUpdatePacket.CUR_HP)
            caster.sendPacket(su)
            target.sendPacket(su)
            target.broadcastStatusUpdate()
        }

        if (targetCreature.isPlayer) {
            if (skill.id == 4051) {
                targetCreature.sendPacket(SystemMsg.REJUVENATING_HP)
            } else {
                if (caster.isPlayer && caster != targetCreature) {
                    val sm = SystemMessagePacket(SystemMsg.S2_HP_HAS_BEEN_RESTORED_BY_C1)
                    sm.addName(caster)
                    sm.addInteger(amount)
                    targetCreature.sendPacket(sm)
                } else {
                    val sm = SystemMessagePacket(SystemMsg.S1_HP_HAS_BEEN_RESTORED)
                    sm.addInteger(amount)
                    targetCreature.sendPacket(sm)
                }
            }
        }
    }

}
