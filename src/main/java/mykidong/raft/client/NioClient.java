package mykidong.raft.client;

import java.nio.ByteBuffer;

public class NioClient implements Client {

    private SocketChannelClient socketChannelClient;

    public NioClient(String host, int port, long pollTimeout) {
        this.socketChannelClient = new SocketChannelClient(host, port, pollTimeout);
        this.socketChannelClient.start();
    }

    @Override
    public ByteBuffer doRequest(ByteBuffer messageBuffer) {
        return this.socketChannelClient.request(messageBuffer);
    }
}
