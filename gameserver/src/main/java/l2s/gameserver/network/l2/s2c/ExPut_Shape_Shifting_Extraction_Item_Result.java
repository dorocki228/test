package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
**/
public class ExPut_Shape_Shifting_Extraction_Item_Result implements IClientOutgoingPacket
{
	public static IClientOutgoingPacket FAIL = new ExPut_Shape_Shifting_Extraction_Item_Result(0x00);
	public static IClientOutgoingPacket SUCCESS = new ExPut_Shape_Shifting_Extraction_Item_Result(0x01);

	private final int _result;

	public ExPut_Shape_Shifting_Extraction_Item_Result(int result)
	{
		_result = result;
	}
	
	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_PUT_SHAPE_SHIFTING_EXTRACTION_ITEM_RESULT.writeId(packetWriter);
		packetWriter.writeD(_result); //Result

		return true;
	}
}