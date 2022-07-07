package l2s.gameserver.model.items;

import l2s.commons.math.SafeMath;
import l2s.gameserver.dao.ItemsDAO;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.utils.ItemFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class ItemContainer
{
	private static final Logger _log = LoggerFactory.getLogger(ItemContainer.class);

    protected static final ItemsDAO _itemsDAO = ItemsDAO.getInstance();
	protected final List<ItemInstance> _items;
	protected final ReadWriteLock lock;
	protected final Lock readLock;
	protected final Lock writeLock;

	protected ItemContainer()
	{
		_items = new ArrayList<>();
		lock = new ReentrantReadWriteLock();
		readLock = lock.readLock();
		writeLock = lock.writeLock();
	}

	public int getSize()
	{
		return _items.size();
	}

	public ItemInstance[] getItems()
	{
		readLock();
		try
		{
			return _items.toArray(new ItemInstance[0]);
		}
		finally
		{
			readUnlock();
		}
	}

	public void clear()
	{
		writeLock();
		try
		{
			_items.clear();
		}
		finally
		{
			writeUnlock();
		}
	}

	public final void writeLock()
	{
		writeLock.lock();
	}

	public final void writeUnlock()
	{
		writeLock.unlock();
	}

	public final void readLock()
	{
		readLock.lock();
	}

	public final void readUnlock()
	{
		readLock.unlock();
	}

	public ItemInstance getItemByObjectId(int objectId)
	{
		readLock();
		try
		{
			for(int i = 0; i < _items.size(); ++i)
			{
				ItemInstance item = _items.get(i);
				if(item.getObjectId() == objectId)
					return item;
			}
		}
		finally
		{
			readUnlock();
		}
		return null;
	}

	public ItemInstance getItemByItemId(int itemId)
	{
		readLock();
		try
		{
			for(int i = 0; i < _items.size(); ++i)
			{
				ItemInstance item = _items.get(i);
				if(item.getItemId() == itemId)
					return item;
			}
		}
		finally
		{
			readUnlock();
		}
		return null;
	}

	public List<ItemInstance> getItemsByItemId(int itemId)
	{
        readLock();
        List<ItemInstance> result = new ArrayList<>();
        try
		{
			for(int i = 0; i < _items.size(); ++i)
			{
				ItemInstance item = _items.get(i);
				if(item.getItemId() == itemId)
					result.add(item);
			}
		}
		finally
		{
			readUnlock();
		}
		return result;
	}

	public long getCountOf(int itemId)
	{
        readLock();
        long count = 0L;
        try
		{
			for(int i = 0; i < _items.size(); ++i)
			{
				ItemInstance item = _items.get(i);
				if(item.getItemId() == itemId)
					count = SafeMath.addAndLimit(count, item.getCount());
			}
		}
		finally
		{
			readUnlock();
		}
		return count;
	}

	public ItemInstance addItem(int itemId, long count, int enchantLevel)
	{
		if(count < 1L)
			return null;

		writeLock();

		ItemInstance item;
		try
		{
			item = getItemByItemId(itemId);

			if(item != null && item.isStackable())
				synchronized (item)
				{
					item.setCount(SafeMath.addAndLimit(item.getCount(), count));
					onModifyItem(item);
				}
			else
			{
				item = ItemFunctions.createItem(itemId);
				item.setCount(count);
				item.setEnchantLevel(enchantLevel);

				_items.add(item);
				onAddItem(item);
			}
		}
		finally
		{
			writeUnlock();
		}

		return item;
	}

	public ItemInstance addItem(int itemId, long count)
	{
		return addItem(itemId, count, 0);
	}

	public ItemInstance addItem(ItemInstance item)
	{
		if(item == null)
			return null;
		if(item.getCount() < 1L)
			return null;
        writeLock();
        ItemInstance result = null;
        try
		{
			if(getItemByObjectId(item.getObjectId()) != null)
				return null;
			if(item.isStackable())
			{
				int itemId = item.getItemId();
				result = getItemByItemId(itemId);
				if(result != null)
					synchronized (result)
					{
						result.setCount(SafeMath.addAndLimit(item.getCount(), result.getCount()));
						onModifyItem(result);
						onDestroyItem(item);
					}
			}
			if(result == null)
			{
				_items.add(item);
				result = item;
				onAddItem(result);
			}
		}
		finally
		{
			writeUnlock();
		}
		return result;
	}

	public ItemInstance removeItemByObjectId(int objectId, long count)
	{
		if(count < 1L)
			return null;
		writeLock();
		ItemInstance result;
		try
		{
			ItemInstance item;
			if((item = getItemByObjectId(objectId)) == null)
				return null;
			synchronized (item)
			{
				result = removeItem(item, count);
			}
		}
		finally
		{
			writeUnlock();
		}
		return result;
	}

	public ItemInstance removeItemByItemId(int itemId, long count)
	{
		if(count < 1L)
			return null;
		writeLock();
		ItemInstance result;
		try
		{
			ItemInstance item;
			if((item = getItemByItemId(itemId)) == null)
				return null;
			synchronized (item)
			{
				result = removeItem(item, count);
			}
		}
		finally
		{
			writeUnlock();
		}
		return result;
	}

	public ItemInstance removeItem(ItemInstance item, long count)
	{
		if(item == null)
			return null;
		if(count < 1L)
			return null;
		if(item.getCount() < count)
			return null;
		writeLock();
		try
		{
			if(!_items.contains(item))
				return null;
			if(item.getCount() > count)
			{
				item.setCount(item.getCount() - count);
				onModifyItem(item);
				ItemInstance newItem = new ItemInstance(IdFactory.getInstance().getNextId(), item.getItemId());
				newItem.setCount(count);
				return newItem;
			}
			return removeItem(item);
		}
		finally
		{
			writeUnlock();
		}
	}

	public ItemInstance removeItem(ItemInstance item)
	{
		if(item == null)
			return null;
		writeLock();
		try
		{
			if(!_items.remove(item))
				return null;
			onRemoveItem(item);
			return item;
		}
		finally
		{
			writeUnlock();
		}
	}

	public boolean destroyItemByObjectId(int objectId, long count)
	{
		writeLock();
		try
		{
			ItemInstance item;
			if((item = getItemByObjectId(objectId)) == null)
				return false;
			synchronized (item)
			{
				return destroyItem(item, count);
			}
		}
		finally
		{
			writeUnlock();
		}
	}

	public boolean destroyItemByItemId(int itemId, long count)
	{
		writeLock();
		try
		{
			ItemInstance item;
			if((item = getItemByItemId(itemId)) == null)
				return false;
			synchronized (item)
			{
				return destroyItem(item, count);
			}
		}
		finally
		{
			writeUnlock();
		}
	}

	public boolean destroyItem(ItemInstance item, long count)
	{
		if(item == null)
			return false;
		if(count < 1L)
			return false;
		if(item.getCount() < count)
			return false;
		writeLock();
		try
		{
			if(!_items.contains(item))
				return false;
			if(item.getCount() > count)
			{
				item.setCount(item.getCount() - count);
				onModifyItem(item);
				return true;
			}
			return destroyItem(item);
		}
		finally
		{
			writeUnlock();
		}
	}

	public boolean destroyItem(ItemInstance item)
	{
		if(item == null)
			return false;
		writeLock();
		try
		{
			if(!_items.remove(item))
				return false;
			onRemoveItem(item);
			onDestroyItem(item);
			return true;
		}
		finally
		{
			writeUnlock();
		}
	}

	protected abstract void onAddItem(ItemInstance p0);

	protected abstract void onModifyItem(ItemInstance p0);

	protected abstract void onRemoveItem(ItemInstance p0);

	protected abstract void onDestroyItem(ItemInstance p0);

	public long getAdena()
	{
		ItemInstance _adena = getItemByItemId(57);
		if(_adena == null)
			return 0L;
		return _adena.getCount();
	}
}
