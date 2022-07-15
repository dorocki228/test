package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.Elemental;
import l2s.gameserver.network.l2.OutgoingExPackets;
import l2s.gameserver.templates.elemental.ElementalAbsorbItem;
import l2s.gameserver.utils.ItemFunctions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Bonux
**/
public class ElementalSpiritAbsorbInfo implements IClientOutgoingPacket
{
	private final int _unk;
	private final int _elementId;
	private final Elemental _elemental;
	private final List<ElementalAbsorbItem> _absorbItems;

	public ElementalSpiritAbsorbInfo(Player player, int unk, int elementId)
	{
		_unk = unk;
		_elementId = elementId;
		_elemental = player.getElementalList().get(elementId);

		if(_elemental != null)
		{
			_absorbItems = new ArrayList<ElementalAbsorbItem>();
			for(ElementalAbsorbItem item : _elemental.getTemplate().getAbsorbItems())
			{
				_absorbItems.add(new ElementalAbsorbItem(item.getId(), ItemFunctions.getItemCount(player, item.getId()), item.getPower()));
			}
		}
		else
			_absorbItems = Collections.emptyList();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_ELEMENTAL_SPIRIT_ABSORB_INFO.writeId(packetWriter);
		packetWriter.writeC(_unk); // Value received from client (ExElementalSpiritAbsorbInfo)
		packetWriter.writeC(_elementId); // AttributeType ID
		if(_elemental != null)
		{
			packetWriter.writeC(_elemental.getEvolutionLevel()); // Unk
			packetWriter.writeQ(_elemental.getExp()); // Current Exp
			packetWriter.writeQ(_elemental.getMaxExp()); // Min Exp For Current Level
			packetWriter.writeQ(_elemental.getEvolution().getMaxExp()); // Max Exp For Current Level
			packetWriter.writeD(_elemental.getLevel()); // Level 1-10
			packetWriter.writeD(_elemental.getEvolution().getMaxLevel()); // Max Level

			packetWriter.writeD(_absorbItems.size()); // Elementals Count
			for(ElementalAbsorbItem item : _absorbItems)
			{
				packetWriter.writeD(item.getId()); // Item ID
				packetWriter.writeD((int) item.getCount()); // Item Count
				packetWriter.writeD(item.getPower()); // Exp Per Item
			}
		}
		else
		{
			packetWriter.writeC(0); // Unk
			packetWriter.writeQ(0); // Current Exp
			packetWriter.writeQ(0); // Min Exp For Current Level
			packetWriter.writeQ(0); // Max Exp For Current Level
			packetWriter.writeD(0); // Level 1-10
			packetWriter.writeD(0); // Max Level
			packetWriter.writeD(0); // Elementals Count
		}
		return true;
	}
}