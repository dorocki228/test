package l2s.gameserver.model.items.listeners;

import l2s.gameserver.listener.inventory.OnEquipListener;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.stats.funcs.Func;

public final class StatsListener implements OnEquipListener
{
	private static final StatsListener _instance = new StatsListener();

	public static StatsListener getInstance()
	{
		return _instance;
	}

	@Override
	public int onEquip(int slot, ItemInstance item, Playable actor)
	{
		return Inventory.UPDATE_STATS_FLAG;
	}

	@Override
	public int onUnequip(int slot, ItemInstance item, Playable actor)
	{
		return Inventory.UPDATE_STATS_FLAG;
	}

	@Override
	public int onRefreshEquip(ItemInstance item, Playable actor)
	{
		return Inventory.UPDATE_STATS_FLAG;
	}
}