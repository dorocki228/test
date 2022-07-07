package l2s.gameserver.instancemanager;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.security.HwidUtils;
import l2s.gameserver.utils.ItemFunctions;

public class PvPRewardManager
{
	private static final String PVP_REWARD_VAR = "@pvp_manager";
	private static final boolean no_msg;

	private static boolean basicCheck(Player killed, Player killer)
	{
		if(killed == null || killer == null)
			return false;
		if(killed.getLevel() < Config.PVP_REWARD_MIN_PL_LEVEL)
		{
			if(!no_msg)
				if(killer.isLangRus())
					killer.sendMessage("\u0421\u0438\u0441\u0442\u0435\u043c\u0430 PvP: \u0412\u044b \u0443\u0431\u0438\u043b\u0438 \u0438\u0433\u0440\u043e\u043a\u0430 \u043d\u043e \u0443\u0440\u043e\u0432\u0435\u043d\u044c \u043f\u0440\u043e\u0442\u0438\u0432\u043d\u0438\u043a\u0430 \u043d\u0435 \u043f\u043e\u0434\u0445\u043e\u0434\u0438\u0442. \u041c\u0438\u043d\u0438\u043c\u0430\u043b\u044c\u043d\u044b\u0439 \u0443\u0440\u043e\u0432\u0435\u043d\u044c: " + Config.PVP_REWARD_MIN_PL_LEVEL);
				else
					killer.sendMessage("PvP System: You killed a player, but his level is too low. Suggested level: " + Config.PVP_REWARD_MIN_PL_LEVEL);
			return false;
		}
		if(killed.getClassLevel().ordinal() < Config.PVP_REWARD_MIN_PL_PROFF)
		{
			if(!no_msg)
				if(killer.isLangRus())
					killer.sendMessage("\u0421\u0438\u0441\u0442\u0435\u043c\u0430 PvP: \u0412\u044b \u0443\u0431\u0438\u043b\u0438 \u0438\u0433\u0440\u043e\u043a\u0430 \u043d\u043e \u0435\u0433\u043e \u043f\u0440\u043e\u0444\u0435\u0441\u0441\u0438\u044f \u0441\u043b\u0438\u0448\u043a\u043e\u043c \u043d\u0438\u0437\u043a\u0430\u044f. \u041c\u0438\u043d\u0438\u043c\u0430\u043b\u044c\u043d\u044b\u0439 \u0443\u0440\u043e\u0432\u0435\u043d\u044c \u043f\u0440\u043e\u0444\u0435\u0441\u0441\u0438\u0438: " + (Config.PVP_REWARD_MIN_PL_PROFF - 1));
				else
					killer.sendMessage("PvP System: You killed a player, but his job level is too low. Suggested job level: " + (Config.PVP_REWARD_MIN_PL_PROFF - 1));
			return false;
		}
		if(System.currentTimeMillis() - killer.getLastAccess() < Config.PVP_REWARD_MIN_PL_UPTIME_MINUTE * 60000)
		{
			if(!no_msg)
				if(killer.isLangRus())
					killer.sendMessage("\u0421\u0438\u0441\u0442\u0435\u043c\u0430 PvP: \u0412\u044b \u0443\u0431\u0438\u043b\u0438 \u0438\u0433\u0440\u043e\u043a\u0430 \u043d\u043e \u0432\u044b \u043d\u0435 \u0434\u043e\u0441\u0442\u0430\u0442\u043e\u0447\u043d\u043e \u043f\u0440\u043e\u0432\u0435\u043b\u0438 \u0432\u0440\u0435\u043c\u044f \u0432 \u0438\u0433\u0440\u0435, \u043c\u0438\u043d\u0438\u043c\u0430\u043b\u044c\u043d\u043e\u0435 \u0432\u0440\u0435\u043c\u044f \u0432 \u0438\u0433\u0440\u0435: " + Config.PVP_REWARD_MIN_PL_UPTIME_MINUTE + " \u043c\u0438\u043d.");
				else
					killer.sendMessage("PvP System: You killed a player, but you spent a little time ingame. Suggested minimal ingame time: " + Config.PVP_REWARD_MIN_PL_UPTIME_MINUTE + " min.");
			return false;
		}
		if(System.currentTimeMillis() - killed.getLastAccess() < Config.PVP_REWARD_MIN_PL_UPTIME_MINUTE * 60000)
		{
			if(!no_msg)
				if(killer.isLangRus())
					killer.sendMessage("\u0421\u0438\u0441\u0442\u0435\u043c\u0430 PvP: \u0412\u044b \u0443\u0431\u0438\u043b\u0438 \u0438\u0433\u0440\u043e\u043a\u0430 \u043d\u043e \u043e\u043d \u043d\u0435 \u0434\u043e\u0441\u0442\u0430\u0442\u043e\u0447\u043d\u043e \u0432\u0440\u0435\u043c\u044f \u0432 \u0438\u0433\u0440\u0435, \u043c\u0438\u043d\u0438\u043c\u0430\u043b\u044c\u043d\u043e\u0435 \u0432\u0440\u0435\u043c\u044f \u0432 \u0438\u0433\u0440\u0435: " + Config.PVP_REWARD_MIN_PL_UPTIME_MINUTE + " \u043c\u0438\u043d.");
				else
					killer.sendMessage("PvP System: You killed a player, but he has spent a little time ingame. Suggested minimal ingame time: " + Config.PVP_REWARD_MIN_PL_UPTIME_MINUTE + " min.");
			return false;
		}
		if(!Config.PVP_REWARD_PK_GIVE && killer.isPK())
		{
			if(!no_msg)
				if(killer.isLangRus())
					killer.sendMessage("\u0421\u0438\u0441\u0442\u0435\u043c\u0430 PvP: \u0412\u044b \u0443\u0431\u0438\u043b\u0438 \u0438\u0433\u0440\u043e\u043a\u0430 \u043d\u043e \u041f\u041a \u0443\u0431\u0438\u0439\u0441\u0442\u0432\u0430 \u0437\u0430\u043f\u0440\u0435\u0449\u0435\u043d\u044b.");
				else
					killer.sendMessage("PvP System: You killed a player, but PK kills are disallowed");
			return false;
		}
		if(!Config.PVP_REWARD_ON_EVENT_GIVE && killer.getTeam() != TeamType.NONE)
		{
			if(!no_msg)
				if(killer.isLangRus())
					killer.sendMessage("\u0421\u0438\u0441\u0442\u0435\u043c\u0430 PvP: \u0412\u044b \u0443\u0431\u0438\u043b\u0438 \u0438\u0433\u0440\u043e\u043a\u0430 \u043d\u043e \u0443\u0431\u0438\u0439\u0441\u0442\u0432\u0430 \u043d\u0430 \u0442\u0443\u0440\u043d\u0438\u0440\u0430\u0445 \u043d\u0435 \u0437\u0430\u0441\u0447\u0438\u0442\u044b\u0432\u0430\u0442\u044c\u0441\u044f.");
				else
					killer.sendMessage("PvP System: You killed a player, but event kills won't count");
			return false;
		}
		if(Config.PVP_REWARD_ONLY_BATTLE_ZONE && (!killer.isInZone(Zone.ZoneType.battle_zone) || !killed.isInZone(Zone.ZoneType.battle_zone)))
		{
			if(!no_msg)
				if(killer.isLangRus())
					killer.sendMessage("\u0421\u0438\u0441\u0442\u0435\u043c\u0430 PvP: \u0412\u044b \u0443\u0431\u0438\u043b\u0438 \u0438\u0433\u0440\u043e\u043a\u0430 \u043d\u043e \u0440\u0430\u0437\u0440\u0435\u0448\u0435\u043d\u043e \u0443\u0431\u0438\u0432\u0430\u0442\u044c \u0438\u0433\u0440\u043e\u043a\u043e\u0432 \u0442\u043e\u043b\u044c\u043a\u043e \u043d\u0430 \u0431\u043e\u0435\u0432\u044b\u0445 \u043f\u043b\u043e\u0449\u0430\u0434\u043a\u0430\u0445.");
				else
					killer.sendMessage("PvP System: You killed a player, it's allowed to kill players only on battle fields.");
			return false;
		}
		if(!Config.PVP_REWARD_SAME_PARTY_GIVE && killer.getParty() != null && killer.getParty() == killed.getParty() && (killer.getParty().getCommandChannel() == null || killer.getParty().getCommandChannel() == killed.getParty().getCommandChannel()))
		{
			if(!no_msg)
				if(killer.isLangRus())
					killer.sendMessage("\u0421\u0438\u0441\u0442\u0435\u043c\u0430 PvP: \u0412\u044b \u0443\u0431\u0438\u043b\u0438 \u0438\u0433\u0440\u043e\u043a\u0430 \u043d\u043e \u0432\u044b \u043d\u0430\u0445\u043e\u0434\u0438\u0442\u0435\u0441\u044c \u0432 \u043e\u0434\u043d\u043e\u0439 \u043f\u0430\u0440\u0442\u0438\u0438, \u0447\u0442\u043e \u0437\u0430\u043f\u0440\u0435\u0449\u0435\u043d\u043e.");
				else
					killer.sendMessage("PvP System: You killed a player, but you both are in the same party, it's not allowed");
			return false;
		}
		if(!Config.PVP_REWARD_SAME_CLAN_GIVE && killer.getClan() != null && killer.getClan() == killed.getClan())
		{
			if(!no_msg)
				if(killer.isLangRus())
					killer.sendMessage("\u0421\u0438\u0441\u0442\u0435\u043c\u0430 PvP: \u0412\u044b \u0443\u0431\u0438\u043b\u0438 \u0438\u0433\u0440\u043e\u043a\u0430 \u043d\u043e \u0432\u044b \u043d\u0430\u0445\u043e\u0434\u0438\u0442\u0435\u0441\u044c \u0432 \u043e\u0434\u043d\u043e\u0439 \u043a\u043b\u0430\u043d\u0435 , \u0447\u0442\u043e \u0437\u0430\u043f\u0440\u0435\u0449\u0435\u043d\u043e.");
				else
					killer.sendMessage("PvP System: You killed a player, but you both are in the same clan, it's not allowed");
			return false;
		}
		if(!Config.PVP_REWARD_SAME_ALLY_GIVE && killer.getAllyId() > 0 && killer.getAllyId() == killed.getAllyId())
		{
			if(!no_msg)
				if(killer.isLangRus())
					killer.sendMessage("\u0421\u0438\u0441\u0442\u0435\u043c\u0430 PvP: \u0412\u044b \u0443\u0431\u0438\u043b\u0438 \u0438\u0433\u0440\u043e\u043a\u0430 \u043d\u043e \u0432\u044b \u043d\u0430\u0445\u043e\u0434\u0438\u0442\u0435\u0441\u044c \u0432 \u043e\u0434\u043d\u043e\u043c \u0430\u043b\u044c\u044f\u043d\u0441\u0435, \u0447\u0442\u043e \u0437\u0430\u043f\u0440\u0435\u0449\u0435\u043d\u043e.");
				else
					killer.sendMessage("PvP System: You killed a player, but you both are in the same alliance, it's not allowed");
			return false;
		}
		if(!Config.PVP_REWARD_SAME_HWID_GIVE && HwidUtils.INSTANCE.isSameHWID(killer, killed))
		{
			if(!no_msg)
				if(killer.isLangRus())
					killer.sendMessage("\u0421\u0438\u0441\u0442\u0435\u043c\u0430 PvP: \u0412\u044b \u0443\u0431\u0438\u043b\u0438 \u0438\u0433\u0440\u043e\u043a\u0430 \u043d\u043e \u0441\u0438\u0441\u0442\u0435\u043c\u0430 \u043e\u043f\u0440\u0435\u0434\u0435\u043b\u0438\u043b\u0430 \u0447\u0442\u043e \u044d\u0442\u043e \u0432\u0430\u0448\u0435 \u0434\u0440\u0443\u0433\u043e\u0435 \u043e\u043a\u043d\u043e, \u044d\u0442\u043e \u0437\u0430\u043f\u0440\u0435\u0449\u0435\u043d\u043e!");
				else
					killer.sendMessage("PvP System: You killed a player, but it seems you both playing from the same PC, it's not allowed");
			return false;
		}
		if(!Config.PVP_REWARD_SAME_IP_GIVE && killer.getIP().equals(killed.getIP()))
		{
			if(!no_msg)
				if(killer.isLangRus())
					killer.sendMessage("\u0421\u0438\u0441\u0442\u0435\u043c\u0430 PvP: \u0412\u044b \u0443\u0431\u0438\u043b\u0438 \u0438\u0433\u0440\u043e\u043a\u0430 \u043d\u043e \u0432\u0430\u0448\u0438 \u0418\u041f \u0441\u043e\u0432\u043f\u0430\u0434\u0430\u044e\u0442, \u0447\u0442\u043e \u0437\u0430\u043f\u0440\u0435\u0449\u0435\u043d\u043e.");
				else
					killer.sendMessage("PvP System: You killed a player, but your IP are the same, it's not allowed!");
			return false;
		}
		if(Config.PVP_REWARD_SPECIAL_ANTI_TWINK_TIMER && System.currentTimeMillis() - killed.getCreateTime() < Config.PVP_REWARD_HR_NEW_CHAR_BEFORE_GET_ITEM * 60000 * 60)
		{
			if(!no_msg)
				if(killer.isLangRus())
					killer.sendMessage("\u0421\u0438\u0441\u0442\u0435\u043c\u0430 PvP: \u0412\u044b \u0443\u0431\u0438\u043b\u0438 \u0438\u0433\u0440\u043e\u043a\u0430 \u043d\u043e \u0447\u0430\u0440 \u0431\u044b\u043b \u0441\u043e\u0437\u0434\u0430\u043d \u043d\u0435\u0434\u0430\u0432\u043d\u043e, \u0447\u0442\u043e \u043d\u0435 \u0440\u0430\u0437\u0440\u0435\u0448\u0435\u043d\u043e \u0441\u0438\u0441\u0442\u0435\u043c\u043e\u0439. \u0427\u0430\u0440 \u0434\u043e\u043b\u0436\u0435\u043d \u0431\u044b\u0442\u044c \u0441\u043e\u0437\u0434\u0430\u043d \u043d\u0435 \u043c\u0435\u043d\u0435\u0435: " + Config.PVP_REWARD_HR_NEW_CHAR_BEFORE_GET_ITEM + " \u0447\u0430\u0441\u043e\u0432 \u043d\u0430\u0437\u0430\u0434.");
				else
					killer.sendMessage("PvP System: You killed a player, but char has been created really short time ago, suggested char creation, not less than " + Config.PVP_REWARD_HR_NEW_CHAR_BEFORE_GET_ITEM + " hours!");
			return false;
		}
		if(Config.PVP_REWARD_CHECK_EQUIP && !checkEquip(killed))
		{
			if(!no_msg)
				if(killer.isLangRus())
					killer.sendMessage("\u0421\u0438\u0441\u0442\u0435\u043c\u0430 PvP: \u0412\u044b \u0443\u0431\u0438\u043b\u0438 \u0438\u0433\u0440\u043e\u043a\u0430 \u043d\u043e \u0435\u0433\u043e \u044d\u043a\u0438\u043f\u0438\u0440\u043e\u0432\u043a\u0430 \u0441\u043b\u0438\u0448\u043a\u043e\u043c \u043f\u043b\u043e\u0445\u0430.");
				else
					killer.sendMessage("PvP System: You killed a player, but his equip is very low.");
			return false;
		}
		return true;
	}

