package mykidong.raft.config;

import java.util.Arrays;

public enum  Configuration {
    // configuration path.
    CONF(null, "conf", "/config/raft.yml"),

    // log4j configuration path.
    LOG4J_CONF("log4j.conf", "log4j.conf", "/log4j.xml"),

    // server port.
    SERVER_PORT("server.port", "port", 9912),

    // the list of nodes which participate in the peer group.
    NODE_LIST("node.list", "nodes", Arrays.asList("0:localhost:9912"));

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
