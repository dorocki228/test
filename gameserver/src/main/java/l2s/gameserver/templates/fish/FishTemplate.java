package l2s.gameserver.templates.fish;

public final class FishTemplate
{
	private final int _id;
	private final double _chance;
	private final int _rewardType;

	public FishTemplate(int id, double chance, int rewardType)
	{
		_id = id;
		_chance = chance;
		_rewardType = rewardType;
	}

	public int getId()
	{
		return _id;
	}

	public double getChance()
	{
		return _chance;
	}

	public int getRewardType()
	{
		return _rewardType;
	}
}
