package services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.Config;
import l2s.gameserver.dao.CharacterVariablesDAO;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.handler.bypass.Bypass;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.CharacterVariable;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.entity.events.impl.SingleMatchEvent;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.entity.residence.Fortress;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.UnitMember;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.service.FactionLeaderService;
import l2s.gameserver.service.FractionService;
import l2s.gameserver.service.MercenaryService;
import l2s.gameserver.service.PlayerService;
import l2s.gameserver.tables.ClanTable;
import l2s.gameserver.utils.Functions;
import l2s.gameserver.utils.ItemFunctions;

public class ChangeFraction {
	private static final String LAST_CHANGE_VAR = "fraction_last_change";
	private static final String CLAN_LAST_CHANGE_VAR = "clan_fraction_last_change";

	@Bypass("services.ChangeFraction:personal")
	public void personal(Player player, NpcInstance npc, String[] param) {
		Clan clan = player.getClan();


		Fraction fraction = player.getFraction();
		int percentage = FractionService.getInstance().getFractionPlayersCountPercentage(fraction);

		int price = Config.GVE_FRACTION_CHANGE_PERSONAL;

		if(percentage >= 60) {
			price = 100;
		}
		else if(percentage <= 40) {
			price = 2000;
		}
		if(player.isMercenary()) {
			player.sendMessage(new CustomMessage("mercenary.s3"));
			Functions.show("scripts/services/change_fraction_requirements.htm", player, npc);
			return;
		}
		if(FactionLeaderService.getInstance().isFactionLeader(player)) {
			player.sendMessage(new CustomMessage("changeFraction.s11"));
			Functions.show("scripts/services/change_fraction_requirements.htm", player, npc);
			return;
		}
		if(clan != null) {
			player.sendMessage(new CustomMessage("changeFraction.s1"));
			Functions.show("scripts/services/change_fraction_requirements.htm", player, npc);
			return;
		}
		if(!ItemFunctions.haveItem(player, 57, price)) {
			player.sendMessage(new CustomMessage("changeFraction.s2"));
			Functions.show("scripts/services/change_fraction_requirements.htm", player, npc);
			return;
		}

		if(player.isRegisteredInEvent() || player.containsEvent(SingleMatchEvent.class) || PlayerService.getInstance().isPlayerRegisteredEvent(player)) {
			player.sendMessage(new CustomMessage("changeFraction.s3"));
			Functions.show("scripts/services/change_fraction_requirements.htm", player, npc);
			return;
		}
		if(player.isInOlympiadMode() || Olympiad.isRegisteredInComp(player)) {
			player.sendMessage(new CustomMessage("changeFraction.s4"));
			Functions.show("scripts/services/change_fraction_requirements.htm", player, npc);
			return;
		}
		if(player.isInParty()) {
			player.sendMessage(new CustomMessage("changeFraction.s5"));
			Functions.show("scripts/services/change_fraction_requirements.htm", player, npc);
			return;
		}
		long reuseTime = player.getVarLong(LAST_CHANGE_VAR, 0);
		long currentTime = System.currentTimeMillis() / 1000;
		if(reuseTime > currentTime) {
			long timeLeft = reuseTime - currentTime;
			int hours = (int) (timeLeft / 3600);
			int minutes = (int) ((timeLeft - hours * 3600) / 60);

			player.sendMessage(new CustomMessage("changeFraction.s6").addNumber(hours).addNumber(minutes));
			Functions.show("scripts/services/change_fraction_requirements.htm", player, npc);
			return;
		}
/*		if(FactionLeaderService.getInstance().isFactionLeader(player)) {
			player.sendMessage(new CustomMessage("changeFraction.s11"));
			Functions.show("scripts/services/change_fraction_requirements.htm", player, npc);
			return;
		}
		else if(FactionLeaderService.getInstance().isRequest(player)) {
			player.sendMessage(new CustomMessage("changeFraction.s12"));
			Functions.show("scripts/services/change_fraction_requirements.htm", player, npc);
			return;
		}
		else if(FactionLeaderService.getInstance().isVote(player)) {
			player.sendMessage(new CustomMessage("changeFraction.s13"));
			Functions.show("scripts/services/change_fraction_requirements.htm", player, npc);
			return;
		}*/
		if(percentage <= Config.FACTION_BALANCE_MIN_PERCENT_TO_CHANGE_PLAYER) {
			player.sendMessage(new CustomMessage("changeFraction.s7").addNumber(Config.FACTION_BALANCE_MIN_PERCENT_TO_CHANGE_PLAYER));
			Functions.show("scripts/services/change_fraction_requirements.htm", player, npc);
			return;
		}
		Fraction revertFraction = player.getFraction().revert();
		if(ItemFunctions.deleteItem(player, 57, price)) {
			if(player.isInParty()) {
				player.leaveParty();
			}
			long time = (System.currentTimeMillis() + TimeUnit.HOURS.toMillis(12)) / 1000;
			player.setVar(LAST_CHANGE_VAR, time);
			player.changeFraction(revertFraction);
			Functions.show("scripts/services/change_fraction_congratulation.htm", player, npc);
		}
	}

