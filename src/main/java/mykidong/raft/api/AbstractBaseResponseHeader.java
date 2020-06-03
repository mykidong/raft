package mykidong.raft.api;

import java.nio.ByteBuffer;

public abstract class AbstractBaseResponseHeader<H> extends AbstractBaseHeaderBody<H, BaseResponseHeader>{

    public AbstractBaseResponseHeader(Translatable<BaseResponseHeader> baseResponseHeader) {
        super(baseResponseHeader);

        buildBuffer();
    }
    public AbstractBaseResponseHeader(ByteBuffer buffer) {
        super(buffer, BaseResponseHeader.class);

        buildBuffer();
    }
}
