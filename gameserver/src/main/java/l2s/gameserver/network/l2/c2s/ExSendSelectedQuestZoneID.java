package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.QuestListPacket;

/**
 * @author Bonux
**/
public class ExSendSelectedQuestZoneID implements IClientIncomingPacket
{
	private int _questZoneId;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_questZoneId = packet.readD();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;

		activeChar.setQuestZoneId(_questZoneId);
	}
}