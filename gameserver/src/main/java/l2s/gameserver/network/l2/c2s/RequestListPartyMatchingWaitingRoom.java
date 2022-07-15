package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ExListPartyMatchingWaitingRoom;

/**
 * @author VISTALL
 */
public class RequestListPartyMatchingWaitingRoom implements IClientIncomingPacket
{
	private int _minLevel, _maxLevel, _page, _classes[];

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_page = packet.readD();
		_minLevel = packet.readD();
		_maxLevel = packet.readD();
		int size = packet.readD();
		if(size > Byte.MAX_VALUE || size < 0)
			size = 0;
		_classes = new int[size];
		for(int i = 0; i < size; i++)
			_classes[i] = packet.readD();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;

		activeChar.sendPacket(new ExListPartyMatchingWaitingRoom(activeChar, _minLevel, _maxLevel, _page, _classes));
	}
}