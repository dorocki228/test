package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Servitor;
import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExPartyPetWindowDelete implements IClientOutgoingPacket
{
	private int _summonObjectId;
	private int _ownerObjectId;
	private int _type;
	private String _summonName;

	public ExPartyPetWindowDelete(Servitor summon)
	{
		_summonObjectId = summon.getObjectId();
		_summonName = summon.getName();
		_type = summon.getServitorType();
		_ownerObjectId = summon.getPlayer().getObjectId();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_PARTY_PET_WINDOW_DELETE.writeId(packetWriter);
		packetWriter.writeD(_summonObjectId);
		packetWriter.writeD(_type);
		packetWriter.writeD(_ownerObjectId);
		packetWriter.writeS(_summonName);

		return true;
	}
}