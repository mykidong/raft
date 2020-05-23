package mykidong.raft;

import mykidong.raft.server.NioServer;
import mykidong.raft.server.RaftServer;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunServer {

    private static Logger LOG = LoggerFactory.getLogger(RunServer.class);

    @Test
    public void run() throws Exception
    {
        int port = 9912;
        RaftServer raftServer = new RaftServer(port);

        Thread.sleep(Long.MAX_VALUE);
    }

    @Test
    public void runSingleThreadNioServer() throws Exception {
        // log4j init.
        DOMConfigurator.configure(this.getClass().getResource("/log4j.xml"));

        int port = 9912;
        NioServer nioServer = new NioServer(port);
        nioServer.start();

        LOG.info("single threaded nio server is listening on [{}]", port);


        Thread.sleep(Long.MAX_VALUE);
    }
}
