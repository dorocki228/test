package l2s.gameserver.listener.event;

import l2s.gameserver.listener.EventListener;
import l2s.gameserver.model.entity.events.Event;

public interface OnStartStopListener extends EventListener
{
	void onStart(Event p0);

	void onStop(Event p0);
}
