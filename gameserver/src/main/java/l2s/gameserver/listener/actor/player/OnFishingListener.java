package l2s.gameserver.listener.actor.player;

import l2s.gameserver.listener.PlayerListener;
import l2s.gameserver.model.Player;

import java.util.OptionalInt;

public interface OnFishingListener extends PlayerListener
{
	void onFishing(Player player, OptionalInt fish);
}
