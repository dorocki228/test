package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.OutgoingExPackets;
import l2s.gameserver.stats.DoubleStat;

public class ExStorageMaxCount implements IClientOutgoingPacket
{
	private int _inventory;
	private int _warehouse;
	private int _clan;
	private int _privateSell;
	private int _privateBuy;
	private int _recipeDwarven;
	private int _recipeCommon;
	private int _inventoryExtraSlots;
	private int _questItemsLimit;

	public ExStorageMaxCount(Player player)
	{
		_inventory = player.getInventoryLimit();
		_warehouse = player.getWarehouseLimit();
		_clan = Config.WAREHOUSE_SLOTS_CLAN;
		_privateSell = player.getPrivateSellStoreLimit();
		_privateBuy = player.getPrivateBuyStoreLimit();
		_recipeDwarven = player.getDwarvenRecipeLimit();
		_recipeCommon = player.getCommonRecipeLimit();
		_inventoryExtraSlots = (int) player.getStat().getValue(DoubleStat.INVENTORY_NORMAL, 0);
		_questItemsLimit = Config.QUEST_INVENTORY_MAXIMUM;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_STORAGE_MAX_COUNT.writeId(packetWriter);
		packetWriter.writeD(_inventory);
		packetWriter.writeD(_warehouse);
		packetWriter.writeD(_clan);
		packetWriter.writeD(_privateSell);
		packetWriter.writeD(_privateBuy);
		packetWriter.writeD(_recipeDwarven);
		packetWriter.writeD(_recipeCommon);
		packetWriter.writeD(_inventoryExtraSlots); // belt inventory slots increase count
		packetWriter.writeD(_questItemsLimit); //  quests list  by off 100 maximum
		packetWriter.writeD(40); // ??? 40 slots
		packetWriter.writeD(40); // ??? 40 slots
		packetWriter.writeD(0x00); // Artifact slots (Fixed)

		return true;
	}
}