package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.Config;

public class ExPledgeEmblem extends L2GameServerPacket
{
	private final int _clanId;
	private final int _crestId;
	private final int _crestPart;
	private final int _totalSize;
	private final byte[] _data;

	public ExPledgeEmblem(int clanId, int crestId, int crestPart, int totalSize, byte[] data)
	{
		_clanId = clanId;
		_crestId = crestId;
		_crestPart = crestPart;
		_totalSize = totalSize;
		_data = data;
	}

	@Override
	protected final void writeImpl()
	{
        writeD(Config.REQUEST_ID);
        writeD(_clanId);
        writeD(_crestId);
        writeD(_crestPart);
        writeD(_totalSize);
        writeD(_data.length);
		writeB(_data);
	}
}
