package l2s.gameserver.model.items.listeners;

import l2s.gameserver.listener.inventory.OnEquipListener;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;

public class GveRewardItemListener implements OnEquipListener
{

	@Override
	public void onEquip(int p0, ItemInstance p1, Playable p)
	{
		Player player = p.getPlayer();
		player.calcItemReward();
		player.calcEnchantReward();
		player.calcSetReward();
	}

	@Override
	public void onUnequip(int p0, ItemInstance p1, Playable p)
	{
		Player player = p.getPlayer();
		player.calcItemReward();
		player.calcEnchantReward();
		player.calcSetReward();
	}

	private static final GveRewardItemListener _instance = new GveRewardItemListener();

	public static GveRewardItemListener getInstance()
	{
		return _instance;
	}

}
