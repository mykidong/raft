package mykidong.raft.controller;

public interface Controllable {
    void changeCurrentState(int ops);
}
