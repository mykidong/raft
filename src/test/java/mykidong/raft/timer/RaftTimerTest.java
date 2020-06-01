package mykidong.raft.timer;

import mykidong.raft.test.TestBase;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TimerTask;

public class RaftTimerTest extends TestBase {

    private static Logger LOG = LoggerFactory.getLogger(RaftTimerTest.class);

    @Test
    public void runTask() throws Exception {
        Schedulable schedulable = new RaftTimer("test timer");
        schedulable.runTimer(new TimerTask() {
            @Override
            public void run() {
                LOG.debug("hello....");
            }
        }, 1000L);

        Thread.sleep(2000);

        schedulable.cancel();

        schedulable.runTimer(new TimerTask() {
            @Override
            public void run() {
                LOG.debug("hello....");
            }
        }, 1000L);
        schedulable.cancel();


        schedulable.runTimerOnce(new TimerTask() {
            @Override
            public void run() {
                LOG.debug("hello once ....");
            }
        });


        Thread.sleep(Long.MAX_VALUE);
    }
}
