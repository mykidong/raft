package mykidong.raft.controller;

import mykidong.raft.timer.RaftTimer;
import mykidong.raft.timer.Schedulable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class LeaderElectionController extends Thread implements Controllable{
    private static Logger LOG = LoggerFactory.getLogger(LeaderElectionController.class);

    public static final int OP_TRY_VOTE = 1 << 0;
    public static final int OP_VOTE_REQUEST_ARRIVED = 1 << 1;
    public static final int OP_BECOME_LEADER = 1 << 3;
    public static final int OP_HEARTBEAT_ARRIVED = 1 << 5;

    private BlockingQueue<Object> queue;
    private long delayRangeGreaterThanEquals;
    private long delayRangeLessThan;
    private Random random;
    private Schedulable voteTimer;
    private Schedulable heartbeatTimer;
    private long leaderPeriod;
    private long followerDelay;
    private int currentState = OP_TRY_VOTE;

    public LeaderElectionController(long delayRangeGreaterThanEquals,
                                    long delayRangeLessThan,
                                    long leaderPeriod,
                                    long followerDelay) {
        this.delayRangeGreaterThanEquals = delayRangeGreaterThanEquals;
        this.delayRangeLessThan = delayRangeLessThan;
        random = new Random();
        voteTimer = new RaftTimer("Vote Timer");
        this.leaderPeriod = leaderPeriod;
        this.followerDelay = followerDelay;
        heartbeatTimer = new RaftTimer("Heartbeat Timer");
        queue = new LinkedBlockingQueue<>();
    }

    private long getRandomDelay() {
        int value = random.nextInt(Long.valueOf(delayRangeLessThan - delayRangeGreaterThanEquals).intValue());
        return value + delayRangeGreaterThanEquals;
    }

    @Override
    public void changeCurrentState(int ops) {
        currentState = ops;

        if(currentState == OP_TRY_VOTE) {
            voteTimer.cancel();
        } else if(currentState == OP_VOTE_REQUEST_ARRIVED) {
            heartbeatTimer.cancel();
        } else if(currentState == OP_HEARTBEAT_ARRIVED) {
            heartbeatTimer.cancel();
        }
    }

    @Override
    public void run() {
        while (true) {
            if(currentState == OP_TRY_VOTE) {
                LOG.debug("current state: OP_TRY_VOTE");
                runVoteTimer();
            } else if(currentState == OP_VOTE_REQUEST_ARRIVED) {
                LOG.debug("current state: OP_VOTE_REQUEST_ARRIVED");
                runFollowerHeartbeatTimer();
            } else if(currentState == OP_HEARTBEAT_ARRIVED) {
                LOG.debug("current state: OP_HEARTBEAT_ARRIVED");
                runFollowerHeartbeatTimer();
            } else if(currentState == OP_BECOME_LEADER) {
                LOG.debug("current state: OP_BECOME_LEADER");
                runLeaderHeartbeatTimer();
            }
        }
    }

    private void runLeaderHeartbeatTimer() {
        heartbeatTimer.cancel();
        heartbeatTimer.runTimer(new LeaderHeartbeatTimerTask(this), leaderPeriod);
    }

    private static class LeaderHeartbeatTimerTask extends TimerTask {
        private Controllable controllable;

        public LeaderHeartbeatTimerTask(Controllable controllable) {
            this.controllable = controllable;
        }

        @Override
        public void run() {
            // TODO: as leader, run timer for heartbeat timer to send leader alive info to the followers.
            LOG.debug("as leader, run timer for heartbeat timer to send leader alive info to the followers");
            controllable.changeCurrentState(LeaderElectionController.OP_BECOME_LEADER);
        }
    }

    private void runFollowerHeartbeatTimer() {
        heartbeatTimer.cancel();
        heartbeatTimer.runTimerOnceWithDelay(new FollowerHeartbeatTimerTask(this), followerDelay);
    }

    private static class FollowerHeartbeatTimerTask extends TimerTask {
        private Controllable controllable;

        public FollowerHeartbeatTimerTask(Controllable controllable) {
            this.controllable = controllable;
        }

        @Override
        public void run() {
            // TODO: if hearbeat timer timed out, signal to run vote timer as follower.
            LOG.debug("leader heartbeat timed out...");
            controllable.changeCurrentState(LeaderElectionController.OP_TRY_VOTE);
        }
    }

    private void runVoteTimer() {
        voteTimer.cancel();

        voteTimer.runTimerOnceWithDelay(new VoteTimerTask(this), getRandomDelay());
    }

    private static class VoteTimerTask extends TimerTask {

        private Controllable controllable;

        public VoteTimerTask(Controllable controllable) {
            this.controllable = controllable;
        }

        @Override
        public void run() {
            // if any vote requests have not arrived yet, as candidate, send vote request to the followers.
            // TODO: send vote request as candidate to the followers.
            //       if this candidate gets votes from the majority of the followers, it will become to leader.
            LOG.debug("send vote request as candidate to the followers...");

            controllable.changeCurrentState(LeaderElectionController.OP_BECOME_LEADER);
        }
    }
}
