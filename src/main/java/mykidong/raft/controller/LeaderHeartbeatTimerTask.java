package mykidong.raft.controller;

public class LeaderHeartbeatTimerTask extends AbstractTimerTask {

    public LeaderHeartbeatTimerTask(Controllable controllable) {
        super(controllable);
    }

    @Override
    public void run() {
        // TODO: as leader, run timer for heartbeat timer to send leader alive info to the followers.
        LOG.debug("as leader, run timer for heartbeat timer to send leader alive info to the followers");
    }
}
