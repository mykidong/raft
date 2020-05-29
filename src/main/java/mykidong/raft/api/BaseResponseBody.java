package mykidong.raft.api;

import java.nio.ByteBuffer;

public class BaseResponseBody implements Translatable<BaseResponseBody>{
    private short errorCode;
    private ByteBuffer buffer;

    public BaseResponseBody(short errorCode) {
        this.errorCode = errorCode;

        buildBuffer();
    }

    public BaseResponseBody(ByteBuffer buffer) {
        this.errorCode = buffer.getShort();

        buildBuffer();
    }

    private void buildBuffer() {
        int size = 0;
        size += 2; // errorCode.

        buffer = ByteBuffer.allocate(size);
        buffer.putShort(errorCode);
    }

    public short getErrorCode() {
        return errorCode;
    }

    @Override
    public ByteBuffer toBuffer() {
        return this.buffer;
    }

    @Override
    public BaseResponseBody toObject() {
        return this;
    }
}
