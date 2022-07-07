package l2s.gameserver.permission.impl;

import l2s.gameserver.model.*;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.permission.ActionPermissionContext;
import l2s.gameserver.permission.EActionPermissionReturnType;
import l2s.gameserver.permission.interfaces.IAttackPermission;

import java.util.Objects;

/**
 * @author mangol
 */
public class AttackNoLimitPvpZoneImpl implements IAttackPermission {
	private final Zone zone;

	public AttackNoLimitPvpZoneImpl(Zone zone) {
		this.zone = zone;
	}

	@Override
	public EActionPermissionReturnType canAttack(ActionPermissionContext context, Creature attacker, Creature target, Skill skill) {
		if(!attacker.isPlayable() || !target.isPlayable()) {
			return EActionPermissionReturnType.None;
		}
		if(!zone.checkIfInZone(target)) {
			context.setMessage(SystemMsg.INVALID_TARGET);
			return EActionPermissionReturnType.Failure;
		}

		Playable pAttacker = (Playable) attacker;
		Playable pTarget = (Playable) target;

		Player playerAttacker = pAttacker.getPlayer();
		Player playerTarget = pTarget.getPlayer();

		if(playerAttacker == playerTarget && (skill == null || skill.isOffensive())) {
			context.setMessage(SystemMsg.INVALID_TARGET);
			return EActionPermissionReturnType.None;
		}

		if(pTarget.isSummon()) {
			if(playerAttacker == playerTarget) {
				if(!skill.isOffensive() || skill.getSkillType().isHeal()) {
					return EActionPermissionReturnType.Success;
				}
				else {
					context.setMessage(SystemMsg.INVALID_TARGET);
					return EActionPermissionReturnType.Failure;
				}
			}
		}

		if(!pAttacker.getFraction().canAttack(pTarget)) {
			Party playerAttackerParty = playerAttacker.getParty();
			Party playerTargetParty = playerTarget.getParty();
			if(playerAttackerParty == null || playerTargetParty == null) {
				if(skill != null && (!skill.isOffensive() || skill.getSkillType().isHeal())) {
					context.setMessage(SystemMsg.INVALID_TARGET);
					return EActionPermissionReturnType.Failure;
				}
				return EActionPermissionReturnType.Success;
			}
			if(Objects.equals(playerAttackerParty, playerTargetParty)) {
				if(skill != null && (skill.getSkillType().isHeal() || !skill.isOffensive())) {
					return EActionPermissionReturnType.Success;
				}
				context.setMessage(SystemMsg.INVALID_TARGET);
				return EActionPermissionReturnType.Failure;
			}

			return EActionPermissionReturnType.Success;
		}
		return EActionPermissionReturnType.None;
	}
}
