package l2s.gameserver.listener.actor;

import l2s.gameserver.listener.CharListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;

public interface OnCreatureSkillFinishCastListener extends CharListener
{
	void onCreatureSkillFinishCast(Creature caster, Creature target, Skill skill);
}
