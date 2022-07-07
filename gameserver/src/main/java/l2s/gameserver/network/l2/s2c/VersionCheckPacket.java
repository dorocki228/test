package l2s.gameserver.network.l2.s2c;

public class VersionCheckPacket extends L2GameServerPacket
{
	private final byte[] _key;

	public VersionCheckPacket(byte[] key)
	{
		_key = key;
	}

	@Override
	public void writeImpl()
	{
		if(_key == null || _key.length == 0)
		{
			writeC(0x00);
			return;
		}

		writeC(0x01);

		for(int i = 0; i < 8; ++i)
			writeC(_key[i]);

		writeD(0x01);
		writeD(0x00);
		writeC(0x00);
		writeD(0x00);
		writeC(0x01);
		writeC(0x00);
	}
}
