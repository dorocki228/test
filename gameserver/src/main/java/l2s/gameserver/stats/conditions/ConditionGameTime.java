package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.time.GameTimeService;

public class ConditionGameTime extends Condition
{
	private final CheckGameTime _check;
	private final boolean _required;

	public ConditionGameTime(CheckGameTime check, boolean required)
	{
		_check = check;
		_required = required;
	}

	@Override
	protected boolean testImpl(Creature creature, Creature target, Skill skill, ItemInstance item, double value)
	{
		switch(_check)
		{
			case NIGHT:
			{
				return GameTimeService.INSTANCE.isNowNight() == _required;
			}
			default:
			{
				return !_required;
			}
		}
	}

	public enum CheckGameTime
	{
		NIGHT
    }
}
