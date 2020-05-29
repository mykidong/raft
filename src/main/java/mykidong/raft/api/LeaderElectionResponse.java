package mykidong.raft.api;

import mykidong.raft.util.StringUtils;

import java.nio.ByteBuffer;

public class LeaderElectionResponse {
    public static class LeaderElectionResponseHeader
            extends AbstractBaseHeaderBody<LeaderElectionResponseHeader, BaseResponseHeader>
            implements Translatable<LeaderElectionResponseHeader> {
        public LeaderElectionResponseHeader(Translatable<BaseResponseHeader> baseResponseHeader) {
            super(baseResponseHeader);

            buildBuffer();
        }

        public LeaderElectionResponseHeader(ByteBuffer buffer) {
            super(buffer, BaseResponseHeader.class);

            buildBuffer();
        }
    }

    public static class LeaderElectionResponseBody
            extends AbstractBaseResponseBody<LeaderElectionResponseBody>
            implements Translatable<LeaderElectionResponseBody>{
        private String followerId;

        public LeaderElectionResponseBody(Translatable<BaseResponseBody> baseResponseBody,
                                          String followerId) {
            super(baseResponseBody);

            // no errors.
            if(errorCode == 0) {
                this.followerId = followerId;
            }

            buildBuffer();
        }

        public LeaderElectionResponseBody(ByteBuffer buffer) {
            super(buffer);

            // no errors.
            if(errorCode == 0) {
                short followerIdSize = buffer.getShort();
                byte[] followerIdBytes = new byte[followerIdSize];
                buffer.get(followerIdBytes);
                this.followerId = StringUtils.toString(followerIdBytes);
            }

            buildBuffer();
        }

        @Override
        public void buildBuffer() {
            ByteBuffer baseResponseBodyBuffer = baseHeaderBody.toBuffer();
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

        public String getFollowerId() {
            return followerId;
        }
    }
}
