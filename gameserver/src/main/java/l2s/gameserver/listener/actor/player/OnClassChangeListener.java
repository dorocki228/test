package l2s.gameserver.listener.actor.player;

import l2s.gameserver.listener.PlayerListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.ClassId;

public interface OnClassChangeListener extends PlayerListener
{
	void onClassChange(Player p0, ClassId p1, ClassId p2, boolean onRestore);
}
