package mykidong.raft.server;

import com.cedarsoftware.util.io.JsonWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import mykidong.raft.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class NioSelector {

    private static Logger LOG = LoggerFactory.getLogger(NioSelector.class);

    private Selector selector;
    private Map<String, SocketChannel> channelMap;

    public static NioSelector open()
    {
        return new NioSelector();
    }

    private NioSelector()
    {
        channelMap = new ConcurrentHashMap<>();

        try {
            this.selector = Selector.open();
        }catch (IOException e)
        {
            LOG.error(e.getMessage());
        }
    }

    public void printKeys() {
        Set<SelectionKey> keys = this.selector.keys();
        List<String> currentKeys = new ArrayList<>();
        for(SelectionKey key : keys) {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            String channelId = NioSelector.makeChannelId(socketChannel);
            currentKeys.add(channelId);
        }

        LOG.debug("keys: [{}]", JsonWriter.formatJson(JsonUtils.toJson(new ObjectMapper(), currentKeys)));

        Set<SelectionKey> selectedKeys = this.selector.selectedKeys();
        List<String> currentSelectedKeys = new ArrayList<>();
        for(SelectionKey key : selectedKeys) {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            String channelId = NioSelector.makeChannelId(socketChannel);
            currentSelectedKeys.add(channelId);
        }
        LOG.debug("selectedKeys: [{}]", JsonWriter.formatJson(JsonUtils.toJson(new ObjectMapper(), currentSelectedKeys)));
    }


    public void register(String channelId, SocketChannel socketChannel, int interestOps)
    {
        this.channelMap.put(channelId, socketChannel);

        try {
            synchronized (this.selector) {
                socketChannel.register(this.selector, interestOps);
            }
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
    }


    public void removeSocketChannel(String channelId)
    {
        SocketChannel socketChannel = this.channelMap.get(channelId);

        if (socketChannel != null) {
            try {
                socketChannel.close();
            } catch (IOException e) {
                LOG.error(e.getMessage());
            }
        }
        this.channelMap.remove(channelId);
    }

    public void attach(String channelId, int interestOps, Object attachment)
    {
        SocketChannel socketChannel = this.channelMap.get(channelId);

        if (socketChannel != null) {
            this.attach(socketChannel, interestOps, attachment);
        } else {
            LOG.warn("socket channel for channelId [{}] is null.", channelId);
        }
    }


    public void attach(SocketChannel socketChannel, int interestOps, Object attachment)
    {
        try
        {
            socketChannel.register(this.selector, interestOps, attachment);
        }catch (ClosedChannelException e)
        {
            LOG.error(e.getMessage());
        }
    }

    public SelectionKey interestOps(SocketChannel socketChannel, int interestOps)
    {
        return socketChannel.keyFor(this.selector).interestOps(interestOps);
    }


    public SelectionKey interestOps(String channelId, int interestOps)
    {
        return this.channelMap.get(channelId).keyFor(this.selector).interestOps(interestOps);
    }

    public Selector wakeup()
    {
        return this.selector.wakeup();
    }

    public int select()
    {
        try {
            return this.selector.select();
        }catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public Set<SelectionKey> selectedKeys()
    {
        return this.selector.selectedKeys();
    }

    public static String makeChannelId(SocketChannel socketChannel)
    {
        String localHost = socketChannel.socket().getLocalAddress().getHostAddress();
        int localPort = socketChannel.socket().getLocalPort();
        String remoteHost = socketChannel.socket().getInetAddress().getHostAddress();
        int remotePort = socketChannel.socket().getPort();

        StringBuffer sb = new StringBuffer();
        sb.append(localHost).append(":").append(localPort).append("-").append(remoteHost).append(":").append(remotePort);

        return sb.toString();
    }
}
