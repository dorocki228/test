package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

//пропадает почти весь интерфейс и пооявляется кнопка отказ
//связан с пакетом RequestLeaveCuriousHouse
public class ExCuriousHouseLeave implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket STATIC = new ExCuriousHouseLeave();

	private ExCuriousHouseLeave()
	{
		//TRIGGER
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_CURIOUHOUSE_LEAVE.writeId(packetWriter);
		//

		return true;
	}
}
