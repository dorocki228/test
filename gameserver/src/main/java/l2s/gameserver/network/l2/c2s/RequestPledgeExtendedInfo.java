package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;

public class RequestPledgeExtendedInfo extends L2GameClientPacket
{
	private String _name;

	@Override
	protected void readImpl()
	{
		_name = readS(16);
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(activeChar.isGM())
			activeChar.sendMessage("RequestPledgeExtendedInfo");
	}
}
