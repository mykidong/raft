package mykidong.raft;

import org.apache.log4j.xml.DOMConfigurator;
import org.junit.Before;
import org.junit.Test;
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


public class ProducerWithNIO {

    private static Logger LOG = LoggerFactory.getLogger(ProducerWithNIO.class);

    private Selector selector;
    private int port = 9912;
    private long count = 0;

    @Before
    public void init() throws Exception {
        // initialize log4j.
        DOMConfigurator.configure(new ProducerWithNIO().getClass().getResource("/log4j.xml"));
    }

    @Test
    public void run() throws Exception {
        selector = Selector.open();
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_CONNECT);
        socketChannel.connect(new InetSocketAddress("localhost", port));

        while (true) {
            try {
                int readyChannels = selector.select();
                if (readyChannels == 0) {
                    continue;
                }
            } catch (ClosedSelectorException e) {
                LOG.warn(e.getMessage());
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


    private void connect(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        socketChannel.configureBlocking(false);

        if (socketChannel.isConnectionPending()) {
            socketChannel.finishConnect();

            LOG.info("connected...");
        }

        LOG.info("connected to remote host [{}]: ", socketChannel.socket().getRemoteSocketAddress());

        // switch to write.
        socketChannel.register(this.selector, SelectionKey.OP_WRITE);
    }

    private void readBytes(SelectionKey key) throws IOException {
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

        // DO NOT SEND ANY MORE!
        // switch to write.
        socketChannel.register(this.selector, SelectionKey.OP_WRITE);
    }

    private void writeBytes(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        byte[] messageBytes = new String("this is client request..." + count++).getBytes();
        int messageLength = messageBytes.length;
        ByteBuffer buffer = ByteBuffer.allocate(4 + messageLength);
        buffer.putInt(messageBytes.length);
        buffer.put(messageBytes);

        buffer.rewind();

        while (buffer.hasRemaining()) {
            socketChannel.write(buffer);
        }

        // if the send count reaches to the max, close socket channel.
        if(count == 100)
        {
            socketChannel.close();
            key.cancel();
            this.selector.close();
            return;
        }

        // switch to read.
        socketChannel.register(this.selector, SelectionKey.OP_READ);
    }
}
