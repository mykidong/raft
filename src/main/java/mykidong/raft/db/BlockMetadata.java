package mykidong.raft.db;

import org.joda.time.DateTimeUtils;

public class BlockMetadata {

    /**
     * term number.
     */
    private long term;

    /**
     * log index number.
     */
    private long index;

    /**
     * key path to identify the data object.
     */
    private String keyPath;

    /**
     * block file path where the block buffer will be written.
     */
    private String blockFilePath;

    /**
     * block size for the data object.
     */
    private long blockSize;

    /**
     * starting position in the block file.
     */
    private long position;

    /**
     * block data size.
     */
    private long length;

    /**
     * timestamp where the block file is created.
     */
    private long saveTimestamp;

    /**
     * status of commit.
     * default is false,
     * but if acks from majority arrived, log commit status will be set to true.
     */
    private boolean committed;

    /**
     * timestamp where log commit is executed.
     */
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
