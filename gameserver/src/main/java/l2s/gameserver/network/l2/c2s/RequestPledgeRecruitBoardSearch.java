package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.clansearch.ClanSearchParams;
import l2s.gameserver.model.clansearch.base.ClanSearchClanSortType;
import l2s.gameserver.model.clansearch.base.ClanSearchListType;
import l2s.gameserver.model.clansearch.base.ClanSearchSortOrder;
import l2s.gameserver.model.clansearch.base.ClanSearchTargetType;
import l2s.gameserver.network.l2.s2c.ExPledgeRecruitBoardSearch;

public class RequestPledgeRecruitBoardSearch extends L2GameClientPacket
{
	private ClanSearchParams _params;

	@Override
	protected void readImpl()
	{
		int clanLevel = readD();
		ClanSearchListType csListType = ClanSearchListType.getType(readD());
		ClanSearchTargetType csTargetType = ClanSearchTargetType.valueOf(readD());
		String targetName = readS();
		ClanSearchClanSortType csClanShortType = ClanSearchClanSortType.valueOf(readD());
		ClanSearchSortOrder csOrderType = ClanSearchSortOrder.valueOf(readD());
		int page = readD();
		_params = new ClanSearchParams(clanLevel, csListType, csTargetType, targetName, csClanShortType, csOrderType, page);
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		activeChar.sendPacket(new ExPledgeRecruitBoardSearch(_params));
	}
}
