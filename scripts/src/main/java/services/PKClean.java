package services;

import l2s.gameserver.Config;
import l2s.gameserver.handler.bypass.Bypass;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.utils.Functions;
import l2s.gameserver.utils.ItemFunctions;

public class PKClean
{
	@Bypass("services.PKClean:pkclean_page")
	public void pkclean_page(Player player, NpcInstance npc, String[] param)
	{
		if(player == null)
			return;
		if(!Config.SERVICES_PK_CLEAN_ENABLED)
		{
			Functions.show("scripts/services/service_disabled.htm", player, npc);
			return;
		}
		if(player.getPkKills() == 0)
		{
			Functions.show("scripts/services/service_no_pk.htm", player, npc);
			return;
		}

		Functions.show("scripts/services/pk_clean.htm", player, npc, "%item_count%", String.valueOf(Config.SERVICES_PK_CLEAN_SELL_PRICE), "%item_id%", String.valueOf(Config.SERVICES_PK_CLEAN_SELL_ITEM));
	}

	@Bypass("services.PKClean:pkclean")
	public void pkclean(Player player, NpcInstance npc, String[] param)
	{
		if(player == null)
			return;
		if(!Config.SERVICES_PK_CLEAN_ENABLED)
		{
			Functions.show("scripts/services/service_disabled.htm", player, npc);
			return;
		}
		if(player.getPkKills() == 0)
		{
			Functions.show("scripts/services/service_no_pk.htm", player, npc);
			return;
		}
		if(ItemFunctions.getItemCount(player, Config.SERVICES_PK_CLEAN_SELL_ITEM) < Config.SERVICES_PK_CLEAN_SELL_PRICE)
		{
			player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
			return;
		}
		ItemFunctions.deleteItem(player, Config.SERVICES_PK_CLEAN_SELL_ITEM, Config.SERVICES_PK_CLEAN_SELL_PRICE);
		player.setPkKills(player.getPkKills() - 1);
		player.sendUserInfo(true);
	}
}
