package mykidong.raft.config;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import mykidong.raft.test.TestBase;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConfiguratorTest extends TestBase {

    @Test
    public void configure() throws Exception {

        String confStr = "/config/raft-test.yml";
        String log4jConfStr = "/log/log4j.xml";
        int portInt = 9913;
        String nodesStr = "0:localhost:9912,1:localhost:9913,2:localhost:9914";

        List<String> argsList = new ArrayList<>();
        argsList.add("--" + Configuration.CONF.getArgConf());
        argsList.add(confStr);
        argsList.add("--" + Configuration.LOG4J_CONF.getArgConf());
        argsList.add(log4jConfStr);
        argsList.add("--" + Configuration.SERVER_PORT.getArgConf());
        argsList.add("" + portInt);
        argsList.add("--" + Configuration.NODE_LIST.getArgConf());
        argsList.add(nodesStr);

        String[] args = argsList.toArray(new String[0]);

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
        Assert.assertEquals(log4jConfStr, (String) configurator.get(Configuration.LOG4J_CONF.getConf()).get());

        // server port.
        Optional optionalServerPortConf =  configurator.get(Configuration.SERVER_PORT.getConf());
        int serverPort = (options.has(Configuration.SERVER_PORT.getArgConf())) ? (Integer) options.valueOf(Configuration.SERVER_PORT.getArgConf())
                : ((optionalServerPortConf.isPresent()) ? (Integer) optionalServerPortConf.get() : (Integer) Configuration.SERVER_PORT.getDefaultValue());

        // update server port conf to configurator.
        configurator.put(Configuration.SERVER_PORT.getConf(), serverPort);
        Assert.assertTrue(portInt == (Integer) configurator.get(Configuration.SERVER_PORT.getConf()).get());

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
        Assert.assertEquals(3, ((List<String>)configurator.get(Configuration.NODE_LIST.getConf()).get()).size());
    }

    @Test
    public void configurationOptions() throws Exception {
        String confPath = "/config/raft-test.yml";

        // load configuration.
        Configurator configurator = YamlConfigurator.open(confPath);

        int channelProcessorCount = (Integer) configurator.get(Configuration.NIO_CHANNEL_PROCESSOR_COUNT.getConf()).get();
        Assert.assertEquals(10, channelProcessorCount);

        int queueSize = (Integer) configurator.get(Configuration.NIO_SOCKET_CHANNEL_QUEUE_SIZE.getConf()).get();
        Assert.assertEquals(4, queueSize);

        // change configuration value of queue size;
        configurator.put(Configuration.NIO_SOCKET_CHANNEL_QUEUE_SIZE.getConf(), 20);

        queueSize = (Integer) configurator.get(Configuration.NIO_SOCKET_CHANNEL_QUEUE_SIZE.getConf()).get();
        Assert.assertEquals(20, queueSize);

        Object pollTimeoutObj = configurator.get(Configuration.NIO_SOCKET_CHANNEL_QUEUE_POLL_TIMEOUT.getConf()).get();
        long pollTimeout = (pollTimeoutObj instanceof Integer) ? new Long((int)pollTimeoutObj) : new Long((long) pollTimeoutObj);
        Assert.assertEquals(1000, pollTimeout);

        Object delayRangeGreaterThanEqualsObj = configurator.get(Configuration.TIMER_DELAY_RANGE_GREATER_THAN_EQUALS.getConf()).get();
        long delayRangeGreaterThanEquals = (delayRangeGreaterThanEqualsObj instanceof Integer) ? new Long((int)delayRangeGreaterThanEqualsObj)
                : new Long((long) delayRangeGreaterThanEqualsObj);
        Assert.assertEquals(2000, delayRangeGreaterThanEquals);

        Object delayRangeLessThanObj = configurator.get(Configuration.TIMER_DELAY_RANGE_LESS_THAN.getConf()).get();
        long delayRangeLessThan = (delayRangeLessThanObj instanceof Integer) ? new Long((int)delayRangeLessThanObj)
                : new Long((long) delayRangeLessThanObj);
        Assert.assertEquals(3000, delayRangeLessThan);

        Object leaderPeriodObj = configurator.get(Configuration.TIMER_LEADER_PERIOD.getConf()).get();
        long leaderPeriod = (leaderPeriodObj instanceof Integer) ? new Long((int)leaderPeriodObj)
                : new Long((long) leaderPeriodObj);
        Assert.assertEquals(4000, leaderPeriod);

        Object followerDelayObj = configurator.get(Configuration.TIMER_FOLLOWER_DELAY.getConf()).get();
        long followerDelay = (followerDelayObj instanceof Integer) ? new Long((int)followerDelayObj)
                : new Long((long) followerDelayObj);
        Assert.assertEquals(5000, followerDelay);

        // put any configuration.
        String anyProperty = "k1:k2:k3";
        long anyValue = 200000L;
        configurator.put(anyProperty, anyValue);
        Object anyPropertyObj = configurator.get(anyProperty).get();
        long retAnyValue = (anyPropertyObj instanceof Integer) ? new Long((int)anyPropertyObj)
                : new Long((long) anyPropertyObj);
        Assert.assertEquals(anyValue, retAnyValue);


        // change key containing duplicated sub key names.
        anyProperty = "k1:k2:k1";
        anyValue = 500000L;
        configurator.put(anyProperty, anyValue);
        anyPropertyObj = configurator.get(anyProperty).get();
        retAnyValue = (anyPropertyObj instanceof Integer) ? new Long((int)anyPropertyObj)
                : new Long((long) anyPropertyObj);
        Assert.assertEquals(anyValue, retAnyValue);

        // key containing hyphen.
        String anyKey1 = "anyKey:subKey:last-sub-key";
        String value1 = "value1";
        String retValue1 = (String) configurator.get(anyKey1).get();
        Assert.assertEquals(value1, retValue1);

        String anyKey2 = "anyKey:subKey2:last-sub-key";
        String value2 = "value2";
        String retValue2 = (String) configurator.get(anyKey2).get();
        Assert.assertEquals(value2, retValue2);


        String anyKey3 = "anyKey:subKey2:last-sub-key2";
        String value3 = "value3";
        configurator.put(anyKey3, value3);
        String retValue3 = (String) configurator.get(anyKey3).get();
        Assert.assertEquals(value3, retValue3);

        retValue2 = (String) configurator.get(anyKey2).get();
        Assert.assertEquals(value2, retValue2);

        configurator.showConfiguration();
    }

    @Test
    public void argsOption() throws Exception {

        String confStr = "/config/raft-test.yml";
        String log4jConfStr = "/log/log4j.xml";

        List<String> argsList = new ArrayList<>();
        argsList.add("--" + Configuration.CONF.getArgConf());
        argsList.add(confStr);
        argsList.add("--" + Configuration.LOG4J_CONF.getArgConf());
        argsList.add(log4jConfStr);

        String[] args = argsList.toArray(new String[0]);

        OptionParser parser = new OptionParser();
        parser.accepts(Configuration.CONF.getArgConf()).withRequiredArg().ofType(String.class);
        parser.accepts(Configuration.LOG4J_CONF.getArgConf()).withRequiredArg().ofType(String.class);
        parser.accepts(Configuration.SERVER_PORT.getArgConf()).withRequiredArg().ofType(Integer.class);

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
        Assert.assertEquals(log4jConfStr, (String) configurator.get(Configuration.LOG4J_CONF.getConf()).get());
    }
}
