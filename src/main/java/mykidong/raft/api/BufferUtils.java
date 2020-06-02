package mykidong.raft.api;

import java.nio.ByteBuffer;

public class BufferUtils {

    /**
     * get message buffer with adding total size of the api request / response buffer.
     */
    public static ByteBuffer toMessageBuffer(ByteBuffer requestResponseBuffer) {
        requestResponseBuffer.rewind();
        int size = requestResponseBuffer.remaining();

        ByteBuffer messageBuffer = ByteBuffer.allocate(4 + size);
        messageBuffer.putInt(size);
        messageBuffer.put(requestResponseBuffer);
        messageBuffer.rewind();

        return messageBuffer;
    }

    /**
     * get request / response buffer with removing total size from the message buffer.
     */
    public static ByteBuffer toRequestResponseBuffer(ByteBuffer messageBuffer) {
        messageBuffer.rewind();
        int size = messageBuffer.getInt();
        byte[] requestResponseBytes = new byte[size];
        messageBuffer.get(requestResponseBytes);

        ByteBuffer requestResponseBuffer = ByteBuffer.allocate(size);
        requestResponseBuffer.put(requestResponseBytes);
        requestResponseBuffer.rewind();

        return requestResponseBuffer;
    }
}
