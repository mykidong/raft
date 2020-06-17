package mykidong.raft.server;

import mykidong.raft.config.Configuration;
import mykidong.raft.config.Configurator;
import mykidong.raft.config.YamlConfigurator;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

public class RunServer {

    private static Logger LOG = LoggerFactory.getLogger(RunServer.class);

    /**
     * run raft server in which nio server is running which assigns the new socket channel connections to ChannelProcessors
     * having individual selector which handles the assigned socket channels.
     */
    @Test
    public void run() throws Exception
    {
        int port = 9912;
        Configurator configurator = YamlConfigurator.open();
        configurator.put(Configuration.SERVER_PORT.getConf(), port);

        RaftServer raftServer = new RaftServer(configurator);

        Thread.sleep(Long.MAX_VALUE);
    }

    /**
     * run nio server in single thread.
     */
    @Test
    public void runSingleThreadNioServer() throws Exception {
        // log4j init.
        DOMConfigurator.configure(this.getClass().getResource("/log4j.xml"));

        int port = 9912;
        SingleThreadNioServer nioServer = new SingleThreadNioServer(port);
        nioServer.start();

        LOG.info("single threaded nio server is listening on [{}]", port);


        Thread.sleep(Long.MAX_VALUE);
    }
}
