package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Servitor;
import l2s.gameserver.network.l2.OutgoingPackets;

public class SetSummonRemainTimePacket implements IClientOutgoingPacket
{
	private final int _maxFed;
	private final int _curFed;

	public SetSummonRemainTimePacket(Servitor summon)
	{
		_curFed = summon.getCurrentFed();
		_maxFed = summon.getMaxFed();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.SET_SUMMON_REMAIN_TIME.writeId(packetWriter);
		packetWriter.writeD(_maxFed);
		packetWriter.writeD(_curFed);

		return true;
	}
}