package me.earth.phobos.util;

public class Timer1
{
    private long time;
    long startTime;
    long delay;
    boolean paused;

    public boolean isPassed() {
        return !this.paused && System.currentTimeMillis() - this.startTime >= this.delay;
    }

    public long getTimePassed() {
        return System.currentTimeMillis() - this.time;
    }

    public Timer1() {
        this.startTime = System.currentTimeMillis();
        this.delay = 0L;
        this.paused = false;
        this.time = -1L;
    }

    public final boolean passed(final long delay) {
        return this.passed(delay, false);
    }

    public boolean passed(final long delay, final boolean reset) {
        if (reset) {
            this.reset();
        }
        return System.currentTimeMillis() - this.time >= delay;
    }

    public final void reset() {
        this.time = System.currentTimeMillis();
        this.startTime = System.currentTimeMillis();
    }
}
