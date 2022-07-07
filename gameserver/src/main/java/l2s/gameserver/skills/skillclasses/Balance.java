package l2s.gameserver.skills.skillclasses;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.templates.StatsSet;

import java.util.List;

public class Balance extends Skill
{
	public Balance(StatsSet set)
	{
		super(set);
	}

	@Override
	public void onEndCast(Creature activeChar, List<Creature> targets)
	{
		super.onEndCast(activeChar, targets);
		double summaryCurrentHp = 0.0;
		int summaryMaximumHp = 0;
		for(Creature target : targets)
		{
			if(target == null)
				continue;
			if(target.isAlikeDead())
				continue;
			summaryCurrentHp += target.getCurrentHp();
			summaryMaximumHp += target.getMaxHp();
		}
		double percent = summaryCurrentHp / summaryMaximumHp;
		for(Creature target2 : targets)
		{
			if(target2 == null)
				continue;
			if(target2.isAlikeDead())
				continue;
			double hp = target2.getMaxHp() * percent;
			if(hp > target2.getCurrentHp())
			{
				double limit = target2.calcStat(Stats.HP_LIMIT, null, null) * target2.getMaxHp() / 100.0;
				if(target2.getCurrentHp() >= limit)
					continue;
				target2.setCurrentHp(Math.min(hp, limit), false);
			}
			else
				target2.setCurrentHp(Math.max(1.01, hp), false);
		}
	}
}
