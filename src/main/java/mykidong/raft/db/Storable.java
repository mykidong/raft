package mykidong.raft.db;

import java.nio.ByteBuffer;

public interface Storable {

    /**
     * save block of file to the block file with byte buffer.
     * it also saves block metadata with the properties like
     * term, index, block file path, block size, etc. to the rocks db.
     *
     * The last index and term will be updated to the rocks db.
     *
     *
     * @param term term number.
     * @param index log index number.
     * @param keyPath key path to identify the data object.
     * @param blockSize block size for the data object.
     * @param blockNumber the number of the blocks to which the data object is split.
     * @param blockBuffer byte buffer for the block.
     * @return the number of the index of the log which is saved as block.
     */
    long saveBlock(long term, long index, String keyPath, long blockSize, int blockNumber, ByteBuffer blockBuffer);

    /**
     * get block metadata with the specified log index number from rocks db.
     *
     * @param index the number of log index.
     * @return block metadata.
     */
    BlockMetadata getBlockMetadata(long index);

    /**
     * update last index for the block.
     *
     * @param index the number of log index.
     */
    void saveLastIndex(long index);

    /**
     * update last term number if necessary.
     *
     * @param term term number.
     */
    void saveLastTerm(long term);

    /**
     * get last term number.
     *
     * @return term number.
     */
    long getLastTerm();

    /**
     * get last log index number.
     *
     * @return the number of log index.
     */
    long getLastIndex();

    /**
     * delete block log file and block metadata with the specified log index number.
     *
     * @param index the number of the log index.
     */
    void deleteIndex(long index);
}
