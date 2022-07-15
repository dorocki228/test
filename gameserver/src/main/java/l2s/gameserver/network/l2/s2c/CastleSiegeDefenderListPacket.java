package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import l2s.gameserver.network.l2.OutgoingPackets;
import org.apache.commons.lang3.StringUtils;
import l2s.gameserver.model.entity.events.impl.CastleSiegeEvent;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.entity.events.objects.SiegeClanObject;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.pledge.Alliance;
import l2s.gameserver.model.pledge.Clan;

/**
 * Populates the Siege Defender List in the SiegeInfo Window<BR>
 * <BR>
 * packet type id 0xcb<BR>
 * format: cddddddd + dSSdddSSd<BR>
 * <BR>
 * c = 0xcb<BR>
 * d = unitId<BR>
 * d = unknow (0x00)<BR>
 * d = активация регистрации (0x01)<BR>
 * d = unknow (0x00)<BR>
 * d = Number of Defending Clans?<BR>
 * d = Number of Defending Clans<BR>
 * { //repeats<BR>
 * d = ClanID<BR>
 * S = ClanName<BR>
 * S = ClanLeaderName<BR>
 * d = ClanCrestID<BR>
 * d = signed time (seconds)<BR>
 * d = Type -> Owner = 0x01 || Waiting = 0x02 || Accepted = 0x03 || Refuse = 0x04<BR>
 * d = AllyID<BR>
 * S = AllyName<BR>
 * S = AllyLeaderName<BR>
 * d = AllyCrestID<BR>
 *
 * @reworked VISTALL
 */
public class CastleSiegeDefenderListPacket implements IClientOutgoingPacket
{
	public static int OWNER = 1;
	public static int WAITING = 2;
	public static int ACCEPTED = 3;
	public static int REFUSE = 4;

	private int _id, _registrationValid;
	private List<DefenderClan> _defenderClans = Collections.emptyList();

	public CastleSiegeDefenderListPacket(Castle castle)
	{
		_id = castle.getId();

		CastleSiegeEvent siegeEvent = castle.getSiegeEvent();
		if(siegeEvent != null)
		{
			_registrationValid = !siegeEvent.isRegistrationOver() && castle.getOwner() != null ? 1 : 0;

			List<SiegeClanObject> defenders = siegeEvent.getObjects(SiegeEvent.DEFENDERS);
			List<SiegeClanObject> defendersWaiting = siegeEvent.getObjects(CastleSiegeEvent.DEFENDERS_WAITING);
			List<SiegeClanObject> defendersRefused = siegeEvent.getObjects(CastleSiegeEvent.DEFENDERS_REFUSED);
			_defenderClans = new ArrayList<DefenderClan>(defenders.size() + defendersWaiting.size() + defendersRefused.size());
			if(castle.getOwner() != null)
				_defenderClans.add(new DefenderClan(castle.getOwner(), OWNER, 0));
			for(SiegeClanObject siegeClan : defenders)
				_defenderClans.add(new DefenderClan(siegeClan.getClan(), ACCEPTED, (int) (siegeClan.getDate() / 1000L)));
			for(SiegeClanObject siegeClan : defendersWaiting)
				_defenderClans.add(new DefenderClan(siegeClan.getClan(), WAITING, (int) (siegeClan.getDate() / 1000L)));
			for(SiegeClanObject siegeClan : defendersRefused)
				_defenderClans.add(new DefenderClan(siegeClan.getClan(), REFUSE, (int) (siegeClan.getDate() / 1000L)));
		}
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.CASTLE_SIEGE_DEFENDER_LIST.writeId(packetWriter);
		packetWriter.writeD(_id);
		packetWriter.writeD(0x00); // Owner's view
		packetWriter.writeD(_registrationValid);
		packetWriter.writeD(0x00); // Page number

		packetWriter.writeD(_defenderClans.size());
		packetWriter.writeD(_defenderClans.size());
		for(DefenderClan defenderClan : _defenderClans)
		{
			Clan clan = defenderClan._clan;

			packetWriter.writeD(clan.getClanId());
			packetWriter.writeS(clan.getName());
			packetWriter.writeS(clan.getLeaderName());
			packetWriter.writeD(clan.getCrestId());
			packetWriter.writeD(defenderClan._time);
			packetWriter.writeD(defenderClan._type);
			packetWriter.writeD(clan.getAllyId());
			Alliance alliance = clan.getAlliance();
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
				packetWriter.writeD(0x00);
			}
		}
		return true;
	}

	private static class DefenderClan
	{
		private Clan _clan;
		private int _type;
		private int _time;

		public DefenderClan(Clan clan, int type, int time)
		{
			_clan = clan;
			_type = type;
			_time = time;
		}
	}
}