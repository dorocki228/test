package l2s.gameserver.templates.item;

import l2s.gameserver.handler.items.IItemHandler;

public interface ItemType
{
	long mask();

	IItemHandler getHandler();

	ExItemType getExType();
}
