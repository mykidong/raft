package mykidong.raft.config;

public enum  Configuration {
    CONF(null, "conf", "/config/raft.yml"),
    LOG4J_CONF("log4j.conf", "log4j.conf", "/log4j.xml"),
    SERVER_PORT("server.port", "port", 9912);

    private String conf;
    private String argConf;
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
