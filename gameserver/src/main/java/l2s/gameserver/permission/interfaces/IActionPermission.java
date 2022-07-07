package l2s.gameserver.permission.interfaces;

import l2s.gameserver.permission.ActionPermissionContext;
import l2s.gameserver.permission.EActionPermissionReturnType;

/**
 * @author mangol
 */
@FunctionalInterface
public interface IActionPermission {
	EActionPermissionReturnType test(ActionPermissionContext context, Object... args);
}
