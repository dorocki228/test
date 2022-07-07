package l2s.commons.time.cron;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.util.Optional;

public class SchedulingPattern
{
    private static final CronDefinition CRON_DEFINITION = createCronDefinition();

    private static final CronDescriptor CRON_DESCRIPTOR = CronDescriptor.instance();

    private final Cron cron;
    private final ExecutionTime executionTime;

    public SchedulingPattern(String pattern)
    {
        CronParser parser = new CronParser(CRON_DEFINITION);
        cron = parser.parse("0 " + pattern);
        executionTime = ExecutionTime.forCron(cron);
    }

    public long next(long millis)
    {
        Instant instant = Instant.ofEpochMilli(millis);
        return next(instant).toEpochMilli();
    }

    public Instant next(Instant instant)
    {
        ZonedDateTime dateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
        return next(dateTime)
                .map(ChronoZonedDateTime::toInstant)
                .orElseThrow(() ->
                        new IllegalArgumentException("Can't find next execution time for cron " + this));
    }

    public Optional<ZonedDateTime> next(ZonedDateTime dateTime)
    {
        return executionTime.nextExecution(dateTime);
    }

    public Instant lastExecution(Instant instant)
    {
        ZonedDateTime dateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
        return next(dateTime)
                .map(ChronoZonedDateTime::toInstant)
                .orElseThrow(() ->
                        new IllegalArgumentException("Can't find last execution time for cron " + this));
    }

    public Optional<ZonedDateTime> lastExecution(ZonedDateTime dateTime)
    {
        return executionTime.lastExecution(dateTime);
    }

    public Optional<Duration> timeToNextExecution(ZonedDateTime dateTime)
    {
        return executionTime.timeToNextExecution(dateTime);
    }

    public String description()
    {
        return CRON_DESCRIPTOR.describe(cron);
    }

    @Override
    public String toString()
    {
        return cron.asString();
    }

    private static CronDefinition createCronDefinition()
    {
        return CronDefinitionBuilder.defineCron()
                .withSeconds().and()
                .withMinutes().and()
                .withHours().and()
                .withDayOfMonth()
                .supportsHash().supportsL().supportsW().and()
                .withMonth().and()
                .withDayOfWeek()
                .withIntMapping(7, 0)
                .supportsHash().supportsL().supportsW().and()
                .instance();
    }
}
