package mykidong.raft.api;

import mykidong.raft.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class LeaderElection {
    public static class LeaderElectionRequest {
        public static class LeaderElectionRequestHeader implements Translatable<LeaderElectionRequestHeader>  {
            private static Logger LOG = LoggerFactory.getLogger(LeaderElectionRequestHeader.class);

            private Translatable<BaseRequestHeader> baseRequestHeader;
            private ByteBuffer buffer;
            public LeaderElectionRequestHeader(Translatable<BaseRequestHeader> baseRequestHeader) {
                this.baseRequestHeader = baseRequestHeader;

                buildBuffer();
            }
            public LeaderElectionRequestHeader(ByteBuffer buffer) {
                baseRequestHeader = new BaseRequestHeader(buffer);

                buildBuffer();
            }

            private void buildBuffer() {
                buffer = this.baseRequestHeader.toBuffer();
            }

            public BaseRequestHeader getBaseRequestHeader() {
                return baseRequestHeader.toObject();
            }

            @Override
            public ByteBuffer toBuffer() {
                return this.buffer;
            }

            @Override
            public LeaderElectionRequestHeader toObject() {
                return this;
            }
        }

        public static class LeaderElectionRequestBody implements Translatable<LeaderElectionRequestBody> {

            // TODo: wrong field, see the protocol docs!!!

            private String followerId;
            private ByteBuffer buffer;

            public LeaderElectionRequestBody(String followerId) {
                this.followerId = followerId;

                buildBuffer();
            }

            public LeaderElectionRequestBody(ByteBuffer buffer) {
                short size = buffer.getShort();
                byte[] followerIdBytes = new byte[size];
                buffer.get(followerIdBytes);
                this.followerId = StringUtils.toString(followerIdBytes, "UTF-8");

                buildBuffer();
            }

            private void buildBuffer() {
                byte[] followIdBytes = StringUtils.getBytes(this.followerId, "UTF-8");
                int size = followIdBytes.length;

                buffer = ByteBuffer.allocate(2 + size);
                buffer.putShort((short)size);
                buffer.put(followIdBytes);
            }

            public String getFollowerId() {
                return followerId;
            }

            @Override
            public ByteBuffer toBuffer() {
                return this.buffer;
            }

            @Override
            public LeaderElectionRequestBody toObject() {
                return this;
            }
        }

    }

    public static class LeaderElectionResponse {
        public static class LeaderElectionResponseHeader implements Translatable<LeaderElectionResponseHeader> {
            @Override
            public ByteBuffer toBuffer() {
                return null;
            }

            @Override
            public LeaderElectionResponseHeader toObject() {
                return null;
            }
        }

        public static class LeaderElectionResponseBody implements Translatable<LeaderElectionResponseBody>{
            @Override
            public ByteBuffer toBuffer() {
                return null;
            }

            @Override
            public LeaderElectionResponseBody toObject() {
                return null;
            }
        }
    }
}
