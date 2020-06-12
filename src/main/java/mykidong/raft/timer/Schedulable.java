package mykidong.raft.timer;

import java.util.TimerTask;

public interface Schedulable {
    /**
     * cancel timer.
     */
    void cancel();

    /**
     * run timer periodically.
     * @param task timer task.
     * @param period period in milliseconds between successive timer task executions.
     */
    void runTimer(TimerTask task, long period);

    /**
     * run timer task only once.
     * @param task timer task.
     */
    void runTimerOnce(TimerTask task);

    /**
     * run timer task only once with delay in milliseconds.
     *
     * @param task timer task.
     * @param delay delay in milliseconds.
     */
    void runTimerOnceWithDelay(TimerTask task, long delay);
}
