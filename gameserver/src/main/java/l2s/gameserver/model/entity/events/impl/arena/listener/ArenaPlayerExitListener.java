package l2s.gameserver.model.entity.events.impl.arena.listener;

import l2s.gameserver.listener.actor.player.OnPlayerExitListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.ArenaEvent;

/**
 * @author mangol
 */
public class ArenaPlayerExitListener implements OnPlayerExitListener {
	private final ArenaEvent event;

	public ArenaPlayerExitListener(ArenaEvent event) {
		this.event = event;
	}

	@Override
	public void onPlayerExit(Player p0) {
		this.event.logoutOrTeleport(p0);
	}
}
