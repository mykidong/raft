package mykidong.raft.api;

import mykidong.raft.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class LeaderElectionRequest {
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
        private String candidateId;
        private int newTermNumber;
        private int lastTermNumber;
        private long lastLogIndexNumber;
        private ByteBuffer buffer;

        public LeaderElectionRequestBody(String candidateId,
                                         int newTermNumber,
                                         int lastTermNumber,
                                         long lastLogIndexNumber) {
            this.candidateId = candidateId;
            this.newTermNumber = newTermNumber;
            this.lastTermNumber = lastTermNumber;
            this.lastLogIndexNumber = lastLogIndexNumber;

            buildBuffer();
        }

        public LeaderElectionRequestBody(ByteBuffer buffer) {
            short candidateIdSize = buffer.getShort();
            byte[] candidateIdBytes = new byte[candidateIdSize];
            buffer.get(candidateIdBytes);
            this.candidateId = StringUtils.toString(candidateIdBytes);
            this.newTermNumber = buffer.getInt();
            this.lastTermNumber = buffer.getInt();
            this.lastLogIndexNumber = buffer.getLong();

            buildBuffer();
        }

        private void buildBuffer() {
            byte[] candidateIdBytes = StringUtils.getBytes(this.candidateId);
            int candidateIdSize = candidateIdBytes.length;

            int size = 0;
            size += (2 + candidateIdSize); // candidateId.
            size += 4; // newTermNumber.
            size += 4; // lastTermNumber.
            size += 8; // lastLogIndexNumber.

            buffer = ByteBuffer.allocate(size);
            buffer.putShort((short) candidateIdSize);
            buffer.put(candidateIdBytes);
            buffer.putInt(this.newTermNumber);
            buffer.putInt(this.lastTermNumber);
            buffer.putLong(this.lastLogIndexNumber);
        }

        public String getCandidateId() {
            return candidateId;
        }

        public int getNewTermNumber() {
            return newTermNumber;
        }

        public int getLastTermNumber() {
            return lastTermNumber;
        }

        public long getLastLogIndexNumber() {
            return lastLogIndexNumber;
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
