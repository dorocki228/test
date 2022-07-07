package l2s.gameserver.model.items.attachment;

import l2s.gameserver.model.Player;

public interface PickableAttachment extends ItemAttachment
{
	boolean canPickUp(Player p0);

	void pickUp(Player p0);
}
