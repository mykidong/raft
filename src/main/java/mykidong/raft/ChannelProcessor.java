package mykidong.raft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class ChannelProcessor extends Thread {

    private static Logger LOG = LoggerFactory.getLogger(ChannelProcessor.class);

    private BlockingQueue<SocketChannel> socketChannelQueue;
    private BlockingQueue<Request> requestQueue;
    private NioSelector nioSelector;
    private long pollTimeout;

    public ChannelProcessor(BlockingQueue<SocketChannel> socketChannelQueue, BlockingQueue<Request> requestQueue, long pollTimeout) {
        this.socketChannelQueue = socketChannelQueue;
        this.requestQueue = requestQueue;
        this.nioSelector = NioSelector.open();
        this.pollTimeout = pollTimeout;
    }

    @Override
    public void run() {
        while (true) {
            try {
                SocketChannel socketChannel = this.socketChannelQueue.poll(pollTimeout, TimeUnit.MILLISECONDS);

                // if new connection is returned, register it to selector.
                if (socketChannel != null) {
                    String channelId = NioSelector.makeChannelId(socketChannel);
                    nioSelector.register(channelId, socketChannel, SelectionKey.OP_READ);

                    LOG.info("channelId: [{}] registered in thread: [{}]", channelId, Thread.currentThread());
                }

                int ready = this.nioSelector.select();
                // if not ready, continue.
                if (ready == 0) {
                    continue;
                }

                Iterator<SelectionKey> iter = this.nioSelector.selectedKeys().iterator();

                // all the requests and responses from the registered socket channels will be handled here!
                while (iter.hasNext()) {
                    SelectionKey key = iter.next();

                    iter.remove();

                    // handle request.
                    if (key.isReadable()) {
                        LOG.debug("handle incoming request...");
                        this.request(key);
                    }
                    // do response.
                    else if (key.isWritable()) {
                        LOG.debug("handle outgoing response...");
                        this.response(key);
                    }
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private void request(SelectionKey key) {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        // channel id.
        String channelId = NioSelector.makeChannelId(socketChannel);

        try {
            // request.
            Request request = new Request(channelId, nioSelector);

            // send request processor.
            requestQueue.put(request);
        } catch (Exception e) {
            nioSelector.removeSocketChannel(channelId);
            key.cancel();
        }
    }

    private void response(SelectionKey key) {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        // channel id.
        String channelId = NioSelector.makeChannelId(socketChannel);
        LOG.info("channelId for response: " + channelId);

        try {
            ByteBuffer buffer = (ByteBuffer) key.attachment();

            if (buffer == null) {
                return;
            }

            buffer.rewind();

            while (buffer.hasRemaining()) {
                socketChannel.write(buffer);
            }
            LOG.info("response done for channel id: " + channelId);

            buffer.clear();

            this.nioSelector.interestOps(socketChannel, SelectionKey.OP_READ);
        } catch (IOException e) {
            nioSelector.removeSocketChannel(channelId);
            key.cancel();
        }
    }
}
