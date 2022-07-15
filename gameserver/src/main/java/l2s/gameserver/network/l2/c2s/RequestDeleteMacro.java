package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;

public class RequestDeleteMacro implements IClientIncomingPacket
{
	private int _id;

	/**
	 * format:		cd
	 */
	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_id = packet.readD();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;
		activeChar.deleteMacro(_id);
	}
}