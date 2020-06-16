package mykidong.raft.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TimerTask;

public abstract class AbstractTimerTask extends TimerTask {

    protected static Logger LOG = LoggerFactory.getLogger(AbstractTimerTask.class);

    protected Controllable controllable;

    public AbstractTimerTask(Controllable controllable) {
        this.controllable = controllable;
    }
}
