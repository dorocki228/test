package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.Config;

public class PledgeCrestPacket extends L2GameServerPacket
{
	private final int _crestId;
	private final int _crestSize;
	private final byte[] _data;

	public PledgeCrestPacket(int crestId, byte[] data)
	{
		_crestId = crestId;
		_data = data;
		_crestSize = _data.length;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(Config.REQUEST_ID);
		writeD(_crestId);
		writeD(_crestSize);
		writeB(_data);
	}
}
