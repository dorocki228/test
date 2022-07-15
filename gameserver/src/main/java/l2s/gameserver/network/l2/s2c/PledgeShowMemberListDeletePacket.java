package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;

public class PledgeShowMemberListDeletePacket implements IClientOutgoingPacket
{
	private String _player;

	public PledgeShowMemberListDeletePacket(String playerName)
	{
		_player = playerName;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.PLEDGE_SHOW_MEMBER_LIST_DELETE.writeId(packetWriter);
		packetWriter.writeS(_player);

		return true;
	}
}