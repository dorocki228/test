package l2s.gameserver.handler.admincommands.impl;

import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.handler.admincommands.IAdminCommandHandler;
import l2s.gameserver.logging.LogService;
import l2s.gameserver.logging.LoggerType;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SkillCoolTimePacket;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.stats.Calculator;
import l2s.gameserver.stats.funcs.Func;
import l2s.gameserver.utils.HtmlUtils;
import org.apache.logging.log4j.message.SimpleMessage;

import java.util.Collection;

public class AdminSkill implements IAdminCommandHandler
{
	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		if(!activeChar.getPlayerAccess().CanEditChar)
			return false;
		switch(command)
		{
			case admin_show_skills:
			{
				showSkillsPage(activeChar);
				break;
			}
			case admin_show_effects:
			{
				showEffects(activeChar);
				break;
			}
			case admin_remove_skills:
			{
				removeSkillsPage(activeChar);
				break;
			}
			case admin_remove_all_skills:
			{
				removeAllSkills(activeChar);
				break;
			}
			case admin_skill_list:
			{
				activeChar.sendPacket(new HtmlMessage(5).setFile("admin/skills.htm"));
				break;
			}
			case admin_skill_index:
			{
				if(wordList.length > 1)
				{
					activeChar.sendPacket(new HtmlMessage(5).setFile("admin/skills/" + wordList[1] + ".htm"));
					break;
				}
				break;
			}
			case admin_add_skill:
			{
				adminAddSkill(activeChar, wordList);
				break;
			}
			case admin_remove_skill:
			{
				adminRemoveSkill(activeChar, wordList);
				break;
			}
			case admin_get_skills:
			{
				adminGetSkills(activeChar);
				break;
			}
			case admin_reset_skills:
			{
				adminResetSkills(activeChar);
				break;
			}
			case admin_give_all_skills:
			{
				adminGiveAllSkills(activeChar);
				break;
			}
			case admin_debug_stats:
			{
				debug_stats(activeChar);
				break;
			}
			case admin_remove_cooldown:
			{
				GameObject target = activeChar.getTarget();
				if(target != null && target.isPlayer() && (activeChar == target || activeChar.getPlayerAccess().CanEditCharAll))
				{
					Player player = (Player) target;
					player.resetReuse();
					player.sendPacket(new SkillCoolTimePacket(activeChar));
					player.sendMessage("The reuse delay of all skills has been reseted.");
					showSkillsPage(activeChar);
					break;
				}
				activeChar.sendPacket(SystemMsg.INVALID_TARGET);
				return false;
			}
			case admin_buff:
			{
				for(int i = 7041; i <= 7064; ++i)
					activeChar.addSkill(SkillHolder.getInstance().getSkillEntry(i, 1));
				activeChar.sendSkillList();
				break;
			}
		}
		return true;
	}

	private void debug_stats(Player activeChar)
	{
		GameObject target_obj = activeChar.getTarget();
		if (target_obj == null)
			target_obj = activeChar;
		if(!target_obj.isCreature())
		{
			activeChar.sendPacket(SystemMsg.INVALID_TARGET);
			return;
		}

		Creature target = (Creature) target_obj;

		Calculator[] calculators = target.getCalculators();

		StringBuilder str = new StringBuilder(40000);
		str.append("--- Debug for ");
		str.append(target.getName());
		str.append(" ---\r\n");

		for(Calculator calculator : calculators)
		{
			if(calculator == null)
				continue;

            var value = calculator.getBase();
			str.append("Stat: ");
			str.append(calculator._stat.getValue());
			str.append(", prevValue: ");
			str.append(calculator.getLast());
			str.append("\r\n");
			Func[] funcs = calculator.getFunctions();
			for(int i = 0; i < funcs.length; i++)
			{
				str.append("\tFunc #");
				str.append(i);
				str.append("@ [0x");
				String order = Integer.toHexString(funcs[i].order).toUpperCase();
				if(order.length() == 1)
					str.append("0");
				str.append(order);
				str.append("]");
				str.append(funcs[i].getClass().getSimpleName());
				str.append("\t");
				str.append(value);
				if(funcs[i].getCondition() == null
                        || funcs[i].getCondition().test(target, activeChar, null, null, value))
                    value = funcs[i].calc(target, activeChar, null, value);
				str.append(" -> ");
				str.append(value);
				if (funcs[i].owner != null)
				{
					str.append("; owner: ");
					str.append(funcs[i].owner.toString());
				}
				else
					str.append("; no owner");
				str.append("\r\n");
			}
		}

		LogService.getInstance().log(LoggerType.DEBUG, new SimpleMessage(str));
	}

	private void adminGiveAllSkills(Player activeChar)
	{
		GameObject target = activeChar.getTarget();
		if(target != null && target.isPlayer() && (activeChar == target || activeChar.getPlayerAccess().CanEditCharAll))
		{
			Player player = (Player) target;
			int skillCounter = player.rewardSkills(true, true, true);
			player.sendMessage("Admin gave you " + skillCounter + " skills.");
			activeChar.sendMessage("You gave " + skillCounter + " skills to " + player.getName());
			return;
		}
		activeChar.sendPacket(SystemMsg.INVALID_TARGET);
	}

	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}

	private void removeSkillsPage(Player activeChar)
	{
		GameObject target = activeChar.getTarget();
		if(target.isPlayer() && (activeChar == target || activeChar.getPlayerAccess().CanEditCharAll))
		{
			Player player = (Player) target;
			Collection<SkillEntry> skills = player.getAllSkills();
			HtmlMessage adminReply = new HtmlMessage(5);
			StringBuilder replyMSG = new StringBuilder("<html><body>");
			replyMSG.append("<table width=260><tr>");
			replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
			replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_show_skills\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("</tr></table>");
			replyMSG.append("<br><br>");
			replyMSG.append("<center>Editing character: " + player.getName());
			replyMSG.append("<br>Level: " + player.getLevel() + " " + HtmlUtils.htmlClassName(player.getClassId().getId()) + "</center>");
			replyMSG.append("<br><center>Click on the skill you wish to remove:</center>");
			replyMSG.append("<br><table width=270>");
			replyMSG.append("<tr><td width=80>Name:</td><td width=60>Level:</td><td width=40>Id:</td></tr>");
			for(SkillEntry element : skills)
				replyMSG.append("<tr><td width=80><a action=\"bypass -h admin_remove_skill " + element.getId() + "\">" + element.getName(activeChar) + "</a></td><td width=60>" + element.getLevel() + "</td><td width=40>" + element.getId() + "</td></tr>");
			replyMSG.append("</table>");
			replyMSG.append("<br><center><table>");
			replyMSG.append("Remove custom skill:");
			replyMSG.append("<tr><td>Id: </td>");
			replyMSG.append("<td><edit var=\"id_to_remove\" width=110></td></tr>");
			replyMSG.append("</table></center>");
			replyMSG.append("<center><button value=\"Remove skill\" action=\"bypass -h admin_remove_skill $id_to_remove\" width=110 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center>");
			replyMSG.append("<br><center><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15></center>");
			replyMSG.append("</body></html>");
			adminReply.setHtml(replyMSG.toString());
			activeChar.sendPacket(adminReply);
			return;
		}
		activeChar.sendPacket(SystemMsg.INVALID_TARGET);
	}

	private void removeAllSkills(Player activeChar)
	{
		GameObject target = activeChar.getTarget();
		if(target.isPlayer() && (activeChar == target || activeChar.getPlayerAccess().CanEditCharAll))
		{
			Player player = (Player) target;
			Collection<SkillEntry> skills = player.getAllSkills();
			for(SkillEntry skillEntry : skills)
				if(skillEntry != null)
					player.removeSkill(skillEntry, true);
			player.sendSkillList();
			activeChar.sendMessage("You removed all skills from target: " + player.getName() + ".");
			showSkillsPage(activeChar);
			return;
		}
		activeChar.sendPacket(SystemMsg.INVALID_TARGET);
	}

	private void showSkillsPage(Player activeChar)
	{
		GameObject target = activeChar.getTarget();
		if(target != null && target.isPlayer() && (activeChar == target || activeChar.getPlayerAccess().CanEditCharAll))
		{
			Player player = (Player) target;
			HtmlMessage adminReply = new HtmlMessage(5);
			StringBuilder replyMSG = new StringBuilder("<html><body>");
			replyMSG.append("<table width=260><tr>");
			replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
			replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("</tr></table>");
			replyMSG.append("<br><br>");
			replyMSG.append("<center>Editing character: " + player.getName());
			replyMSG.append("<br>Level: " + player.getLevel() + " " + HtmlUtils.htmlClassName(player.getClassId().getId()) + "</center>");
			replyMSG.append("<br><center><table>");
			replyMSG.append("<tr><td><button value=\"Add skills\" action=\"bypass -h admin_skill_list\" width=100 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("<td><button value=\"Get skills\" action=\"bypass -h admin_get_skills\" width=100 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
			replyMSG.append("<tr><td><button value=\"Delete skills\" action=\"bypass -h admin_remove_skills\" width=100 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("<td><button value=\"Delete all skills\" action=\"bypass -h admin_remove_all_skills\" width=100 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
			replyMSG.append("<tr><td><button value=\"Reset skills\" action=\"bypass -h admin_reset_skills\" width=100 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("<td><button value=\"Reset reuse\" action=\"bypass -h admin_remove_cooldown\" width=100 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
			replyMSG.append("<tr><td><button value=\"Give All Skills\" action=\"bypass -h admin_give_all_skills\" width=100 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
			replyMSG.append("</table></center>");
			replyMSG.append("</body></html>");
			adminReply.setHtml(replyMSG.toString());
			activeChar.sendPacket(adminReply);
			return;
		}
		activeChar.sendPacket(SystemMsg.INVALID_TARGET);
	}

	private void showEffects(Player activeChar)
	{
		GameObject target = activeChar.getTarget();
		if(target != null && target.isPlayer() && (activeChar == target || activeChar.getPlayerAccess().CanEditCharAll))
		{
			Player player = (Player) target;
			HtmlMessage adminReply = new HtmlMessage(5);
			StringBuilder replyMSG = new StringBuilder("<html><body>");
			replyMSG.append("<table width=260><tr>");
			replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
			replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("</tr></table>");
			replyMSG.append("<br><br>");
			replyMSG.append("<center>Editing character: " + player.getName() + "</center>");
			replyMSG.append("<br><center><button value=\"");
			replyMSG.append(player.isLangRus() ? "\u041e\u0431\u043d\u043e\u0432\u0438\u0442\u044c" : "Refresh");
			replyMSG.append("\" action=\"bypass -h admin_show_effects\" width=100 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" /></center>");
			replyMSG.append("<br>");
			for(Abnormal e : player.getAbnormalList().getEffects())
				replyMSG.append(e.getSkill().getName(activeChar)).append(" ").append(e.getSkill().getLevel()).append(" - ").append(e.getSkill().isToggle() ? "Infinity" : e.getTimeLeft() + " seconds").append("<br1>");
			replyMSG.append("<br></body></html>");
			adminReply.setHtml(replyMSG.toString());
			activeChar.sendPacket(adminReply);
			return;
		}
		activeChar.sendPacket(SystemMsg.INVALID_TARGET);
	}

	private void adminGetSkills(Player activeChar)
	{
		GameObject target = activeChar.getTarget();
		if(target.isPlayer() && (activeChar == target || activeChar.getPlayerAccess().CanEditCharAll))
		{
			Player player = (Player) target;
			if(player.getName().equals(activeChar.getName()))
				player.sendMessage("There is no point in doing it on your character.");
			else
			{
				Collection<SkillEntry> skills = player.getAllSkills();
				activeChar.getAllSkillsStream()
						.forEach(element -> activeChar.removeSkill(element, true));
				for(SkillEntry element2 : skills)
					activeChar.addSkill(element2, true);
				activeChar.sendMessage("You now have all the skills of  " + player.getName() + ".");
			}
			showSkillsPage(activeChar);
			return;
		}
		activeChar.sendPacket(SystemMsg.INVALID_TARGET);
	}

	private void adminResetSkills(Player activeChar)
	{
		GameObject target = activeChar.getTarget();
		if(target.isPlayer() && (activeChar == target || activeChar.getPlayerAccess().CanEditCharAll))
		{
			Player player = (Player) target;
			player.checkSkills();
			player.sendSkillList();
			player.sendMessage("[GM]" + activeChar.getName() + " has updated your skills.");
			int counter = 0;
			activeChar.sendMessage(counter + " skills removed.");
			showSkillsPage(activeChar);
			return;
		}
		activeChar.sendPacket(SystemMsg.INVALID_TARGET);
	}

	private void adminAddSkill(Player activeChar, String[] wordList)
	{
		GameObject target = activeChar.getTarget();
		if(target != null && target.isPlayer() && (activeChar == target || activeChar.getPlayerAccess().CanEditCharAll))
		{
			Player player = (Player) target;
			if(wordList.length >= 2)
			{
				int id = Integer.parseInt(wordList[1]);
				int level = 1;
				if(wordList.length >= 3)
					level = Integer.parseInt(wordList[2]);
				SkillEntry skillEntry = SkillHolder.getInstance().getSkillEntry(id, level);
				if(skillEntry != null)
				{
					player.sendMessage("Admin gave you the skill " + skillEntry.getName(player) + ".");
					player.addSkill(skillEntry, true);
					player.sendSkillList();
					activeChar.sendMessage("You gave the skill " + skillEntry.getName(activeChar) + " to " + player.getName() + ".");
				}
				else
					activeChar.sendMessage("Error: there is no such skill.");
			}
			showSkillsPage(activeChar);
			return;
		}
		activeChar.sendPacket(SystemMsg.INVALID_TARGET);
	}

	private void adminRemoveSkill(Player activeChar, String[] wordList)
	{
		GameObject target = activeChar.getTarget();
		if(target.isPlayer() && (activeChar == target || activeChar.getPlayerAccess().CanEditCharAll))
		{
			Player player = (Player) target;
			if(wordList.length == 2)
			{
				int id = Integer.parseInt(wordList[1]);
				int level = player.getSkillLevel(id);
				SkillEntry skillEntry = SkillHolder.getInstance().getSkillEntry(id, level);
				if(skillEntry != null)
				{
					player.sendMessage("Admin removed the skill " + skillEntry.getName(player) + ".");
					player.removeSkill(skillEntry, true);
					player.sendSkillList();
					activeChar.sendMessage("You removed the skill " + skillEntry.getName(activeChar) + " from " + player.getName() + ".");
				}
				else
					activeChar.sendMessage("Error: there is no such skill.");
			}
			removeSkillsPage(activeChar);
			return;
		}
		activeChar.sendPacket(SystemMsg.INVALID_TARGET);
	}

	private enum Commands
	{
		admin_show_skills,
		admin_remove_skills,
		admin_remove_all_skills,
		admin_skill_list,
		admin_skill_index,
		admin_add_skill,
		admin_remove_skill,
		admin_get_skills,
		admin_reset_skills,
		admin_give_all_skills,
		admin_show_effects,
		admin_debug_stats,
		admin_remove_cooldown,
		admin_buff
    }
}
