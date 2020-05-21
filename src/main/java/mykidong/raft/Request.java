package mykidong.raft;

import java.nio.channels.SocketChannel;

public class Request {

    private String channelId;
    private NioSelector nioSelector;

    public Request(String channelId,
                   NioSelector nioSelector) {
        this.channelId = channelId;
        this.nioSelector = nioSelector;
    }

    public String getChannelId() {
        return channelId;
    }

    public NioSelector getNioSelector() {
        return nioSelector;
    }
}
