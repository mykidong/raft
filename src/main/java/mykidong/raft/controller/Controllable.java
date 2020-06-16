package mykidong.raft.controller;

import java.util.TimerTask;

public interface Controllable {
    void setVoteTimerTask(TimerTask voteTimerTask);
    void changeState(int ops, TimerTask timerTask);
}
