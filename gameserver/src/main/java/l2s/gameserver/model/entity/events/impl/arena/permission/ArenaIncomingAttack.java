package l2s.gameserver.model.entity.events.impl.arena.permission;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.entity.events.impl.ArenaEvent;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.permission.ActionPermissionContext;
import l2s.gameserver.permission.EActionPermissionReturnType;
import l2s.gameserver.permission.interfaces.IIncomingAttackPermission;

/**
 * @author mangol
 */
public class ArenaIncomingAttack implements IIncomingAttackPermission {
	private final ArenaEvent arenaEvent;

	public ArenaIncomingAttack(ArenaEvent arenaEvent) {
		this.arenaEvent = arenaEvent;
	}

	@Override
	public EActionPermissionReturnType canTakeAttack(ActionPermissionContext context, Creature attacker, Creature actor, Skill skill) {
		if(!arenaEvent.isParticipant(attacker.getObjectId())) {
			context.setMessage(SystemMsg.INVALID_TARGET);
			return EActionPermissionReturnType.Failure;
		}
		return EActionPermissionReturnType.Success;
	}
}
