package mykidong.raft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class RequestProcessor extends Thread {

    private static Logger log = LoggerFactory.getLogger(RequestProcessor.class);

    private BlockingQueue<Request> requestQueue;
    private long pollTimeout;

    public RequestProcessor(BlockingQueue<Request> requestQueue, long pollTimeout) {
        this.requestQueue = requestQueue;
        this.pollTimeout = pollTimeout;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Request request = requestQueue.poll(pollTimeout, TimeUnit.MILLISECONDS);
                if(request == null)
                {
                    log.debug("request is null!");
                    continue;
                }

                String channelId = request.getChannelId();
                log.info("channel id: [{}]", channelId);

                NioSelector nioSelector = request.getNioSelector();
                SocketChannel socketChannel = nioSelector.getSocketChannel(channelId);

                // TODO: parse request byte buffer!!!!
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

                log.info("request messages: [{}]", new String(messageBytes));

                // TODO: do response.
                byte[] responseBytes = new String("this is response from server: " + Thread.currentThread()).getBytes();
                int responseLength = responseBytes.length;
                ByteBuffer responseBuffer = ByteBuffer.allocate(4 + responseLength);
                responseBuffer.putInt(responseBytes.length);
                responseBuffer.put(responseBytes);

                nioSelector.attach(channelId, SelectionKey.OP_WRITE, responseBuffer);

                // wakeup must be called.
                nioSelector.wakeup();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
