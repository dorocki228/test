package services;

import l2s.gameserver.Config;
import l2s.gameserver.handler.bypass.Bypass;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.utils.Functions;
import l2s.gameserver.utils.ItemFunctions;

public class ExpandInventory
{
	@Bypass("services.ExpandInventory:get")
	public void get(Player player, NpcInstance npc, String[] param)
	{

		if(player == null)
			return;
		if(!Config.SERVICES_EXPAND_INVENTORY_ENABLED)
		{
			Functions.show("scripts/services/service_disabled.htm", player, npc);
			return;
		}
		if(player.getInventoryLimit() >= Config.SERVICES_EXPAND_INVENTORY_MAX)
		{
			Functions.show("scripts/services/expand_inventory_max.htm", player, npc);
			return;
		}
		if(ItemFunctions.deleteItem(player, Config.SERVICES_EXPAND_INVENTORY_ITEM, Config.SERVICES_EXPAND_INVENTORY_PRICE, true))
		{
			player.setExpandInventory(player.getExpandInventory() + 1);
			player.setVar("ExpandInventory", String.valueOf(player.getExpandInventory()), -1);
		}
		else if(Config.SERVICES_EXPAND_INVENTORY_ITEM == 57)
			player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
		else
			player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
        show(player, npc, param);
	}

	@Bypass("services.ExpandInventory:show")
	public void show(Player player, NpcInstance npc, String[] param)
	{
		if(player == null)
			return;
		if(!Config.SERVICES_EXPAND_INVENTORY_ENABLED)
		{
			Functions.show("scripts/services/service_disabled.htm", player, npc);
			return;
		}

		Functions.show("scripts/services/expand_inventory.htm", player, npc, "%inven_exp_item%", String.valueOf(Config.SERVICES_EXPAND_INVENTORY_ITEM), "%inven_exp_price%", String.valueOf(Config.SERVICES_EXPAND_INVENTORY_PRICE), "%inven_limit%", String.valueOf(Config.SERVICES_EXPAND_INVENTORY_MAX), "%inven_cap_now%", String.valueOf(player.getInventoryLimit()));
	}
}
