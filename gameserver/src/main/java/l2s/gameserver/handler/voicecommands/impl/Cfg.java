package l2s.gameserver.handler.voicecommands.impl;

import java.util.HashMap;
import l2s.gameserver.Config;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.data.string.StringsHolder;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.utils.Functions;
import l2s.gameserver.utils.Language;
import org.apache.commons.lang3.math.NumberUtils;

public class Cfg implements IVoicedCommandHandler
{
	private final String[] _commandList;

	public Cfg()
	{
		_commandList = new String[] { "lang", "menu", "cfg"};
	}

	@Override
	public boolean useVoicedCommand(String command, Player player, String args)
	{
		if(!Config.ALLOW_VOICED_COMMANDS)
			return false;
		if("menu".equals(command) && args != null)
		{
			String[] param = args.split(" ");
			if(param.length == 2)
			{
				if("lang".equalsIgnoreCase(param[0]))
					if(!Config.USE_CLIENT_LANG)
						player.setLanguage(param[1]);
					else
						player.sendMessage(new CustomMessage("l2s.gameserver.handler.voicecommands.impl.Cfg.useVoicedCommand.Lang"));
				if("noe".equalsIgnoreCase(param[0]))
					if("on".equalsIgnoreCase(param[1]))
						player.setVar("NoExp", "1", -1L);
					else if("of".equalsIgnoreCase(param[1]))
						player.unsetVar("NoExp");
				if("notraders".equalsIgnoreCase(param[0]))
					if("on".equalsIgnoreCase(param[1]))
					{
						player.setNotShowTraders(true);
						player.setVar("notraders", "1", -1L);
					}
					else if("of".equalsIgnoreCase(param[1]))
					{
						player.setNotShowTraders(false);
						player.unsetVar("notraders");
					}
				if("nobuffers".equalsIgnoreCase(param[0]))
					if("on".equalsIgnoreCase(param[1]))
					{
						player.setNotShowPrivateBuffers(true);
						player.setVar(Player.NO_PRIVATEBUFFERS_VAR, true, -1L);
					}
					else if("of".equalsIgnoreCase(param[1]))
					{
						player.setNotShowPrivateBuffers(false);
						player.unsetVar(Player.NO_PRIVATEBUFFERS_VAR);
					}
				if("notShowBuffAnim".equalsIgnoreCase(param[0]))
					if("on".equalsIgnoreCase(param[1]))
					{
						player.setNotShowBuffAnim(true);
						player.setVar("notShowBuffAnim", true, -1L);
					}
					else if("of".equalsIgnoreCase(param[1]))
					{
						player.setNotShowBuffAnim(false);
						player.unsetVar("notShowBuffAnim");
					}
				if("noShift".equalsIgnoreCase(param[0]))
					if("on".equalsIgnoreCase(param[1]))
						player.setVar("noShift", "1", -1L);
					else if("of".equalsIgnoreCase(param[1]))
						player.unsetVar("noShift");
				if(Config.SERVICES_ENABLE_NO_CARRIER && "noCarrier".equalsIgnoreCase(param[0]))
				{
					int time = NumberUtils.toInt(param[1], Config.SERVICES_NO_CARRIER_DEFAULT_TIME);
					if(time > Config.SERVICES_NO_CARRIER_MAX_TIME)
						time = Config.SERVICES_NO_CARRIER_MAX_TIME;
					else if(time < Config.SERVICES_NO_CARRIER_MIN_TIME)
						time = Config.SERVICES_NO_CARRIER_MIN_TIME;
					player.setVar("noCarrier", String.valueOf(time), -1L);
				}
				if("translit".equalsIgnoreCase(param[0]))
					if("on".equalsIgnoreCase(param[1]))
						player.setVar("translit", "tl", -1L);
					else if("la".equalsIgnoreCase(param[1]))
						player.setVar("translit", "tc", -1L);
					else if("of".equalsIgnoreCase(param[1]))
						player.unsetVar("translit");
				if(Config.AUTO_LOOT_INDIVIDUAL)
				{
					if("autoloot".equalsIgnoreCase(param[0]))
						player.setAutoLoot(Boolean.parseBoolean(param[1]));
					if(Config.AUTO_LOOT_ONLY_ADENA && "autolootonlyadena".equalsIgnoreCase(param[0]))
						player.setAutoLootOnlyAdena(Boolean.parseBoolean(param[1]));
					if("autolooth".equalsIgnoreCase(param[0]))
						player.setAutoLootHerbs(Boolean.parseBoolean(param[1]));
				}
			}
		}
		HashMap<Integer, String> templates = HtmCache.getInstance().getTemplates("command/cfg.htm", player);
		String langBlock = "";
		if(!Config.USE_CLIENT_LANG)
		{
			boolean haveMoreLanguages = false;
			StringBuilder languagesButtons = new StringBuilder();
			String langButton = templates.get(2);
			for(Language lang : Config.AVAILABLE_LANGUAGES)
			{
				if(player.getLanguage() == lang)
					continue;
				haveMoreLanguages = true;
				String button = langButton;
				button = button.replace("<?short_lang_name?>", lang.getShortName());
				button = button.replace("<?lang_name?>", StringsHolder.getInstance().getString("LangFull", lang));
				languagesButtons.append(button);
			}
			if(haveMoreLanguages)
			{
				langBlock = templates.get(1);
				langBlock = langBlock.replace("<?current_lang?>", new CustomMessage("LangFull").toString(player));
				langBlock = langBlock.replace("<?available_languages?>", languagesButtons.toString());
			}
		}
		String disableMessage = new CustomMessage("common.Disable").toString(player);
		String enableMessage = new CustomMessage("common.Enable").toString(player);
		String autolootBlock = "";
		if(Config.AUTO_LOOT_INDIVIDUAL)
		{
			autolootBlock = templates.get(3);
			String autolootAdena = "";
			if(Config.AUTO_LOOT_ONLY_ADENA)
			{
				autolootAdena = templates.get(4);
				autolootAdena = autolootAdena.replace("<?value_adena?>", String.valueOf(!player.isAutoLootOnlyAdenaEnabled()));
				if(player.isAutoLootOnlyAdenaEnabled())
					autolootAdena = autolootAdena.replace("<?value_name_adena?>", disableMessage);
				else
					autolootAdena = autolootAdena.replace("<?value_name_adena?>", enableMessage);
			}
			autolootBlock = autolootBlock.replace("<?adena_autoloot?>", autolootAdena);
			autolootBlock = autolootBlock.replace("<?value_items?>", String.valueOf(!player.isAutoLootEnabled()));
			if(player.isAutoLootEnabled())
				autolootBlock = autolootBlock.replace("<?value_name_items?>", disableMessage);
			else
				autolootBlock = autolootBlock.replace("<?value_name_items?>", enableMessage);
			autolootBlock = autolootBlock.replace("<?value_herbs?>", String.valueOf(!player.isAutoLootHerbsEnabled()));
			if(player.isAutoLootHerbsEnabled())
				autolootBlock = autolootBlock.replace("<?value_name_herbs?>", disableMessage);
			else
				autolootBlock = autolootBlock.replace("<?value_name_herbs?>", enableMessage);
		}
		String noCarrierBlock = "";
		if(Config.SERVICES_ENABLE_NO_CARRIER)
		{
			noCarrierBlock = templates.get(5);
			noCarrierBlock = noCarrierBlock.replace("<?no_carrier_time?>", Config.SERVICES_ENABLE_NO_CARRIER ? player.getVarBoolean("noCarrier") ? player.getVar("noCarrier") : "0" : "N/A");
		}
		String dialog = templates.get(0);
		dialog = dialog.replace("<?lang_block?>", langBlock);
		dialog = dialog.replace("<?autoloot_block?>", autolootBlock);
		dialog = dialog.replace("<?no_carrier_block?>", noCarrierBlock);
		if(player.getVarBoolean("NoExp"))
		{
			dialog = dialog.replace("<?value_noe?>", "of");
			dialog = dialog.replace("<?value_name_noe?>", disableMessage);
		}
		else
		{
			dialog = dialog.replace("<?value_noe?>", "on");
			dialog = dialog.replace("<?value_name_noe?>", enableMessage);
		}
		if(player.getVarBoolean("notraders"))
		{
			dialog = dialog.replace("<?value_notraders?>", "of");
			dialog = dialog.replace("<?value_name_notraders?>", disableMessage);
		}
		else
		{
			dialog = dialog.replace("<?value_notraders?>", "on");
			dialog = dialog.replace("<?value_name_notraders?>", enableMessage);
		}
		if(player.getVarBoolean(Player.NO_PRIVATEBUFFERS_VAR))
		{
			dialog = dialog.replace("<?value_nobuffers?>", "of");
			dialog = dialog.replace("<?value_name_nobuffers?>", disableMessage);
		}
		else
		{
			dialog = dialog.replace("<?value_nobuffers?>", "on");
			dialog = dialog.replace("<?value_name_nobuffers?>", enableMessage);
		}
		if(player.getVarBoolean("notShowBuffAnim"))
		{
			dialog = dialog.replace("<?value_notShowBuffAnim?>", "of");
			dialog = dialog.replace("<?value_name_notShowBuffAnim?>", disableMessage);
		}
		else
		{
			dialog = dialog.replace("<?value_notShowBuffAnim?>", "on");
			dialog = dialog.replace("<?value_name_notShowBuffAnim?>", enableMessage);
		}
		if(player.getVarBoolean("noShift"))
		{
			dialog = dialog.replace("<?value_noShift?>", "of");
			dialog = dialog.replace("<?value_name_noShift?>", disableMessage);
		}
		else
		{
			dialog = dialog.replace("<?value_noShift?>", "on");
			dialog = dialog.replace("<?value_name_noShift?>", enableMessage);
		}
		String tl = player.getVar("translit");
		if(tl == null)
		{
			dialog = dialog.replace("<?value_translit?>", "on");
			dialog = dialog.replace("<?value_name_translit?>", enableMessage);
		}
		else if("tl".equals(tl))
		{
			dialog = dialog.replace("<?value_translit?>", "la");
			dialog = dialog.replace("<?value_name_translit?>", "Lt");
		}
		else if("tc".equals(tl))
		{
			dialog = dialog.replace("<?value_translit?>", "of");
			dialog = dialog.replace("<?value_name_translit?>", disableMessage);
		}
		Functions.show(dialog, player);
		return true;
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}
}
