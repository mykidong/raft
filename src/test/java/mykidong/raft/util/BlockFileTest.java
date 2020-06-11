package mykidong.raft.util;

import mykidong.raft.test.TestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BlockFileTest extends TestBase {

    private static Logger LOG = LoggerFactory.getLogger(BlockFileTest.class);

    private static String blockFilePrefix = "block-";
    private static String output = "target/file-channel";
    private static String inputDir = "src/test/resources/data";
    private static String inputFile = "my-photo-new.jpg";
    private static int blockSize = 100 * 1024;

    @Before
    public void init() throws Exception {
        inputDir = System.getProperty("inputDir", inputDir);
        inputFile = System.getProperty("inputFile", inputFile);
        blockSize = Integer.valueOf(System.getProperty("blockSize", String.valueOf(blockSize)));
    }

    @Test
    public void splitAndMerge() throws Exception {

        FileUtils.deleteDirectory(output);
        FileUtils.createDirectoryIfNotExists(output);

        String file = inputDir + "/" + inputFile;
        split(file, blockSize);
        LOG.info("split to block files...");

        merge();
        LOG.info("merged to one complete file...");

        long inputSize = new RandomAccessFile(inputDir + "/" + inputFile, "r").getChannel().size();
        long outputSize = new RandomAccessFile(output + "/" + inputFile, "r").getChannel().size();

        Assert.assertEquals(inputSize, outputSize);
    }

    private void merge() throws Exception {
        File blockFileDir = new File(output);
        String[] blockFiles = blockFileDir.list();
        List<String> blockFileList = Arrays.asList(blockFiles);
        // sort block files.
        Collections.sort(blockFileList, (o1, o2) -> {
            o1 = o1.replaceAll(blockFilePrefix, "");
            o2 = o2.replaceAll(blockFilePrefix, "");
            return Integer.valueOf(o1) - Integer.valueOf(o2);
        });

        boolean append = true;
        FileChannel completeFileChannel = new FileOutputStream(output + "/" + inputFile, append).getChannel();
        for(String blockFile : blockFileList) {
            RandomAccessFile raf = new RandomAccessFile(output + "/" + blockFile, "rw");
            FileChannel fileChannel = raf.getChannel();
            long fileSize = fileChannel.size();
            ByteBuffer buffer = ByteBuffer.allocate((int)fileSize);
            fileChannel.read(buffer);

            buffer.rewind();
            while(buffer.hasRemaining()) {
                completeFileChannel.write(buffer);
            }
            fileChannel.close();

            LOG.info("block file: [{}] with the size of [{}] merged...", blockFile, fileSize);
        }
        completeFileChannel.close();
    }

    private void split(String file, long blockSize) throws Exception {
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        FileChannel fileChannel = raf.getChannel();

        long fileSize = fileChannel.size();
        LOG.info("file size: [{}]", fileSize);

        int position = 0;
        long nextPosition = position + blockSize;

        int count = 0;
        while (true) {
            if(fileSize < nextPosition) {
                long diff = fileSize - position;
                LOG.info("diff: [{}]", diff);
                ByteBuffer buffer = getMMap(fileChannel, position, diff);
                writeBlockFile(buffer, count);
                break;
            } else {
                ByteBuffer buffer = getMMap(fileChannel, position, blockSize);
                writeBlockFile(buffer, count);

                count ++;
                position += blockSize;
                nextPosition = position + blockSize;
            }
        }
    }

    private static void writeBlockFile(ByteBuffer buffer, int count) throws Exception {
        buffer.rewind();
        int bufferSize = buffer.remaining();
        String blockFileName = output + "/" + blockFilePrefix + count;
        FileChannel channel = new FileOutputStream(blockFileName, false).getChannel();
        buffer.rewind();
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
        channel.close();

        LOG.info("block file: [{}] with the size of [{}] written", blockFileName, bufferSize);
    }


    private static ByteBuffer getMMap(FileChannel fileChannel, int position, long length) {
        try {
            return fileChannel.map(FileChannel.MapMode.READ_WRITE, position, length).duplicate();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
