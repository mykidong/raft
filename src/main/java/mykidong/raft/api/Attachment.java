package mykidong.raft.api;

import java.nio.ByteBuffer;

public class Attachment {
    private BaseRequestHeader baseRequestHeader;
    private ByteBuffer requestBuffer;
    private ByteBuffer responseBuffer;

    public Attachment(BaseRequestHeader baseRequestHeader, ByteBuffer requestBuffer, ByteBuffer responseBuffer) {
        this.baseRequestHeader = baseRequestHeader;
        this.requestBuffer = requestBuffer;
        this.responseBuffer = responseBuffer;
    }

    public BaseRequestHeader getBaseRequestHeader() {
        return baseRequestHeader;
    }

    public ByteBuffer getRequestBuffer() {
        return requestBuffer;
    }

    public ByteBuffer getResponseBuffer() {
        return responseBuffer;
    }
}
