package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.instancemanager.clansearch.ClanSearchManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.clansearch.ClanSearchClan;
import l2s.gameserver.model.clansearch.base.ClanSearchListType;
import l2s.gameserver.model.clansearch.base.ClanSearchRequestType;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExPledgeRecruitApplyInfo;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;

public class RequestPledgeRecruitBoardAccess extends L2GameClientPacket
{
	private int _action;
	private ClanSearchListType _searchType;
	private String _desc;
	private ClanSearchRequestType _request;

	@Override
	protected void readImpl()
	{
		_action = readD();
		_searchType = ClanSearchListType.getType(readD());
		readS();
		_desc = readS();
		_request = ClanSearchRequestType.getType(readD());
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		Clan clan = activeChar.getClan();
		if(clan == null)
		{
			activeChar.sendPacket(SystemMsg.ONLY_THE_CLAN_LEADER_OR_SOMEONE_WITH_RANK_MANAGEMENT_AUTHORITY_MAY_REGISTER_THE_CLAN);
			return;
		}

		if((activeChar.getClanPrivileges() & 0x10) != 0x10)
		{
			activeChar.sendPacket(SystemMsg.ONLY_THE_CLAN_LEADER_OR_SOMEONE_WITH_RANK_MANAGEMENT_AUTHORITY_MAY_REGISTER_THE_CLAN);
			return;
		}

		if(_desc.length() > 256)
			_desc = _desc.substring(0, 255);

		ClanSearchClan csc = null;
		switch(_action)
		{
			case 0:
			{
				csc = ClanSearchManager.getInstance().getClan(clan.getClanId());

				if(csc != null && ClanSearchManager.getInstance().removeClan(csc))
				{
					activeChar.sendPacket(new SystemMessagePacket(SystemMsg.YOU_MAY_APPLY_FOR_ENTRY_AFTER_S1_MINUTES_DUE_TO_CANCELLING_YOUR_APPLICATION).addNumber(5));
					activeChar.sendPacket(ExPledgeRecruitApplyInfo.DEFAULT);
				}
				break;
			}
			case 1:
			case 2:
			{
				csc = new ClanSearchClan(clan.getClanId(), _searchType, _request, _desc);
				if(ClanSearchManager.getInstance().addClan(csc))
				{
					activeChar.sendPacket(SystemMsg.ENTRY_APPLICATION_COMPLETE_USE_ENTRY_APPLICATION_INFO_TO_CHECK_OR_CANCEL_YOUR_APPLICATION);
					activeChar.sendPacket(ExPledgeRecruitApplyInfo.ORDER_LIST);
				}
				break;
			}
		}
	}
}
