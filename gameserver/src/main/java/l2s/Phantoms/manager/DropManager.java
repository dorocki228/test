package  l2s.Phantoms.manager;

import java.util.ArrayList;
import java.util.List;

import  l2s.gameserver.model.items.ItemInstance;

public class DropManager
{
	private static DropManager _instance;

	private List<ItemInstance> phantomPickupItems = new ArrayList<ItemInstance>();

	public static DropManager getInstance()
	{
		if(_instance == null)
			_instance = new DropManager();
		return _instance;
	}

	public void pickupItem(ItemInstance item)
	{
		//phantomPickupItems.add(item);
	}

	public List<ItemInstance> getAllItems()
	{
		return phantomPickupItems;
	}

}
