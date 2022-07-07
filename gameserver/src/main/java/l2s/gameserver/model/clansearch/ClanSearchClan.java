package l2s.gameserver.model.clansearch;

import l2s.gameserver.model.clansearch.base.ClanSearchListType;
import l2s.gameserver.model.clansearch.base.ClanSearchRequestType;

public class ClanSearchClan
{
	private final int _clanId;
	private ClanSearchListType _searchType;
	private ClanSearchRequestType _requestType;
	private String _desc;

	public ClanSearchClan(int clanId, ClanSearchListType searchType, ClanSearchRequestType requestType, String desc)
	{
		_clanId = clanId;
		_searchType = searchType;
		_requestType = requestType;
		_desc = desc;
	}

	public int getClanId()
	{
		return _clanId;
	}

	public ClanSearchRequestType getRequestType()
	{
		return _requestType;
	}

	public void setRequestType(ClanSearchRequestType requestType)
	{
		_requestType = requestType;
	}

	public ClanSearchListType getSearchType()
	{
		return _searchType;
	}

	public void setSearchType(ClanSearchListType searchType)
	{
		_searchType = searchType;
	}

	public String getDesc()
	{
		return _desc;
	}

	public void setDesc(String desc)
	{
		_desc = desc;
	}
}
