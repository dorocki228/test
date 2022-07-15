package l2s.gameserver.network.l2.s2c;

import java.util.List;

import l2s.gameserver.instancemanager.clansearch.ClanSearchManager;
import l2s.gameserver.model.clansearch.ClanSearchClan;
import l2s.gameserver.model.clansearch.ClanSearchParams;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.OutgoingExPackets;
import l2s.gameserver.tables.ClanTable;

/**
 * @author GodWorld
 * @reworked by Bonux
**/
public class ExPledgeRecruitBoardSearch implements IClientOutgoingPacket
{
	private static final int PAGINATION_LIMIT = 12;

	private final ClanSearchParams _params;
	private final List<ClanSearchClan> _clans;

	public ExPledgeRecruitBoardSearch(ClanSearchParams params)
	{
		_params = params;
		_clans = ClanSearchManager.getInstance().listClans(PAGINATION_LIMIT, params);
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_PLEDGE_RECRUIT_BOARD_SEARCH.writeId(packetWriter);
		packetWriter.writeD(_params.getCurrentPage());
		packetWriter.writeD(ClanSearchManager.getInstance().getPageCount(PAGINATION_LIMIT));

		packetWriter.writeD(_clans.size());

		for(ClanSearchClan clanHolder : _clans)
		{
			packetWriter.writeD(clanHolder.getClanId());
			packetWriter.writeD(0); // Alliance
		}

		for(ClanSearchClan clanHolder : _clans)
		{
			Clan clan = ClanTable.getInstance().getClan(clanHolder.getClanId());

			packetWriter.writeD(clan.getCrestId());
			packetWriter.writeD(clan.getAlliance() == null ? 0 : clan.getAlliance().getAllyCrestId());

			packetWriter.writeS(clan.getName());
			packetWriter.writeS(clan.getLeaderName());

			packetWriter.writeD(clan.getLevel());
			packetWriter.writeD(clan.getAllSize());
			packetWriter.writeD(clanHolder.getSearchType().ordinal());

			packetWriter.writeS(""); // Title (deprecated)

			packetWriter.writeD(clanHolder.getApplication()); // Application
			packetWriter.writeD(clanHolder.getSubUnit()); // Sub Unit Type
		}

		return true;
	}
}