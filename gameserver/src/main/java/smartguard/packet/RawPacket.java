package smartguard.packet;


import l2s.gameserver.network.l2.s2c.IClientOutgoingPacket;

import java.nio.ByteBuffer;

public class RawPacket implements IClientOutgoingPacket
{
    private byte[] data;

    public RawPacket(ByteBuffer byteBuffer)
    {
        data = new byte[byteBuffer.remaining()];
        byteBuffer.get(data, 0, data.length);
    }

    @Override
    public boolean write(l2s.commons.network.PacketWriter packetWriter)
    {
        packetWriter.writeB(data);
        return true;
    }
}
