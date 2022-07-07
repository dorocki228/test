package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.instancemanager.clansearch.ClanSearchManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.clansearch.ClanSearchPlayer;
import l2s.gameserver.model.clansearch.base.ClanSearchListType;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExPledgeRecruitApplyInfo;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;

public class RequestPledgeWaitingApply extends L2GameClientPacket
{
	private boolean _enter;
	private int _clanId;
	private String _desc;

	@Override
	protected void readImpl()
	{
		_enter = readD() == 1;
		_clanId = readD();
		_desc = readS();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(activeChar.getClan() != null)
		{
			activeChar.sendPacket(SystemMsg.ONLY_THE_CLAN_LEADER_OR_SOMEONE_WITH_RANK_MANAGEMENT_AUTHORITY_MAY_REGISTER_THE_CLAN);
			return;
		}
		ClanSearchPlayer csPlayer = new ClanSearchPlayer(activeChar.getObjectId(), activeChar.getName(), activeChar.getLevel(), activeChar.getBaseClassId(), _clanId, ClanSearchListType.SLT_ANY, _desc);
		if(_enter)
		{
			if(ClanSearchManager.getInstance().addPlayer(csPlayer))
				activeChar.sendPacket(ExPledgeRecruitApplyInfo.WAITING);
		}
		else
		{
			ClanSearchManager.getInstance().removeApplicant(_clanId, activeChar.getObjectId());
			activeChar.sendPacket(new SystemMessagePacket(SystemMsg.YOU_MAY_APPLY_FOR_ENTRY_AFTER_S1_MINUTES_DUE_TO_CANCELLING_YOUR_APPLICATION).addNumber(5));
		}
	}
}
