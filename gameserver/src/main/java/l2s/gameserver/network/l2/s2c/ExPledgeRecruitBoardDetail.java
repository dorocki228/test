package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.clansearch.ClanSearchClan;
import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author GodWorld
 * @reworked by Bonux
**/
public class ExPledgeRecruitBoardDetail implements IClientOutgoingPacket
{
	private final ClanSearchClan _clan;

	public ExPledgeRecruitBoardDetail(ClanSearchClan clan)
	{
		_clan = clan;
	}

	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_PLEDGE_RECRUIT_BOARD_DETAIL.writeId(packetWriter);
		packetWriter.writeD(_clan.getClanId());
		packetWriter.writeD(_clan.getSearchType().ordinal());
		packetWriter.writeS(""); // Title (deprecated)
		packetWriter.writeS(_clan.getDesc());
		packetWriter.writeD(_clan.getApplication()); // Application
		packetWriter.writeD(_clan.getSubUnit()); // Sub Unit Type

		return true;
	}
}