package mykidong.raft.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;

public abstract class AbstractBaseHeaderBody<T, HB> implements Translatable<T>, Bufferable {
    private static Logger LOG = LoggerFactory.getLogger(AbstractBaseHeaderBody.class);

    protected Translatable<HB> baseHeaderBody;
    protected ByteBuffer buffer;

    public AbstractBaseHeaderBody(Translatable<HB> baseHeaderBody) {
        this.baseHeaderBody = baseHeaderBody;
    }

    public AbstractBaseHeaderBody(ByteBuffer buffer, Class<HB> baseHeaderBodyClass) {
        try {
            Constructor constructor = baseHeaderBodyClass.getConstructor(ByteBuffer.class);
            this.baseHeaderBody = (Translatable) constructor.newInstance(buffer);
        } catch (InstantiationException e) {
            LOG.error(e.getMessage());
        } catch (InvocationTargetException e) {
            LOG.error(e.getMessage());
        } catch (NoSuchMethodException e) {
            LOG.error(e.getMessage());
        } catch (IllegalAccessException e) {
            LOG.error(e.getMessage());
        }
    }

    public Translatable<HB> getBaseHeaderBody() {
        return baseHeaderBody;
    }

    @Override
    public void buildBuffer() {
        buffer = this.baseHeaderBody.toBuffer();
    }

    @Override
    public ByteBuffer toBuffer() {
        return this.buffer;
    }

    @Override
    public T toObject() {
        return (T) this;
    }
}
