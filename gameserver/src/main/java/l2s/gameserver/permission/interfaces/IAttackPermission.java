package l2s.gameserver.permission.interfaces;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.permission.ActionPermissionContext;
import l2s.gameserver.permission.EActionPermissionReturnType;

/**
 * @author mangol
 */
public interface IAttackPermission extends IActionPermission {
	@Override
	default EActionPermissionReturnType test(ActionPermissionContext context, Object... args) {
		Skill skill = args.length > 2 ? args[2] != null ? (Skill) args[2] : null : null;
		return canAttack(context, (Creature) args[0], (Creature) args[1], skill);
	}

	EActionPermissionReturnType canAttack(ActionPermissionContext context, Creature attacker, Creature target, Skill skill);
}
