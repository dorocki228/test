package l2s.gameserver.model.entity.events.impl.arena.listener;

import l2s.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.entity.events.impl.ArenaEvent;

/**
 * @author mangol
 */
public class ArenaZoneListener implements OnZoneEnterLeaveListener {
	private final ArenaEvent arenaEvent;

	public ArenaZoneListener(ArenaEvent arenaEvent) {
		this.arenaEvent = arenaEvent;
	}

	@Override
	public void onZoneEnter(Zone zone, Creature creature) {
		Player player = creature.getPlayer();
		if(player == null) {
			return;
		}
		if(!arenaEvent.isInProgress()) {
			return;
		}
		if(!arenaEvent.isParticipant(player.getObjectId()) && !player.isGM()) {
			player.teleToClosestTown();
			return;
		}
	}

	@Override
	public void onZoneLeave(Zone zone, Creature creature) {

	}
}
