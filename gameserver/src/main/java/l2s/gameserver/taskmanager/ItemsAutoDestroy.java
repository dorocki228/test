package l2s.gameserver.taskmanager;

import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.items.ItemInstance;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ItemsAutoDestroy
{
	private static ItemsAutoDestroy _instance;
	private ConcurrentLinkedQueue<ItemInstance> _items;
	private ConcurrentLinkedQueue<ItemInstance> _playersItems;
	private ConcurrentLinkedQueue<ItemInstance> _herbs;

	private ItemsAutoDestroy()
	{
		_items = null;
		_playersItems = null;
		_herbs = null;
		if(Config.AUTODESTROY_ITEM_AFTER > 0)
		{
			_items = new ConcurrentLinkedQueue<>();
			ThreadPoolManager.getInstance().scheduleAtFixedRate(new CheckItemsForDestroy(), 60000L, 60000L);
		}
		if(Config.AUTODESTROY_PLAYER_ITEM_AFTER > 0)
		{
			_playersItems = new ConcurrentLinkedQueue<>();
			ThreadPoolManager.getInstance().scheduleAtFixedRate(new CheckPlayersItemsForDestroy(), 60000L, 60000L);
		}
		_herbs = new ConcurrentLinkedQueue<>();
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new CheckHerbsForDestroy(), 1000L, 1000L);
	}

	public static ItemsAutoDestroy getInstance()
	{
		if(_instance == null)
			_instance = new ItemsAutoDestroy();
		return _instance;
	}

	public void addItem(ItemInstance item)
	{
		item.setDropTime(System.currentTimeMillis());
		_items.add(item);
	}

	public void addPlayerItem(ItemInstance item)
	{
		item.setDropTime(System.currentTimeMillis());
		_playersItems.add(item);
	}

	public void addHerb(ItemInstance herb)
	{
		herb.setDropTime(System.currentTimeMillis());
		_herbs.add(herb);
	}

	public class CheckItemsForDestroy implements Runnable
	{
		@Override
		public void run()
		{
			long _sleep = Config.AUTODESTROY_ITEM_AFTER * 1000L;
			long curtime = System.currentTimeMillis();
			for(ItemInstance item : _items)
				if(item == null || item.getLastDropTime() == 0L || item.getLocation() != ItemInstance.ItemLocation.VOID)
					_items.remove(item);
				else
				{
					if(item.getLastDropTime() + _sleep >= curtime)
						continue;
					item.deleteMe();
					_items.remove(item);
				}
		}
	}

	public class CheckPlayersItemsForDestroy implements Runnable
	{
		@Override
		public void run()
		{
			long _sleep = Config.AUTODESTROY_PLAYER_ITEM_AFTER * 1000L;
			long curtime = System.currentTimeMillis();
			for(ItemInstance item : _playersItems)
				if(item == null || item.getLastDropTime() == 0L || item.getLocation() != ItemInstance.ItemLocation.VOID)
					_playersItems.remove(item);
				else
				{
					if(item.getLastDropTime() + _sleep >= curtime)
						continue;
					item.deleteMe();
					_playersItems.remove(item);
				}
		}
	}

	public class CheckHerbsForDestroy implements Runnable
	{
		static final long _sleep = 60000L;

		@Override
		public void run()
		{
			long curtime = System.currentTimeMillis();
			for(ItemInstance item : _herbs)
				if(item == null || item.getLastDropTime() == 0L || item.getLocation() != ItemInstance.ItemLocation.VOID)
					_herbs.remove(item);
				else
				{
					if(item.getLastDropTime() + 60000L >= curtime)
						continue;
					item.deleteMe();
					_herbs.remove(item);
				}
		}
	}
}
