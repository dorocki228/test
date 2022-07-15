package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
**/
public class ExPutEnchantScrollItemResult implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket FAIL = new ExPutEnchantScrollItemResult(0x00);

	private int _result;

	public ExPutEnchantScrollItemResult(int result)
	{
		_result = result;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_PUT_ENCHANT_SCROLL_ITEM_RESULT.writeId(packetWriter);
		packetWriter.writeD(_result);

		return true;
	}
}