package l2s.gameserver.listener.actor;

import l2s.gameserver.listener.PlayerListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.utils.Location;

public interface OnChangeLocationListener extends PlayerListener {
	void location(Creature actor, Location location, int reflectionId);
}
