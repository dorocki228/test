package l2s.gameserver.model.pledge;

public class ClanChangeLeaderRequest
{
	private final int _clanId;
	private final int _newLeaderId;
	private final long _time;

	public ClanChangeLeaderRequest(int clanId, int newLeaderId, long time)
	{
		_clanId = clanId;
		_newLeaderId = newLeaderId;
		_time = time;
	}

	public int getClanId()
	{
		return _clanId;
	}

	public int getNewLeaderId()
	{
		return _newLeaderId;
	}

	public long getTime()
	{
		return _time;
	}
}
