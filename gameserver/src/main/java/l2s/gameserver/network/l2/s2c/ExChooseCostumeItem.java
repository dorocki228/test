package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 24.05.2019
 * Developed for L2-Scripts.com
 **/
public class ExChooseCostumeItem implements IClientOutgoingPacket {
	private final int itemId;

	public ExChooseCostumeItem(int itemId) {
		this.itemId = itemId;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter) {
		OutgoingExPackets.EX_CHOOSE_COSTUME_ITEM.writeId(packetWriter);
		packetWriter.writeD(itemId); //ItemClassID*/

		return true;
	}
}