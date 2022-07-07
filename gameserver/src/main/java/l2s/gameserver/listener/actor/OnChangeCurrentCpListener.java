package l2s.gameserver.listener.actor;

import l2s.gameserver.listener.CharListener;
import l2s.gameserver.model.Creature;

public interface OnChangeCurrentCpListener extends CharListener
{
	void onChangeCurrentCp(Creature p0, double p1, double p2);
}
