package mykidong.raft.config;

import mykidong.raft.db.RocksDBKVStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class YamlConfigurator implements Configurator {

    private static Logger LOG = LoggerFactory.getLogger(YamlConfigurator.class);

    public static final String DEFAULT_CONFIG_PATH = "/config/raft.yml";

    private Map<String, Object> configMap;

    private static final Object lock = new Object();

    private static Configurator configurator;

    public static Configurator open() {
        return YamlConfigurator.open(DEFAULT_CONFIG_PATH);
    }

    public static Configurator open(String configPath) {
        if (configurator == null) {
            synchronized (lock) {
                if (configurator == null) {
                    configurator = new YamlConfigurator(configPath);
                }
            }
        }
        return configurator;
    }


    private YamlConfigurator(String configPath) {
        java.net.URL url = this.getClass().getResource(configPath);
        try {
            Yaml yaml = new Yaml();
            configMap = (Map<String, Object>) yaml.load(url.openStream());
            LOG.info("configuration: [{}] loaded...", configPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void put(String key, Object value) {
        configMap.put(key, value);
    }

    @Override
    public Optional get(String key) {
        if (this.configMap.containsKey(key)) {
            Object value  =  configMap.get(key);
            return Optional.of(value);
        } else {
            return Optional.empty();
        }
    }
}
