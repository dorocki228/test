package l2s.gameserver.listener.actor;

import l2s.gameserver.listener.CharListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;

public interface OnCurrentHpDamageListener extends CharListener
{
	void onCurrentHpDamage(Creature p0, double p1, Creature p2, Skill p3, boolean sharedDamage);
}
