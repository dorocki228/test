package l2s.gameserver.model.items;

/**
 * @author VISTALL
 * @date 13:51/16.05.2011
 */
public enum LockType
{
	NONE(-1),
	EXCLUDE(0),
	INCLUDE(1);

	private int _clientId;

	LockType(int clientId) {
		_clientId = clientId;
	}

	public int getClientId() {
		return _clientId;
	}
}
