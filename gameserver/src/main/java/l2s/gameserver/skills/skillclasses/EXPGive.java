package l2s.gameserver.skills.skillclasses;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.templates.StatsSet;

public class EXPGive extends Skill
{
	private final long _power;

	public EXPGive(StatsSet set)
	{
		super(set);
		_power = 5078544041L;
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		return super.checkCondition(activeChar, target, forceUse, dontMove, first) && activeChar.isPlayer();
	}

	@Override
	protected void useSkill(Creature activeChar, Creature target, boolean reflected)
	{
		if(!target.isPlayer())
			return;
		target.getPlayer().addExpAndSp(_power, 0L);
	}
}
