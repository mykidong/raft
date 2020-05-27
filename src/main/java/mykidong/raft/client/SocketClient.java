package mykidong.raft.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class SocketClient extends Thread {

    private static Logger LOG = LoggerFactory.getLogger(SocketClient.class);

    private Selector selector;
    private String host;
    private int port;
    private boolean ready = false;
    private BlockingQueue<ByteBuffer> requestQueue;
    private BlockingQueue<ByteBuffer> responseQueue;

    public SocketClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.requestQueue = new LinkedBlockingQueue<>();
        this.responseQueue = new LinkedBlockingQueue<>();
    }

    private void putRequestToQueue(ByteBuffer requestBuffer) {
        try {
            this.requestQueue.put(requestBuffer);
        } catch (InterruptedException e) {
            LOG.error(e.getMessage());
        }
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

        // put request buffer to queue.
        putRequestToQueue(requestBuffer);

        // get response message.
        ByteBuffer responseBuffer = null;
        while(true) {
            if(this.responseQueue.isEmpty()) {
                try {
                    responseBuffer = responseQueue.poll(100, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    LOG.error(e.getMessage());
                }
            } else {
                responseBuffer = this.responseQueue.poll();
            }

            if(responseBuffer != null) {
                break;
            }
        }
        return responseBuffer;
    }

    private void receive(SelectionKey key) {
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

            ByteBuffer all = ByteBuffer.allocate(4 + totalSize);
            all.putInt(totalSize);
            all.put(buffer);
            all.rewind();

            this.responseQueue.put(all);

            socketChannel.register(this.selector, SelectionKey.OP_WRITE);

            LOG.debug("response: [{}]", new String(buffer.array()));
        } catch (ClosedChannelException e) {
            LOG.error(e.getMessage());
        } catch (IOException e) {
            LOG.error(e.getMessage());
        } catch (InterruptedException e) {
            LOG.error(e.getMessage());
        }
    }

    private void send(SelectionKey key) {
        try {
            SocketChannel socketChannel = (SocketChannel) key.channel();

            ByteBuffer request = null;
            while(true) {
                if(this.requestQueue.isEmpty()) {
                    try {
                        request = requestQueue.poll(100, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        LOG.error(e.getMessage());
                    }
                } else {
                    request = this.requestQueue.poll();
                }

                if(request != null) {
                    break;
                }
            }

            request.rewind();
            while (request.hasRemaining()) {
                socketChannel.write(request);
            }

            socketChannel.register(this.selector, SelectionKey.OP_READ);
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
                } else if(key.isReadable()) {
                    this.receive(key);
                } else if(key.isWritable()) {
                    this.send(key);
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
}
