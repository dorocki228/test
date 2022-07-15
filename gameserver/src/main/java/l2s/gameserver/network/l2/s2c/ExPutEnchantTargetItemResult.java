package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExPutEnchantTargetItemResult implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket FAIL = new ExPutEnchantTargetItemResult(0);
	public static final IClientOutgoingPacket SUCCESS = new ExPutEnchantTargetItemResult(1);

	private int _result;

	public ExPutEnchantTargetItemResult(int result)
	{
		_result = result;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_PUT_ENCHANT_TARGET_ITEM_RESULT.writeId(packetWriter);
		packetWriter.writeD(_result);

		return true;
	}
}