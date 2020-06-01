package mykidong.raft.timer;

import java.util.TimerTask;

public interface Schedulable {
    void cancel();
    void runTimer(TimerTask task, long period);
    void runTimerOnce(TimerTask task);
    void runTimerOnceWithDelay(TimerTask task, long delay);
}
