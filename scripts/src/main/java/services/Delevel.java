package services;

import l2s.gameserver.Config;
import l2s.gameserver.handler.bypass.Bypass;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.utils.Functions;
import l2s.gameserver.utils.ItemFunctions;

public class Delevel
{
	@Bypass("services.Delevel:delevel_page")
	public void delevel_page(Player player, NpcInstance npc, String[] param)
	{
		if(player == null)
			return;
		if(!Config.SERVICES_DELEVEL_SELL_ENABLED)
		{
			Functions.show("scripts/services/service_disabled.htm", player, npc);
			return;
		}

		Functions.show("scripts/services/level_change.htm", player, npc, "%item_count%", String.valueOf(Config.SERVICES_DELEVEL_SELL_PRICE), "%item_id%", String.valueOf(Config.SERVICES_DELEVEL_SELL_ITEM));
	}

	@Bypass("services.Delevel:delevel")
	public void delevel(Player player, NpcInstance npc, String[] param)
	{
		if(player == null)
			return;
		if(!Config.SERVICES_DELEVEL_SELL_ENABLED)
		{
			Functions.show("scripts/services/service_disabled.htm", player, npc);
			return;
		}
		if(player.getLevel() < 3 || player.getLevel() > player.getMaxExp())
			return;
		if(ItemFunctions.getItemCount(player, Config.SERVICES_DELEVEL_SELL_ITEM) < Config.SERVICES_DELEVEL_SELL_PRICE)
		{
			player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
			return;
		}
		ItemFunctions.deleteItem(player, Config.SERVICES_DELEVEL_SELL_ITEM, Config.SERVICES_DELEVEL_SELL_PRICE);
		player.addExpAndSp(Experience.LEVEL[player.getLevel() - 2] - player.getExp(), 0);
	}
}
