package l2s.gameserver.skills.skillclasses;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.residences.SiegeFlagInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.templates.StatsSet;

public class Sacrifice extends Skill
{
	public Sacrifice(StatsSet set)
	{
		super(set);
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		return super.checkCondition(activeChar, target, forceUse, dontMove, first) && target != null && !target.isDoor() && !(target instanceof SiegeFlagInstance);
	}

	@Override
	protected void useSkill(Creature activeChar, Creature target, boolean reflected)
	{
		if(target.isHealBlocked())
			return;
		double addToHp = Math.max(0.0, Math.min(getPower(), target.calcStat(Stats.HP_LIMIT, null, null) * target.getMaxHp() / 100.0 - target.getCurrentHp()));
		if(addToHp > 0.0)
		{
			target.setCurrentHp(addToHp + target.getCurrentHp(), false);
			if(getId() == 4051)
				target.sendPacket(SystemMsg.REJUVENATING_HP);
			else if(target.isPlayer())
				if(activeChar == target)
					activeChar.sendPacket(new SystemMessagePacket(SystemMsg.S1_HP_HAS_BEEN_RESTORED).addNumber(Math.round(addToHp)));
				else
					target.sendPacket(new SystemMessagePacket(SystemMsg.S2_HP_HAS_BEEN_RESTORED_BY_C1).addName(activeChar).addNumber(Math.round(addToHp)));
		}
	}
}
