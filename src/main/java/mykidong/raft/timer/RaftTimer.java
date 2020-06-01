package mykidong.raft.timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

public class RaftTimer implements Schedulable{
    private static Logger LOG = LoggerFactory.getLogger(RaftTimer.class);

    private Timer timer;
    private String name;

    public RaftTimer(String name) {
        this.name = name;
    }

    private Timer newTimer() {
        return new Timer(name);
    }

    @Override
    public void cancel() {
        if(timer != null) {
            timer.cancel();
            timer.purge();
            LOG.debug("[{}] called...", name);
        }
    }

    @Override
    public void runTimer(TimerTask task, long period) {
        LOG.debug("[{}] started with period [{}]ms ...", name, period);

        timer = newTimer();
        timer.scheduleAtFixedRate(task, 0, period);
    }

    @Override
    public void runTimerOnce(TimerTask task) {
        LOG.debug("[{}] once started ...", name);

        timer = newTimer();
        timer.schedule(task, 0);
    }

    @Override
    public void runTimerOnceWithDelay(TimerTask task, long delay) {
        LOG.debug("[{}] once with delay started ...", name);

        timer = newTimer();
        timer.schedule(task, delay);
    }
}
