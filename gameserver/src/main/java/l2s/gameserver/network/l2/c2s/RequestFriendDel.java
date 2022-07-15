package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;

public class RequestFriendDel implements IClientIncomingPacket
{
	private String _name;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_name = packet.readS(16);
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player player = client.getActiveChar();
		if(player == null)
			return;

		player.getFriendList().remove(_name);
	}
}