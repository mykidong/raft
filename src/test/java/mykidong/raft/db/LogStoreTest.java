package mykidong.raft.db;

import mykidong.raft.server.ProducerWithOldSocket;
import mykidong.raft.test.TestBase;
import mykidong.raft.util.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Random;

public class LogStoreTest extends TestBase {

    private static Logger LOG = LoggerFactory.getLogger(LogStoreTest.class);

    private String dbPathForBlock = "/tmp/db/block";
    private String logPath = "/tmp/log";
    private String dbPathForLastTermAndIndex = "/tmp/db/term-index";

    @Before
    public void reset() throws Exception {
        FileUtils.deleteDirectory(dbPathForBlock);
        FileUtils.createDirectoryIfNotExists(dbPathForBlock);

        FileUtils.deleteDirectory(logPath);
        FileUtils.createDirectoryIfNotExists(logPath);

        FileUtils.deleteDirectory(dbPathForLastTermAndIndex);
        FileUtils.createDirectoryIfNotExists(dbPathForLastTermAndIndex);
    }

    @Test
    public void saveBlock() throws Exception {
        Storable storable = new LogStore(dbPathForBlock, logPath, dbPathForLastTermAndIndex);

        long term = 1;
        long index = 1;
        String keyPath = "/any/key/path";
        long blockSize = 256 * 1024 * 1024;
        int blockNumber = 0;
        Random random = new Random();
        byte[] bytes = new byte[(int)blockSize];
        random.nextBytes(bytes);
        ByteBuffer blockBuffer = ByteBuffer.allocate((int)blockSize);
        blockBuffer.put(bytes);

        // save block with metadata.
        long retIndex = storable.saveBlock(term, index, keyPath, blockSize, blockNumber, blockBuffer);
        Assert.assertEquals(retIndex, index);

        // get block metadata.
        BlockMetadata retBlockMetadata = storable.getBlockMetadata(index);
        Assert.assertEquals(term, retBlockMetadata.getTerm());
        Assert.assertEquals(index, retBlockMetadata.getIndex());
        Assert.assertEquals(0, retBlockMetadata.getPosition());
        Assert.assertEquals(blockSize, retBlockMetadata.getLength());
        Assert.assertEquals(blockSize, retBlockMetadata.getBlockSize());
        Assert.assertEquals(keyPath, retBlockMetadata.getKeyPath());
        Assert.assertFalse(retBlockMetadata.isCommitted());
        Assert.assertNotNull(retBlockMetadata.getBlockFilePath());

        long lastIndex = storable.getLastIndex();
        Assert.assertEquals(lastIndex, index);

        long lastTerm = storable.getLastTerm();
        Assert.assertEquals(term, lastTerm);

        // commit block with metadata.
        storable.commitBlock(index);

        BlockMetadata committedBlockMetadata = storable.getBlockMetadata(index);
        Assert.assertTrue(committedBlockMetadata.isCommitted());
        Assert.assertTrue(committedBlockMetadata.getCommitTimestamp() > 0);
        Assert.assertEquals(term, committedBlockMetadata.getTerm());
        Assert.assertEquals(index, committedBlockMetadata.getIndex());
        Assert.assertEquals(0, committedBlockMetadata.getPosition());
        Assert.assertEquals(blockSize, committedBlockMetadata.getLength());
        Assert.assertEquals(blockSize, committedBlockMetadata.getBlockSize());
        Assert.assertEquals(keyPath, committedBlockMetadata.getKeyPath());
        Assert.assertNotNull(committedBlockMetadata.getBlockFilePath());

        ByteBuffer retBlockBuffer = storable.getBlockBuffer(index);
        retBlockBuffer.rewind();
        Assert.assertEquals(bytes.length, retBlockBuffer.remaining());

        Assert.assertTrue(new File(retBlockMetadata.getBlockFilePath()).exists());

        // delete block file with metadata.
        storable.deleteIndex(index);

        // block file does not exist.
        Assert.assertFalse(new File(retBlockMetadata.getBlockFilePath()).exists());

        Assert.assertNull(storable.getBlockMetadata(index));
        Assert.assertTrue(storable.getLastIndex() < 0);
        Assert.assertEquals(term, storable.getLastTerm());
    }

    @Test
    public void benchmark() throws Exception {
        Storable storable = new LogStore(dbPathForBlock, logPath, dbPathForLastTermAndIndex);

        long term = 1;
        long blockSize = 256 * 1024;
        int blockNumber = 0;
        Random random = new Random();
        byte[] bytes = new byte[(int) blockSize];
        random.nextBytes(bytes);
        ByteBuffer blockBuffer = ByteBuffer.allocate((int) blockSize);
        blockBuffer.put(bytes);

        long start = System.currentTimeMillis();
        int MAX = 100;
        for(int i = 0; i < MAX; i++) {
            long index = i + 1;
            String keyPath = "/any/key/path" + i;

            // save block with metadata.
            long retIndex = storable.saveBlock(term, index, keyPath, blockSize, blockNumber, blockBuffer);
        }
        LOG.info("tps: [{}] for save...", (double)MAX / (double) (System.currentTimeMillis() - start) * 1000);


        start = System.currentTimeMillis();
        for(int i = 0; i < MAX; i++) {
            long index = i + 1;
            BlockMetadata blockMetadata = storable.getBlockMetadata(index);
        }
        LOG.info("tps: [{}] for read...", (double)MAX / (double) (System.currentTimeMillis() - start) * 1000);
    }
}
