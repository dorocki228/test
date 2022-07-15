package l2s.gameserver.network.l2.s2c;

import java.util.Collection;

import l2s.gameserver.data.xml.holder.PetitionGroupHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.petition.PetitionMainGroup;
import l2s.gameserver.network.l2.OutgoingExPackets;
import l2s.gameserver.utils.Language;

/**
 * @author VISTALL
 */
public class ExResponseShowStepOne implements IClientOutgoingPacket
{
	private Language _language;

	public ExResponseShowStepOne(Player player)
	{
		_language = player.getLanguage();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_RESPONSE_SHOWSTEP_ONE.writeId(packetWriter);
		Collection<PetitionMainGroup> petitionGroups = PetitionGroupHolder.getInstance().getPetitionGroups();
		packetWriter.writeD(petitionGroups.size());
		for(PetitionMainGroup group : petitionGroups)
		{
			packetWriter.writeC(group.getId());
			packetWriter.writeS(group.getName(_language));
		}

		return true;
	}
}