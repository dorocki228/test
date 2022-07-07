package l2s.gameserver.service;

import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.config.GveTimeDiscountConfig;
import l2s.gameserver.config.GveTimeDiscountConfig.DateTime;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.MultiSellListContainer;
import l2s.gameserver.model.Player;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

public class MultisellDiscountService {
	private static final MultisellDiscountService instance = new MultisellDiscountService();
	private static final double defaultMul = 1.0d;
	private volatile double currentPercent = defaultMul;

	public static MultisellDiscountService getInstance() {
		return instance;
	}

	public void init() {
		nextDiscount();
	}

	public double getDiscountPercent() {
		return currentPercent;
	}

	public boolean isDiscountDefault() {
		return currentPercent == defaultMul;
	}

	private void nextDiscount() {
		ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
		List<DateTime> list = Config.GVE_TIME_DISCOUNT.discountDateTime();
		double[] doubles = Config.GVE_TIME_DISCOUNT.discountPercents();
		int nextDiscountIndex = -1;
		for(int i = 0; i < list.size(); i++) {
			DateTime dateTime = list.get(i);
			ZonedDateTime start = dateTime.getStart();
			ZonedDateTime end = dateTime.getEnd();
			if((start.isBefore(now) || start.isEqual(now)) && end.isAfter(now)) {
				double percent = doubles[i];
				startDiscount(percent, end);
				return;
			}
			if(nextDiscountIndex == -1) {
				nextDiscountIndex = i;
				continue;
			}
			ZonedDateTime nextStartDiscount = list.get(nextDiscountIndex).getStart();
			if(start.isBefore(nextStartDiscount)) {
				nextDiscountIndex = i;
			}
		}
		DateTime dateTime = list.get(nextDiscountIndex);
		double percent = doubles[nextDiscountIndex];
		ZonedDateTime start = dateTime.getStart();
		ZonedDateTime end = dateTime.getEnd();
		long delayMillis = Duration.between(now, start).toMillis();
		ThreadPoolManager.getInstance().schedule(new StartDiscount(percent, end), delayMillis);
	}

	private void endDiscount() {
		currentPercent = defaultMul;
		invalidatePlayerMultisell();
		nextDiscount();
	}

	private void invalidatePlayerMultisell() {
		GameObjectsStorage.getPlayers().stream().map(Player::getMultisell).filter(Objects::nonNull).filter(MultiSellListContainer::isDiscount).forEach(MultiSellListContainer::invalidate);
	}

	private void startDiscount(double percent, ZonedDateTime end) {
		currentPercent = percent;
		invalidatePlayerMultisell();

		ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
		long delayMillis = Duration.between(now, end).toMillis();
		ThreadPoolManager.getInstance().schedule(new EndDiscount(), delayMillis);
	}

	static class StartDiscount implements Runnable {
		private final double percent;
		private final ZonedDateTime end;

		StartDiscount(double percent, ZonedDateTime end) {
			this.percent = percent;
			this.end = end;
		}

		@Override
		public void run() {
			MultisellDiscountService.getInstance().startDiscount(percent, end);
		}
	}

	static class EndDiscount implements Runnable {
		@Override
		public void run() {
			MultisellDiscountService.getInstance().endDiscount();
		}
	}
}
