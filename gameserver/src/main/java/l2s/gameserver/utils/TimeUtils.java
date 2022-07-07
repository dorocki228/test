package l2s.gameserver.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;

/**
 * @author VISTALL
 * @date 16:18/14.02.2011
 */

public class TimeUtils
{
	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yy");
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
			.withZone(ZoneId.systemDefault());

	public static String timeFormat(TemporalAccessor time)
	{
		return TIME_FORMATTER.format(time);
	}

	public static String dateFormat(TemporalAccessor dateTime)
	{
		return DATE_FORMATTER.format(dateTime);
	}

	public static String dateFormat(Instant instant)
	{
		ZonedDateTime dateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
		return dateFormat(dateTime);
	}

	public static String dateTimeFormat(TemporalAccessor dateTime)
	{
		return DATE_TIME_FORMATTER.format(dateTime);
	}

	public static String dateTimeFormat(Instant instant)
	{
		ZonedDateTime dateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
		return dateTimeFormat(dateTime);
	}

	public static final SimpleDateFormat SIMPLE_FORMAT = new SimpleDateFormat("HH:mm dd.MM.yyyy");

	private static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");

	public static String time()
	{
		return TIME_FORMAT.format(new Date(System.currentTimeMillis()));
	}

	public static String date()
	{
		return DATE_FORMAT.format(new Date(System.currentTimeMillis()));
	}

	public static String toSimpleFormat(Calendar cal)
	{
		return SIMPLE_FORMAT.format(cal.getTime());
	}

	public static String toSimpleFormat(long cal)
	{
		return SIMPLE_FORMAT.format(cal);
	}

	public static String toSimpleTime(long cal)
	{
		return TIME_FORMAT.format(cal);
	}

	public static String toSimpleDate(long cal)
	{
		return DATE_FORMAT.format(cal);
	}

	public static long parse(String time) throws ParseException
	{
		return SIMPLE_FORMAT.parse(time).getTime();
	}

	public static long addDay(int count)
	{
		long DAY = count * 60 * 60 * 24 * 1000L;
		return DAY;
	}

	public static long addHours(int count)
	{
		long HOUR = count * 60 * 60 * 1000L;
		return HOUR;
	}

	public static long addMinutes(int count)
	{
		long MINUTE = count * 60 * 1000L;
		return MINUTE;
	}

	public static long addSecond(int count)
	{
		long SECONDS = count * 1000L;
		return SECONDS;
	}

	public static long considerMinutes(int count)
	{
		long TIME = addMinutes(count) + System.currentTimeMillis();
		return TIME;
	}

	public static String formatTime(int time, Language lang)
	{
		return formatTime(time, lang, true);
	}

	public static String formatTime(int time, Language lang, boolean cut)
	{

        int days = time / (24 * 3600);
        int hours = (time - days * 24 * 3600) / 3600;
        int minutes = (time - days * 24 * 3600 - hours * 3600) / 60;
        int seconds = (time - days * 24 * 3600 - hours * 3600) % 60;

        String result;
		if(days >= 1)
		{
			if(hours < 1 || cut)
				result = days + " " + Util.declension(lang, days, DeclensionKey.DAYS);
			else
				result = days + " " + Util.declension(lang, days, DeclensionKey.DAYS) + " " + hours + " " + Util.declension(lang, hours, DeclensionKey.HOUR);
		}
		else if(hours >= 1)
		{
			if(minutes < 1 || cut)
				result = hours + " " + Util.declension(lang, hours, DeclensionKey.HOUR);
			else
				result = hours + " " + Util.declension(lang, hours, DeclensionKey.HOUR) + " " + minutes + " " + Util.declension(lang, minutes, DeclensionKey.MINUTES);
		}
		else if(minutes >= 1)
		{
			if(seconds < 1 || cut)
				result = minutes + " " + Util.declension(lang, minutes, DeclensionKey.MINUTES);
			else
				result = minutes + " " + Util.declension(lang, minutes, DeclensionKey.MINUTES) + " " + seconds + " " + Util.declension(lang, seconds, DeclensionKey.SECONDS);
		}
		else
			result = seconds + " " + Util.declension(lang, seconds, DeclensionKey.SECONDS);

		return result;
	}

	public static Calendar getCalendarFromString(String datetime, String format)
	{
		DateFormat df = new SimpleDateFormat(format);
		try
		{
			Date time = df.parse(datetime);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(time);
			return calendar;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
