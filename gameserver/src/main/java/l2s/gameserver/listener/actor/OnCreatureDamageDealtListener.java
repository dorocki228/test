package l2s.gameserver.listener.actor;

import l2s.gameserver.listener.CharListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;

public interface OnCreatureDamageDealtListener extends CharListener
{
	void onCreatureDamageDealt(Creature attacker, Creature target, double damage, Skill skill, boolean crit, boolean damageOverTime, boolean reflect);
}
