package mykidong.raft.api;

import mykidong.raft.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.util.calendar.LocalGregorianCalendar;

import java.nio.ByteBuffer;

public class BaseRequestHeader implements Translatable<BaseRequestHeader>{
    private static Logger LOG = LoggerFactory.getLogger(BaseRequestHeader.class);

    private short apiId;
    private short version;
    private int messageId;
    private String clientId;
    private ByteBuffer buffer;

    public BaseRequestHeader(short apiId,
                             short version,
                             int messageId,
                             String clientId) {
        this.apiId = apiId;
        this.version = version;
        this.messageId = messageId;
        this.clientId = clientId;

        buildBuffer();
    }

    public BaseRequestHeader(ByteBuffer buffer) {
        this.apiId = buffer.getShort();
        this.version = buffer.getShort();
        this.messageId = buffer.getInt();
        short clientIdSize = buffer.getShort();
        byte[] clientIdBytes = new  byte[clientIdSize];
        buffer.get(clientIdBytes);
        this.clientId = StringUtils.toString(clientIdBytes);

        buildBuffer();
    }

    private void buildBuffer() {
        byte[] clientIdBytes = StringUtils.getBytes(clientId);

        int size = 0;
        size += 2; // api.
        size += 2; // version.
        size += 4; // messageId.
        size += (2 + clientIdBytes.length);

        buffer = ByteBuffer.allocate(size);
        buffer.putShort(apiId);
        buffer.putShort(version);
        buffer.putInt(messageId);
        buffer.putShort((short) clientIdBytes.length);
        buffer.put(clientIdBytes);
    }

    public short getApiId() {
        return apiId;
    }

    public short getVersion() {
        return version;
    }

    public int getMessageId() {
        return messageId;
    }

    public String getClientId() {
        return clientId;
    }


    @Override
    public ByteBuffer toBuffer() {
        return this.buffer;
    }

    @Override
    public BaseRequestHeader toObject() {
        return this;
    }
}
