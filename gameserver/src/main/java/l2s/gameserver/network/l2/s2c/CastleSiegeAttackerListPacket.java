package l2s.gameserver.network.l2.s2c;

import java.util.Collections;
import java.util.List;

import l2s.gameserver.network.l2.OutgoingPackets;
import org.apache.commons.lang3.StringUtils;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.entity.events.objects.SiegeClanObject;
import l2s.gameserver.model.entity.residence.Residence;
import l2s.gameserver.model.pledge.Alliance;
import l2s.gameserver.model.pledge.Clan;

/**
 * Populates the Siege Attacker List in the SiegeInfo Window<BR>
 * <BR>
 * packet type id 0xca<BR>
 * format: cddddddd + dSSdddSSd<BR>
 * <BR>
 * c = ca<BR>
 * d = CastleID<BR>
 * d = unknow (0x00)<BR>
 * d = registration valid (0x01)<BR>
 * d = unknow (0x00)<BR>
 * d = Number of Attackers Clans?<BR>
 * d = Number of Attackers Clans<BR>
 * { //repeats<BR>
 * d = ClanID<BR>
 * S = ClanName<BR>
 * S = ClanLeaderName<BR>
 * d = ClanCrestID<BR>
 * d = signed time (seconds)<BR>
 * d = AllyID<BR>
 * S = AllyName<BR>
 * S = AllyLeaderName<BR>
 * d = AllyCrestID<BR>
 *
 * @reworked VISTALL
 */
public class CastleSiegeAttackerListPacket implements IClientOutgoingPacket
{
	private int _id, _registrationValid;
	private List<SiegeClanObject> _clans = Collections.emptyList();

	public CastleSiegeAttackerListPacket(Residence residence)
	{
		_id = residence.getId();

		SiegeEvent<?,?> siegeEvent = residence.getSiegeEvent();
		if(siegeEvent != null)
		{
			_registrationValid = !siegeEvent.isRegistrationOver() ? 1 : 0;
			_clans = siegeEvent.getObjects(SiegeEvent.ATTACKERS);
		}
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.CASTLE_SIEGE_ATTACKER_LIST.writeId(packetWriter);
		packetWriter.writeD(_id);

		packetWriter.writeD(0x00); // Owner's view
		packetWriter.writeD(_registrationValid);
		packetWriter.writeD(0x00); // Page number

		packetWriter.writeD(_clans.size());
		packetWriter.writeD(_clans.size());

		for(SiegeClanObject siegeClan : _clans)
		{
			Clan clan = siegeClan.getClan();

			packetWriter.writeD(clan.getClanId());
			packetWriter.writeS(clan.getName());
			packetWriter.writeS(clan.getLeaderName());
			packetWriter.writeD(clan.getCrestId());
			packetWriter.writeD((int) (siegeClan.getDate() / 1000L));

			Alliance alliance = clan.getAlliance();
			packetWriter.writeD(clan.getAllyId());
			if(alliance != null)
			{
				packetWriter.writeS(alliance.getAllyName());
				packetWriter.writeS(alliance.getAllyLeaderName());
				packetWriter.writeD(alliance.getAllyCrestId());
			}
			else
			{
				packetWriter.writeS(StringUtils.EMPTY);
				packetWriter.writeS(StringUtils.EMPTY);
				packetWriter.writeD(0);
			}
		}

		return true;
	}
}