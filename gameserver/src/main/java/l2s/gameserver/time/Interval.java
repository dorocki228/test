package l2s.gameserver.time;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * @author KRonst
 */
public class Interval {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final LocalTime from;
    private final LocalTime to;

    public Interval(String value) {
        String[] times = value.split("-");
        from = LocalTime.parse(times[0], TIME_FORMATTER);
        to = LocalTime.parse(times[1], TIME_FORMATTER);
    }

    public LocalTime getFrom() {
        return from;
    }

    public LocalTime getTo() {
        return to;
    }
}
