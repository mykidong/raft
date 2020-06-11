package mykidong.raft.db;

import java.nio.ByteBuffer;

public interface Storable {
    long saveBlock(long term, long index, String keyPath, long blockSize, int blockNumber, ByteBuffer blockBuffer);
    long getLastTerm();
    long getLastIndex();
    void saveLastIndex(long index);
    void saveLastTerm(long term);
    void deleteIndex(long index);
}
