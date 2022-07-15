package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ElementalSpiritAbsorbInfo;

/**
 * @author Bonux
**/
public class ExElementalSpiritAbsorbInfo implements IClientIncomingPacket
{
	private int _unk, _elementId;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_unk = packet.readC();
		_elementId = packet.readC();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;

		activeChar.sendPacket(new ElementalSpiritAbsorbInfo(activeChar, _unk, _elementId));
	}
}