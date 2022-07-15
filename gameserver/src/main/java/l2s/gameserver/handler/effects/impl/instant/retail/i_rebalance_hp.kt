package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.templates.skill.EffectTemplate
import l2s.gameserver.utils.PositionUtils
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Rebalance HP effect implementation.
 * @author Adry_85, earendil
 * @author Java-man
 */
class i_rebalance_hp(template: EffectTemplate?) : i_abstract_effect(template) {

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        if (!caster.isPlayer) {
            return
        }

        var fullHP = 0.0
        var currentHPs = 0.0
        val party = caster.party
        if (party != null) {
            val affectRange = skill.affectRange

            for (member in party) {
                if (!member.isDead && PositionUtils.checkIfInRange(affectRange, caster, member, true)) {
                    fullHP += member.maxHp
                    currentHPs += member.currentHp
                }
                /*val summon = member.pet
                if (summon != null && !summon.isDead && PositionUtils.checkIfInRange(skill.affectRange, caster, summon, true)) {
                    fullHP += summon.maxHp
                    currentHPs += summon.currentHp
                }*/
                for (servitors in member.servitors) {
                    if (!servitors.isDead && PositionUtils.checkIfInRange(affectRange, caster, servitors, true)) {
                        fullHP += servitors.maxHp
                        currentHPs += servitors.currentHp
                    }
                }
            }
            val percentHP = currentHPs / fullHP
            for (member in party) {
                if (!member.isDead && PositionUtils.checkIfInRange(affectRange, caster, member, true)) {
                    var newHP: Double = member.maxHp * percentHP
                    if (newHP > member.currentHp) // The target gets healed
                    {
                        // The heal will be blocked if the current hp passes the limit
                        if (member.currentHp > member.stat.getMaxRecoverableHp()) {
                            newHP = member.currentHp
                        } else if (newHP > member.stat.getMaxRecoverableHp()) {
                            newHP = member.stat.getMaxRecoverableHp().toDouble()
                        }
                    }
                    member.currentHp = newHP
                }
                /*val summon = member.pet
                if (summon != null && !summon.isDead && PositionUtils.checkIfInRange(skill.affectRange, caster, summon, true)) {
                    var newHP: Double = summon.maxHp * percentHP
                    if (newHP > summon.currentHp) // The target gets healed
                    {
                        // The heal will be blocked if the current hp passes the limit
                        if (summon.currentHp > summon.stat.getMaxRecoverableHp()) {
                            newHP = summon.currentHp
                        } else if (newHP > summon.stat.getMaxRecoverableHp()) {
                            newHP = summon.stat.getMaxRecoverableHp().toDouble()
                        }
                    }
                    summon.currentHp = newHP
                }*/
                for (servitors in member.servitors) {
                    if (!servitors.isDead && PositionUtils.checkIfInRange(affectRange, caster, servitors, true)) {
                        var newHP: Double = servitors.maxHp * percentHP
                        if (newHP > servitors.currentHp) // The target gets healed
                        {
                            // The heal will be blocked if the current hp passes the limit
                            if (servitors.currentHp > servitors.stat.getMaxRecoverableHp()) {
                                newHP = servitors.currentHp
                            } else if (newHP > servitors.stat.getMaxRecoverableHp()) {
                                newHP = servitors.stat.getMaxRecoverableHp().toDouble()
                            }
                        }
                        servitors.currentHp = newHP
                    }
                }
            }
        }
    }

}