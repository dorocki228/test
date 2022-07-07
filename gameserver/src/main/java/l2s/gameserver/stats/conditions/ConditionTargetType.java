package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;

public class ConditionTargetType extends Condition
{
	private final TargetType _targetType;

	public ConditionTargetType(String targetType)
	{
		_targetType = TargetType.valueOf(targetType.toUpperCase());
	}

	@Override
	protected boolean testImpl(Creature creature, Creature target, Skill skill, ItemInstance item, double value)
	{
		switch(_targetType)
		{
			case PLAYABLE:
			{
				return target != null && target.isPlayable();
			}
			case PLAYER:
			{
				return target != null && target.isPlayer();
			}
			case NPC:
			{
				return target != null && target.isNpc();
			}
			case MONSTER:
			{
				return target != null && target.isMonster();
			}
			case RAID_BOSS:
			{
				return target != null && target.isRaid();
			}
			case SERVITOR:
			{
				return target != null && target.isServitor();
			}
			case SUMMON:
			{
				return target != null && target.isSummon();
			}
			case PET:
			{
				return target != null && target.isPet();
			}
			default:
			{
				return true;
			}
		}
	}

	private enum TargetType
	{
		NPC,
		MONSTER,
		RAID_BOSS,
		PLAYABLE,
		PLAYER,
		SERVITOR,
		SUMMON,
		PET
    }
}
