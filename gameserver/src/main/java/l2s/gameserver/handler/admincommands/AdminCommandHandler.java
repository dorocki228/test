package l2s.gameserver.handler.admincommands;

import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.handler.admincommands.impl.*;
import l2s.gameserver.logging.LogService;
import l2s.gameserver.logging.LoggerType;
import l2s.gameserver.logging.message.AdminCommandLogMessage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.PlayerAccess;
import l2s.gameserver.network.l2.components.CustomMessage;
import org.apache.commons.lang3.ArrayUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AdminCommandHandler extends AbstractHolder
{
	private static final AdminCommandHandler _instance;
	private final Map<String, IAdminCommandHandler> _datatable;

	public static AdminCommandHandler getInstance()
	{
		return _instance;
	}

	private AdminCommandHandler()
	{
		_datatable = new HashMap<>();
		registerAdminCommandHandler(new AdminAdmin());
		registerAdminCommandHandler(new AdminAnnouncements());
		registerAdminCommandHandler(new AdminAttributes());
		registerAdminCommandHandler(new AdminBan());
		registerAdminCommandHandler(new AdminCamera());
		registerAdminCommandHandler(new AdminCancel());
		registerAdminCommandHandler(new AdminChangeAccessLevel());
		registerAdminCommandHandler(new AdminClanHall());
		registerAdminCommandHandler(new AdminCreateItem());
		registerAdminCommandHandler(new AdminDebug());
		registerAdminCommandHandler(new AdminDelete());
		registerAdminCommandHandler(new AdminDisconnect());
		registerAdminCommandHandler(new AdminDoorControl());
		registerAdminCommandHandler(new AdminEditChar());
		registerAdminCommandHandler(new AdminEffects());
		registerAdminCommandHandler(new AdminEnchant());
		registerAdminCommandHandler(new AdminGeodata());
		registerAdminCommandHandler(new AdminGiveAll());
		registerAdminCommandHandler(new AdminGm());
		registerAdminCommandHandler(new AdminGmChat());
		registerAdminCommandHandler(new AdminGve());
		registerAdminCommandHandler(new AdminHeal());
		registerAdminCommandHandler(new AdminHelpPage());
		registerAdminCommandHandler(new AdminInstance());
		registerAdminCommandHandler(new AdminIP());
		registerAdminCommandHandler(new AdminLevel());
		registerAdminCommandHandler(new AdminMammon());
		registerAdminCommandHandler(new AdminManor());
		registerAdminCommandHandler(new AdminMenu());
		registerAdminCommandHandler(new AdminMonsterRace());
		registerAdminCommandHandler(new AdminNochannel());
		registerAdminCommandHandler(new AdminPetition());
		registerAdminCommandHandler(new AdminPForge());
		registerAdminCommandHandler(new AdminPledge());
		registerAdminCommandHandler(new AdminPolymorph());
		registerAdminCommandHandler(new AdminQuests());
		registerAdminCommandHandler(new AdminReload());
		registerAdminCommandHandler(new AdminRepairChar());
		registerAdminCommandHandler(new AdminRes());
		registerAdminCommandHandler(new AdminRide());
		registerAdminCommandHandler(new AdminServer());
		registerAdminCommandHandler(new AdminShop());
		registerAdminCommandHandler(new AdminShutdown());
		registerAdminCommandHandler(new AdminSkill());
		registerAdminCommandHandler(new AdminScripts());
		registerAdminCommandHandler(AdminScreenString.INSTANCE);
		registerAdminCommandHandler(new AdminSpawn());
		registerAdminCommandHandler(new AdminTarget());
		registerAdminCommandHandler(new AdminTeleport());
		registerAdminCommandHandler(new AdminZone());
		registerAdminCommandHandler(new AdminKill());
		registerAdminCommandHandler(new AdminSpamFilter());
		registerAdminCommandHandler(new AdminOlympiad());
	}

	public void registerAdminCommandHandler(IAdminCommandHandler handler)
	{
		for(Enum<?> e : handler.getAdminCommandEnum())
			_datatable.put(e.toString().toLowerCase(), handler);
	}

	public IAdminCommandHandler getAdminCommandHandler(String adminCommand)
	{
		String command = adminCommand;
		if(adminCommand.contains(" "))
			command = adminCommand.substring(0, adminCommand.indexOf(" "));
		return _datatable.get(command);
	}

	public void useAdminCommandHandler(Player activeChar, String adminCommand)
	{
		PlayerAccess playerAccess = activeChar.getPlayerAccess();
		if(playerAccess == null || (!playerAccess.IsModerator && !activeChar.isGM() && !playerAccess.CanUseGMCommand))
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.network.l2.c2s.SendBypassBuildCmd.NoCommandOrAccess").addString(adminCommand));
			return;
		}
		String[] wordList = adminCommand.split(" ");
		IAdminCommandHandler handler = _datatable.get(wordList[0]);
		if(handler != null)
		{
			boolean success = false;
			try
			{
				for(Enum<?> e : handler.getAdminCommandEnum())
					if(e.toString().equalsIgnoreCase(wordList[0]))
					{
						if(playerAccess.IsModerator) {
							String s = wordList[0];
							if(!ArrayUtils.contains(playerAccess.ModeratorCommands, s.substring(6))) {
								break;
							}
						}
						success = handler.useAdminCommand(e, wordList, adminCommand, activeChar);
						break;
					}
			}
			catch(Exception e2)
			{
                error("", e2);
			}
			AdminCommandLogMessage message = new AdminCommandLogMessage(activeChar, activeChar.getTarget(),
					adminCommand, success);
			LogService.getInstance().log(LoggerType.ADMIN_ACTIONS, message);
		}
	}

	@Override
	public void process()
	{}

	@Override
	public int size()
	{
		return _datatable.size();
	}

	@Override
	public void clear()
	{
		_datatable.clear();
	}

	public Set<String> getAllCommands()
	{
		return _datatable.keySet();
	}

	static
	{
		_instance = new AdminCommandHandler();
	}
}
