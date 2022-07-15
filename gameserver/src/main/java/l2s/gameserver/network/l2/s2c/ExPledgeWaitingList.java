package l2s.gameserver.network.l2.s2c;

import java.util.Collection;

import l2s.gameserver.instancemanager.clansearch.ClanSearchManager;
import l2s.gameserver.model.clansearch.ClanSearchPlayer;
import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author GodWorld
 * @reworked by Bonux
**/
public class ExPledgeWaitingList implements IClientOutgoingPacket
{
	private final Collection<ClanSearchPlayer> _applicants;

	public ExPledgeWaitingList(int clanId)
	{
		_applicants = ClanSearchManager.getInstance().applicantsCollection(clanId);
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_PLEDGE_WAITING_LIST.writeId(packetWriter);
		packetWriter.writeD(_applicants.size());
		for(ClanSearchPlayer applicant : _applicants)
		{
			packetWriter.writeD(applicant.getCharId());
			packetWriter.writeS(applicant.getName());
			packetWriter.writeD(applicant.getClassId());
			packetWriter.writeD(applicant.getLevel());
		}

		return true;
	}
}