package l2s.gameserver.model.entity.events.impl.arena.action;

import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.EventAction;
import l2s.gameserver.model.entity.events.impl.ArenaEvent;

/**
 * @author mangol
 */
public class EventScreenCustomMessage implements EventAction {
	private final String customMessage;
	private final int time;

	public EventScreenCustomMessage(String customMessage, int time) {
		this.customMessage = customMessage;
		this.time = time;
	}

	@Override
	public void call(Event p0) {
		ArenaEvent event = (ArenaEvent) p0;
		event.sendScreenCustomMessage(customMessage, time);
	}
}
