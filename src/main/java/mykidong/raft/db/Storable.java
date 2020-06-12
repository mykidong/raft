package mykidong.raft.db;

import java.nio.ByteBuffer;

public interface Storable {
    long saveBlock(long term, long index, String keyPath, long blockSize, int blockNumber, ByteBuffer blockBuffer);
    BlockMetadata getBlockMetadata(long index);
    void saveLastIndex(long index);
    void saveLastTerm(long term);
    long getLastTerm();
    long getLastIndex();
    void deleteIndex(long index);
}
