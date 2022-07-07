package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.PcInventory;
import l2s.gameserver.network.l2.s2c.updatetype.InventorySlot;

public class ExUserInfoEquipSlot extends AbstractMaskPacket<InventorySlot>
{
	private final Player _player;
	private final byte[] _masks;

	@Override
	protected byte[] getMasks()
	{
		return _masks;
	}

	@Override
	protected void onNewMaskAdded(InventorySlot component)
	{}

	public ExUserInfoEquipSlot(Player player)
	{
		_masks = new byte[] { 0, 0, 0, 0, 0 };
		_player = player;
		addComponentType(InventorySlot.VALUES);
	}

	public ExUserInfoEquipSlot(Player player, int slot)
	{
		_masks = new byte[] { 0, 0, 0, 0, 0 };
		_player = player;
		InventorySlot inventorySlot = InventorySlot.valueOf(slot);
		if(inventorySlot != null)
		{
			switch(inventorySlot)
			{
				case HAIR:
				case HAIR2:
				{
					addComponentType(InventorySlot.HAIR);
					addComponentType(InventorySlot.HAIR2);
					break;
				}
				default:
				{
					addComponentType(inventorySlot);
				}
			}
		}
		else
			addComponentType(InventorySlot.valueOf(slot));
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_player.getObjectId());
		writeH(InventorySlot.VALUES.length);
		writeB(_masks);
		PcInventory inventory = _player.getInventory();
		for(InventorySlot slot : InventorySlot.VALUES)
			if(containsMask(slot))
			{
				writeH(22);
				writeD(inventory.getPaperdollObjectId(slot.getSlot()));
				writeD(inventory.getPaperdollItemId(slot.getSlot()));
				int[] augmentations = inventory.getPaperdollItemAugmentationId(slot.getSlot());
				writeD(augmentations[0]);
				writeD(augmentations[1]);
				writeD(inventory.getPaperdollItemVisualId(slot.getSlot()));
			}
	}
}
