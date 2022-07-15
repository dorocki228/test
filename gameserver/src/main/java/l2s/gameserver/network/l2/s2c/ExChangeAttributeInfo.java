package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.base.AttributeType;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
 */
public class ExChangeAttributeInfo implements IClientOutgoingPacket
{
	private int _crystalItemId;
	private int _attributes;
	private int _itemObjId;

	public ExChangeAttributeInfo(int crystalItemId, ItemInstance item)
	{
		_crystalItemId = crystalItemId;
		_attributes = 0;
		for(AttributeType e : AttributeType.VALUES)
		{
			if(e == item.getAttackElement())
				continue;
			_attributes |= e.getMask();
		}
	}

	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_CHANGE_ATTRIBUTE_INFO.writeId(packetWriter);
		packetWriter.writeD(_crystalItemId);//unk??
		packetWriter.writeD(_attributes);
		packetWriter.writeD(_itemObjId);//unk??

		return true;
	}
}