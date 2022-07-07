package l2s.gameserver.permission;

import l2s.gameserver.network.l2.components.SystemMsg;

/**
 * @author mangol
 */
public class ActionPermissionContext {
	private EActionPermissionLevel level;
	private SystemMsg message;
	private boolean success;

	public EActionPermissionLevel getLevel() {
		return level;
	}

	public void setLevel(EActionPermissionLevel level) {
		this.level = level;
	}

	public void setMessage(SystemMsg message) {
		this.message = message;
	}

	public SystemMsg getMessage() {
		return message;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public boolean isSuccess() {
		return success;
	}
}
