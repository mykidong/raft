package mykidong.raft.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class BaseRequestBody implements Translatable<BaseRequestBody>, Bufferable{
    private static Logger LOG = LoggerFactory.getLogger(BaseRequestBody.class);

    @Override
    public void buildBuffer() {
    }

    @Override
    public ByteBuffer toBuffer() {
        return null;
    }

    @Override
    public BaseRequestBody toObject() {
        return null;
    }
}
