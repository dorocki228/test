package l2s.gameserver.model.entity.events.impl.arena.listener;

import l2s.gameserver.listener.actor.player.OnTeleportListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.entity.events.impl.ArenaEvent;

/**
 * @author Mangol
 */
public class ArenaOnTeleportListenerImpl implements OnTeleportListener {
	private final ArenaEvent arenaEvent;

	public ArenaOnTeleportListenerImpl(ArenaEvent arenaEvent) {
		this.arenaEvent = arenaEvent;
	}

	@Override
	public void onTeleport(Player player, int x, int y, int z, Reflection reflection) {
		if(!arenaEvent.isInZoneBattle(x, y, z)) {
			arenaEvent.logoutOrTeleport(player);
		}
	}
}
