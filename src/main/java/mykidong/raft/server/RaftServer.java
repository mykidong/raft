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

        // channel processor count.
        int channelProcessorCount = (Integer) configurator.get(Configuration.NIO_CHANNEL_PROCESSOR_COUNT.getConf()).get();

        // leader election controller.
        Object delayRangeGreaterThanEqualsObj = configurator.get(Configuration.TIMER_DELAY_RANGE_GREATER_THAN_EQUALS.getConf()).get();
        long delayRangeGreaterThanEquals = (delayRangeGreaterThanEqualsObj instanceof Integer) ? new Long((int)delayRangeGreaterThanEqualsObj)
                : new Long((long) delayRangeGreaterThanEqualsObj);

        Object delayRangeLessThanObj = configurator.get(Configuration.TIMER_DELAY_RANGE_LESS_THAN.getConf()).get();
        long delayRangeLessThan = (delayRangeLessThanObj instanceof Integer) ? new Long((int)delayRangeLessThanObj)
                : new Long((long) delayRangeLessThanObj);

        Object leaderPeriodObj = configurator.get(Configuration.TIMER_LEADER_PERIOD.getConf()).get();
        long leaderPeriod = (leaderPeriodObj instanceof Integer) ? new Long((int)leaderPeriodObj)
                : new Long((long) leaderPeriodObj);

        Object followerDelayObj = configurator.get(Configuration.TIMER_FOLLOWER_DELAY.getConf()).get();
        long followerDelay = (followerDelayObj instanceof Integer) ? new Long((int)followerDelayObj)
                : new Long((long) followerDelayObj);
        LeaderElectionController leaderElectionController =
                new LeaderElectionController(delayRangeGreaterThanEquals, delayRangeLessThan, leaderPeriod, followerDelay);

        // channel processors.
        List<ChannelProcessor> channelProcessors = new ArrayList<>(channelProcessorCount);
        for(int i = 0; i < channelProcessorCount; i++)
        {
            channelProcessors.add(new ChannelProcessor(configurator, leaderElectionController));
        }

        // start channel processors.
        for(ChannelProcessor channelProcessor : channelProcessors) {
            channelProcessor.start();
        }

        // nio server.
        NioServer nioServer = new NioServer(port, channelProcessors, configurator);

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
