package l2s.gameserver.listener.actor.player;

import l2s.gameserver.listener.PlayerListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Reflection;

public interface OnPlayerReflectionListener extends PlayerListener
{
	void onPlayerEnterReflection(Player player, Reflection reflection);

	void onPlayerExitReflection(Player player, Reflection reflection);
}
