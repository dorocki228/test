package handler.voicecommands;

import com.google.common.primitives.Ints;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.BuyListHolder;
import l2s.gameserver.data.xml.holder.MultiSellHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2s.gameserver.listener.script.OnInitScriptListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.model.entity.events.impl.SingleMatchEvent;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.network.l2.s2c.ExBuySellListPacket;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.templates.npc.BuyListTemplate;
import l2s.gameserver.utils.Functions;
import l2s.gameserver.utils.HtmlUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author KanuToIIIKa
 */
public class Obt implements IVoicedCommandHandler, OnInitScriptListener {
	private String[] COMMANDS = {"obt", "setlevel", "addskills", "deleteskills", "shop", "mageset", "warriorset", "regen", "clanlvl", "multisell"};
	private static final int[] multisellValidationIds = new int[]{1041};
	private int[] mageset = {
			1040,
			1389,
			1059,
			1062,
			1078,
			1085,
			1204,
			1303,
			1036,
			4347,
			4395,
			1035,
			1397,
			1389,
			273,
			276,
			267,
			268,
			264,
			304,
			349,
			363,
			365,
			1414,
			1461};
	private int[] warriorset = {
			1397,
			1035,
			1040,
			1068,
			1062,
			1077,
			1204,
			1268,
			1036,
			4347,
			1086,
			1240,
			1242,
			1388,
			274,
			271,
			275,
			310,
			269,
			267,
			268,
			264,
			304,
			349,
			363,
			364,
			1363,
			1461};

	@Override
	public boolean useVoicedCommand(String command, Player player, String args) {
		if(player.isInCombat()) {
			player.sendMessage("You cannot use this command while being in fight.");
			return false;
		}

		if(player.containsEvent(SingleMatchEvent.class)) {
			player.sendMessage("You cannot use this command while being in event.");
			return false;
		}

		if(player.isInOlympiadMode() || Olympiad.isRegisteredInComp(player)) {
			player.sendMessage("You cannot use this command while being in olympiad.");
			return false;
		}

		if(command.equals("obt")) {
			StringBuilder sb = new StringBuilder();

			sb.append("<html><title>OBT Shop</title><body><center>");

//			sb.append(HtmlUtils.htmlButton("Weapons", "bypass -h user_shop 90001", 290));
//			sb.append(HtmlUtils.htmlButton("Armors", "bypass -h user_shop 90002", 290));
//			sb.append(HtmlUtils.htmlButton("Jewerly", "bypass -h user_shop 90003", 290));
//			sb.append(HtmlUtils.htmlButton("Consumables", "bypass -h user_shop 90004", 290));
//			sb.append(HtmlUtils.htmlButton("Scrolls", "bypass -h user_shop 90005", 290));
//			sb.append(HtmlUtils.htmlButton("Misc", "bypass -h user_shop 90006", 290) + "<br>");
			sb.append(HtmlUtils.htmlButton("Class Master", "bypass -h user_cm", 290) + "<br>");
			sb.append(HtmlUtils.htmlButton("Give 5 Lvl clan", "bypass -h user_clanlvl", 290) + "<br>");
			sb.append("<edit var=\"level\" length=\"2\" width=270 height=18>");
			sb.append(HtmlUtils.htmlButton("Set Level", "bypass -h user_setlevel $level", 290) + "<br>");
			sb.append(HtmlUtils.htmlButton("OBT Shop", "bypass -h user_multisell 1041", 290) + "<br>");
//			sb.append(HtmlUtils.htmlButton("Give All Skills", "bypass -h user_addskills", 290));
//			sb.append(HtmlUtils.htmlButton("Remove All Skills", "bypass -h user_deleteskills", 290) + "<br>");
//			sb.append(HtmlUtils.htmlButton("Mage Buff Set", "bypass -h user_mageset", 290));
//			sb.append(HtmlUtils.htmlButton("Warrior Buff Set", "bypass -h user_warriorset", 290));
//			sb.append(HtmlUtils.htmlButton("Regen HP|MP|CP", "bypass -h user_regen", 290));

			sb.append("</center></body></html>");

			Functions.show(sb.toString(), player);
		}
		else if(command.equals("setlevel")) {

			if(StringUtils.isNumeric(args)) {
				int level = Math.min(80, Math.max(Integer.parseInt(args), 1));
				long exp_add = Experience.LEVEL[level] - player.getExp();
				player.addExpAndSp(exp_add, 0L, true);
				return true;
			}
		}
		else if(command.equals("addskills")) {
			player.rewardSkills(true, true, true);
			return true;
		}
		else if(command.equals("deleteskills")) {
			player.removeAllSkills();
			return true;
		}
		else if(command.equals("shop")) {
			Integer id = Ints.tryParse(args);
			if(id == null) {
				return false;
			}

			BuyListTemplate list = BuyListHolder.getInstance().getBuyList(-1, id);
			if(list != null) {
				player.sendPacket(new ExBuySellListPacket.BuyList(list, player, 0.0), new ExBuySellListPacket.SellRefundList(player, false, 0.0));
			}
			player.sendActionFailed();
		}
		else if(command.equals("mageset")) {
			getEffects(player, mageset);
			return true;
		}
		else if(command.equals("warriorset")) {
			getEffects(player, warriorset);
			return true;

		}
		else if(command.equals("regen")) {
			if(player.isInCombat()) {
				return false;
			}
			else {
				player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
				player.setCurrentCp(player.getMaxCp());
				return true;
			}
		}
		else if(command.equals("clanlvl")) {
			if(player.isClanLeader()) {
				player.getClan().setLevel(5);
				player.getClan().broadcastClanStatus(true, true, true);
			}
		}
		else if(command.equals("multisell")) {
			if(args == null) {
				return false;
			}
			int id = Integer.parseInt(args);
			if(!ArrayUtils.contains(multisellValidationIds, id)) {
				return false;
			}
			MultiSellHolder.getInstance().SeparateAndSend(id, player, 0, 0);
		}

		return false;
	}

	private void getEffects(Player activeChar, int[] set) {
		for(int id : set) {
			List<SkillEntry> skills = SkillHolder.getInstance().getSkills(id);
			if(!skills.isEmpty()) {
				SkillEntry maxSkill = skills.get(0);
				for(SkillEntry skill : skills) {
					if(skill.getLevel() > maxSkill.getLevel()) {
						maxSkill = skill;
					}
				}

				maxSkill.getEffects(activeChar, activeChar, 20 * 60000, 0);
			}
		}
	}

	@Override
	public String[] getVoicedCommandList() {
		return COMMANDS;
	}

	@Override
	public void onInit() {
		if(Config.ENABLE_OBT_COMMAND) {
			VoicedCommandHandler.getInstance().registerVoicedCommandHandler(this);
		}
		else {
			for(int multisellValidationId : multisellValidationIds) {
				MultiSellHolder.getInstance().remove(multisellValidationId);
			}
		}
	}
}
