package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.s2c.CharacterSelectionInfoPacket;

public class CharacterRestore implements IClientIncomingPacket
{
	// cd
	private int _charSlot;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_charSlot = packet.readD();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		try
		{
			client.markRestoredChar(_charSlot);
		}
		catch(Exception e)
		{}
		CharacterSelectionInfoPacket cl = new CharacterSelectionInfoPacket(client);
		client.sendPacket(cl);
		client.setCharSelection(cl.getCharInfo());
	}
}