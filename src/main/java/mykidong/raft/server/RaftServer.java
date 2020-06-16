package mykidong.raft.server;

import mykidong.raft.controller.LeaderElectionController;
import mykidong.raft.util.TimeUtils;
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

        // channel processor count.
        int channelProcessorCount = 10;

        // leader election controller.
        long delayRangeGreaterThanEquals = 2000;
        long delayRangeLessThan = 3000;
        long leaderPeriod = 4000;
        long followerDelay = 5000;
        LeaderElectionController leaderElectionController =
                new LeaderElectionController(delayRangeGreaterThanEquals, delayRangeLessThan, leaderPeriod, followerDelay);

        // channel processors.
        List<ChannelProcessor> channelProcessors = new ArrayList<>(channelProcessorCount);
        for(int i = 0; i < channelProcessorCount; i++)
        {
            channelProcessors.add(new ChannelProcessor(socketChannelQueuePollTimeout, queueSize, leaderElectionController));
        }

        // start channel processors.
        for(ChannelProcessor channelProcessor : channelProcessors) {
            channelProcessor.start();
        }

        // nio server.
        NioServer nioServer = new NioServer(this.port, channelProcessors);

        // start nio server.
        nioServer.start();

        // start leader election controller after initializing nio server.
        if(!nioServer.isReady()) {
            while (true) {
                if(!nioServer.isReady()) {
                    TimeUtils.pause(100);
                } else {
                    break;
                }
            }
        }
        leaderElectionController.start();
    }
}
