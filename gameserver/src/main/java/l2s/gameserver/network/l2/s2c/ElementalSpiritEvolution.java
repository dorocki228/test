package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.Elemental;
import l2s.gameserver.network.l2.OutgoingExPackets;
import l2s.gameserver.templates.elemental.ElementalLevelData;

/**
 * @author Bonux
**/
public class ElementalSpiritEvolution implements IClientOutgoingPacket
{
	private final boolean _success;
	private final Elemental _elemental;

	public ElementalSpiritEvolution(Player player, boolean success, int elementId)
	{
		_success = success;
		_elemental = player.getElementalList().get(elementId);
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_ELEMENTAL_SPIRIT_EVOLUTION.writeId(packetWriter);
		packetWriter.writeC(_success); // Result
		packetWriter.writeC(_elemental.getElementId()); //  Elemental ID

		int evolutionLevel = _elemental.getEvolutionLevel();
		packetWriter.writeC(evolutionLevel > 0);
		if(evolutionLevel > 0)
		{
			packetWriter.writeC(evolutionLevel); // Evolution Level (1-3)
			packetWriter.writeD(_elemental.getEvolution().getId()); // Evolution ID from client
			packetWriter.writeQ(_elemental.getExp()); // Current Exp
			packetWriter.writeQ(_elemental.getMaxExp()); // Min Exp For Current Level
			packetWriter.writeQ(_elemental.getEvolution().getMaxExp()); // Max Exp For Current Level
			packetWriter.writeD(_elemental.getLevel()); // Level 1-10
			packetWriter.writeD(_elemental.getEvolution().getMaxLevel()); // Max Level
			packetWriter.writeD(_elemental.getAvailablePoints()); // Available Points
			packetWriter.writeD(_elemental.getAttackPoints()); // Current Attack Points
			packetWriter.writeD(_elemental.getDefencePoints()); // Current Defence Points
			packetWriter.writeD(_elemental.getCritRatePoints()); // Current Crit Rate
			packetWriter.writeD(_elemental.getCritAttackPoints()); // Current Crit Damage
			packetWriter.writeD(_elemental.getEvolution().getMaxAttackPoints()); // Max Attack Points
			packetWriter.writeD(_elemental.getEvolution().getMaxDefencePoints()); // Max Defence Points
			packetWriter.writeD(_elemental.getEvolution().getMaxCritRatePoints()); // Max Crit Rate
			packetWriter.writeD(_elemental.getEvolution().getMaxCritAttackPoints()); // Max Crit Damage

			ElementalLevelData[] datas = _elemental.getEvolution().getLevelDatas();
			packetWriter.writeC(datas.length);
			for(ElementalLevelData data : datas)
			{
				packetWriter.writeH(data.getLevel()); // Level
				packetWriter.writeQ(data.getExp()); // EXP
			}
		}
		return true;
	}
}