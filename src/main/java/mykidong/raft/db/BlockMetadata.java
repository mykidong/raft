package mykidong.raft.db;

import org.joda.time.DateTimeUtils;

public class BlockMetadata {

    private long term;
    private long index;
    private String keyPath;
    private String blockFilePath;
    private long blockSize;
    private long position;
    private long length;
    private long saveTimestamp;
    private boolean committed;
    private long commitTimestamp;

    public BlockMetadata() {}

    public BlockMetadata(long term,
                         long index,
                         String keyPath,
                         String blockFilePath,
                         long blockSize,
                         long position,
                         long length,
                         long saveTimestamp,
                         boolean committed,
                         long commitTimestamp) {
        this.term = term;
        this.index = index;
        this.keyPath = keyPath;
        this.blockFilePath = blockFilePath;
        this.blockSize = blockSize;
        this.position = position;
        this.length = length;
        this.saveTimestamp = saveTimestamp;
        this.committed = committed;
        this.commitTimestamp = commitTimestamp;
    }

    public boolean isCommitted() {
        return committed;
    }

    public long getCommitTimestamp() {
        return commitTimestamp;
    }

    public void commit() {
        this.committed = true;
        this.commitTimestamp = DateTimeUtils.currentTimeMillis();
    }

    public long getTerm() {
        return term;
    }

    public long getIndex() {
        return index;
    }

    public String getKeyPath() {
        return keyPath;
    }

    public String getBlockFilePath() {
        return blockFilePath;
    }

    public long getBlockSize() {
        return blockSize;
    }

    public long getPosition() {
        return position;
    }

    public long getLength() {
        return length;
    }

    public long getSaveTimestamp() {
        return saveTimestamp;
    }
}
