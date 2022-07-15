package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.OutgoingPackets;

/**
 * dddddQ
 */
public class RecipeShopItemInfoPacket implements IClientOutgoingPacket
{
	private int _recipeId, _shopId, _curMp, _maxMp;
	private int _success = 0xFFFFFFFF;
	private long _price;

	public RecipeShopItemInfoPacket(Player activeChar, Player manufacturer, int recipeId, long price, int success)
	{
		_recipeId = recipeId;
		_shopId = manufacturer.getObjectId();
		_price = price;
		_success = success;
		_curMp = (int) manufacturer.getCurrentMp();
		_maxMp = manufacturer.getMaxMp();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.RECIPE_SHOP_ITEM_INFO.writeId(packetWriter);
		packetWriter.writeD(_shopId);
		packetWriter.writeD(_recipeId);
		packetWriter.writeD(_curMp);
		packetWriter.writeD(_maxMp);
		packetWriter.writeD(_success);
		packetWriter.writeQ(_price);

		return true;
	}
}