package mykidong.raft.api;

import mykidong.raft.util.StringUtils;

import java.nio.ByteBuffer;

public class HeartbeatRequest {

    public static final short API_ID = 1;

    public static class HeartbeatRequestHeader
            extends AbstractBaseRequestHeader<HeartbeatRequestHeader> {

        public HeartbeatRequestHeader(Translatable<BaseRequestHeader> baseRequestHeader) {
            super(baseRequestHeader);
        }
        public HeartbeatRequestHeader(ByteBuffer buffer) {
            super(buffer);
        }
    }

    public static class HeartbeatRequestBody
            extends AbstractBaseHeaderBody<HeartbeatRequestBody, BaseRequestBody> {
        private String leaderId;
     
        public HeartbeatRequestBody(String leaderId) {
            super(null);

            this.leaderId = leaderId;          

            buildBuffer();
        }

        public HeartbeatRequestBody(ByteBuffer buffer) {
            super(null);

            short leaderIdSize = buffer.getShort();
            byte[] leaderIdBytes = new byte[leaderIdSize];
            buffer.get(leaderIdBytes);
            this.leaderId = StringUtils.toString(leaderIdBytes);

            buildBuffer();
        }

        @Override
        public void buildBuffer() {
            byte[] leaderIdBytes = StringUtils.getBytes(this.leaderId);
            int leaderIdSize = leaderIdBytes.length;

            int size = 0;
            size += (2 + leaderIdSize); // leaderId.

            buffer = ByteBuffer.allocate(size);
            buffer.putShort((short) leaderIdSize);
            buffer.put(leaderIdBytes);
        }

        public String getLeaderId() {
            return leaderId;
        }
    }
}
