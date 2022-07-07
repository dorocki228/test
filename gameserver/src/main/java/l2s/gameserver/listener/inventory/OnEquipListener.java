package l2s.gameserver.listener.inventory;

import l2s.commons.listener.Listener;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.items.ItemInstance;

public interface OnEquipListener extends Listener<Playable>
{
	void onEquip(int p0, ItemInstance p1, Playable p2);

	void onUnequip(int p0, ItemInstance p1, Playable p2);
}
