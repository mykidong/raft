package mykidong.raft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;

public class SocketServer extends Thread {

    private static Logger log = LoggerFactory.getLogger(SocketServer.class);

    private Selector selector;
    private BlockingQueue<SocketChannel> socketChannelQueue;
    private boolean shutdown = false;
    private int port;

    public SocketServer(int port, BlockingQueue<SocketChannel> socketChannelQueue) {
        this.port = port;
        this.socketChannelQueue = socketChannelQueue;
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
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void accept(SelectionKey key) throws Exception {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.socket().setTcpNoDelay(true);
        socketChannel.socket().setKeepAlive(true);

        log.info("socket channel accepted: [{}]", socketChannel.socket().getRemoteSocketAddress());

        // put socket channel to read channel processor.
        this.socketChannelQueue.put(socketChannel);
    }

    public void shutdown()
    {
        shutdown = true;
    }
}
