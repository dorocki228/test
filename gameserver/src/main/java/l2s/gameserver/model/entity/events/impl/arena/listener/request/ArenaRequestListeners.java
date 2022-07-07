package l2s.gameserver.model.entity.events.impl.arena.listener.request;

import l2s.gameserver.listener.actor.party.OnLeavePlayerParty;
import l2s.gameserver.listener.actor.player.OnPlayerExitListener;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.service.ArenaEventService;

/**
 * @author mangol
 */
public class ArenaRequestListeners {
	private static final ArenaRequestListeners instance = new ArenaRequestListeners();

	public static ArenaRequestListeners getInstance() {
		return instance;
	}

	public PlayerExitListener getPlayerExit() {
		return PlayerExitListener.getInstance();
	}

	public PartyMemberLeave getLeavePlayerParty() {
		return PartyMemberLeave.getInstance();
	}

	private static final class PlayerExitListener implements OnPlayerExitListener {
		private static final PlayerExitListener instance = new PlayerExitListener();

		public static PlayerExitListener getInstance() {
			return instance;
		}

		@Override
		public void onPlayerExit(Player player) {
			ArenaEventService.getInstance().removeRequestFromPlayer(player, true);
		}
	}

	private static final class PartyMemberLeave implements OnLeavePlayerParty {
		private static final PartyMemberLeave instance = new PartyMemberLeave();

		public static PartyMemberLeave getInstance() {
			return instance;
		}

		@Override
		public void leave(Party party, Player player, boolean kick) {
			ArenaEventService.getInstance().removeRequestFromPlayer(player, true);
		}
	}
}
