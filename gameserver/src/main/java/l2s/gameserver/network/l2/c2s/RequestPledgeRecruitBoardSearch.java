package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.clansearch.ClanSearchParams;
import l2s.gameserver.model.clansearch.base.ClanSearchClanSortType;
import l2s.gameserver.model.clansearch.base.ClanSearchListType;
import l2s.gameserver.model.clansearch.base.ClanSearchSortOrder;
import l2s.gameserver.model.clansearch.base.ClanSearchTargetType;
import l2s.gameserver.network.l2.s2c.ExPledgeRecruitBoardSearch;

/**
 * @author GodWorld
 * @reworked by Bonux
**/
public class RequestPledgeRecruitBoardSearch implements IClientIncomingPacket
{
	private ClanSearchParams _params;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_params = new ClanSearchParams(packet.readD(), ClanSearchListType.getType(packet.readD()), ClanSearchTargetType.valueOf(packet.readD()), packet.readS(), ClanSearchClanSortType.valueOf(packet.readD()), ClanSearchSortOrder.valueOf(packet.readD()), packet.readD(), packet.readD());
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;

		//if(!((L2GameClient)client).getFloodProtectors().getClanSearch().tryPerformAction(FloodAction.CLAN_BOARD_SEARCH))
			//return;

		activeChar.sendPacket(new ExPledgeRecruitBoardSearch(_params));
	}
}