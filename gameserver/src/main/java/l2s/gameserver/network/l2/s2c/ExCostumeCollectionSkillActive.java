package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 24.05.2019
 * Developed for L2-Scripts.com
 **/
public class ExCostumeCollectionSkillActive implements IClientOutgoingPacket {
	public ExCostumeCollectionSkillActive() {
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter) {
		OutgoingExPackets.EX_COSTUME_COLLECTION_SKILL_ACTIVE.writeId(packetWriter);
		/*"d" //CostumeCollectID
		"d" //CostumeCollectReuseCooltime*/

		return true;
	}
}