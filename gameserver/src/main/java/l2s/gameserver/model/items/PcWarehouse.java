package l2s.gameserver.model.items;

import l2s.gameserver.model.Player;

public class PcWarehouse extends Warehouse
{
	public PcWarehouse(Player owner)
	{
		super(owner.getObjectId());
	}

	public PcWarehouse(int ownerId)
	{
		super(ownerId);
	}

	@Override
	public ItemInstance.ItemLocation getItemLocation()
	{
		return ItemInstance.ItemLocation.WAREHOUSE;
	}
}
