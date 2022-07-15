package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.commons.math.constrain
import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Player
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.model.skill.SkillTarget
import l2s.gameserver.network.l2.components.StatusUpdate
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.network.l2.s2c.StatusUpdatePacket
import l2s.gameserver.network.l2.s2c.SystemMessagePacket
import l2s.gameserver.stats.StatModifierType
import l2s.gameserver.templates.skill.EffectTemplate
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Heal link effect implementation.
 * @author Java-man
 */
class i_heal_link(template: EffectTemplate) : i_abstract_effect(template) {

    private val startHealPercent = params.getDouble("i_heal_link_param1")
    private val modifierType: StatModifierType =
            params.getEnum(
                    "i_heal_link_param2",
                    StatModifierType::class.java,
                    true
            )
    private val healModifier = params.getDouble("i_heal_link_param3")

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        // don't need to implement
    }

    override fun instantUse(caster: Creature, targets: List<SkillTarget>, soulShotUsed: AtomicBoolean, cubic: Cubic) {
        var healPercent = startHealPercent

        for (target in targets) {
            if (healPercent <= 0) {
                break
            }

            val target = target.target
            if (target.isHpBlocked) {
                continue
            }

            val hp: Double = target.stat.getMaxRecoverableHp() * healPercent / 100.0
            // Prevents overheal
            val addToHp = hp.constrain(0.0, target.stat.getMaxRecoverableHp() - target.currentHp)
            if (addToHp > 0) {
                target.setCurrentHp(addToHp + target.currentHp, false, false)
                // TODO target.broadcastStatusUpdate(caster)

                val su = StatusUpdate(target, caster, StatusUpdatePacket.UpdateType.REGEN, StatusUpdatePacket.CUR_HP)
                caster.sendPacket(su)
                target.sendPacket(su)
                target.broadcastStatusUpdate()
            }
            if (target.isPlayer) {
                if (caster.isPlayer && caster != target) {
                    val sm = SystemMessagePacket(SystemMsg.S2_HP_HAS_BEEN_RESTORED_BY_C1)
                    sm.addName(caster)
                    sm.addInteger(addToHp)
                    target.sendPacket(sm)
                } else {
                    val sm = SystemMessagePacket(SystemMsg.S1_HP_HAS_BEEN_RESTORED)
                    sm.addInteger(addToHp)
                    target.sendPacket(sm)
                }
            }

            healPercent -= healModifier
        }
    }

    private fun checkMainTarget(activeChar: Creature, target: Creature?): Boolean {
        // @Rivelia.
        // If the target is ourself and we are not heal blocked, it should heal because we are main target.
        // The heal won't apply if our main target:
        // 	- Is heal blocked (see UseSkill).
        //	- Is a door.
        //	- Is a monster.
        //  - Is in duel.
        //  - Is curse weapon equipped.
        //  - Is not friendly (same party/clan/ally).
        //  - Is auto attackable (oly fix).
        if (target == null) return false
        if (activeChar === target) return true
        if (target.isDoor || target.isMonster || activeChar.isAutoAttackable(target)) return false
        if (target.isPlayer) {
            val activeCharTarget = target.player
            val activeCharPlayer = activeChar.player
            // @Rivelia. Do not accept target in duel if it's not ourself, is curse weapon equipped or is not friendly.
            if (activeCharTarget.isInDuel && activeCharPlayer!!.objectId != activeCharTarget.objectId || activeCharPlayer != null && !isTargetFriendly(activeCharPlayer, activeCharTarget)) return false
        }
        return true
    }

    private fun isTargetFriendly(activeCharPlayer: Player, activeCharTarget: Player): Boolean { /*На оффе можно хилить даже несопартийцев.
		boolean _party = activeCharTarget.isInParty();
		boolean _partySelf = activeCharPlayer.isInParty();
		boolean _isInSameParty = false;

		boolean _clan = activeCharTarget.getClan() == null ? false : true;
		boolean _clanSelf = activeCharPlayer.getClan() == null ? false : true;
		boolean _isInSameClan = false;

		boolean _ally = activeCharTarget.getAlliance() == null ? false : true;
		boolean _allySelf = activeCharPlayer.getAlliance() == null ? false : true;
		boolean isInSameAlly = false;

		if(_party && _partySelf)
			for(Player member : activeCharPlayer.getParty().getPartyMembers())
				if(member == activeCharTarget)
					_isInSameParty = true;

		if(_clan && _clanSelf)
			for (Player clanMember : activeCharPlayer.getClan().getOnlineMembers())
				if(clanMember == activeCharTarget)
					_isInSameClan = true;

		if(_ally && _allySelf)
			if(activeCharPlayer.getClan().getAllyId() == activeCharTarget.getClan().getAllyId() && activeCharPlayer.getClan().getAllyId() != 0)
				isInSameAlly = true;

		if(!isInSameAlly && !_isInSameClan && !_isInSameParty)
			return false;*/

        return true
    }

}
