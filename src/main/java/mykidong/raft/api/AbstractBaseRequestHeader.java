package mykidong.raft.api;

import java.nio.ByteBuffer;

public abstract class AbstractBaseRequestHeader<H> extends AbstractBaseHeaderBody<H, BaseRequestHeader>{

    public AbstractBaseRequestHeader(Translatable<BaseRequestHeader> baseRequestHeader) {
        super(baseRequestHeader);

        buildBuffer();
    }
    public AbstractBaseRequestHeader(ByteBuffer buffer) {
        super(buffer, BaseRequestHeader.class);

        buildBuffer();
    }
}
