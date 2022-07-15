package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.commons.util.Rnd
import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.stats.Formulas
import l2s.gameserver.templates.skill.EffectTemplate
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Lethal effect implementation.
 * @author Adry_85
 * @author Java-man
 */
class i_death(template: EffectTemplate) : i_abstract_effect(template) {

    private val fullLethal = params.getDouble("i_death_param1")
    private val halfLethal = params.getDouble("i_death_param2")

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        val targetCreature = target.asCreature() ?: return

        /*if (caster.isPlayer && !caster.accessLevel.canGiveDamage()) {
            return
        }*/

        if (skill.magicLevel < targetCreature.level - 6) {
            return
        }

        if (targetCreature.isLethalImmune || targetCreature.isHpBlocked) {
            return
        }

        /*if (caster.isPlayer && targetCreature.isPlayer && targetCreature.stat.has(BooleanStat.FACE_OFF) && targetCreature.asPlayer().getAttackerObjId() != caster.objectId) {
            return
        }*/

        val attributeBonus = Formulas.calcAttributeBonus(caster, targetCreature, skill)
        val traitBonus = Formulas.calcGeneralTraitBonus(caster, targetCreature, skill.traitType, false)
        val resist = targetCreature.stat.getValue(DoubleStat.INSTANT_KILL_RESIST, 1.0)
        val chanceMultiplier = attributeBonus * traitBonus * resist

        if (Rnd.get(100) < fullLethal * chanceMultiplier) {
            // for Players CP and HP is set to 1.
            if (targetCreature.isPlayer) {
                targetCreature.currentCp = 1.0
                targetCreature.currentHp = 1.0
                targetCreature.sendPacket(SystemMsg.LETHAL_STRIKE)
            } else if (targetCreature.isMonster || targetCreature.isSummon) {
                // for Monsters HP is set to 1.
                targetCreature.currentHp = 1.0
            }
            caster.sendPacket(SystemMsg.YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL)
        } else if (Rnd.get(100) < halfLethal * chanceMultiplier) {
            // for Players CP is set to 1.
            if (targetCreature.isPlayer) {
                targetCreature.currentCp = 1.0
                targetCreature.sendPacket(SystemMsg.CP_SIPHON)
                targetCreature.sendPacket(SystemMsg.YOUR_CP_WAS_DRAINED_BECAUSE_YOU_WERE_HIT_WITH_A_CP_SIPHON_SKILL)
            } else if (targetCreature.isMonster || targetCreature.isSummon) {
                // for Monsters HP is set to 50%.
                targetCreature.currentHp = targetCreature.currentHp * 0.5
            }
            caster.sendPacket(SystemMsg.CP_SIPHON)
        }

        // No matter if lethal succeeded or not, its reflected.
        Formulas.calcCounterAttack(caster, targetCreature, skill, false)
    }

}
