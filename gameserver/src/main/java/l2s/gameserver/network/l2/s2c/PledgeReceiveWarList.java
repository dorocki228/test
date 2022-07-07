package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.ClanWar;

import java.util.List;

public class PledgeReceiveWarList extends L2GameServerPacket
{
	private final Clan _clan;
	private final int _state;
	private final int _page;

	public PledgeReceiveWarList(Clan clan, int state, int page)
	{
		_clan = clan;
		_page = page;
		_state = state;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_page);
		List<ClanWar> wars = _clan.getClanWars();
		writeD(wars.size());
		for(ClanWar war : wars)
		{
			Clan opposingClan = war.getAttackerClan();
			if(opposingClan == _clan)
				opposingClan = war.getOpposingClan();
			if(opposingClan == null)
				continue;
			int pointDiff = war.getPointDiff(_clan);
			int duration = (int) (war.getPeriodDuration() / 1000L);
			if(war.getClanWarState(_clan).ordinal() >= 3)
				duration += 172800;
			else if(war.getClanWarState(_clan).ordinal() <= 1)
				duration += 345600;
			writeS(opposingClan.getName());
			writeD(war.getClanWarState(_clan).ordinal());
			writeD(duration);
			writeD(pointDiff);
			writeD(war.calculateWarProgress(pointDiff).ordinal());
			writeD(opposingClan.getAllSize());
		}
	}
}
