package l2s.gameserver.listener.actor;

import l2s.gameserver.listener.CharListener;
import l2s.gameserver.model.Creature;

public interface OnCreatureAttackAvoidListener extends CharListener
{
	void onCreatureAttackAvoid(Creature attacker, Creature target, boolean isDot);
}
