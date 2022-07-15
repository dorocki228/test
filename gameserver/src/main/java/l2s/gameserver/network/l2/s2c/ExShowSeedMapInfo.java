package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * Probably the destination coordinates where you should fly your clan's airship.<BR>
 * Exactly at the specified coordinates an airship controller is spawned.<BR>
 * Sent while being in Gracia, when world map is opened, in response to RequestSeedPhase.<BR>
 * FE A1 00		- opcodes<BR>
 * 02 00 00 00	- list size<BR>
 * <BR>
 * B7 3B FC FF	- x<BR>
 * 38 D8 03 00	- y<BR>
 * EB 10 00 00	- z<BR>
 * D3 0A 00 00	- sysmsg id<BR>
 * <BR>
 * F6 BC FC FF	- x<BR>
 * 48 37 03 00	- y<BR>
 * 30 11 00 00	- z<BR>
 * CE 0A 00 00	- sysmsg id
 * 
 * @author n0nam3
 */
public class ExShowSeedMapInfo implements IClientOutgoingPacket
{
	public static final ExShowSeedMapInfo STATIC_PACKET = new ExShowSeedMapInfo();

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_SHOW_SEED_MAP_INFO.writeId(packetWriter);
		// empty in classic
		packetWriter.writeD(0); // seed count

		return true;
	}
}
