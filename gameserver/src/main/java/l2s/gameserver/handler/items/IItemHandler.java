package l2s.gameserver.handler.items;

import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.utils.Location;

public interface IItemHandler
{
	boolean forceUseItem(Playable p0, ItemInstance p1, boolean p2);

	boolean useItem(Playable p0, ItemInstance p1, boolean p2);

	void dropItem(Player p0, ItemInstance p1, long p2, Location p3);

	boolean pickupItem(Playable p0, ItemInstance p1);

	void onRestoreItem(Playable p0, ItemInstance p1);

	void onAddItem(Playable p0, ItemInstance p1);

	void onRemoveItem(Playable p0, ItemInstance p1);

	boolean isAutoUse();
}
