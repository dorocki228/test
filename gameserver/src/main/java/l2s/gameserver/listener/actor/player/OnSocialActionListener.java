package l2s.gameserver.listener.actor.player;

import l2s.gameserver.listener.PlayerListener;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.c2s.RequestActionUse;

public interface OnSocialActionListener extends PlayerListener
{
	void onSocialAction(Player p0, GameObject p1, RequestActionUse.Action p2);
}
