package l2s.gameserver.handler.voicecommands.impl;

import l2s.gameserver.Config;
import l2s.gameserver.data.string.ItemNameHolder;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.Zone.ZoneType;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.punishment.PunishmentService;
import l2s.gameserver.punishment.PunishmentType;
import l2s.gameserver.utils.ItemFunctions;

public class Offline implements IVoicedCommandHandler
{
	private final String[] _commandList;

	public Offline()
	{
		_commandList = new String[] { "offline" };
	}

	@Override
	public boolean useVoicedCommand(String command, Player player, String args)
	{
		if(!Config.ALLOW_VOICED_COMMANDS)
			return false;
		if(!Config.SERVICES_OFFLINE_TRADE_ALLOW)
		{
			player.sendMessage(new CustomMessage("voicedcommandhandlers.Offline.Disabled"));
			return false;
		}
		if(player.isPK())
		{
			player.sendActionFailed();
			return false;
		}

		if(player.getOlympiadObserveGame() != null || player.getOlympiadGame() != null || Olympiad.isRegisteredInComp(player) || player.isPK())
		{
			player.sendActionFailed();
			return false;
		}

		if(player.getLevel() < Config.SERVICES_OFFLINE_TRADE_MIN_LEVEL)
		{
			player.sendMessage(new CustomMessage("voicedcommandhandlers.Offline.LowLevel").addNumber(Config.SERVICES_OFFLINE_TRADE_MIN_LEVEL));
			return false;
		}
		if(!player.isInStoreMode() && !player.isPrivateBuffer())
		{
			player.sendMessage(new CustomMessage("voicedcommandhandlers.Offline.IncorrectUse"));
			return false;
		}

		if(PunishmentService.INSTANCE.isPunished(PunishmentType.CHAT, player))
		{
			player.sendMessage(new CustomMessage("voicedcommandhandlers.Offline.BanChat"));
			return false;
		}

		switch(Config.SERVICES_OFFLINE_TRADE_ALLOW_ZONE)
		{
			case 1:
			{
				if(!player.isInPeaceZone())
				{
					player.sendMessage(new CustomMessage("trade.OfflineNoTradeZoneOnlyPeace"));
					return false;
				}
				break;
			}
			case 2:
			{
				if(!player.isInZone(Zone.ZoneType.offshore)
					|| (player.isPrivateBuffer() && Config.PRIVATE_BUFFER.onlyInSpecialZone() && !player.isInZone(ZoneType.private_buffer)))
				{
					player.sendMessage(new CustomMessage("trade.OfflineNoTradeZoneOnlyOffshore"));
					return false;
				}
				break;
			}
		}
		if(player.isActionBlocked("open_private_store") && !player.isPrivateBuffer())
		{
			player.sendMessage(new CustomMessage("trade.OfflineNoTradeZone"));
			return false;
		}
		if(Config.SERVICES_OFFLINE_TRADE_PRICE > 0 && Config.SERVICES_OFFLINE_TRADE_PRICE_ITEM > 0 && !ItemFunctions.deleteItem(player, Config.SERVICES_OFFLINE_TRADE_PRICE_ITEM, Config.SERVICES_OFFLINE_TRADE_PRICE))
		{
			player.sendMessage(new CustomMessage("voicedcommandhandlers.Offline.NotEnough").addString(ItemNameHolder.getInstance().getItemName(player, Config.SERVICES_OFFLINE_TRADE_PRICE_ITEM)).addNumber(Config.SERVICES_OFFLINE_TRADE_PRICE));
			return false;
		}
		player.offline();
		return true;
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}
}
