package l2s.commons.limiter;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

public final class RateLimiter implements Limiter {
    private final AtomicLong passed = new AtomicLong();
    private final AtomicLong rejected = new AtomicLong();

    private final AtomicLong rest;
    private final AtomicLong deadline;

    /**
     * Maximum number of events per duration (in ms)
     */
    private final long threshold;
    private final long duration;

    /**
     * @param threshold maximum number of events per duration
     * @param duration  timespan duration in millis
     */
    public RateLimiter(long threshold, long duration) {
        this.threshold = threshold;
        this.duration = duration;
        this.rest = new AtomicLong(threshold);
        this.deadline = new AtomicLong(now() + this.duration);
    }

    private static long now() {
        return System.currentTimeMillis();
    }

    public long getThreshold() {
        return threshold;
    }

    public long getDuration() {
        return duration;
    }

    @Override
    public boolean pass() {
        boolean pass = remain(rest::getAndDecrement) > 0;
        (pass ? passed : rejected).incrementAndGet();
        return pass;
    }

    public boolean passWithoutStat() {
        return remain(rest::getAndDecrement) > 0;
    }

    public long getRest() {
        return remain(rest::get);
    }

    public void setRest(long rest, long ttl) {
        deadline.set(now() + ttl);
        this.rest.set(rest);
    }

    private long remain(LongSupplier rest) {
        long remain = rest.getAsLong();

        if (remain > 0)
            return remain;

        long now = now();
        long reset = deadline.get();

        if (now < reset)
            return remain;

        reset(reset, now);
        return remain(rest);
    }

    private void reset(long last, long current) {
        if (deadline.compareAndSet(last, current + duration))
            rest.set(threshold);
    }

    /**
     * Stats
     */
    public long getPassedCount() {
        return passed.get();
    }

    public long getRejectedCount() {
        return rejected.get();
    }

    @Override
    public long getTimeLeft() {
        return deadline.get() - now();
    }

    @Override
    public long getTimeElapsed() {
        return now() - (deadline.get() - duration);
    }
}