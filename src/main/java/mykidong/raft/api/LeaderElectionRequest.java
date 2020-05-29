package mykidong.raft.api;

import mykidong.raft.util.StringUtils;

import java.nio.ByteBuffer;

public class LeaderElectionRequest {
    public static class LeaderElectionRequestHeader
            extends AbstractBaseHeaderBody<LeaderElectionRequestHeader, BaseRequestHeader>
            implements Translatable<LeaderElectionRequestHeader> {

        public LeaderElectionRequestHeader(Translatable<BaseRequestHeader> baseRequestHeader) {
            super(baseRequestHeader);

            buildBuffer();
        }
        public LeaderElectionRequestHeader(ByteBuffer buffer) {
            super(buffer, BaseRequestHeader.class);

            buildBuffer();
        }
    }

    public static class LeaderElectionRequestBody
            extends AbstractBaseHeaderBody<LeaderElectionRequestBody, BaseRequestBody>
            implements Translatable<LeaderElectionRequestBody>, Bufferable {
        private String candidateId;
        private int newTermNumber;
        private int lastTermNumber;
        private long lastLogIndexNumber;

        public LeaderElectionRequestBody(String candidateId,
                                         int newTermNumber,
                                         int lastTermNumber,
                                         long lastLogIndexNumber) {
            super(null);

            this.candidateId = candidateId;
            this.newTermNumber = newTermNumber;
            this.lastTermNumber = lastTermNumber;
            this.lastLogIndexNumber = lastLogIndexNumber;

            buildBuffer();
        }

        public LeaderElectionRequestBody(ByteBuffer buffer) {
            super(null);

            short candidateIdSize = buffer.getShort();
            byte[] candidateIdBytes = new byte[candidateIdSize];
            buffer.get(candidateIdBytes);
            this.candidateId = StringUtils.toString(candidateIdBytes);
            this.newTermNumber = buffer.getInt();
            this.lastTermNumber = buffer.getInt();
            this.lastLogIndexNumber = buffer.getLong();

            buildBuffer();
        }

        @Override
        public void buildBuffer() {
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
    }
}
