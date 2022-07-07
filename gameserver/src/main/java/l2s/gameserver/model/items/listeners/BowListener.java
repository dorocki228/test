package l2s.gameserver.model.items.listeners;

import l2s.gameserver.listener.inventory.OnEquipListener;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.templates.item.WeaponTemplate;

public final class BowListener implements OnEquipListener
{
	private static final BowListener _instance;

	public static BowListener getInstance()
	{
		return _instance;
	}

	@Override
	public void onUnequip(int slot, ItemInstance item, Playable actor)
	{}

	@Override
	public void onEquip(int slot, ItemInstance item, Playable actor)
	{
		if(!item.isEquipable() || slot != 7)
			return;
		Player player = (Player) actor;
		if(item.getItemType() == WeaponTemplate.WeaponType.BOW)
		{
			ItemInstance arrow = player.getInventory().findArrowForBow(item.getTemplate());
			if(arrow != null)
				player.getInventory().setPaperdollItem(8, arrow);
		}
	}

	static
	{
		_instance = new BowListener();
	}
}
