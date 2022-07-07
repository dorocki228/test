package l2s.gameserver.listener.zone;

import l2s.commons.listener.Listener;
import l2s.gameserver.model.Zone;

public interface OnZoneTickListener extends Listener<Zone>
{
	void onTick(Zone zone);
}
