package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.ConnectionState;
import l2s.gameserver.network.l2.s2c.ExUISetting;

/**
 * format: (ch)db
 */
public class RequestSaveKeyMapping implements IClientIncomingPacket
{
	private byte[] _data;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		int length = packet.readD();
		if(length > packet.getReadableBytes() || length > Short.MAX_VALUE || length < 0)
		{
			_data = null;
			return false;
		}
		_data = packet.readB(length);
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player activeChar = client.getActiveChar();
		if(activeChar == null || _data == null
				|| client.getConnectionState() != ConnectionState.IN_GAME)
			return;
		activeChar.setKeyBindings(_data);
		activeChar.sendPacket(new ExUISetting(activeChar));
	}
}