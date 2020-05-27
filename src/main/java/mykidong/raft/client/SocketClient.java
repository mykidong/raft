package mykidong.raft.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

public class SocketClient extends Thread {

    private static Logger LOG = LoggerFactory.getLogger(SocketClient.class);

    private Selector selector;
    private String host;
    private int port;
    private SocketChannel socketChannel;
    private boolean ready = false;

    public SocketClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public ByteBuffer sendMessage(ByteBuffer requestBuffer) {
        while (!ready)
        {
            LOG.info("client not ready...");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                LOG.error(e.getMessage());
            }
        }

        // send request message.
        send(requestBuffer);

        // get response message.
        ByteBuffer responseBuffer = receive();

        return responseBuffer;
    }

    private ByteBuffer receive() {
        try {
            // switch to read.
            socketChannel.register(this.selector, SelectionKey.OP_READ);

            // to get total size.
            ByteBuffer totalSizeBuffer = ByteBuffer.allocate(4);
            socketChannel.read(totalSizeBuffer);

            totalSizeBuffer.rewind();

            // total size.
            int totalSize = totalSizeBuffer.getInt();

            // subsequent bytes buffer.
            ByteBuffer buffer = ByteBuffer.allocate(totalSize);
            socketChannel.read(buffer);

            buffer.rewind();

            return buffer;
        } catch (ClosedChannelException e) {
            LOG.error(e.getMessage());
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }

        return null;
    }

    private void send(ByteBuffer request) {
        try {
            // switch to write.
            socketChannel.register(this.selector, SelectionKey.OP_WRITE);

            while (request.hasRemaining()) {
                socketChannel.write(request);
            }
        } catch (ClosedChannelException e) {
            LOG.error(e.getMessage());
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
    }

    private void connect() {
        try {
            selector = Selector.open();
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
            socketChannel.connect(new InetSocketAddress("localhost", port));
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
    }

    @Override
    public void run() {
        connect();

        // client is ready to request.
        this.ready = true;

        while (true) {
            try {
                int readyChannels = selector.select();
                if (readyChannels == 0) {
                    continue;
                }
            } catch (ClosedSelectorException e) {
                LOG.warn(e.getMessage());
                break;
            } catch (IOException e) {
                LOG.error(e.getMessage());
                break;
            }

            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                iter.remove();

                if (!key.isValid()) {
                    continue;
                }

                if (key.isConnectable()) {
                    this.connect(key);
                }
            }
        }
    }

    private void connect(SelectionKey key) {
        try {
            this.socketChannel = (SocketChannel) key.channel();
            socketChannel.configureBlocking(false);

            if (socketChannel.isConnectionPending()) {
                socketChannel.finishConnect();

                LOG.info("connected...");
            }

            LOG.info("connected to remote host [{}]: ", socketChannel.socket().getRemoteSocketAddress());

            // switch to write.
            socketChannel.register(this.selector, SelectionKey.OP_WRITE);
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
    }
}
