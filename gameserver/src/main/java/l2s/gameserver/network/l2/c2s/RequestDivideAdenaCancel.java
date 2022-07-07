package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExDivideAdenaCancel;

public class RequestDivideAdenaCancel extends L2GameClientPacket
{
	private int _cancel;

	@Override
	protected void readImpl()
	{
		_cancel = readC();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(_cancel == 0)
		{
			if(activeChar.getParty().getCommandChannel() != null && activeChar.getParty().getCommandChannel().isLeaderCommandChannel(activeChar))
				activeChar.getParty().getCommandChannel().broadCast(SystemMsg.ADENA_DISTRIBUTION_HAS_BEEN_CANCELLED);
			else
				activeChar.getParty().broadCast(SystemMsg.ADENA_DISTRIBUTION_HAS_BEEN_CANCELLED);

			activeChar.sendPacket(ExDivideAdenaCancel.STATIC);
		}
	}
}
