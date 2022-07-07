package l2s.gameserver.model.instances;

import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.AcquireType;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.entity.residence.Residence;
import l2s.gameserver.model.pledge.*;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.PledgeShowInfoUpdatePacket;
import l2s.gameserver.tables.ClanTable;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Util;

import java.util.concurrent.TimeUnit;

public class VillageMasterPledgeBypasses
{
	public static void createClan(NpcInstance npc, Player player, String clanName)
	{
		if(player.getLevel() < 10 || player.getClan() != null)
		{
			player.sendPacket(SystemMsg.YOU_DO_NOT_MEET_THE_CRITERIA_IN_ORDER_TO_CREATE_A_CLAN);
			return;
		}
		if(player.isMercenary()) {
			player.sendMessage(new CustomMessage("mercenary.s3"));
			return;
		}
		if(!player.canCreateClan())
		{
			player.sendPacket(SystemMsg.YOU_MUST_WAIT_10_DAYS_BEFORE_CREATING_A_NEW_CLAN);
			return;
		}
		if(clanName.length() > 16)
		{
			player.sendPacket(SystemMsg.CLAN_NAMES_LENGTH_IS_INCORRECT);
			return;
		}
		if(!Util.isMatchingRegexp(clanName, Config.CLAN_NAME_TEMPLATE))
		{
			player.sendPacket(SystemMsg.CLAN_NAME_IS_INVALID);
			return;
		}
        if(Config.RESTRICTED_CHAR_CLAN_NAME_ENABLE && Config.RESTRICTED_CHAR_CLAN_NAME.matcher(clanName).find())
        {
            player.sendPacket(SystemMsg.CLAN_NAME_IS_INVALID);
            return;
        }

		Clan clan = ClanTable.getInstance().createClan(player, clanName);
		if(clan == null)
		{
			player.sendPacket(SystemMsg.THIS_NAME_ALREADY_EXISTS);
			return;
		}
		player.sendPacket(clan.listAll());
		player.sendPacket(new PledgeShowInfoUpdatePacket(clan));
		player.updatePledgeRank();
		player.broadcastCharInfo();
		npc.showChatWindow(player, "pledge/pl006.htm", false);
	}

	public static void showClanSkillList(NpcInstance npc, Player player)
	{
		if(!player.isClanLeader())
		{
			npc.showChatWindow(player, "pledge/pl017.htm", false);
			return;
		}
		if(player.getClan().getLevel() == 0)
		{
			npc.showChatWindow(player, "pledge/pl_err_plv.htm", false);
			return;
		}
		NpcInstance.showAcquireList(AcquireType.CLAN, player);
	}

	public static void levelUpClan(NpcInstance npc, Player player)
	{
		if(!checkPlayerForClanLeader(npc, player))
			return;
		Clan clan = player.getClan();
		if(clan.isPlacedForDisband())
		{
			player.sendPacket(SystemMsg.AS_YOU_ARE_CURRENTLY_SCHEDULE_FOR_CLAN_DISSOLUTION_YOUR_CLAN_LEVEL_CANNOT_BE_INCREASED);
			return;
		}
		if(Config.CLAN_MAX_LEVEL <= clan.getLevel())
		{
			npc.showChatWindow(player, "pledge/pl016.htm", false);
			return;
		}
		int nextClanLevel = clan.getLevel() + 1;
		int spCost = Config.CLAN_LVL_UP_SP_COST[nextClanLevel];
		int rpCost = Config.CLAN_LVL_UP_RP_COST[nextClanLevel];
		int minMembers = Config.CLAN_LVL_UP_MIN_MEMBERS[nextClanLevel];
		long[][][] itemsRequiredVariations = Config.CLAN_LVL_UP_ITEMS_REQUIRED[nextClanLevel];
		boolean needCastle = Config.CLAN_LVL_UP_NEED_CASTLE[nextClanLevel];
		if((spCost == 0 || player.getSp() >= spCost) && (rpCost == 0 || clan.getReputationScore() >= rpCost) && clan.getAllSize() >= minMembers && (!needCastle || clan.getCastle() != 0))
		{
			player.getInventory().writeLock();
			try
			{
				long[][] itemsRequired = null;
				int i = 0;
				Label_0161: while(i < itemsRequiredVariations.length)
				{
					itemsRequired = itemsRequiredVariations[i];
					if(itemsRequired == null)
						itemsRequired = new long[0][];
					for(int j = 0; j < itemsRequired.length; ++j)
					{
						int itemId = (int) itemsRequired[j][0];
						long itemsCount = itemsRequired[j][1];
						if(itemId > 0 && itemsCount > 0L && ItemFunctions.getItemCount(player, itemId) < itemsCount)
						{
							itemsRequired = null;
							++i;
							continue Label_0161;
						}
					}
					break;
				}
				if(itemsRequired == null)
				{
					npc.showChatWindow(player, "pledge/pl_err_not_enough_items.htm", false);
					return;
				}
				for(i = 0; i < itemsRequired.length; ++i)
				{
					int itemId = (int) itemsRequired[i][0];
					long itemsCount = itemsRequired[i][1];
					if(itemId > 0 && itemsCount > 0L)
						ItemFunctions.deleteItem(player, itemId, itemsCount, true);
				}
			}
			finally
			{
				player.getInventory().writeUnlock();
			}
			if(spCost > 0)
				player.setSp(player.getSp() - spCost);
			if(rpCost > 0)
				clan.incReputation(-rpCost, false, "LvlUpClan");
			int oldLevel = clan.getLevel();
			clan.setLevel(nextClanLevel);
			clan.updateClanInDB();
			npc.doCast(SkillHolder.getInstance().getSkillEntry(5103, 1), player, true);
			clan.onLevelChange(oldLevel, clan.getLevel());
		}
		else
			npc.showChatWindow(player, "pledge/pl016.htm", false);
	}

