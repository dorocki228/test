package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

//пир отправке этого пакета на экране появляется иконка получения письма
public class ExCuriousHouseEnter implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket STATIC = new ExCuriousHouseEnter();

	public void ExCuriousHouseEnter()
	{
		//TRIGGER
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_CURIOUHOUSE_ENTER.writeId(packetWriter);
		//

		return true;
	}
}
