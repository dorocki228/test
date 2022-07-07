package services;

import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.handler.bypass.Bypass;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.utils.Functions;
import l2s.gameserver.utils.ItemFunctions;

import java.util.Arrays;
import java.util.List;

public class ClanSkillSell
{
	private static final List<SkillEntry> CLAN_SKILL_BUY_LIST = Arrays.asList(SkillHolder.getInstance().getSkillEntry(392, 3), SkillHolder.getInstance().getSkillEntry(389, 3), SkillHolder.getInstance().getSkillEntry(391, 1));

	@Bypass("services.ClanSkillSell:clan_skill_sell_page")
	public void clan_skill_sell_page(Player player, NpcInstance npc, String[] param)
	{

		if(player == null)
			return;
		if(!Config.SERVICES_CLANSKILL_SELL_ENABLED)
		{
			Functions.show("scripts/services/service_disabled.htm", player, npc);
			return;
		}
		if(!player.isInPeaceZone())
		{
			Functions.show("scripts/services/service_peace_zone.htm", player, npc);
			return;
		}
		Functions.show("scripts/services/clan_skills_sell.htm", player, npc, "%clan_min_level%", String.valueOf(Config.SERVICES_CLANSKIL_SELL_MIN_LEVEL), "%item_id%", String.valueOf(Config.SERVICES_CLAN_SKILL_SELL_ITEM), "%item_count%", String.valueOf(Config.SERVICES_CLAN_SKILL_SELL_PRICE));
	}

	@Bypass("services.ClanSkillSell:clanSkillBuy")
	public void clanSkillBuy(Player player, NpcInstance npc, String[] param)
	{
		if(player == null)
			return;
		if(!Config.SERVICES_CLANSKILL_SELL_ENABLED)
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
		if(!player.isClanLeader())
		{
			player.sendPacket(SystemMsg.ONLY_THE_CLAN_LEADER_IS_ENABLED);
			return;
		}
		if(clan.getLevel() < Config.SERVICES_CLANSKIL_SELL_MIN_LEVEL)
		{
			player.sendMessage("Clan level to low.");
			return;
		}
		if(ItemFunctions.getItemCount(player, Config.SERVICES_CLAN_SKILL_SELL_ITEM) < Config.SERVICES_CLAN_SKILL_SELL_PRICE)
		{
			if(Config.SERVICES_CLAN_SKILL_SELL_ITEM == 57)
				player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			else
				player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
			return;
		}
		ItemFunctions.deleteItem(player, Config.SERVICES_CLAN_SKILL_SELL_ITEM, Config.SERVICES_CLAN_SKILL_SELL_PRICE);
		for(SkillEntry aNewClanSkill : CLAN_SKILL_BUY_LIST)
		{
			clan.addSkill(aNewClanSkill, true);
			clan.broadcastToOnlineMembers(new SystemMessagePacket(SystemMsg.THE_CLAN_SKILL_S1_HAS_BEEN_ADDED).addSkillName(aNewClanSkill.getTemplate()));
		}
	}

}
