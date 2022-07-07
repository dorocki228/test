package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.entity.events.impl.CastleSiegeEvent;
import l2s.gameserver.model.entity.events.objects.SiegeClanObject;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.pledge.Alliance;
import l2s.gameserver.model.pledge.Clan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CastleSiegeDefenderListPacket extends L2GameServerPacket
{
	public static int OWNER;
	public static int WAITING;
	public static int ACCEPTED;
	public static int REFUSE;
	private final int _id;
	private int _registrationValid;
	private List<DefenderClan> _defenderClans;

	public CastleSiegeDefenderListPacket(Castle castle)
	{
		_defenderClans = Collections.emptyList();
		_id = castle.getId();
		CastleSiegeEvent siegeEvent = castle.getSiegeEvent();
		if(siegeEvent != null)
		{
			_registrationValid = !siegeEvent.isRegistrationOver() && castle.getOwner() != null ? 1 : 0;
			List<SiegeClanObject> defenders = siegeEvent.getObjects("defenders");
			List<SiegeClanObject> defendersWaiting = siegeEvent.getObjects("defenders_waiting");
			List<SiegeClanObject> defendersRefused = siegeEvent.getObjects("defenders_refused");
			_defenderClans = new ArrayList<>(defenders.size() + defendersWaiting.size() + defendersRefused.size());
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
	protected final void writeImpl()
	{
        writeD(_id);
        writeD(0);
        writeD(_registrationValid);
        writeD(0);
        writeD(_defenderClans.size());
        writeD(_defenderClans.size());
		for(DefenderClan defenderClan : _defenderClans)
		{
			Clan clan = defenderClan._clan;
            writeD(clan.getClanId());
			writeS(clan.getName());
			writeS(clan.getLeaderName());
            writeD(clan.getCrestId());
            writeD(defenderClan._time);
            writeD(defenderClan._type);
            writeD(clan.getAllyId());
			Alliance alliance = clan.getAlliance();
			if(alliance != null)
			{
				writeS(alliance.getAllyName());
				writeS(alliance.getAllyLeaderName());
                writeD(alliance.getAllyCrestId());
			}
			else
			{
				writeS("");
				writeS("");
                writeD(0);
			}
		}
	}

	static
	{
		OWNER = 1;
		WAITING = 2;
		ACCEPTED = 3;
		REFUSE = 4;
	}

	private static class DefenderClan
	{
		private final Clan _clan;
		private final int _type;
		private final int _time;

		public DefenderClan(Clan clan, int type, int time)
		{
			_clan = clan;
			_type = type;
			_time = time;
		}
	}
}
