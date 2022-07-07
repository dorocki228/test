package l2s.authserver.network.l2.s2c;

import l2s.authserver.network.l2.SessionKey;

public final class PlayOk extends L2LoginServerPacket
{
	private final int _playOk1;
	private final int _playOk2;
	private final int _serverId;

	public PlayOk(SessionKey sessionKey, int serverId)
	{
		_playOk1 = sessionKey.playOkID1;
		_playOk2 = sessionKey.playOkID2;
		_serverId = serverId;
	}

	@Override
	protected void writeImpl()
	{
		writeC(7);
		writeD(_playOk1);
		writeD(_playOk2);
		writeC(_serverId);
	}
}
