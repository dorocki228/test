package handler.voicecommands;

import l2s.gameserver.Config;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2s.gameserver.listener.script.OnInitScriptListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.utils.Functions;
import l2s.gameserver.utils.HtmlUtils;
import l2s.gameserver.utils.ItemFunctions;

/**
 * @author KanuToIIIKa
 */

public class ClassMaster implements IVoicedCommandHandler, OnInitScriptListener
{

	private final String[] COMMANDS = { "cm", "classmaster" };

	@Override
	public boolean useVoicedCommand(String command, Player player, String args)
	{
		if("classmaster".equals(command) || "cm".equals(command))
		{
			if(args.isEmpty())
			{
				ClassId playerClassId = player.getClassId();
				if(playerClassId.isLast())
				{
					Functions.show("scripts/services/classmaster-no_last_class.htm", player);
					return false;
				}

				int newClassLvl = player.getClassLevel().ordinal() + 1;

				if(!Config.ALLOW_CLASS_MASTERS_LIST.containsKey(newClassLvl))
				{
					Functions.show("scripts/services/classmaster-no_class_lvl_" + newClassLvl + ".htm", player);
					return false;
				}

				if(!checkMinLvl(player))
				{
					Functions.show("scripts/services/classmaster-no_player_lvl_" + newClassLvl + ".htm", player);
					return false;
				}

				int[] pay = Config.ALLOW_CLASS_MASTERS_LIST.get(newClassLvl);
				int payItemId = 0;
				int payItemCount = 0;
				if(pay.length >= 2)
				{
					payItemId = pay[0];
					payItemCount = pay[1];
				}

				String availClassList = generateAvailClassList(player.getClassId());
				if(payItemId > 0 && payItemCount > 0)
				{
					Functions.show("scripts/services/classmaster-class_list_pay.htm", player, 0, "<?AVAIL_CLASS_LIST?>", availClassList, "<?PAY_ITEM?>", HtmlUtils.htmlItemName(payItemId), "<?PAY_ITEM_COUNT?>", String.valueOf(payItemCount));

					return false;
				}
				Functions.show("scripts/services/classmaster-class_list_pay.htm", player, 0, "<?AVAIL_CLASS_LIST?>", availClassList);
			}
			else
			{
				int val = Integer.parseInt(args);
				ClassId classId = ClassId.VALUES[val];
				int newClassLvl = classId.getClassLevel().ordinal();

				if(!classId.childOf(player.getClassId()) || newClassLvl != player.getClassLevel().ordinal() + 1)
					return false;

				if(!Config.ALLOW_CLASS_MASTERS_LIST.containsKey(newClassLvl))
					return false;

				if(!checkMinLvl(player))
					return false;

				if (player.getSubClassList().containsClassId(val)) {
					return false;
				}

				int[] pay = Config.ALLOW_CLASS_MASTERS_LIST.get(newClassLvl);
				if(pay.length >= 2)
				{
					int payItemId = pay[0];
					int payItemCount = pay[1];
					long notEnoughItemCount = payItemCount - ItemFunctions.getItemCount(player, payItemId);
					if(notEnoughItemCount > 0)
					{
						Functions.show("scripts/services/classmaster-class_list_pay.htm", player, 0, "<?PAY_ITEM?>", HtmlUtils.htmlItemName(payItemId), "<?NOT_ENOUGH_ITEM_COUNT?>", String.valueOf(notEnoughItemCount));
						return false;
					}
					ItemFunctions.deleteItem(player, payItemId, payItemCount, true);
				}
				
				if(classId.getClassLevel() == ClassLevel.THIRD)
					ItemFunctions.addItem(player, 57, 50);
				player.setClassId(val, false);
				player.broadcastUserInfo(true);
				Functions.show("scripts/services/classmaster-class_changed.htm", player, 0, "<?CLASS_NAME?>", HtmlUtils.htmlClassName(val));
			}

		}

		return true;
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return COMMANDS;
	}

	@Override
	public void onInit()
	{
		VoicedCommandHandler.getInstance().registerVoicedCommandHandler(this);
	}

	private String generateAvailClassList(ClassId classId)
	{
		StringBuilder classList = new StringBuilder();
		for(ClassId cid : ClassId.VALUES)
		{
			if(cid.childOf(classId) && cid.getClassLevel().ordinal() == classId.getClassLevel().ordinal() + 1)
				classList.append("<button ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h user_cm ").append(cid.getId()).append("\">").append(HtmlUtils.htmlClassName(cid.getId())).append("</button>");
		}
		return classList.toString();
	}

	private boolean checkMinLvl(Player player)
	{
		if(player.getLevel() < player.getClassId().getClassMinLevel(true))
			return false;
		return true;
	}
}
