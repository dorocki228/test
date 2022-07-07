package services;

import l2s.gameserver.Config;
import l2s.gameserver.handler.bypass.Bypass;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.utils.Functions;
import l2s.gameserver.utils.ItemFunctions;

public class ClanUpgrade
{
	@Bypass("services.ClanUpgrade:clan_upgrade_page")
	public void clan_upgrade_page(Player player, NpcInstance npc, String[] param)
	{
		if(player == null)
			return;

		if(!Config.SERVICES_CLANLEVEL_SELL_ENABLED)
		{
			Functions.show("scripts/services/service_disabled.htm", player, npc);
			return;
		}
		if(!player.isInPeaceZone())
		{
			Functions.show("scripts/services/service_peace_zone.htm", player, npc);
			return;
		}
		Clan clan = player.getClan();
		if(clan == null)
		{
			player.sendMessage("Get clan first.");
			return;
		}
		if(clan.getLeaderId() != player.getObjectId())
		{
			player.sendMessage("Only clan leader can do that.");
			return;
		}
		if(clan.getLevel() < 1 || clan.getLevel() >= Config.SERVICES_CLAN_MAX_SELL_LEVEL)
		{
			player.sendMessage("Clan level to high or to low.");
			return;
		}
		Functions.show("scripts/services/clan_upgrade.htm", player, npc, "%clan_level_next%", String.valueOf(clan.getLevel() + 1), "%item_count%", String.valueOf(Config.SERVICES_CLANLEVEL_SELL_PRICE[clan.getLevel() - 1]), "%item_id%", String.valueOf(Config.SERVICES_CLANLEVEL_SELL_ITEM[clan.getLevel() - 1]));
	}

	@Bypass("services.ClanUpgrade:clan_upgrade")
	public void clan_upgrade(Player player, NpcInstance npc, String[] param)
	{

		if(player == null)
			return;
		if(!Config.SERVICES_CLANLEVEL_SELL_ENABLED)
		{
			Functions.show("scripts/services/service_disabled.htm", player, npc);
			return;
		}
		if(!player.isInPeaceZone())
		{
			Functions.show("scripts/services/service_peace_zone.htm", player, npc);
			return;
		}
		Clan clan = player.getClan();
		if(clan == null)
		{
			player.sendMessage("Get clan first.");
			return;
		}
		if(clan.getLeaderId() != player.getObjectId())
		{
			player.sendMessage("Only clan leader can do that.");
			return;
		}
		if(clan.getLevel() < 1 || clan.getLevel() >= Config.SERVICES_CLAN_MAX_SELL_LEVEL)
		{
			player.sendMessage("Clan level to high or to low.");
			return;
		}
		int toLvl = clan.getLevel() + 1;
		int requiredItemId = Config.SERVICES_CLANLEVEL_SELL_ITEM[Math.min(Math.max(0, toLvl - 2), Config.SERVICES_CLANLEVEL_SELL_ITEM.length - 1)];
		long requiredItemCount = Config.SERVICES_CLANLEVEL_SELL_PRICE[Math.min(Math.max(0, toLvl - 2), Config.SERVICES_CLANLEVEL_SELL_PRICE.length - 1)];
		if(ItemFunctions.getItemCount(player, requiredItemId) < requiredItemCount)
		{
			player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
			return;
		}
		ItemFunctions.deleteItem(player, requiredItemId, requiredItemCount);
		clan.setLevel(clan.getLevel() + 1);
		clan.updateClanInDB();
		clan.broadcastClanStatus(true, true, true);
		player.sendMessage("Congratulation! Clan level up!.");
	}
}
