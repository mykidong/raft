package mykidong.raft.server;

import mykidong.raft.api.Attachment;
import mykidong.raft.api.BaseRequestHeader;
import mykidong.raft.api.BufferUtils;
import mykidong.raft.config.Configuration;
import mykidong.raft.config.Configurator;
import mykidong.raft.controller.Controllable;
import mykidong.raft.processor.Handlerable;
import mykidong.raft.processor.RequestResponseHandler;
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
    private Handlerable handlerable;
    private Configurator configurator;

    public ChannelProcessor(Configurator configurator, Controllable controllable) {
        this.configurator = configurator;
        int queueSize = (Integer) configurator.get(Configuration.NIO_SOCKET_CHANNEL_QUEUE_SIZE.getConf()).get();
        this.socketChannelQueue = new ArrayBlockingQueue<>(queueSize);
        Object pollTimeoutObj = configurator.get(Configuration.NIO_SOCKET_CHANNEL_QUEUE_POLL_TIMEOUT.getConf()).get();
        this.pollTimeout = (pollTimeoutObj instanceof Integer) ? new Long((int) pollTimeoutObj) : new Long((long) pollTimeoutObj);
        handlerable = new RequestResponseHandler(controllable);

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
                    e.printStackTrace();
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

            // base request header.
            BaseRequestHeader baseRequestHeader = new BaseRequestHeader(buffer);
            buffer.rewind();

            // handle request.
            Attachment attachment = handlerable.handleRequest(baseRequestHeader, buffer);
            socketChannel.register(this.selector, SelectionKey.OP_WRITE, attachment);
        } catch (IOException e) {
            LOG.warn(e.getMessage());

            removeSocketChannel(key);
        }
    }

    private void response(SelectionKey key) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        try {
            Attachment attachment = (Attachment) key.attachment();
            // handle response.
            ByteBuffer responseMessageBuffer = BufferUtils.toMessageBuffer(handlerable.handleResponse(attachment));

            while (responseMessageBuffer.hasRemaining()) {
                socketChannel.write(responseMessageBuffer);
            }

            socketChannel.register(this.selector, SelectionKey.OP_READ);
        } catch (IOException e) {
            LOG.error("error: " + e.getMessage());
            removeSocketChannel(key);
        }
    }
}
