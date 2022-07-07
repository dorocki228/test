package l2s.gameserver.component.player;

import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.component.AbstractComponent;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.service.MercenaryService;

import java.util.concurrent.Future;

/**
 * @author mangol
 */
public class MercenaryComponent extends AbstractComponent<Player> {
	private volatile Future<?> future;

	public MercenaryComponent(Player player) {
		super(player);
	}

	public boolean isMercenary() {
		return getObject().getVarBoolean(MercenaryService.MERCENARY, false);
	}

	@Override
	public void restore() {
		long endTimeStamp = getEndTimeStamp();
		long currentTimeMillis = System.currentTimeMillis();

		if((endTimeStamp == 0 || endTimeStamp <= currentTimeMillis)) {
			getObject().setVar(MercenaryService.MERCENARY, false);
		}
	}

	@Override
	public void enterWorld() {
		long endTimeStamp = getEndTimeStamp();
		long currentTimeMillis = System.currentTimeMillis();

		if((endTimeStamp >= currentTimeMillis)) {
			startMercenaryEndTask(endTimeStamp);
			boolean firstEnter = !getObject().getVarBoolean(MercenaryService.MERCENARY_FIRST_ENTER, false);
			if(firstEnter) {
				long timeLeft = (endTimeStamp - currentTimeMillis) / 1000;
				int hours = (int) (timeLeft / 3600);
				int minutes = (int) ((timeLeft - hours * 3600) / 60);
				getObject().sendMessage(new CustomMessage("mercenary.s1").addNumber(hours).addNumber(minutes));
				getObject().setVar(MercenaryService.MERCENARY_FIRST_ENTER, true);
			}
		}
	}

	public synchronized boolean startMercenary(long timeMillis) {
		if(isMercenary()) {
			return false;
		}
		long currentTimeMillis = System.currentTimeMillis();
		long endTimeStamp = currentTimeMillis + timeMillis;
		getObject().setVar(MercenaryService.MERCENARY, true);
		getObject().setVar(MercenaryService.MERCENARY_END_TIME_STAMP, endTimeStamp);
		getObject().setVar(MercenaryService.MERCENARY_FIRST_ENTER, false);
		startMercenaryEndTask(endTimeStamp);
		return true;
	}

	private void endMercenary() {
		Fraction currentFraction = getObject().getFraction();
		getObject().setVar(MercenaryService.MERCENARY, false);
		getObject().setVar(MercenaryService.MERCENARY_FIRST_ENTER, false);
		Fraction restore = getObject().getFraction();
		stopMercenaryEndTask();
		if(!getObject().isInOfflineMode()) {
			MercenaryService.getInstance().startOrEndMercenary(getObject(), currentFraction, restore);
		}
	}

	private void startMercenaryEndTask(long endTime) {
		long currentTimeMillis = System.currentTimeMillis();
		long delay = endTime - currentTimeMillis;
		delay = Math.max(0, delay);
		stopMercenaryEndTask();
		future = ThreadPoolManager.getInstance().schedule(this::endMercenary, delay);
	}

	private void stopMercenaryEndTask() {
		if(future != null) {
			future.cancel(false);
		}
		future = null;
	}

	@Override
	public void logout() {
		stopMercenaryEndTask();
	}

	public long getEndTimeStamp() {
		return getObject().getVarLong(MercenaryService.MERCENARY_END_TIME_STAMP, 0);
	}
}
