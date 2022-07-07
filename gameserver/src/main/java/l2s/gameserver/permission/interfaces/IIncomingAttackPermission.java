package l2s.gameserver.permission.interfaces;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.permission.ActionPermissionContext;
import l2s.gameserver.permission.EActionPermissionReturnType;

/**
 * @author mangol
 */
public interface IIncomingAttackPermission extends IActionPermission {
	@Override
	default EActionPermissionReturnType test(ActionPermissionContext context, Object... args) {
		Skill skill = args.length > 2 ? args[2] != null ? (Skill) args[2] : null : null;
		return canTakeAttack(context, (Creature) args[0], (Creature) args[1], skill);
	}

	EActionPermissionReturnType canTakeAttack(ActionPermissionContext context, Creature attacker, Creature actor, Skill skill);
}
