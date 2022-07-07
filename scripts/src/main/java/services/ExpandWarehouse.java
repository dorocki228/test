package services;

import l2s.gameserver.Config;
import l2s.gameserver.handler.bypass.Bypass;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.utils.Functions;
import l2s.gameserver.utils.ItemFunctions;

public class ExpandWarehouse
{
	@Bypass("services.ExpandWarehouse:get")
	public void get(Player player, NpcInstance npc, String[] param)
	{
		if(player == null)
			return;
		if(!Config.SERVICES_EXPAND_WAREHOUSE_ENABLED)
		{
			Functions.show("scripts/services/service_disabled.htm", player, npc);
			return;
		}
		if(ItemFunctions.deleteItem(player, Config.SERVICES_EXPAND_WAREHOUSE_ITEM, Config.SERVICES_EXPAND_WAREHOUSE_PRICE, true))
		{
			player.setExpandWarehouse(player.getExpandWarehouse() + 1);
			player.setVar("ExpandWarehouse", String.valueOf(player.getExpandWarehouse()), -1);
		}
		else if(Config.SERVICES_EXPAND_WAREHOUSE_ITEM == 57)
			player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
		else
			player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
        show(player, npc, param);
	}

	@Bypass("services.ExpandWarehouse:show")
	public void show(Player player, NpcInstance npc, String[] param)
	{
		if(player == null)
			return;
		if(!Config.SERVICES_EXPAND_WAREHOUSE_ENABLED)
		{
			Functions.show("scripts/services/service_disabled.htm", player, npc);
			return;
		}

		Functions.show("scripts/services/expand_warehouse.htm", player, npc, "%wh_exp_item%", String.valueOf(Config.SERVICES_EXPAND_WAREHOUSE_ITEM), "%wh_exp_price%", String.valueOf(Config.SERVICES_EXPAND_WAREHOUSE_PRICE), "%wh_limit%", String.valueOf(player.getWarehouseLimit()));

	}
}
