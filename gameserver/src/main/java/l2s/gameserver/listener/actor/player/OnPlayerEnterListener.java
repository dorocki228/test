package l2s.gameserver.listener.actor.player;

import l2s.gameserver.listener.PlayerListener;
import l2s.gameserver.model.Player;

@FunctionalInterface
public interface OnPlayerEnterListener extends PlayerListener
{
	void onPlayerEnter(Player p0);
}
