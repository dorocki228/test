package l2s.gameserver.listener.actor;

import l2s.gameserver.listener.CharListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.skills.SkillEntry;

public interface OnMagicUseListener extends CharListener
{
	void onMagicUse(Creature actor, SkillEntry skillEntry, Creature target, boolean alt);
}
