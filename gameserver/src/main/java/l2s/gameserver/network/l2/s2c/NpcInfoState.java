package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Creature;

public class NpcInfoState extends L2GameServerPacket
{
	private static final int IS_DEAD = 1;
	private static final int IS_IN_COMBAT = 2;
	private static final int IS_RUNNING = 4;
	private final int _objectId;
	private int _state;

	public NpcInfoState(Creature npc)
	{
		_objectId = npc.getObjectId();
		if(npc.isAlikeDead())
			_state |= 0x1;
		if(npc.isInCombat())
			_state |= 0x2;
		if(npc.isRunning())
			_state |= 0x4;
	}

	public NpcInfoState(Creature npc, int state)
	{
		_objectId = npc.getObjectId();
		_state |= state;
	}

	@Override
	protected void writeImpl()
	{
        writeD(_objectId);
        writeC(_state);
	}
}
