package mykidong.raft.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.util.*;

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
            LOG.info("configuration: [{}] loaded...", configMap);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void put(String key, Object value) {
        String[] subKeys = key.split(":");
        Map<String, Object> currentConfigMap = configMap;
        int count = 0;
        int size = subKeys.length;
        List<SubKeyValue> subKeyValues = new ArrayList<>();
        for(String subKey : subKeys) {
            if(count == size - 1) {
                subKeyValues.add(new SubKeyValue(subKey, value));
            } else {
                if (!currentConfigMap.containsKey(subKey)) {
                    currentConfigMap.put(subKey, new HashMap<String, Object>());
                }
                Map<String, Object> mapValue = (Map<String, Object>) currentConfigMap.get(subKey);
                currentConfigMap = mapValue;
                count++;
                subKeyValues.add(new SubKeyValue(subKey, mapValue));
            }
        }

        // sort sub key value list in reverse oder.
        Collections.reverse(subKeyValues);

        count = 0;
        size = subKeyValues.size();
        Map<String, Object> parentConfigMap = null;
        for(SubKeyValue subKeyValue : subKeyValues) {
            String subKey = subKeyValue.getSubKey();
            Object subValue = subKeyValue.getValue();
            if(count == size - 1) {
                configMap.put(subKey, subValue);
                return;
            } else {
                parentConfigMap = (Map<String, Object>)((SubKeyValue) subKeyValues.get(count + 1)).getValue();
                parentConfigMap.put(subKey, subValue);
                count++;
            }
        }
    }

    private static class SubKeyValue {
        private String subKey;
        private Object value;

        public SubKeyValue(String subKey, Object value) {
            this.subKey = subKey;
            this.value = value;
        }

        public String getSubKey() {
            return subKey;
        }

        public Object getValue() {
            return value;
        }
    }

    @Override
    public Optional get(String key) {
        String[] subKeys = key.split(":");
        Map<String, Object> currentConfigMap = configMap;
        int count = 0;
        int size = subKeys.length;
        for(String subKey : subKeys) {
            if(count == size - 1) {
                if(currentConfigMap.containsKey(subKey)) {
                    Object value = currentConfigMap.get(subKey);
                    return Optional.of(value);
                } else {
                    return Optional.empty();
                }
            } else {
                if(currentConfigMap.containsKey(subKey)) {
                    currentConfigMap = (Map<String, Object>) currentConfigMap.get(subKey);
                    count++;
                    continue;
                } else {
                    return Optional.empty();
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public void showConfiguration() {
        LOG.info("current configuration: [{}]", configMap);
    }
}
