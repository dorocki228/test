package l2s.gameserver.model.reward;

public enum RewardType
{
	RATED_GROUPED,
	RATED_NOT_GROUPED,
	NOT_RATED_NOT_GROUPED,
	NOT_RATED_GROUPED,
	SWEEP,
	EVENT_GROUPED;

	public static final RewardType[] VALUES = values();
}
