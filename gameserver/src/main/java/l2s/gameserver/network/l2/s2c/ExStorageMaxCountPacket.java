package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;

public class ExStorageMaxCountPacket extends L2GameServerPacket
{
	private final int _inventory;
	private final int _warehouse;
	private final int _clan;
	private final int _privateSell;
	private final int _privateBuy;
	private final int _recipeDwarven;
	private final int _recipeCommon;
	private final int _questItemsLimit;

	public ExStorageMaxCountPacket(Player player)
	{
		_inventory = player.getInventoryLimit();
		_warehouse = player.getWarehouseLimit();
		_clan = Config.WAREHOUSE_SLOTS_CLAN;
		int tradeLimit = player.getTradeLimit();
		_privateSell = tradeLimit;
		_privateBuy = tradeLimit;
		_recipeDwarven = player.getDwarvenRecipeLimit();
		_recipeCommon = player.getCommonRecipeLimit();
		_questItemsLimit = Config.QUEST_INVENTORY_MAXIMUM;
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_inventory);
        writeD(_warehouse);
        writeD(_clan);
        writeD(_privateSell);
        writeD(_privateBuy);
        writeD(_recipeDwarven);
        writeD(_recipeCommon);
        writeD(0);
        writeD(_questItemsLimit);
        writeD(40);
        writeD(40);
	}
}
