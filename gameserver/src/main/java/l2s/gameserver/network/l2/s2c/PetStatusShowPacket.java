package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Servitor;
import l2s.gameserver.network.l2.OutgoingPackets;

public class PetStatusShowPacket implements IClientOutgoingPacket
{
	private int _summonType, _summonObjId;

	public PetStatusShowPacket(Servitor summon)
	{
		_summonType = summon.getServitorType();
		_summonObjId = summon.getObjectId();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.PET_STATUS_SHOW.writeId(packetWriter);
		packetWriter.writeD(_summonType);
		packetWriter.writeD(_summonObjId);

		return true;
	}
}