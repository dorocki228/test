package l2s.gameserver.model.entity.events.actions;

import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.EventAction;
import l2s.gameserver.network.l2.components.SystemMsg;

public class AnnounceAction implements EventAction
{
	private final int _id;
	private final SystemMsg _msgId;

	public AnnounceAction(int _id, SystemMsg _msgId) {
		this._id = _id;
		this._msgId = _msgId;
	}

	@Override
	public void call(Event event)
	{
		event.announce(_id, _msgId);
	}
}
