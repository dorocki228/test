package l2s.commons.limiter;

public interface Limiter {
    Limiter NOOP = () -> true;

    boolean pass();

    default long getTimeLeft() {
        return 0;
    }

    default long getTimeElapsed() {
        return 0;
    }
}
