package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;

public class ConditionPlayerState extends Condition
{
	private final CheckPlayerState _check;
	private final boolean _required;

	public ConditionPlayerState(CheckPlayerState check, boolean required)
	{
		_check = check;
		_required = required;
	}

	@Override
	protected boolean testImpl(Creature creature, Creature target, Skill skill, ItemInstance item, double value)
	{
		switch(_check)
		{
			case RESTING:
			{
				if(creature.isPlayer())
					return creature.isSitting() == _required;
				return !_required;
			}
			case MOVING:
			{
				return creature.isMoving() == _required;
			}
			case RUNNING:
			{
				return (creature.isMoving() && creature.isRunning()) == _required;
			}
			case STANDING:
			{
				if(creature.isPlayer())
					return creature.isSitting() != _required && creature.isMoving() != _required;
				return creature.isMoving() != _required;
			}
			case FLYING:
			{
				if(creature.isPlayer())
					return creature.isFlying() == _required;
				return !_required;
			}
			case FLYING_TRANSFORM:
			{
				if(creature.isPlayer())
					return creature.isInFlyingTransform() == _required;
				return !_required;
			}
			default:
			{
				return !_required;
			}
		}
	}

	public enum CheckPlayerState
	{
		RESTING,
		MOVING,
		RUNNING,
		STANDING,
		FLYING,
		FLYING_TRANSFORM
    }
}
