package l2s.gameserver.model.items;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.PetInstance;
import l2s.gameserver.network.l2.s2c.PetInventoryUpdatePacket;
import l2s.gameserver.utils.ItemFunctions;

import java.util.Collection;

public class PetInventory extends Inventory
{
	private final PetInstance _actor;

	public PetInventory(PetInstance actor)
	{
		super(actor.getPlayer().getObjectId());
		_actor = actor;
	}

	@Override
	public PetInstance getActor()
	{
		return _actor;
	}

	public Player getOwner()
	{
		return _actor.getPlayer();
	}

	@Override
	protected ItemInstance.ItemLocation getBaseLocation()
	{
		return ItemInstance.ItemLocation.PET_INVENTORY;
	}

	@Override
	protected ItemInstance.ItemLocation getEquipLocation()
	{
		return ItemInstance.ItemLocation.PET_PAPERDOLL;
	}

	@Override
	protected void onRefreshWeight()
	{
		getActor().sendPetInfo();
	}

	@Override
	public void sendAddItem(ItemInstance item)
	{
		getOwner().sendPacket(new PetInventoryUpdatePacket().addNewItem(item));
	}

	@Override
	public void sendModifyItem(ItemInstance... items)
	{
		PetInventoryUpdatePacket piu = new PetInventoryUpdatePacket();
		for(ItemInstance item : items)
			piu.addModifiedItem(item);
		getOwner().sendPacket(piu);
	}

	@Override
	public void sendRemoveItem(ItemInstance item)
	{
		getOwner().sendPacket(new PetInventoryUpdatePacket().addRemovedItem(item));
	}

	@Override
	public void restore()
	{
		int ownerId = getOwnerId();
		writeLock();
		try
		{
			Collection<ItemInstance> items = ItemContainer._itemsDAO.getItemsByOwnerIdAndLoc(ownerId, getBaseLocation());
			for(ItemInstance item : items)
			{
				_items.add(item);
				onRestoreItem(item);
			}
			items = ItemContainer._itemsDAO.getItemsByOwnerIdAndLoc(ownerId, getEquipLocation());
			for(ItemInstance item : items)
			{
				_items.add(item);
				onRestoreItem(item);
				if(ItemFunctions.checkIfCanEquip(getActor(), item) == null)
					setPaperdollItem(item.getEquipSlot(), item);
			}
		}
		finally
		{
			writeUnlock();
		}
		refreshWeight();
	}

	@Override
	public void store()
	{
		writeLock();
		try
		{
			ItemContainer._itemsDAO.update(_items);
		}
		finally
		{
			writeUnlock();
		}
	}

	public void validateItems()
	{
		for(ItemInstance item : _paperdoll)
			if(item != null && (ItemFunctions.checkIfCanEquip(getActor(), item) != null || !item.getTemplate().testCondition(getActor(), item, false)))
				unEquipItem(item);
	}
}