	private static boolean checkEquip(Player killed)
	{
		if(killed.getWeaponsExpertisePenalty() > 0 || killed.getArmorsExpertisePenalty() > 0)
			return false;
		ItemInstance weapon = killed.getActiveWeaponInstance();
		return weapon != null && weapon.getGrade().extOrdinal() >= Config.PVP_REWARD_WEAPON_GRADE_TO_CHECK;
	}

	public static void tryGiveReward(Player victim, Player player)
	{
		if(!Config.ALLOW_PVP_REWARD)
			return;
		if(!isNoDelayActive(victim, player))
		{
			if(player.isLangRus())
				player.sendMessage("\u0421\u0438\u0441\u0442\u0435\u043c\u0430 PvP: \u0412\u044b \u0443\u0431\u0438\u043b\u0438 \u0438\u0433\u0440\u043e\u043a\u0430 \u044d\u0442\u043e\u0433\u043e \u0438\u0433\u0440\u043e\u043a\u0430 \u0441\u043e\u0432\u0441\u0435\u043c \u043d\u0435\u0434\u0430\u0432\u043d\u043e! \u0415\u0449\u0435 \u043d\u0435 \u043f\u0440\u043e\u0448\u043b\u043e \u0432\u0440\u0435\u043c\u044f \u0434\u043e \u0432\u043e\u0437\u043c\u043e\u0436\u043d\u043e\u0441\u0442\u0438 \u043f\u043e\u0432\u0442\u043e\u0440\u043d\u043e\u0433\u043e \u0443\u0431\u0438\u0439\u0441\u0442\u0432\u0430.");
			else
				player.sendMessage("PvP System: You killed the player that player recently! Yet as time passed up the possibility of re-killing.");
			return;
		}
		if(!basicCheck(victim, player))
			return;
		victim.setVar("@pvp_manager_" + player.getObjectId(), true, System.currentTimeMillis() + Config.PVP_REWARD_DELAY_ONE_KILL * 1000);
		giveItem(player);
		if(Config.PVP_REWARD_SEND_SUCC_NOTIF)
		{
			if(victim.isLangRus())
				victim.sendMessage("\u0421\u0438\u0441\u0442\u0435\u043c\u0430 PvP: \u0412\u0430\u0441 \u0443\u0431\u0438\u043b\u0438!");
			else
				victim.sendMessage("PvP System: You killed!");
			if(player.isLangRus())
				player.sendMessage("\u0421\u0438\u0441\u0442\u0435\u043c\u0430 PvP: \u0412\u044b \u0443\u0431\u0438\u043b\u0438 \u0438\u0433\u0440\u043e\u043a\u0430!");
			else
				player.sendMessage("PvP System: You killed player!");
		}
	}

