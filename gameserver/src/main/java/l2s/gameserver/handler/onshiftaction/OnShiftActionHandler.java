package l2s.gameserver.handler.onshiftaction;

import l2s.gameserver.model.Player;

public interface OnShiftActionHandler<T>
{
	boolean call(T p0, Player p1);
}
