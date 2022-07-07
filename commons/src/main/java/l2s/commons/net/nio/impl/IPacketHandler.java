package l2s.commons.net.nio.impl;

import java.nio.ByteBuffer;

public interface IPacketHandler<T extends MMOClient>
{
	ReceivablePacket<T> handlePacket(ByteBuffer p0, T p1);
}