	private static void giveItem(Player player)
	{
		if(player == null)
			return;
		if(Config.PVP_REWARD_REWARD_IDS.length != Config.PVP_REWARD_COUNTS.length)
			return;
		if(Config.PVP_REWARD_RANDOM_ONE)
		{
			int index = Rnd.get(Config.PVP_REWARD_REWARD_IDS.length);
			int rewardId = Config.PVP_REWARD_REWARD_IDS[index];
			long rewardCount = Config.PVP_REWARD_COUNTS[index];
			if(rewardId > 0 && rewardCount > 0L)
				ItemFunctions.addItem(player, rewardId, rewardCount, true);
		}
		else
			for(int i = 0; i < Config.PVP_REWARD_REWARD_IDS.length - 1; ++i)
			{
				int rewardId = Config.PVP_REWARD_REWARD_IDS[i];
				long rewardCount = Config.PVP_REWARD_COUNTS[i];
				if(rewardId > 0 && rewardCount > 0L)
					ItemFunctions.addItem(player, Config.PVP_REWARD_REWARD_IDS[i], Config.PVP_REWARD_COUNTS[i], true);
			}
	}

	private static boolean isNoDelayActive(Player victim, Player killer)
	{
		String delay = victim.getVar("@pvp_manager_" + killer.getObjectId());
		return delay == null;
	}

	static
	{
		no_msg = Config.DISALLOW_MSG_TO_PL;
	}
}
