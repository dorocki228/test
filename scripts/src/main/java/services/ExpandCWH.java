package services;

import l2s.gameserver.Config;
import l2s.gameserver.handler.bypass.Bypass;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.utils.Functions;
import l2s.gameserver.utils.ItemFunctions;

public class ExpandCWH
{
	@Bypass("services.ExpandCWH:get")
	public void get(Player player, NpcInstance npc, String[] param)
	{
		if(player == null)
			return;
		if(!Config.SERVICES_EXPAND_CWH_ENABLED)
		{
			Functions.show("scripts/services/service_disabled.htm", player, npc);
			return;
		}
		if(player.getClan() == null)
		{
			Functions.show("scripts/services/expand_cwh_clanrestriction.htm", player, npc);
			return;
		}
		if(ItemFunctions.deleteItem(player, Config.SERVICES_EXPAND_CWH_ITEM, Config.SERVICES_EXPAND_CWH_PRICE, true))
		{
			player.getClan().setWhBonus(player.getClan().getWhBonus() + 1);
			player.sendMessage("Warehouse capacity is now " + (Config.WAREHOUSE_SLOTS_CLAN + player.getClan().getWhBonus()));
		}
		else if(Config.SERVICES_EXPAND_CWH_ITEM == 57)
			player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
		else
			player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
        show(player, npc, param);
	}

	@Bypass("services.ExpandCWH:show")
	public void show(Player player, NpcInstance npc, String[] param)
	{
		if(player == null)
			return;
		if(!Config.SERVICES_EXPAND_WAREHOUSE_ENABLED)
		{
			Functions.show("scripts/services/service_disabled.htm", player, npc);
			return;
		}
		if(player.getClan() == null)
		{
			Functions.show("scripts/services/expand_cwh_clanrestriction.htm", player, npc);
			return;
		}

		Functions.show("scripts/services/expand_cwh.htm", player, npc, "%cwh_exp_item%", String.valueOf(Config.SERVICES_EXPAND_CWH_ITEM), "%cwh_exp_price%", String.valueOf(Config.SERVICES_EXPAND_CWH_PRICE), "%cwh_cap_now%", String.valueOf(Config.WAREHOUSE_SLOTS_CLAN + player.getClan().getWhBonus()));
	}
}
