package mykidong.raft.controller;

import mykidong.raft.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TimerTask;

public abstract class AbstractTimerTask extends TimerTask {

    protected static Logger LOG = LoggerFactory.getLogger(AbstractTimerTask.class);

    protected Controllable controllable;
    protected Configurator configurator;

    public AbstractTimerTask(Controllable controllable, Configurator configurator) {
        this.controllable = controllable;
        this.configurator = configurator;
    }
}
