package handler.dailymissions;

import l2s.gameserver.listener.CharListener;
import l2s.gameserver.listener.actor.OnKillListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.instances.PortalInstance;

/**
 * @author Java-man
 * @since 12.01.2019
 */
public class KillPersonalPortalDailyMissionHandler extends ProgressDailyMissionHandler {
	private class HandlerListeners implements OnKillListener {
		@Override
		public void onKill(Creature killer, Creature victim) {
			if(killer.isPlayable() && victim.isPortal()) {
				var portal = PortalInstance.class.cast(victim);
				if(portal.isPersonalPortal()) {
					progressMission(killer.getPlayer(), 1);
				}
			}
		}

		@Override
		public boolean ignorePetOrSummon() {
			return false;
		}
	}

	private final HandlerListeners listeners = new HandlerListeners();

	@Override
	public CharListener getListener() {
		return listeners;
	}

	@Override
	public boolean canBeDistributedToParty() {
		return true;
	}
}