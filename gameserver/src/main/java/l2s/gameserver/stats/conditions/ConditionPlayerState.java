package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConditionPlayerState extends Condition
{
	public enum CheckPlayerState
	{
		RESTING,
		MOVING,
		RUNNING,
		STANDING,
		FLYING,
		FLYING_TRANSFORM
	}

	private final CheckPlayerState _check;

	private final boolean _required;

	public ConditionPlayerState(CheckPlayerState check, boolean required)
	{
		_check = check;
		_required = required;
	}

	@Override
	protected boolean testImpl(@NotNull Creature actor, @Nullable Creature target, @Nullable Skill skill, @Nullable ItemInstance item, double value)
	{
		switch(_check)
		{
			case RESTING:
				if(actor.isPlayer())
					return actor.isSitting() == _required;
				return !_required;
			case MOVING:
				return actor.getMovement().isMoving() == _required;
			case RUNNING:
				return (actor.getMovement().isMoving() && actor.isRunning()) == _required;
			case STANDING:
				if(actor.isPlayer())
					return actor.isSitting() != _required && actor.getMovement().isMoving() != _required;
				return actor.getMovement().isMoving() != _required;
			case FLYING:
				if(actor.isPlayer())
					return actor.isFlying() == _required;
				return !_required;
			case FLYING_TRANSFORM:
				if(actor.isPlayer())
					return actor.isInFlyingTransform() == _required;
				return !_required;
		}
		return !_required;
	}
}
