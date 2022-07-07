package l2s.gameserver.skills.skillclasses;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.templates.StatsSet;

import java.util.List;

public class DrainSoul extends Skill
{
	public DrainSoul(StatsSet set)
	{
		super(set);
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(!super.checkCondition(activeChar, target, forceUse, dontMove, first))
			return false;
		if(!target.isMonster())
		{
			activeChar.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
			return false;
		}
		return true;
	}

	@Override
	public void onEndCast(Creature activeChar, List<Creature> targets)
	{
		if(!activeChar.isPlayer())
			return;
		super.onEndCast(activeChar, targets);
	}
}
