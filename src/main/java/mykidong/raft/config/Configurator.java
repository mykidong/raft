package mykidong.raft.config;


import java.util.Optional;

public interface Configurator<T> {
    Optional get(String key);
}
