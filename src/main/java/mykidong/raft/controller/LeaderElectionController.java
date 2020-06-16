package mykidong.raft.controller;

import mykidong.raft.timer.RaftTimer;
import mykidong.raft.timer.Schedulable;
import mykidong.raft.util.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.TimerTask;

public class LeaderElectionController extends Thread implements Controllable{
    private static Logger LOG = LoggerFactory.getLogger(LeaderElectionController.class);

    public static final int OP_VOTE = 1 << 0;
    public static final int OP_VOTE_REQUEST_ARRIVED = 1 << 1;
    public static final int OP_BECOME_LEADER = 1 << 2;
    public static final int OP_HEARTBEAT_ARRIVED = 1 << 3;

    private long delayRangeGreaterThanEquals;
    private long delayRangeLessThan;
    private Random random;
    private Schedulable voteTimer;
    private Schedulable heartbeatTimer;
    private long leaderPeriod;
    private long followerDelay;
    private int currentState = OP_VOTE;
    private TimerTask currentTimerTask;
    private final Object lock = new Object();

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
    }

    private long getRandomDelay() {
        int value = random.nextInt(Long.valueOf(delayRangeLessThan - delayRangeGreaterThanEquals).intValue());
        return value + delayRangeGreaterThanEquals;
    }

    private void pauseThread() {
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                LOG.error(e.getMessage());
            }
        }
    }

    private void resumeThread() {
        synchronized (lock) {
            lock.notify();
        }
    }

    @Override
    public void setVoteTimerTask(TimerTask voteTimerTask) {
        currentState = OP_VOTE;
        currentTimerTask = voteTimerTask;
        LOG.info("vote timer task set...");
    }

    @Override
    public void changeState(int ops, TimerTask timerTask) {
        currentState = ops;
        currentTimerTask = timerTask;

        voteTimer.cancel();
        heartbeatTimer.cancel();

        resumeThread();
    }

    @Override
    public void run() {
        while (true) {
            if(currentState == OP_VOTE) {
                LOG.debug("current state: OP_VOTE");
                runVoteTimer();
                pauseThread();
            } else if(currentState == OP_VOTE_REQUEST_ARRIVED) {
                LOG.debug("current state: OP_VOTE_REQUEST_ARRIVED");
                runFollowerHeartbeatTimer();
                pauseThread();
            } else if(currentState == OP_HEARTBEAT_ARRIVED) {
                LOG.debug("current state: OP_HEARTBEAT_ARRIVED");
                runFollowerHeartbeatTimer();
                pauseThread();
            } else if(currentState == OP_BECOME_LEADER) {
                LOG.debug("current state: OP_BECOME_LEADER");
                runLeaderHeartbeatTimer();
                pauseThread();
            }
        }
    }

    private void runLeaderHeartbeatTimer() {
        if(currentTimerTask == null) {
            LOG.info("current timer task not set yet...");
            TimeUtils.pause(1000);
            return;
        }
        heartbeatTimer.cancel();
        heartbeatTimer.runTimer(currentTimerTask, leaderPeriod);
    }

    private void runFollowerHeartbeatTimer() {
        if(currentTimerTask == null) {
            LOG.info("current timer task not set yet...");
            TimeUtils.pause(1000);
            return;
        }
        heartbeatTimer.cancel();
        heartbeatTimer.runTimerOnceWithDelay(currentTimerTask, followerDelay);
    }

    private void runVoteTimer() {
        if(currentTimerTask == null) {
            LOG.info("current timer task not set yet...");
            TimeUtils.pause(1000);
            return;
        }
        voteTimer.cancel();
        voteTimer.runTimerOnceWithDelay(currentTimerTask, getRandomDelay());
    }
}
