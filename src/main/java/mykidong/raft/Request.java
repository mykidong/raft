package mykidong.raft;

import java.nio.channels.SocketChannel;

public class Request {

    private String channelId;
    private SocketChannel socketChannel;
    private NioSelector nioSelector;

    public Request(String channelId,
                   SocketChannel socketChannel,
                   NioSelector nioSelector) {
        this.channelId = channelId;
        this.socketChannel = socketChannel;
        this.nioSelector = nioSelector;
    }

    public String getChannelId() {
        return channelId;
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public NioSelector getNioSelector() {
        return nioSelector;
    }
}
