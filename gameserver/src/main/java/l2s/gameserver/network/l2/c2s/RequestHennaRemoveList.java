package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.HennaUnequipListPacket;

public class RequestHennaRemoveList implements IClientIncomingPacket
{
	private int _symbolId;

	/**
	 * format: d
	 */
	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_symbolId = packet.readD(); //?
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;

		HennaUnequipListPacket he = new HennaUnequipListPacket(activeChar);
		activeChar.sendPacket(he);
	}
}