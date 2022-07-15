/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2s.gameserver.network.l2.s2c;

import l2s.commons.network.PacketWriter;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInfo;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.LockType;
import l2s.gameserver.model.items.PcInventory;
import l2s.gameserver.network.l2.s2c.updatetype.ItemListType;
import l2s.gameserver.skills.TimeStamp;
import l2s.gameserver.templates.item.support.Ensoul;

/**
 * @author UnAfraid
 */
public abstract class AbstractItemPacket extends AbstractMaskPacket<ItemListType>
{
	private static final byte[] MASKS =
	{
		0x00
	};
	
	@Override
	protected byte[] getMasks()
	{
		return MASKS;
	}

	@Override
	protected void onNewMaskAdded(ItemListType component)
	{

	}
	
	/*protected void writeItem(PacketWriter packet, TradeItem item, long count)
	{
		writeItem(packet, new ItemInfo(item), count);
	}
	
	protected void writeItem(PacketWriter packet, TradeItem item)
	{
		writeItem(packet, new ItemInfo(item));
	}
	
	protected void writeItem(PacketWriter packet, WarehouseItem item)
	{
		writeItem(packet, new ItemInfo(item));
	}*/
	
	protected void writeItem(PacketWriter packet, Player player, ItemInstance item)
	{
		writeItem(packet, new ItemInfo(player, item));
	}
	
	/*protected void writeItem(PacketWriter packet, Product item)
	{
		writeItem(packet, new ItemInfo(item));
	}*/
	
	protected void writeItem(PacketWriter packetWriter, ItemInfo item)
	{
		writeItem(packetWriter, item, item.getCount());
	}
	
	protected void writeItem(PacketWriter packetWriter, ItemInfo item, long count)
	{
		final int mask = calculateMask(item);
		packetWriter.writeC(mask);
		packetWriter.writeD(item.getObjectId()); // ObjectId
		packetWriter.writeD(item.getItemId()); // ItemId
		packetWriter.writeC(item.getItem().isQuest() || item.isEquipped() ? 0xFF : item.getEquipSlot()); // T1
		packetWriter.writeQ(count); // Quantity
		packetWriter.writeC(item.getItem().getType2()); // Item Type 2 : 00-weapon, 01-shield/armor, 02-ring/earring/necklace, 03-questitem, 04-adena, 05-item
		packetWriter.writeC(item.getCustomType1()); // Filler (always 0)
		packetWriter.writeH(item.isEquipped() ? 1 : 0); // Equipped : 00-No, 01-yes
		packetWriter.writeQ(item.getItem().getBodyPart()); // Slot : 0006-lr.ear, 0008-neck, 0030-lr.finger, 0040-head, 0100-l.hand, 0200-gloves, 0400-chest, 0800-pants, 1000-feet, 4000-r.hand, 8000-r.hand
		packetWriter.writeC(item.getEnchantLevel()); // Enchant level (pet level shown in control item)
		packetWriter.writeC(item.getCustomType2());
		packetWriter.writeD(item.getShadowLifeTime());
		packetWriter.writeD(item.getTemporalLifeTime());
		packetWriter.writeC(!item.isBlocked() ? 1 : 0); // GOD Item enabled = 1 disabled (red) = 0
		packetWriter.writeC(0x00); // 140 protocol
		packetWriter.writeC(0x00); // 140 protocol

		if (containsMask(mask, ItemListType.AUGMENT_BONUS))
		{
			writeItemAugment(packetWriter, item);
		}

		if (containsMask(mask, ItemListType.ELEMENTAL_ATTRIBUTE))
		{
			writeItemElements(packetWriter, item);
		}

		if (containsMask(mask, ItemListType.ENCHANT_EFFECT))
		{
			writeItemEnchantEffect(packetWriter, item);
		}

		if (containsMask(mask, ItemListType.VISUAL_ID))
		{
			packetWriter.writeD(item.getVisualId()); // Item remodel visual ID
		}

		if (containsMask(mask, ItemListType.SOUL_CRYSTAL))
		{
			writeItemEnsoulOptions(packetWriter, item);
		}

		if (containsMask(mask, ItemListType.REUSE_DELAY))
		{
			packetWriter.writeD(item.getReuseDelay()); // reuse delay
		}
	}
	
