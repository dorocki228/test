package l2s.gameserver.service;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.*;
import l2s.gameserver.utils.ItemFunctions;

import java.util.concurrent.TimeUnit;

/**
 * @author mangol
 */
public class MercenaryService {
	public static final String MERCENARY_END_TIME_STAMP = "mercenaryEndTimeStamp";
	public static final String MERCENARY = "mercenary";
	public static final String MERCENARY_FIRST_ENTER = "mercenaryFirstEnter";

	private static final MercenaryService instance = new MercenaryService();

	public static MercenaryService getInstance() {
		return instance;
	}

	public void transition(Player player, int indexTransitionTime) {
		if(player == null) {
			return;
		}
		if(indexTransitionTime >= Config.MERCENARY_TIME.length || indexTransitionTime < 0) {
			return;
		}
		if(player.isMercenary()) {
			player.sendMessage(new CustomMessage("mercenary.s5"));
			return;
		}
		if(player.isClanLeader()) {
			player.sendMessage(new CustomMessage("mercenary.s6"));
			return;
		}
		if(FactionLeaderService.getInstance().isFactionLeader(player) || FactionLeaderService.getInstance().isRequest(player)) {
			player.sendMessage(new CustomMessage("mercenary.s7"));
			return;
		}
		if(PlayerService.getInstance().isPlayerRegisteredEvent(player) || PlayerService.getInstance().isParticipatesEvent(player)) {
			player.sendMessage(new CustomMessage("mercenary.s8"));
			return;
		}
		if(PlayerService.getInstance().isParticipatesOlympiad(player)) {
			player.sendMessage(new CustomMessage("mercenary.s8"));
			return;
		}
		if(player.getLevel() < 76) {
			player.sendMessage(new CustomMessage("mercenary.s10"));
			return;
		}
		if(isCanNotTransitionForFraction(player)) {
			player.sendMessage(new CustomMessage("mercenary.s9"));
			return;
		}
		int duration = Config.MERCENARY_TIME[indexTransitionTime];
		long time = TimeUnit.MINUTES.toMillis(duration);
		Fraction old = player.getFraction();
		if(player.getMercenaryComponent().startMercenary(time)) {
			long hours = TimeUnit.MINUTES.toHours(duration);
			long itemCount = hours / 3;
			ItemFunctions.addItem(player, 77736, itemCount, true);
			startOrEndMercenary(player, old, player.getFraction());
		}
	}

	public void startOrEndMercenary(Player player, Fraction old, Fraction newFraction) {
		if(player == null) {
			return;
		}
		player.leaveParty();
		Clan clan = player.getClan();
		if(clan != null) {
			int pledgeType = player.getPledgeType();
			clan.removeClanMember(pledgeType, player.getObjectId());
			clan.broadcastToOnlineMembers(new SystemMessagePacket(SystemMsg.S1_HAS_WITHDRAWN_FROM_THE_CLAN).addString(player.getName()), new PledgeShowMemberListDeletePacket(player.getName()));
			if(pledgeType == -1) {
				player.setLvlJoinedAcademy(0);
			}
			player.setClan(null);
			player.setTitle("");
			player.sendPacket(SystemMsg.YOU_HAVE_RECENTLY_BEEN_DISMISSED_FROM_A_CLAN, PledgeShowMemberListDeleteAllPacket.STATIC);
		}
		player.teleToClosestTown();
		GameClient client = player.getNetConnection();
		if(client != null) {
			client.setState(GameClient.GameClientState.AUTHED);
			player.restart();
			CharacterSelectionInfoPacket cl = new CharacterSelectionInfoPacket(client);
			client.sendPacket(RestartResponsePacket.OK, cl);
			client.setCharSelection(cl.getCharInfo());
			client.playerSelected(client.getSelectedIndex());
		}
		else {
			player.kick();
		}
	}

	public boolean isCanNotTransitionForFraction(Player player) {
		Fraction fractionEnemy = player.getFraction().revert();
		int percentage = FractionService.getInstance().getFractionPlayersCountPercentage(fractionEnemy);
		return percentage > 50;
	}
}
