package l2s.gameserver.model.entity.events.impl.arena.listener;

import l2s.gameserver.listener.actor.OnDeathFromUndyingListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.entity.events.impl.ArenaEvent;
import l2s.gameserver.service.ArenaEventService;

import java.util.List;

/**
 * @author Mangol
 */
public class ArenaOnDeathFromUndyingListenerImpl implements OnDeathFromUndyingListener {
	private final ArenaEvent arenaEvent;

	public ArenaOnDeathFromUndyingListenerImpl(ArenaEvent arenaEvent) {
		this.arenaEvent = arenaEvent;
	}

	@Override
	public void onDeathFromUndying(Creature actor, Creature killer) {
		Player player = actor.getPlayer();
		if(player == null) {
			return;
		}
		if(!arenaEvent.isInProgress()) {
			return;
		}
		List<Servitor> servitors = player.getServitors();
		for(Servitor servitor : servitors) {
			servitor.abortAttack(true, false);
			servitor.abortCast(true, false);
		}
		ArenaEventService.setFakeDeath(player);
		arenaEvent.checkWinner();
	}
}
