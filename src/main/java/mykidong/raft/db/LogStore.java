package mykidong.raft.db;

import mykidong.raft.util.FileUtils;
import mykidong.raft.util.StringUtils;
import org.joda.time.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;


/**
 * This class handles block files with metadata and last term and last index of the log which are saved on rocks db.
 * There are three rocks db tables which are handled by this class.
 *
 * Block Metadata Table: block metadata by log index number will be saved.
 * (log index number -> block metadata)
 *
 * Last Term: last term number will be updated.
 * (last term key -> last term number)
 *
 * Last Index: last log index number will be updated.
 * (last index key -> last log index number)
 *
 */

public class LogStore implements Storable {
    private static Logger LOG = LoggerFactory.getLogger(LogStore.class);

    private static final String KEY_LAST_TERM = "key-last-term";
    private static final String KEY_LAST_INDEX = "key-last-index";
    private static final String BLOCK_FILE_PREFIX =  "part-";
    private String logPath;
    private RocksDBKVStore dbBlock;
    private RocksDBKVStore dbLastTermAndIndex;
    // TODO: block size can be configured.
    private static final long BLOCK_SIZE = 256 * 1024 * 1024; // 256MB

    public LogStore(String dbPathForBlock,
                    String logPath,
                    String dbPathForLastTermAndIndex) {
        this.logPath = StringUtils.removeSuffixSlash(logPath);
        this.logPath = FileUtils.createDirectoryIfNotExists(this.logPath);
        dbBlock = RocksDBKVStore.open(FileUtils.createDirectoryIfNotExists(dbPathForBlock));
        dbLastTermAndIndex = RocksDBKVStore.open(FileUtils.createDirectoryIfNotExists(dbPathForLastTermAndIndex));
    }

    @Override
    public long saveBlock(long term,
                          long index,
                          String keyPath,
                          long blockSize,
                          int blockNumber,
                          ByteBuffer blockBuffer) {
        keyPath = StringUtils.removeSuffixSlash(keyPath);
        String blockDirPath = (!keyPath.startsWith("/")) ? this.logPath + "/" + keyPath : this.logPath + keyPath;
        FileUtils.createDirectoryIfNotExists(blockDirPath);

        String blockFileName = BLOCK_FILE_PREFIX + blockNumber;
        String blockFile = blockDirPath + "/" + blockFileName;
        FileUtils.createFileIfNotExists(blockFile);

        blockSize = (blockSize > 0) ? blockSize : BLOCK_SIZE;
        long length = writeBufferToBlockFile(blockFile, blockBuffer);

        if(index < 0) {
            index = 0;
        }
        BlockMetadata blockMetadata = new BlockMetadata(term,
                                                        index,
                                                        keyPath,
                                                        blockFile,
                                                        blockSize,
                                                        0,
                                                        length,
                                                        DateTimeUtils.currentTimeMillis(),
                                                        false,
                                                        -1);

        // save block metadata.
        dbBlock.save(String.valueOf(index), blockMetadata);
        // save last index.
        saveLastIndex(index);
        long lastTerm = getLastTerm();
        if(lastTerm != term) {
            saveLastTerm(term);
        }

        return index;
    }

    @Override
    public void commitBlock(long index) {
        BlockMetadata blockMetadata = getBlockMetadata(index);
        if(blockMetadata != null) {
            blockMetadata.commit();
            dbBlock.save(String.valueOf(index), blockMetadata);
        }
    }

    @Override
    public BlockMetadata getBlockMetadata(long index) {
        Optional<BlockMetadata> optionalBlockMetadata = dbBlock.find(String.valueOf(index), BlockMetadata.class);
        return (optionalBlockMetadata.isPresent()) ? optionalBlockMetadata.get() : null;
    }

    @Override
    public ByteBuffer getBlockBuffer(long index) {
        BlockMetadata blockMetadata = getBlockMetadata(index);
        if(blockMetadata != null) {
            String blockFilePath = blockMetadata.getBlockFilePath();
            long position = blockMetadata.getPosition();
            long length = blockMetadata.getLength();
            return FileUtils.getMMap(blockFilePath, position, length);
        }
        return null;
    }

    private long writeBufferToBlockFile(String blockFile, ByteBuffer blockBuffer) {
        int length = 0;
        try {
            FileChannel fileChannel = new FileOutputStream(blockFile, false).getChannel();

            blockBuffer.rewind();
            length = blockBuffer.remaining();

            while (blockBuffer.hasRemaining()) {
                fileChannel.write(blockBuffer);
            }
            if(fileChannel.isOpen()) {
                fileChannel.close();
                LOG.debug("file channel for [{}] closed...", blockFile);
            }

        } catch (FileNotFoundException e) {
            LOG.error(e.getMessage());
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }

        return length;
    }

    @Override
    public void saveLastIndex(long index) {
        dbLastTermAndIndex.save(KEY_LAST_INDEX, index);
    }

    @Override
    public void saveLastTerm(long term) {
        dbLastTermAndIndex.save(KEY_LAST_TERM, term);
    }

    @Override
    public void deleteIndex(long index) {
        Optional<BlockMetadata> optionalBlockMetadata = dbBlock.find(String.valueOf(index), BlockMetadata.class);
        if(optionalBlockMetadata.isPresent()) {
            BlockMetadata blockMetadata = optionalBlockMetadata.get();
            String blockFilaPath = blockMetadata.getBlockFilePath();
            try {
                // delete block file.
                Files.deleteIfExists(Paths.get(blockFilaPath));
                LOG.debug("block file [{}] deleted...", blockFilaPath);

                // delete block metadata.
                dbBlock.delete(String.valueOf(index));

                long lastIndex = getLastIndex();
                if(index == lastIndex) {
                    dbLastTermAndIndex.delete(KEY_LAST_INDEX);
                }
            } catch (IOException e) {
                e.printStackTrace();
                LOG.error(e.getMessage());
            }
        }
    }

    @Override
    public long getLastTerm() {
        Optional<Long> value = dbLastTermAndIndex.find(KEY_LAST_TERM, Long.class);
        if(value.isPresent()) {
            return value.get();
        } else {
            return -1;
        }
    }

    @Override
    public long getLastIndex() {
        Optional<Long> value = dbLastTermAndIndex.find(KEY_LAST_INDEX, Long.class);
        if(value.isPresent()) {
            return value.get();
        } else {
            return -1;
        }
    }

}
