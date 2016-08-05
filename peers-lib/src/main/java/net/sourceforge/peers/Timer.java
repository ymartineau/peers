package net.sourceforge.peers;

import java.util.Date;
import java.util.TimerTask;
import java.util.function.BooleanSupplier;

/**
 * Basically a java.util.Timer, but it does not throw IllegalStateExceptions if you try to
 * schedule a task after it has been cancelled
 */
public class Timer extends java.util.Timer {


    public Timer() {
        super();
    }

    public Timer(boolean isDaemon) {
        super(isDaemon);
    }

    public Timer(String name) {
        super(name);
    }

    public Timer(String name, boolean isDaemon) {
        super(name, isDaemon);
    }

    public void schedule(TimerTask task, long delay) {
        callIgnoreIllegalStateTransaction(() -> { super.schedule(task, delay); return true; });
    }

    public void schedule(TimerTask task, Date time) {
        callIgnoreIllegalStateTransaction(() -> { super.schedule(task, time); return true; });
    }

    public void schedule(TimerTask task, long delay, long period) {
        callIgnoreIllegalStateTransaction(() -> { super.schedule(task, delay, period); return true; });
    }

    public void schedule(TimerTask task, Date firstTime, long period) {
        callIgnoreIllegalStateTransaction(() -> { super.schedule(task, firstTime, period); return true; });
    }

    public void scheduleAtFixedRate(TimerTask task, long delay, long period) {
        callIgnoreIllegalStateTransaction(() -> { super.scheduleAtFixedRate(task, delay, period); return true; });
    }

    public void scheduleAtFixedRate(TimerTask task, Date firstTime, long period) {
        callIgnoreIllegalStateTransaction(() -> { super.scheduleAtFixedRate(task, firstTime, period); return true; });
    }

    private void callIgnoreIllegalStateTransaction(BooleanSupplier code) {
        try {
            code.getAsBoolean();
        } catch (IllegalStateException e) {
            // ignore
        }
    }

}
