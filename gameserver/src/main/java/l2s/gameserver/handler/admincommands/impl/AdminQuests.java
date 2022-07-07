package l2s.gameserver.handler.admincommands.impl;

import l2s.commons.text.PrintfFormat;
import l2s.gameserver.data.QuestHolder;
import l2s.gameserver.handler.admincommands.IAdminCommandHandler;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.components.HtmlMessage;

import java.util.Map;

public class AdminQuests implements IAdminCommandHandler
{
	private static final PrintfFormat fmtHEAD;
	private static final PrintfFormat fmtRow;
	private static final PrintfFormat fmtSetButton;
	private static final PrintfFormat fmtFOOT;
	private static final PrintfFormat fmtListRow;
	private static final PrintfFormat fmtListNew;

	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		if(!activeChar.getPlayerAccess().CanEditCharAll)
			return false;
		switch(command)
		{
			case admin_quests:
			{
				return ShowQuestList(getTargetChar(wordList, 1, activeChar), activeChar);
			}
			case admin_quest:
			{
				if(wordList.length < 2)
				{
					activeChar.sendMessage("USAGE: //quest id|name [SHOW|STATE|VAR|CLEAR] ...");
					return true;
				}
				Quest _quest = QuestHolder.getInstance().getQuest(Integer.parseInt(wordList[1]));
				if(_quest == null)
				{
					activeChar.sendMessage("Quest " + wordList[1] + " undefined");
					return true;
				}
				if(wordList.length < 3 || "SHOW".equalsIgnoreCase(wordList[2]))
					return cmd_Show(_quest, wordList, activeChar);
				if("STATE".equalsIgnoreCase(wordList[2]))
					return cmd_State(_quest, wordList, activeChar);
				if("VAR".equalsIgnoreCase(wordList[2]))
					return cmd_Var(_quest, wordList, activeChar);
				if("CLEAR".equalsIgnoreCase(wordList[2]))
					return cmd_Clear(_quest, wordList, activeChar);
				return cmd_Show(_quest, wordList, activeChar);
			}
			default:
			{
				return true;
			}
		}
	}

	private boolean cmd_Clear(Quest _quest, String[] wordList, Player activeChar)
	{
		Player targetChar = getTargetChar(wordList, 3, activeChar);
		QuestState qs = targetChar.getQuestState(_quest);
		if(qs == null)
		{
			activeChar.sendMessage("Player " + targetChar.getName() + " havn't Quest [" + _quest.getName() + "]");
			return false;
		}
		qs.abortQuest();
		return ShowQuestList(targetChar, activeChar);
	}

	private boolean cmd_Show(Quest _quest, String[] wordList, Player activeChar)
	{
		Player targetChar = getTargetChar(wordList, 3, activeChar);
		QuestState qs = targetChar.getQuestState(_quest);
		if(qs == null)
		{
			activeChar.sendMessage("Player " + targetChar.getName() + " havn't Quest [" + _quest.getName() + "]");
			return false;
		}
		return ShowQuestState(qs, activeChar);
	}

	private static boolean ShowQuestState(QuestState qs, Player activeChar)
	{
		Map<String, String> vars = qs.getVars();
		int id = qs.getQuest().getId();
		String char_name = qs.getPlayer().getName();
		HtmlMessage adminReply = new HtmlMessage(5);
		StringBuilder replyMSG = new StringBuilder("<html><body>");
		replyMSG.append(fmtHEAD.sprintf(qs.getQuest().getClass().getSimpleName(), id));
		replyMSG.append("<table width=260>");
		replyMSG.append(fmtRow.sprintf("PLAYER: ", char_name, ""));
		replyMSG.append(fmtRow.sprintf("STATE: ",
                "1",
                fmtSetButton.sprintf(id, "STATE", "$new_val", char_name, "")));
		for(String key : vars.keySet())
			if(!"<state>".equalsIgnoreCase(key))
				replyMSG.append(fmtRow.sprintf(key + ": ",
                        vars.get(key),
                        fmtSetButton.sprintf(id, "VAR", key, "$new_val", char_name)));
		replyMSG.append(fmtRow.sprintf("<edit var=\"new_name\" width=50 height=12>",
                "~new var~",
                fmtSetButton.sprintf(id, "VAR", "$new_name", "$new_val", char_name)));
		replyMSG.append("</table>");
		replyMSG.append(fmtFOOT.sprintf(id, char_name, char_name));
		replyMSG.append("</body></html>");
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
		vars.clear();
		return true;
	}

	private static boolean ShowQuestList(Player targetChar, Player activeChar)
	{
		HtmlMessage adminReply = new HtmlMessage(5);
		StringBuilder replyMSG = new StringBuilder("<html><body><table width=260>");
		for(QuestState qs : targetChar.getAllQuestsStates())
			if(qs != null && qs.getQuest().isVisible(activeChar))
				replyMSG.append(fmtListRow.sprintf(qs.getQuest().getId(), targetChar.getName(), qs.getQuest().getName(), "1"));
		replyMSG.append(fmtListNew.sprintf(new Object[] { targetChar.getName() }));
		replyMSG.append("</table></body></html>");
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
		return true;
	}

	private boolean cmd_Var(Quest _quest, String[] wordList, Player activeChar)
	{
		if(wordList.length < 5)
		{
			activeChar.sendMessage("USAGE: //quest id|name VAR varname newvalue [target]");
			return false;
		}
		Player targetChar = getTargetChar(wordList, 5, activeChar);
		QuestState qs = targetChar.getQuestState(_quest);
		if(qs == null)
		{
			activeChar.sendMessage("Player " + targetChar.getName() + " havn't Quest [" + _quest.getName() + "], init quest by command:");
			activeChar.sendMessage("//quest id|name STATE 1|2|3 [target]");
			return false;
		}
		if("~".equalsIgnoreCase(wordList[4]) || "#".equalsIgnoreCase(wordList[4]))
			qs.unset(wordList[3]);
		else
			qs.set(wordList[3], wordList[4]);
		return ShowQuestState(qs, activeChar);
	}

	private boolean cmd_State(Quest _quest, String[] wordList, Player activeChar)
	{
		if(wordList.length < 4)
		{
			activeChar.sendMessage("USAGE: //quest id|name STATE 1|2|3 [target]");
			return false;
		}
        try
		{
            int state = Integer.parseInt(wordList[3]);
        }
		catch(Exception e)
		{
			activeChar.sendMessage("Wrong State ID: " + wordList[3]);
			return false;
		}
		Player targetChar = getTargetChar(wordList, 4, activeChar);
		QuestState qs = targetChar.getQuestState(_quest);
		if(qs == null)
		{
			activeChar.sendMessage("Init Quest [" + _quest.getName() + "] for " + targetChar.getName());
			qs = _quest.newQuestState(targetChar);
			qs.setCond(1);
		}
		return ShowQuestState(qs, activeChar);
	}

	private Player getTargetChar(String[] wordList, int wordListIndex, Player activeChar)
	{
		if(wordListIndex >= 0 && wordList.length > wordListIndex)
		{
			Player player = GameObjectsStorage.getPlayer(wordList[wordListIndex]);
			if(player == null)
				activeChar.sendMessage("Can't find player: " + wordList[wordListIndex]);
			return player;
		}
		GameObject my_target = activeChar.getTarget();
		if(my_target != null && my_target.isPlayer())
			return (Player) my_target;
		return activeChar;
	}

	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}

	static
	{
		fmtHEAD = new PrintfFormat("<center><font color=\"LEVEL\">%s [id=%d]</font><br><edit var=\"new_val\" width=100 height=12></center><br>");
		fmtRow = new PrintfFormat("<tr><td>%s</td><td>%s</td><td width=30>%s</td></tr>");
		fmtSetButton = new PrintfFormat("<button value=\"Set\" action=\"bypass -h admin_quest %d %s %s %s %s\" width=30 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\">");
		fmtFOOT = new PrintfFormat("<br><br><br><center><button value=\"Clear Quest\" action=\"bypass -h admin_quest %d CLEAR %s\" width=100 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"> <button value=\"Quests List\" action=\"bypass -h admin_quests %s\" width=100 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></center>");
		fmtListRow = new PrintfFormat("<tr><td><a action=\"bypass -h admin_quest %d %s\">%s</a></td><td>%s</td></tr>");
		fmtListNew = new PrintfFormat("<tr><td><edit var=\"new_quest\" width=100 height=12></td><td><button value=\"Add\" action=\"bypass -h admin_quest $new_quest STATE 2 %s\" width=40 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td></tr>");
	}

	private enum Commands
	{
		admin_quests,
		admin_quest
    }
}
