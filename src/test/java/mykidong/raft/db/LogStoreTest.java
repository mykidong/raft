package mykidong.raft.db;

import mykidong.raft.test.TestBase;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Random;

public class LogStoreTest extends TestBase {

    @Test
    public void saveIndex() throws Exception {
        String dbPathForBlock = "/tmp/db/block";
        String logPath = "/tmp/log";
        String dbPathForLastTermAndIndex = "/tmp/db/term-index";

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

        long retIndex = storable.saveBlock(term, index, keyPath, blockSize, blockNumber, blockBuffer);
        Assert.assertEquals(retIndex, index);

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

        storable.deleteIndex(index);

        // block file does not exist.
        Assert.assertFalse(new File(retBlockMetadata.getBlockFilePath()).exists());

        Assert.assertNull(storable.getBlockMetadata(index));
        Assert.assertTrue(storable.getLastIndex() < 0);
        Assert.assertEquals(term, storable.getLastTerm());
    }
}
