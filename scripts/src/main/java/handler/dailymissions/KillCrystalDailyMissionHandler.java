package handler.dailymissions;

import l2s.gameserver.listener.CharListener;
import l2s.gameserver.listener.actor.OnKillListener;
import l2s.gameserver.model.Creature;

/**
 * @author Java-man
 * @since 13.01.2019
 */
public class KillCrystalDailyMissionHandler extends ProgressDailyMissionHandler {
	private class HandlerListeners implements OnKillListener {
		@Override
		public void onKill(Creature killer, Creature victim) {
			if(killer.isPlayable() && victim.getNpcId() == 13002 || victim.getNpcId() == 13003
					|| victim.getNpcId() == 13004 || victim.getNpcId() == 13005) {
				progressMission(killer.getPlayer(), 1);
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