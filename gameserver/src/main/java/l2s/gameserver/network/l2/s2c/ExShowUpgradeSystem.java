package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author VISTALL
 */
public class ExShowUpgradeSystem implements IClientOutgoingPacket {
	private final int unk;

	public ExShowUpgradeSystem(int unk) {
		this.unk = unk;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter) {
		OutgoingExPackets.EX_SHOW_UPGRADE_SYSTEM.writeId(packetWriter);
		packetWriter.writeH(unk);    // unk, maybe type
		packetWriter.writeH(0x00);    // unk

		return true;
	}
}