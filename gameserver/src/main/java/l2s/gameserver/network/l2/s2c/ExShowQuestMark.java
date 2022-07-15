package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExShowQuestMark implements IClientOutgoingPacket
{
	private final int _questId, _cond;

	public ExShowQuestMark(int questId, int cond)
	{
		_questId = questId;
		_cond = cond;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_SHOW_QUEST_MARK.writeId(packetWriter);
		packetWriter.writeD(_questId);
		packetWriter.writeD(_cond);

		return true;
	}
}