package services;

import l2s.gameserver.Config;
import l2s.gameserver.dao.CharacterDAO;
import l2s.gameserver.handler.bypass.Bypass;
import l2s.gameserver.logging.LogService;
import l2s.gameserver.logging.LoggerType;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.utils.Functions;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Util;
import org.apache.logging.log4j.message.ParameterizedMessage;

public class Rename
{
	@Bypass("services.Rename:rename_page")
	public void rename_page(Player player, NpcInstance npc, String[] param)
	{
		if(player == null)
			return;
		if(!Config.SERVICES_CHANGE_NICK_ENABLED)
		{
			Functions.show("scripts/services/service_disabled.htm", player, npc);
			return;
		}

		Functions.show("scripts/services/rename_char.htm", player, npc, "%item_count%", String.valueOf(Config.SERVICES_CHANGE_NICK_PRICE), "%item_id%", String.valueOf(Config.SERVICES_CHANGE_NICK_ITEM));
	}

	@Bypass("services.Rename:rename")
	public void rename(Player player, NpcInstance npc, String[] arg)
	{
		if(player == null)
			return;
		if(!Config.SERVICES_CHANGE_NICK_ENABLED)
		{
			Functions.show("scripts/services/service_disabled.htm", player, npc);
			return;
		}
		if(arg == null || arg.length < 1)
		{
			Functions.show("scripts/services/rename_char_err01.htm", player, npc);
			return;
		}
		if(player.isClanLeader())
		{
			Functions.show("scripts/services/rename_char_err03.htm", player, npc);
			return;
		}
		if(player.getEvent(SiegeEvent.class) != null)
		{
			Functions.show("scripts/services/rename_char_err03.htm", player, npc);
			return;
		}
		String name = arg[0];
		if(CharacterDAO.getInstance().getObjectIdByName(name) > 0)
		{
			Functions.show("scripts/services/rename_char_err02.htm", player, npc);
			return;
		}
		if(!Util.isMatchingRegexp(name, Config.CUSTOM_CNAME_TEMPLATE))
		{
			Functions.show("scripts/services/rename_char_err01.htm", player, npc);
			return;
		}
		if(Config.RESTRICTED_CHAR_CLAN_NAME_ENABLE && Config.RESTRICTED_CHAR_CLAN_NAME.matcher(arg[0]).find())
        {
            Functions.show("scripts/services/rename_char_err01.htm", player, npc);
            return;
        }
		if(ItemFunctions.getItemCount(player, Config.SERVICES_CHANGE_NICK_ITEM) < Config.SERVICES_CHANGE_NICK_PRICE)
		{
			if(Config.SERVICES_CHANGE_NICK_ITEM == 57)
				player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			else
				player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
			return;
		}
		ItemFunctions.deleteItem(player, Config.SERVICES_CHANGE_NICK_ITEM, Config.SERVICES_CHANGE_NICK_PRICE, true);
		String oldName = player.getName();
		player.reName(name, true);

		String messagePattern = "Character {} renamed to {}";
		ParameterizedMessage message = new ParameterizedMessage(messagePattern, oldName, name);
		LogService.getInstance().log(LoggerType.RENAMES, message);

		Functions.show("scripts/services/rename_char_msg01.htm", player, npc, "%new_name%", name, "%old_name%", oldName);
	}
}
