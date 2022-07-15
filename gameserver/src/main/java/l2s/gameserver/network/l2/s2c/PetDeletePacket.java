package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;

public class PetDeletePacket implements IClientOutgoingPacket
{
	private int _petId;
	private int _petnum;

	public PetDeletePacket(int petId, int petnum)
	{
		_petId = petId;
		_petnum = petnum;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.PET_DELETE.writeId(packetWriter);
		packetWriter.writeD(_petnum);
		packetWriter.writeD(_petId);

		return true;
	}
}