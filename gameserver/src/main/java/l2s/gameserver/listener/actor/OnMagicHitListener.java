package l2s.gameserver.listener.actor;

import l2s.gameserver.listener.CharListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;

public interface OnMagicHitListener extends CharListener
{
	void onMagicHit(Creature p0, Skill p1, Creature p2);
}
