package mykidong.raft.api;

import java.nio.ByteBuffer;

public abstract class AbstractBaseResponseBody<T> extends AbstractBaseHeaderBody<T, BaseResponseBody> {
    protected short errorCode;

    public AbstractBaseResponseBody(Translatable<BaseResponseBody> baseResponseBody) {
        super(baseResponseBody);

        errorCode = this.baseHeaderBody.toObject().getErrorCode();
    }

    public AbstractBaseResponseBody(ByteBuffer buffer) {
        super(buffer, BaseResponseBody.class);

        errorCode = this.baseHeaderBody.toObject().getErrorCode();
    }
}
