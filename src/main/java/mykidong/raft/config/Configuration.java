package mykidong.raft.config;

import java.util.Arrays;

/**
 * This class defines Yaml configuration keys and default values.
 *
 * for instance, say, the following is yaml configuration
 * anyKey:
 *   subKey:
 *     last-sub-key: value1
 *
 * , then we can define configuration key like "anyKey:subKey:last-sub-key".
 * The sub sequential keys will be appended with colon separator.
 *
 */

public enum  Configuration {
    // configuration path.
    CONF(null, "conf", "/config/raft.yml"),

    // log4j configuration path.
    LOG4J_CONF("log4j.conf", "log4j.conf", "/log4j.xml"),

    // server port.
    SERVER_PORT("server.port", "port", 9912),

    // the list of nodes which participate in the peer group.
    NODE_LIST("node.list", "nodes", Arrays.asList("0:localhost:9912")),

    NIO_CHANNEL_PROCESSOR_COUNT("nio:channelProcessorCount", null, 10),

    NIO_SOCKET_CHANNEL_QUEUE_SIZE("nio:socketChannel:queue:size", null, 4),
    NIO_SOCKET_CHANNEL_QUEUE_POLL_TIMEOUT("nio:socketChannel:queue:pollTimeout", null, 1000),

    TIMER_DELAY_RANGE_GREATER_THAN_EQUALS("timer:delayRangeGreaterThanEquals", null, 2000),
    TIMER_DELAY_RANGE_LESS_THAN("timer:delayRangeLessThan", null, 3000),
    TIMER_LEADER_PERIOD("timer:leaderPeriod", null, 4000),
    TIMER_FOLLOWER_DELAY("timer:followerDelay", null, 5000);


    /**
     * property name of the configuration file.
     */
    private String conf;

    /**
     * argument parameter name passed when the server is running.
     */
    private String argConf;

    /**
     * default value of the configuration property.
     */
    private Object defaultValue;

    private Configuration(String conf, String argConf, Object defaultValue) {
        this.conf = conf;
        this.argConf = argConf;
        this.defaultValue = defaultValue;
    }

    public String getConf() {
        return conf;
    }

    public String getArgConf() {
        return argConf;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }
}
