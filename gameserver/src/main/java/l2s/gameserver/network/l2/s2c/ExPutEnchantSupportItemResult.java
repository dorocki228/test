package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExPutEnchantSupportItemResult implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket FAIL = new ExPutEnchantSupportItemResult(0x01);
	public static final IClientOutgoingPacket SUCCESS = new ExPutEnchantSupportItemResult(0x01);

	private int _result;

	public ExPutEnchantSupportItemResult(int result)
	{
		_result = result;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_PUT_ENCHANT_SUPPORT_ITEM_RESULT.writeId(packetWriter);
		packetWriter.writeD(_result);

		return true;
	}
}