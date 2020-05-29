package mykidong.raft.api;

import java.nio.ByteBuffer;

public class BaseResponseHeader implements Translatable<BaseResponseHeader>, Bufferable{
    private int correlationId;
    private ByteBuffer buffer;

    public BaseResponseHeader(int correlationId) {
        this.correlationId = correlationId;

        buildBuffer();
    }

    public BaseResponseHeader(ByteBuffer buffer) {
        this.correlationId = buffer.getInt();

        buildBuffer();
    }

    @Override
    public void buildBuffer() {
        int size = 0;
        size += 4; // correlationId.

        buffer = ByteBuffer.allocate(size);
        buffer.putInt(correlationId);
    }

    public int getCorrelationId() {
        return correlationId;
    }

    @Override
    public ByteBuffer toBuffer() {
        return this.buffer;
    }

    @Override
    public BaseResponseHeader toObject() {
        return this;
    }
}
