package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.PcInventory;
import l2s.gameserver.network.l2.OutgoingExPackets;
import l2s.gameserver.network.l2.s2c.updatetype.InventorySlot;

/**
 * @author Sdw
 * @reworked by Bonux
**/
public class ExUserInfoEquipSlot extends AbstractMaskPacket<InventorySlot>
{
	private final Player _player;

	private final byte[] _masks = new byte[]
	{
		(byte) 0x00,
		(byte) 0x00,
		(byte) 0x00,
		(byte) 0x00,
		(byte) 0x00,
		(byte) 0x00,
		(byte) 0x00,
		(byte) 0x00
	};
	
	@Override
	protected byte[] getMasks()
	{
		return _masks;
	}
	
	@Override
	protected void onNewMaskAdded(InventorySlot component)
	{
		//
	}

	public ExUserInfoEquipSlot(Player player)
	{
		_player = player;
		addComponentType(InventorySlot.VALUES);
	}

	public ExUserInfoEquipSlot(Player player, int slot)
	{
		_player = player;
		addComponentType(InventorySlot.valueOf(slot));
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_USER_INFO_EQUIPSLOT.writeId(packetWriter);
		packetWriter.writeD(_player.getObjectId());
		packetWriter.writeH(InventorySlot.VALUES.length);
		packetWriter.writeB(_masks);

		final PcInventory inventory = _player.getInventory();
		for(InventorySlot slot : InventorySlot.VALUES)
		{
			if(containsMask(slot))
			{
				packetWriter.writeH(22); // size
				packetWriter.writeD(inventory.getPaperdollObjectId(slot.getSlot()));
				packetWriter.writeD(inventory.getPaperdollItemId(slot.getSlot()));
				packetWriter.writeD(inventory.getPaperdollVariation1Id(slot.getSlot()));
				packetWriter.writeD(inventory.getPaperdollVariation2Id(slot.getSlot()));
				packetWriter.writeD(inventory.getPaperdollVisualId(slot.getSlot()));
			}
		}

		return true;
	}
}