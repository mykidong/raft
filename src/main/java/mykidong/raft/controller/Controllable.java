package mykidong.raft.controller;

public interface Controllable {
    void changeState(int ops);
}
