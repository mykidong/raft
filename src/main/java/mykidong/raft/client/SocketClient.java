package mykidong.raft.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class SocketClient extends Thread {

    private static Logger LOG = LoggerFactory.getLogger(SocketClient.class);

    private Selector selector;
    private String host;
    private int port;
    private BlockingQueue<ByteBuffer> requestBufferQueue;
    private BlockingQueue<ByteBuffer> responseBufferQueue;

    public SocketClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.requestBufferQueue = new LinkedBlockingQueue<>(1);
        this.responseBufferQueue = new LinkedBlockingQueue<>(1);
    }

    public ByteBuffer sendMessage(ByteBuffer request) {
        ByteBuffer response = null;
        try {
            this.requestBufferQueue.put(request);
            while(true) {
                if(this.responseBufferQueue.isEmpty()) {
                    response = this.responseBufferQueue.poll(10, TimeUnit.MICROSECONDS);
                } else {
                    response = this.responseBufferQueue.poll();
                }

                if(response != null) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            LOG.error(e.getMessage());
        }

        return response;
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
                } else if (key.isReadable()) {
                    this.readBytes(key);
                } else if (key.isWritable()) {
                    this.writeBytes(key);
                }
            }
        }
    }

    private void connect(SelectionKey key) {
        try {
            SocketChannel socketChannel = (SocketChannel) key.channel();
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

    private void readBytes(SelectionKey key) {
        try {
            SocketChannel socketChannel = (SocketChannel) key.channel();

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

            LOG.info("response: [{}]", new String(buffer.array()));

            // switch to write.
            socketChannel.register(this.selector, SelectionKey.OP_WRITE);
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
    }

    private void writeBytes(SelectionKey key) {
        try {
            SocketChannel socketChannel = (SocketChannel) key.channel();

            byte[] messageBytes = new String("this is client request...").getBytes();
            int messageLength = messageBytes.length;
            ByteBuffer buffer = ByteBuffer.allocate(4 + messageLength);
            buffer.putInt(messageBytes.length);
            buffer.put(messageBytes);

            buffer.rewind();

            while (buffer.hasRemaining()) {
                socketChannel.write(buffer);
            }

            // switch to read.
            socketChannel.register(this.selector, SelectionKey.OP_READ);
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
    }
}
