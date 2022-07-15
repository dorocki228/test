package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConditionTargetType extends Condition
{
	private enum TargetType
	{
		NPC,
		MONSTER,
		RAID_BOSS,
		PLAYABLE,
		PLAYER,
		SERVITOR,
		SUMMON,
		PET;
	}

	private final TargetType _targetType;

	public ConditionTargetType(String targetType)
	{
		_targetType = TargetType.valueOf(targetType.toUpperCase());
	}

	@Override
	protected boolean testImpl(@NotNull Creature actor, @Nullable Creature target, @Nullable Skill skill, @Nullable ItemInstance item, double value)
	{
		switch(_targetType)
		{
			case PLAYABLE:
				return target != null && target.isPlayable();
			case PLAYER:
				return target != null && target.isPlayer();
			case NPC:
				return target != null && target.isNpc();
			case MONSTER:
				return target != null && target.isMonster();
			case RAID_BOSS:
				return target != null && target.isRaid();
			case SERVITOR:
				return target != null && target.isServitor();
			case SUMMON:
				return target != null && target.isSummon();
			case PET:
				return target != null && target.isPet();
		}
		return true;
	}
}
