package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;
import l2s.gameserver.templates.item.support.AppearanceStone;

/**
 * @author Bonux
**/
public class ExChoose_Shape_Shifting_Item implements IClientOutgoingPacket
{
	private final int _type;
	private final int _targetType;
	private final int _itemId;

	public ExChoose_Shape_Shifting_Item(AppearanceStone stone)
	{
		_type = stone.getType().ordinal();
		_targetType = stone.getClientTargetType().ordinal();
		_itemId = stone.getItemId();
	}
	
	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_CHOOSE_SHAPE_SHIFTING_ITEM.writeId(packetWriter);
		packetWriter.writeD(_targetType); //ShapeType
		packetWriter.writeD(_type); //ShapeShiftingType
		packetWriter.writeD(_itemId); //ItemID

		return true;
	}
}