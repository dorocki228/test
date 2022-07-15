package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
**/
public class ExAdenaInvenCount implements IClientOutgoingPacket
{
	private final long _adena;
	private final int _useInventorySlots;

	public ExAdenaInvenCount(Player player)
	{
		_adena = player.getAdena();
		_useInventorySlots = player.getInventory().getSize();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_ADENA_INVEN_COUNT.writeId(packetWriter);
		packetWriter.writeQ(_adena);
		packetWriter.writeH(_useInventorySlots);

		return true;
	}
}