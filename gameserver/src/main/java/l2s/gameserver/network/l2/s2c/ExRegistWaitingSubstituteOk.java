package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;

/**
 * Created by IntelliJ IDEA. User: Cain Date: 25.05.12 Time: 21:05 запрос
 * выбранному чару на вступление в пати
 */
public class ExRegistWaitingSubstituteOk implements IClientOutgoingPacket
{
	private final Player _partyLeader;

	public ExRegistWaitingSubstituteOk(final Player player)
	{
		_partyLeader = player;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		// FIXME OutgoingExPackets..writeId(packet);
		/*
		 * TODO
		 */

		return true;
	}
}
