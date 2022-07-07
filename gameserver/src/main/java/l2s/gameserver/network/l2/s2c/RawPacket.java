package l2s.gameserver.network.l2.s2c;

import java.nio.ByteBuffer;

public class RawPacket extends L2GameServerPacket
{
	byte[] data;

	public RawPacket(ByteBuffer byteBuffer)
	{
        data = new byte[byteBuffer.position() + 1];
		byteBuffer.position(0);
		byteBuffer.get(data, 0, data.length);
	}

	protected void writeImpl()
	{
        writeB(data);
	}

	protected boolean writeOpcodes()
	{
		return false;
	}
}
