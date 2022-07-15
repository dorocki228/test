package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExUserInfoFishing implements IClientOutgoingPacket
{
	private Player _activeChar;

	public ExUserInfoFishing(Player character)
	{
		_activeChar = character;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_USER_INFO_FISHING.writeId(packetWriter);
		packetWriter.writeD(_activeChar.getObjectId());
		if(_activeChar.getFishing().isInProcess())
		{
			packetWriter.writeC(1);
			packetWriter.writeD(_activeChar.getFishing().getHookLocation().getX());
			packetWriter.writeD(_activeChar.getFishing().getHookLocation().getY());
			packetWriter.writeD(_activeChar.getFishing().getHookLocation().getZ());
		}
		else
			packetWriter.writeC(0);

		return true;
	}
}
