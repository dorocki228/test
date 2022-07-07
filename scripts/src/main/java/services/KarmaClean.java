package services;

import l2s.gameserver.Config;
import l2s.gameserver.handler.bypass.Bypass;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.utils.Functions;
import l2s.gameserver.utils.ItemFunctions;

public class KarmaClean
{
	@Bypass("services.KarmaClean:karmaclean_page")
	public void karmaclean_page(Player player, NpcInstance npc, String[] param)
	{
		if(player == null)
			return;
		if(!Config.SERVICES_KARMA_CLEAN_ENABLED)
		{
			Functions.show("scripts/services/service_disabled.htm", player, npc);
			return;
		}
		if(player.getKarma() == 0)
		{
			Functions.show("scripts/services/service_karma_clean.htm", player, npc);
			return;
		}

		Functions.show("scripts/services/karma_clean.htm", player, npc, "%item_count%", String.valueOf(Config.SERVICES_KARMA_CLEAN_SELL_PRICE), "%item_id%", String.valueOf(Config.SERVICES_KARMA_CLEAN_SELL_ITEM));
	}

	@Bypass("services.KarmaClean:karmaclean")
	public void karmaclean(Player player, NpcInstance npc, String[] param)
	{
		if(player == null)
			return;
		if(!Config.SERVICES_KARMA_CLEAN_ENABLED)
		{
			Functions.show("scripts/services/service_disabled.htm", player, npc);
			return;
		}
		if(player.getKarma() == 0)
		{
			Functions.show("scripts/services/service_karma_clean.htm", player, npc);
			return;
		}
		if(ItemFunctions.getItemCount(player, Config.SERVICES_KARMA_CLEAN_SELL_ITEM) < Config.SERVICES_KARMA_CLEAN_SELL_PRICE)
		{
			player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
			return;
		}
		ItemFunctions.deleteItem(player, Config.SERVICES_KARMA_CLEAN_SELL_ITEM, Config.SERVICES_KARMA_CLEAN_SELL_PRICE);
		player.setKarma(0);
	}
}
