package mykidong.raft.config;


import java.util.Optional;

public interface Configurator {
    void put(String key, Object value);
    Optional get(String key);
}
