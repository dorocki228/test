package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.base.AttributeType;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author VISTALL
 */
public class ExBaseAttributeCancelResult implements IClientOutgoingPacket
{
	private boolean _result;
	private int _objectId;
	private AttributeType attributeType;

	public ExBaseAttributeCancelResult(boolean result, ItemInstance item, AttributeType attributeType)
	{
		_result = result;
		_objectId = item.getObjectId();
		this.attributeType = attributeType;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_BASE_ATTRIBUTE_CANCEL_RESULT.writeId(packetWriter);
		packetWriter.writeD(_result);
		packetWriter.writeD(_objectId);
		packetWriter.writeD(attributeType.getId());

		return true;
	}
}