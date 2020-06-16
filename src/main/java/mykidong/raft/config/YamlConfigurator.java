package mykidong.raft.config;

import org.yaml.snakeyaml.Yaml;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class YamlConfigurator implements Configurator {

    public static final String DEFAULT_CONFIG_PATH = "/config/raft.yml";

    private Map<String, Object> configMap;

    private static final Object lock = new Object();

    private static Configurator configHandler;

    public static Configurator open() {
        return YamlConfigurator.open(DEFAULT_CONFIG_PATH);
    }

    public static Configurator open(String configPath) {
        if (configHandler == null) {
            synchronized (lock) {
                if (configHandler == null) {
                    configHandler = new YamlConfigurator(configPath);
                }
            }
        }
        return configHandler;
    }


    private YamlConfigurator(String configPath) {
        java.net.URL url = this.getClass().getResource(configPath);
        try {
            Yaml yaml = new Yaml();
            Map<String, Object> yamlMap = (Map<String, Object>) yaml.load(url.openStream());

            configMap = Collections.unmodifiableMap(yamlMap);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
