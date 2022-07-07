package services;

import l2s.gameserver.Config;
import l2s.gameserver.handler.bypass.Bypass;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.pledge.SubUnit;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.ClanTable;
import l2s.gameserver.utils.Functions;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Util;

public class ClanRename
{

	@Bypass("services.ClanRename:rename_clan_page")
	public void rename_clan_page(Player player, NpcInstance npc, String[] param)
	{
		if(player == null)
			return;
		if(!Config.SERVICES_CHANGE_CLAN_NAME_ENABLED)
		{
			Functions.show("scripts/services/service_disabled.htm", player, npc);
			return;
		}
		if(player.getClan() == null || !player.isClanLeader())
		{
			player.sendPacket(new SystemMessage(SystemMsg.S1_IS_NOT_A_CLAN_LEADER).addName(player));
			return;
		}

		Functions.show("scripts/services/rename_clan.htm", player, npc, "%item_id%", String.valueOf(Config.SERVICES_CHANGE_CLAN_NAME_ITEM), "%item_count%", String.valueOf(Config.SERVICES_CHANGE_CLAN_NAME_PRICE));
	}

	@Bypass("services.ClanRename:rename_clan")
	public void rename_clan(Player player, NpcInstance npc, String[] arg)
	{
		if(player == null)
			return;
		if(arg == null || arg.length < 1)
			return;
		if(!Config.SERVICES_CHANGE_CLAN_NAME_ENABLED)
		{
			Functions.show("scripts/services/service_disabled.htm", player, npc);
			return;
		}
		if(player.getClan() == null || !player.isClanLeader())
		{
			player.sendPacket(new SystemMessage(SystemMsg.S1_IS_NOT_A_CLAN_LEADER).addName(player));
			return;
		}
		if(ClanTable.getInstance().getClanByName(arg[0]) != null)
		{
			Functions.show("scripts/services/rename_clan_err02.htm", player, npc);
			return;
		}
		if(!Util.isMatchingRegexp(arg[0], Config.CLAN_NAME_TEMPLATE))
		{
			Functions.show("scripts/services/rename_clan_err01.htm", player, npc);
			return;
		}
		if(Config.RESTRICTED_CHAR_CLAN_NAME_ENABLE && Config.RESTRICTED_CHAR_CLAN_NAME.matcher(arg[0]).find())
        {
            Functions.show("scripts/services/rename_clan_err01.htm", player, npc);
            return;
        }
		if(player.getEvent(SiegeEvent.class) != null)
		{
			Functions.show("scripts/services/rename_clan_err03.htm", player, npc);
			return;
		}
		if(ItemFunctions.getItemCount(player, Config.SERVICES_CHANGE_CLAN_NAME_ITEM) < Config.SERVICES_CHANGE_CLAN_NAME_PRICE)
		{
			if(Config.SERVICES_CHANGE_CLAN_NAME_ITEM == 57)
				player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			else
				player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
			return;
		}

		ItemFunctions.deleteItem(player, Config.SERVICES_CHANGE_CLAN_NAME_ITEM, Config.SERVICES_CHANGE_CLAN_NAME_PRICE, true);
		String name = arg[0];
		SubUnit sub = player.getClan().getSubUnit(0);
		String oldName = sub.getName();
		sub.setName(name, true);
		player.getClan().broadcastClanStatus(true, true, false);

		Functions.show("scripts/services/rename_clan_msg01.htm", player, npc, "%old_name%", oldName, "%new_name%", name);
	}
}
