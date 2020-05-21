package mykidong.raft;

import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class RaftServer {

    private static Logger LOG = LoggerFactory.getLogger(RaftServer.class);

    private int port = 9912;

    public RaftServer(int port) {
        this.port = port;

        // log4j init.
        DOMConfigurator.configure(this.getClass().getResource("/log4j.xml"));

        // socket channel queue.
        BlockingQueue<SocketChannel> socketChannelQueue = new LinkedBlockingQueue<>();

        // poll timeout for socket channel queue.
        long socketChannelQueuePollTimeout = 1000;

        // channel processors.
        List<ChannelProcessor> channelProcessors = new ArrayList<>();
        for(int i = 0; i < 5; i++)
        {
            channelProcessors.add(new ChannelProcessor(socketChannelQueue, socketChannelQueuePollTimeout));
        }

        // start channel processors.
        for(ChannelProcessor channelProcessor : channelProcessors) {
            channelProcessor.start();
        }

        // socket server.
        SocketServer socketServer = new SocketServer(this.port, socketChannelQueue);

        // start socket server.
        socketServer.start();

        LOG.info("socket server is listening on " + port + " ...");
    }
}
