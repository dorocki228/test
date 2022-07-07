package l2s.gameserver.listener.actor.player;

import l2s.gameserver.listener.PlayerListener;
import l2s.gameserver.model.Player;

public interface OnQuestFinishListener extends PlayerListener
{
	void onQuestFinish(Player p0, int p1);
}
