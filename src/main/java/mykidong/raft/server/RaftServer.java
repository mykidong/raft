package mykidong.raft.server;

import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class RaftServer {

    private static Logger LOG = LoggerFactory.getLogger(RaftServer.class);

    private int port = 9912;

    public RaftServer(int port) {
        this.port = port;

        // log4j init.
        DOMConfigurator.configure(this.getClass().getResource("/log4j.xml"));

        // poll timeout for socket channel queue.
        long socketChannelQueuePollTimeout = 1000;

        // blocking queue size.
        int queueSize = 4;

        // channel processors.
        List<ChannelProcessor> channelProcessors = new ArrayList<>();
        for(int i = 0; i < 5; i++)
        {
            channelProcessors.add(new ChannelProcessor(socketChannelQueuePollTimeout, queueSize));
        }

        // start channel processors.
        for(ChannelProcessor channelProcessor : channelProcessors) {
            channelProcessor.start();
        }

        // socket server.
        SocketServer socketServer = new SocketServer(this.port, channelProcessors);

        // start socket server.
        socketServer.start();

        LOG.info("socket server is listening on " + port + " ...");
    }
}
