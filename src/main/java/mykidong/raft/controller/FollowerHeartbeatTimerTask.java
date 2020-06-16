package mykidong.raft.controller;

import java.util.TimerTask;

public class FollowerHeartbeatTimerTask extends AbstractTimerTask {

    private TimerTask voteTimerTask;

    public FollowerHeartbeatTimerTask(Controllable controllable, TimerTask voteTimerTask) {
        super(controllable);
        this.voteTimerTask = voteTimerTask;
    }

    @Override
    public void run() {
        LOG.info("leader heartbeat timed out...");
        controllable.changeState(LeaderElectionController.OP_VOTE, voteTimerTask);
    }
}
