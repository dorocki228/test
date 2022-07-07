package l2s.gameserver.listener.actor.player;

import l2s.gameserver.listener.PlayerListener;
import l2s.gameserver.model.Player;

@FunctionalInterface
public interface OnLevelChangeListener extends PlayerListener
{
	void onLevelChange(Player player, int oldLvl, int newLvl);
}
