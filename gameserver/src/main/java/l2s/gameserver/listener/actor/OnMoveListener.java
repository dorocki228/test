package l2s.gameserver.listener.actor;

import l2s.gameserver.listener.CharListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.utils.Location;

public interface OnMoveListener extends CharListener
{
	void onMove(Creature p0, Location p1);
}
