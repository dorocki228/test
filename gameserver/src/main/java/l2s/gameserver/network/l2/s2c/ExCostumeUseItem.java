package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 24.05.2019
 * Developed for L2-Scripts.com
 **/
public class ExCostumeUseItem implements IClientOutgoingPacket {
	private final boolean success;
	private final int costumeId;

	public ExCostumeUseItem(boolean success, int costumeId) {
		this.success = success;
		this.costumeId = costumeId;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter) {
		OutgoingExPackets.EX_COSTUME_USE_ITEM.writeId(packetWriter);
		packetWriter.writeC(success);    //IsSuccess
		packetWriter.writeD(costumeId);  //CostumeId

		return true;
	}
}