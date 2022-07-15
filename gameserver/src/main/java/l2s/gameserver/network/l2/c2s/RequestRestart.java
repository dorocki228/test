package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.ConnectionState;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ActionFailPacket;
import l2s.gameserver.network.l2.s2c.CharacterSelectionInfoPacket;
import l2s.gameserver.network.l2.s2c.RestartResponsePacket;

public class RequestRestart implements IClientIncomingPacket
{
	/**
	 * packet type id 0x57
	 * format:      c
	 */

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player activeChar = client.getActiveChar();

		if(activeChar == null)
			return;

		if(activeChar.isInObserverMode())
		{
			activeChar.sendPacket(SystemMsg.OBSERVERS_CANNOT_PARTICIPATE, RestartResponsePacket.FAIL, ActionFailPacket.STATIC);
			return;
		}

		if(activeChar.isInCombat())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_RESTART_WHILE_IN_COMBAT, RestartResponsePacket.FAIL, ActionFailPacket.STATIC);
			return;
		}

		if(activeChar.isFishing())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_DO_THAT_WHILE_FISHING_2, RestartResponsePacket.FAIL, ActionFailPacket.STATIC);
			return;
		}

		if(activeChar.isBlocked() && !activeChar.isInTrainingCamp() && !activeChar.isFlying() && !activeChar.isInAwayingMode()) // Разрешаем выходить из игры если используется сервис HireWyvern. Вернет в начальную точку.
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.network.l2.c2s.RequestRestart.OutOfControl"));
			activeChar.sendPacket(RestartResponsePacket.FAIL, ActionFailPacket.STATIC);
			return;
		}

		client.setConnectionState(ConnectionState.AUTHENTICATED);

		activeChar.restart();
		// send char list
		CharacterSelectionInfoPacket cl = new CharacterSelectionInfoPacket(client);
		client.sendPacket(RestartResponsePacket.OK);
		client.sendPacket(new CharacterSelectionInfoPacket(client));
		client.setCharSelection(cl.getCharInfo());
	}
}