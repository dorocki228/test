package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ElementalSpiritInfo;

public class ExElementalSpiritInfo implements IClientIncomingPacket
{
	private int type;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		type = packet.readC();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;

		if(!Config.ELEMENTAL_SYSTEM_ENABLED)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.getClassLevel().ordinal() < ClassLevel.THIRD.ordinal())
		{
			activeChar.sendPacket(SystemMsg.UNABLE_TO_OPEN_ATTRIBUTE_AFTER_THE_THIRD_CLASS_CHANGE);
			return;
		}

		activeChar.sendPacket(new ElementalSpiritInfo(activeChar, type));
	}
}