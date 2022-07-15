package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Creature;
import l2s.gameserver.network.l2.OutgoingPackets;

public class NickNameChangedPacket implements IClientOutgoingPacket
{
	private final int objectId;
	private final String title;

	public NickNameChangedPacket(Creature cha)
	{
		objectId = cha.getObjectId();
		title = cha.getTitle();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.NICK_NAME_CHANGED.writeId(packetWriter);
		packetWriter.writeD(objectId);
		packetWriter.writeS(title);

		return true;
	}
}