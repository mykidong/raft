package mykidong.raft.timer;

import mykidong.raft.controller.LeaderElectionController;
import mykidong.raft.test.TestBase;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LeaderElectionControllerTest extends TestBase {

    private static Logger LOG = LoggerFactory.getLogger(LeaderElectionControllerTest.class);

    @Test
    public void runTimerController() throws Exception {
        LeaderElectionController leaderElectionController =
                new LeaderElectionController(2000, 3000, 4000, 5000);
        leaderElectionController.start();

        Thread.sleep(1000);
        leaderElectionController.changeState(LeaderElectionController.OP_VOTE_REQUEST_ARRIVED);

        Thread.sleep(3000);
        leaderElectionController.changeState(LeaderElectionController.OP_HEARTBEAT_ARRIVED);

        Thread.sleep(Long.MAX_VALUE);
    }

    @Test
    public void bitInt() throws Exception {
        LOG.debug("1 << 0: [{}]", (1 << 0));
        LOG.debug("1 << 1: [{}]", (1 << 1));
        LOG.debug("1 << 2: [{}]", (1 << 2));
        LOG.debug("1 << 3: [{}]", (1 << 3));
        LOG.debug("1 << 4: [{}]", (1 << 4));
    }
}
