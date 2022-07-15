package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;

public class CharacterDeleteFailPacket implements IClientOutgoingPacket
{
	public static int REASON_DELETION_FAILED = 0x01;
	public static int REASON_YOU_MAY_NOT_DELETE_CLAN_MEMBER = 0x02;
	public static int REASON_CLAN_LEADERS_MAY_NOT_BE_DELETED = 0x03;
	int _error;

	public CharacterDeleteFailPacket(int error)
	{
		_error = error;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.CHARACTER_DELETE_FAIL.writeId(packetWriter);
		packetWriter.writeD(_error);
		return true;
	}
}