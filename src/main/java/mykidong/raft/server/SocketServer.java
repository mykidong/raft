package mykidong.raft.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class SocketServer extends Thread {

    private static Logger LOG = LoggerFactory.getLogger(SocketServer.class);

    private Selector selector;
    private boolean shutdown = false;
    private List<ChannelProcessor> channelProcessors;
    private int port;
    private Random random;
    private AtomicLong socketChannelCount = new AtomicLong(0);

    public SocketServer(int port, List<ChannelProcessor> channelProcessors) {
        this.port = port;
        this.channelProcessors = channelProcessors;
        this.random = new Random();
    }

    private ChannelProcessor getNextChannelProcessor() {
        long currentSocketChannelCount = socketChannelCount.getAndIncrement();
        int channelProcessorsCount = this.channelProcessors.size();

        int index = new Long(currentSocketChannelCount % channelProcessorsCount).intValue();
        LOG.debug("channel processor selected index: [{}]", index);

        return this.channelProcessors.get(index);
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

                    if (key.isAcceptable()) {
                        this.accept(key);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void accept(SelectionKey key) throws Exception {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.socket().setTcpNoDelay(true);
        socketChannel.socket().setKeepAlive(true);

        LOG.info("socket channel accepted: [{}]", socketChannel.socket().getRemoteSocketAddress());

        // put socket channel to read channel processor.
        this.getNextChannelProcessor().putSocketChannel(socketChannel);
    }

    public void shutdown()
    {
        shutdown = true;
    }
}
