package services;

import l2s.gameserver.Config;
import l2s.gameserver.handler.bypass.Bypass;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.utils.Functions;
import l2s.gameserver.utils.ItemFunctions;

public class ClanReputationSell
{
	@Bypass("services.ClanReputationSell:clan_reputation_page")
	public void clan_reputation_page(Player player, NpcInstance npc, String[] param)
	{
		if(player == null)
			return;
		if(!Config.SERVICES_CLAN_REPUTATION_ENABLE)
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
		Functions.show("scripts/services/clan_reputation_sell.htm", player, npc, "%item_id%", String.valueOf(Config.SERVICES_CLAN_REPUTATION_ITEM_ID), "%item_count%", String.valueOf(Config.SERVICES_CLAN_REPUTATION_ITEM_COUNT), "%reputation_amount%", String.valueOf(Config.SERVICES_CLAN_REPUTATION_AMOUNT));
	}

	@Bypass("services.ClanReputationSell:clan_reputation_up")
	public void clan_reputation_up(Player player, NpcInstance npc, String[] param)
	{
		if(player == null)
			return;
		if(!Config.SERVICES_CLAN_REPUTATION_ENABLE)
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
		if(ItemFunctions.getItemCount(player, Config.SERVICES_CLAN_REPUTATION_ITEM_ID) < Config.SERVICES_CLAN_REPUTATION_ITEM_COUNT)
		{
			player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
			return;
		}
		ItemFunctions.deleteItem(player, Config.SERVICES_CLAN_REPUTATION_ITEM_ID, Config.SERVICES_CLAN_REPUTATION_ITEM_COUNT);
		clan.incReputation(Config.SERVICES_CLAN_REPUTATION_AMOUNT, true, "ClanReputationServicesAdd");
		player.sendPacket(new SystemMessage(1777).addNumber(Config.SERVICES_CLAN_REPUTATION_AMOUNT));
	}
}
