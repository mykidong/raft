package mykidong.raft.server;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import mykidong.raft.config.Configuration;
import mykidong.raft.config.Configurator;
import mykidong.raft.config.YamlConfigurator;
import mykidong.raft.controller.LeaderElectionController;
import mykidong.raft.util.TimeUtils;
import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RaftServer {

    private static Logger LOG = LoggerFactory.getLogger(RaftServer.class);

    private Configurator configurator;

    public RaftServer(Configurator configurator) {
        this.configurator = configurator;

        int port = (Integer) configurator.get(Configuration.SERVER_PORT.getConf()).get();

        // log4j init.
        String log4jConf = (String) configurator.get(Configuration.LOG4J_CONF.getConf()).get();
        DOMConfigurator.configure(this.getClass().getResource(log4jConf));

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
        NioServer nioServer = new NioServer(port, channelProcessors);

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

    public static void main(String[] args) {

        OptionParser parser = new OptionParser();
        parser.accepts(Configuration.CONF.getArgConf()).withRequiredArg().ofType(String.class);
        parser.accepts(Configuration.LOG4J_CONF.getArgConf()).withRequiredArg().ofType(String.class);
        parser.accepts(Configuration.SERVER_PORT.getArgConf()).withRequiredArg().ofType(Integer.class);
        parser.accepts(Configuration.NODE_LIST.getArgConf()).withRequiredArg().ofType(String.class);

        OptionSet options = parser.parse(args);

        String conf = (options.has(Configuration.CONF.getArgConf())) ? (String) options.valueOf(Configuration.CONF.getArgConf())
                : (String) Configuration.CONF.getDefaultValue();

        // load configuration.
        Configurator configurator = YamlConfigurator.open(conf);

        // log4j conf.
        Optional optionalLog4JConf =  configurator.get(Configuration.LOG4J_CONF.getConf());
        String log4jConf = (options.has(Configuration.LOG4J_CONF.getArgConf())) ? (String) options.valueOf(Configuration.LOG4J_CONF.getArgConf())
                : ((optionalLog4JConf.isPresent()) ? (String) optionalLog4JConf.get() : (String) Configuration.LOG4J_CONF.getDefaultValue());

        // update log4j conf to configurator.
        configurator.put(Configuration.LOG4J_CONF.getConf(), log4jConf);

        // server port.
        Optional optionalServerPortConf =  configurator.get(Configuration.SERVER_PORT.getConf());
        int serverPort = (options.has(Configuration.SERVER_PORT.getArgConf())) ? (Integer) options.valueOf(Configuration.SERVER_PORT.getArgConf())
                : ((optionalServerPortConf.isPresent()) ? (Integer) optionalServerPortConf.get() : (Integer) Configuration.SERVER_PORT.getDefaultValue());

        // update server port conf to configurator.
        configurator.put(Configuration.SERVER_PORT.getConf(), serverPort);


        // node list.
        List<String> nodeList = new ArrayList<>();
        if(options.has(Configuration.NODE_LIST.getArgConf())) {
            String nodes = (String) options.valueOf(Configuration.NODE_LIST.getArgConf());
            for(String nodeLine : nodes.split(",")) {
                nodeList.add(nodeLine);
            }
        } else {
            Optional optionalNodeListConf =  configurator.get(Configuration.NODE_LIST.getConf());
            nodeList = (optionalNodeListConf.isPresent()) ? (List<String>) optionalNodeListConf.get()
                    : (List<String>) Configuration.NODE_LIST.getDefaultValue();
        }

        // update node list conf to configurator.
        configurator.put(Configuration.NODE_LIST.getConf(), nodeList);

        RaftServer raftServer = new RaftServer(configurator);
    }
}
