package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.OutgoingExPackets;
import l2s.gameserver.utils.VariationUtils;

/**
 * @author VISTALL
 */
public class ExPutItemResultForVariationCancel implements IClientOutgoingPacket
{
	private int _itemObjectId;
	private int _itemId;
	private int _aug1;
	private int _aug2;
	private long _price;

	public ExPutItemResultForVariationCancel(ItemInstance item)
	{
		_itemObjectId = item.getObjectId();
		_itemId = item.getItemId();
		_aug1 = item.getVariation1Id();
		_aug2 = item.getVariation2Id();
		_price = VariationUtils.getRemovePrice(item);
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_PUT_ITEM_RESULT_FOR_VARIATION_CANCEL.writeId(packetWriter);
		packetWriter.writeD(_itemObjectId);
		packetWriter.writeD(_itemId);
		packetWriter.writeD(_aug1);
		packetWriter.writeD(_aug2);
		packetWriter.writeQ(_price);
		packetWriter.writeD(0x01);

		return true;
	}
}