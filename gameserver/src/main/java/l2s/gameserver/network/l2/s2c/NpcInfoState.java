package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Creature;
import l2s.gameserver.network.l2.OutgoingPackets;

/**
 * @author Bonux
**/
public class NpcInfoState implements IClientOutgoingPacket
{
	private static final int IS_DEAD = 1 << 0;
	private static final int IS_IN_COMBAT = 1 << 1;
	private static final int IS_RUNNING = 1 << 2;

	private final int _objectId;
	private int _state;

	public NpcInfoState(Creature npc)
	{
		_objectId = npc.getObjectId();

		if(npc.isAlikeDead())
			_state |= IS_DEAD;

		if(npc.isInCombat())
			_state |= IS_IN_COMBAT;

		if(npc.isRunning())
			_state |= IS_RUNNING;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.NPC_INFO_STATE.writeId(packetWriter);
		packetWriter.writeD(_objectId);
		packetWriter.writeC(_state);

		return true;
	}
}
