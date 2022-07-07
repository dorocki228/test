package l2s.gameserver.config;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import l2s.gameserver.Config;
import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.Converter;
import org.aeonbits.owner.Reloadable;

@Sources("file:config/gve_timediscounts.properties")
public interface GveTimeDiscountConfig extends Reloadable {
	@Key("MultiselsUnderDiscounts")
	@Separator(",")
	int[] multiselsUnderDiscounts();

	@Key("Discount")
	@Separator(";")
	double[] discountPercents();

	@Key("DiscountDateTime")
	@Separator(";")
	@ConverterClass(DiscountDateTimeConverter.class)
	List<DateTime> discountDateTime();

	public static DiscountTime next() {
		ZonedDateTime now = ZonedDateTime.now();
		int nextIndex = -1;
		for (int i = 0; i < Config.GVE_TIME_DISCOUNT.discountDateTime().size(); i++) {
			DateTime discountTime = Config.GVE_TIME_DISCOUNT.discountDateTime().get(i);
			if (discountTime.start.isBefore(now) && discountTime.end.isAfter(now)) {
				nextIndex = i + 1;
				break;
			}
		}

		if (nextIndex != -1 && nextIndex < Config.GVE_TIME_DISCOUNT.discountDateTime().size()) {
			DateTime nextDiscount = Config.GVE_TIME_DISCOUNT.discountDateTime().get(nextIndex);
			long totalMinutes = Duration.between(now, nextDiscount.start).toMinutes();
			long minutes = totalMinutes % 60;
			long hours = totalMinutes / 60;
			return new DiscountTime(hours, minutes);
		} else {
			return null;
		}
	}

	public static class DiscountTime {
		private final long hours;
		private final long minutes;

		public DiscountTime(long hours, long minutes) {
			this.hours = hours;
			this.minutes = minutes;
		}

		public long hours() {
			return hours;
		}

		public long minutes() {
			return minutes;
		}
	}

	public static class DiscountDateTimeConverter implements Converter<DateTime> {
		@Override
		public DateTime convert(Method method, String input) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());
			String[] timeRange = input.split(",");
			ZonedDateTime start = ZonedDateTime.parse(timeRange[0], formatter);
			ZonedDateTime end = ZonedDateTime.parse(timeRange[1], formatter);
			return new DateTime(start, end);
		}
	}

	public class DateTime {
		private final ZonedDateTime start;
		private final ZonedDateTime end;

		DateTime(ZonedDateTime start, ZonedDateTime end) {
			this.start = start;
			this.end = end;
		}

		public ZonedDateTime getStart() {
			return start;
		}

		public ZonedDateTime getEnd() {
			return end;
		}
	}
}
