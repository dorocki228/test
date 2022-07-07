package l2s.gameserver.listener.actor;

import l2s.gameserver.listener.CharListener;
import l2s.gameserver.model.Creature;

public interface OnDeathFromUndyingListener extends CharListener
{
	void onDeathFromUndying(Creature p0, Creature p1);
}
