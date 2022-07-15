package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
**/
public class ExItemAnnounce implements IClientOutgoingPacket
{
	private final String name;
	private final int itemId;
	private final int enchantLevel;

	public ExItemAnnounce(Player player, ItemInstance item)
	{
		name = player.getName();
		itemId = item.getItemId();
		enchantLevel = item.getEnchantLevel();
	}

	public ExItemAnnounce(String name, int itemId, int enchantLevel)
	{
		this.name = name;
		this.itemId = itemId;
		this.enchantLevel = enchantLevel;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_ITEM_ANNOUNCE.writeId(packetWriter);
		packetWriter.writeC(0x00);	// Unk. Maybe fail == 1, success == 0?
		packetWriter.writeSizedString(name);
		packetWriter.writeD(itemId);
		packetWriter.writeC(enchantLevel);
		packetWriter.writeD(0x00);	// UNK

		return true;
	}
}