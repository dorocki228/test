package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.OutgoingPackets;

public class L2FriendStatus implements IClientOutgoingPacket
{
	private String _charName;
	private boolean _login;

	public L2FriendStatus(Player player, boolean login)
	{
		_login = login;
		_charName = player.getName();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.L2_FRIEND_STATUS.writeId(packetWriter);
		packetWriter.writeD(_login ? 1 : 0); //Logged in 1 logged off 0
		packetWriter.writeS(_charName);
		packetWriter.writeD(0); //id персонажа с базы оффа, не object_id

		return true;
	}
}