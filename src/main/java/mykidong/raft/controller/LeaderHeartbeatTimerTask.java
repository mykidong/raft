package mykidong.raft.controller;

import mykidong.raft.config.Configurator;

public class LeaderHeartbeatTimerTask extends AbstractTimerTask {

    public LeaderHeartbeatTimerTask(Controllable controllable, Configurator configurator) {
        super(controllable, configurator);
    }

    @Override
    public void run() {
        // TODO: as leader, run timer for heartbeat timer to send leader alive info to the followers.
        LOG.debug("as leader, run timer for heartbeat timer to send leader alive info to the followers");
    }
}
