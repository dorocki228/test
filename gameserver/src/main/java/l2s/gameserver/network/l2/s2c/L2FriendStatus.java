package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;

public class L2FriendStatus extends L2GameServerPacket
{
	private final String _charName;
	private final boolean _login;

	public L2FriendStatus(Player player, boolean login)
	{
		_login = login;
		_charName = player.getName();
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_login ? 1 : 0);
		writeS(_charName);
        writeD(0);
	}
}
