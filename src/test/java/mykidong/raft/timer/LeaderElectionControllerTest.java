package mykidong.raft.timer;

import mykidong.raft.config.Configurator;
import mykidong.raft.config.YamlConfigurator;
import mykidong.raft.controller.FollowerHeartbeatTimerTask;
import mykidong.raft.controller.LeaderElectionController;
import mykidong.raft.controller.LeaderHeartbeatTimerTask;
import mykidong.raft.controller.VoteTimerTask;
import mykidong.raft.test.TestBase;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LeaderElectionControllerTest extends TestBase {

    private static Logger LOG = LoggerFactory.getLogger(LeaderElectionControllerTest.class);

    @Test
    public void runTimerController() throws Exception {
        Configurator configurator = YamlConfigurator.open();
        LeaderElectionController leaderElectionController =
                new LeaderElectionController(2000, 3000, 4000, 5000);
        leaderElectionController.setVoteTimerTask(new VoteTimerTask(leaderElectionController,
                                                                    new LeaderHeartbeatTimerTask(leaderElectionController, configurator),
                                                                    configurator));
        leaderElectionController.start();

        Thread.sleep(1000);
        leaderElectionController.changeState(LeaderElectionController.OP_VOTE_REQUEST_ARRIVED,
                new FollowerHeartbeatTimerTask(leaderElectionController,
                                               new VoteTimerTask(leaderElectionController,
                                                                 new LeaderHeartbeatTimerTask(leaderElectionController, configurator),
                                                                 configurator),
                                               configurator));

        Thread.sleep(3000);
        leaderElectionController.changeState(LeaderElectionController.OP_HEARTBEAT_ARRIVED,
                new FollowerHeartbeatTimerTask(leaderElectionController,
                                               new VoteTimerTask(leaderElectionController,
                                                                 new LeaderHeartbeatTimerTask(leaderElectionController, configurator),
                                                                 configurator),
                                               configurator));

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
