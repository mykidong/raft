package mykidong.raft.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class ChannelProcessor extends Thread {

    private static Logger LOG = LoggerFactory.getLogger(ChannelProcessor.class);

    private BlockingQueue<SocketChannel> socketChannelQueue;
    private NioSelector nioSelector;
    private long pollTimeout;
    private Thread socketChannelQueueThread;

    public ChannelProcessor(long pollTimeout, int queueSize) {
        this.socketChannelQueue = new ArrayBlockingQueue<>(queueSize);
        this.nioSelector = NioSelector.open();
        this.pollTimeout = pollTimeout;

        // run thread to detect the new socket connections from queue to register them to selector.
        socketChannelQueueThread = new Thread(runSocketChannelQueueThread(this.socketChannelQueue, this.nioSelector, pollTimeout));
        socketChannelQueueThread.start();
    }

    public void putSocketChannel(SocketChannel socketChannel) {
        try {
            this.socketChannelQueue.put(socketChannel);
            LOG.debug("socket channel: {} put to queue", socketChannel);
        } catch (InterruptedException e) {
            LOG.error("error: " + e.getMessage());
        }
    }

    private Runnable runSocketChannelQueueThread(BlockingQueue<SocketChannel> socketChannelQueue, NioSelector nioSelector, long pollTimeout) {
        return () -> {
            while (true) {
                try {
                    SocketChannel socketChannel = socketChannelQueue.poll(pollTimeout, TimeUnit.MILLISECONDS);
                    LOG.debug("socket channel: [{}]", socketChannel);

                    nioSelector.wakeup();

                    nioSelector.printKeys();

                    nioSelector.wakeup();

                    // if new connection is returned, register it to selector.
                    if (socketChannel != null) {
                        String channelId = NioSelector.makeChannelId(socketChannel);

                        nioSelector.wakeup();

                        // TODO:
                        //  .....
                        //  selector does not register new socket channel !!!!
                        nioSelector.register(channelId, socketChannel, SelectionKey.OP_READ);

                        nioSelector.wakeup();

                        LOG.info("channelId: [{}] registered", channelId);
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage());
                }
            }
        };
    }


    @Override
    public void run() {
        while (true) {
            int ready = this.nioSelector.select();
            LOG.debug("ready: [{}]", ready);

            Iterator<SelectionKey> iter = this.nioSelector.selectedKeys().iterator();

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
                        LOG.debug("handle incoming request...");
                        this.request(key);
                    }
                    // do response.
                    else if (key.isWritable()) {
                        LOG.debug("handle outgoing response...");
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
        String channelId = NioSelector.makeChannelId(socketChannel);
        nioSelector.removeSocketChannel(channelId);
        key.cancel();
        nioSelector.wakeup();
    }

    private void request(SelectionKey key) {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        // channel id.
        String channelId = NioSelector.makeChannelId(socketChannel);


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

            LOG.info("request messages: [{}]", new String(messageBytes));

            // TODO: create response.

            byte[] responseBytes = ("this is response for request [" + new String(messageBytes) + "] from server in thread [" + Thread.currentThread().toString() + "]").getBytes();
            int responseLength = responseBytes.length;
            ByteBuffer responseBuffer = ByteBuffer.allocate(4 + responseLength);
            responseBuffer.putInt(responseBytes.length);
            responseBuffer.put(responseBytes);

            nioSelector.attach(channelId, SelectionKey.OP_WRITE, responseBuffer);
        } catch (IOException e) {
            LOG.warn(e.getMessage());

            removeSocketChannel(key);
            LOG.warn("socket channel [{}] removed from selector...", channelId);
        }
    }

    private void response(SelectionKey key) {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        // channel id.
        String channelId = NioSelector.makeChannelId(socketChannel);
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
            LOG.error("error: " + e.getMessage());
            removeSocketChannel(key);
        }
    }
}
