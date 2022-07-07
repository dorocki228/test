package l2s.gameserver.listener.actor.player;

import l2s.gameserver.listener.PlayerListener;
import l2s.gameserver.model.Player;

public interface OnPlayerPartyLeaveListener extends PlayerListener
{
	void onPartyLeave(Player p0);
}
