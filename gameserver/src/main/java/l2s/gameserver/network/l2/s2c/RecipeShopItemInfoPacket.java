package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;

public class RecipeShopItemInfoPacket extends L2GameServerPacket
{
	private final int _recipeId;
	private final int _shopId;
	private final int _curMp;
	private final int _maxMp;
	private int _success;
	private final long _price;

	public RecipeShopItemInfoPacket(Player activeChar, Player manufacturer, int recipeId, long price, int success)
	{
		_success = -1;
		_recipeId = recipeId;
		_shopId = manufacturer.getObjectId();
		_price = price;
		_success = success;
		_curMp = (int) manufacturer.getCurrentMp();
		_maxMp = manufacturer.getMaxMp();
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_shopId);
        writeD(_recipeId);
        writeD(_curMp);
        writeD(_maxMp);
        writeD(_success);
		writeQ(_price);
	}
}
