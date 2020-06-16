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
        // TODO: if heartbeat timer timed out, signal to run vote timer as follower.
        LOG.debug("leader heartbeat timed out...");
        controllable.changeState(LeaderElectionController.OP_VOTE, voteTimerTask);
    }
}
