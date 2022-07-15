package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ElementalSpiritEvolutionInfo;

/**
 * @author Bonux
**/
public class ExElementalSpiritEvolutionInfo implements IClientIncomingPacket
{
	private int _elementId;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_elementId = packet.readC();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;

		activeChar.sendPacket(new ElementalSpiritEvolutionInfo(activeChar, _elementId));
	}
}