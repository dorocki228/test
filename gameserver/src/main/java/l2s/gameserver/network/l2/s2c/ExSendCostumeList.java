package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.Costume;
import l2s.gameserver.model.actor.instances.player.CostumeList;
import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 24.05.2019
 * Developed for L2-Scripts.com
 **/
public class ExSendCostumeList implements IClientOutgoingPacket {
	private final Player player;
	private final CostumeList costumeList;

	public ExSendCostumeList(Player player) {
		this.player = player;
		costumeList = player.getCostumeList();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter) {
		OutgoingExPackets.EX_SEND_COSTUME_LIST.writeId(packetWriter);
		packetWriter.writeD(costumeList.size()); //CostumeListSize
		for (Costume costume : costumeList) {
			packetWriter.writeD(costume.getId()); //CostumeID
			packetWriter.writeQ(costume.getCount()); //Amount
			packetWriter.writeC(costume.isFlag(Costume.IS_LOCKED)); //LockState 0 - Unlocked, 1 - Locked
			packetWriter.writeC(costume.isFlag(Costume.IS_NEW)); //ChangedType 0 - Normal, 1 - New,
		}

		return true;
	}
}