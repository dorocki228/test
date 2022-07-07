package l2s.gameserver.templates.dailymissions;

import l2s.commons.time.cron.SchedulingPattern;
import l2s.gameserver.handler.dailymissions.DailyMissionHandlerHolder;
import l2s.gameserver.handler.dailymissions.IDailyMissionHandler;

import java.util.ArrayList;
import java.util.List;

public class DailyMissionTemplate implements Comparable<DailyMissionTemplate>
{
	private final int _id;
	private final IDailyMissionHandler _handler;
	private final String _value;
	private final int repetitionCount;
	private final SchedulingPattern reusePattern;
	private final boolean checkHwid;
	private final List<DailyRewardTemplate> _rewards;
	private final boolean partyShared;

	public DailyMissionTemplate(int id, String handler, String value, int repetitionCount,
								SchedulingPattern reusePattern, boolean checkHwid, boolean partyShared)
	{
		_rewards = new ArrayList<>();
		_id = id;
		_handler = DailyMissionHandlerHolder.getInstance().getHandler(handler);
		_value = value;
		this.repetitionCount = repetitionCount;
		this.reusePattern = reusePattern;
		this.checkHwid = checkHwid;
		this.partyShared = partyShared;
	}

	public int getId()
	{
		return _id;
	}

	public IDailyMissionHandler getHandler()
	{
		return _handler;
	}

	public String getValue()
	{
		return _value;
	}

	public int getRepetitionCount() {
		return repetitionCount;
	}

	public SchedulingPattern getReusePattern()
	{
		return reusePattern;
	}

	public boolean isCheckHwid()
	{
		return checkHwid;
	}

	public void addReward(DailyRewardTemplate reward)
	{
		_rewards.add(reward);
	}

	public DailyRewardTemplate[] getRewards()
	{
		return _rewards.toArray(new DailyRewardTemplate[0]);
	}

	@Override
	public int compareTo(DailyMissionTemplate o)
	{
		return getId() - o.getId();
	}

	public boolean isPartyShared() {
		return partyShared;
	}
}
