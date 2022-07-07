package l2s.gameserver.listener.actor.player;

import l2s.gameserver.listener.PlayerListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.SubClass;
import l2s.gameserver.model.base.ClassId;

public interface OnActiveClassListener extends PlayerListener
{
	void onActiveClass(Player player, ClassId classId, SubClass newActiveSub, boolean onRestore);
}
