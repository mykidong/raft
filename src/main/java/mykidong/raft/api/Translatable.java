package mykidong.raft.api;

import java.nio.ByteBuffer;

public interface Translatable<T> {

    ByteBuffer toBuffer();
    T toObject();
}
