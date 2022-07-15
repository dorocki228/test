package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.CostumeList;
import l2s.gameserver.network.l2.OutgoingExPackets;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 24.05.2019
 * Developed for L2-Scripts.com
 **/
public class ExCostumeShortcutList implements IClientOutgoingPacket {
	private final boolean add;
	private final Map<Integer, Integer> shortcuts;

	public ExCostumeShortcutList(Player player) {
		add = true;
		shortcuts = player.getCostumeList().getShortcuts();
	}

	public ExCostumeShortcutList(int page, int slot, int costumeId) {
		add = false;
		shortcuts = new HashMap<>(1);
		shortcuts.put(CostumeList.getShortCutId(page, slot), costumeId);
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter) {
		OutgoingExPackets.EX_COSTUME_SHORTCUT_LIST.writeId(packetWriter);
		packetWriter.writeC(add ? 0x01 : 0x02); //Result (1 - add, 2 - remove)
		packetWriter.writeD(shortcuts.size());   //ListSize
		for (Map.Entry<Integer, Integer> entry : shortcuts.entrySet()) {
			packetWriter.writeD(CostumeList.getPageId(entry.getKey()));  //Page
			packetWriter.writeD(CostumeList.getSlotId(entry.getKey()));  //SlotIndex
			packetWriter.writeD(entry.getValue());   // CostumeId
		}

		return true;
	}
}