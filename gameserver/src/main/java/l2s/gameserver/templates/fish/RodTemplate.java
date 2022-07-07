package l2s.gameserver.templates.fish;

public final class RodTemplate
{
	private final int _id;
	private final double _durationModifier;
	private final double _rewardModifier;
	private final int _shotConsumeCount;
	private final int _refreshDelay;

	public RodTemplate(int id, double durationModifier, double rewardModifier, int shotConsumeCount, int refreshDelay)
	{
		_id = id;
		_durationModifier = durationModifier;
		_rewardModifier = rewardModifier;
		_shotConsumeCount = shotConsumeCount;
		_refreshDelay = refreshDelay;
	}

	public int getId()
	{
		return _id;
	}

	public double getDurationModifier()
	{
		return _durationModifier;
	}

	public double getRewardModifier()
	{
		return _rewardModifier;
	}

	public int getShotConsumeCount()
	{
		return _shotConsumeCount;
	}

	public long getRefreshDelay()
	{
		return _refreshDelay;
	}
}
