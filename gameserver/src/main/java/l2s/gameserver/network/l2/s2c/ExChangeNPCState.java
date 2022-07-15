package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExChangeNPCState implements IClientOutgoingPacket
{
	private int _objId;
	private int _state;

	public ExChangeNPCState(int objId, int state)
	{
		_objId = objId;
		_state = state;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_CHANGE_NPC_STATE.writeId(packetWriter);
		packetWriter.writeD(_objId);
		packetWriter.writeD(_state);

		return true;
	}
}
