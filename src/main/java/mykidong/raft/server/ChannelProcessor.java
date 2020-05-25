package mykidong.raft.server;

import mykidong.raft.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class ChannelProcessor extends Thread {

    private static Logger LOG = LoggerFactory.getLogger(ChannelProcessor.class);

    private BlockingQueue<SocketChannel> socketChannelQueue;
    private Selector selector;
    private long pollTimeout;

    public ChannelProcessor(long pollTimeout, int queueSize) {
        this.socketChannelQueue = new ArrayBlockingQueue<>(queueSize);        
        this.pollTimeout = pollTimeout;
        
        // create new selector.
        newSelector();
    }
    
    private void newSelector() {
        try {
            this.selector = Selector.open();
        } catch (IOException e) {
            LOG.error(e.getMessage());            
        }
    }

    public void putSocketChannel(SocketChannel socketChannel) {
        try {
            this.socketChannelQueue.put(socketChannel);
            LOG.debug("socket channel: {} put to queue", socketChannel);

            this.selector.wakeup();
        } catch (InterruptedException e) {
            LOG.error("error: " + e.getMessage());
        }
    }

    private void registerNewConnections() {
        try {
            if(!this.socketChannelQueue.isEmpty()) {
                SocketChannel socketChannel = this.socketChannelQueue.poll(this.pollTimeout, TimeUnit.MILLISECONDS);

                // if new socket channel is returned, register it to selector.
                if (socketChannel != null) {
                    try {
                        socketChannel.register(this.selector, SelectionKey.OP_READ);
                    } catch (ClosedChannelException e) {
                        LOG.error(e.getMessage());
                    }
                }
            }
        } catch (InterruptedException e) {
            LOG.error(e.getMessage());
        }
    }

    private int select() {
        try {
            int readyChannels = this.selector.select();
            LOG.debug("readyChannels: [{}]", readyChannels);

            return readyChannels;
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }

        return 0;
    }

    @Override
    public void run() {
        while (true) {
            // register new socket channel with the selector.
            registerNewConnections();

            int readyChannels = select();
            LOG.debug("readyChannels: [{}]", readyChannels);

            // print current keys info.
            if(LOG.isDebugEnabled()) {
                List<String> currentKeys = ChannelUtils.getCurrentKeys(this.selector);
                LOG.debug("keys: [{}]", JsonUtils.jsonPretty(currentKeys));
            }

            if (readyChannels == 0) {
                continue;
            }

            Iterator<SelectionKey> iter = this.selector.selectedKeys().iterator();

            // all the requests and responses from the registered socket channels will be handled here!
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                iter.remove();
                try {
                    if (!key.isValid()) {
                        continue;
                    }

                    // handle request.
                    if (key.isReadable()) {
                        this.request(key);
                    }
                    // do response.
                    else if (key.isWritable()) {
                        this.response(key);
                    }
                } catch (CancelledKeyException e) {
                    LOG.error("cancelled key error: " + e.getMessage());
                } catch (Exception e) {
                    LOG.error(e.getMessage());
                }
            }
        }
    }

    private void removeSocketChannel(SelectionKey key) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        key.cancel();
        LOG.info("channel [{}] removed from the selector...", socketChannel);

        // selector wakeup.
        selector.wakeup();
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
            socketChannel.register(this.selector, SelectionKey.OP_WRITE, new String(messageBytes) + " as a metadata");
        } catch (IOException e) {
            LOG.warn(e.getMessage());

            removeSocketChannel(key);
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
            removeSocketChannel(key);
        }
    }
}
