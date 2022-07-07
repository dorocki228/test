package services;

import l2s.gameserver.Config;
import l2s.gameserver.handler.bypass.Bypass;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.utils.Functions;
import l2s.gameserver.utils.ItemFunctions;

public class ChangeSex
{
	@Bypass("services.ChangeSex:changesex_page")
	public void changesex_page(Player player, NpcInstance npc, String[] param)
	{
		if(player == null)
			return;
		if(!Config.SERVICES_CHANGE_SEX_ENABLED)
		{
			Functions.show("scripts/services/service_disabled.htm", player, npc);
			return;
		}
		Functions.show("scripts/services/sex_change.htm", player, npc, "%item_id%", String.valueOf(Config.SERVICES_CHANGE_SEX_ITEM), "%item_count%", String.valueOf(Config.SERVICES_CHANGE_SEX_PRICE));
	}

	@Bypass("services.ChangeSex:change_sex")
	public void change_sex(Player player, NpcInstance npc, String[] param)
	{
		if(player == null)
			return;
		if(!Config.SERVICES_CHANGE_SEX_ENABLED)
		{
			Functions.show("scripts/services/service_disabled.htm", player, npc);
			return;
		}
		if(!player.isInPeaceZone() || !player.getReflection().isDefault())
		{
			Functions.show("scripts/services/service_peace_zone.htm", player, npc);
			return;
		}

		if(ItemFunctions.getItemCount(player, Config.SERVICES_CHANGE_SEX_ITEM) < Config.SERVICES_CHANGE_SEX_PRICE)
		{
			if(Config.SERVICES_CHANGE_SEX_ITEM == 57)
				player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			else
				player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
			return;
		}
		ItemFunctions.deleteItem(player, Config.SERVICES_CHANGE_SEX_ITEM, Config.SERVICES_CHANGE_SEX_PRICE, true);

		player.changeSex();
		player.broadcastUserInfo(true);
		player.store(true);
	}
}
