package l2s.gameserver.listener.actor;

import l2s.gameserver.listener.CharListener;
import l2s.gameserver.model.Creature;

public interface OnAttackHitListener extends CharListener
{
	void onAttackHit(Creature attacked, Creature attacker);
}
