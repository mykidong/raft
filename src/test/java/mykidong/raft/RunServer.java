package mykidong.raft;

import mykidong.raft.server.RaftServer;
import org.junit.Test;

public class RunServer {

    @Test
    public void run() throws Exception
    {
        int port = 9912;
        RaftServer raftServer = new RaftServer(port);

        Thread.sleep(Long.MAX_VALUE);
    }
}
