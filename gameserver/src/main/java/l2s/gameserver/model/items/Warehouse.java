package l2s.gameserver.model.items;

import l2s.commons.dao.JdbcEntityState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public abstract class Warehouse extends ItemContainer
{
	protected final int _ownerId;

	protected Warehouse(int ownerId)
	{
		_ownerId = ownerId;
	}

	public int getOwnerId()
	{
		return _ownerId;
	}

	public abstract ItemInstance.ItemLocation getItemLocation();

	@Override
	public ItemInstance[] getItems()
	{
        readLock();
        List<ItemInstance> result = new ArrayList<>();
        try
		{
            result.addAll(_items);
		}
		finally
		{
			readUnlock();
		}
		return result.toArray(new ItemInstance[0]);
	}

	public long getCountOfAdena()
	{
		return getCountOf(57);
	}

	@Override
	protected void onAddItem(ItemInstance item)
	{
		item.setOwnerId(getOwnerId());
		item.setLocation(getItemLocation());
		item.setLocData(0);
		if(item.getJdbcState().isSavable())
			item.save();
		else
		{
			item.setJdbcState(JdbcEntityState.UPDATED);
			item.update();
		}
	}

	@Override
	protected void onModifyItem(ItemInstance item)
	{
		item.setJdbcState(JdbcEntityState.UPDATED);
		item.update();
	}

	@Override
	protected void onRemoveItem(ItemInstance item)
	{
		item.setLocData(-1);
	}

	@Override
	protected void onDestroyItem(ItemInstance item)
	{
		item.setCount(0L);
		item.delete();
	}

	public void restore()
	{
		int ownerId = getOwnerId();
		writeLock();
		try
		{
			Collection<ItemInstance> items = ItemContainer._itemsDAO.getItemsByOwnerIdAndLoc(ownerId, getItemLocation());
            _items.addAll(items);
		}
		finally
		{
			writeUnlock();
		}
	}

	public enum WarehouseType
	{
		NONE,
		PRIVATE,
		CLAN,
		CASTLE,
		FREIGHT
    }

	public static class ItemClassComparator implements Comparator<ItemInstance>
	{
		private static final Comparator<ItemInstance> instance;

		public static final Comparator<ItemInstance> getInstance()
		{
			return instance;
		}

		@Override
		public int compare(ItemInstance o1, ItemInstance o2)
		{
			if(o1 == null || o2 == null)
				return 0;
			int diff = o1.getExType().mask() - o2.getExType().mask();
			if(diff == 0)
				diff = o1.getGrade().ordinal() - o2.getGrade().ordinal();
			if(diff == 0)
				diff = o1.getItemId() - o2.getItemId();
			if(diff == 0)
				diff = o1.getEnchantLevel() - o2.getEnchantLevel();
			return diff;
		}

		static
		{
			instance = new ItemClassComparator();
		}
	}
}
