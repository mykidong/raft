package mykidong.raft.serialize;

/**
 * Created by mykidong on 2018-06-07.
 */
public interface KryoContext<T> {

    byte[] serialze(Object obj);

    byte[] serialze(Object obj, int bufferSize);

    T deserialze(Class<T> clazz, byte[] serialized);
}
