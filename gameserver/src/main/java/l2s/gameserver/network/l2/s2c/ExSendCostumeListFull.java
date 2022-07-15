package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 24.05.2019
 * Developed for L2-Scripts.com
 **/
public class ExSendCostumeListFull implements IClientOutgoingPacket {
	private final Player player;

	public ExSendCostumeListFull(Player player) {
		this.player = player;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter) {
		OutgoingExPackets.EX_SEND_COSTUME_LIST_FULL.writeId(packetWriter);

		/*"d" //CostumeListSize
		if (CostumeListSize <= 0) {
			"d" //CostumeShortCutSize
			"d" //CostumeCollectID
			"d" //CollectReuseCooltime
			for (CostumeShortCutSize) {
				"d" //Page
				"d" //SlotIndex
				"d" //?????
			}
		} else {
			for (CostumeListSize) {
				"d" //CostumeID
				"Q" //Amount
				"c" //LockState
				"c" //ChangedType
			}
		}*/

		return true;
	}
}