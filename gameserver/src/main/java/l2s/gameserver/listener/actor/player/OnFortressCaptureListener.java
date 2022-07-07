package l2s.gameserver.listener.actor.player;

import l2s.gameserver.listener.CharListener;
import l2s.gameserver.model.Player;

public interface OnFortressCaptureListener extends CharListener
{
	void onFortressCapture(Player player);
}
