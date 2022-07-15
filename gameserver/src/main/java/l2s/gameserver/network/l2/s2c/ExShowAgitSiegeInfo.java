package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.model.entity.residence.clanhall.NormalClanHall;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.OutgoingExPackets;
import l2s.gameserver.tables.ClanTable;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bonux
**/
public class ExShowAgitSiegeInfo implements IClientOutgoingPacket
{
	private final List<AgitInfo> _infos;

	public ExShowAgitSiegeInfo()
	{
		List<NormalClanHall> clanHalls = ResidenceHolder.getInstance().getResidenceList(NormalClanHall.class);

		_infos = new ArrayList<AgitInfo>(clanHalls.size());

		clanHalls.forEach(clanHall ->
		{
			int ch_id = clanHall.getId();
			int getType = clanHall.getClanHallType().ordinal();
			Clan clan = ClanTable.getInstance().getClan(clanHall.getOwnerId());
			String clan_name = clanHall.getOwnerId() == 0 || clan == null ? StringUtils.EMPTY : clan.getName();
			String leader_name = clanHall.getOwnerId() == 0 || clan == null ? StringUtils.EMPTY : clan.getLeaderName();
			int siegeDate = (int) (clanHall.getSiegeDate().getTimeInMillis() / 1000);
			_infos.add(new AgitInfo(clan_name, leader_name, ch_id, getType, siegeDate));
		});
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_SHOW_AGIT_SIEGE_INFO.writeId(packetWriter);
		packetWriter.writeD(_infos.size());
		_infos.forEach(info ->
		{
			packetWriter.writeD(info.ch_id);
			packetWriter.writeD(info.siegeDate);
			packetWriter.writeSizedString(info.clan_name);
			packetWriter.writeSizedString(info.leader_name);
			packetWriter.writeH(info.getType);
		});

		return true;
	}

	static class AgitInfo
	{
		public String clan_name, leader_name;
		public int ch_id, getType, siegeDate;

		public AgitInfo(String clan_name, String leader_name, int ch_id, int lease, int siegeDate)
		{
			this.clan_name = clan_name;
			this.leader_name = leader_name;
			this.ch_id = ch_id;
			this.getType = lease;
			this.siegeDate = siegeDate;
		}
	}
}