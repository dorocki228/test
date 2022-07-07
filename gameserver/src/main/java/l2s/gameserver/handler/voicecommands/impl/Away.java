package l2s.gameserver.handler.voicecommands.impl;

import l2s.gameserver.Config;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.instancemanager.AwayManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.network.l2.components.CustomMessage;

public class Away implements IVoicedCommandHandler
{
	private final String[] VOICED_COMMANDS;

	public Away()
	{
		VOICED_COMMANDS = new String[] { "away", "back" };
	}

	@Override
	public boolean useVoicedCommand(String command, Player player, String args)
	{
		if(!command.startsWith("away"))
			return command.startsWith("back") && back(player);
		if(Config.AWAY_ONLY_FOR_PREMIUM && !player.hasPremiumAccount())
		{
			player.sendMessage(new CustomMessage("PremiumOnly"));
			return false;
		}
		return away(player, args);
	}

	private boolean away(Player activeChar, String text)
	{
		if(activeChar.isInAwayingMode())
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.handler.voicecommands.impl.Away.Already"));
			return false;
		}
		if(!activeChar.isInZone(Zone.ZoneType.peace_zone) && Config.AWAY_PEACE_ZONE)
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.handler.voicecommands.impl.Away.PieceOnly"));
			return false;
		}
		if(activeChar.isMovementDisabled() || activeChar.isAlikeDead())
			return false;
		SiegeEvent<?, ?> siege = activeChar.getEvent(SiegeEvent.class);
		if(siege != null)
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.handler.voicecommands.impl.Away.Siege"));
			return false;
		}
		if(activeChar.isInDuel())
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.handler.voicecommands.impl.Away.Duel"));
			return false;
		}
		if(activeChar.isInOlympiadMode() || activeChar.getOlympiadGame() != null)
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.handler.voicecommands.impl.Away.Olympiad"));
			return false;
		}
		if(activeChar.isInObserverMode())
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.handler.voicecommands.impl.Away.Observer"));
			return false;
		}
		if(activeChar.getKarma() > 0 || activeChar.getPvpFlag() > 0)
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.handler.voicecommands.impl.Away.Pvp"));
			return false;
		}
		if(text == null)
			text = "";
		if(text.length() > 10)
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.handler.voicecommands.impl.Away.Text"));
			return false;
		}
		if(activeChar.getTarget() == null)
		{
			AwayManager.getInstance().setAway(activeChar, text);
			return true;
		}
		activeChar.sendMessage(new CustomMessage("l2s.gameserver.handler.voicecommands.impl.Away.Target"));
		return false;
	}

	private boolean back(Player activeChar)
	{
		if(!activeChar.isInAwayingMode())
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.handler.voicecommands.impl.Away.Not"));
			return false;
		}
		AwayManager.getInstance().setBack(activeChar);
		return true;
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}
