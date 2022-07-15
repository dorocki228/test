package l2s.gameserver.network.l2.s2c;

import java.util.Collection;

import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.ClanWar;
import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author GodWorld
 * @reworked by Bonux
**/
public class ExViewPledgeWarList implements IClientOutgoingPacket
{
	private Clan _clan;
	private int _state;
	private int _page;

	public ExViewPledgeWarList(Clan clan, int state, int page)
	{
		_clan = clan;
		_page = page;
		_state = state;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_VIEW_PLEDGE_WARLIST.writeId(packetWriter);
		packetWriter.writeD(_page);

		Collection<ClanWar> wars = _clan.getWars().valueCollection();

		packetWriter.writeD(wars.size());
		for(ClanWar war : wars)
		{
			packetWriter.writeS(war.getOpposingClan(_clan).getName());
			packetWriter.writeD(war.getClanWarState(_clan).ordinal());
			packetWriter.writeD(war.getPeriodDuration());
			packetWriter.writeD(war.getPointDiff(_clan));
			packetWriter.writeD(war.calculateWarProgress(_clan).ordinal());
			packetWriter.writeD(war.getKillToStart()); // Friends to start war left
		}

		return true;
	}
}