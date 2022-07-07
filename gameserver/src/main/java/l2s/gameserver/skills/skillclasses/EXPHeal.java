package l2s.gameserver.skills.skillclasses;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.templates.StatsSet;

public class EXPHeal extends Skill
{
	private final int _percentPower;
	private final int _percentPowerMaxLvl;

	public EXPHeal(StatsSet set)
	{
		super(set);
		_percentPower = set.getInteger("percent_power", 0);
		_percentPowerMaxLvl = set.getInteger("percent_power_max_lvl", 0);
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
		Player player = target.getPlayer();
		long power = (long) getPower();
		if(_percentPowerMaxLvl != 0 && player.getLevel() < _percentPowerMaxLvl)
			power = (long) (player.getExp() / 100.0 * _percentPower);
		player.addExpAndSp(power, 0L);
	}
}