	protected static int calculateMask(ItemInfo item)
	{
		int mask = 0;

		if (item.getVariation1Id() > 0 || item.getVariation2Id() > 0)
		{
			mask |= ItemListType.AUGMENT_BONUS.getMask();
		}

		if(item.getAttackElementValue() > 0 || item.getDefenceFire() > 0
				|| item.getDefenceWater() > 0 || item.getDefenceWind() > 0
				|| item.getDefenceEarth() > 0 || item.getDefenceHoly() > 0
				|| item.getDefenceUnholy() > 0) {
			mask |= ItemListType.ELEMENTAL_ATTRIBUTE.getMask();
		}

		
		if (item.getEnchantOptions() != null)
		{
			for (int id : item.getEnchantOptions())
			{
				if (id > 0)
				{
					mask |= ItemListType.ENCHANT_EFFECT.getMask();
					break;
				}
			}
		}
		
		if (item.getVisualId() > 0)
		{
			mask |= ItemListType.VISUAL_ID.getMask();
		}

		Ensoul[] normalEnsouls = item.getNormalEnsouls();
		Ensoul[] specialEnsouls = item.getSpecialEnsouls();
		if(normalEnsouls.length > 0 || specialEnsouls.length > 0) {
			mask |= ItemListType.SOUL_CRYSTAL.getMask();
		}

		if(item.getReuseDelay() > 0)
			mask |= ItemListType.REUSE_DELAY.getMask();

		return mask;
	}
	
	protected void writeItemAugment(PacketWriter packetWriter, ItemInfo item)
	{
		if (item != null && (item.getVariation1Id() > 0 || item.getVariation2Id() > 0))
		{
			packetWriter.writeD(item.getVariation1Id());
			packetWriter.writeD(item.getVariation2Id());
		}
		else
		{
			packetWriter.writeD(0);
			packetWriter.writeD(0);
		}
	}
	
	protected void writeItemElementsAndEnchant(PacketWriter packetWriter, ItemInfo item)
	{
		writeItemElements(packetWriter, item);
		writeItemEnchantEffect(packetWriter, item);
	}

	protected void writeItemElements(PacketWriter packetWriter, ItemInfo item)
	{
		if (item != null) {
			packetWriter.writeH(item.getAttackElement()); // attack element (-1 - none)
			packetWriter.writeH(item.getAttackElementValue()); // attack element value
			packetWriter.writeH(item.getDefenceFire()); // огненная стихия (fire pdef)
			packetWriter.writeH(item.getDefenceWater()); // водная стихия (water pdef)
			packetWriter.writeH(item.getDefenceWind()); // воздушная стихия (wind pdef)
			packetWriter.writeH(item.getDefenceEarth()); // земляная стихия (earth pdef)
			packetWriter.writeH(item.getDefenceHoly()); // светлая стихия (holy pdef)
			packetWriter.writeH(item.getDefenceUnholy()); // темная стихия (dark pdef)
		} else {
			packetWriter.writeH(-1); // attack element (-1 - none)
			packetWriter.writeH(0x00); // attack element value
			packetWriter.writeH(0x00); // огненная стихия (fire pdef)
			packetWriter.writeH(0x00); // водная стихия (water pdef)
			packetWriter.writeH(0x00); // воздушная стихия (wind pdef)
			packetWriter.writeH(0x00); // земляная стихия (earth pdef)
			packetWriter.writeH(0x00); // светлая стихия (holy pdef)
			packetWriter.writeH(0x00); // темная стихия (dark pdef)
		}
	}
	
	protected void writeItemEnchantEffect(PacketWriter packetWriter, ItemInfo item)
	{
		// Enchant Effects
		for (int op : item.getEnchantOptions())
		{
			packetWriter.writeD(op);
		}
	}
	
	protected void writeItemEnsoulOptions(PacketWriter packetWriter, ItemInfo item)
	{
		if (item != null)
		{
			Ensoul[] normalEnsouls = item.getNormalEnsouls();
			Ensoul[] specialEnsouls = item.getSpecialEnsouls();

			packetWriter.writeC(normalEnsouls.length); // Size of regular soul crystal options.
			for(Ensoul ensoul : normalEnsouls)
				packetWriter.writeD(ensoul.getId()); // Regular Soul Crystal Ability ID.

			packetWriter.writeC(specialEnsouls.length); // Size of special soul crystal options.
			for(Ensoul ensoul : specialEnsouls)
				packetWriter.writeD(ensoul.getId()); // Special Soul Crystal Ability ID.
		}
		else
		{
			packetWriter.writeC(0); // Size of regular soul crystal options.
			packetWriter.writeC(0); // Size of special soul crystal options.
		}
	}
	
	protected void writeInventoryBlock(PacketWriter packetWriter, PcInventory inventory)
	{
		if (inventory.hasInventoryBlock()) {
			final LockType lockType = inventory.getLockType();
			final int[] lockItems = inventory.getLockItems();

			packetWriter.writeH((short) lockItems.length);
			packetWriter.writeC((byte) lockType.getClientId());
			for (int id : lockItems) {
				packetWriter.writeD(id);
			}
		} else {
			packetWriter.writeH((short) 0x00);
		}
	}
}
