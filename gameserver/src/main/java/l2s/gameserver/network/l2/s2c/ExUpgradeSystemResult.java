package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExUpgradeSystemResult implements IClientOutgoingPacket {
	private final int result;
	private final int objectId;

	public ExUpgradeSystemResult(int result, int objectId) {
		this.result = result;
		this.objectId = objectId;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter) {
		OutgoingExPackets.EX_UPGRADE_SYSTEM_RESULT.writeId(packetWriter);
		packetWriter.writeH(result);
		packetWriter.writeD(objectId);

		return true;
	}
}