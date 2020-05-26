package mykidong.raft;

import mykidong.raft.client.SocketClient;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.Before;
import org.junit.Test;

public class RunClient {

    @Before
    public void init() throws Exception {
        // log4j init.
        DOMConfigurator.configure(this.getClass().getResource("/log4j.xml"));
    }

    @Test
    public void run() throws Exception {
        String host = "localhost";
        int port = 9912;
        SocketClient client = new SocketClient(host, port);
        client.start();

        Thread.sleep(Long.MAX_VALUE);
    }
}
