package l2s.gameserver.model.entity.olympiad;

import l2s.gameserver.Config;

public enum CompType
{
	TEAM(2, 0, 0, 0),
	NON_CLASSED(2, Config.OLYMPIAD_NONCLASSED_WINNER_REWARD_COUNT, Config.OLYMPIAD_NONCLASSED_LOOSER_REWARD_COUNT, 5),
	CLASSED(2, Config.OLYMPIAD_CLASSED_WINNER_REWARD_COUNT, Config.OLYMPIAD_CLASSED_LOOSER_REWARD_COUNT, 3);

	private final int _minSize;
	private final int _winnerReward;
	private final int _looserReward;
	private final int _looseMult;

	CompType(int minSize, int winnerReward, int looserReward, int looseMult)
	{
        _minSize = minSize;
        _winnerReward = winnerReward;
		_looserReward = looserReward;
		_looseMult = looseMult;
	}

    public int getMinSize()
    {
        return _minSize;
    }

	public int getWinnerReward()
	{
		return _winnerReward;
	}

	public int getLooserReward()
	{
		return _looserReward;
	}

	public int getLooseMult()
	{
		return _looseMult;
	}
}
