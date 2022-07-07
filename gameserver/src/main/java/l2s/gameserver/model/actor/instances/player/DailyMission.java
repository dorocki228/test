package l2s.gameserver.model.actor.instances.player;

import l2s.commons.time.cron.SchedulingPattern;
import l2s.gameserver.templates.dailymissions.DailyMissionTemplate;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class DailyMission {
    private final DailyMissionTemplate template;

    private final AtomicReference<ZonedDateTime> completed;
    private volatile int value;

    public DailyMission(DailyMissionTemplate template, ZonedDateTime completed, int value) {
        this.template = template;
        this.completed = new AtomicReference<>(completed);
        this.value = value;
    }

    public int getId() {
        return template.getId();
    }

    public DailyMissionTemplate getTemplate() {
        return template;
    }

    public SchedulingPattern getReusePattern() {
        return template.getReusePattern();
    }

    public void setCompleted(ZonedDateTime completed) {
        this.completed.set(completed);
    }

    public boolean isCompleted() {
        ZonedDateTime expectedCompleted = completed.get();
        if (expectedCompleted != null) {
            SchedulingPattern reusePattern = getReusePattern();
            if (reusePattern == null) {
                return true;
            }

            return reusePattern.next(expectedCompleted)
                    .filter(nextExecution -> !nextExecution.isAfter(ZonedDateTime.now()))
                    .map(nextExecution -> {
                        if (completed.compareAndSet(expectedCompleted, null)) {
                            setValue(0);

                            Optional<ZonedDateTime> lastExecution = reusePattern.lastExecution(expectedCompleted);
                            lastExecution.ifPresent(lastExecutionTime ->
                                    template.getHandler().reset(this, nextExecution, lastExecutionTime));

                            return false;
                        }

                        return true;
                    })
                    .orElse(true);
        }

        return false;
    }

    public ZonedDateTime getCompleted() {
        return completed.get();
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "DailyMission[id=" + template.getId() + ", handler=" + template.getHandler().getClass().getName()
                + ", completed=" + completed.get() + ", value=" + value + "]";
    }
}
