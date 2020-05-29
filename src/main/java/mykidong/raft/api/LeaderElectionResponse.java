package mykidong.raft.api;

import mykidong.raft.util.StringUtils;

import java.nio.ByteBuffer;

public class LeaderElectionResponse {
    public static class LeaderElectionResponseHeader implements Translatable<LeaderElectionResponseHeader> {

        private Translatable<BaseResponseHeader> baseResponseHeader;
        private ByteBuffer buffer;

        public LeaderElectionResponseHeader(Translatable<BaseResponseHeader> baseResponseHeader) {
            this.baseResponseHeader = baseResponseHeader;

            buildBuffer();
        }

        public LeaderElectionResponseHeader(ByteBuffer buffer) {
            this.baseResponseHeader = new BaseResponseHeader(buffer);

            buildBuffer();
        }

        private void buildBuffer() {
            buffer = this.baseResponseHeader.toBuffer();
        }

        public Translatable<BaseResponseHeader> getBaseResponseHeader() {
            return baseResponseHeader;
        }

        @Override
        public ByteBuffer toBuffer() {
            return this.buffer;
        }

        @Override
        public LeaderElectionResponseHeader toObject() {
            return this;
        }
    }

    public static class LeaderElectionResponseBody implements Translatable<LeaderElectionResponseBody>{

        private Translatable<BaseResponseBody> baseResponseBody;
        private String followerId;
        private ByteBuffer buffer;
        private short errorCode = -1;

        public LeaderElectionResponseBody(Translatable<BaseResponseBody> baseResponseBody,
                                          String followerId) {
            this.baseResponseBody = baseResponseBody;
            errorCode = this.baseResponseBody.toObject().getErrorCode();

            // no errors.
            if(errorCode == 0) {
                this.followerId = followerId;
            }

            buildBuffer();
        }


        public LeaderElectionResponseBody(ByteBuffer buffer) {
            this.baseResponseBody = new BaseResponseBody(buffer);
            errorCode = this.baseResponseBody.toObject().getErrorCode();

            // no errors.
            if(errorCode == 0) {
                short followerIdSize = buffer.getShort();
                byte[] followerIdBytes = new byte[followerIdSize];
                buffer.get(followerIdBytes);
                this.followerId = StringUtils.toString(followerIdBytes);
            }

            buildBuffer();
        }

        private void buildBuffer() {
            ByteBuffer baseResponseBodyBuffer = baseResponseBody.toBuffer();
            baseResponseBodyBuffer.rewind();
            int baseResponseBodySize = baseResponseBodyBuffer.remaining();

            // no error.
            if(errorCode == 0) {
                byte[] followerIdBytes = StringUtils.getBytes(this.followerId);
                int followerIdSize = followerIdBytes.length;

                int size = 0;
                size += baseResponseBodySize; // baseResponseBody.
                size += (2 + followerIdSize); // followerId.

                this.buffer = ByteBuffer.allocate(size);
                this.buffer.put(baseResponseBodyBuffer);
                this.buffer.putShort((short) followerIdSize);
                this.buffer.put(followerIdBytes);
            }
            // errors occurred.
            else {
                int size = 0;
                size += baseResponseBodySize; // baseResponseBody.

                this.buffer = ByteBuffer.allocate(size);
                this.buffer.put(baseResponseBodyBuffer);
            }
        }

        public Translatable<BaseResponseBody> getBaseResponseBody() {
            return baseResponseBody;
        }

        public String getFollowerId() {
            return followerId;
        }

        @Override
        public ByteBuffer toBuffer() {
            return this.buffer;
        }

        @Override
        public LeaderElectionResponseBody toObject() {
            return this;
        }
    }
}
