package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.templates.skill.EffectTemplate;

public class i_get_exp extends i_abstract_effect
{
	private final long _power;
	private final int _percentPower;
	private final int _percentPowerMaxLvl;

	public i_get_exp(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
		_power = template.getParam().getLong("power");
		_percentPower = template.getParam().getInteger("percent_power", 0);
		_percentPowerMaxLvl = template.getParam().getInteger("percent_power_max_lvl", 0);
	}

	@Override
	public boolean checkCondition()
	{
		if(!getEffected().isPlayer())
			return false;
		return super.checkCondition();
	}

	@Override
	public void instantUse()
	{
		Player player = getEffected().getPlayer();
		long power = _power;
		if(_percentPowerMaxLvl != 0 && player.getLevel() < _percentPowerMaxLvl)
			power = (long) ((double) player.getExp() / 100.0 * (double) _percentPower);
		player.addExpAndSp(power, 0);
	}
}