	public static boolean checkPlayerForClanLeader(NpcInstance npc, Player player)
	{
		if(player.getClan() == null)
		{
			npc.showChatWindow(player, "pledge/pl_no_pledgeman.htm", false);
			return false;
		}
		if(!player.isClanLeader())
		{
			npc.showChatWindow(player, "pledge/pl_err_master.htm", false);
			return false;
		}
		return true;
	}

	protected static void dissolveClan(NpcInstance npc, Player player)
	{
		if(player == null || player.getClan() == null)
			return;
		Clan clan = player.getClan();
		if(!player.isClanLeader())
		{
			player.sendPacket(SystemMsg.ONLY_THE_CLAN_LEADER_IS_ENABLED);
			return;
		}
		if(clan.isPlacedForDisband())
		{
			player.sendPacket(SystemMsg.YOU_HAVE_ALREADY_REQUESTED_THE_DISSOLUTION_OF_YOUR_CLAN);
			return;
		}
		if(!clan.canDisband())
		{
			player.sendPacket(SystemMsg.YOU_CANNOT_APPLY_FOR_DISSOLUTION_AGAIN_WITHIN_SEVEN_DAYS_AFTER_A_PREVIOUS_APPLICATION_FOR_DISSOLUTION);
			return;
		}
		if(clan.getAllyId() != 0)
		{
			player.sendPacket(SystemMsg.YOU_CANNOT_DISPERSE_THE_CLANS_IN_YOUR_ALLIANCE);
			return;
		}
		if(clan.isAtWar())
		{
			player.sendPacket(SystemMsg.YOU_CANNOT_DISSOLVE_A_CLAN_WHILE_ENGAGED_IN_A_WAR);
			return;
		}
		if(clan.getCastle() != 0 || clan.getHasHideout() != 0)
		{
			player.sendPacket(SystemMsg.UNABLE_TO_DISSOLVE_YOUR_CLAN_OWNS_ONE_OR_MORE_CASTLES_OR_HIDEOUTS);
			return;
		}
		for(Residence r : ResidenceHolder.getInstance().getResidences())
		{
			SiegeEvent<?, ?> siegeEvent = r.getSiegeEvent();
			if(siegeEvent == null)
				continue;
			if(siegeEvent.getSiegeClan("attackers", clan) != null || siegeEvent.getSiegeClan("defenders", clan) != null || siegeEvent.getSiegeClan("defenders_waiting", clan) != null)
			{
				player.sendPacket(SystemMsg.UNABLE_TO_DISSOLVE_YOUR_CLAN_HAS_REQUESTED_TO_PARTICIPATE_IN_A_CASTLE_SIEGE);
				return;
			}
		}
		clan.placeForDisband();
		clan.broadcastClanStatus(true, true, false);
		npc.showChatWindow(player, "pledge/pl009.htm", false);
	}

