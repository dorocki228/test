package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
**/
public class ExPut_Shape_Shifting_Target_Item_Result implements IClientOutgoingPacket
{
	public static IClientOutgoingPacket FAIL = new ExPut_Shape_Shifting_Target_Item_Result(0x00, 0L);
	public static int SUCCESS_RESULT = 0x01;

	private final int _resultId;
	private final long _price;

	public ExPut_Shape_Shifting_Target_Item_Result(int resultId, long price)
	{
		_resultId = resultId;
		_price = price;
	}
	
	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_PUT_SHAPE_SHIFTING_TARGET_ITEM_RESULT.writeId(packetWriter);
		packetWriter.writeD(_resultId);
		packetWriter.writeQ(_price);

		return true;
	}
}