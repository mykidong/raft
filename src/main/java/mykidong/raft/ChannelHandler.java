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

public class ChannelHandler extends Thread {

    private static Logger log = LoggerFactory.getLogger(ChannelHandler.class);

    private BlockingQueue<SocketChannel> socketChannelQueue;
    private BlockingQueue<Request> requestQueue;
    private NioSelector nioSelector;
    private long pollTimeout;

    public ChannelHandler(BlockingQueue<SocketChannel> socketChannelQueue, BlockingQueue<Request> requestQueue, long pollTimeout) {
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

                // if new connection is added, register it to selector.
                if (socketChannel != null) {
                    String channelId = NioSelector.makeChannelId(socketChannel);
                    nioSelector.register(channelId, socketChannel, SelectionKey.OP_READ);

                    log.info("channelId: [{}] registered, thread: [{}]", channelId, Thread.currentThread());
                }

                int ready = this.nioSelector.select();
                if (ready == 0) {
                    continue;
                }

                Iterator<SelectionKey> iter = this.nioSelector.selectedKeys().iterator();

                while (iter.hasNext()) {
                    SelectionKey key = iter.next();

                    iter.remove();

                    // handle request.
                    if (key.isReadable()) {
                        this.request(key);
                    }
                    // handle response.
                    else if (key.isWritable()) {
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
        log.info("channelId: [{}]", channelId);

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
        log.info("channelId for response: " + channelId);

        try {
            ByteBuffer buffer = (ByteBuffer) key.attachment();

            if (buffer == null) {
                return;
            }

            buffer.rewind();

            while (buffer.hasRemaining()) {
                socketChannel.write(buffer);
            }
            log.info("response done for channel id: " + channelId);

            buffer.clear();

            this.nioSelector.interestOps(socketChannel, SelectionKey.OP_READ);
        } catch (IOException e) {
            nioSelector.removeSocketChannel(channelId);
            key.cancel();
        }
    }
}
