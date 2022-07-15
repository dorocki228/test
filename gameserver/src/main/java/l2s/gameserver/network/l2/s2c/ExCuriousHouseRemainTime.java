package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExCuriousHouseRemainTime implements IClientOutgoingPacket
{
	private int _time;

	public ExCuriousHouseRemainTime(int time)
	{
		_time = time;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_CURIOUHOUSE_REMAIN_TIME.writeId(packetWriter);
		packetWriter.writeD(_time);

		return true;
	}
}
