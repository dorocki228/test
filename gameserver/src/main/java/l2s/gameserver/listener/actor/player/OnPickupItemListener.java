package l2s.gameserver.listener.actor.player;

import l2s.gameserver.listener.PlayerListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;

public interface OnPickupItemListener extends PlayerListener
{
	void onPickupItem(Player p0, ItemInstance p1);
}