	@Bypass("services.ChangeFraction:clan")
	public void clan(Player player, NpcInstance npc, String[] param) {
		if(!player.isClanLeader()) {
			player.sendMessage(new CustomMessage("changeFraction.s8"));
			Functions.show("scripts/services/change_fraction_requirements.htm", player, npc);
			return;
		}

		Clan clan = player.getClan();

		Fraction fraction = player.getFraction();
		int percentage = FractionService.getInstance().getFractionPlayersCountPercentage(fraction);

		int price = Config.GVE_FRACTION_CHANGE_CLAN;

		if(percentage >= 60) {
			price = 2000;
		}
		else if(percentage <= 40) {
			price = 10000;
		}

		if(player.isMercenary()) {
			player.sendMessage(new CustomMessage("mercenary.s3"));
			Functions.show("scripts/services/change_fraction_requirements.htm", player, npc);
			return;
		}
		if(FactionLeaderService.getInstance().isFactionLeader(player)) {
			player.sendMessage(new CustomMessage("changeFraction.s11"));
			Functions.show("scripts/services/change_fraction_requirements.htm", player, npc);
			return;
		}

		if(!ItemFunctions.haveItem(player, 57, price)) {
			player.sendMessage(new CustomMessage("changeFraction.s2"));
			Functions.show("scripts/services/change_fraction_requirements.htm", player, npc);
			return;
		}

		long playerClanReuseTime = player.getVarLong(CLAN_LAST_CHANGE_VAR, 0);
		long currentTime = System.currentTimeMillis() / 1000;
		if(playerClanReuseTime > currentTime) {
			long timeLeft = playerClanReuseTime - currentTime;
			int hours = (int) (timeLeft / 3600);
			int minutes = (int) (timeLeft - hours * 3600) / 60;
			player.sendMessage(new CustomMessage("changeFraction.s9").addNumber(hours).addNumber(minutes));
			Functions.show("scripts/services/change_fraction_requirements.htm", player, npc);
			return;
		}
		if(player.isRegisteredInEvent() || player.containsEvent(SingleMatchEvent.class) || PlayerService.getInstance().isPlayerRegisteredEvent(player)) {
			player.sendMessage(new CustomMessage("changeFraction.s3"));
			Functions.show("scripts/services/change_fraction_requirements.htm", player, npc);
			return;
		}
		if(player.isInOlympiadMode() || Olympiad.isRegisteredInComp(player)) {
			player.sendMessage(new CustomMessage("changeFraction.s4"));
			Functions.show("scripts/services/change_fraction_requirements.htm", player, npc);
			return;
		}
		if(player.isInParty()) {
			player.sendMessage(new CustomMessage("changeFraction.s5"));
			Functions.show("scripts/services/change_fraction_requirements.htm", player, npc);
			return;
		}
		if(percentage < Config.FACTION_BALANCE_MIN_PERCENT_TO_CHANGE_CLAN) {
			player.sendMessage(new CustomMessage("changeFraction.s10").addNumber(Config.FACTION_BALANCE_MIN_PERCENT_TO_CHANGE_CLAN));
			Functions.show("scripts/services/change_fraction_requirements.htm", player, npc);
			return;
		}
		StringBuilder sb = new StringBuilder();
		List<Integer> objectIds = clan.getAllMembers().stream().filter(e -> !e.isOnline()).map(UnitMember::getObjectId).collect(Collectors.toList());
		List<String> types = Arrays.asList(LAST_CHANGE_VAR, CLAN_LAST_CHANGE_VAR, MercenaryService.MERCENARY_END_TIME_STAMP);

		Map<Integer, MultiValueSet<String>> vars = CharacterVariablesDAO.getInstance().getVarsFromPlayers(types, objectIds);

		for(UnitMember member : clan.getAllMembers()) {
			if(member.getObjectId() == player.getObjectId()) {
				continue;
			}
			if(FactionLeaderService.getInstance().isFactionLeader(member.getObjectId())) {
				sb.append(member.getName()).append("<br1>");
				continue;
			}
			long ct = System.currentTimeMillis() / 1000;
			if(member.isOnline()) {
				Player pmember = member.getPlayer();
				if(pmember.isRegisteredInEvent() || player.containsEvent(SingleMatchEvent.class) || PlayerService.getInstance().isPlayerRegisteredEvent(pmember)) {
					sb.append(pmember.getName()).append("<br1>");
					continue;
				}
				if(pmember.isInOlympiadMode() || Olympiad.isRegisteredInComp(pmember)) {
					sb.append(pmember.getName()).append("<br1>");
					continue;
				}
			}
			else {
				MultiValueSet<String> playerMap = vars.getOrDefault(member.getObjectId(), new MultiValueSet<>());
				long mercenaryEndTimeStamp = playerMap.getLong(MercenaryService.MERCENARY_END_TIME_STAMP, 0);
				if(mercenaryEndTimeStamp > ct) {
					sb.append(member.getName()).append("<br1>");
					continue;
				}
			}
		}

		if(sb.length() != 0) {
			//Тут в штмл указать переменную %members%, в ней будут ники тех игроков которые не подходят под проверку
			Functions.show("scripts/services/change_fraction_requirements_clan.htm", player, npc, "%members%", sb.toString());
			return;
		}
		new ArrayList<>(clan.getClanWars()).forEach(cw -> {
			cw.getAttackerClan().removeClanWar(cw);
			cw.getAttackerClan().broadcastClanStatus(true, true, true);
			cw.getOpposingClan().removeClanWar(cw);
			cw.getOpposingClan().broadcastClanStatus(true, true, true);
			cw.getAttackerClan().broadcastToOnlineMembers(new SystemMessagePacket(SystemMsg.WAR_WITH_THE_S1_CLAN_HAS_ENDED).addString(cw.getOpposingClan().getName()));
			cw.getOpposingClan().broadcastToOnlineMembers(new SystemMessagePacket(SystemMsg.WAR_WITH_THE_S1_CLAN_HAS_ENDED).addString(cw.getAttackerClan().getName()));
			ClanTable.getInstance().deleteClanWar(cw);
		});
		clan.getClanWars().clear();

		if(ItemFunctions.deleteItem(player, 57, price)) {
			long time = (System.currentTimeMillis() + TimeUnit.HOURS.toMillis(24)) / 1000;
			Fraction revertFraction = fraction.revert();
			for(UnitMember member : clan.getAllMembers()) {
				if(member.isOnline()) {
					Player pmember = member.getPlayer();
					if(pmember.isInParty()) {
						pmember.leaveParty();
					}
					if(pmember.getObjectId() == player.getObjectId()) {
						pmember.setVar(CLAN_LAST_CHANGE_VAR, time);
					}
					pmember.changeFraction(revertFraction);
				}
				else {
					if(member.getObjectId() == player.getObjectId()) {
						CharacterVariablesDAO.getInstance().insert(member.getObjectId(), new CharacterVariable(CLAN_LAST_CHANGE_VAR, String.valueOf(time), -1));
					}
					CharacterVariablesDAO.getInstance().insert(member.getObjectId(), new CharacterVariable("fraction", String.valueOf(revertFraction.ordinal()), -1));
				}
			}

			if (clan.getHasFortress() != 0) {
				final Fortress fortress = ResidenceHolder.getInstance().getResidence(Fortress.class, clan.getHasFortress());
				if (fortress != null) {
					fortress.changeOwner(null);
					clan.setHasFortress(0);
				}
			}
			if (clan.getCastle() != 0) {
				final Castle castle = ResidenceHolder.getInstance().getResidence(Castle.class, clan.getCastle());
				if (castle != null) {
					castle.changeOwner(null);
					clan.setHasCastle(0);
				}
			}

			Functions.show("scripts/services/change_fraction_congratulation_clan.htm", player, npc);
		}

	}
}
