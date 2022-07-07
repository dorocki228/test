package l2s.gameserver.model.entity.events.actions;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.EventAction;

public class GiveItemAction implements EventAction
{
	private final int _itemId;
	private final long _count;

	public GiveItemAction(int itemId, long count)
	{
		_itemId = itemId;
		_count = count;
	}

	@Override
	public void call(Event event)
	{
		for(Player player : event.itemObtainPlayers())
			event.giveItem(player, _itemId, _count);
	}
}
