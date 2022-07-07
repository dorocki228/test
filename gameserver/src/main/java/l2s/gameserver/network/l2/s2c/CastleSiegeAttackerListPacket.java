package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.entity.events.objects.SiegeClanObject;
import l2s.gameserver.model.entity.residence.Residence;
import l2s.gameserver.model.pledge.Alliance;
import l2s.gameserver.model.pledge.Clan;

import java.util.Collections;
import java.util.List;

public class CastleSiegeAttackerListPacket extends L2GameServerPacket
{
	private final int _id;
	private int _registrationValid;
	private List<SiegeClanObject> _clans;

	public CastleSiegeAttackerListPacket(Residence residence)
	{
		_clans = Collections.emptyList();
		_id = residence.getId();
		SiegeEvent<?, ?> siegeEvent = residence.getSiegeEvent();
		if(siegeEvent != null)
		{
			_registrationValid = siegeEvent.isRegistrationOver() ? 0 : 1;
			_clans = siegeEvent.getObjects("attackers");
		}
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_id);
        writeD(0);
        writeD(_registrationValid);
        writeD(0);
        writeD(_clans.size());
        writeD(_clans.size());
		for(SiegeClanObject siegeClan : _clans)
		{
			Clan clan = siegeClan.getClan();
            writeD(clan.getClanId());
			writeS(clan.getName());
			writeS(clan.getLeaderName());
            writeD(clan.getCrestId());
            writeD((int) (siegeClan.getDate() / 1000L));
			Alliance alliance = clan.getAlliance();
            writeD(clan.getAllyId());
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
}
