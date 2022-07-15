package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.ElementalElement;
import l2s.gameserver.network.l2.s2c.ElementalSpiritInfo;

/**
 * @author Bonux
**/
public class ExElementalSpiritChangeType implements IClientIncomingPacket
{
	private int elementId;
	private int type;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		type = packet.readC();
		elementId = packet.readC(); /* 1 - Fire, 2 - Water, 3 - Wind, 4 Earth */
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;

		if(!activeChar.changeActiveElement(ElementalElement.getElementById(elementId)))
		{
			activeChar.sendActionFailed();
			return;
		}

		activeChar.sendElementalInfo();
		activeChar.sendPacket(new ElementalSpiritInfo(activeChar, type));
	}
}