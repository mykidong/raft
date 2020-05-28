package mykidong.raft;

import com.cedarsoftware.util.io.JsonWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import mykidong.raft.server.ChannelUtils;
import mykidong.raft.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class SingleThreadNioServer extends Thread {

    private static Logger LOG = LoggerFactory.getLogger(SingleThreadNioServer.class);

    private Selector selector;
    private boolean shutdown = false;
    private int port;

    public SingleThreadNioServer(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            this.selector = Selector.open();

            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(this.port));
            serverSocketChannel.configureBlocking(false);

            // server socket registered for accept.
            serverSocketChannel.register(this.selector, SelectionKey.OP_ACCEPT);

            while (!shutdown) {
                int readyChannels = this.selector.select();

                // print current keys info.
                if(LOG.isDebugEnabled()) printKeys();

                if (readyChannels == 0) {
                    continue;
                }

                Iterator<SelectionKey> iter = this.selector.selectedKeys().iterator();
                while (iter.hasNext()) {
                    SelectionKey key = iter.next();

                    iter.remove();

                    if (!key.isValid()) {
                        continue;
                    }

                    // handle new socket connections.
                    if (key.isAcceptable()) {
                        this.accept(key);
                    }
                    // handle request.
                    else if (key.isReadable()) {
                        LOG.debug("handle incoming request...");
                        this.request(key);
                    }
                    // do response.
                    else if (key.isWritable()) {
                        LOG.debug("handle outgoing response...");
                        this.response(key);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void printKeys() {
        Set<SelectionKey> keys = this.selector.keys();
        List<String> currentKeys = new ArrayList<>();
        for(SelectionKey key : keys) {
            if(key.channel() instanceof SocketChannel) {
                SocketChannel socketChannel = (SocketChannel) key.channel();
                String channelId = ChannelUtils.makeChannelId(socketChannel);
                currentKeys.add(channelId);
            }
        }

        LOG.debug("keys: [{}]", JsonWriter.formatJson(JsonUtils.toJson(new ObjectMapper(), currentKeys)));

        Set<SelectionKey> selectedKeys = this.selector.selectedKeys();
        List<String> currentSelectedKeys = new ArrayList<>();
        for(SelectionKey key : selectedKeys) {
            if(key.channel() instanceof SocketChannel) {
                SocketChannel socketChannel = (SocketChannel) key.channel();
                String channelId = ChannelUtils.makeChannelId(socketChannel);
                currentSelectedKeys.add(channelId);
            }
        }
        LOG.debug("selectedKeys: [{}]", JsonWriter.formatJson(JsonUtils.toJson(new ObjectMapper(), currentSelectedKeys)));
    }

    private void accept(SelectionKey key) throws Exception {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);

        LOG.info("socket channel accepted: [{}]", socketChannel.socket().getRemoteSocketAddress());

        socketChannel.register(this.selector, SelectionKey.OP_READ);
    }


    private void request(SelectionKey key) {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        try {
            ByteBuffer totalSizeBuffer = ByteBuffer.allocate(4);
            socketChannel.read(totalSizeBuffer);
            totalSizeBuffer.rewind();

            // total size.
            int totalSize = totalSizeBuffer.getInt();

            ByteBuffer buffer = ByteBuffer.allocate(totalSize);
            socketChannel.read(buffer);
            buffer.rewind();

            byte[] messageBytes = new byte[totalSize];
            buffer.get(messageBytes);

            LOG.debug("request messages: [{}]", new String(messageBytes));

            // TODO:
            //  1. parse request header and body,
            //  2. and then create metadata object for header and body.
            //  3. attache the meta data object to this channel.

            // attache some metadata.
            socketChannel.register(this.selector, SelectionKey.OP_WRITE, new String(messageBytes) + " as a metadata");
        } catch (IOException e) {
            LOG.warn(e.getMessage());
            LOG.warn("socket channel [{}] removed from selector...", socketChannel);

            key.cancel();
            this.selector.wakeup();
        }
    }

    private void response(SelectionKey key) {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        try {
            String requestMessage = (String) key.attachment();
            byte[] responseBytes = ("this is response for request [" + requestMessage + "] from server in thread [" + Thread.currentThread().toString() + "]").getBytes();

            // TODO:
            //  1. get metadata from attachment.
            //  2. PROCESS THE REQUEST WITH METADATA(header, body, params, etc)
            //  3. do response. e.g., write bytebuffer directly to the channel.

            int responseLength = responseBytes.length;
            ByteBuffer responseBuffer = ByteBuffer.allocate(4 + responseLength);
            responseBuffer.putInt(responseBytes.length);
            responseBuffer.put(responseBytes);

            responseBuffer.flip();

            while (responseBuffer.hasRemaining()) {
                socketChannel.write(responseBuffer);
            }

            socketChannel.register(this.selector, SelectionKey.OP_READ);
        } catch (IOException e) {
            LOG.error("error: " + e.getMessage());
            key.cancel();
            this.selector.wakeup();
        }
    }

    public void shutdown()
    {
        shutdown = true;
    }
}