	public static void restoreClan(VillageMasterInstance npc, Player player)
	{
		if(!checkPlayerForClanLeader(npc, player))
			return;
		Clan clan = player.getClan();
		if(!clan.isPlacedForDisband())
		{
			player.sendPacket(SystemMsg.THERE_ARE_NO_REQUESTS_TO_DISPERSE);
			return;
		}
		clan.unPlaceDisband();
		clan.broadcastClanStatus(true, true, false);
		npc.showChatWindow(player, "pledge/pl012.htm", false);
	}

	protected static boolean createAlly(Player player, String allyName)
	{
		if(!player.isClanLeader())
		{
			player.sendPacket(SystemMsg.ONLY_CLAN_LEADERS_MAY_CREATE_ALLIANCES);
			return false;
		}
		if(player.getClan().getAllyId() != 0)
		{
			player.sendPacket(SystemMsg.YOU_ALREADY_BELONG_TO_ANOTHER_ALLIANCE);
			return false;
		}
		if(player.getClan().isPlacedForDisband())
		{
			player.sendPacket(SystemMsg.AS_YOU_ARE_CURRENTLY_SCHEDULE_FOR_CLAN_DISSOLUTION_NO_ALLIANCE_CAN_BE_CREATED);
			return false;
		}
		if(allyName.length() > 16)
		{
			player.sendPacket(SystemMsg.INCORRECT_LENGTH_FOR_AN_ALLIANCE_NAME);
			return false;
		}
		if(!Util.isMatchingRegexp(allyName, Config.ALLY_NAME_TEMPLATE))
		{
			player.sendPacket(SystemMsg.INCORRECT_ALLIANCE_NAME__PLEASE_TRY_AGAIN);
			return false;
		}
		if(player.getClan().getLevel() < 5)
		{
			player.sendPacket(SystemMsg.TO_CREATE_AN_ALLIANCE_YOUR_CLAN_MUST_BE_LEVEL_5_OR_HIGHER);
			return false;
		}
		if(ClanTable.getInstance().getAllyByName(allyName) != null)
		{
			player.sendPacket(SystemMsg.THAT_ALLIANCE_NAME_ALREADY_EXISTS);
			return false;
		}
		if(!player.getClan().canCreateAlly())
		{
			player.sendPacket(SystemMsg.YOU_CANNOT_CREATE_A_NEW_ALLIANCE_WITHIN_1_DAY_OF_DISSOLUTION);
			return false;
		}
		Alliance alliance = ClanTable.getInstance().createAlliance(player, allyName);
		if(alliance == null)
			return false;
		player.broadcastCharInfo();
		return true;
	}

	public static boolean changeLeader(NpcInstance npc, Player player, int pledgeId, String leaderName)
	{
		if(!checkPlayerForClanLeader(npc, player))
			return false;
		Clan clan = player.getClan();
		SubUnit mainUnit = clan.getSubUnit(0);
		SubUnit subUnit = clan.getSubUnit(pledgeId);
		UnitMember subLeader = mainUnit.getUnitMember(leaderName);
		if(subUnit == null || subLeader == null || subLeader.isLeaderOf() != -128)
		{
			npc.showChatWindow(player, "pledge/pl_err_man.htm", false);
			return false;
		}
		if(pledgeId == 0)
		{
			ClanChangeLeaderRequest request = ClanTable.getInstance().getRequest(clan.getClanId());
			if(request != null)
			{
				npc.showChatWindow(player, "pledge/pl_transfer_already.htm", false);
				return false;
			}
			if(Config.CLAN_CHANGE_LEADER_TIME_SECOND != -1) {
				request = new ClanChangeLeaderRequest(clan.getClanId(), subLeader.getObjectId(),
						System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(Config.CLAN_CHANGE_LEADER_TIME_SECOND));
			}
			else {
				request = new ClanChangeLeaderRequest(clan.getClanId(), subLeader.getObjectId(), Clan.CHANGE_LEADER_TIME_PATTERN.next(System.currentTimeMillis()));
			}
			ClanTable.getInstance().addRequest(request);
			npc.showChatWindow(player, "pledge/pl_transfer_success.htm", false);
		}
		return true;
	}

	public static void cancelLeaderChange(VillageMasterInstance npc, Player player)
	{
		if(!checkPlayerForClanLeader(npc, player))
			return;
		Clan clan = player.getClan();
		ClanChangeLeaderRequest request = ClanTable.getInstance().getRequest(clan.getClanId());
		if(request == null)
		{
			npc.showChatWindow(player, "pledge/pl_not_transfer.htm", false);
			return;
		}
		ClanTable.getInstance().cancelRequest(request, false);
		npc.showChatWindow(player, "pledge/pl_cancel_success.htm", false);
	}
}
