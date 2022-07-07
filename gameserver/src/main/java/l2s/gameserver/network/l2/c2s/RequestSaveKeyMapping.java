package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ExUISettingPacket;

public class RequestSaveKeyMapping extends L2GameClientPacket
{
	private byte[] _data;

	@Override
	protected void readImpl()
	{
		int length = readD();
		if(length > _buf.remaining() || length > 32767 || length < 0)
		{
			_data = null;
			return;
		}
        readB(_data = new byte[length]);
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null || _data == null)
			return;
		activeChar.setKeyBindings(_data);
		activeChar.sendPacket(new ExUISettingPacket(activeChar));
	}
}
