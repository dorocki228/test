package l2s.gameserver.model.entity.events.impl.arena.permission;

import l2s.gameserver.model.*;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.model.entity.events.impl.ArenaEvent;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.permission.ActionPermissionContext;
import l2s.gameserver.permission.EActionPermissionReturnType;
import l2s.gameserver.permission.interfaces.IAttackPermission;

/**
 * @author mangol
 */
public class ArenaAttackTeamPvpZoneImpl implements IAttackPermission {
	private final Zone zone;
	private final ArenaEvent arenaEvent;

	public ArenaAttackTeamPvpZoneImpl(Zone zone, ArenaEvent arenaEvent) {
		this.zone = zone;
		this.arenaEvent = arenaEvent;
	}

	@Override
	public EActionPermissionReturnType canAttack(ActionPermissionContext context, Creature attacker, Creature target, Skill skill) {
		if(!attacker.isPlayable() || !target.isPlayable()) {
			return EActionPermissionReturnType.None;
		}
		if(!arenaEvent.isInBattle()) {
			context.setMessage(SystemMsg.INVALID_TARGET);
			return EActionPermissionReturnType.Failure;
		}
		if(!zone.checkIfInZone(target)) {
			context.setMessage(SystemMsg.INVALID_TARGET);
			return EActionPermissionReturnType.Failure;
		}

		Playable pAttacker = (Playable) attacker;
		Playable pTarget = (Playable) target;

		if(pAttacker.getTeam() == TeamType.NONE || pTarget.getTeam() == TeamType.NONE) {
			context.setMessage(SystemMsg.INVALID_TARGET);
			return EActionPermissionReturnType.Failure;
		}

		Player playerAttacker = pAttacker.getPlayer();
		Player playerTarget = pTarget.getPlayer();

		if(playerAttacker == playerTarget && (skill == null || skill.isOffensive())) {
			context.setMessage(SystemMsg.INVALID_TARGET);
			return EActionPermissionReturnType.Success;
		}

		if(pTarget.isSummon()) {
			if(playerTarget == playerAttacker) {
				if(!skill.isOffensive() || skill.getSkillType().isHeal()) {
					return EActionPermissionReturnType.Success;
				}
				else {
					context.setMessage(SystemMsg.INVALID_TARGET);
					return EActionPermissionReturnType.Failure;
				}
			}
		}

		if(pAttacker.getTeam() == pTarget.getTeam()) {
			if(skill != null && (skill.getSkillType().isHeal() || !skill.isOffensive())) {
				return EActionPermissionReturnType.Success;
			}
			else {
				context.setMessage(SystemMsg.INVALID_TARGET);
				return EActionPermissionReturnType.Failure;
			}
		}
		return EActionPermissionReturnType.Success;
	}
}
