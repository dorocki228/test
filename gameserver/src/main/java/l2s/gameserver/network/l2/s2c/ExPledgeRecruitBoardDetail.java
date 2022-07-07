package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.clansearch.ClanSearchClan;

public class ExPledgeRecruitBoardDetail extends L2GameServerPacket
{
	private final ClanSearchClan _clan;

	public ExPledgeRecruitBoardDetail(ClanSearchClan clan)
	{
		_clan = clan;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_clan.getClanId());
		writeD(_clan.getSearchType().ordinal());
		writeS("");
		writeS(_clan.getDesc());
		writeD(_clan.getRequestType().ordinal());
		writeD(0);
	}
}
