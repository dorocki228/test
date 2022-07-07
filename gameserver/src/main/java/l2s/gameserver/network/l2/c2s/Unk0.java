package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.instancemanager.clansearch.ClanSearchManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.clansearch.ClanSearchClan;
import l2s.gameserver.model.clansearch.base.ClanSearchRequestType;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.tables.ClanTable;

public class Unk0 extends L2GameClientPacket
{
	private int _clanId;

	@Override
	protected void readImpl()
	{
		_clanId = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		ClanSearchClan csc = ClanSearchManager.getInstance().getClan(_clanId);
		Clan clan = ClanTable.getInstance().getClan(_clanId);
		if(clan == null)
			return;
		if(!clan.checkJoinPledgeCondition(activeChar, 0))
			return;
		if(clan.getUnitMembersSize(0) >= clan.getSubPledgeLimit(0)) {
			activeChar.sendActionFailed();
			return;
		}
		if(csc != null && csc.getRequestType() == ClanSearchRequestType.ENTER_WITH_OUT_REQUEST)
		{
			ClanSearchManager.getInstance().removeWaiter(activeChar.getObjectId());
			ClanSearchManager.getInstance().removeApplicant(clan.getClanId(), activeChar.getObjectId());
			clan.joinInPledge(activeChar, 0);
		}
	}
}
