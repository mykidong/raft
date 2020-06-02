package mykidong.raft.client;

import java.nio.ByteBuffer;

public interface Client {
    public ByteBuffer doRequest(ByteBuffer messageBuffer);
}
