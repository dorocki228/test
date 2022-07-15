package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @autor Monithly
 */
public class ExAlterSkillRequest implements IClientOutgoingPacket
{
	private final int _activeId, _requestId, _duration;

	public ExAlterSkillRequest(int requestId, int activeId, int duration)
	{
		_requestId = requestId;
		_activeId = activeId;
		_duration = duration;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_ALTER_SKILL_REQUEST.writeId(packetWriter);
		packetWriter.writeD(_requestId);
		packetWriter.writeD(_activeId);
		packetWriter.writeD(_duration);

		return true;
	}
}
