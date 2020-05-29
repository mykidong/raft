package mykidong.raft.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;

public class RequestResponse<H, B> implements Translatable<RequestResponse>, Bufferable {
    private static Logger LOG = LoggerFactory.getLogger(RequestResponse.class);

    private Translatable<H> header;
    private Translatable<B> body;
    private ByteBuffer buffer;

    public RequestResponse(Translatable<H> header,
                           Translatable<B> body) {
        this.header = header;
        this.body = body;

        buildBuffer();
    }

    public RequestResponse(ByteBuffer buffer, Class<H> headerClass, Class<B> bodyClass) {
        try {
            Constructor headerConstructor = headerClass.getConstructor(ByteBuffer.class);
            this.header = (Translatable) headerConstructor.newInstance(buffer);

            Constructor bodyConstructor = bodyClass.getConstructor(ByteBuffer.class);
            this.body = (Translatable) bodyConstructor.newInstance(buffer);
        } catch (NoSuchMethodException e) {
            LOG.error(e.getMessage());
        } catch (IllegalAccessException e) {
            LOG.error(e.getMessage());
        } catch (InstantiationException e) {
            LOG.error(e.getMessage());
        } catch (InvocationTargetException e) {
            LOG.error(e.getMessage());
        }

        buildBuffer();
    }

    @Override
    public void buildBuffer() {
        ByteBuffer headerBuffer = header.toBuffer();
        headerBuffer.rewind();
        int headerSize = headerBuffer.remaining();

        ByteBuffer bodyBuffer = body.toBuffer();
        bodyBuffer.rewind();
        int bodySize = bodyBuffer.remaining();

        buffer = ByteBuffer.allocate(headerSize + bodySize);
        buffer.put(headerBuffer);
        buffer.put(bodyBuffer);
    }

    public Translatable<H> getHeader() {
        return header;
    }

    public Translatable<B> getBody() {
        return body;
    }

    @Override
    public ByteBuffer toBuffer() {
        return buffer;
    }

    @Override
    public RequestResponse toObject() {
        return this;
    }
}
