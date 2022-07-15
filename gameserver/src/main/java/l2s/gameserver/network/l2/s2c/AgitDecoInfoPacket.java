package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.base.ResidenceFunctionType;
import l2s.gameserver.model.entity.residence.ClanHall;
import l2s.gameserver.model.entity.residence.ResidenceFunction;
import l2s.gameserver.network.l2.OutgoingPackets;

/**
 * @author Bonux
**/
public class AgitDecoInfoPacket implements IClientOutgoingPacket
{
	private final ClanHall _clanHall;

	public AgitDecoInfoPacket(ClanHall clanHall)
	{
		_clanHall = clanHall;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.AGIT_DECO_INFO.writeId(packetWriter);

		packetWriter.writeD(_clanHall.getId());

		for(ResidenceFunctionType type : ResidenceFunctionType.VALUES)
		{
			ResidenceFunction function = _clanHall.getActiveFunction(type);
			if(function != null)
				packetWriter.writeC(function.getTemplate().getDepth());
			else
				packetWriter.writeC(0x00);
		}

		packetWriter.writeD(0);
		packetWriter.writeD(0);
		packetWriter.writeD(0);
		packetWriter.writeD(0);
		packetWriter.writeD(0);
		return true;
	}
}