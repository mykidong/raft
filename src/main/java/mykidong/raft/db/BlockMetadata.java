package mykidong.raft.db;

import org.joda.time.DateTimeUtils;

public class BlockMetadata {

    private long term;
    private long index;
    private String blockFilePath;
    private int position;
    private int size;
    private long saveTimestamp;
    private boolean committed;
    private long commitTimestamp;

    public BlockMetadata() {}

    public BlockMetadata(long term,
                         long index,
                         String blockFilePath,
                         int position,
                         int size,
                         long saveTimestamp,
                         boolean committed,
                         long commitTimestamp) {
        this.term = term;
        this.index = index;
        this.blockFilePath = blockFilePath;
        this.position = position;
        this.size = size;
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

    public String getBlockFilePath() {
        return blockFilePath;
    }

    public int getPosition() {
        return position;
    }

    public int getSize() {
        return size;
    }

    public long getSaveTimestamp() {
        return saveTimestamp;
    }
}
