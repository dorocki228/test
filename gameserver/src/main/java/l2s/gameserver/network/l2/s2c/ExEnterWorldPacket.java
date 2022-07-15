package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.Config;
import l2s.gameserver.network.l2.OutgoingExPackets;

import java.time.Instant;
import java.time.ZonedDateTime;

/**
 * @author Java-man
 **/
public class ExEnterWorldPacket implements IClientOutgoingPacket {
    private final int nanosecondsSinceEpoch;
    private final int offset;
    private final int daylightSeconds;
    private final int teleportFreeLevel;

    public ExEnterWorldPacket() {
        nanosecondsSinceEpoch = Math.toIntExact(Instant.now().getEpochSecond());
		offset = ZonedDateTime.now().getOffset().getTotalSeconds();
		daylightSeconds = 0; // almost always 0
		teleportFreeLevel = Config.TELEPORT_FREE_UNTIL_LEVEL;
    }

    @Override
    public boolean write(l2s.commons.network.PacketWriter packetWriter) {
        OutgoingExPackets.EX_ENTER_WORLD.writeId(packetWriter);
        packetWriter.writeD(nanosecondsSinceEpoch);
        packetWriter.writeD(offset);
        packetWriter.writeD(daylightSeconds);
        packetWriter.writeD(teleportFreeLevel);

        return true;
    }
}