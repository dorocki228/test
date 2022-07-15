package smartguard.packet;

import l2s.gameserver.Config;
import l2s.gameserver.network.l2.OutgoingPackets;
import l2s.gameserver.network.l2.s2c.IClientOutgoingPacket;
import smartguard.spi.SmartGuardSPI;

public class VersionCheckPacket implements IClientOutgoingPacket
{
    private byte[] _key;

    public VersionCheckPacket(final byte[] key)
    {
        _key = key;

        if(_key != null)
            SmartGuardSPI.getSmartGuardService().getLicenseManager().cryptInternalData(_key);
    }

    @Override
    public boolean write(l2s.commons.network.PacketWriter packetWriter)
    {
        OutgoingPackets.VERSION_CHECK.writeId(packetWriter);

        if(_key == null || _key.length == 0)
        {
            packetWriter.writeC(0x00);
            return true;
        }

        packetWriter.writeC(0x01);

        for(int i = 0; i < 8; i++)
            packetWriter.writeC(_key[i]);

        packetWriter.writeD(0x01);
        packetWriter.writeD(Config.REQUEST_ID);	// Server ID
        packetWriter.writeC(0x01);
        packetWriter.writeD(0x00); // Seed (obfuscation key)
        packetWriter.writeC(0x01);	// Classic
        packetWriter.writeC(0x00);	// Arena
        packetWriter.writeC(0x00); // Unk

        return true;
    }
}