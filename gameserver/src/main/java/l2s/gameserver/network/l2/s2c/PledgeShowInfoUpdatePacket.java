package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;
import org.apache.commons.lang3.StringUtils;

import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.model.entity.residence.ClanHall;
import l2s.gameserver.model.pledge.Alliance;
import l2s.gameserver.model.pledge.Clan;

public class PledgeShowInfoUpdatePacket implements IClientOutgoingPacket
{
	private int clan_id, clan_level, clan_rank, clan_rep, crest_id, ally_id, ally_crest;
	private final boolean atwar;
	private String ally_name = StringUtils.EMPTY;
	private int _hasCastle, _hasClanHall, _hasInstantClanHall;
	private boolean _isDisbanded;

	public PledgeShowInfoUpdatePacket(final Clan clan)
	{
		clan_id = clan.getClanId();
		clan_level = clan.getLevel();

		_hasCastle = clan.getCastle();

		ClanHall clanHall = ResidenceHolder.getInstance().getResidence(ClanHall.class, clan.getHasHideout());
		if(clanHall != null)
		{
			_hasClanHall = clanHall.getId();
			_hasInstantClanHall = clanHall.getInstantZoneId();
		}
		else
		{
			_hasClanHall = 0;
			_hasInstantClanHall = 0;
		}

		clan_rank = clan.getRank();
		clan_rep = clan.getReputationScore();
		crest_id = clan.getCrestId();
		ally_id = clan.getAllyId();
		atwar = clan.isAtWar();
		_isDisbanded = clan.isPlacedForDisband();
		Alliance ally = clan.getAlliance();
		if(ally != null)
		{
			ally_name = ally.getAllyName();
			ally_crest = ally.getAllyCrestId();
		}
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.PLEDGE_SHOW_INFO_UPDATE.writeId(packetWriter);
		//sending empty data so client will ask all the info in response ;)
		packetWriter.writeD(clan_id);
		packetWriter.writeD(Config.REQUEST_ID);
		packetWriter.writeD(crest_id);
		packetWriter.writeD(clan_level);
		packetWriter.writeD(_hasCastle);
		if(_hasInstantClanHall > 0)
		{
			packetWriter.writeD(1);
			packetWriter.writeD(_hasInstantClanHall);
		}
		else if(_hasClanHall != 0)
		{
			packetWriter.writeD(0);
			packetWriter.writeD(_hasClanHall);
		}
		else
		{
			packetWriter.writeD(0);
			packetWriter.writeD(0);
		}
		packetWriter.writeD(0x00); // Has fortress
		packetWriter.writeD(clan_rank);// displayed in the "tree" view (with the clan skills)
		packetWriter.writeD(clan_rep);
		packetWriter.writeD(_isDisbanded ? 3 : 0);
		packetWriter.writeD(0);
		packetWriter.writeD(ally_id); //c5
		packetWriter.writeS(ally_name); //c5
		packetWriter.writeD(ally_crest); //c5
		packetWriter.writeD(atwar); //c5

		packetWriter.writeD(0x00);
		packetWriter.writeD(0x00);

		return true;
	}
}