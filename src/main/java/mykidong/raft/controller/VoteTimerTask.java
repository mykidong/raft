package mykidong.raft.controller;

import java.util.TimerTask;

public class VoteTimerTask extends AbstractTimerTask {

    private TimerTask leaderHeartbeatTimerTask;

    public VoteTimerTask(Controllable controllable, TimerTask leaderHeartbeatTimerTask) {
        super(controllable);
        this.leaderHeartbeatTimerTask = leaderHeartbeatTimerTask;
    }

    @Override
    public void run() {
        // if any vote requests have not arrived yet, as candidate, send vote request to the followers.
        // TODO: send vote request as candidate to the followers.
        //       if this candidate gets votes from the majority of the followers, it will become to leader.
        LOG.debug("send vote request as candidate to the followers...");

        // if this candidate gets votes from the majority of the followers, it will become to leader.
        boolean gotMajorityVote = true;
        if(gotMajorityVote) {
            controllable.changeState(LeaderElectionController.OP_BECOME_LEADER, leaderHeartbeatTimerTask);
        }
    }
}
