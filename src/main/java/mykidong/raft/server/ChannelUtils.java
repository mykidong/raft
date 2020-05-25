package mykidong.raft.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ChannelUtils {

    private static Logger LOG = LoggerFactory.getLogger(ChannelUtils.class);

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

    public static List<String> getCurrentKeys(Selector selector) {
        Set<SelectionKey> keys = selector.keys();
        List<String> currentKeys = new ArrayList<>();
        for(SelectionKey key : keys) {
            if(key.channel() instanceof SocketChannel) {
                SocketChannel socketChannel = (SocketChannel) key.channel();
                String channelId = makeChannelId(socketChannel);
                currentKeys.add(channelId);
            }
        }

        return currentKeys;
    }
}
