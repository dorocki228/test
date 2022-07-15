package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.OutgoingPackets;

public class PartySmallWindowDeletePacket implements IClientOutgoingPacket
{
	private final int _objId;
	private final String _name;

	public PartySmallWindowDeletePacket(Player member)
	{
		_objId = member.getObjectId();
		_name = member.getName();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.PARTY_SMALL_WINDOW_DELETE.writeId(packetWriter);
		packetWriter.writeD(_objId);
		packetWriter.writeS(_name);

		return true;
	}
}